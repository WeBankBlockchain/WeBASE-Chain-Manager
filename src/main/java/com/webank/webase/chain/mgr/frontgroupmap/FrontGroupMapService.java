/**
 * Copyright 2014-2019  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.frontgroupmap;

import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbFrontGroupMap;
import com.webank.webase.chain.mgr.repository.bean.TbFrontGroupMapExample;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Slf4j
public class FrontGroupMapService {

    @Autowired
    private TbFrontGroupMapMapper tbFrontGroupMapMapper;
    @Autowired
    private TbFrontMapper frontMapper;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;

    /**
     * add new mapping
     */
    @Transactional
    public TbFrontGroupMap newFrontGroup(String chainId, Integer frontId, String groupId) {

        //add db
        TbFrontGroupMap exists = this.tbFrontGroupMapMapper.selectByChainIdAndFrontIdAndGroupId(chainId, frontId, groupId);
        if (exists == null) {
            TbFrontGroupMap tbFrontGroupMap = new TbFrontGroupMap(chainId, frontId, groupId);
            try {
                this.tbFrontGroupMapMapper.insertSelective(tbFrontGroupMap);
            } catch (Exception e) {
                log.error("Insert front group map error", e);
                throw e;
            }
        }
        return exists;
    }

    /**
     * get map count
     */
    public int getCount(MapListParam param) {
        return this.tbFrontGroupMapMapper.countByParam(param);
    }

    /**
     * remove by chainId
     */
    public void removeByChainId(String chainId) {
        if (chainId.isEmpty()) {
            return;
        }
        //remove by chainId
        this.tbFrontGroupMapMapper.deleteByChainId(chainId);
    }

    /**
     * remove by groupId
     */
    public void removeByGroupId(String chainId, String groupId) {
        if (chainId.isEmpty() || groupId.isEmpty()) {
            return;
        }
        //remove by groupId
        this.tbFrontGroupMapMapper.deleteByGroupId(chainId, groupId);
    }

    /**
     * remove by frontId
     */
    public void removeByFrontId(int frontId) {
        log.info("removeByFrontId:{}", frontId);
        if (frontId == 0) {
            return;
        }
        //remove by frontId
        this.tbFrontGroupMapMapper.deleteByFrontId(frontId);
    }

    public void removeByFrontListAndChain(String chainId, List<Integer> frontIdList) {
        if (CollectionUtils.isEmpty(frontIdList))
            return;

        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.equals(chainId);
        criteria.andFrontIdIn(frontIdList);

        tbFrontGroupMapMapper.deleteByExample(example);


    }

    /**
     * get map list by groupId
     */
    public List<TbFrontGroupMap> listByChainAndGroup(String chainId, String groupId) {
        log.info("start exec method[listByChainAndGroup], chainId:{} groupId:{}", chainId, groupId);

        //param
        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.equals(chainId);
        if (Objects.nonNull(groupId)) {
            criteria.equals(groupId);
        }

        List<TbFrontGroupMap> frontGroupMapList = tbFrontGroupMapMapper.selectByExample(example);
        log.info("success exec method[listByChainAndGroup], chainId:{} groupId:{} result:{}", chainId, groupId, JsonTools.objToString(frontGroupMapList));
        return frontGroupMapList;
    }


    /**
     * @param frontIdList
     * @return
     */
    public List<Pair<String, String>> listGroupByFronts(List<Integer> frontIdList) {
        log.info("start exec method[listGroupByFronts], frontIdList:{}", JsonTools.objToString(frontIdList));
        if (CollectionUtils.isEmpty(frontIdList)) return Collections.EMPTY_LIST;

        //param
        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.andFrontIdIn(frontIdList);

        //query
        List<TbFrontGroupMap> frontGroupMapList = tbFrontGroupMapMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(frontGroupMapList)) return Collections.EMPTY_LIST;

        List<Pair<String, String>> chainGroupPairList = frontGroupMapList.stream().map(map -> Pair.of(map.getChainId(), map.getGroupId())).distinct().collect(Collectors.toList());
        log.info("success exec method[listGroupByFronts], frontIdList:{} result:{}", JsonTools.objToString(frontIdList), JsonTools.objToString(chainGroupPairList));
        return chainGroupPairList;
    }


    /**
     * @param chainId
     * @param frontIdList
     * @return
     */
    public List<String> listGroupIdByChainAndFronts(String chainId, List<Integer> frontIdList) {
        log.info("start exec method[listGroupIdByChainAndFronts], chainId:{} frontIdList:{}", chainId, JsonTools.objToString(frontIdList));
        if (CollectionUtils.isEmpty(frontIdList)) return Collections.EMPTY_LIST;

        //param
        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.equals(chainId);
        criteria.andFrontIdIn(frontIdList);

        //query
        List<TbFrontGroupMap> frontGroupMapList = tbFrontGroupMapMapper.selectByExample(example);
        log.info("frontGroupMapList:{}", JsonTools.objToString(frontGroupMapList));

        if (CollectionUtils.isEmpty(frontGroupMapList)) return Collections.EMPTY_LIST;
        List<String> groupIdList = frontGroupMapList.stream().map(map -> map.getGroupId()).distinct().collect(Collectors.toList());
        log.info("success exec method[listGroupIdByChainAndFronts], frontIdList:{} result:{}", JsonTools.objToString(frontIdList), JsonTools.objToString(groupIdList));
        return groupIdList;
    }


    /**
     * @param chainId
     * @param frontId
     * @return
     */
    public List<String> getGroupByChainAndFront(String chainId, int frontId) {
        log.info("start exec method[getGroupByChainAndFront], chainId:{} frontId:{}", chainId, frontId);
        //param
        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.equals(chainId);
        criteria.andFrontIdEqualTo(frontId);

        //query
        List<TbFrontGroupMap> frontGroupMapList = tbFrontGroupMapMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(frontGroupMapList)) return Collections.EMPTY_LIST;
        List<String> groupIdList = frontGroupMapList.stream().map(map -> map.getGroupId()).distinct().collect(Collectors.toList());

        log.info("success exec method[listGroupByFronts], chainId:{} frontId:{} result:{}", chainId, frontId, JsonTools.objToString(groupIdList));
        return groupIdList;

    }


    /**
     * @param chain
     * @param group
     * @param node
     */
    public void removeByChainAndGroupAndNode(String chain, String group, String node) {
        log.info("start exec method[removeByChainAndGroupAndNode], chain:{},  group:{}, node:{}", chain, group, node);
        TbFront tbFront = frontMapper.getByChainIdAndNodeId(chain, node);
        if (Objects.isNull(tbFront)) {
            log.info("finish exec method[removeByChainAndGroupAndNode]. not found front by chain:{}, node:{}", chain, node);
            return;
        }

        //param
        TbFrontGroupMapExample example = new TbFrontGroupMapExample();
        TbFrontGroupMapExample.Criteria criteria = example.createCriteria();
        criteria.equals(chain);
        criteria.equals(group);
        criteria.andFrontIdEqualTo(tbFront.getFrontId());

        //delete
        int deleteCount = tbFrontGroupMapMapper.deleteByExample(example);

        // clear cache
        frontGroupMapCache.clearMapList(tbFront.getChainId());
        log.info("finish exec method[removeByChainAndGroupAndNode], chain:{},  group:{}, node:{} deleteCount", chain, group, node, deleteCount);
    }


//    @Transactional(propagation = Propagation.REQUIRED)
//    public void updateFrontMapStatus(String chainId,int frontId, GroupStatus status) {
//        // update status
//        log.info("Update front:[{}] all group map to status:[{}]", frontId, status);
//        tbFrontGroupMapMapper.updateAllGroupsStatus(frontId,status.getValue());
//        this.frontGroupMapCache.clearMapList(chainId);
//    }
//
//    @Transactional(propagation = Propagation.REQUIRED)
//    public void updateFrontMapStatus(String chainId, int frontId, String groupId, GroupStatus status) {
//        // update status
//        log.info("Update front:[{}] group:[{}] map to status:[{}]", frontId, groupId, status);
//        tbFrontGroupMapMapper.updateOneGroupStatus(frontId,status.getValue(),groupId);
//        this.frontGroupMapCache.clearMapList(chainId);
//    }
}
