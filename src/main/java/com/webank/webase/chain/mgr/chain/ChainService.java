/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.chain;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.DeployTypeEnum;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.ClientVersionDTO;
import com.webank.webase.chain.mgr.front.entity.FrontInfo;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.chain.mgr.task.TaskManager;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.util.NetUtils;
import com.webank.webase.chain.mgr.util.NumberUtil;
import com.webank.webase.chain.mgr.util.SshUtil;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * service of chain.
 */
@Log4j2
@Service
public class ChainService {

    /**
     * Is operating chain
     */
    public static AtomicBoolean isDeleting = new AtomicBoolean(false);


    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private TbGroupMapper tbGroupMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private GroupManager groupManager;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    @Lazy
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private ConstantProperties constantProperties;


    /**
     * add new chain
     */
    @Transactional
    public TbChain newChain(ChainInfo chainInfo) {
        log.info("start newChain chainInfo:{}", chainInfo);
        //check before new chain
        checkBeforeAddNewChain(chainInfo);

        // copy attribute
        TbChain tbChain = new TbChain();
        BeanUtils.copyProperties(chainInfo, tbChain);
        Date now = new Date();
        tbChain.setCreateTime(now);
        tbChain.setModifyTime(now);
        tbChain.setRemark("");
        tbChain.setChainStatus(ChainStatusEnum.RUNNING.getId());
        tbChain.setChainType(chainInfo.getChainType().byteValue());
        if (chainInfo.getDeployType() == null) {
            tbChain.setDeployType(DeployTypeEnum.MANUALLY.getType());
        }
        // fix add chain or visual deploy chain
        if (chainInfo.getChainType() == null) {
            tbChain.setDeployType(DeployTypeEnum.API.getType());

            FrontInfo frontInfo = chainInfo.getFrontList().get(0);
            Integer chainType = frontInterface
                .getEncryptTypeFromSpecificFront(frontInfo.getFrontPeerName(),
                    frontInfo.getFrontIp(), frontInfo.getFrontPort());
            tbChain.setChainType(chainType.byteValue());
            //chain version
            ClientVersionDTO clientVersionDTO = frontInterface.getClientVersionFromSpecificFront(frontInfo.getFrontPeerName(), frontInfo.getFrontIp(), frontInfo.getFrontPort());
            tbChain.setVersion(clientVersionDTO.getVersion());
        }
        log.info("newChain tbChain:{}", tbChain);
        // save chain info
        int result = tbChainMapper.insertSelective(tbChain);
        if (result == 0) {
            log.warn("fail newChain, after save, tbChain:{}", JsonTools.toJSONString(tbChain));
            throw new BaseException(ConstantCode.SAVE_CHAIN_FAIL);
        }
        if (CollectionUtils.isNotEmpty(chainInfo.getFrontList())) {
            chainInfo.getFrontList().forEach((front) -> {
                log.info("Add front [{}:{}]", front.getFrontIp(), front.getFrontPort());
                front.setChainId(chainInfo.getChainId());
                frontService.newFront(front);
            });
        }

        log.info("finish newChain chainId:{}", tbChain.getChainId());
        return tbChainMapper.selectByPrimaryKey(tbChain.getChainId());
    }


    /**
     * @param chainInfo
     */
    private void checkBeforeAddNewChain(ChainInfo chainInfo) {
        log.info("start checkBeforeAddNewChain chainInfo:{}", JsonTools.objToString(chainInfo));
        // check id
        if (Objects.nonNull(tbChainMapper.selectByPrimaryKey(chainInfo.getChainId()))) {
            throw new BaseException(ConstantCode.CHAIN_ID_EXISTS);
        }
        // check name
        if (tbChainMapper.countByName(chainInfo.getChainName()) > 0) {
            throw new BaseException(ConstantCode.CHAIN_NAME_EXISTS);
        }
        Integer encryptType = null;// front's encrypt type same as chain(guomi or standard)
        String buildTime = null;// node's build time

        // fix add chain or visual deploy chain
        List<FrontInfo> frontInfos = chainInfo.getFrontList();
        if (frontInfos == null) {
            log.info("new chain of added(not visual deploy chain)");
            return;
        }
        for (int i = 0; i < chainInfo.getFrontList().size(); i++) {
            FrontInfo front = chainInfo.getFrontList().get(i);
            log.info("check front [{}:{}]", front.getFrontIp(), front.getFrontPort());
            String frontPeerName = front.getFrontPeerName();
            String frontIp = front.getFrontIp();
            Integer frontPort = front.getFrontPort();

            // check front ip and port
            CommonUtils.checkServerConnect(frontIp, frontPort);

            //check encryptType and build time
            if (i == 0) {
                encryptType = frontInterface.getEncryptTypeFromSpecificFront(frontPeerName, frontIp, frontPort);
                ClientVersionDTO clientVersionDTO = frontInterface.getClientVersionFromSpecificFront(frontPeerName, frontIp, frontPort);
                buildTime = clientVersionDTO.getBuildTime();
            } else {
                //check encryptType
                if (!Objects.equals(encryptType, frontInterface.getEncryptTypeFromSpecificFront(frontPeerName, frontIp, frontPort))) {
                    log.error("fail checkBeforeAddNewChain, frontIp:{},frontPort:{},front's encryptType not match first encryptType:{}", frontIp, frontPort, encryptType);
                    throw new BaseException(ConstantCode.ENCRYPT_TYPE_NOT_MATCH);
                }

                //check build time
                ClientVersionDTO clientVersionDTO = frontInterface.getClientVersionFromSpecificFront(frontPeerName, frontIp, frontPort);
                if (!Objects.equals(buildTime, clientVersionDTO.getBuildTime())) {
                    log.error("fail checkBeforeAddNewChain, frontIp:{},frontPort:{},front's buildTime not match first buildTime:{}", frontIp, frontPort, buildTime);
                    throw new BaseException(ConstantCode.BUILD_TIME_NOT_MATCH);
                }
            }
        }

        log.info("finish checkBeforeAddNewChain ");
    }


    /**
     * remove chain
     */
    @Transactional
    public void removeChain(String chainId) {
        // check chainId
        TbChain chain = tbChainMapper.selectByPrimaryKey(chainId);
        if (chain == null) {
            throw new BaseException(ConstantCode.INVALID_CHAIN_ID);
        }

        try {
            isDeleting.set(true);

            // remove chain
            tbChainMapper.deleteByPrimaryKey(chainId);
            // remove group
            groupService.removeByChainId(chainId);
            // remove front
            frontService.removeByChainId(chainId);
            // remove front group map
            this.frontGroupMapService.removeByChainId(chainId);
            // remove node
            nodeService.deleteByChainId(chainId);
            // remove contract
            contractService.deleteContractByChainId(chainId);
            // clear cache
            frontGroupMapCache.clearMapList(chainId);
            // reset group list
            resetGroupListTask.asyncResetGroupList();
            // remove task
            taskManager.removeByChainId(chainId);

        } finally {
            isDeleting.set(false);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbChain insert(String chainId, String chainName, String chainDesc, String version, EncryptTypeEnum encryptType, ChainStatusEnum status,
                          String consensusType, String storageType, DeployTypeEnum deployTypeEnum) throws BaseException {
        TbChain chain = TbChain.init(chainId, chainName, chainDesc, version, consensusType, storageType, encryptType, status, deployTypeEnum);

        if (tbChainMapper.insertSelective(chain) != 1) {
            throw new BaseException(ConstantCode.INSERT_CHAIN_ERROR);
        }
        return chain;
    }


    /**
     * @param chainId
     * @param newStatus
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(String chainId, ChainStatusEnum newStatus, String remark) {
        log.info("Update chain:[{}] status to:[{}]", chainId, newStatus.toString());
        TbChain newChain = new TbChain();
        newChain.setChainId(chainId);
        newChain.setChainStatus(newStatus.getId());
        newChain.setModifyTime(new Date());
        newChain.setRemark(remark);
        return this.tbChainMapper.updateByPrimaryKeySelective(newChain) == 1;
    }

    /**
     * run task.
     *
     * @return
     */
    public boolean runTask(TbChain chain) {
        if (isDeleting.get()) {
            return false;
        }
        if (chain == null) {
            log.error("Run task, chain not exists");
            return false;
        }
        DeployTypeEnum deployTypeEnum = DeployTypeEnum.getById(chain.getDeployType());

        if (deployTypeEnum == DeployTypeEnum.MANUALLY) {
            log.info("Chain:[{}] deployed manually, run task ", chain.getChainId());
            return true;
        }
        if (chain.getChainStatus() == ChainStatusEnum.RUNNING.getId()) {
            return true;
        }
        log.error("Chain:[{}] is not running, cancel reset group task.", chain.getChainId());
        return false;
    }

    /**
     * @param chain
     * @return
     */
    public int progress(TbChain chain) {
        int progress = ChainStatusEnum.progress(chain.getChainStatus());
        switch (progress) {
            // deploy or upgrade failed
            case NumberUtil.PERCENTAGE_FAILED:

                // deploy or upgrade success
            case NumberUtil.PERCENTAGE_FINISH:
                return progress;
            default:
                break;
        }

        // check front start
        return this.frontService.frontProgress(chain.getChainId());
    }


}
