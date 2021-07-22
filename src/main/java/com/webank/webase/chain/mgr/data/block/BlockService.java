/**
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
package com.webank.webase.chain.mgr.data.block;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.data.block.entity.BlockListParam;
import com.webank.webase.chain.mgr.data.block.entity.TbBlock;
import com.webank.webase.chain.mgr.data.transaction.TransactionService;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock.Block;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock.TransactionResult;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * services for block data. including pull block from chain and block service
 */
@Log4j2
@Service
public class BlockService {

    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private BlockMapper blockMapper;
    @Autowired
    @Lazy
    private TransactionService transactionService;

    private static final Long SAVE_TRANS_SLEEP_TIME = 5L;

    /**
     * save report block info.
     */
    @Transactional
    @SuppressWarnings("rawtypes")
    public void saveBlockInfo(Block block, int chainId, int groupId) throws BaseException {
        // save block info
        TbBlock tbBlock = chainBlock2TbBlock(block);
        addBlockInfo(tbBlock, chainId, groupId);

        // save trans hash
        List<TransactionResult> transList = block.getTransactions();
        for (TransactionResult result : transList) {
            // save trans
            Transaction trans = (Transaction) result.get();
            TbTransaction tbTransaction = new TbTransaction(chainId, groupId, trans.getHash(), trans.getBlockNumber(),
                    tbBlock.getBlockTimestamp(), JsonTools.objToString(trans));
            transactionService.addTransInfo(chainId, groupId, tbTransaction);
            // save receipt
//            receiptService.handleReceiptInfo(chainId, groupId, trans.getHash(), tbBlock.getBlockTimestamp());
            try {
                Thread.sleep(SAVE_TRANS_SLEEP_TIME);
            } catch (InterruptedException ex) {
                log.error("saveBLockInfo error.", ex);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * add block info to db.
     */
    public void addBlockInfo(TbBlock tbBlock, int chainId, int groupId) throws BaseException {
        // save block info
        blockMapper.add(chainId, groupId, tbBlock);
    }

    /**
     * query block info list.
     */
    public List<TbBlock> queryBlockList(int chainId, int groupId, BlockListParam queryParam) throws BaseException {
        List<TbBlock> listOfBlock = blockMapper.getList(chainId, groupId, queryParam);
        return listOfBlock;
    }

    /**
     * query count of block.
     */
    public Integer queryCountOfBlock(int chainId, int groupId, String blockHash, BigInteger blockNumber)
            throws BaseException {
        try {
            Integer count = blockMapper.getCount(chainId, groupId, blockHash, blockNumber);
            log.info("end countOfBlock groupId:{} blockHash:{} count:{}", groupId, blockHash, count);
            if (count == null) {
                return 0;
            }
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfBlock groupId:{} blockHash:{}", groupId, blockHash, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    public TbBlock getBlockByBlockNumber(int chainId, int groupId, long blockNumber) {
        return blockMapper.findByBlockNumber(chainId, groupId, blockNumber);
    }

    public Integer queryCountOfBlockByMinus(int chainId, int groupId) {
        try {
            Integer count = blockMapper.getBlockCountByMinMax(chainId, groupId);
            log.info("end queryCountOfBlockByMinus groupId:{} count:{}", groupId, count);
            if (count == null) {
                return 0;
            }
            return count;
        } catch (RuntimeException ex) {
            log.error("fail queryCountOfBlockByMinus groupId:{},exception:{}", groupId, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * remove block into.
     */
    public Integer remove(int chainId, int groupId, BigInteger blockRetainMax) throws BaseException {
        Integer affectRow = blockMapper.remove(chainId, groupId, blockRetainMax);
        return affectRow;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(int chainId, int groupId) {
        return blockMapper.getLatestBlockNumber(chainId, groupId);
    }

    /**
     * get block by block from front server
     */
    public Block getBlockFromFrontByNumber(int chainId, int groupId, BigInteger blockNumber) {
        return frontInterface.getBlockByNumber(chainId, groupId, blockNumber);
    }

    /**
     * copy chainBlock properties;
     */
    private static TbBlock chainBlock2TbBlock(Block block) {
        if (block == null) {
            return null;
        }
        LocalDateTime blockTimestamp = CommonUtils.timestamp2LocalDateTime(block.getTimestamp().longValue());
        int sealerIndex = Integer.parseInt(block.getSealer().substring(2), 16);
        List<String> sealerList = block.getSealerList();
        String sealer = "0x0";
        if (!CollectionUtils.isEmpty(sealerList)) {
            if (sealerIndex < sealerList.size()) {
                sealer = sealerList.get(sealerIndex);
            } else {
                sealer = sealerList.get(0);
            }
        }
        // save block info
        TbBlock tbBlock = new TbBlock(block.getHash(), block.getNumber(), blockTimestamp,
                block.getTransactions().size(), sealerIndex, sealer, JsonTools.objToString(block));
        return tbBlock;
    }
}
