package com.webank.webase.chain.mgr.front;

import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbFrontExample;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FrontManager {
    @Autowired
    private TbFrontMapper tbFrontMapper;

    /**
     * @param agencyId
     * @param frontPeerName
     * @return
     */
    public List<TbFront> queryFrontByAgencyIdAndFrontPeerNameAndNodeId(Integer agencyId, String frontPeerName, String nodeId) {
        log.debug("start exec method [listFrontByAgencyIdAndFrontPeerName]. agencyId:{} frontPeerName:{} nodeId:{}", agencyId, frontPeerName, nodeId);
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        if (Objects.nonNull(agencyId)) {
            criteria.andExtAgencyIdEqualTo(agencyId);
        }

        if (StringUtils.isNotBlank(frontPeerName)) {
            criteria.andFrontPeerNameEqualTo(frontPeerName);
        }

        if (StringUtils.isNotBlank(nodeId)) {
            criteria.andNodeIdEqualTo(nodeId);
        }

        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.debug("success exec method [listFrontByAgencyIdAndFrontPeerName]. agencyId:{} frontPeerName:{} nodeId:{} result:{}", agencyId, frontPeerName, nodeId, JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param frontIdList
     * @return
     */
    public List<TbFront> queryFrontByIdList(List<Integer> frontIdList) {
        log.info("start exec method [listFrontByIdList]. frontIdList:{}", JsonTools.objToString(frontIdList));

        if (CollectionUtils.isEmpty(frontIdList))
            return Collections.EMPTY_LIST;

        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andFrontIdIn(frontIdList);

        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.debug("success exec method [listFrontByIdList]. frontIdList:{} result:{}", JsonTools.objToString(frontIdList), JsonTools.objToString(frontList));
        return frontList;
    }
}
