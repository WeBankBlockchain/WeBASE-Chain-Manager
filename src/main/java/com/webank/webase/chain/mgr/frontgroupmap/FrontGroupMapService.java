/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.frontgroupmap;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.chain.mgr.frontgroupmap.entity.MapListParam;
import com.webank.webase.chain.mgr.repository.bean.TbFrontGroupMap;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class FrontGroupMapService {

    @Autowired
    private TbFrontGroupMapMapper tbFrontGroupMapMapper;

    /**
     * add new mapping
     */
    @Transactional
    public TbFrontGroupMap newFrontGroup(Integer chainId, Integer frontId, Integer groupId) {

        //add db
        TbFrontGroupMap exists = this.tbFrontGroupMapMapper.selectByChainIdAndFrontIdAndGroupId(chainId, frontId, groupId);
        if (exists == null){
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
    public void removeByChainId(int chainId) {
        if (chainId == 0) {
            return;
        }
        //remove by chainId
        this.tbFrontGroupMapMapper.deleteByChainId(chainId);
    }
    
    /**
     * remove by groupId
     */
    public void removeByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        //remove by groupId
        this.tbFrontGroupMapMapper.deleteByGroupId(chainId,groupId);
    }

    /**
     * remove by frontId
     */
    public void removeByFrontId(int frontId) {
        if (frontId == 0) {
            return;
        }
        //remove by frontId
        this.tbFrontGroupMapMapper.deleteByFrontId(frontId);
    }

    /**
     * get map list by groupId
     */
    public List<FrontGroup> listByGroupId(int groupId) {
        if (groupId == 0) {
            return null;
        }
        return this.tbFrontGroupMapMapper.selectByGroupId(groupId);
    }
}
