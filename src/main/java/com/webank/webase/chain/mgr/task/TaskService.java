package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.enums.TaskStatusEnum;
import com.webank.webase.chain.mgr.base.enums.TaskTypeEnum;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class TaskService {
    @Autowired
    private TaskManager taskManager;


    public synchronized void addSealerNodeFromDbTask() {
        log.info("start exec method[addSealerNodeFromDb] ");

        //query task
        List<Byte> statusList = Arrays.asList(TaskStatusEnum.WAITING.getValue(), TaskStatusEnum.FAIL.getValue());
        List<TbTask> taskList = taskManager.selectTaskList(statusList, TaskTypeEnum.OBSERVER_TO_SEALER);
        if (CollectionUtils.isEmpty(taskList)) {
            log.info("finish addSealerNodeFromDb. taskList is empty");
            return;
        }




        log.info("start exec method[addSealerNodeFromDb] ");
    }
}
