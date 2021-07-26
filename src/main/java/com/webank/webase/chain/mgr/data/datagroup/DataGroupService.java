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
package com.webank.webase.chain.mgr.data.datagroup;

import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseQueryParam;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.data.block.entity.BlockListParam;
import com.webank.webase.chain.mgr.data.block.entity.TbBlock;
import com.webank.webase.chain.mgr.data.datagroup.entity.ContractInfoDto;
import com.webank.webase.chain.mgr.data.datagroup.entity.GroupGeneral;
import com.webank.webase.chain.mgr.data.table.TableService;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.data.transaction.entity.TransListParam;
import com.webank.webase.chain.mgr.data.txndaily.TxnDailyService;
import com.webank.webase.chain.mgr.data.txndaily.entity.TbTxnDaily;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import java.math.BigInteger;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * services for group data.
 */
@Log4j2
@Service
public class DataGroupService {

    @Autowired
    private DataGroupMapper dataGroupMapper;
    @Autowired
    private TxnDailyService txnDailyService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private TableService tableService;
    @Autowired
    private GroupService groupService;

    /**
     * refreshSubTable
     */
    @Async("asyncExecutor")
    public synchronized void refreshSubTable() {
        log.info("refreshSubTable.");
        // get group list
        List<TbGroup> groupList = groupService.getGroupList(null, DataStatus.NORMAL.getValue());
        // create sub table
        groupList.forEach(group -> tableService.newSubTable(group.getChainId(), group.getGroupId()));
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(Integer chainId, Integer groupId) throws BaseException {
        NodeParam nodeParam = new NodeParam();
        nodeParam.setChainId(chainId);
        nodeParam.setGroupId(groupId);
        int nodeCount = nodeService.countOfNode(nodeParam);
        ContractParam contractParam = new ContractParam();
        contractParam.setChainId(chainId);
        contractParam.setGroupId(groupId);
        contractParam.setContractStatus((int)ContractStatus.DEPLOYED.getValue());
        int contractCount = contractService.countOfContract(contractParam);
        // getGeneral
        GroupGeneral generalInfo = dataGroupMapper.getGeneral(chainId, groupId);
        if (generalInfo == null) {
            return new GroupGeneral(chainId, groupId, nodeCount, contractCount);
        }
        BaseQueryParam queryParam = new BaseQueryParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        generalInfo.setNodeCount(nodeCount);
//        generalInfo.setUserCount(countOfUser(queryParam));
        generalInfo.setContractCount(contractCount);
        return generalInfo;
    }

    public List<TbTxnDaily> getTransDaily(Integer chainId, Integer groupId) {
        try {
            List<TbTxnDaily> listTrans = txnDailyService.listSeventDayOfTrans(chainId, groupId);
            return listTrans;
        } catch (RuntimeException ex) {
            log.error("fail getTransDaily. ", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of all block counts.
     */
    public BigInteger getBlockCounts() throws BaseException {
        try {
            BigInteger count = dataGroupMapper.getBlockCounts();
            return count == null ? BigInteger.ZERO : count;
        } catch (RuntimeException ex) {
            log.error("fail getBlockCounts. ", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of all transaction counts.
     */
    public BigInteger getTxnCounts() throws BaseException {
        try {
            BigInteger count = dataGroupMapper.getTxnCounts();
            return count == null ? BigInteger.ZERO : count;
        } catch (RuntimeException ex) {
            log.error("fail getTxnCounts. ", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of block.
     */
    public int countOfBlock(BlockListParam queryParam) throws BaseException {
        try {
            Integer count = dataGroupMapper.countOfBlock(queryParam.getChainId(), queryParam.getGroupId(), queryParam);
            return count == null ? 0 : count;
        } catch (RuntimeException ex) {
            log.error("fail countOfBlock queryParam:{} ", queryParam, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query block info list.
     */
    public List<TbBlock> queryBlockList(BlockListParam queryParam) throws BaseException {
        try {
            List<TbBlock> listOfBlock = dataGroupMapper.queryBlockList(queryParam.getChainId(), queryParam.getGroupId(),
                    queryParam);
            return listOfBlock;
        } catch (RuntimeException ex) {
            log.error("fail queryBlockList queryParam:{}", queryParam, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of trans.
     */
    public int countOfTrans(TransListParam queryParam) throws BaseException {
        try {
            Integer count = dataGroupMapper.countOfTrans(queryParam.getChainId(), queryParam.getGroupId(), queryParam);
            return count == null ? 0 : count;
        } catch (RuntimeException ex) {
            log.error("fail countOfTrans.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query trans list.
     */
    public List<TbTransaction> queryTransList(TransListParam queryParam) throws BaseException {
        try {
            List<TbTransaction> listOfTran = dataGroupMapper.queryTransList(queryParam.getChainId(), queryParam.getGroupId(), queryParam);
            return listOfTran;
        } catch (RuntimeException ex) {
            log.error("fail queryTransList.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of trans by app
     */
    public int queryTransCountByApp(int chainId, int groupId, String appName) throws BaseException {
        try {
            return dataGroupMapper.queryTransCountByApp(chainId, groupId, appName);
        } catch (RuntimeException ex) {
            log.error("fail query count of trans in app", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of trans by contract
     */
    public int queryTransCountByContract(int chainId, int groupId, String contractAddress) throws BaseException {
        try {
            return dataGroupMapper.queryTransCountByContract(chainId, groupId, contractAddress);
        } catch (RuntimeException ex) {
            log.error("fail query count of trans in contract", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * query count of contract.
     */
    public int countOfContract(BaseQueryParam queryParam) throws BaseException {
        try {
            Integer count = dataGroupMapper.countOfContract(queryParam.getChainId(), queryParam.getGroupId(),
                    queryParam);
            return count == null ? 0 : count;
        } catch (RuntimeException ex) {
            log.error("fail countOfContract.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract list.
     */
    public List<ContractInfoDto> queryContractList(BaseQueryParam queryParam) throws BaseException {
        try {
            List<ContractInfoDto> listOfTran = dataGroupMapper.queryContractList(queryParam.getChainId(),
                    queryParam.getGroupId(), queryParam);
            return listOfTran;
        } catch (RuntimeException ex) {
            log.error("fail queryContractList.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * query count of node.
     */
    public int countOfNode(BaseQueryParam queryParam) throws BaseException {
        try {
            Integer count = dataGroupMapper.countOfNode(queryParam);
            return count == null ? 0 : count;
        } catch (RuntimeException ex) {
            log.error("fail countOfNode . queryParam:{}", queryParam, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query node list by page.
     */
    public List<TbNode> queryNodeList(BaseQueryParam queryParam) throws BaseException {
        try {
            List<TbNode> listOfNode = dataGroupMapper.queryNodeList(queryParam);
            return listOfNode;
        } catch (RuntimeException ex) {
            log.error("fail queryNodeList. queryParam:{}", queryParam, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

}
