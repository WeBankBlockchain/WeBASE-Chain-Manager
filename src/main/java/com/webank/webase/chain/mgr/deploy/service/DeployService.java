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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.repository.bean.TbHost;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbConfigMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbHostMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeployService {

    @Autowired private TbConfigMapper tbConfigMapper;
    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbFrontMapper frontMapper;
    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private HostService hostService;
    @Autowired private ChainService chainService;
    @Autowired private AsyncService asyncService;
    @Autowired private PathService pathService;
    @Autowired private ConstantProperties constantProperties;

    /**
     *
     * @param deploy
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deployChain(ReqDeploy deploy) throws BaseException {
        if (StringUtils.isBlank(deploy.getChainName())) {
            throw new BaseException(ConstantCode.PARAM_EXCEPTION);
        }

        // TODO. check WeBASE Sign accessible
//        deploy.getWebaseSignAddr();

        // generate config files and insert data to db
        this.chainService.generateChainConfig(deploy);

        // init host and start node
        this.asyncService.initHostListAndStart();
    }


}

