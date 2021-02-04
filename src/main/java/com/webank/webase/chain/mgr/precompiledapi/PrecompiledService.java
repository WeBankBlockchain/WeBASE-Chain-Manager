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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
                    addObserverAndSaveSealerTask(chainId, groupId, signUserId, nodeId);
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

    @Transactional
    public void addObserverAndSaveSealerTask(int chainId, int groupId, String signUserId, String nodeId) {


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

        String nodeType = consensusParam.getNodeType();
        log.info("nodeType:{}", nodeType);

        Set<String> nodeIds;
        switch (nodeType) {
            case PrecompiledUtils.NODE_TYPE_SEALER:
                nodeIds = checkBeforeAddSealer(consensusParam);
                break;
            case PrecompiledUtils.NODE_TYPE_OBSERVER:
                nodeIds = checkBeforeAddObserver(consensusParam);
                break;
            case PrecompiledUtils.NODE_TYPE_REMOVE:
                nodeIds = checkBeforeAddNodeOfRemoveType(consensusParam);
                break;
            default:
                throw new BaseException(ConstantCode.INVALID_NODE_TYPE);
        }
        log.info("success exec method[checkBeforeSetConsensusStatus]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }


    /**
     * @param param
     * @return
     */
    private Set<String> checkBeforeAddSealer(ConsensusParam param) {
        log.info("start exec method[checkBeforeAddSealer]. param:{}", JsonTools.objToString(param));

        //check nodeId exist
        Set<String> nodeIds = checkNodeIdByConsensusParam(param);

        //require nodeId is observer
        int chainId = param.getChainId();
        int groupId = param.getGroupId();
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(observerList))
            throw new BaseException(ConstantCode.NOT_FOUND_OBSERVER_NODE);
        Set<String> nodeIdIsNotObserver = nodeIds.stream().filter(node -> !observerList.contains(node)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(nodeIdIsNotObserver))
            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are not observers:%s", JsonTools.objToString(nodeIdIsNotObserver))));

        //check blockNumber
        BigInteger blockNumberOfChain = frontInterfaceService.getLatestBlockNumber(chainId, groupId);
        for (String nodeId : nodeIds) {
            BigInteger blockNumberOfNode = nodeService.getBlockNumberOfNodeOnChain(chainId, groupId, nodeId);
            if (blockNumberOfChain.subtract(blockNumberOfNode).compareTo(constantProperties.getMaxBlockDifferenceOfNewSealer()) > 0) {
                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("New consensus node block height difference is too large:found nodeId:%s blockNumberOfNode:%d blockNumberOfChain:%d ", nodeId, blockNumberOfNode, blockNumberOfChain)));
            }
        }

        log.info("success exec method[checkBeforeAddSealer]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }

    /**
     * @param param
     * @return
     */
    private Set<String> checkBeforeAddObserver(ConsensusParam param) {
        log.info("start exec method[checkBeforeAddObserver]. param:{}", JsonTools.objToString(param));

        //check nodeId exist
        Set<String> nodeIds = checkNodeIdByConsensusParam(param);

        //require nodeId is observer
        int chainId = param.getChainId();
        int groupId = param.getGroupId();
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(observerList)) {
            log.info("finish exec method[checkBeforeAddObserver]. checkBeforeAddObserver is empty");
            return nodeIds;
        }

        Set<String> nodeIdIsObserver = nodeIds.stream().filter(node -> observerList.contains(node)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(nodeIdIsObserver))
            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are observers:%s", JsonTools.objToString(nodeIdIsObserver))));

        log.info("success exec method[checkBeforeAddObserver]. nodeIds:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }


    /**
     * @param param
     * @return
     */
    private Set<String> checkBeforeAddNodeOfRemoveType(ConsensusParam param) {
        log.info("start exec method[checkBeforeAddNodeOfRemoveType]. param:{}", JsonTools.objToString(param));
        //check nodeId exist
        Set<String> nodeIds = checkNodeIdByConsensusParam(param);
        int chainId = param.getChainId();
        int groupId = param.getGroupId();

        //require nodeType is not remove
        List<String> nodesOfRemoveType = nodeService.getNodeIds(chainId, groupId, PrecompiledUtils.NODE_TYPE_REMOVE);
        if (CollectionUtils.isEmpty(nodesOfRemoveType)) {
            log.info("finish exec method[checkBeforeAddNodeOfRemoveType]. nodesOfRemoveType is empty");
            return nodeIds;
        }

        Set<String> nodeIdIsRemoveType = nodeIds.stream().filter(node -> nodesOfRemoveType.contains(node)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(nodeIdIsRemoveType))
            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are already remove:%s", JsonTools.objToString(nodeIdIsRemoveType))));

        log.info("success exec method[checkBeforeAddNodeOfRemoveType]. nodeIds:{}", JsonTools.objToString(nodeIds));
        return nodeIds;

    }


    /**
     * @param param
     * @return
     */
    private Set<String> checkNodeIdByConsensusParam(ConsensusParam param) {
        log.info("start exec method[checkNodeIdByConsensusParam]. param:{}", JsonTools.objToString(param));

        //check nodeId exist
        int chainId = param.getChainId();
        int groupId = param.getGroupId();
        List<String> nodeIdList = param.getNodeIdList();
        if (CollectionUtils.isEmpty(nodeIdList)) {
            if (StringUtils.isBlank(param.getNodeId())) {
                throw new BaseException(ConstantCode.NODE_PARAM_EMPTY);
            }
            nodeIdList.add(param.getNodeId());
        }
        nodeIdList.stream().forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));

        Set<String> nodeIds = nodeIdList.stream().collect(Collectors.toSet());
        log.info("success exec method[checkNodeIdByConsensusParam]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }
}
