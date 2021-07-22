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
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.DeployTypeEnum;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.FrontTypeEnum;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.deploy.config.NodeConfig;
import com.webank.webase.chain.mgr.deploy.req.DeployHost;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.deploy.service.DeployShellService;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.deploy.service.docker.DockerOptions;
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
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.chain.mgr.task.TaskManager;
import com.webank.webase.chain.mgr.util.NetUtils;
import com.webank.webase.chain.mgr.util.NumberUtil;
import com.webank.webase.chain.mgr.util.SshUtil;
import com.webank.webase.chain.mgr.util.ThymeleafUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.aop.framework.AopContext;
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
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private PathService pathService;
    @Autowired
    private DockerOptions dockerOptions;


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

        // chainType
        FrontInfo frontInfo = chainInfo.getFrontList().get(0);
        Integer chainType = frontInterface.getEncryptTypeFromSpecificFront(frontInfo.getFrontPeerName(), frontInfo.getFrontIp(), frontInfo.getFrontPort());
        tbChain.setChainType(chainType.byteValue());

        //chain version
        ClientVersionDTO clientVersionDTO = frontInterface.getClientVersionFromSpecificFront(frontInfo.getFrontPeerName(), frontInfo.getFrontIp(), frontInfo.getFrontPort());
        tbChain.setVersion(clientVersionDTO.getVersion());

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
        if (Objects.nonNull(tbChainMapper.selectByPrimaryKey(chainInfo.getChainId())))
            throw new BaseException(ConstantCode.CHAIN_ID_EXISTS);

        // check name
        if (tbChainMapper.countByName(chainInfo.getChainName()) > 0)
            throw new BaseException(ConstantCode.CHAIN_NAME_EXISTS);

        Integer encryptType = null;// front's encrypt type same as chain(guomi or standard)
        String buildTime = null;// node's build time
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

            //check ssh
            if (StringUtils.isNotBlank(front.getSshUser()) && Objects.nonNull(front.getSshPort())) {
                //check ssh connect
                SshUtil.verifyHostConnect(front.getFrontIp(), front.getSshUser(), front.getSshPort(), constantProperties.getPrivateKey());
                //check port
                Integer[] portArray = new Integer[]{front.getChannelPort(), front.getP2pPort(), front.getJsonrpcPort()};
                Arrays.stream(portArray).forEach(port -> {
                    if (Objects.nonNull(port)) {
                        Pair<Boolean, Integer> portReachable = NetUtils.anyPortNotInUse(front.getFrontIp(),
                                front.getSshUser(),
                                front.getSshPort(),
                                constantProperties.getPrivateKey(),
                                port);
                        if (portReachable.getKey()) {
                            String message = String.format("Port:[%1d] is not in use on host :[%2s] failed", portReachable.getValue(), front.getFrontIp());
                            throw new BaseException(ConstantCode.CHECK_PORT_NOT_SUCCESS.getCode(), message);
                        }
                    }
                });
            }
        }

        log.info("finish checkBeforeAddNewChain ");
    }


    /**
     * remove chain
     */
    @Transactional
    public void removeChain(Integer chainId) {
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
            // stop docker in remote host and remove dir in remote host
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

            log.info("Delete chain:[{}] config files", chainId);
            try {
                this.pathService.deleteChain(chain.getChainName());
            } catch (IOException e) {
                log.error("Delete chain:[{}:{}] files error", chainId, chain.getChainName(), e);
            }
        } finally {
            isDeleting.set(false);
        }
    }

    /**
     * @param deploy
     */
    @Transactional
    public void generateChainConfig(ReqDeploy deploy, DockerImageTypeEnum dockerImageTypeEnum) {
        // check deploy count
        int totalNodeNum = deploy.getDeployHostList().stream().mapToInt(DeployHost::getNum).sum();
        if (totalNodeNum < 2) {
            throw new BaseException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        log.info("Check chainId:[{}] exists....", deploy.getChainId());
        TbChain chain = tbChainMapper.selectByPrimaryKey(deploy.getChainId());
        if (chain != null) {
            throw new BaseException(ConstantCode.CHAIN_ID_EXISTS);
        }

        log.info("Check chainName:[{}] exists....", deploy.getChainName());
        chain = tbChainMapper.getByChainName(deploy.getChainName());
        if (chain != null) {
            throw new BaseException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        // get encrypt type
        EncryptTypeEnum encryptType = EncryptTypeEnum.getById(deploy.getEncryptType());

        // build ipConf
        String[] ipConf = new String[CollectionUtils.size(deploy.getDeployHostList())];
        for (int i = 0; i < deploy.getDeployHostList().size(); i++) {
            DeployHost host = deploy.getDeployHostList().get(i);
            //check host connect
            SshUtil.verifyHostConnect(host.getIp(), host.getSshUser(), host.getSshPort(), constantProperties.getPrivateKey());

            // check docker image exists
            if (DockerImageTypeEnum.MANUAL == dockerImageTypeEnum) {
                boolean exists = this.dockerOptions.checkImageExists(host.getIp(), host.getDockerDemonPort(),
                        host.getSshUser(), host.getSshPort(), deploy.getVersion());
                if (!exists) {
                    log.error("Docker image:[{}] not exists on host:[{}].", deploy.getVersion(), host.getIp());
                    throw new BaseException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(host.getIp()));
                }
            }

            String ipConfigLine = String.format("%s:%s %s %s %s,%s,%s",
                    host.getIp(), host.getNum(), host.getExtOrgId(), ConstantProperties.DEFAULT_GROUP_ID,
                    host.getP2pPort(), host.getChannelPort(), host.getJsonrpcPort());
            ipConf[i] = ipConfigLine;
        }

        // exec build_chain.sh shell script
        String fiscoVersion = StringUtils.removeStart(deploy.getVersion(), "v");
        deployShellService.execBuildChain(encryptType, ipConf, fiscoVersion, deploy.getChainName());

        try {
            // generate chain config
            ((ChainService) AopContext.currentProxy()).initChainDbData(encryptType, deploy.getVersion(), deploy);
        } catch (Exception e) {
            log.error("Init chain:[{}] data error. remove generated files:[{}]",
                    deploy.getChainName(), this.pathService.getChainRoot(deploy.getChainName()), e);
            try {
                this.pathService.deleteChain(deploy.getChainName());
            } catch (IOException ex) {
                log.error("Delete chain directory error when init chain data throws an exception.", e);
                throw new BaseException(ConstantCode.DELETE_CHAIN_ERROR,
                        "Delete chain directory error when init chain data throws an exception");
            }
            throw e;
        }
    }

    /**
     * 1. insert new tb_chain, save new tb_group
     * 2. insert tb_front, tb_node, front_group_map
     * 3. generate front application.yml
     * @param encryptTypeEnum
     * @param version
     * @param reqDeploy
     */
    @Transactional
    public void initChainDbData(EncryptTypeEnum encryptTypeEnum, String version, ReqDeploy reqDeploy) {
        // insert chain
        final TbChain newChain = ((ChainService) AopContext.currentProxy())
                .insert(reqDeploy.getChainId(), reqDeploy.getChainName(), reqDeploy.getDescription(),
                        version, encryptTypeEnum, ChainStatusEnum.INITIALIZED, reqDeploy.getConsensusType(),
                        reqDeploy.getStorageType(), DeployTypeEnum.API);

        // save group if new , default node count = 0
        groupManager.saveGroup("", null, ConstantProperties.DEFAULT_GROUP_ID,
            newChain.getChainId(), null, 0, "deploy", GroupType.DEPLOY.getValue());

        // insert default group
        Map<String, AtomicInteger> ipIndexMap = new HashMap<>();
        for (DeployHost deployHost : reqDeploy.getDeployHostList()) {
            List<Path> nodeOfIpList = null;
            try {
                nodeOfIpList = pathService.listHostNodesPath(newChain.getChainName(), deployHost.getIp());
            } catch (Exception e) {
                throw new BaseException(ConstantCode.LIST_HOST_NODE_DIR_ERROR, deployHost.getIp());
            }

            List<Path> nodeOfHostList = new ArrayList<>();
            AtomicInteger index = ipIndexMap.get(deployHost.getIp());
            if (index != null) { // exists ip
                for (int i = 0; i < deployHost.getNum(); i++) {
                    nodeOfHostList.add(nodeOfIpList.get(index.get()));
                    index.incrementAndGet();
                }
            } else { // new ip
                for (int i = 0; i < deployHost.getNum(); i++) {
                    nodeOfHostList.add(nodeOfIpList.get(i));
                }
                ipIndexMap.put(deployHost.getIp(), new AtomicInteger(deployHost.getNum()));
            }

            for (Path nodeRoot : CollectionUtils.emptyIfNull(nodeOfHostList)) {
                // get node properties
                NodeConfig nodeConfig = NodeConfig.read(nodeRoot, encryptTypeEnum);

                // frontPort = 5002 + indexOnHost(0,1,2,3...)
                int frontPort = deployHost.getFrontPort() + (nodeConfig.getP2pPort() - deployHost.getP2pPort());

                String frontDesc = String.format("front of chain:[%s] on host:[%s:%s]", newChain.getChainId(),
                        deployHost.getIp(), nodeConfig.getHostIndex());
                // pass object
                TbFront front = TbFront.build(newChain.getChainId(), nodeConfig.getNodeId(), deployHost.getIp(), frontPort,
                        String.valueOf(deployHost.getExtOrgId()), frontDesc, FrontStatusEnum.INITIALIZED, FrontTypeEnum.CHAIN_DEPLOY, version,
                        DockerOptions.getContainerName(deployHost.getRootDirOnHost(), reqDeploy.getChainName(), nodeConfig.getHostIndex()),
                        nodeConfig.getJsonrpcPort(), nodeConfig.getP2pPort(), nodeConfig.getChannelPort(), reqDeploy.getChainName(),
                        deployHost.getExtCompanyId(), deployHost.getExtOrgId(), deployHost.getExtHostId(), nodeConfig.getHostIndex(),
                        deployHost.getSshUser(), deployHost.getSshPort(), deployHost.getDockerDemonPort(), deployHost.getRootDirOnHost(),
                        PathService.getNodeRootOnHost(PathService
                                .getChainRootOnHost(deployHost.getRootDirOnHost(), reqDeploy.getChainName()), nodeConfig.getHostIndex()));

                this.frontService.insert(front);

                // insert node and front group mapping
                String nodeName = NodeService.getNodeName(newChain.getChainId(), ConstantProperties.DEFAULT_GROUP_ID, nodeConfig.getNodeId());
                this.nodeService.insert(newChain.getChainId(), nodeConfig.getNodeId(), nodeName,
                        ConstantProperties.DEFAULT_GROUP_ID, deployHost.getIp(), nodeConfig.getP2pPort(),
                        nodeName, DataStatus.INVALID);

                // insert front group mapping
                this.frontGroupMapService.newFrontGroup(newChain.getChainId(), front.getFrontId(), ConstantProperties.DEFAULT_GROUP_ID);

                // generate front application.yml
                try {
                    ThymeleafUtil.newFrontConfig(nodeRoot, (byte) encryptTypeEnum.getType(), nodeConfig.getChannelPort(), frontPort);
                } catch (IOException e) {
                    throw new BaseException(ConstantCode.GENERATE_FRONT_YML_ERROR);
                }
            }

            // update node count of goup
            TbGroup group = this.tbGroupMapper.selectByPrimaryKey(ConstantProperties.DEFAULT_GROUP_ID, newChain.getChainId());
            this.groupService.updateGroupNodeCount(newChain.getChainId(), ConstantProperties.DEFAULT_GROUP_ID, group.getNodeCount() + deployHost.getNum());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbChain insert(int chainId, String chainName, String chainDesc, String version, EncryptTypeEnum encryptType, ChainStatusEnum status,
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
    public boolean updateStatus(int chainId, ChainStatusEnum newStatus, String remark) {
        log.info("Update chain:[{}] status to:[{}]", chainId, newStatus.toString());
        TbChain newChain = new TbChain();
        newChain.setChainId(chainId);
        newChain.setChainStatus(newStatus.getId());
        newChain.setModifyTime(new Date());
        newChain.setRemark(remark);
        return this.tbChainMapper.updateByPrimaryKeySelective(newChain) == 1;
    }

    /**
     * @param ip
     * @param rootDirOnHost
     * @param chainName
     */
    public static void mvChainOnRemote(String ip, String rootDirOnHost, String chainName, String sshUser, int sshPort, String privateKey) {
        // create /opt/fisco/deleted-tmp/ as a parent dir
        String deleteRootOnHost = PathService.getDeletedRootOnHost(rootDirOnHost);
        SshUtil.createDirOnRemote(ip, deleteRootOnHost, sshUser, sshPort, privateKey);

        // like /opt/fisco/default_chain
        String src_chainRootOnHost = PathService.getChainRootOnHost(rootDirOnHost, chainName);
        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss
        String dst_chainDeletedRootOnHost = PathService.getChainDeletedRootOnHost(rootDirOnHost, chainName);

        SshUtil.mvDirOnRemote(ip, src_chainRootOnHost, dst_chainDeletedRootOnHost, sshUser, sshPort, privateKey);
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
