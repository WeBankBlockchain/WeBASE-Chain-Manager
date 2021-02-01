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
package com.webank.webase.chain.mgr.group;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.DeployTypeEnum;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.deploy.service.DeployShellService;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.FrontParam;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.chain.mgr.group.entity.ReqStartGroup;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.repository.bean.*;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private TbFrontMapper tbFrontMapper;
    @Autowired
    private TbGroupMapper tbGroupMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private ChainService chainService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private TbFrontGroupMapMapper tbFrontGroupMapMapper;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ConstantProperties constants;
    @Autowired
    private PathService pathService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private GroupManager groupManager;

    /**
     * generate group to single node.
     *
     * @param req info
     * @return
     */
    @Transactional
    public TbGroup generateToSingleNode(String nodeId, ReqGenerateGroup req) {
        // check id
        Integer chainId = req.getChainId();
        Integer generateGroupId = req.getGenerateGroupId();
        Assert.notNull(req.getGenerateGroupId(), "generateGroupId can not be null");
        Assert.notNull(req.getTimestamp(), "timestamp can not be null");

        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail generateToSingleNode node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }
        TbChain tbChain = chainService.verifyChainId(chainId);

        // save group
        TbGroup tbGroup = saveGroup(req.getGroupName(), req.getTimestamp(), generateGroupId, chainId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue());

        // request front to generate
        GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
        BeanUtils.copyProperties(req, generateGroupInfo);
        frontInterface.generateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                generateGroupInfo);

        // fetch group config file
        if (tbChain.getDeployType() == DeployTypeEnum.API.getType()) {
            this.pullAllGroupFiles(generateGroupId, tbFront);
        }
        return tbGroup;
    }

    /**
     * generate group.
     *
     * @param req info
     * @return
     */
    @Transactional
    public TbGroup generateGroup(ReqGenerateGroup req) {
        //reset all local group
        resetGroupList();

        // check id
        Integer chainId = req.getChainId();
        TbChain tbChain = chainService.verifyChainId(chainId);

        //set groupId
        Integer generateGroupId = req.getGenerateGroupId();
        if (Objects.isNull(generateGroupId)) {
            generateGroupId = tbGroupMapper.getMaxGroup(req.getChainId()) + 1;
        }
        checkGroupIdExisted(chainId, generateGroupId);

        //set groupId
        BigInteger timestamp = req.getTimestamp();
        if (Objects.isNull(timestamp)) {
            timestamp = BigInteger.valueOf(Instant.now().toEpochMilli());
        }

        //get nodeList
        if (CollectionUtils.isEmpty(req.getNodeList())) {
            // select node list from db
            if (CollectionUtils.isEmpty(req.getOrgIdList())) {
                throw new BaseException(ConstantCode.NODE_ID_AND_ORG_LIST_EMPTY);
            }

            Set<String> nodeIdSet = req.getOrgIdList().stream()
                    .map((orgId) -> this.tbFrontMapper.selectByChainIdAndAgencyId(req.getChainId(), orgId))
                    .filter((front) -> front != null)
                    .flatMap(List::stream)
                    .map(TbFront::getNodeId).collect(Collectors.toSet());

            req.setNodeList(new ArrayList<>(nodeIdSet));
        }


        // save group
        TbGroup tbGroup = saveGroup(req.getGroupName(), timestamp, generateGroupId, chainId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue());

        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
            if (tbFront == null) {
                log.error("fail generateGroup node front not exists.");
                throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
            }
            // request front to generate
            GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
            BeanUtils.copyProperties(req, generateGroupInfo);
            generateGroupInfo.setGenerateGroupId(generateGroupId);
            generateGroupInfo.setTimestamp(timestamp);
            frontInterface.generateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                    generateGroupInfo);

            if (tbChain.getDeployType() == DeployTypeEnum.API.getType()) {
                // fetch group config file
                this.pullAllGroupFiles(generateGroupId, tbFront);
            }
        }

        // if create by orgIdList, then start up group
        if (CollectionUtils.isNotEmpty(req.getOrgIdList())) {
            ReqStartGroup reqStartGroup = new ReqStartGroup();
            reqStartGroup.setChainId(req.getChainId());
            reqStartGroup.setGenerateGroupId(generateGroupId);
            reqStartGroup.setNodeList(req.getNodeList());
            this.batchStartGroup(reqStartGroup);
        }
        return tbGroup;
    }

    /**
     * operate group.
     *
     * @param chainId
     * @param nodeId
     * @param groupId
     * @param type
     * @return
     */
    public Object operateGroup(Integer chainId, String nodeId, Integer groupId, String type) {
        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail operateGroup node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }
        TbChain tbChain = chainService.verifyChainId(chainId);
        // request front to operate
        Object groupHandleResult = frontInterface.operateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, type);

        // refresh
        resetGroupList();

        if (tbChain.getDeployType() == DeployTypeEnum.API.getType()) {
            this.pullGroupStatusFile(groupId, tbFront);
        }

        // return
        return groupHandleResult;
    }

    /**
     * batch start group.
     *
     * @param req
     */
    public void batchStartGroup(ReqStartGroup req) {
        Integer groupId = req.getGenerateGroupId();
        // check id
        checkGroupIdValid(req.getChainId(), groupId);
        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByChainIdAndNodeId(req.getChainId(), nodeId);
            if (tbFront == null) {
                log.error("fail batchStartGroup node not exists.");
                throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
            }
            // request front to start
            frontInterface.operateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(), groupId,
                    "start");
        }
        // refresh
        resetGroupList();
    }


    /**
     * save group id
     */
    @Transactional
    public TbGroup saveGroup(String groupName, BigInteger timestamp, int groupId, int chainId, int nodeCount, String description,
                             int groupType) {
        if (groupId == 0) {
            return null;
        }
        // save group id
        if (StringUtils.isBlank(groupName)) {
            groupName = String.format("chain_%s_group_%s", chainId, groupId);
        } else {
            groupManager.requireGroupNameNotFound(groupName);
        }

        TbGroup exists = this.tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (exists == null) {
            TbGroup tbGroup = new TbGroup(timestamp, groupId, chainId, groupName, nodeCount, description, groupType);
            try {
                this.tbGroupMapper.insertSelective(tbGroup);
            } catch (Exception e) {
                log.error("Insert group error", e);
                throw e;
            }
            return tbGroup;
        }
        return exists;
    }

    /**
     * query all group info.
     */
    public List<TbGroup> getGroupList(Integer chainId, Byte groupStatus) throws BaseException {
        log.debug("start getGroupList");
        try {
            List<TbGroup> groupList = null;
            if (groupStatus == null) {
                groupList = this.tbGroupMapper.selectByChainId(chainId);
            } else {
                groupList = this.tbGroupMapper.selectByChainIdAndGroupStatus(chainId, groupStatus);
            }
            log.debug("end getGroupList groupList:{}", JsonTools.toJSONString(groupList));
            return groupList;
        } catch (RuntimeException ex) {
            log.error("fail getGroupList", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update status.
     */
    public void updateGroupStatus(int chainId, int groupId, int groupStatus) {
        log.debug("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        this.tbGroupMapper.updateStatus(chainId, groupId, groupStatus);
        log.debug("end updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(int chainId, int groupId) throws BaseException {
        log.debug("start queryGroupGeneral groupId:{}", groupId);
        GroupGeneral generalInfo = this.tbGroupMapper.getGeneral(chainId, groupId);
        return generalInfo;
    }


    /**
     * reset groupList.
     */
    @Transactional
    public void resetGroupList() {
        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

        List<TbChain> chainList = tbChainMapper.selectAll();
        if (CollectionUtils.isEmpty(chainList)) {
            log.info("No chain found.");
            return;
        }

        for (TbChain tbChain : chainList) {
            if (!this.chainService.runTask(tbChain)) {
                log.warn("Chain status is not running:[{}]", tbChain.getChainStatus());
                continue;
            }

            Integer chainId = tbChain.getChainId();
            // all groupId from chain
            Set<Integer> allGroupSet = new HashSet<>();

            // get all front
            FrontParam param = new FrontParam();
            param.setChainId(chainId);
            List<TbFront> frontList = tbFrontMapper.selectByParam(param);
            if (frontList == null || frontList.size() == 0) {
                log.info("chain {} not found any front.", chainId);
                // remove all group
                // removeAllGroup(chainId);
                continue;
            }

            // get group from chain
            for (TbFront front : frontList) {

                //remove old front-group-map
                tbFrontGroupMapMapper.deleteByFrontId(front.getFrontId());

                String frontPeerName = front.getFrontPeerName();
                String frontIp = front.getFrontIp();
                int frontPort = front.getFrontPort();
                // query group list
                List<String> groupIdList;
                try {
                    groupIdList = frontInterface.getGroupListFromSpecificFront(frontPeerName, frontIp, frontPort);
                } catch (Exception ex) {
                    log.error("fail getGroupListFromSpecificFront frontId:{}.", front.getFrontId(), ex);
                    continue;
                }
                for (String groupId : groupIdList) {
                    Integer gId = Integer.valueOf(groupId);
                    allGroupSet.add(gId);
                    // peer in group
                    List<String> groupPeerList =
                            frontInterface.getGroupPeersFromSpecificFront(frontPeerName, frontIp, frontPort, gId);
                    // save group
                    saveGroup("", null, gId, chainId, groupPeerList.size(), "synchronous",
                            GroupType.SYNC.getValue());

                    //save front-group
                    List<String> sealerList = frontInterface.getSealerListFromSpecificFront(frontPeerName, frontIp, frontPort, gId);
                    if(sealerList.contains(front.getNodeId())){
                        frontGroupMapService.newFrontGroup(chainId, front.getFrontId(), gId);
                    }

                    // save new peers
                    savePeerList(chainId, frontPeerName, frontIp, frontPort, gId, groupPeerList);
                    // remove invalid peers
                    removeInvalidPeer(chainId, gId, groupPeerList);
                    // refresh: add sealer and observer no matter validity
                    frontService.refreshSealerAndObserverInNodeList(frontPeerName, frontIp, frontPort,
                            front.getChainId(), gId);
                }

            }

            // check group status
            checkGroupStatusAndRemoveInvalidGroup(chainId, allGroupSet);
            // clear cache
            frontGroupMapCache.clearMapList(chainId);
        }

        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * Check the validity of the groupId.
     */
    public void checkGroupIdExisted(Integer chainId, Integer groupId) throws BaseException {
        log.debug("start checkGroupIdExisted groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupIdExisted groupId is null");
            throw new BaseException(ConstantCode.GROUP_ID_NULL);
        }

        int groupCount = this.tbGroupMapper.countByChainIdAndGroupId(chainId, groupId);
        log.debug("checkGroupIdExisted groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount > 0) {
            throw new BaseException(ConstantCode.GROUP_ID_EXISTS);
        }
        log.debug("end checkGroupIdExisted");
    }

    /**
     * Check the validity of the groupId.
     */
    public void checkGroupIdValid(Integer chainId, Integer groupId) throws BaseException {
        log.debug("start checkGroupIdValid groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupIdValid groupId is null");
            throw new BaseException(ConstantCode.GROUP_ID_NULL);
        }

        int groupCount = this.tbGroupMapper.countByChainIdAndGroupId(chainId, groupId);
        log.debug("checkGroupIdValid groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount == 0) {
            throw new BaseException(ConstantCode.INVALID_GROUP_ID);
        }
        log.debug("end checkGroupIdValid");
    }

    /**
     * save new peers.
     */
    private void savePeerList(int chainId, String frontPeerName, String frontIp, Integer frontPort, int groupId,
                              List<String> groupPeerList) {
        // get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(chainId, groupId);
        // get peers on chain
        PeerInfo[] peerArr = frontInterface.getPeersFromSpecificFront(frontPeerName, frontIp, frontPort, groupId);
        List<PeerInfo> peerList = Arrays.asList(peerArr);
        // save new nodes
        for (String nodeId : groupPeerList) {
            long count = localNodeList.stream()
                    .filter(ln -> groupId == ln.getGroupId() && nodeId.equals(ln.getNodeId()))
                    .count();
            if (count == 0) {
                PeerInfo newPeer = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                        .findFirst().orElseGet(() -> new PeerInfo(nodeId));
                nodeService.addNodeInfo(chainId, groupId, newPeer);
            }
        }
    }

    /**
     * remove all group.
     */
    private void removeAllGroup(Integer chainId) {
        log.info("start remove all group.");
        List<TbGroup> allGroup = getGroupList(chainId, null);
        if (CollectionUtils.isEmpty(allGroup)) {
            log.info("removeAllGroup jump over. not fount any group");
            return;
        }

        // remove each group
        allGroup.stream().forEach(group -> removeByGroupId(chainId, group.getGroupId()));
    }

    /**
     * remove invalid peer.
     */
    private void removeInvalidPeer(int chainId, int groupId, List<String> groupPeerList) {
        if (groupId == 0) {
            return;
        }
        // get local peers
        List<TbNode> localNodes = nodeService.queryByGroupId(chainId, groupId);
        if (CollectionUtils.isEmpty(localNodes)) {
            return;
        }
        // remove node that's not in groupPeerList and not in sealer/observer list
        localNodes.stream()
                .filter(node -> !groupPeerList.contains(node.getNodeId())
                        && !checkSealerAndObserverListContains(chainId, groupId, node.getNodeId()))
                .forEach(n -> nodeService.deleteByNodeAndGroupId(n.getNodeId(), groupId));
    }

    private boolean checkSealerAndObserverListContains(int chainId, int groupId, String nodeId) {
        log.debug("checkSealerAndObserverListNotContains nodeId:{},groupId:{}", nodeId, groupId);
        // get sealer and observer on chain
        List<PeerInfo> sealerAndObserverList =
                nodeService.getSealerAndObserverList(chainId, groupId);
        for (PeerInfo peerInfo : sealerAndObserverList) {
            if (nodeId.equals(peerInfo.getNodeId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * check group status.
     */
    private void checkGroupStatusAndRemoveInvalidGroup(Integer chainId,
                                                       Set<Integer> allGroupOnChain) {
        if (CollectionUtils.isEmpty(allGroupOnChain)) {
            return;
        }

        List<TbGroup> allLocalGroup = getGroupList(chainId, null);
        if (CollectionUtils.isEmpty(allLocalGroup)) {
            return;
        }

        for (TbGroup localGroup : allLocalGroup) {
            int localGroupId = localGroup.getGroupId();
            long count = allGroupOnChain.stream().filter(id -> id == localGroupId).count();
            try {
                if (count > 0) {
                    log.info("group is valid, chainId:{} localGroupId:{}", chainId, localGroupId);
                    // update NORMAL
                    updateGroupStatus(chainId, localGroupId, DataStatus.NORMAL.getValue());
                    continue;
                }

                Date modifyTime = localGroup.getModifyTime();
                if (!CommonUtils.isDateTimeInValid(CommonUtils.timestamp2LocalDateTime(modifyTime.getTime()),
                        constants.getGroupInvalidGrayscaleValue())) {
                    log.warn("remove group, chainId:{} localGroup:{}", chainId,
                            JsonTools.toJSONString(localGroup));
                    // remove group
                    removeByGroupId(chainId, localGroupId);
                    continue;
                }

                log.warn("group is invalid, chainId:{} localGroupId:{}", chainId, localGroupId);
                if (DataStatus.NORMAL.getValue() == localGroup.getGroupStatus()) {
                    // update invalid
                    updateGroupStatus(chainId, localGroupId, DataStatus.INVALID.getValue());
                    continue;
                }

            } catch (Exception ex) {
                log.info("fail check group. chainId:{} localGroup:{}", chainId,
                        JsonTools.toJSONString(localGroup));
                continue;
            }

        }
    }

    /**
     * remove by groupId.
     */
    private void removeByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        // remove groupId.
        this.tbGroupMapper.deleteByChainIdANdGroupId(chainId, groupId);
        // remove mapping.
        frontGroupMapService.removeByGroupId(chainId, groupId);
        // remove node
        nodeService.deleteByGroupId(chainId, groupId);
        // remove contract
        contractService.deleteByGroupId(chainId, groupId);
    }

    /**
     * remove by chainId.
     */
    public void removeByChainId(int chainId) {
        if (chainId == 0) {
            return;
        }
        // remove chainId.
        this.tbGroupMapper.deleteByChainId(chainId);
    }

    /**
     * update status.
     */
    @Transactional
    public void updateGroupNodeCount(int chainId, int groupId, int nodeCount) {
        log.debug("start updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);
        this.tbGroupMapper.updateNodeCount(chainId, groupId, nodeCount);
        log.debug("end updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);

    }

    private void pullAllGroupFiles(int generateGroupId, TbFront tbFront) {
        this.pullGroupStatusFile(generateGroupId, tbFront);
        this.pullGroupConfigFile(generateGroupId, tbFront);
    }


    /**
     * pull docker node's group config file and group_status file
     * when generateGroup/operateGroup
     *
     * @include group.x.genesis, group.x.ini, .group_status
     */
    private void pullGroupConfigFile(int generateGroupId, TbFront tbFront) {
        // only support docker node/front
        String chainName = tbFront.getChainName();
        int nodeIndex = tbFront.getHostIndex();

        // scp group config files from remote to local
        // path pattern: /host.getRootDir/chain_name
        // ex: (in the remote host) /opt/fisco/chain1
        String remoteChainPath = PathService.getChainRootOnHost(tbFront.getRootOnHost(), chainName);
        // ex: (in the remote host) /opt/fisco/chain1/node0/conf/group.1001.*
        String remoteGroupConfSource = String.format("%s/node%s/conf/group.%s.*",
                remoteChainPath, nodeIndex, generateGroupId);
        // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0
        String localNodePath = pathService.getNodeRoot(chainName, tbFront.getFrontIp(), tbFront.getHostIndex()).toString();
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/conf/group.1001.*
        Path localDst = Paths.get(String.format("%s/conf/", localNodePath, generateGroupId));
        try {
            if (Files.notExists(localDst)) {
                Files.createDirectories(localDst);
            }
            // copy group config files to local node's conf dir
            deployShellService.scp(ScpTypeEnum.DOWNLOAD, tbFront.getSshUser(),
                    tbFront.getFrontIp(), tbFront.getSshPort(), remoteGroupConfSource, localDst.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("Backup group config files:[{} to {}] error.", remoteGroupConfSource, localDst.toAbsolutePath().toString(), e);
        }
    }


    private void pullGroupStatusFile(int generateGroupId, TbFront tbFront) {
        // only support docker node/front
        String chainName = tbFront.getChainName();
        int nodeIndex = tbFront.getHostIndex();
        // scp group status files from remote to local
        // path pattern: /host.getRootDir/chain_name
        // ex: (in the remote host) /opt/fisco/chain1
        String remoteChainPath = PathService.getChainRootOnHost(tbFront.getRootOnHost(), chainName);
        // ex: (in the remote host) /opt/fisco/chain1/node0/data/group1001/.group_status
        String remoteGroupStatusSource = String.format("%s/node%s/data/group%s/.group_status",
                remoteChainPath, nodeIndex, generateGroupId);
        // path pattern: /NODES_ROOT/chain_name/[ip]/node[index]
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0
        String localNodePath = pathService.getNodeRoot(chainName, tbFront.getFrontIp(), tbFront.getHostIndex()).toString();
        // ex: (node-mgr local) ./NODES_ROOT/chain1/127.0.0.1/node0/data/group[groupId]/group.1001.*
        Path localDst = Paths.get(String.format("%s/data/group%s/.group_status", localNodePath, generateGroupId));
        // create data parent directory
        try {
            if (Files.notExists(localDst.getParent())) {
                Files.createDirectories(localDst.getParent());
            }
            // copy group status file to local node's conf dir
            deployShellService.scp(ScpTypeEnum.DOWNLOAD, tbFront.getSshUser(),
                    tbFront.getFrontIp(), tbFront.getSshPort(), remoteGroupStatusSource, localDst.toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("Backup group files:[{} to {}] error.", remoteGroupStatusSource, localDst.toAbsolutePath().toString(), e);
        }

    }

    /**
     * @param chainId
     * @param pageSize
     * @param pageNumber
     * @return
     */
    public BasePageResponse queryGroupByPage(Integer chainId, Integer agencyId, Integer pageSize, Integer pageNumber) {
        // check id
        chainService.verifyChainId(chainId);
        //reset all local group
        resetGroupList();
        //param
        TbGroupExample example = new TbGroupExample();
        example.setStart(Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).filter(p -> p >= 0).orElse(1));
        example.setCount(pageSize);
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);

        //query by agencyId
        if (Objects.nonNull(agencyId)) {
            List<Integer> groupIdList = listGroupIdByAgencyId(agencyId);
            if (CollectionUtils.isEmpty(groupIdList)) {
                criteria.andGroupIdEqualTo(-10);
            } else {
                criteria.andGroupIdIn(groupIdList);
            }
        }


        //query
        BasePageResponse basePageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        basePageResponse.setTotalCount(Integer.parseInt(String.valueOf(tbGroupMapper.countByExample(example))));
        if (basePageResponse.getTotalCount() > 0) {
            basePageResponse.setData(tbGroupMapper.selectByExample(example));
        }
        return basePageResponse;
    }


    /**
     * @param agencyId
     * @return
     */
    public List<Integer> listGroupIdByAgencyId(int agencyId) {
        log.info("start exec method[listGroupIdByAgencyId] agencyId:{}", agencyId);

        //list frontId
        List<TbFront> frontList = frontService.listFrontByAgency(agencyId);
        if (CollectionUtils.isEmpty(frontList)) {
            log.info("finish exec method[listGroupIdByAgencyId] not found front record by agencyId:{}", agencyId);
            return Collections.EMPTY_LIST;
        }
        List<Integer> frontIdList = frontList.stream().map(front -> front.getFrontId()).collect(Collectors.toList());

        //list group
        List<Integer> groupIdList = frontGroupMapService.listGroupByFronts(frontIdList);
        log.info("success exec method[listGroupIdByAgencyId] agencyId:{} result:{}", agencyId, JsonTools.objToString(groupIdList));
        return groupIdList;
    }

    /**
     * @param agencyId
     * @return
     */
    public List<TbGroup> listGroupByAgencyId(int agencyId) {
        log.info("start exec method[listGroupByAgencyId] agencyId:{}", agencyId);
        //list groupId
        List<Integer> groupIdList = listGroupIdByAgencyId(agencyId);
        if (CollectionUtils.isEmpty(groupIdList)) return Collections.EMPTY_LIST;
        //param
        TbGroupExample example = new TbGroupExample();
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andGroupIdIn(groupIdList);
        //query group list
        List<TbGroup> groupList = tbGroupMapper.selectByExample(example);
        log.info("success exec method[listGroupIdByAgencyId] agencyId:{} result:{}", agencyId, JsonTools.objToString(groupList));
        return groupList;
    }
}
