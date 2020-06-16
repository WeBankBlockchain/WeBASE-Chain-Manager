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


import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FrontGroupMapCache {

    @Autowired
    private FrontGroupMapService mapService;

    private static Map<Integer, List<FrontGroup>> mapList = new ConcurrentHashMap<>();


    /**
     * clear mapList.
     */
    public void clearMapList(int chainId) {
        mapList.remove(chainId);
    }

    /**
     * reset mapList.
     */
    public Map<Integer, List<FrontGroup>> resetMapList(int chainId, int groupId) {
        MapListParam param = new MapListParam();
        param.setChainId(chainId);
        param.setGroupId(groupId);
        mapList.put(chainId, mapService.getList(param));
        return mapList;
    }

    /**
     * get mapList.
     */
    public List<FrontGroup> getMapListByChainId(int chainId, int groupId) {
        List<FrontGroup> list = getAllMap(chainId, groupId);
        if (list == null) {
            return null;
        }
        return list;
    }

    /**
     * get all mapList.
     */
    public List<FrontGroup> getAllMap(int chainId, int groupId) {
        if (mapList == null || mapList.get(chainId) == null) {
            mapList = resetMapList(chainId, groupId);
        }
        return mapList.get(chainId);
    }
}
