/*
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.chain.mgr.precompiledapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * Precompiled common service including management of CNS, node consensus status, CRUD based on
 * PrecompiledWithSignService
 */
@Service
public class PrecompiledService {
    @Autowired
    private PrecompiledWithSignService precompiledWithSignService;


    /**
     * Consensus config related
     */
    public String addSealer(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.addSealer(groupId, signUserId, nodeId);
        return res;
    }

    public String addObserver(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.addObserver(groupId, signUserId, nodeId);
        return res;
    }

    public String removeNode(int groupId, String signUserId, String nodeId) {
        String res = precompiledWithSignService.removeNode(groupId, signUserId, nodeId);
        return res;
    }

}
