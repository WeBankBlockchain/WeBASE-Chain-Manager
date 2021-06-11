package com.webank.webase.chain.mgr.scheduler;

import com.webank.webase.chain.mgr.group.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ResetGroupListAsyncTask {

    @Autowired
    private GroupService groupService;

    /**
     * async reset groupList.
     */
    @Async(value = "mgrAsyncExecutor")
    public void asyncResetGroupList() {
        groupService.resetGroupList();
    }

}
