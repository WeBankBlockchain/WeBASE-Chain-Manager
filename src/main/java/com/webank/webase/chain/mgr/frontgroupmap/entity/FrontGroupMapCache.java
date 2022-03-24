/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.frontgroupmap.entity;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;

@Slf4j
@Component
public class FrontGroupMapCache {

    @Autowired private TbFrontGroupMapMapper tbFrontGroupMapMapper;

    private static Map<String, List<FrontGroup>> mapList = new ConcurrentHashMap<String, List<FrontGroup>>();

    /**
     * clear mapList.
     */
    public void clearMapList(String chainId) {
        mapList.remove(chainId);
    }

    /**
     * reset mapList.
     */
    public Map<String, List<FrontGroup>> resetMapList(String chainId) {
        List<FrontGroup> frontGroupList = this.tbFrontGroupMapMapper.selectByChainId(chainId);
        if (frontGroupList.size() > 0) {
            mapList.put(chainId, frontGroupList);
        }
        log.info("get frontGroupList size is 0");
        return mapList;
    }

    /**
     * get mapList.
     */
    public List<FrontGroup> getMapListByChainId(String chainId, String groupId) {
        List<FrontGroup> list = getAllMap(chainId);
        if (list == null) {
            return null;
        }
        List<FrontGroup> map =
                list.stream().filter(m -> m.getGroupId().equals(groupId)).collect(Collectors.toList());
        return map;
    }

    /**
     * get all mapList.
     */
    public List<FrontGroup> getAllMap(String chainId) {
        if (mapList == null || mapList.get(chainId) == null) {
            mapList = resetMapList(chainId);
        }
        return mapList.get(chainId);
    }
}
