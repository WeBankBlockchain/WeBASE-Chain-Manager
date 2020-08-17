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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.util.NetUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeployService {
    @Autowired private ChainService chainService;
    @Autowired private NodeAsyncService nodeAsyncService;

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

        // check WeBASE Sign accessible
        if (StringUtils.isBlank(deploy.getWebaseSignAddr())
                || !NetUtils.checkAddress(deploy.getWebaseSignAddr(), 2000)) {
            throw new BaseException(ConstantCode.WEBASE_SIGN_CONFIG_ERROR);
        }

        // generate config files and insert data to db
        this.chainService.generateChainConfig(deploy, imageTypeEnum);

        // init host and start node
        this.nodeAsyncService.asyncDeployChain(deploy, OptionType.DEPLOY_CHAIN);
    }

}

