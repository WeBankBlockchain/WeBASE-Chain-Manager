/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.frontinterface;

import com.webank.webase.chain.mgr.contract.entity.DeployInputParam;
import com.webank.webase.chain.mgr.contract.entity.TransactionInputParam;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.frontinterface.entity.TransactionOutput;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;

public interface LedgerInterface {

    //获取账本块高
    BlockNumber getBlockNumber(Integer groupId);

    //获取账本view
    PbftView getPbftView(Integer groupId);

    //获取账本出块节点
    SealerList getSealerList(Integer groupId);

    //获取账本观察节点
    ObserverList getObserverList(Integer groupId);

    //获取节点list
    NodeIDList getNodeIDList(Integer groupId);

    //获取账本节点
    GroupPeers getGroupPeers(Integer groupId);

    //获取账本共识状态
    ConsensusStatus getConsensusStatus(Integer groupId);

    //获取账本同步状态
    SyncStatus getSyncStatus(Integer groupId);

    //获取账本系统配置
    SystemConfig getSystemConfigByKey(Integer groupId, String key);

    //获取账本中合约bin
    Code getCode(Integer groupId, String address, DefaultBlockParameter defaultBlockParameter);

    //获取账本总的交易数量
    TotalTransactionCount getTotalTransactionCount(Integer groupId);

    //通过hash获取账本区块信息
    BcosBlock getBlockByHash(Integer groupId, String blockHash, boolean returnFullTransactionObjects);

    //通过块高获取账本区块信息
    BcosBlock getBlockByNumber(
            Integer groupId, DefaultBlockParameter defaultBlockParameter, boolean returnFullTransactionObjects);

    //通过块高获取账本区块hash
    BlockHash getBlockHashByNumber(Integer groupId, DefaultBlockParameter defaultBlockParameter);

    //通过hash获取账本交易
    BcosTransaction getTransactionByHash(Integer groupId, String transactionHash);

    //通过hash获取账本交易回执
    BcosTransactionReceipt getTransactionReceipt(Integer groupId, String transactionHash);

    //获取账本Pending交易数量
    PendingTxSize getPendingTxSize(Integer groupId);

    //合约部署
    Address deployContract(DeployInputParam inputParam);

    //交易接口
    TransactionOutput sendTransaction(TransactionInputParam param);

}
