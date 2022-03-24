/**
 * Copyright 2014-2021  the original author or authors.
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
package com.webank.webase.chain.mgr.data.scheduler;


import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.data.block.BlockService;
import com.webank.webase.chain.mgr.data.transaction.TransactionService;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * delete block/trans/monitorTrans data task
 * related: yml-constant-transRetainMax
 */
@Log4j2
@Component
@ConditionalOnProperty(name = "constant.isDeleteInfo", havingValue = "true")
public class DataDeleteTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ConstantProperties cProperties;


    @Scheduled(cron = "${constant.deleteInfoCron}")
    public void taskStart() {
        // toggle
        if (!cProperties.isIfPullData()) {
            return;
        }
        deleteInfoStart();
    }

    /**
     * start.
     */
    public void deleteInfoStart() {
        Instant startTime = Instant.now();
        log.info("start deleteInfoStart. startTime:{}", startTime.toEpochMilli());
        //get group list
        List<TbGroup> groupList = groupService.getGroupList(null, DataStatus.NORMAL.getValue());
        if (groupList == null || groupList.size() == 0) {
            log.warn("DeleteInfoTask jump over, not found any group");
            return;
        }
        groupList.forEach(g -> deleteByGroupId(g.getChainId(), g.getGroupId()));

        log.info("end deleteInfoStart useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * delete by groupId.
     */
    private void deleteByGroupId(String chainId, String groupId) {
        //delete block
        deleteBlock(chainId, groupId);
        //delete transHash
        deleteTransaction(chainId, groupId);
    }


    /**
     * delete block.
     */
    private void deleteBlock(String chainId, String groupId) {
        log.debug("start deleteBlock. chainId:{},groupId:{}", chainId, groupId);
        try {
            Integer removeCount = blockService.remove(chainId, groupId, cProperties.getBlockRetainMax());
            log.debug("end deleteBlock. chainId:{},groupId:{} removeCount:{}", chainId, groupId, removeCount);
        } catch (Exception ex) {
            log.error("fail deleteBlock. chainId:{},groupId:{}, error:{}", chainId, groupId, ex);
        }
    }

    /**
     * delete transHash.
     */
    private void deleteTransaction(String chainId, String groupId) {
        log.debug("start deleteTransHash. chainId:{},groupId:{}", chainId, groupId);
        try {
//            TransListParam queryParam = new TransListParam(null, null);
//            Integer count = transHashService.queryCountOfTran(groupId, queryParam);
            Integer count = transactionService.queryCountOfTranByMinus(chainId, groupId);
            Integer removeCount = 0;
            if (count > cProperties.getTransRetainMax().intValue()) {
                int subTransNum = count - cProperties.getTransRetainMax().intValue();
                removeCount = transactionService.remove(chainId, groupId, subTransNum);
            }
            log.debug("end deleteTransHash. chainId:{},groupId:{} removeCount:{}", chainId, groupId, removeCount);
        } catch (Exception ex) {
            log.error("fail deleteTransHash. chainId:{},groupId:{},error:{}", chainId, groupId, ex);
        }
    }


}
