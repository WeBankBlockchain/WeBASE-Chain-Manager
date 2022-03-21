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

import static org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled.FUNC_ADDOBSERVER;
import static org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled.FUNC_ADDSEALER;
import static org.fisco.bcos.sdk.contract.precompiled.consensus.ConsensusPrecompiled.FUNC_REMOVE;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.PrecompiledTypes;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.AddSealerAsyncParam;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.node.entity.RspAddSealerAsyncVO;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.task.TaskManager;
import com.webank.webase.chain.mgr.trans.TransService;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import io.jsonwebtoken.lang.Collections;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private FrontManager frontManager;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;


    /**
     * @param consensusParam
     */
    public void setConsensusStatus(ConsensusParam consensusParam) {
        log.info("start exec method[setConsensusStatus] param:{}", JsonTools.objToString(consensusParam));
        //reset group list
//        groupService.resetGroupList();

        //check params
        Set<String> nodeIds = checkBeforeSetConsensusStatus(consensusParam);

        //get signUserId
        String chainId = consensusParam.getChainId();
        String groupId = consensusParam.getGroupId();
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
//        groupService.resetGroupList();

        log.info("finish exec method[setConsensusStatus]");

    }


    /**
     * @param param
     */
    public RspAddSealerAsyncVO addSealerAsync(AddSealerAsyncParam param) {
        log.info("start exec method[checkAndAddSaveSealerTask] param:{}", JsonTools.objToString(param));

        //check nodeId exist
        Set<String> nodeIds = requireAllNodeValid(param.getChainId(), param.getGroupId(), param.getNodeIdList());
        //handle by node status
        Set<String> sealerNodes = new HashSet<>();
        Set<String> successNodes = new HashSet<>();
        Set<String> errorMessages = new HashSet<>();
        for (String node : nodeIds) {
            try {
                //check task
//                taskManager.requireNotFoundTaskByChainAndGroupAndNode(param.getChainId(), param.getGroupId(), node);

                //handle by type
                String nodeType = nodeService.getNodeType(param.getChainId(), param.getGroupId(), node);
                if (PrecompiledUtils.NODE_TYPE_SEALER.equals(nodeType)) {
                    sealerNodes.add(node);
                    continue;
                }

                if (PrecompiledUtils.NODE_TYPE_REMOVE.equals(nodeType)) {
                    addObserverAndSaveSealerTask(param.getChainId(), param.getGroupId(), node);
                    successNodes.add(node);
                    continue;
                }

                if (PrecompiledUtils.NODE_TYPE_OBSERVER.equals(nodeType)) {
                    taskManager.saveTaskOfAddSealerNode(param.getChainId(), param.getGroupId(), node);
                    successNodes.add(node);
                    continue;
                }
                throw new BaseException(ConstantCode.INVALID_NODE_TYPE.attach(String.format("invalid nodeType:%s", nodeType)));

            } catch (BaseException ex) {
                String msg = StringUtils.isBlank(ex.getRetCode().getAttachment()) ? ex.getMessage() : ex.getRetCode().getAttachment();
                errorMessages.add(String.format("node:%s fail:%s", node, msg));
            } catch (Exception ex) {
                errorMessages.add(String.format("node:%s fail:%s", node, ex.getMessage()));
            }
        }

        //response
        RspAddSealerAsyncVO rsp = new RspAddSealerAsyncVO();
        rsp.setSealerNodes(sealerNodes);
        rsp.setSuccessNodes(successNodes);
        rsp.setErrorMessages(errorMessages);
        rsp.setAllSuccessFlag(CollectionUtils.isEmpty(errorMessages));
        log.info("finish exec method[checkAndAddSaveSealerTask] rsp:{}", JsonTools.objToString(rsp));
        return rsp;
    }

    @Transactional
    public void addObserverAndSaveSealerTask(String chainId, String groupId, String nodeId) {
        log.info("start exec method[addObserverAndSaveSealerTask] chain:{} chain:{} node:{}", chainId, groupId, nodeId);

//        checkBeforeAddObserver(chainId, groupId, SetUtils.hashSet(nodeId));
        // Save it to the task table, and take it out periodically by the timed task for processing
        taskManager.saveTaskOfAddSealerNode(chainId, groupId, nodeId);

        addObserver(chainId, groupId, nodeId);

        log.info("success exec method[addObserverAndSaveSealerTask] ");

    }


    public void addSealer(String chainId, String groupId, String nodeId) {
        //signUser
        String signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();
        addSealer(chainId, groupId, signUserId, nodeId);
    }


    /**
     * consensus: add sealer through webase-sign
     */
    public void addSealer(String chainId, String groupId, String signUserId, String nodeId) {
        log.info("start addSealer chainId:{} groupId:{} nodeId:{}", chainId, groupId, nodeId);
        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);

        //generate group.x.genesis group.x.ini
        groupService.generateExistGroupToSingleNode(chainId, groupId, nodeId);

        //send transaction
        TransResultDto transResultDto = transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDSEALER, funcParams);

        //check trans's result
        TransactionReceipt recoverReceipt = new TransactionReceipt();
        BeanUtils.copyProperties(transResultDto, recoverReceipt);
        this.handleTransactionReceipt(recoverReceipt);
        log.info("end addSealer recoverReceipt:{}", recoverReceipt);

        //start group
        groupService.startGroupIfNotRunning(chainId, nodeId, groupId);
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeId
     */
    public void addObserver(String chainId, String groupId, String nodeId) {
        //signUser
        String signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();
        addObserver(chainId, groupId, signUserId, nodeId);
    }


    /**
     * consensus: add observer through webase-sign
     */
    public void addObserver(String chainId, String groupId, String signUserId, String nodeId) {
        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);

        //generate group.x.genesis group.x.ini
        groupService.generateExistGroupToSingleNode(chainId, groupId, nodeId);

        //send transaction
        TransResultDto transResultDto = transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDOBSERVER, funcParams);

        //check trans's result
        TransactionReceipt recoverReceipt = new TransactionReceipt();
        BeanUtils.copyProperties(transResultDto, recoverReceipt);
        this.handleTransactionReceipt(recoverReceipt);

        //start group
        groupService.startGroupIfNotRunning(chainId, nodeId, groupId);
    }


    /**
     * consensus: remove node from list through webase-sign
     */
    public void removeNode(String chainId, String groupId, String signUserId, String nodeId) {
        log.info("start method [removeNode] chainId:{} groupId：{} signUserId：{} nodeId：{}", chainId, groupId, signUserId, nodeId);

        // params
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransResultDto transResultDto = null;
        try {
            //判断节点id是否等于对应front的id（防止tbaas的nginx错发到其他节点去）
            TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
            SyncStatus syncStatus = frontInterfaceService.getSyncStatusFromSpecificFront(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(), groupId);
            log.info("nodeId:{} syncStatus:{}", nodeId, JsonTools.objToString(syncStatus));
            Optional.ofNullable(syncStatus).map(s -> {
                if (!StringUtils.equals(nodeId, syncStatus.getNodeId()))
                    throw new BaseException(ConstantCode.NODE_ID_NOT_MATCH.attach(String.format("handle node:%s but front response:%s", nodeId, syncStatus.getNodeId())));
                return nodeId;
            }).orElseThrow(() -> new BaseException(ConstantCode.NODE_ID_NOT_MATCH.attach("result of syncStatus is null")));


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
        TransactionReceipt recoverReceipt = new TransactionReceipt();
        BeanUtils.copyProperties(transResultDto, recoverReceipt);
        this.handleTransactionReceipt(recoverReceipt);

        //remove front-group map
        frontGroupMapService.removeByChainAndGroupAndNode(chainId, groupId, nodeId);

        //stop
        groupService.stopGroupIfRunning(chainId, nodeId, groupId);
    }


    /**
     * @param param
     * @return
     */
    public Set<String> checkBeforeSetConsensusStatus(ConsensusParam param) {
        log.info("start exec method[checkBeforeSetConsensusStatus]. param:{}", JsonTools.objToString(param));

        String nodeType = param.getNodeType();
        log.info("nodeType:{}", nodeType);

        //check nodeId exist
        Set<String> nodeIds = requireAllNodeValid(param);

        Set<String> successNodes;
        switch (nodeType) {
            case PrecompiledUtils.NODE_TYPE_SEALER:
                successNodes = checkBeforeAddSealer(param.getChainId(), param.getGroupId(), nodeIds);
                break;
            case PrecompiledUtils.NODE_TYPE_OBSERVER:
                successNodes = checkBeforeAddObserver(param.getChainId(), param.getGroupId(), nodeIds);
                break;
            case PrecompiledUtils.NODE_TYPE_REMOVE:
                successNodes = checkBeforeAddNodeOfRemoveType(param.getChainId(), param.getGroupId(), nodeIds);
                break;
            default:
                throw new BaseException(ConstantCode.INVALID_NODE_TYPE);
        }
        log.info("success exec method[checkBeforeSetConsensusStatus]. result:{}", JsonTools.objToString(successNodes));
        return successNodes;
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeIds
     * @return
     */
    public Set<String> checkBeforeAddSealer(String chainId, String groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddSealer]. chainId:{} groupId:{} nodeIds:{}", chainId, groupId, JsonTools.objToString(nodeIds));

        //require nodeId is observer
//        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
//        if (CollectionUtils.isEmpty(observerList))
//            throw new BaseException(ConstantCode.NOT_FOUND_OBSERVER_NODE);
//        Set<String> nodeIdIsNotObserver = nodeIds.stream().filter(node -> !observerList.contains(node)).collect(Collectors.toSet());
//        if (CollectionUtils.isNotEmpty(nodeIdIsNotObserver))
//            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are not observers:%s", JsonTools.objToString(nodeIdIsNotObserver))));

        //check blockNumber
        SyncStatus syncStatus = frontInterfaceService.getSyncStatus(chainId, groupId);
        BigInteger blockNumberOfChain = syncStatus.getBlockNumber();
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
     * @param chainId
     * @param groupId
     * @param nodeIds
     * @return
     */
    private Set<String> checkBeforeAddObserver(String chainId, String groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddObserver]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIds));

        //require nodeId is observer
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(observerList)) {
            log.info("finish exec method[checkBeforeAddObserver]. observerList is empty");
            return nodeIds;
        }

        Set<String> nodeIdIsObserver = nodeIds.stream().filter(observerList::contains).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(nodeIdIsObserver))
            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are observers:%s", JsonTools.objToString(nodeIdIsObserver))));

        log.info("success exec method[checkBeforeAddObserver]. nodeIds:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeIds
     * @return
     */
    private Set<String> checkBeforeAddNodeOfRemoveType(String chainId, String groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddObserver]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIds));

        //can not remove all sealer nodes
        List<String> nodesOfSealerType = nodeService.getNodeIds(chainId, groupId, PrecompiledUtils.NODE_TYPE_SEALER);
        if (Collections.size(nodeIds) >= Collections.size(nodesOfSealerType)) {
            long inputSealerNodesCount = nodeIds.stream().filter(nodesOfSealerType::contains).count();
            log.info("inputSealerNodesCount:{}", inputSealerNodesCount);
            if (inputSealerNodesCount >= Collections.size(nodesOfSealerType))
                throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("can not remove all sealer, input:%s foundSealerList:%s", JsonTools.objToString(nodeIds), JsonTools.objToString(nodesOfSealerType))));
        }

        //require nodeType is not remove
        List<String> nodesOfRemoveType = nodeService.getNodeIds(chainId, groupId, PrecompiledUtils.NODE_TYPE_REMOVE);
        if (CollectionUtils.isEmpty(nodesOfRemoveType)) {
            log.info("finish exec method[checkBeforeAddNodeOfRemoveType]. nodesOfRemoveType is empty");
            return nodeIds;
        }

        Set<String> nodeIdIsNotRemoveType = nodeIds.stream().filter(node -> !nodesOfRemoveType.contains(node)).collect(Collectors.toSet());
        log.info("success exec method[checkBeforeAddNodeOfRemoveType]. nodeIdIsNotRemoveType:{}", JsonTools.objToString(nodeIdIsNotRemoveType));
        return nodeIdIsNotRemoveType;
    }


    /**
     * @param param
     * @return
     */
    public Set<String> requireAllNodeValid(ConsensusParam param) {
        List<String> nodeIdList = param.getNodeIdList();
        if (CollectionUtils.isEmpty(nodeIdList)) {
            if (StringUtils.isBlank(param.getNodeId())) {
                throw new BaseException(ConstantCode.NODE_PARAM_EMPTY);
            }
            nodeIdList.add(param.getNodeId());
        }
        return requireAllNodeValid(param.getChainId(), param.getGroupId(), nodeIdList);
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeIdList
     * @return
     */
    private Set<String> requireAllNodeValid(String chainId, String groupId, List<String> nodeIdList) {
        log.info("start exec method[requireAllNodeValid]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIdList));

        if (CollectionUtils.isEmpty(nodeIdList))
            throw new BaseException(ConstantCode.NODE_PARAM_EMPTY);

        //check nodeId exist
        nodeIdList.forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));

        Set<String> nodeIds = new HashSet<>(nodeIdList);
        log.info("success exec method[requireAllNodeValid]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }


    /**
     * Remove the node, if it is the last node, it will stop the group.
     * TODO  这里的逻辑复杂，需要重构
     *
     * @param agencyId
     * @param chainId
     * @param groupId
     */
    public void removeAgencyFromGroup(int agencyId, String chainId, String groupId) {
        log.info("start exec method[removeAgencyFromGroup] agencyId:{} chainId:{} groupId:{}", agencyId, chainId, groupId);
        groupService.requireFoundGroupByChainAndGroup(agencyId, chainId, groupId);

        List<TbFront> frontList = frontService.selectFrontListByGroupId(chainId, groupId);
        log.info("chain:{} group:{} frontList:{}", chainId, groupId, JsonTools.objToString(frontList));

        List<TbFront> frontOfAgencyOnDb = CollectionUtils.emptyIfNull(frontList).stream().filter(front -> front.getExtAgencyId() == agencyId).distinct().collect(Collectors.toList());
        log.info("frontOfAgencyOnDb:{}", JsonTools.objToString(frontOfAgencyOnDb));
        if (CollectionUtils.isEmpty(frontOfAgencyOnDb))
            throw new BaseException(ConstantCode.FRONT_LIST_NOT_FOUNT.attach("not found front by chain:" + chainId + " and agency:" + agencyId + " and group:" + groupId));

        List<String> allPeersOnGroup = new ArrayList<>();
        for (TbFront front : frontOfAgencyOnDb) {
            try {
                allPeersOnGroup = frontInterfaceService.getGroupPeersFromSpecificFront(front.getFrontPeerName(), front.getFrontIp(), front.getFrontPort(), groupId);
                break;
            } catch (BaseException ex) {
                log.warn("query peer fail for BaseException", ex);
                if (101004 == ex.getRetCode().getCode()) {
                    log.info("front:{} already on group:{}", front.getFrontIp(), groupId);
                    frontGroupMapService.removeByChainAndGroupAndNode(chainId, groupId, front.getNodeId());
                    groupService.stopGroupIfRunning(chainId, front.getNodeId(), groupId);
                }
            } catch (Exception ex) {
                log.warn("query peer fail", ex);
            }
            continue;
        }

        if (CollectionUtils.isEmpty(allPeersOnGroup))
            throw new BaseException(ConstantCode.NOT_FOUND_VALID_NODE);

        //remove
        final List<String> allPeersOnGroupFinal = allPeersOnGroup;
        List<Integer> frontIdsNotInGroup = frontList.stream()
                .filter(front -> !allPeersOnGroupFinal.contains(front.getNodeId()))
                .map(TbFront::getFrontId)
                .distinct()
                .collect(Collectors.toList());
        frontGroupMapService.removeByFrontListAndChain(chainId, frontIdsNotInGroup);
        frontGroupMapCache.clearMapList(chainId);

        List<String> nodesFromDbByAgency = frontOfAgencyOnDb.stream().map(TbFront::getNodeId).collect(Collectors.toList());
        List<String> peersOfAgency = allPeersOnGroup.stream().filter(nodesFromDbByAgency::contains).distinct().collect(Collectors.toList());
        String signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();
        log.info("peersOfAgency:{}", JsonTools.objToString(peersOfAgency));
        for (String nodeId : peersOfAgency) {
            try {
                removeNode(chainId, groupId, signUserId, nodeId);
            } catch (BaseException ex) {
                List<Integer> lastSealerArray = Arrays.asList(-51101, 51101, 100);
                if (!lastSealerArray.contains(ex.getRetCode().getCode()))
                    throw ex;
                log.info("remove the last node:{}", nodeId);
                //stop
                groupService.stopGroupIfRunning(chainId, nodeId, groupId);
                //remove front-group map
                frontGroupMapService.removeByChainAndGroupAndNode(chainId, groupId, nodeId);
            }
        }
        log.info("success exec method[removeAgencyFromGroup] agencyId:{} chainId:{} groupId:{}", agencyId, chainId, groupId);
    }

    /**
     * handle receipt of precompiled
     * @related: PrecompiledRetCode and ReceiptParser
     * return: {"code":1,"msg":"Success"} => {"code":0,"message":"Success"}
     */
    private String handleTransactionReceipt(TransactionReceipt receipt) {
        log.debug("handle tx receipt of precompiled");
        try {
            RetCode sdkRetCode = ReceiptParser.parseTransactionReceipt(receipt);
            log.info("handleTransactionReceipt sdkRetCode:{}", sdkRetCode);
            if (sdkRetCode.getCode() >= 0) {
                return new BaseResponse(ConstantCode.SUCCESS, sdkRetCode.getMessage()).toString();
            } else {
                throw new BaseException(sdkRetCode.getCode(), sdkRetCode.getMessage());
            }
        } catch (ContractException e) {
            log.error("handleTransactionReceipt e:[]", e);
            throw new BaseException(e.getErrorCode(), e.getMessage());
        }
    }

}


