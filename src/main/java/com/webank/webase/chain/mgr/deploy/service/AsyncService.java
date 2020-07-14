/**
 * Copyright 2014-2020  the original author or authors.
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

package com.webank.webase.chain.mgr.deploy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.repository.bean.TbHost;
import com.webank.webase.chain.mgr.repository.mapper.TbHostMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AsyncService {

    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private HostService hostService;
    @Autowired private ConstantProperties constant;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;


    @Async("deployAsyncScheduler")
    public void initHostList(List<TbHost> tbHostList) {
        try {
            this.hostService.initHostList(tbHostList);
        } catch (InterruptedException e) {
            log.error("Init host error",e);
        }

    }
}


