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
package com.webank.webase.chain.mgr.data.transaction;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.TableName;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.data.block.entity.MinMaxBlock;
import com.webank.webase.chain.mgr.data.table.TableService;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.data.transaction.entity.TransListParam;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * services for block data.
 */
@Log4j2
@Service
public class TransactionService {

    @Autowired
    private TbChainMapper chainMapper;
    @Autowired
    private TbGroupMapper groupMapper;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private TableService tableService;

    /**
     * add trans hash info.
     */
    public void addTransInfo(int chainId, int groupId, TbTransaction tbTransaction) throws BaseException {
        TbChain tbChain = chainMapper.selectByPrimaryKey(chainId);
        if (!ObjectUtils.isEmpty(tbChain)) {
            tbTransaction.setChainName(tbChain.getChainName());
        }
        TbGroup tbGroup = groupMapper.getGroupById(chainId, groupId);
//        if (!ObjectUtils.isEmpty(tbGroup)) {
//            tbTransaction.setAppName(tbGroup.getAppName());
//        }
        transactionMapper.add(chainId, groupId, tbTransaction);
    }


    class QueryParams {

        public List<String> countTables = new ArrayList<>();
        public long startTime;
        public long endTime;

        public QueryParams(int chainId, int groupId, long start, long end) {
            List<String> tables = transactionMapper.getAllTranxTables();
            for (String table : tables) {
                String[] infos = table.split("_");
                int tChainId = Integer.parseInt(infos[infos.length - 2]);
                int tGroupId = Integer.parseInt(infos[infos.length - 1]);
                if (chainId < 0 || chainId == tChainId) {
                    if (groupId < 0 || groupId == tGroupId) {
                        countTables.add(table);
                    }
                }
            }
            startTime = start < 0 ? 0 : start;
            endTime = end < 0 ? System.currentTimeMillis() / 1000 : end;
        }
    }

    public int getTranxCountAll(int chainId, int groupId, long start, long end) {

        QueryParams queryParams = new QueryParams(chainId, groupId, start, end);
        return transactionMapper.getAllTranxCount(queryParams.countTables, queryParams.startTime, queryParams.endTime);
    }


    public List<TbTransaction> getTranxListAll(Integer chainId, Integer groupId, Long start, Long end) {
        QueryParams queryParams = new QueryParams(chainId, groupId, start, end);
        return transactionMapper.getAllTranxList(queryParams.countTables, queryParams.startTime, queryParams.endTime);
    }

    /**
     * query trans list.
     */
    public List<TbTransaction> queryTransList(int chainId, int groupId, TransListParam param) throws BaseException {
        List<TbTransaction> listOfTran = null;
        try {
            listOfTran = transactionMapper.getList(chainId, groupId, param);
        } catch (RuntimeException ex) {
            log.error("fail queryBlockList.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
        return listOfTran;
    }

    /**
     * query count of trans hash.
     */
    public Integer queryCountOfTran(int chainId, int groupId, TransListParam queryParam) throws BaseException {
        try {
            Integer count = transactionMapper.getCount(chainId, groupId, queryParam);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTran.", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query count of trans by minus max and min trans_number
     */
    public Integer queryCountOfTranByMinus(int chainId, int groupId) throws BaseException {
        try {
            Integer count = transactionMapper.getCountByMinMax(chainId, groupId);
            log.info("end queryCountOfTranByMinus. count:{}", count);
            if (count == null) {
                return 0;
            }
            return count;
        } catch (BadSqlGrammarException ex) {
            log.info("restart from queryCountOfTranByMinus to queryCountOfTran: []", ex.getCause());
            TransListParam queryParam = new TransListParam(null, null);
            Integer count = queryCountOfTran(chainId, groupId, queryParam);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfTranByMinus. ", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query min and max block number.
     */
    public List<MinMaxBlock> queryMinMaxBlock(int chainId, int groupId) throws BaseException {
        log.debug("start queryMinMaxBlock");
        try {
            List<MinMaxBlock> listMinMaxBlock = transactionMapper.queryMinMaxBlock(chainId, groupId);
            int listSize = Optional.ofNullable(listMinMaxBlock).map(list -> list.size()).orElse(0);
            log.info("end queryMinMaxBlock listMinMaxBlockSize:{}", listSize);
            return listMinMaxBlock;
        } catch (RuntimeException ex) {
            log.error("fail queryMinMaxBlock", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query un statistics transaction hash list.
     */
    public List<String> queryUnStatTransHashList(int chainId, int groupId) {
        List<String> list = transactionMapper.listOfUnStatTransHash(chainId, groupId);
        return list;
    }

    /**
     * update trans statistic flag.
     */
    public void updateTransStatFlag(int chainId, int groupId, String transHash) {
        transactionMapper.updateTransStatFlag(chainId, groupId, transHash);
    }

    /**
     * getTbTransByHash.
     */
    public TbTransaction getTbTransByHash(int chainId, Integer groupId, String transHash) {
        return transactionMapper.getByHash(chainId, groupId, transHash);
    }

    /**
     * request front for transaction by hash.
     */
    public TbTransaction getTbTransFromFrontByHash(int chainId, Integer groupId, String transHash)
            throws BaseException {
        Transaction trans = frontInterface.getTransaction(chainId, groupId, transHash);
        TbTransaction tbTransaction = null;
        if (trans != null) {
            tbTransaction = new TbTransaction(chainId, groupId, transHash, trans.getBlockNumber(), null,
                    JsonTools.objToString(trans));
        }
        return tbTransaction;
    }

//    /**
//     * get transaction info
//     */
//    public Transaction getTransaction(int chainId, int groupId, String transHash) {
//        return frontInterface.getTransaction(chainId, groupId, transHash);
//    }
//
//    /**
//     * get transaction receipt
//     */
//    public TransactionReceipt getTransReceipt(int chainId, int groupId, String transHash) {
//        return frontInterface.getTransReceipt(chainId, groupId, transHash);
//    }

    /**
     * Remove trans info.
     */
    public Integer remove(Integer chainId, Integer groupId, Integer subTransNum) {
        Integer affectRow = transactionMapper.remove(chainId, groupId, subTransNum);
        return affectRow;
    }

}
