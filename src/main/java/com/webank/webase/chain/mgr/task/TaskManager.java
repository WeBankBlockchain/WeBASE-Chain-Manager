package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.enums.TaskTypeEnum;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import com.webank.webase.chain.mgr.repository.bean.TbTaskExample;
import com.webank.webase.chain.mgr.repository.mapper.TbTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

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


    /**
     * @param statusList
     * @param taskTypeEnum
     * @return
     */
    public List<TbTask> selectTaskList(List<Byte> statusList, TaskTypeEnum taskTypeEnum) {
        log.info("start exec method[selectTaskList] statusList:{} taskTypeEnum:{} ", JsonTools.objToString(statusList), JsonTools.objToString(taskTypeEnum));

        //param
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andTaskTypeEqualTo(taskTypeEnum.getValue());
        criteria.andTaskStatusIn(statusList);

        //query
        List<TbTask> taskList = taskMapper.selectByExample(example);

        log.info("start exec method[selectTaskList] result:{}", JsonTools.objToString(taskList));
        return taskList;
    }
}
