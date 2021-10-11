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
package com.webank.webase.chain.mgr.front;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.chain.mgr.front.entity.FrontInfo;
import com.webank.webase.chain.mgr.front.entity.FrontParam;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbFrontExample;
import com.webank.webase.chain.mgr.repository.bean.TbFrontGroupMap;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontGroupMapMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import com.webank.webase.chain.mgr.util.NumberUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * service of web3.
 */
@Log4j2
@Service
public class FrontService {

    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private TbFrontMapper tbFrontMapper;
    @Autowired
    private TbFrontGroupMapMapper tbFrontGroupMapMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    @Lazy
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private FrontService frontService;
    @Autowired
    private DockerOptions dockerOptions;
    @Autowired
    private PathService pathService;
    @Autowired
    private ConstantProperties constantProperties;
    @Qualifier(value = "deployAsyncScheduler")
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private FrontManager frontManager;
    @Autowired
    private GroupManager groupManager;

    /**
     * add new front
     * TODO.
     */
    @Transactional
    public TbFront newFront(FrontInfo frontInfo) {
        log.debug("start newFront frontInfo:{}", frontInfo);
        // check chainId
        Integer chainId = frontInfo.getChainId();
        TbChain tbChain = tbChainMapper.selectByPrimaryKey(chainId);
        if (tbChain == null) {
            throw new BaseException(ConstantCode.INVALID_CHAIN_ID);
        }

        // query group list
        List<String> groupIdList = null;
        String frontPeerName = frontInfo.getFrontPeerName();
        String frontIp = frontInfo.getFrontIp();
        Integer frontPort = frontInfo.getFrontPort();
        try {
            groupIdList = frontInterface.getGroupListFromSpecificFront(frontPeerName, frontIp, frontPort);
        } catch (Exception e) {
            log.error("fail newFront, frontIp:{},frontPort:{}", frontIp, frontPort);
            throw new BaseException(ConstantCode.REQUEST_FRONT_FAIL);
        }
        // check front not exist
        String newNodeId = null;
        if (CollectionUtils.isNotEmpty(groupIdList)) {
            SyncStatus syncStatus = frontInterface.getSyncStatusFromSpecificFront(frontPeerName, frontIp, frontPort,
                    Integer.valueOf(groupIdList.get(0)));
            newNodeId = syncStatus.getNodeId();
        }
        if (StringUtils.isBlank(newNodeId)) //TODO 临时方案，最终等front有nodeInfo接口后改成调nodeInfo接口
            newNodeId = frontInfo.getNodeId();

        if (StringUtils.isNotBlank(frontInfo.getNodeId()) && !newNodeId.equals(frontInfo.getNodeId()))
            throw new BaseException(ConstantCode.PARAM_EXCEPTION.attach(String.format("input nodeId:%s but front connect the node is:%s", frontInfo.getNodeId(), newNodeId)));

        if (StringUtils.isBlank(newNodeId))
            throw new BaseException(ConstantCode.NODE_ID_EMPTY);


        frontManager.requireNotFoundFront(chainId, newNodeId, frontPeerName);
        requireNotFoundFront(frontIp, frontPort, frontPeerName);

        TbFront tbFront = new TbFront();
        tbFront.setChainName(tbChain.getChainName());
        tbFront.setFrontStatus(FrontStatusEnum.RUNNING.getId());
        // copy attribute
        BeanUtils.copyProperties(frontInfo, tbFront);
        tbFront.setNodeId(newNodeId);
        Date now = new Date();
        tbFront.setCreateTime(now);
        tbFront.setModifyTime(now);
        // save front info
        this.tbFrontMapper.insertSelective(tbFront);
        if (tbFront.getFrontId() == null || tbFront.getFrontId() == 0) {
            log.warn("fail newFront, after save, tbFront:{}", JsonTools.toJSONString(tbFront));
            throw new BaseException(ConstantCode.SAVE_FRONT_FAIL);
        }

        if (CollectionUtils.isNotEmpty(groupIdList)) {
            for (String groupId : groupIdList) {
                Integer group = Integer.valueOf(groupId);
                // peer in group
                List<String> groupPeerList =
                        frontInterface.getGroupPeersFromSpecificFront(frontPeerName, frontIp, frontPort, group);
                // get peers on chain
                PeerInfo[] peerArr =
                        frontInterface.getPeersFromSpecificFront(frontPeerName, frontIp, frontPort, group);
                List<PeerInfo> peerList = Arrays.asList(peerArr);
                // add group
                groupManager.saveGroup("", null, group, chainId, null, groupPeerList.size(), "synchronous",
                        GroupType.SYNC.getValue());
                // save front group map
                frontGroupMapService.newFrontGroup(chainId, tbFront.getFrontId(), group);
                // save nodes
                for (String nodeId : groupPeerList) {
                    PeerInfo newPeer =
                            peerList.stream().map(p -> CommonUtils.object2JavaBean(p, PeerInfo.class))
                                    .filter(peer -> nodeId.equals(peer.getNodeId())).findFirst()
                                    .orElseGet(() -> new PeerInfo(nodeId));
                    nodeService.addNodeInfo(chainId, group, newPeer);
                }
                // add sealer(consensus node) and observer in nodeList
                refreshSealerAndObserverInNodeList(frontPeerName, frontIp, frontPort, chainId, group);
            }
        }

        // clear cache
        frontGroupMapCache.clearMapList(chainId);
        return tbFront;
    }

    /**
     * add sealer(consensus node) and observer in nodeList
     *
     * @param groupId
     */
    public void refreshSealerAndObserverInNodeList(String frontPeerName, String frontIp, int frontPort, int chainId,
                                                   int groupId) {
        log.debug("start refreshSealerAndObserverInNodeList frontIp:{}, frontPort:{}, groupId:{}",
                frontIp, frontPort, groupId);
        List<String> sealerList =
                frontInterface.getSealerListFromSpecificFront(frontPeerName, frontIp, frontPort, groupId);
        List<String> observerList =
                frontInterface.getObserverListFromSpecificFront(frontPeerName, frontIp, frontPort, groupId);
        List<PeerInfo> sealerAndObserverList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> sealerAndObserverList.add(new PeerInfo(nodeId)));
        log.debug("refreshSealerAndObserverInNodeList sealerList:{},observerList:{}", sealerList,
                observerList);
        sealerAndObserverList.stream().forEach(peerInfo -> {
//            NodeParam checkParam = new NodeParam();
//            checkParam.setChainId(chainId);
//            checkParam.setGroupId(groupId);
//            checkParam.setNodeId(peerInfo.getNodeId());
//            int existedNodeCount = nodeService.countOfNode(checkParam);
//            log.debug("addSealerAndObserver peerInfo:{},existedNodeCount:{}", peerInfo,
//                    existedNodeCount);
//            if (existedNodeCount == 0) {
//                nodeService.addNodeInfo(chainId, groupId, peerInfo);
//            }
            nodeService.addNodeInfo(chainId, groupId, peerInfo);
        });
        log.debug("end addSealerAndObserver");
    }

    /**
     * get monitor info.
     */
    public Object getNodeMonitorInfo(Integer frontId, LocalDateTime beginDate,
                                     LocalDateTime endDate, LocalDateTime contrastBeginDate, LocalDateTime contrastEndDate,
                                     int gap, int groupId) {
        log.debug(
                "start getNodeMonitorInfo.  frontId:{} beginDate:{} endDate:{}"
                        + " contrastBeginDate:{} contrastEndDate:{} gap:{} groupId:{}",
                frontId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap, groupId);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("gap", String.valueOf(gap));
        if (beginDate != null) {
            map.put("beginDate", String.valueOf(beginDate));
        }
        if (endDate != null) {
            map.put("endDate", String.valueOf(endDate));
        }
        if (contrastBeginDate != null) {
            map.put("contrastBeginDate", String.valueOf(contrastBeginDate));
        }
        if (contrastEndDate != null) {
            map.put("contrastEndDate", String.valueOf(contrastEndDate));
        }

        Object rspObj = frontInterface.getNodeMonitorInfo(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, map);
        log.debug("end getNodeMonitorInfo. rspObj:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }

    /**
     * get ratio of performance.
     */
    public Object getPerformanceRatio(Integer frontId, LocalDateTime beginDate,
                                      LocalDateTime endDate, LocalDateTime contrastBeginDate, LocalDateTime contrastEndDate,
                                      int gap) {
        log.debug(
                "start getPerformanceRatio.  frontId:{} beginDate:{} endDate:{}"
                        + " contrastBeginDate:{} contrastEndDate:{} gap:{}",
                frontId, beginDate, endDate, contrastBeginDate, contrastEndDate, gap);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        Map<String, String> map = new HashMap<>();
        map.put("gap", String.valueOf(gap));
        if (beginDate != null) {
            map.put("beginDate", String.valueOf(beginDate));
        }
        if (endDate != null) {
            map.put("endDate", String.valueOf(endDate));
        }
        if (contrastBeginDate != null) {
            map.put("contrastBeginDate", String.valueOf(contrastBeginDate));
        }
        if (contrastEndDate != null) {
            map.put("contrastEndDate", String.valueOf(contrastEndDate));
        }

        Object rspObj = frontInterface.getPerformanceRatio(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), map);
        log.debug("end getPerformanceRatio. rspObj:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }

    /**
     * get config of performance.
     */
    public Object getPerformanceConfig(int frontId) {
        log.debug("start getPerformanceConfig.  frontId:{} ", frontId);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        Object rspObj =
                frontInterface.getPerformanceConfig(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort());
        log.debug("end getPerformanceConfig. frontRsp:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }

    /**
     * check node process.
     */
    public Object checkNodeProcess(int frontId) {
        log.debug("start checkNodeProcess. frontId:{} ", frontId);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        Object rspObj =
                frontInterface.checkNodeProcess(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort());
        log.debug("end checkNodeProcess. response:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }

    /**
     * check node process.
     */
    public Object getGroupSizeInfos(int frontId) {
        log.debug("start getGroupSizeInfos. frontId:{} ", frontId);

        // query by front Id
        TbFront tbFront = frontService.getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        Object rspObj =
                frontInterface.getGroupSizeInfos(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort());
        log.debug("end getGroupSizeInfos. response:{}", JsonTools.toJSONString(rspObj));
        return rspObj;
    }


    /**
     * query front by frontId.
     */
    public TbFront getById(int frontId) {
        if (frontId == 0) {
            return null;
        }
        return this.tbFrontMapper.selectByPrimaryKey(frontId);
    }

    /**
     * query front by nodeId.
     */
    public TbFront getByChainIdAndNodeId(Integer chainId, String nodeId) {
        if (chainId == null || StringUtils.isBlank(nodeId)) {
            return null;
        }
        return this.tbFrontMapper.getByChainIdAndNodeId(chainId, nodeId);
    }

    /**
     * remove front by frontId
     */
    public void removeByFrontId(int frontId) {
        // check frontId
        TbFront tbFront = getById(frontId);
        if (tbFront == null) {
            throw new BaseException(ConstantCode.INVALID_FRONT_ID);
        }

        // remove front
        this.tbFrontMapper.deleteByPrimaryKey(frontId);
        // remove map
        frontGroupMapService.removeByFrontId(frontId);
        // reset group list
        resetGroupListTask.asyncResetGroupList();
        // clear cache
        frontGroupMapCache.clearMapList(tbFront.getChainId());
//
//        // remote docker container
//        this.dockerOptions.stop(tbFront.getFrontIp(),
//                tbFront.getDockerPort(), tbFront.getSshUser(),
//                tbFront.getSshPort(), tbFront.getContainerName());
//
//        // move node directory to tmp
//        try {
//            this.pathService.deleteNode(tbFront.getChainName(), tbFront.getFrontIp(),
//                    tbFront.getHostIndex(), tbFront.getNodeId());
//        } catch (IOException e) {
//            log.error("Delete node:[{}:{}:{}] config files error.",
//                    tbFront.getChainName(), tbFront.getFrontIp(), tbFront.getHostIndex(), e);
//            throw new BaseException(ConstantCode.DELETE_NODE_DIR_ERROR);
//        }
//
//        // move node of remote host files to temp directory, e.g./opt/fisco/delete-tmp
//        NodeService.mvNodeOnRemoteHost(tbFront.getFrontIp(), tbFront.getRootOnHost(), tbFront.getChainName(), tbFront.getHostIndex(),
//                tbFront.getNodeId(), tbFront.getSshUser(), tbFront.getSshPort(), constantProperties.getPrivateKey());

    }

    /**
     * remove front by chainId
     */
    public void removeByChainId(int chainId) {
        if (chainId == 0) {
            return;
        }
        log.info("Delete front data by chain id:[{}].", chainId);

        // select host, front, group in agency
        List<TbFront> frontList = this.tbFrontMapper.selectByChainId(chainId);
        if (CollectionUtils.isEmpty(frontList)) {
            log.warn("No front in chain:[{}]", chainId);
            return;
        }
        Set<Integer> deleteHostId = new HashSet<>();
        for (TbFront front : frontList) {

            // delete on remote host
            if (deleteHostId.contains(front.getExtHostId())) {
                continue;
            }

            if (Objects.nonNull(front.getDockerPort()) && StringUtils.isNotBlank(front.getContainerName())) {
                // remote docker container
                this.dockerOptions.stop(front.getFrontIp(),
                        front.getDockerPort(), front.getSshUser(),
                        front.getSshPort(), front.getContainerName());


                // move chain config files
                ChainService.mvChainOnRemote(front.getFrontIp(), front.getRootOnHost(), front.getChainName(),
                        front.getSshUser(), front.getSshPort(), constantProperties.getPrivateKey());
                deleteHostId.add(front.getExtHostId());
            }

        }

        // remove front
        this.tbFrontMapper.deleteByChainId(chainId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbFront insert(TbFront tbFront) throws BaseException {
        if (this.tbFrontMapper.insertSelective(tbFront) != 1 || tbFront.getFrontId() <= 0) {
            throw new BaseException(ConstantCode.INSERT_FRONT_ERROR);
        }
        return tbFront;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int frontId, FrontStatusEnum newStatus) {
        log.info("Update front:[{}] status to:[{}]", frontId, newStatus.toString());
        return this.tbFrontMapper.updateStatus(frontId, newStatus.getId()) == 1;
    }

    /**
     * @param chainId
     * @param nodeId
     * @param optionType
     * @param before
     * @param success
     * @param failed
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public boolean restart(int chainId, String nodeId, OptionType optionType, FrontStatusEnum before,
                           FrontStatusEnum success, FrontStatusEnum failed) {
        TbChain chain = this.tbChainMapper.selectByPrimaryKey(chainId);
        if (chain == null) {
            log.error("Chain:[{}] not exists.", chainId);
            return false;
        }

        log.info("Restart node:[{}:{}]", chainId, nodeId);
        // get front
        TbFront front = this.tbFrontMapper.getByChainIdAndNodeId(chainId, nodeId);
        if (front == null) {
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        // set front status to stopped to avoid error for time task.
        ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), before);

        log.info("Docker start container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        try {
            this.dockerOptions.run(
                    front.getFrontIp(), front.getDockerPort(), front.getSshUser(), front.getSshPort(),
                    front.getVersion(), front.getContainerName(),
                    PathService.getChainRootOnHost(front.getRootOnHost(), front.getChainName()), front.getHostIndex());

            threadPoolTaskScheduler.schedule(() -> {
                // update front status
                this.updateStatus(front.getFrontId(), success);
            }, Instant.now().plusMillis(constantProperties.getDockerRestartPeriodTime()));
            return true;
        } catch (Exception e) {
            log.error("Start front:[{}:{}] failed.", front.getFrontIp(), front.getHostIndex(), e);
            ((FrontService) AopContext.currentProxy()).updateStatus(front.getFrontId(), failed);
            throw new BaseException(ConstantCode.START_NODE_ERROR);
        }
    }


    /**
     * @param groupId
     * @return
     */
    public List<TbFront> selectFrontListByGroupId(int chainId, int groupId) {
        // select all agencies by chainId
        List<TbFrontGroupMap> frontGroupMapList = this.tbFrontGroupMapMapper.selectListByGroupId(chainId, groupId);
        if (CollectionUtils.isEmpty(frontGroupMapList)) {
            return Collections.emptyList();
        }

        // select all fronts by all agencies
        List<TbFront> tbFrontList = frontGroupMapList.stream()
                .map((map) -> tbFrontMapper.getById(map.getFrontId()))
                .filter((front) -> front != null)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbFrontList)) {
            log.error("Group:[{}] has no front.", groupId);
            return Collections.emptyList();
        }
        return tbFrontList;
    }


    /**
     * @param chainId
     * @param groupIdSet
     * @return
     */
    public List<TbFront> selectFrontListByGroupIdSet(int chainId, Set<Integer> groupIdSet) {
        // select all fronts of all group id
        List<TbFront> allTbFrontList = groupIdSet.stream()
                .map((groupId) -> this.selectFrontListByGroupId(chainId, groupId))
                .filter((front) -> front != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(allTbFrontList)) {
            log.error("Group id set:[{}] has no front.", JsonTools.toJSONString(groupIdSet));
            return Collections.emptyList();
        }

        // delete replication
        return allTbFrontList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @param chainId
     */
    public int frontProgress(int chainId) {
        // check host init
        int frontFinishCount = 0;
        List<TbFront> frontList = this.tbFrontMapper.selectByChainId(chainId);
        if (CollectionUtils.isEmpty(frontList)) {
            return NumberUtil.PERCENTAGE_FINISH;
        }
        for (TbFront front : frontList) {
            if (FrontStatusEnum.isRunning(front.getFrontStatus())) {
                frontFinishCount++;
            }
        }
        // check front init finish ?
        if (frontFinishCount == frontList.size()) {
            // init success
            return NumberUtil.PERCENTAGE_FINISH;
        }
        return NumberUtil.percentage(frontFinishCount, frontList.size());
    }

    /**
     * @param frontIp
     * @param frontPort
     * @param frontPeerName
     */
    public void requireNotFoundFront(String frontIp, int frontPort, String frontPeerName) {
        log.info("start exec method[requireNotFoundFront] frontIp:{} frontPort:{} frontPeerName:{}", frontIp, frontPort, frontPeerName);

        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andFrontIpEqualTo(frontIp);
        criteria.andFrontPortEqualTo(frontPort);
        if(StringUtils.isNotBlank(frontPeerName)){
            criteria.andFrontPeerNameEqualTo(frontPeerName);
        }

        long count = this.tbFrontMapper.countByExample(example);
        if (count > 0)
            throw new BaseException(ConstantCode.FRONT_EXISTS.attach(String.format("found front record by frontIp:%s frontPort:%s frontPeerName:%s", frontIp, frontPort, frontPeerName)));

        log.info("finish exec method[requireNotFoundFront] frontIp:{} frontPort:{} frontPeerName:{}", frontIp, frontPort, frontPeerName);

    }


    /**
     * @param chainId
     * @param groupId
     * @param frontId
     * @param agencyId
     * @return
     */
    public List<TbFront> listFront(int chainId, int groupId, Integer frontId, Integer agencyId) {
        log.info("start exec method[listFront] chainId:{} groupId:{} frontId:{} agencyId:{}", chainId, groupId, frontId, agencyId);

        //query nodeIdList from group
        List<String> nodeIdList = nodeService.getSealerAndObserverList(chainId, groupId);
        if (CollectionUtils.isEmpty(nodeIdList))
            return new ArrayList<>();

        //db param
        FrontParam param = new FrontParam();
        param.setNodeIdList(nodeIdList);
        param.setChainId(chainId);
        param.setFrontId(frontId);
        param.setExtAgencyId(agencyId);

        List<TbFront> frontList = frontManager.listByParam(param);
        log.info("success exec method[listFront] result:{}", JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param chainId
     * @param nodeIds
     * @return
     */
    public List<TbFront> selectFrontByNodeIdListAndChain(int chainId, List<String> nodeIds) {
        log.info("start exec method [selectFrontByNodeIdListAndChain]. chainId:{} nodeIds:{}", chainId, JsonTools.objToString(nodeIds));
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andNodeIdIn(nodeIds);
        criteria.andChainIdEqualTo(chainId);
        criteria.andFrontStatusNotEqualTo(FrontStatusEnum.ABANDONED.getId());
        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.info("success exec method [selectFrontByNodeIdListAndChain]. result:{}", JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param agencyId
     */
    public void abandonedFrontByAgencyId(int agencyId) {
        log.info("start exec method [abandonedFrontByAgencyId]. agencyId:{}", agencyId);

        List<TbFront> frontList = frontManager.listFrontByAgency(agencyId);
        log.info("agency:{} frontList:{}", agencyId, JsonTools.objToString(frontList));
        for (TbFront front : CollectionUtils.emptyIfNull(frontList)) {
            if (updateStatus(front.getFrontId(), FrontStatusEnum.ABANDONED))
                frontGroupMapService.removeByFrontId(front.getFrontId());
        }

        log.info("finish exec method [abandonedFrontByAgencyId]. agencyId:{}", agencyId);
    }


}
