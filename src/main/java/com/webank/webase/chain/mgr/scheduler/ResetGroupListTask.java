/**
 * Copyright 2014-2019  the original author or authors.
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResetGroupListTask {

    @Autowired
    private GroupService groupService;
    @Autowired
    private ResetGroupListAsyncTask resetGroupListAsyncTask;

    @Scheduled(fixedDelayString = "${constant.resetGroupListCycle}")
    public void taskStart() {
        resetGroupList();
    }

    /**
     * async reset groupList.
     */

    public void asyncResetGroupList() {
        try {
            resetGroupListAsyncTask.asyncResetGroupList();
        } catch (Exception ex) {
            log.error("asyncResetGroupList for exception", ex);
        }

    }

    /**
     * reset groupList.
     */
    public void resetGroupList() {
        groupService.resetGroupList();
    }
}