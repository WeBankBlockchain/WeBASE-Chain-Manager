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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;

import lombok.extern.slf4j.Slf4j;

@Deprecated
@Component
@Slf4j
public class DeployService {
    @Autowired private ChainService chainService;
    @Autowired private ConstantProperties constantProperties;
    @Autowired private ImageService imageService;

    /**
     * @param deploy
     * @param imageTypeEnum
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deployChain(ReqDeploy deploy, DockerImageTypeEnum imageTypeEnum) throws BaseException {

        // check image tar file when install with offline
        imageService.checkLocalImageByDockerImageTypeEnum(imageTypeEnum,deploy.getVersion());
        // generate config files and insert data to db
//        this.chainService.generateChainConfig(deploy, imageTypeEnum);
    }


}

