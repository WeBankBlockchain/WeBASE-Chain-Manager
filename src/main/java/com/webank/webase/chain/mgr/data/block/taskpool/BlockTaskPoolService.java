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
package com.webank.webase.chain.mgr.data.block.taskpool;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.TableName;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.data.block.BlockService;
import com.webank.webase.chain.mgr.data.block.entity.TbBlockTaskPool;
import com.webank.webase.chain.mgr.data.block.enums.BlockCertaintyEnum;
import com.webank.webase.chain.mgr.data.block.enums.TxInfoStatusEnum;
import com.webank.webase.chain.mgr.data.table.TableService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock.Block;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * BlockTaskPoolService
 *
 */
@Service
@Slf4j
public class BlockTaskPoolService {

    @Autowired
    private BlockTaskPoolMapper taskPoolMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private BlockService blockService;
    @Autowired
    private RollBackService rollBackService;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private TableService tableService;

    @Async("asyncExecutor")
    public void pullBlockProcess(CountDownLatch latch, String chainId, String groupId) {
        log.info("start pullBlockProcess. chainId:{} groupId:{}", chainId, groupId);
        try {
            // check table
            tableService.newSubTable(chainId, groupId);
            boolean check = true;
            Instant startTimem = Instant.now();
            Long useTimeSum = 0L;
            while (check) {
                // max block in chain
                long currentChainHeight = frontInterface.getLatestBlockNumber(chainId, groupId).longValue();
                long fromHeight = getHeight(getTaskPoolHeight(chainId, groupId));
                // control the batch unit number
                long end = fromHeight + cProperties.getCrawlBatchUnit() - 1;
                long toHeight = Math.min(currentChainHeight, end);
                log.debug("Current depot status: {} of {}, and try to process block from {} to {}", fromHeight - 1,
                        currentChainHeight, fromHeight, toHeight);
                boolean certainty = toHeight + 1 < currentChainHeight - ConstantProperties.MAX_FORK_CERTAINTY_BLOCK_NUMBER;
                if (fromHeight <= toHeight) {
                    log.debug("Try to sync block number {} to {} of {}", fromHeight, toHeight, currentChainHeight);
                    prepareTask(chainId, groupId, fromHeight, toHeight, certainty);
                }

                log.debug("Begin to fetch at most {} tasks", cProperties.getCrawlBatchUnit());
                List<Block> taskList = fetchData(chainId, groupId, cProperties.getCrawlBatchUnit());
                for (Block b : taskList) {
                    handleSingleBlock(chainId, groupId, b, currentChainHeight);
                }
                if (!certainty) {
                    checkForks(chainId, groupId, currentChainHeight);
                    checkTaskCount(chainId, groupId, cProperties.getStartBlockNumber(), currentChainHeight);
                }
                checkTimeOut(chainId, groupId);
                processErrors(chainId, groupId);
                // useTime
                useTimeSum = Duration.between(startTimem, Instant.now()).toMillis();
//                if (fromHeight > toHeight || useTimeSum > cProperties.getDataParserTaskFixedDelay()) {
                if (fromHeight > toHeight) {
                    check = false;
                }
            }
        } catch (Exception ex) {
            log.error("fail pullBlockProcess. chainId:{} groupId:{} ", chainId, groupId, ex);
        } finally {
            if (Objects.nonNull(latch)) {
                latch.countDown();
            }
        }
        log.info("end pullBlockProcess. chainId:{} groupId:{}", chainId, groupId);
    }

    private long getHeight(long height) {
        return height > cProperties.getStartBlockNumber() ? height : cProperties.getStartBlockNumber();
    }

    public long getTaskPoolHeight(String chainId, String groupId) {
        long height = 0;
        BigInteger localMaxBlockNumber = taskPoolMapper.getLatestBlockNumber(chainId, groupId);
        if (Objects.nonNull(localMaxBlockNumber)) {
            height = localMaxBlockNumber.longValue() + 1;
        }
        return height;
    }

    @Transactional
    public void prepareTask(String chainId, String groupId, long begin, long end, boolean certainty) {
        log.debug("Begin to prepare sync blocks from {} to {}", begin, end);
        List<TbBlockTaskPool> list = Lists.newArrayList();
        for (long i = begin; i <= end; i++) {
            TbBlockTaskPool pool = new TbBlockTaskPool().setBlockNumber(i)
                    .setSyncStatus(TxInfoStatusEnum.INIT.getStatus());
            if (certainty) {
                pool.setCertainty(BlockCertaintyEnum.FIXED.getCertainty());
            } else {
                if (i <= end - ConstantProperties.MAX_FORK_CERTAINTY_BLOCK_NUMBER) {
                    pool.setCertainty(BlockCertaintyEnum.FIXED.getCertainty());
                } else {
                    pool.setCertainty(BlockCertaintyEnum.UNCERTAIN.getCertainty());
                }
            }
            list.add(pool);
        }
        taskPoolMapper.saveAll(chainId, groupId, list);
        log.debug("Sync blocks from {} to {} are prepared.", begin, end);
    }

    public List<Block> fetchData(String chainId, String groupId, int count) {
        List<TbBlockTaskPool> tasks = taskPoolMapper.findBySyncStatusOrderByBlockHeightLimit(
            TableName.TASK.getValue(),
                chainId, groupId, TxInfoStatusEnum.INIT.getStatus(), count);
        if (CollectionUtils.isEmpty(tasks)) {
            return new ArrayList<>();
        } else {
            return getTasks(chainId, groupId, tasks);
        }
    }

    public List<Block> getTasks(String chainId, String groupId, List<TbBlockTaskPool> tasks) {
        List<Block> result = new ArrayList<>();
        List<TbBlockTaskPool> pools = new ArrayList<>();
        for (TbBlockTaskPool task : tasks) {
            task.setSyncStatus(TxInfoStatusEnum.DOING.getStatus());
            BigInteger bigBlockHeight = new BigInteger(Long.toString(task.getBlockNumber()));
            Block block;
            try {
                block = frontInterface.getBlockByNumber(chainId, groupId, bigBlockHeight);
                result.add(block);
                pools.add(task);
            } catch (Exception e) {
                log.error("Block {},  exception occur in job processing: {}", task.getBlockNumber(), e.getMessage());
                taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.ERROR.getStatus(),
                        task.getBlockNumber());
            }
        }
        taskPoolMapper.saveAll(chainId, groupId, pools);
        log.debug("Successful fetch {} Blocks.", result.size());
        return result;
    }

    @Async("asyncExecutor")
    public void handleSingleBlock(String chainId, String groupId, Block b, long total) {
        process(chainId, groupId, b, total);
    }

    public void processDataSequence(String chainId, String groupId, List<Block> data, long total) {
        for (Block b : data) {
            process(chainId, groupId, b, total);
        }
    }

    public void process(String chainId, String groupId, Block b, long total) {
        try {
            log.info("process chainId:{} groupId:{} number:{}.", chainId, groupId, b.getNumber());
            blockService.saveBlockInfo(b, chainId, groupId);
            taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.DONE.getStatus(),
                    b.getNumber());
            log.debug("Block {} of {} sync block succeed.", b.getNumber(), total);
        } catch (Exception e) {
            log.error("block {}, exception occur in job processing: {}", b.getNumber(), e.getMessage());
            taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.ERROR.getStatus(),
                    b.getNumber());
        }
    }

    public void resetDataByBlockNumber(String chainId, String groupId, long blockNumber) {
        TbBlockTaskPool blockTaskPool = taskPoolMapper.findByBlockNumber(chainId, groupId, blockNumber);
        if (Objects.isNull(blockTaskPool)) {
            throw new BaseException(ConstantCode.INVALID_BLOCK_NUMBER);
        }
        if (blockTaskPool.getSyncStatus() == TxInfoStatusEnum.DOING.getStatus()) {
            throw new BaseException(ConstantCode.TASK_RUNNING);
        }
        if (blockTaskPool.getSyncStatus() == TxInfoStatusEnum.INIT.getStatus()) {
            throw new BaseException(ConstantCode.BLOCK_BEEN_RESET);
        }
        rollBackService.rollback(chainId, groupId, blockNumber);
        taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.INIT.getStatus(), blockNumber);
    }

    public void checkForks(String chainId, String groupId, long currentBlockHeight) throws IOException {
        log.debug("current block height is {}, and begin to check forks", currentBlockHeight);
        List<TbBlockTaskPool> uncertainBlocks = taskPoolMapper.findByCertainty(chainId, groupId,
                BlockCertaintyEnum.UNCERTAIN.getCertainty());
        for (TbBlockTaskPool pool : uncertainBlocks) {
            if (pool.getBlockNumber() <= currentBlockHeight - ConstantProperties.MAX_FORK_CERTAINTY_BLOCK_NUMBER) {
                if (pool.getSyncStatus() == TxInfoStatusEnum.DOING.getStatus()) {
                    log.error("block {} is doing!", pool.getBlockNumber());
                    continue;
                }
                if (pool.getSyncStatus() == TxInfoStatusEnum.INIT.getStatus()) {
                    log.error("block {} is not sync!", pool.getBlockNumber());
                    taskPoolMapper.setCertaintyByBlockHeight(chainId, groupId, BlockCertaintyEnum.FIXED.getCertainty(),
                            pool.getBlockNumber());
                    continue;
                }
                Block block = frontInterface.getBlockByNumber(chainId, groupId,
                        BigInteger.valueOf(pool.getBlockNumber()));
                String newHash = block.getHash();
                if (!StringUtils.equals(newHash,
                        blockService.getBlockByBlockNumber(chainId, groupId, pool.getBlockNumber()).getBlockHash())) {
                    log.debug("Block {} is forked!!! ready to resync", pool.getBlockNumber());
                    rollBackService.rollback(chainId, groupId, pool.getBlockNumber());
                    taskPoolMapper.setSyncStatusAndCertaintyByBlockHeight(chainId, groupId,
                            TxInfoStatusEnum.INIT.getStatus(), BlockCertaintyEnum.FIXED.getCertainty(),
                            pool.getBlockNumber());
                } else {
                    log.debug("Block {} is not forked!", pool.getBlockNumber());
                    taskPoolMapper.setCertaintyByBlockHeight(chainId, groupId, BlockCertaintyEnum.FIXED.getCertainty(),
                            pool.getBlockNumber());
                }
            }
        }
    }

    public void checkTaskCount(String chainId, String groupId, long startBlockNumber, long currentMaxTaskPoolNumber) {
        log.debug("Check task count from {} to {}", startBlockNumber, currentMaxTaskPoolNumber);
        if (isComplete(chainId, groupId, startBlockNumber, currentMaxTaskPoolNumber)) {
            return;
        }
        List<TbBlockTaskPool> supplements = new ArrayList<>();
        long t = startBlockNumber;
        for (long i = startBlockNumber; i <= currentMaxTaskPoolNumber
                - cProperties.getCrawlBatchUnit(); i += cProperties.getCrawlBatchUnit()) {
            long j = i + cProperties.getCrawlBatchUnit() - 1;
            Optional<List<TbBlockTaskPool>> optional = findMissingPoolRecords(chainId, groupId, i, j);
            if (optional.isPresent()) {
                supplements.addAll(optional.get());
            }
            t = j + 1;
        }
        Optional<List<TbBlockTaskPool>> optional = findMissingPoolRecords(chainId, groupId, t,
                currentMaxTaskPoolNumber);
        if (optional.isPresent()) {
            supplements.addAll(optional.get());
        }
        log.debug("Find {} missing pool numbers", supplements.size());
        taskPoolMapper.saveAll(chainId, groupId, supplements);
    }

    public void checkTimeOut(String chainId, String groupId) {
        DateTime offsetDate = DateUtil.offsetSecond(DateUtil.date(), 0 - ConstantProperties.DEPOT_TIME_OUT);
        log.debug("Begin to check timeout transactions which is ealier than {}", offsetDate);
        List<TbBlockTaskPool> list = taskPoolMapper.findBySyncStatusAndDepotUpdatetimeLessThan(chainId, groupId,
                TxInfoStatusEnum.DOING.getStatus(), offsetDate);
        if (!CollectionUtils.isEmpty(list)) {
            log.debug("Detect {} timeout transactions.", list.size());
        }
        list.forEach(p -> {
            log.error("Block {} sync block timeout!!, the depot_time is {}, and the threshold time is {}",
                    p.getBlockNumber(), p.getModifyTime(), offsetDate);
            taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.TIMEOUT.getStatus(),
                    p.getBlockNumber());
        });
    }

    public void processErrors(String chainId, String groupId) {
        log.debug("Begin to check error records");
        List<TbBlockTaskPool> unnormalRecords = taskPoolMapper.findUnNormalRecords(chainId, groupId);
        if (CollectionUtils.isEmpty(unnormalRecords)) {
            return;
        } else {
            log.debug("sync block detect {} error transactions.", unnormalRecords.size());
            unnormalRecords.parallelStream().map(b -> b.getBlockNumber()).forEach(e -> {
                log.error("Block {} sync error, and begin to rollback.", e);
                rollBackService.rollback(chainId, groupId, e);
                taskPoolMapper.setSyncStatusByBlockHeight(chainId, groupId, TxInfoStatusEnum.INIT.getStatus(), e);
            });
        }
    }

    private Optional<List<TbBlockTaskPool>> findMissingPoolRecords(String chainId, String groupId, long startIndex,
            long endIndex) {
        if (isComplete(chainId, groupId, startIndex, endIndex)) {
            return Optional.empty();
        }
        List<TbBlockTaskPool> list = taskPoolMapper.findByBlockHeightRange(chainId, groupId, startIndex, endIndex);
        List<Long> ids = list.stream().map(p -> p.getBlockNumber()).collect(Collectors.toList());
        List<TbBlockTaskPool> supplements = new ArrayList<>();
        for (long tmpIndex = startIndex; tmpIndex <= endIndex; tmpIndex++) {
            if (ids.indexOf(tmpIndex) >= 0) {
                continue;
            }
            log.debug("Successfully detect block {} is missing. Try to sync block again.", tmpIndex);
            TbBlockTaskPool pool = new TbBlockTaskPool().setBlockNumber(tmpIndex)
                    .setSyncStatus(TxInfoStatusEnum.ERROR.getStatus())
                    .setCertainty(BlockCertaintyEnum.UNCERTAIN.getCertainty());
            supplements.add(pool);
        }
        return Optional.of(supplements);
    }

    private boolean isComplete(String chainId, String groupId, long startBlockNumber, long currentMaxTaskPoolNumber) {
        long deserveCount = currentMaxTaskPoolNumber - startBlockNumber + 1;
        long actualCount = taskPoolMapper.countByBlockHeightRange(chainId, groupId, startBlockNumber,
                currentMaxTaskPoolNumber);
        log.debug("Check task count from block {} to {}, deserve count is {}, and actual count is {}", startBlockNumber,
                currentMaxTaskPoolNumber, deserveCount, actualCount);
        if (deserveCount == actualCount) {
            return true;
        } else {
            return false;
        }
    }
}
