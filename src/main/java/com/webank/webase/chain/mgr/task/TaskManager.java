package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.enums.TaskTypeEnum;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import com.webank.webase.chain.mgr.repository.mapper.TbTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Slf4j
@Service
public class TaskManager {
    @Autowired
    private TbTaskMapper taskMapper;

    /**
     * @param chain
     * @param group
     * @param nodeId
     */
    @Transactional
    public TbTask saveTaskOfAddSealerNode(int chain, int group, String nodeId) {
        log.info("start exec method[saveTaskOfAddSealerNode] chain:{} group:{} nodeId:{}", chain, group, nodeId);

        //todo check before save

        //save
        TbTask tbTask = new TbTask();
        tbTask.setChainId(chain);
        tbTask.setGroupId(group);
        tbTask.setNodeId(nodeId);
        tbTask.setTaskType(TaskTypeEnum.OBSERVER_TO_SEALER.getValue());
        tbTask.setGmtCreate(new Date());
        tbTask.setGmtModified(new Date());
        taskMapper.insertSelective(tbTask);

        log.info("success exec method[saveTaskOfAddSealerNode] chain:{} group:{} nodeId:{},result:{}", chain, group, nodeId);
        return taskMapper.selectByPrimaryKey(tbTask.getId());
    }

}
