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

import com.webank.webase.chain.mgr.agency.entity.RspAgencyVo;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.DeployTypeEnum;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.GroupOperateTypeEnum;
import com.webank.webase.chain.mgr.base.enums.GroupStatusEnum;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainManager;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.deploy.config.NodeConfig;
import com.webank.webase.chain.mgr.deploy.service.DeployShellService;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.RspEntityOfGroupPage;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.chain.mgr.group.entity.ReqStartGroup;
import com.webank.webase.chain.mgr.group.entity.RspGroupDetailVo;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.node.entity.RspNodeInfoVo;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbGroupExample;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.task.TaskManager;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private ChainManager chainManager;
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
    private UserService userService;
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
    @Autowired
    private FrontManager frontManager;
    @Qualifier(value = "mgrAsyncExecutor")
    @Autowired
    private ThreadPoolTaskExecutor mgrAsyncExecutor;
    @Autowired
    private TaskManager taskManager;


    /**
     * generate exist group to single node.
     * 【purpose:generate group.x.genesis group.x.ini】
     *
     * @return
     */
    public void generateExistGroupToSingleNode(int chainId, int groupId, String nodeId) {
        log.info("start exec method[generateExistGroupToSingleNode] chainId:{},  groupId:{},  nodeId:{}", chainId, groupId, nodeId);
        TbGroup existsGroup = this.tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (Objects.isNull(existsGroup))
            throw new BaseException(ConstantCode.INVALID_GROUP_ID.attach(String.format("not found group by chainId:%d groupId:%d", chainId, groupId)));

        // if is group1:copy group.1.genesis group.1.ini files manually
        if (ConstantProperties.DEFAULT_GROUP_ID == groupId) {
            log.info("jump over exec method[generateExistGroupToSingleNode]. groupId is 1");
            return;
        }
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null)
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS.attach(String.format("not found front by chainId:%d nodeId:%s", chainId, nodeId)));

        //sealer nodes
        TbGroup tbGroup = tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (Objects.isNull(tbGroup))
            throw new BaseException(ConstantCode.NOT_FOUND_GROUP_BY_ID_AND_CHAIN.attach(String.format("not found group by chainId:%d nodeId:%s", chainId, nodeId)));

        if (StringUtils.isBlank(tbGroup.getNodeIdList()))
            throw new BaseException(ConstantCode.NOT_FOUND_GENESIS_NODE_LIST_OF_GROUP.attach(String.format("group:%s", groupId)));

        List<String> genesisNodeIList = JsonTools.toJavaObjectList(tbGroup.getNodeIdList(), String.class);
        log.info("group:{} genesisNodeIdList:{}", groupId, JsonTools.objToString(genesisNodeIList));

        // request front to generate
        GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
        generateGroupInfo.setGenerateGroupId(groupId);
        generateGroupInfo.setTimestamp(BigInteger.valueOf(Long.valueOf(existsGroup.getGroupTimestamp())));
        generateGroupInfo.setNodeList(genesisNodeIList);

        try {
            frontInterface.generateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                    generateGroupInfo);
        } catch (BaseException be) {
            if (ConstantProperties.GROUP_ALREADY_EXIST_RETURN_CODE != be.getRetCode().getCode())
                throw be;
        }

        log.info("success exec method[generateExistGroupToSingleNode] chainId:{},  groupId:{},  nodeId:{}", chainId, groupId, nodeId);

    }

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
        TbChain tbChain = chainManager.requireChainIdExist(chainId);

        // save group
        TbGroup tbGroup = groupManager.saveGroup(req.getGroupName(), req.getTimestamp(), generateGroupId, chainId, req.getNodeList(), req.getNodeList().size(),
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
//        resetGroupList();

        // check id
        Integer chainId = req.getChainId();
        TbChain tbChain = chainManager.requireChainIdExist(chainId);

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

        //get front list
        Set<TbFront> fronts = req.getNodeList().stream()
                .map((nodeId) -> frontService.getByChainIdAndNodeId(chainId, nodeId))
                .filter((front) -> Objects.nonNull(front))
                .collect(Collectors.toSet());
        log.debug("fronts:{} nodeIds:{}", JsonTools.objToString(fronts), JsonTools.objToString(req.getNodeList()));

        if (CollectionUtils.isEmpty(fronts) || req.getNodeList().size() != fronts.size()) {
            Set<String> notFoundFrontByTheseNodes = req.getNodeList().stream().collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(fronts)) {
                Set<String> foundFrontByTheseNodes = fronts.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
                notFoundFrontByTheseNodes = req.getNodeList().stream()
                        .filter(node -> !foundFrontByTheseNodes.contains(node))
                        .collect(Collectors.toSet());
            }
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS.attach(String.format("not found front by these nodes:%s", JsonTools.objToString(notFoundFrontByTheseNodes))));
        }


        // save group
        TbGroup tbGroup = groupManager.saveGroup(req.getGroupName(), timestamp, generateGroupId, chainId, req.getNodeList(), req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue());

        for (TbFront tbFront : fronts) {
            // request front to generate
            GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
            BeanUtils.copyProperties(req, generateGroupInfo);
            generateGroupInfo.setGenerateGroupId(generateGroupId);
            generateGroupInfo.setTimestamp(timestamp);
            frontInterface.generateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(), generateGroupInfo);

            frontGroupMapService.newFrontGroup(chainId, tbFront.getFrontId(), generateGroupId);
            if (tbChain.getDeployType() == DeployTypeEnum.API.getType()) {
                // fetch group config file
                this.pullAllGroupFiles(generateGroupId, tbFront);
            }
        }
        // clear cache
        frontGroupMapCache.clearMapList(chainId);


        ReqStartGroup reqStartGroup = new ReqStartGroup();
        reqStartGroup.setChainId(req.getChainId());
        reqStartGroup.setGenerateGroupId(generateGroupId);
        reqStartGroup.setNodeList(req.getNodeList());
        this.batchStartGroup(reqStartGroup);

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
        TbChain tbChain = chainManager.requireChainIdExist(chainId);
        // request front to operate
        Object groupHandleResult = frontInterface.operateGroup(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, type);

        // refresh   20210225 挪到controller异步调用
        // resetGroupList();


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
        // refresh  20210225 挪到controller异步调用
//        resetGroupList();
    }


    /**
     * query all group info.
     */
    public List<TbGroup> getGroupList(Integer chainId, Byte groupStatus) throws BaseException {
        log.debug("start getGroupList");
        try {
            List<TbGroup> groupList = new ArrayList<>();
            if (Objects.nonNull(chainId) && Objects.nonNull(groupStatus)) {
                groupList = this.tbGroupMapper.selectByChainIdAndGroupStatus(chainId, groupStatus);
            } else if (Objects.nonNull(chainId)) {
                groupList = this.tbGroupMapper.selectByChainId(chainId);
            } else if (Objects.nonNull(groupStatus)) {
                groupList = this.tbGroupMapper.selectByGroupStatus(groupStatus);
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
        log.info("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        this.tbGroupMapper.updateStatus(chainId, groupId, groupStatus);
        log.info("end updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
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
     *
     */
    public synchronized void resetGroupList() {
        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

        List<TbChain> chainList = tbChainMapper.selectAll();
        if (CollectionUtils.isEmpty(chainList)) {
            log.info("No chain found.");
            return;
        }


        final CountDownLatch startLatch = new CountDownLatch(CollectionUtils.size(chainList));
        for (TbChain tbChain : chainList) {
            if (!this.chainService.runTask(tbChain)) {
                log.warn("Chain status is not running:[{}]", tbChain.getChainStatus());
                continue;
            }

            mgrAsyncExecutor.execute(() -> {
                try {
                    resetGroupByChain(tbChain.getChainId());
                } catch (Exception ex) {
                    log.error("fail resetGroupByChain chainId:{}.", tbChain.getChainId());
                } finally {
                    startLatch.countDown();
                }
            });
        }

        try {
            log.info("Wait to reset all group");
            startLatch.await(constants.getResetGroupListCycle(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("reset group error", e);
        }

        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }


    /**
     * @param chainId
     */
    @Transactional
    public void resetGroupByChain(int chainId) {
        Instant startTime = Instant.now();
        log.info("start exec method [resetGroupByChain] chainId:{} startTime:{}", chainId, startTime.toEpochMilli());

        // get all front
        List<TbFront> frontList = tbFrontMapper.selectByChainIdAndStatus(chainId, FrontStatusEnum.RUNNING.getId());
        if (frontList == null || frontList.size() == 0) {
            log.info("chain {} not found any front.", chainId);
            return;
        }

        // all groupId from chain
        Set<Integer> allGroupSet = new HashSet<>();

        // get group from chain
        for (TbFront front : frontList) {
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

            if (CollectionUtils.isEmpty(groupIdList)) {
                log.info("jump over. not found group by frontPeerName:{}, frontIp:{}, frontPort:{}.", frontPeerName, frontIp, frontPort);
                continue;
            }
            for (String groupId : groupIdList) {
                Integer gId = Integer.valueOf(groupId);
                allGroupSet.add(gId);
                // peer in group
                List<String> groupPeerList =
                        frontInterface.getGroupPeersFromSpecificFront(frontPeerName, frontIp, frontPort, gId);
                // save group
                groupManager.saveGroup("", null, gId, chainId, null, groupPeerList.size(), "synchronous",
                        GroupType.SYNC.getValue());

                //save front-group
                List<PeerInfo> peerInfoList = nodeService.getSealerAndObserverListFromSpecificFront(gId, frontPeerName, frontIp, frontPort);
                List<String> sealerAndObserverList = peerInfoList.stream().map(PeerInfo::getNodeId).collect(Collectors.toList());
                if (sealerAndObserverList.contains(front.getNodeId())) {
                    frontGroupMapService.newFrontGroup(chainId, front.getFrontId(), gId);
                } else {
                    //remove old front-group-map(just save sealer's and observer's front map)
                    tbFrontGroupMapMapper.deleteByChainIdAndFrontIdAndGroupId(chainId, front.getFrontId(), gId);
                }

                // save new peers
                savePeerList(chainId, frontPeerName, frontIp, frontPort, gId, groupPeerList);
                // remove invalid peers
                removeInvalidPeer(chainId, gId, frontPeerName, frontIp, frontPort, groupPeerList);
                // refresh: add sealer and observer no matter validity
                frontService.refreshSealerAndObserverInNodeList(frontPeerName, frontIp, frontPort,
                        front.getChainId(), gId);
            }

            //remove front-group-map by invalid group
            List<Integer> groupOnMapList = frontGroupMapService.getGroupByChainAndFront(chainId, front.getFrontId());
            if (CollectionUtils.isEmpty(groupOnMapList)) continue;
            groupOnMapList.stream()
                    .filter(g -> !groupIdList.contains(String.valueOf(g)))
                    .forEach(g -> tbFrontGroupMapMapper.deleteByChainIdAndFrontIdAndGroupId(chainId, front.getFrontId(), g));

        }

        // check group status
        checkGroupStatusAndRemoveInvalidGroup(chainId, allGroupSet);
        // clear cache
        frontGroupMapCache.clearMapList(chainId);

        log.info("end exec method [resetGroupByChain] . chainId:{} useTime:{} ", chainId,
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
    private void removeInvalidPeer(int chainId, int groupId, String peerName, String frontIp, Integer frontPort, List<String> groupPeerList) {
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
                        && !checkSealerAndObserverListContains(groupId, peerName, frontIp, frontPort, node.getNodeId()))
                .forEach(n -> nodeService.deleteByNodeAndGroupId(n.getNodeId(), groupId));
    }

    /**
     * @param groupId
     * @param peerName
     * @param frontIp
     * @param frontPort
     * @param nodeId
     * @return
     */
    private boolean checkSealerAndObserverListContains(int groupId, String peerName, String frontIp, Integer frontPort, String nodeId) {
        log.debug("checkSealerAndObserverListNotContains  groupId:{} peerName:{} frontIp:{} frontPort:{} nodeId:{}", groupId, peerName, frontIp, frontPort, nodeId);
        // get sealer and observer on chain
        List<PeerInfo> sealerAndObserverList =
                nodeService.getSealerAndObserverListFromSpecificFront(groupId, peerName, frontIp, frontPort);
        for (PeerInfo peerInfo : sealerAndObserverList) {
            if (nodeId.equals(peerInfo.getNodeId())) {
                return true;
            }
        }
        log.debug("finish checkSealerAndObserverListNotContains  groupId:{} peerName:{} frontIp:{} frontPort:{} nodeId:{}, result: false", groupId, peerName, frontIp, frontPort, nodeId);
        return false;
    }

    /**
     * check group status.
     */
    private void checkGroupStatusAndRemoveInvalidGroup(Integer chainId,
                                                       Set<Integer> allGroupOnChain) {
        log.info("start exec method [checkGroupStatusAndRemoveInvalidGroup] chain:{} allGroupOnChain:{}", chainId, JsonTools.objToString(allGroupOnChain));
        if (CollectionUtils.isEmpty(allGroupOnChain))
            return;

        List<TbGroup> allLocalGroup = getGroupList(chainId, null);
        if (CollectionUtils.isEmpty(allLocalGroup))
            return;

        for (TbGroup localGroup : allLocalGroup) {
            int localGroupId = localGroup.getGroupId();
            long count = allGroupOnChain.stream().filter(id -> id.intValue() == localGroupId).count();
            try {
                if (count > 0) {
                    log.info("group is valid, chainId:{} localGroupId:{}", chainId, localGroupId);
                    if (!Objects.equals(DataStatus.NORMAL.getValue(), localGroup.getGroupStatus()))
                        // update NORMAL
                        updateGroupStatus(chainId, localGroupId, DataStatus.NORMAL.getValue());
                    continue;
                }

                Date modifyTime = localGroup.getModifyTime();
                log.warn("group is invalid, chainId:{} localGroupId:{}", chainId, localGroupId);
                if (!CommonUtils.isDateTimeInValid(CommonUtils.timestamp2LocalDateTime(modifyTime.getTime()),
                        constants.getGroupInvalidGrayscaleValue())) {
                    log.warn("remove group, chainId:{} localGroup:{}", chainId,
                            JsonTools.toJSONString(localGroup));
                    // remove group
                    removeByGroupId(chainId, localGroupId);
                    continue;
                }

                if (Objects.equals(DataStatus.NORMAL.getValue(), localGroup.getGroupStatus()))
                    // update invalid
                    updateGroupStatus(chainId, localGroupId, DataStatus.INVALID.getValue());
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
        // remove task
        taskManager.removeByChainAndGroup(chainId, groupId);
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
    public BasePageResponse queryGroupByPage(Integer chainId, Integer agencyId, Integer pageSize, Integer pageNumber, Byte status, String sortType) {
        // check id
        chainManager.requireChainIdExist(chainId);
        //reset all local group
//        resetGroupList();
        //param
        TbGroupExample example = new TbGroupExample();
        example.setStart(Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).filter(p -> p >= 0).orElse(1));
        example.setCount(pageSize);
        example.setOrderByClause(String.format(ConstantProperties.ORDER_BY_CREATE_TIME_FORMAT, sortType));
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        if (Objects.nonNull(status)) {
            criteria.andGroupStatusEqualTo(status);
        }

        //query by agencyId
        if (Objects.nonNull(agencyId)) {
            List<Integer> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
            if (CollectionUtils.isEmpty(groupIdList)) {
                criteria.andGroupIdEqualTo(-10);
            } else {
                criteria.andGroupIdIn(groupIdList);
            }
        }

        //query
        BasePageResponse rsp = new BasePageResponse(ConstantCode.SUCCESS);
        rsp.setTotalCount(Integer.parseInt(String.valueOf(tbGroupMapper.countByExample(example))));

        if (rsp.getTotalCount() > 0) {
            List<TbGroup> groupList = tbGroupMapper.selectByExample(example);
            List<RspEntityOfGroupPage> rspGroupList = new ArrayList<>();
            for (TbGroup tbGroup : groupList) {
                RspEntityOfGroupPage rspGroup = new RspEntityOfGroupPage();
                BeanUtils.copyProperties(tbGroup, rspGroup);

                //set node count by agency
                if (Objects.nonNull(agencyId)) {
                    List<String> listNodeIdOfAgency = nodeService.listSealerAndObserverByGroupAndAgency(chainId, tbGroup.getGroupId(), agencyId);
                    if (CollectionUtils.isNotEmpty(listNodeIdOfAgency))
                        rspGroup.setNodeCountOfAgency(listNodeIdOfAgency.stream().distinct().count());
                }
                rspGroupList.add(rspGroup);
            }

            rsp.setData(rspGroupList);
        }

        return rsp;
    }


    /**
     * @param chainId
     * @param agencyId
     * @return
     */
    public List<Integer> listGroupIdByChainAndAgencyId(int chainId, int agencyId) {
        log.info("start exec method[listGroupIdByChainAndAgencyId] chainId:{} agencyId:{}", chainId, agencyId);

        //list frontId
        List<Integer> frontIdList = frontManager.listFrontIdByChainAndAgency(chainId, agencyId);
        if (CollectionUtils.isEmpty(frontIdList)) {
            log.info("finish exec method[listGroupIdByChainAndAgencyId] not found front record by agencyId:{}", agencyId);
            return Collections.EMPTY_LIST;
        }

        //list group
        List<Integer> groupIdList = frontGroupMapService.listGroupIdByChainAndFronts(chainId, frontIdList);
        log.info("success exec method[listGroupIdByChainAndAgencyId] chainId:{} agencyId:{} result:{}", chainId, agencyId, JsonTools.objToString(groupIdList));
        return groupIdList;
    }


    /**
     * @param chainId
     * @param agencyId
     * @return
     */
    public List<TbGroup> listGroupByChainAndAgencyId(int chainId, int agencyId) {
        log.info("start exec method[listGroupByChainAndAgencyId] chainId:{} agencyId:{}", chainId, agencyId);
        List<Integer> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
        log.info("groupIdList:{}", JsonTools.objToString(groupIdList));
        if (CollectionUtils.isEmpty(groupIdList))
            return Collections.EMPTY_LIST;

        TbGroupExample example = new TbGroupExample();
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdIn(groupIdList);

        List<TbGroup> groupList = tbGroupMapper.selectByExample(example);
        log.info("success exec method[listGroupByChainAndAgencyId] chainId:{} agencyId:{} result:{}", chainId, agencyId, JsonTools.objToString(groupList));
        return groupList;
    }


    /**
     * @param chainId
     * @param nodeId
     * @param groupId
     */
    public void startGroupIfNotRunning(Integer chainId, String nodeId, Integer groupId) {
        log.info("start exec method[startGroupIfNotRunning] chainId:{} nodeId:{} groupId:{}", chainId, nodeId, groupId);
        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail startGroupIfNotRunning node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        //get group status
        Map<Integer, String> restGroupStatus = frontInterface.
                queryGroupStatus(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(), Arrays.asList(groupId));
        log.info("restGroupStatus:{}", JsonTools.objToString(restGroupStatus));

        //start group
        if (!GroupStatusEnum.RUNNING.getValue().equalsIgnoreCase(restGroupStatus.get(groupId)))
            operateGroup(chainId, nodeId, groupId, GroupOperateTypeEnum.START.getValue());

        log.info("finish exec method[startGroupIfNotRunning] chainId:{} nodeId:{} groupId:{}", chainId, nodeId, groupId);
    }


    /**
     * @param chainId
     * @param nodeId
     * @param groupId
     */
    public void stopGroupIfRunning(Integer chainId, String nodeId, Integer groupId) {
        log.info("start exec method[stopGroupIfRunning] chainId:{} nodeId:{} groupId:{}", chainId, nodeId, groupId);
        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail stopGroupIfRunning node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        //get group status
        Map<Integer, String> restGroupStatus = frontInterface.
                queryGroupStatus(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(), Arrays.asList(groupId));
        log.info("restGroupStatus:{}", JsonTools.objToString(restGroupStatus));

        //start group
        if (GroupStatusEnum.RUNNING.getValue().equalsIgnoreCase(restGroupStatus.get(groupId)))
            operateGroup(chainId, nodeId, groupId, GroupOperateTypeEnum.STOP.getValue());

        log.info("finish exec method[stopGroupIfRunning] chainId:{} nodeId:{} groupId:{}", chainId, nodeId, groupId);
    }


    /**
     * @param chain
     * @param group
     * @return
     */
    public RspGroupDetailVo queryGroupDetail(int chain, int group) {
        log.info("start exec method[queryGroupDetail] chainId:{}  groupId:{}", chain, group);

        // query by private key
        TbGroup tbGroup = tbGroupMapper.selectByPrimaryKey(group, chain);
        RspGroupDetailVo rspGroupDetailVo = new RspGroupDetailVo();
        if (Objects.isNull(tbGroup))
            return rspGroupDetailVo;
        BeanUtils.copyProperties(tbGroup, rspGroupDetailVo);

        //query tbNodes
        NodeParam nodeParam = new NodeParam();
        nodeParam.setChainId(chain);
        nodeParam.setGroupId(group);
        List<TbNode> tbNodes = nodeService.qureyNodeList(nodeParam);
        log.info("tbNodes:{}", JsonTools.objToString(tbNodes));
        if (CollectionUtils.isEmpty(tbNodes))
            return rspGroupDetailVo;

        //get node list and agency list
        List<String> nodeIdList = tbNodes.stream().map(node -> node.getNodeId()).collect(Collectors.toList());
        List<TbFront> frontList = frontManager.listByChainAndNodeIds(chain, nodeIdList);
        List<RspNodeInfoVo> nodeInfoList = new ArrayList<>();
        List<RspAgencyVo> agencyVoList = new ArrayList<>();
        for (TbNode tbNode : tbNodes) {
            RspNodeInfoVo rspNodeInfoVo = new RspNodeInfoVo();
            BeanUtils.copyProperties(tbNode, rspNodeInfoVo);
            if (CollectionUtils.isNotEmpty(frontList)) {
                frontList.stream()
                        .filter(front -> tbNode.getNodeId().equals(front.getNodeId()))
                        .findFirst()
                        .ifPresent(f -> {
                            //add agency
                            RspAgencyVo rspAgencyVo = new RspAgencyVo();
                            rspAgencyVo.setAgencyId(f.getExtAgencyId());
                            rspAgencyVo.setAgencyName(f.getAgency());
                            agencyVoList.add(rspAgencyVo);

                            //update rspNodeInfoVo
                            rspNodeInfoVo.setAgency(f.getExtAgencyId());
                            rspNodeInfoVo.setAgencyName(f.getAgency());
                            rspNodeInfoVo.setFrontPeerName(f.getFrontPeerName());
                        });
                nodeInfoList.add(rspNodeInfoVo);
            }
        }

        rspGroupDetailVo.setAgencyList(agencyVoList.stream().distinct().collect(Collectors.toList()));
        rspGroupDetailVo.setNodeInfoList(nodeInfoList.stream().distinct().collect(Collectors.toList()));
        log.info("finish exec method[queryGroupDetail] result:{}", JsonTools.objToString(rspGroupDetailVo));
        return rspGroupDetailVo;
    }


    /**
     * @param agencyId
     * @param chainId
     * @param groupId
     */
    public void requireFoundGroupByChainAndGroup(int agencyId, int chainId, int groupId) {
        List<Integer> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
        if (CollectionUtils.isEmpty(groupIdList) || !groupIdList.contains(groupId))
            throw new BaseException(ConstantCode.NOT_FOUND_GROUP_BY_AGENCY_AND_CHAIN.attach("agency:" + agencyId + " chain:" + chainId + " group:" + groupId));
    }

    /**
     * generate group.x.ini group.x.genesis
     * @param chain
     * @param groupId
     * @param newFrontList
     * @throws IOException
     */
    public void generateNewNodesGroupConfigsAndScp(TbChain chain, int groupId, List<TbFront> newFrontList) {
        log.info("start generateNewNodesGroupConfigsAndScp chain:{},groupId:{},newFrontList:{}",
            chain, groupId, newFrontList);
        int chainId = chain.getChainId();
        String chainName = chain.getChainName();

        // 1.4.3 not support add group when add node
        // long now = System.currentTimeMillis();
        // List<String> nodeIdList = newFrontList.stream().map(TbFront::getNodeId)
        //        .collect(Collectors.toList());

        // copy group.x.[genesis|conf] from old front
        TbNode oldNode = this.nodeService.getOldestNodeByChainIdAndGroupId(chainId, groupId);
        TbFront oldFront = null;
        if (oldNode != null) {
            oldFront = this.tbFrontMapper.getByChainIdAndNodeId(chainId, oldNode.getNodeId());
        }

        for (TbFront newFront : newFrontList) {
            String ip = newFront.getFrontIp();
            // local node root
            Path nodeRoot = this.pathService.getNodeRoot(chainName, ip, newFront.getHostIndex());

            // 1.4.3 not support add group when add node
            //if (newGroup) {
            //    // generate conf/group.[groupId].ini
            //    ThymeleafUtil.newGroupConfigs(nodeRoot, groupId, now, nodeIdList);
            // copy old group files
            if (oldFront != null) {
                Path oldNodePath = this.pathService.getNodeRoot(chainName, oldFront.getFrontIp(), oldFront.getHostIndex());
                NodeConfig.copyGroupConfigFiles(oldNodePath, nodeRoot, groupId);
            }


            // scp node to remote host
            // NODES_ROOT/[chainName]/[ip]/node[index] as a {@link Path}, a directory.
            String src = String.format("%s", nodeRoot.toAbsolutePath().toString());
            String dst = PathService.getChainRootOnHost(newFront.getRootOnHost(), chainName);
            String sshUser = newFront.getSshUser();
            int sshPort = newFront.getSshPort();
            log.info("generateNewNodesGroupConfigsAndScp Send files from:[{}] to:[{}:{}].", src, ip, dst);
            try {
                deployShellService.scp(ScpTypeEnum.UP, sshUser, ip, sshPort, src, dst);
                log.info("generateNewNodesGroupConfigsAndScp scp success.");
            } catch (Exception e) {
                log.error("generateNewNodesGroupConfigsAndScp Send files from:[{}] to:[{}:{}] error.", src, ip, dst, e);
            }
        }
    }

}
