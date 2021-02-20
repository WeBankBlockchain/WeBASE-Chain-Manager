package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.TaskStatusEnum;
import com.webank.webase.chain.mgr.base.enums.TaskTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import com.webank.webase.chain.mgr.repository.bean.TbTaskExample;
import com.webank.webase.chain.mgr.repository.mapper.TbTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

        //check before save
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chain);
        criteria.andGroupIdEqualTo(group);
        criteria.andNodeIdEqualTo(nodeId);
        Optional<TbTask> taskOptional = taskMapper.getOneByExample(example);
        if (taskOptional.isPresent()) {
            log.info("finish exec method[saveTaskOfAddSealerNode] fount task record by chain:{} group:{} nodeId:{}", chain, group, nodeId);
            return taskOptional.get();
        }

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
     * @param chain
     * @param group
     * @param nodeId
     */
    public void queryByChainAndGroupAndNode(int chain, int group, String nodeId) {
        log.info("start exec method[queryByChainAndGroupAndNode] chain:{} group:{} nodeId:{}", chain, group, nodeId);
        //params
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chain);
        criteria.andGroupIdEqualTo(group);
        criteria.andNodeIdEqualTo(nodeId);
        //query
        long count = taskMapper.countByExample(example);
        log.info("count:{}", count);
        if (count > 0)
            throw new BaseException(ConstantCode.NODE_IN_TASK.attach(String.format("node:%s already in task", nodeId)));

        log.info("finish exec method[queryByChainAndGroupAndNode] chain:{} group:{} nodeId:{}", chain, group, nodeId);
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


    /**
     * @param task
     * @param taskStatusEnum
     * @return
     */
    public TbTask updateStatusByTbTask(TbTask task, TaskStatusEnum taskStatusEnum) {
        return updateStatusByTbTask(task, taskStatusEnum, "");
    }


    /**
     * @param oldTask
     * @param taskStatusEnum
     * @return true:success  false:fail
     */
    public TbTask updateStatusByTbTask(TbTask oldTask, TaskStatusEnum taskStatusEnum, String remark) {
        log.debug("start exec method[updateTaskStatus] task:{} taskTypeEnum:{} remark:{}", JsonTools.objToString(oldTask), JsonTools.objToString(taskStatusEnum), remark);
        if (Objects.isNull(oldTask)) {
            log.warn("fail exec method[updateTaskStatus]. task is null");
            return null;
        }

        //param
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(oldTask.getId());
        criteria.andTaskStatusEqualTo(oldTask.getTaskStatus());

        // new task
        TbTask newTask = new TbTask();
        BeanUtils.copyProperties(oldTask, newTask);
        newTask.setGmtModified(new Date());
        newTask.setTaskStatus(taskStatusEnum.getValue());
        newTask.setRemark(remark);

        //update
        int modifyCount = taskMapper.updateByExampleWithBLOBs(newTask, example);
        log.info("modifyCount:{}", modifyCount);


        if (modifyCount > 0)
            newTask = taskMapper.selectByPrimaryKey(oldTask.getId());


        log.warn("finish exec method[updateTaskStatus]. result:{}", JsonTools.objToString(newTask));
        return newTask;
    }


    /**
     * @param taskId
     * @param taskStatusEnum
     * @param remark
     * @return
     */
    public TbTask updateStatusByPrimaryKey(int taskId, TaskStatusEnum taskStatusEnum, String remark) {
        log.debug("start exec method[updateStatusByPrimaryKey] taskId:{} taskStatusEnum:{} remark:{}", taskId, JsonTools.objToString(taskStatusEnum), remark);
        TbTask tbTask = taskMapper.selectByPrimaryKey(taskId);
        if (Objects.isNull(tbTask)) {
            log.warn("fail exec method [updateStatusByPrimaryKey]. not found task record by taskId:{}", taskId);
            return null;
        }

        TbTask taskRsp = updateStatusByTbTask(tbTask, taskStatusEnum, remark);
        log.debug("finish exec method[updateStatusByPrimaryKey]. result:{}", JsonTools.objToString(taskRsp));
        return taskRsp;
    }


    /**
     * @param taskId
     * @param taskStatusEnum
     * @return
     */
    public TbTask updateStatusByPrimaryKey(int taskId, TaskStatusEnum taskStatusEnum) {
        return updateStatusByPrimaryKey(taskId, taskStatusEnum, "");
    }


    /**
     *
     */
    public void removeFinishTask() {
        log.info("start exec method[removeFinishTask] ");
        //finish
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andTaskStatusEqualTo(TaskStatusEnum.SUCCESS.getValue());

        //remove
        taskMapper.deleteByExample(example);
    }
}
