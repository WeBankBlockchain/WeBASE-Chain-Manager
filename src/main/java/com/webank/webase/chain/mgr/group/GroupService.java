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
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.chain.ChainManager;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.RspEntityOfGroupPage;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
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
import com.webank.webase.chain.mgr.util.JsonTools;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.protocol.response.Peers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private GroupManager groupManager;
    @Autowired
    private FrontManager frontManager;
    @Qualifier(value = "asyncExecutor")
    @Autowired
    private ThreadPoolTaskExecutor mgrAsyncExecutor;
    @Autowired
    private TaskManager taskManager;

    /**
     * query all group info.
     */
    public List<TbGroup> getGroupList(String chainId, Byte groupStatus) throws BaseException {
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
    public void updateGroupStatus(String chainId, String groupId, int groupStatus) {
        log.info("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        this.tbGroupMapper.updateStatus(chainId, groupId, groupStatus);
        log.info("end updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(String chainId, String groupId) throws BaseException {
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
    public void resetGroupByChain(String chainId) {
        Instant startTime = Instant.now();
        log.info("start exec method [resetGroupByChain] chainId:{} startTime:{}", chainId, startTime.toEpochMilli());

        // get all front
        List<TbFront> frontList = tbFrontMapper.selectByChainIdAndStatus(chainId, FrontStatusEnum.RUNNING.getId());
        if (frontList == null || frontList.size() == 0) {
            log.info("chain {} not found any front.", chainId);
            return;
        }

        // all groupId from chain
        Set<String> allGroupSet = new HashSet<>();

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
                String gId = groupId;
                allGroupSet.add(gId);
                // peer in group
                List<String> groupPeerList =
                        frontInterface.getGroupPeersFromSpecificFront(frontPeerName, frontIp, frontPort, gId);
                // save group
                groupManager.saveGroup("", null, gId, chainId, null, groupPeerList.size(), "synchronous",
                        GroupType.SYNC.getValue());

                List<PeerInfo> peerInfoList = nodeService.getSealerAndObserverListFromSpecificFront(gId, frontPeerName, frontIp, frontPort);
                List<String> sealerAndObserverList = peerInfoList.stream().map(p -> p.getNodeId()).collect(Collectors.toList());
                if (sealerAndObserverList.contains(front.getNodeId())) {
                    frontGroupMapService.newFrontGroup(chainId, front.getFrontId(), gId);
                } else {
                    //remove old front-group-map(just save sealer's and observer's front map)
                    //todo open
                    //tbFrontGroupMapMapper.deleteByChainIdAndFrontIdAndGroupId(chainId, front.getFrontId(), gId);
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
            List<String> groupOnMapList = frontGroupMapService.getGroupByChainAndFront(chainId, front.getFrontId());
            if (CollectionUtils.isEmpty(groupOnMapList)) continue;
            //todo open
//            groupOnMapList.stream()
//                    .filter(g -> !groupIdList.contains(String.valueOf(g)))
//                    .forEach(g -> tbFrontGroupMapMapper.deleteByChainIdAndFrontIdAndGroupId(chainId, front.getFrontId(), g));

        }

        // check group status
        // todo check "del the front-group-map"
        //checkGroupStatusAndRemoveInvalidGroup(chainId, allGroupSet);
        // clear cache
        frontGroupMapCache.clearMapList(chainId);

        log.info("end exec method [resetGroupByChain] . chainId:{} useTime:{} ", chainId,
                Duration.between(startTime, Instant.now()).toMillis());
    }


    /**
     * Check the validity of the groupId.
     */
    public void checkGroupIdExisted(String chainId, String groupId) throws BaseException {
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
    public void checkGroupIdValid(String chainId, String groupId) throws BaseException {
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
    private void savePeerList(String chainId, String frontPeerName, String frontIp, Integer frontPort, String groupId,
                              List<String> groupPeerList) {
        // get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(chainId, groupId);
        // get peers on chain
        Peers.PeersInfo peersPeersInfo =
            frontInterface.getPeersFromSpecificFront(frontPeerName, frontIp, frontPort,
                groupId);
        List<PeerInfo> peerList = new ArrayList<>();
        for (int i = 0; i < peersPeersInfo.getPeers().size(); i++) {
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.setNodeId(peersPeersInfo.getPeers().get(i).getP2pNodeID());
            peerInfo.setIPAndPort(peersPeersInfo.getEndPoint());
            peerList.add(peerInfo);
        }
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
    private void removeAllGroup(String chainId) {
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
    private void removeInvalidPeer(String chainId, String groupId, String peerName, String frontIp, Integer frontPort, List<String> groupPeerList) {
        if (StringUtils.isBlank(groupId)) {
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
    private boolean checkSealerAndObserverListContains(String groupId, String peerName, String frontIp, Integer frontPort, String nodeId) {
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
    private void checkGroupStatusAndRemoveInvalidGroup(String chainId,
                                                       Set<String> allGroupOnChain) {
        log.info("start exec method [checkGroupStatusAndRemoveInvalidGroup] chain:{} allGroupOnChain:{}", chainId, JsonTools.objToString(allGroupOnChain));
        if (CollectionUtils.isEmpty(allGroupOnChain))
            return;

        List<TbGroup> allLocalGroup = getGroupList(chainId, null);
        if (CollectionUtils.isEmpty(allLocalGroup))
            return;

        for (TbGroup localGroup : allLocalGroup) {
            String localGroupId = localGroup.getGroupId();
            //todo check "filter(id -> id == localGroupId).count()"
            long count = allGroupOnChain.stream().filter(id -> id.equals(localGroupId)).count();
            try {
                if (count > 0) {
                    log.info("group check pass, chainId:{} localGroupId:{}", chainId, localGroupId);
                    if (!Objects.equals(DataStatus.NORMAL.getValue(), localGroup.getGroupStatus()))
                        // update NORMAL
                        updateGroupStatus(chainId, localGroupId, DataStatus.NORMAL.getValue());
                    continue;
                }

                Date modifyTime = localGroup.getModifyTime();
                log.warn("group check pass, chainId:{} localGroupId:{}", chainId, localGroupId);
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
    private void removeByGroupId(String chainId, String groupId) {
        if (StringUtils.isBlank(chainId)|| StringUtils.isBlank(groupId)) {
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
    public void removeByChainId(String chainId) {
        if (chainId.isEmpty()) {
            return;
        }
        // remove chainId.
        this.tbGroupMapper.deleteByChainId(chainId);
    }

    /**
     * update status.
     */
    @Transactional
    public void updateGroupNodeCount(String chainId, String groupId, int nodeCount) {
        log.debug("start updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);
        this.tbGroupMapper.updateNodeCount(chainId, groupId, nodeCount);
        log.debug("end updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);

    }



    /**
     * @param chainId
     * @param pageSize
     * @param pageNumber
     * @return
     */
    public BasePageResponse queryGroupByPage(String chainId, Integer agencyId, Integer pageSize, Integer pageNumber, Byte status, String sortType) {
        // check id
        chainManager.requireChainIdExist(chainId);
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
            List<String> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
            if (CollectionUtils.isEmpty(groupIdList)) {
                //todo check
                criteria.andGroupIdEqualTo("group0");
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
    public List<String> listGroupIdByChainAndAgencyId(String chainId, int agencyId) {
        log.info("start exec method[listGroupIdByChainAndAgencyId] chainId:{} agencyId:{}", chainId, agencyId);

        //list frontId
        List<Integer> frontIdList = frontManager.listFrontIdByChainAndAgency(chainId, agencyId);
        if (CollectionUtils.isEmpty(frontIdList)) {
            log.info("finish exec method[listGroupIdByChainAndAgencyId] not found front record by agencyId:{}", agencyId);
            return Collections.EMPTY_LIST;
        }

        //list group
        List<String> groupIdList = frontGroupMapService.listGroupIdByChainAndFronts(chainId, frontIdList);
        log.info("success exec method[listGroupIdByChainAndAgencyId] chainId:{} agencyId:{} result:{}", chainId, agencyId, JsonTools.objToString(groupIdList));
        return groupIdList;
    }


    /**
     * @param chainId
     * @param agencyId
     * @return
     */
    public List<TbGroup> listGroupByChainAndAgencyId(String chainId, int agencyId) {
        log.info("start exec method[listGroupByChainAndAgencyId] chainId:{} agencyId:{}", chainId, agencyId);
        List<String> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
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
     * @param chain
     * @param group
     * @return
     */
    public RspGroupDetailVo queryGroupDetail(String chain, String group) {
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
    public void requireFoundGroupByChainAndGroup(int agencyId, String chainId, String groupId) {
        List<String> groupIdList = listGroupIdByChainAndAgencyId(chainId, agencyId);
        if (CollectionUtils.isEmpty(groupIdList) || !groupIdList.contains(groupId))
            throw new BaseException(ConstantCode.NOT_FOUND_GROUP_BY_AGENCY_AND_CHAIN.attach("agency:" + agencyId + " chain:" + chainId + " group:" + groupId));
    }
}
