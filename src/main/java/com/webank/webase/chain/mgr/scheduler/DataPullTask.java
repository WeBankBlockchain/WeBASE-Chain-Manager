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
package com.webank.webase.chain.mgr.scheduler;

import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.data.block.taskpool.BlockTaskPoolService;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * get block info from chain. including tb_block and tb_trans (block contains trans)
 */
@Log4j2
@Component
//@ConditionalOnProperty(value = {"constant.multiLiving"}, havingValue = "false")
public class DataPullTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private BlockTaskPoolService blockTaskPoolService;
    @Autowired
    private ConstantProperties cProperties;

    @Scheduled(cron = "${constant.dataPullCron}")
    public void taskStart() {
        // toggle
        if (!cProperties.isIfPullData()) {
            return;
        }
        pullBlockStart();
    }

    /**
     * task start
     */
    public void pullBlockStart() {
        log.info("start pullBLock.");
        Instant startTime = Instant.now();
//        List<TbGroup> groupList = groupService.getGroupList(null, null, DataStatus.NORMAL.getValue());
        List<TbGroup> groupList = groupService.getGroupList(null, DataStatus.NORMAL.getValue());
        if (CollectionUtils.isEmpty(groupList)) {
            log.warn("pullBlock jump over: not found any group");
            return;
        }

        CountDownLatch latch = new CountDownLatch(groupList.size());
        groupList.stream().forEach(group -> blockTaskPoolService.pullBlockProcess(latch,
                group.getChainId(), group.getGroupId()));

        try {
            latch.await();
        } catch (InterruptedException ex) {
            log.error("InterruptedException", ex);
            Thread.currentThread().interrupt();
        }
        log.info("end pullBLock useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }
}
