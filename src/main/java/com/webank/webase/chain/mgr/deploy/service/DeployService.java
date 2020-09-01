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

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DeployService {
    @Autowired private ChainService chainService;
    @Autowired private NodeAsyncService nodeAsyncService;
    @Autowired private ConstantProperties constantProperties;

    /**
     * @param deploy
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deployChain(ReqDeploy deploy) throws BaseException {
        DockerImageTypeEnum imageTypeEnum = DockerImageTypeEnum.getById(deploy.getDockerImageType());
        if (imageTypeEnum == null) {
            throw new BaseException(ConstantCode.UNKNOWN_DOCKER_IMAGE_TYPE);
        }

        // check image tar file when install with offline
        if (imageTypeEnum == DockerImageTypeEnum.LOCAL_OFFLINE){
            String localTarFile = String.format(constantProperties.getImageTar(),deploy.getVersion());
            if(FileUtil.notExists(localTarFile)){
                log.error("Image tar file:[{}] not exists when use local offline.",localTarFile);
                throw new BaseException(ConstantCode.FILE_NOT_EXISTS.attach(localTarFile));
            }
        }

        // generate config files and insert data to db
        this.chainService.generateChainConfig(deploy, imageTypeEnum);

        // init host and start node
        this.nodeAsyncService.asyncDeployChain(deploy, OptionType.DEPLOY_CHAIN, imageTypeEnum);
    }


}

