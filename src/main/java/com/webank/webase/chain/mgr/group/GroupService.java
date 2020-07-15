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

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.contract.ContractService;
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
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;

import lombok.extern.log4j.Log4j2;

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
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private ConstantProperties constants;

    /**
     * generate group to single node.
     * 
     * @param req info
     * @return
     */
    public TbGroup generateToSingleNode(String nodeId, ReqGenerateGroup req) {
        // check id
        Integer chainId = req.getChainId();
        Integer generateGroupId = req.getGenerateGroupId();

        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail generateToSingleNode node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }
        // request front to generate
        GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
        BeanUtils.copyProperties(req, generateGroupInfo);
        frontInterface.generateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(),
                generateGroupInfo);
        // save group
        TbGroup tbGroup = saveGroup(generateGroupId, chainId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue());
        return tbGroup;
    }

    /**
     * generate group.
     * 
     * @param req info
     * @return
     */
    public TbGroup generateGroup(ReqGenerateGroup req) {
        // check id
        Integer chainId = req.getChainId();
        Integer generateGroupId = req.getGenerateGroupId();
        checkGroupIdExisted(chainId, generateGroupId);

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
            frontInterface.generateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(),
                    generateGroupInfo);
        }
        // save group
        TbGroup tbGroup = saveGroup(generateGroupId, chainId, req.getNodeList().size(),
                req.getDescription(), GroupType.MANUAL.getValue());
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
        // request front to operate
        Object groupHandleResult = frontInterface.operateGroup(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, type);

        // refresh
        resetGroupList();

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
            frontInterface.operateGroup(tbFront.getFrontIp(), tbFront.getFrontPort(), groupId,
                    "start");
        }
        // refresh
        resetGroupList();
    }


    /**
     * save group id
     */
    @Transactional
    public TbGroup saveGroup(int groupId, int chainId, int nodeCount, String description,
            int groupType) {
        if (groupId == 0) {
            return null;
        }
        // save group id
        String groupName = String.format("chain_%s_group_%s", chainId, groupId);
        TbGroup exists = this.tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (exists == null) {
            TbGroup tbGroup = new TbGroup(groupId, chainId, groupName, nodeCount, description, groupType);
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
            List<TbGroup> groupList = this.tbGroupMapper.selectByChainIdAndGroupStatus(chainId, groupStatus);

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
            if (! ChainStatusEnum.isRunning(tbChain.getChainStatus())){
                log.warn("Chain is not running:[{}]", tbChain.getChainStatus());
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
                String frontIp = front.getFrontIp();
                int frontPort = front.getFrontPort();
                // query group list
                List<String> groupIdList;
                try {
                    groupIdList = frontInterface.getGroupListFromSpecificFront(frontIp, frontPort);
                } catch (Exception ex) {
                    log.error("fail getGroupListFromSpecificFront frontId:{}.", front.getFrontId(), ex);
                    continue;
                }
                for (String groupId : groupIdList) {
                    Integer gId = Integer.valueOf(groupId);
                    allGroupSet.add(gId);
                    // peer in group
                    List<String> groupPeerList =
                            frontInterface.getGroupPeersFromSpecificFront(frontIp, frontPort, gId);
                    // save group
                    saveGroup(gId, chainId, groupPeerList.size(), "synchronous",
                            GroupType.SYNC.getValue());
                    frontGroupMapService.newFrontGroup(chainId, front.getFrontId(), gId);
                    // save new peers
                    savePeerList(chainId, frontIp, frontPort, gId, groupPeerList);
                    // remove invalid peers
                    removeInvalidPeer(chainId, gId, groupPeerList);
                    // refresh: add sealer and observer no matter validity
                    frontService.refreshSealerAndObserverInNodeList(frontIp, frontPort,
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
    private void savePeerList(int chainId, String frontIp, Integer frontPort, int groupId,
            List<String> groupPeerList) {
        // get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(chainId, groupId);
        // get peers on chain
        PeerInfo[] peerArr = frontInterface.getPeersFromSpecificFront(frontIp, frontPort, groupId);
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
    public void updateGroupNodeCount(int chainId,int groupId, int nodeCount) {
        log.debug("start updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);
        this.tbGroupMapper.updateNodeCount(chainId,groupId, nodeCount);
        log.debug("end updateGroupNodeCount groupId:{} nodeCount:{}", groupId, nodeCount);

    }
}
