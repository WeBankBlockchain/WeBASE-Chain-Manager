package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.enums.TaskStatusEnum;
import com.webank.webase.chain.mgr.base.enums.TaskTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.precompiledapi.PrecompiledByTaskService;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class TaskService {
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private PrecompiledByTaskService precompiledByTaskService;


    public synchronized void addSealerNodeFromDbTask() {
        Instant startTime = Instant.now();
        log.info("start exec method[addSealerNodeFromDb] startTime:{}", startTime.toEpochMilli());

        //query task
        List<Byte> statusList = Arrays.asList(TaskStatusEnum.WAITING.getValue(), TaskStatusEnum.FAIL.getValue());
        List<TbTask> taskList = taskManager.selectTaskList(statusList, TaskTypeEnum.OBSERVER_TO_SEALER);
        if (CollectionUtils.isEmpty(taskList)) {
            log.info("finish addSealerNodeFromDb. taskList is empty");
            return;
        }

        for (TbTask task : taskList) {
            try {
                TbTask newTask = taskManager.updateStatusByTbTask(task, TaskStatusEnum.HANDLING);
                if (Objects.nonNull(newTask))
                    precompiledByTaskService.checkAndAddSealer(task.getId(), task.getChainId(), task.getGroupId(), task.getNodeId());
            } catch (BaseException ex) {
                log.error("fail exec method [addSealerNodeFromDbTask]. task:{}", JsonTools.objToString(task), ex);
                String msg = StringUtils.isBlank(ex.getRetCode().getAttachment()) ? ex.getMessage() : ex.getRetCode().getAttachment();
                taskManager.updateStatusByPrimaryKey(task.getId(), TaskStatusEnum.FAIL, msg);
                continue;
            } catch (Exception ex) {
                log.error("fail exec method [addSealerNodeFromDbTask]. task:{}", JsonTools.objToString(task), ex);
                taskManager.updateStatusByPrimaryKey(task.getId(), TaskStatusEnum.FAIL, ex.getMessage());
                continue;
            }

        }
        log.info("start exec method[addSealerNodeFromDb]  useTime:{}", Duration.between(startTime, Instant.now()).toMillis());
    }
}
