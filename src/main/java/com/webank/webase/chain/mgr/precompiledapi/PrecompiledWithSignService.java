/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.chain.mgr.precompiledapi;


import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.PrecompiledTypes;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.trans.TransService;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDOBSERVER;
import static org.fisco.bcos.web3j.precompile.consensus.Consensus.FUNC_ADDSEALER;
import static org.fisco.bcos.web3j.precompile.permission.Permission.FUNC_REMOVE;

/**
 * send raw transaction through webase-sign to call precompiled
 *
 * @author marsli
 */
@Slf4j
@Service
public class PrecompiledWithSignService {

    @Autowired
    private TransService transService;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private UserService userService;


    public void setConsensusStatus(ConsensusParam consensusParam) {
        log.info("start exec method[setConsensusStatus] param:{}", JsonTools.objToString(consensusParam));

        //get nodeIds
        Set<String> nodeIds = SetUtils.hashSet(consensusParam.getNodeId());
        if (CollectionUtils.isEmpty(nodeIds)) {
            if (Objects.isNull(consensusParam.getAgencyId())) {
                log.warn("fail exec method[setConsensusStatus]. nodeId and agencyId param both empty");
                throw new BaseException(ConstantCode.BOTH_NODE_AND_AGENCY_EMPTY);
            }
            List<TbFront> frontList = frontService.listFrontByAgency(consensusParam.getAgencyId());
            nodeIds = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(nodeIds))
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);

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
                case PrecompiledUtils.NODE_TYPE_OBSERVER:
                    addObserver(chainId, groupId, signUserId, nodeId);
                case PrecompiledUtils.NODE_TYPE_REMOVE:
                    removeNode(chainId, groupId, signUserId, nodeId);
                default:
                    log.debug("end nodeManageControl invalid node type");
                    throw new BaseException(ConstantCode.INVALID_NODE_TYPE);
            }
        }

    }


    /**
     * consensus: add sealer through webase-sign
     */
    public String addSealer(int chainId, int groupId, String signUserId, String nodeId) {
        // check node id
        if (!isValidNodeID(nodeId)) {
            try {
                return PrecompiledCommon.transferToJson(PrecompiledCommon.P2pNetwork);
            } catch (IOException e) {
                throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
            }
        }
        List<String> sealerList = frontInterfaceService.getSealerList(chainId, groupId);
        if (sealerList.contains(nodeId)) {
            try {
                return PrecompiledCommon.transferToJson(PrecompiledCommon.SealerList);
            } catch (IOException e) {
                throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
            }
        }
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        return transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDSEALER, funcParams);
    }

    /**
     * consensus: add observer through webase-sign
     */
    public String addObserver(int chainId, int groupId, String signUserId, String nodeId) {
        // check node id
        if (!isValidNodeID(nodeId)) {
            try {
                return PrecompiledCommon.transferToJson(PrecompiledCommon.P2pNetwork);
            } catch (IOException e) {
                throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
            }
        }
        List<String> observerList = frontInterfaceService.getObserverList(chainId, groupId);
        if (observerList.contains(nodeId)) {
            try {
                return PrecompiledCommon.transferToJson(PrecompiledCommon.ObserverList);
            } catch (IOException e) {
                throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
            }
        }
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransactionReceipt receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                PrecompiledTypes.CONSENSUS, FUNC_ADDOBSERVER, funcParams);
        return this.handleTransactionReceipt(receipt, web3ApiService.getWeb3j(groupId));
    }

    /**
     * consensus: remove node from list through webase-sign
     */
    public String removeNode(int chainId, int groupId, String signUserId, String nodeId) {
        List<String> groupPeers = frontInterfaceService.getGroupPeers(chainId, groupId);
        if (!groupPeers.contains(nodeId)) {
            try {
                return PrecompiledCommon.transferToJson(PrecompiledCommon.GroupPeers);
            } catch (IOException e) {
                throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
            }
        }
        // trans
        List<Object> funcParams = new ArrayList<>();
        funcParams.add(nodeId);
        TransactionReceipt receipt = new TransactionReceipt();
        try {
            receipt = (TransactionReceipt) transService.transHandleWithSignForPrecompile(chainId, groupId, signUserId,
                    PrecompiledTypes.CONSENSUS, FUNC_REMOVE, funcParams);
        } catch (RuntimeException e) {
            // firstly remove node that sdk connected to the node, return the request that present
            // susscces
            // because the exception is throwed by getTransactionReceipt, we need ignore it.
            if (e.getMessage().contains("Don't send requests to this group")) {
                try {
                    return PrecompiledCommon.transferToJson(0);
                } catch (IOException ex) {
                    throw new BaseException(ConstantCode.PRECOMPILED_COMMON_TRANSFER_JSON_FAIL);
                }
            } else {
                throw e;
            }
        }
        return this.handleTransactionReceipt(receipt, web3ApiService.getWeb3j(groupId));
    }

    /**
     * check node id
     */
    private boolean isValidNodeID(String _nodeID) {
        boolean flag = false;
        List<String> nodeIDs = web3ApiService.getNodeIdList();
        for (String nodeID : nodeIDs) {
            if (_nodeID.equals(nodeID)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

}
