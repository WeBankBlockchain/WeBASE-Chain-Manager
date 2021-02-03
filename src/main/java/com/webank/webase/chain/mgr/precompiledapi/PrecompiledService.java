/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.precompiledapi;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.PrecompiledTypes;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.trans.TransService;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import com.webank.webase.chain.mgr.util.CommUtils;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDOBSERVER;
import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDSEALER;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_REMOVE;


/**
 * Precompiled common service including management of CNS, node consensus status, CRUD based on
 * PrecompiledWithSignService
 */
@Slf4j
@Service
public class PrecompiledService {

    @Autowired
    private TransService transService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private UserService userService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ConstantProperties constantProperties;


    /**
     * @param consensusParam
     */
    public void setConsensusStatus(ConsensusParam consensusParam) {
        log.info("start exec method[setConsensusStatus] param:{}", JsonTools.objToString(consensusParam));
        //reset group list
        groupService.resetGroupList();

        //check params
        Set<String> nodeIds = checkBeforeSetConsensusStatus(consensusParam);

        //get signUserId
        int chainId = consensusParam.getChainId();
        int groupId = consensusParam.getGroupId();
        String signUserId = consensusParam.getSignUserId();
        if (StringUtils.isBlank(signUserId))
            signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();

        //setConsensusStatus
        for (String nodeId : nodeIds) {
            switch (consensusParam.getNodeType()) {
                case PrecompiledUtils.NODE_TYPE_SEALER:
                    addSealer(chainId, groupId, signUserId, nodeId);
                    break;
                case PrecompiledUtils.NODE_TYPE_OBSERVER:
                    addObserver(chainId, groupId, signUserId, nodeId);
                    break;
                case PrecompiledUtils.NODE_TYPE_REMOVE:
                    removeNode(chainId, groupId, signUserId, nodeId);
                    break;
                default:
                    log.debug("end nodeManageControl invalid node type");
                    throw new BaseException(ConstantCode.INVALID_NODE_TYPE);
            }
        }

        //reset group list
        groupService.resetGroupList();
    }


    /**
     * consensus: add sealer through webase-sign
     */
    public void addSealer(int chainId, int groupId, String signUserId, String nodeId) {
        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);

        //send transaction
        TransResultDto transResultDto = transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDSEALER, funcParams);

        //check trans's result
        CommUtils.handleTransResultDto(transResultDto);
    }

    /**
     * consensus: add observer through webase-sign
     */
    public void addObserver(int chainId, int groupId, String signUserId, String nodeId) {
        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);

        //send transaction
        TransResultDto transResultDto = transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDOBSERVER, funcParams);

        //check trans's result
        CommUtils.handleTransResultDto(transResultDto);
    }


    /**
     * consensus: remove node from list through webase-sign
     */
    public void removeNode(int chainId, int groupId, String signUserId, String nodeId) {
        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransResultDto transResultDto = null;
        try {
            transResultDto = transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                    PrecompiledTypes.CONSENSUS, FUNC_REMOVE, funcParams);
        } catch (RuntimeException e) {
            log.info("catch runtimeException", e);
            // firstly remove node that sdk connected to the node, return the request that present
            // susscces
            // because the exception is throwed by getTransactionReceipt, we need ignore it.
            if (!e.getMessage().contains("Don't send requests to this group")) {
                throw e;
            }
        }

        //check trans's result
        CommUtils.handleTransResultDto(transResultDto);
    }


    /**
     * @param consensusParam
     * @return
     */
    public Set<String> checkBeforeSetConsensusStatus(ConsensusParam consensusParam) {
        log.info("start exec method[checkBeforeSetConsensusStatus]. param:{}", JsonTools.objToString(consensusParam));

        //get nodeIds
        Set<String> nodeIds = new HashSet<>();
        nodeIds.add(consensusParam.getNodeId());
        nodeIds.removeAll(Collections.singleton(null));
        if (CollectionUtils.isEmpty(nodeIds)) {
            if (Objects.isNull(consensusParam.getAgencyId())) {
                log.warn("fail exec method[checkBeforeSetConsensusStatus]. nodeId and agencyId param both empty");
                throw new BaseException(ConstantCode.BOTH_NODE_AND_AGENCY_EMPTY);
            }
            List<TbFront> frontList = frontService.listFrontByAgency(consensusParam.getAgencyId());
            nodeIds = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
        }
        log.info("nodeIds:{}", JsonTools.objToString(nodeIds));

        //check nodeIds
        nodeIds.removeAll(Collections.singleton(null));
        if (CollectionUtils.isEmpty(nodeIds))
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);

        //check nodeId exist
        int chainId = consensusParam.getChainId();
        int groupId = consensusParam.getGroupId();
        nodeIds.stream().forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));

        //check nodeId by nodeType:sealer
        if (PrecompiledUtils.NODE_TYPE_SEALER.equals(consensusParam.getNodeType())) {
            //add sealer
            List<String> sealerList = frontInterfaceService.getSealerList(chainId, groupId);
            Set<String> existSealers = nodeIds.stream().filter(nodeId -> sealerList.contains(nodeId)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(existSealers)) {
                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("already  exist sealers:%s", JsonTools.objToString(existSealers))));
            }

            //check blockNumber
            BigInteger blockNumberOfChain = frontInterfaceService.getLatestBlockNumber(chainId, groupId);
            for (String nodeId : nodeIds) {
                BigInteger blockNumberOfNode = nodeService.getBlockNumberOfNodeOnChain(chainId, groupId, nodeId);
                if (blockNumberOfChain.subtract(blockNumberOfNode).compareTo(constantProperties.getMaxBlockDifferenceOfNewSealer()) > 0) {
                    throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("New consensus node block height difference is too large:found nodeId:%s blockNumberOfNode:%d blockNumberOfChain:%d ", nodeId, blockNumberOfNode, blockNumberOfChain)));
                }
            }

        }

        //check nodeId by nodeType:observer
        if (PrecompiledUtils.NODE_TYPE_OBSERVER.equals(consensusParam.getNodeType())) {
            //add observer
            List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
            Set<String> existObservers = nodeIds.stream().filter(nodeId -> observerList.contains(nodeId)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(existObservers)) {
                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("already  exist observer:%s", JsonTools.objToString(existObservers))));
            }
        }

        //check nodeId by nodeType:remove
        if (PrecompiledUtils.NODE_TYPE_REMOVE.equals(consensusParam.getNodeType())) {
            //remove
            List<String> groupPeers = frontInterfaceService.getGroupPeers(chainId, groupId);
            Set<String> notExistNodes = nodeIds.stream().filter(nodeId -> !groupPeers.contains(nodeId)).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(notExistNodes)) {
                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("not  exist node:%s", JsonTools.objToString(notExistNodes))));
            }
        }

        log.info("success exec method[checkBeforeSetConsensusStatus]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }
//
//    private Set<String> checkBeforeAddSealer(ConsensusParam param) {
//        log.info("start exec method[checkBeforeAddSealer]. param:{}", JsonTools.objToString(param));
//
//        //get nodeIds
//        Set<String> nodeIds = collectNodeIdByConsensusParam(param);
//
//        //query observer list
//        int chainId = param.getChainId();
//        int groupId = param.getGroupId();
//        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
//        if(){
//
//        }
//
//
//        //require nodeId is observer
//        if (StringUtils.isNotBlank(consensusParam.getNodeId())) {
//
//        }
//
//        //get nodeIds
//
//        nodeIds.add(consensusParam.getNodeId());
//        nodeIds.removeAll(Collections.singleton(null));
//        if (CollectionUtils.isEmpty(nodeIds)) {
//            if (Objects.isNull(consensusParam.getAgencyId())) {
//                log.warn("fail exec method[checkBeforeSetConsensusStatus]. nodeId and agencyId param both empty");
//                throw new BaseException(ConstantCode.BOTH_NODE_AND_AGENCY_EMPTY);
//            }
//            List<TbFront> frontList = frontService.listFrontByAgency(consensusParam.getAgencyId());
//            nodeIds = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
//        }
//        log.info("nodeIds:{}", JsonTools.objToString(nodeIds));
//
//        //check nodeIds
//        nodeIds.removeAll(Collections.singleton(null));
//        if (CollectionUtils.isEmpty(nodeIds))
//            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
//
//        //check nodeId exist
//        int chainId = consensusParam.getChainId();
//        int groupId = consensusParam.getGroupId();
//        nodeIds.stream().forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));
//
//        //check nodeId by nodeType:sealer
//        if (PrecompiledUtils.NODE_TYPE_SEALER.equals(consensusParam.getNodeType())) {
//            //add sealer
//            List<String> sealerList = frontInterfaceService.getSealerList(chainId, groupId);
//            Set<String> existSealers = nodeIds.stream().filter(nodeId -> sealerList.contains(nodeId)).collect(Collectors.toSet());
//            if (CollectionUtils.isNotEmpty(existSealers)) {
//                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("already  exist sealers:%s", JsonTools.objToString(existSealers))));
//            }
//
//            //check blockNumber
//            BigInteger blockNumberOfChain = frontInterfaceService.getLatestBlockNumber(chainId, groupId);
//            for (String nodeId : nodeIds) {
//                BigInteger blockNumberOfNode = nodeService.getBlockNumberOfNodeOnChain(chainId, groupId, nodeId);
//                if (blockNumberOfChain.subtract(blockNumberOfNode).compareTo(constantProperties.getMaxBlockDifferenceOfNewSealer()) > 0) {
//                    throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("New consensus node block height difference is too large:found nodeId:%s blockNumberOfNode:%d blockNumberOfChain:%d ", nodeId, blockNumberOfNode, blockNumberOfChain)));
//                }
//            }
//
//        }
//    }
//
//    /**
//     * @param param
//     * @return
//     */
//    private Set<String> collectNodeIdByConsensusParam(ConsensusParam param) {
//        log.info("start exec method[collectNodeIdByConsensusParam]. param:{}", JsonTools.objToString(param));
//
//        //get nodeIds
//        Set<String> nodeIds = new HashSet<>();
//        if (Objects.nonNull(param)) {
//            List<TbFront> frontList = frontService.listFrontByAgency(param.getAgencyId());
//            nodeIds = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
//        } else if (StringUtils.isNotBlank(param.getNodeId())) {
//            nodeIds.add(param.getNodeId());
//        }
//        //check nodeIds
//        nodeIds.removeAll(Collections.singleton(null));
//        log.info("nodeIds:{}", JsonTools.objToString(nodeIds));
//        if (CollectionUtils.isEmpty(nodeIds))
//            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
//
//        //check nodeId exist
//        int chainId = param.getChainId();
//        int groupId = param.getGroupId();
//        nodeIds.stream().forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));
//
//        log.info("success exec method[collectNodeIdByConsensusParam]. param:{}", JsonTools.objToString(param));
//        return nodeIds;
//    }
}
