package com.webank.webase.chain.mgr.task;

import com.webank.webase.chain.mgr.base.enums.TaskStatusEnum;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbTask;
import com.webank.webase.chain.mgr.repository.bean.TbTaskExample;
import com.webank.webase.chain.mgr.repository.mapper.TbTaskMapper;
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
    private TbTaskMapper taskMapper;


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

    public synchronized void addSealerNodeFromDb() {
        log.info("start exec method[addSealerNodeFromDb] ");

        List<Byte> statusList = Arrays.asList(TaskStatusEnum.WAITING.getValue(), TaskStatusEnum.FAIL.getValue());
        if (CollectionUtils.isEmpty(statusList)) {
            log.info("finish addSealerNodeFromDb. statusList is empty");
            return;
        }

        log.info("start exec method[addSealerNodeFromDb] ");
    }
}
