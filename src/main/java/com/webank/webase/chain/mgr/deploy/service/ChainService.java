package com.webank.webase.chain.mgr.deploy.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.crypto.EncryptType;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.deploy.config.IpConfigParse;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbConfig;
import com.webank.webase.chain.mgr.repository.bean.TbHost;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbConfigMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbHostMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Service
public class ChainService {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbHostMapper tbHostMapper;
    @Autowired private TbConfigMapper tbConfigMapper;

    @Autowired private ConstantProperties constantProperties;
    @Autowired private DeployShellService deployShellService;
    /**
     *
     * @param deploy
     */
    @Transactional
    public void generateChainConfig(ReqDeploy deploy){
        log.info("Check chainName:[{}] exists....",deploy.getChainName());
        TbChain chain = tbChainMapper.getByChainName(deploy.getChainName());
        if (chain != null) {
            throw new BaseException(ConstantCode.CHAIN_NAME_EXISTS_ERROR);
        }

        // check tagId existed
        TbConfig imageConfig = this.tbConfigMapper.selectByPrimaryKey(deploy.getTagId());
        if (imageConfig == null || StringUtils.isBlank(imageConfig.getConfigValue())) {
            throw new BaseException(ConstantCode.TAG_ID_PARAM_ERROR);
        }

        byte encryptType = (byte) (imageConfig.getConfigValue().endsWith("-gm") ?
                EncryptType.SM2_TYPE : EncryptType.ECDSA_TYPE);

        // build ipConf
        String[] ipConf = new String[CollectionUtils.size(deploy.getDeployHostList())];
        // deploy configurations
        List<IpConfigParse> ipConfigParseList = new ArrayList<>(ipConf.length);
        for (int i = 0; i < deploy.getDeployHostList().size(); i++) {
            ReqDeploy.DeployHost deployHost = deploy.getDeployHostList().get(i);
            TbHost tbHost = this.tbHostMapper.selectByPrimaryKey(deployHost.getHostId());
            if (tbHost == null){
                throw new BaseException(ConstantCode.HOST_NOT_EXISTS, String.format("Host not exists of host id:[%s]",deployHost.getHostId()));
            }
            String ipConfigLine = String.format("%s:%s %s %s", tbHost.getIp(), deployHost.getNum(), ConstantProperties.DEFAULT_GROUP_ID );
            ipConf[i] = ipConfigLine;


        }

        // exec build_chain.sh shell script
        deployShellService.execBuildChain(encryptType, ipConf, deploy.getChainName());

        // generate chain config
        ((ChainService) AopContext.currentProxy()).initChainDbData(deploy.getChainName(), ipConfigParseList,
                deploy.getWebaseSignAddr(),imageConfig, encryptType);
    }
    /**
     *
     * @param chainName
     * @param ipConfigParseList
     * @param rootDirOnHost
     * @param webaseSignAddr
     * @param imageConfig
     * @param encryptType
     */
    @Transactional
    public void initChainDbData(String chainName, List<IpConfigParse> ipConfigParseList,
                                String rootDirOnHost, String webaseSignAddr, TbConfig imageConfig, byte encryptType,
                                String sshUser,int sshPort,int dockerPort){

        // insert chain
        final TbChain newChain = ((ChainService) AopContext.currentProxy()).insert(chainName, chainName,
                imageConfig.getConfigValue(), encryptType, ChainStatusEnum.INITIALIZED, rootDirOnHost,
                RunTypeEnum.DOCKER, webaseSignAddr);

        // all host ips
        Map<String,TbHost> newIpHostMap = new HashMap<>();

        // insert agency, host , group
        ipConfigParseList.forEach((config) -> {
            // insert agency if new
            TbAgency agency = this.agencyService.insertIfNew(config.getAgencyName(),newChain.getId(),chainName);

            // insert host if new
            TbHost host = this.hostService.insertIfNew(agency.getId(), agency.getAgencyName(), config.getIp(), rootDirOnHost,
                    sshUser,sshPort,dockerPort);

            // insert group if new
            config.getGroupIdSet().forEach((groupId) -> {
                this.groupService.insertIfNew(groupId, config.getNum(), "deploy", GroupType.DEPLOY,
                        GroupStatus.MAINTAINING, newChain.getId(), newChain.getChainName());
            });

            newIpHostMap.putIfAbsent(config.getIp(),host);
        });

        // insert nodes for all hosts. there may be multiple nodes on a host.
        newIpHostMap.keySet().forEach((ip) -> {
            List<Path> nodePathList = null;
            try {
                nodePathList = pathService.listHostNodesPath(newChain.getChainName(), ip);
            } catch (Exception e) {
                throw new BaseException(ConstantCode.LIST_HOST_NODE_DIR_ERROR.attach(ip));
            }

            for (Path nodeRoot : CollectionUtils.emptyIfNull(nodePathList)) {
                // get node properties
                NodeConfig nodeConfig = NodeConfig.read(nodeRoot);

                // frontPort = 5002 + indexOnHost(0,1,2,3...)
                int frontPort = constant.getDefaultFrontPort() + nodeConfig.getHostIndex();

                // host
                TbHost host = newIpHostMap.get(ip);
                // agency
                TbAgency agency = this.tbAgencyMapper.selectByPrimaryKey(host.getAgencyId());
                // insert front
                TbFront front = TbFront.init(nodeConfig.getNodeId(), ip, frontPort,
                        agency.getId(),agency.getAgencyName(), imageConfig.getConfigValue(),
                        RunTypeEnum.DOCKER , host.getId(), nodeConfig.getHostIndex(),
                        imageConfig.getConfigValue(), DockerOptions.getContainerName(rootDirOnHost, chainName,
                                nodeConfig.getHostIndex()), nodeConfig.getJsonrpcPort(), nodeConfig.getP2pPort(),
                        nodeConfig.getChannelPort(), newChain.getId(), newChain.getChainName(), FrontStatusEnum.INITIALIZED);
                this.frontService.insert(front);


                // insert node and front group mapping
                Set<Integer> groupIdSet = ipConfigParseList.stream().map(IpConfigParse::getGroupIdSet)
                        .flatMap(Collection::stream).collect(Collectors.toSet());

                groupIdSet.forEach((groupId) -> {
                    // insert node
                    String nodeName = NodeService.getNodeName(groupId, nodeConfig.getNodeId());
                    this.nodeService.insert(nodeConfig.getNodeId(), nodeName,
                            groupId, ip, nodeConfig.getP2pPort(),
                            nodeName, DataStatus.STARTING);

                    // insert front group mapping
                    this.frontGroupMapService.newFrontGroup(front.getFrontId(), groupId, GroupStatus.MAINTAINING);

                    // update node count of goup
                    TbGroup group = this.groupService.getGroupById(groupId);
                    this.groupService.updateGroupNodeCount(groupId, group.getNodeCount() + 1 );
                });

                // generate front application.yml
                try {
                    ThymeleafUtil.newFrontConfig(nodeRoot,encryptType,nodeConfig.getChannelPort(),
                            frontPort,webaseSignAddr);
                } catch (IOException e) {
                    throw new BaseException(ConstantCode.GENERATE_FRONT_YML_ERROR);
                }
            }
        });
    }

}