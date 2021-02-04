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

import java.util.Objects;

@Slf4j
@Service
public class TaskService {
    @Autowired
    private TbTaskMapper taskMapper;

    /**
     * @param chain
     * @param group
     * @param nodeId
     * @param taskTypeEnum
     */
    @Transactional
    public void saveTaskOfAddSealerNode(int chain, int group, String nodeId, TaskTypeEnum taskTypeEnum) {
        log.info("start exec method[saveTaskOfAddSealerNode] chain:{} group:{} nodeId:{}", chain, group, nodeId);

        //find by chain group node
        TbTask tbTask = getByChainGroupNode(chain, group, nodeId);
        if (Objects.nonNull(tbTask)) {

        }

    }


    /**
     * @param chain
     * @param group
     * @param nodeId
     * @return
     */
    public TbTask getByChainGroupNode(int chain, int group, String nodeId) {
        log.debug("start exec method[getByChainGroupNode] chain:{} group:{} nodeId:{}", chain, group, nodeId);

        //param
        TbTaskExample example = new TbTaskExample();
        TbTaskExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chain);
        criteria.andGroupIdEqualTo(group);
        criteria.andNodeIdEqualTo(nodeId);

        //query
        TbTask tbTask = taskMapper.getOneByExample(example).orElse(null);
        log.debug("finish exec method[getByChainGroupNode] chain:{} group:{} nodeId:{} result:{}", chain, group, nodeId, JsonTools.objToString(tbTask));
        return tbTask;
    }
}
