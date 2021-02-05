package com.webank.webase.chain.mgr.scheduler;

import com.webank.webase.chain.mgr.task.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AddSealerNodeTask {
    @Autowired
    private TaskService taskService;

    @Scheduled(fixedDelayString = "${constant.addSealerNodeCycle}")
    public void taskStart() {
//        resetGroupList();
    }


}

