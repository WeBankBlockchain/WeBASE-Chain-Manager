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
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.AddSealerAsyncParam;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.node.entity.RspAddSealerAsyncVO;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.task.TaskManager;
import com.webank.webase.chain.mgr.trans.TransService;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import com.webank.webase.chain.mgr.util.CommUtils;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
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
    @Autowired
    private TaskManager taskManager;


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
                taskManager.requireNotFoundTaskByChainAndGroupAndNode(param.getChainId(), param.getGroupId(), node);

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
    public void addObserverAndSaveSealerTask(int chainId, int groupId, String nodeId) {
        log.info("start exec method[addObserverAndSaveSealerTask] chain:{} chain:{} node:{}", chainId, groupId, nodeId);

        checkBeforeAddObserver(chainId, groupId, SetUtils.hashSet(nodeId));
        // Save it to the task table, and take it out periodically by the timed task for processing
        taskManager.saveTaskOfAddSealerNode(chainId, groupId, nodeId);

        addObserver(chainId, groupId, nodeId);
        log.info("success exec method[addObserverAndSaveSealerTask] ");

    }


    public void addSealer(int chainId, int groupId, String nodeId) {
        //signUser
        String signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();
        addSealer(chainId, groupId, signUserId, nodeId);
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
     * @param chainId
     * @param groupId
     * @param nodeId
     */
    public void addObserver(int chainId, int groupId, String nodeId) {
        //signUser
        String signUserId = userService.createAdminIfNonexistence(chainId, groupId).getSignUserId();
        addObserver(chainId, groupId, signUserId, nodeId);
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
    public Set<String> checkBeforeAddSealer(int chainId, int groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddSealer]. chainId:{} groupId:{} nodeIds:{}", chainId, groupId, JsonTools.objToString(nodeIds));

        //require nodeId is observer
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(observerList))
            throw new BaseException(ConstantCode.NOT_FOUND_OBSERVER_NODE);
        Set<String> nodeIdIsNotObserver = nodeIds.stream().filter(node -> !observerList.contains(node)).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(nodeIdIsNotObserver))
            throw new BaseException(ConstantCode.SET_CONSENSUS_STATUS_FAIL.attach(String.format("The types of these nodes are not observers:%s", JsonTools.objToString(nodeIdIsNotObserver))));

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
    private Set<String> checkBeforeAddObserver(int chainId, int groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddObserver]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIds));

        //require nodeId is observer
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(observerList)) {
            log.info("finish exec method[checkBeforeAddObserver]. observerList is empty");
            return nodeIds;
        }

        Set<String> nodeIdIsObserver = nodeIds.stream().filter(node -> observerList.contains(node)).collect(Collectors.toSet());
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
    private Set<String> checkBeforeAddNodeOfRemoveType(int chainId, int groupId, Set<String> nodeIds) {
        log.info("start exec method[checkBeforeAddObserver]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIds));

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
    private Set<String> requireAllNodeValid(int chainId, int groupId, List<String> nodeIdList) {
        log.info("start exec method[requireAllNodeValid]. chainId:{} groupId:{} nodeIdList:{}", chainId, groupId, JsonTools.objToString(nodeIdList));

        if (CollectionUtils.isEmpty(nodeIdList))
            throw new BaseException(ConstantCode.NODE_PARAM_EMPTY);

        //check nodeId exist
        nodeIdList.stream().forEach(node -> nodeService.requireNodeIdValid(chainId, groupId, node));

        Set<String> nodeIds = nodeIdList.stream().collect(Collectors.toSet());
        log.info("success exec method[requireAllNodeValid]. result:{}", JsonTools.objToString(nodeIds));
        return nodeIds;
    }
}
