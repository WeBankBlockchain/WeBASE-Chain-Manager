package com.webank.webase.chain.mgr.precompiledapi;

import com.webank.webase.chain.mgr.base.enums.TaskStatusEnum;
import com.webank.webase.chain.mgr.task.TaskManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PrecompiledByTaskService {
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private PrecompiledService precompiledService;

    @Transactional
    public void checkAndAddSealer(int taskId, int chain, int group, String node) {
        log.info("start exec method[checkAndAddSealer]  taskId:{} chain:{} group:{} node:{}", taskId, chain, group, node);

        //check sealer
        precompiledService.checkBeforeAddSealer(chain, group, SetUtils.hashSet(node));

        //update status to success
        taskManager.updateStatusByPrimaryKey(taskId, TaskStatusEnum.SUCCESS);

        //change type of not to sealer
        precompiledService.addSealer(chain, group, node);

        log.info("finish exec method[checkAndAddSealer]  taskId:{} chain:{} group:{} node:{}", taskId, chain, group, node);
    }


}
