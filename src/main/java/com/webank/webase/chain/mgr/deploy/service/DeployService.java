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

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.code.RetCode;
import com.webank.webase.chain.mgr.base.enums.DeployTypeEnum;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.chain.ChainManager;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.req.ReqAddNode;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class DeployService {
    @Autowired private ChainService chainService;
    @Autowired private NodeService nodeService;
    @Autowired private ImageService imageService;
    @Autowired private ChainManager chainManager;

    /**
     * 1. check image locally
     * 2. generate chain config files locally & init chain db data
     * @param deploy
     * @param imageTypeEnum
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deployChain(ReqDeploy deploy, DockerImageTypeEnum imageTypeEnum) throws BaseException {

        // check image tar file when install with offline
        imageService.checkLocalImageByDockerImageTypeEnum(imageTypeEnum, deploy.getVersion());
        // generate config files and insert data to db
        this.chainService.generateChainConfig(deploy, imageTypeEnum);
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> addNodes(ReqAddNode addNode, DockerImageTypeEnum imageTypeEnum)
        throws BaseException {
        // check chain not added by adding front
        TbChain chain = chainManager.requireChainIdExist(addNode.getChainId());
        if (DeployTypeEnum.MANUALLY.getType() == chain.getDeployType()) {
            log.error("addNodes error: manually added chain cannot add new nodes, chain:{}", chain);
            throw new BaseException(ConstantCode.MANUALLY_ADDED_CHAIN_NOT_SUPPORT_ADD_NODE);
        }

        // generate config files and insert data to db
        try {
            // check docker image of version before add in nodeService
            return this.nodeService.addNodes(addNode, imageTypeEnum);
        } catch (InterruptedException e) {
            log.error("addNode error :[]", e);
            throw new BaseException(ConstantCode.ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void stopNode(int chainId, String nodeId) {
        nodeService.stopNode(chainId, nodeId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(int chainId, String nodeId) {
        nodeService.deleteNode(chainId, nodeId);
    }

}

