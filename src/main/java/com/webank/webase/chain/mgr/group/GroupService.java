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

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.GenerateNormalStatus;
import com.webank.webase.chain.mgr.base.enums.GroupType;
import com.webank.webase.chain.mgr.base.enums.StartNormalStatus;
import com.webank.webase.chain.mgr.base.exception.NodeMgrException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.NodeMgrTools;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.FrontParam;
import com.webank.webase.chain.mgr.front.entity.TbFront;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.GroupHandleResult;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.group.entity.GroupParam;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.chain.mgr.group.entity.ReqStartGroup;
import com.webank.webase.chain.mgr.group.entity.TbGroup;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.node.entity.TbNode;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * services for group data.
 */
@Log4j2
@Service
public class GroupService {

    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
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
     * generate group.
     * 
     * @param req info
     * @return
     */
    public TbGroup generateGroup(ReqGenerateGroup req) {
        // check id
        Integer generateGroupId = req.getGenerateGroupId();
        checkGroupIdExisted(generateGroupId);

        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByNodeId(nodeId);
            if (tbFront == null) {
                log.error("fail generateGroup node not exists.");
                throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS);
            }
            // request front to generate
            GenerateGroupInfo generateGroupInfo = new GenerateGroupInfo();
            BeanUtils.copyProperties(req, generateGroupInfo);
            GroupHandleResult groupHandleResult = frontInterface.generateGroup(tbFront.getFrontIp(),
                    tbFront.getFrontPort(), generateGroupInfo);
            int code = NodeMgrTools.parseHexStr2Int(groupHandleResult.getCode());
            // check result
            if (!GenerateNormalStatus.isInclude(code)) {
                log.error("fail generateGroup nodeId:{} code:{}.", nodeId, code);
                throw new NodeMgrException(code, groupHandleResult.getMessage());
            }
        }
        // save group
        TbGroup tbGroup = saveGroup(generateGroupId, req.getNodeList().size(), req.getDescription(),
                GroupType.MANUAL.getValue());
        return tbGroup;
    }

    /**
     * start group.
     * 
     * @param nodeId
     * @param startGroupId
     */
    public void startGroup(String nodeId, Integer startGroupId) {
        // check id
        checkGroupIdValid(startGroupId);
        // get front
        TbFront tbFront = frontService.getByNodeId(nodeId);
        if (tbFront == null) {
            log.error("fail startGroup node not exists.");
            throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS.getCode(),
                    ConstantCode.NODE_NOT_EXISTS.getMessage() + " " + nodeId);
        }
        // request front to start
        GroupHandleResult groupHandleResult = frontInterface.startGroup(tbFront.getFrontIp(),
                tbFront.getFrontPort(), startGroupId);
        // check result
        int code = NodeMgrTools.parseHexStr2Int(groupHandleResult.getCode());
        if (!StartNormalStatus.isInclude(code)) {
            log.error("fail startGroup nodeId:{} code:{}.", nodeId, code);
            throw new NodeMgrException(code, groupHandleResult.getMessage());
        }
        // refresh front
        frontInterface.refreshFront(tbFront.getFrontIp(), tbFront.getFrontPort());
    }

    /**
     * batch start group.
     * 
     * @param req
     */
    public void batchStartGroup(ReqStartGroup req) {
        Integer startGroupId = req.getGenerateGroupId();
        // check id
        checkGroupIdValid(startGroupId);
        for (String nodeId : req.getNodeList()) {
            // get front
            TbFront tbFront = frontService.getByNodeId(nodeId);
            if (tbFront == null) {
                log.error("fail startGroup node not exists.");
                throw new NodeMgrException(ConstantCode.NODE_NOT_EXISTS.getCode(),
                        ConstantCode.NODE_NOT_EXISTS.getMessage() + " " + nodeId);
            }
            // request front to start
            GroupHandleResult groupHandleResult = frontInterface.startGroup(tbFront.getFrontIp(),
                    tbFront.getFrontPort(), startGroupId);
            // check result
            int code = NodeMgrTools.parseHexStr2Int(groupHandleResult.getCode());
            if (!StartNormalStatus.isInclude(code)) {
                log.error("fail startGroup nodeId:{} code:{}.", nodeId, code);
                throw new NodeMgrException(code, groupHandleResult.getMessage());
            }
            // refresh front
            frontInterface.refreshFront(tbFront.getFrontIp(), tbFront.getFrontPort());
        }
    }

    /**
     * save group id
     */
    public TbGroup saveGroup(int groupId, int nodeCount, String description, int groupType) {
        if (groupId == 0) {
            return null;
        }
        // save group id
        String groupName = "group" + groupId;
        TbGroup tbGroup = new TbGroup(groupId, groupName, nodeCount, description, groupType);
        groupMapper.save(tbGroup);
        return tbGroup;
    }

    /**
     * query count of group.
     */
    public Integer countOfGroup(GroupParam groupParam) throws NodeMgrException {
        log.debug("start countOfGroup groupId:{}", groupParam.getGroupId());
        try {
            Integer count = groupMapper.getCount(groupParam);
            log.debug("end countOfGroup groupId:{} count:{}", groupParam.getGroupId(), count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfGroup", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query all group info.
     */
    public List<TbGroup> getGroupList(GroupParam groupParam) throws NodeMgrException {
        log.debug("start getGroupList");
        try {
            List<TbGroup> groupList = groupMapper.getList(groupParam);

            log.debug("end getGroupList groupList:{}", JSON.toJSONString(groupList));
            return groupList;
        } catch (RuntimeException ex) {
            log.error("fail getGroupList", ex);
            throw new NodeMgrException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update status.
     */
    public void updateGroupStatus(int groupId, int groupStatus) {
        log.debug("start updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
        groupMapper.updateStatus(groupId, groupStatus);
        log.debug("end updateGroupStatus groupId:{} groupStatus:{}", groupId, groupStatus);
    }

    /**
     * query group overview information.
     */
    public GroupGeneral queryGroupGeneral(int groupId) throws NodeMgrException {
        log.debug("start queryGroupGeneral groupId:{}", groupId);
        GroupGeneral generalInfo = groupMapper.getGeneral(groupId);
        return generalInfo;
    }


    /**
     * reset groupList.
     */
    @Transactional
    public void resetGroupList() {
        Instant startTime = Instant.now();
        log.info("start resetGroupList. startTime:{}", startTime.toEpochMilli());

        // all groupId from chain
        Set<Integer> allGroupSet = new HashSet<>();

        // get all front
        List<TbFront> frontList = frontService.getFrontList(new FrontParam());
        if (frontList == null || frontList.size() == 0) {
            log.info("not fount any front.");
            // remove all group
            // removeAllGroup();
            return;
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
                log.error("fail getGroupListFromSpecificFront.", ex);
                continue;
            }
            for (String groupId : groupIdList) {
                Integer gId = Integer.valueOf(groupId);
                allGroupSet.add(gId);
                // peer in group
                List<String> groupPeerList =
                        frontInterface.getGroupPeersFromSpecificFront(frontIp, frontPort, gId);
                // save group
                saveGroup(gId, groupPeerList.size(), "synchronous", GroupType.SYNC.getValue());
                frontGroupMapService.newFrontGroup(front.getFrontId(), gId);
                // save new peers
                savePeerList(frontIp, frontPort, gId, groupPeerList);
                // remove invalid peers
                removeInvalidPeer(gId, groupPeerList);
                // refresh: add sealer and observer no matter validity
                frontService.refreshSealerAndObserverInNodeList(frontIp, frontPort, gId);
            }
        }

        // check group status
        checkGroupStatusAndRemoveInvalidGroup(allGroupSet);
        // clear cache
        frontGroupMapCache.clearMapList();

        log.info("end resetGroupList. useTime:{} ",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * Check the validity of the groupId.
     */
    public void checkGroupIdExisted(Integer groupId) throws NodeMgrException {
        log.debug("start checkGroupIdExisted groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupIdExisted groupId is null");
            throw new NodeMgrException(ConstantCode.GROUP_ID_NULL);
        }

        GroupParam groupParam = new GroupParam();
        groupParam.setGroupId(groupId);
        groupParam.setGroupStatus(null);
        Integer groupCount = countOfGroup(groupParam);
        log.debug("checkGroupIdExisted groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount != null && groupCount > 0) {
            throw new NodeMgrException(ConstantCode.GROUP_ID_EXISTS);
        }
        log.debug("end checkGroupIdExisted");
    }

    /**
     * Check the validity of the groupId.
     */
    public void checkGroupIdValid(Integer groupId) throws NodeMgrException {
        log.debug("start checkGroupIdValid groupId:{}", groupId);

        if (groupId == null) {
            log.error("fail checkGroupIdValid groupId is null");
            throw new NodeMgrException(ConstantCode.GROUP_ID_NULL);
        }

        GroupParam groupParam = new GroupParam();
        groupParam.setGroupId(groupId);
        groupParam.setGroupStatus(null);
        Integer groupCount = countOfGroup(groupParam);
        log.debug("checkGroupIdValid groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount == null || groupCount == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_GROUP_ID);
        }
        log.debug("end checkGroupIdValid");
    }

    /**
     * save new peers.
     */
    private void savePeerList(String frontIp, Integer frontPort, int groupId,
            List<String> groupPeerList) {
        // get all local nodes
        List<TbNode> localNodeList = nodeService.queryByGroupId(groupId);
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
                nodeService.addNodeInfo(groupId, newPeer);
            }
        }
    }

    /**
     * remove all group.
     */
    private void removeAllGroup() {
        log.info("start remove all group.");
        List<TbGroup> allGroup = getGroupList(null);
        if (CollectionUtils.isEmpty(allGroup)) {
            log.info("removeAllGroup jump over. not fount any group");
            return;
        }

        // remove each group
        allGroup.stream().forEach(group -> removeByGroupId(group.getGroupId()));
    }

    /**
     * remove invalid peer.
     */
    private void removeInvalidPeer(int groupId, List<String> groupPeerList) {
        if (groupId == 0) {
            return;
        }
        // get local peers
        List<TbNode> localNodes = nodeService.queryByGroupId(groupId);
        if (CollectionUtils.isEmpty(localNodes)) {
            return;
        }
        // remove node that's not in groupPeerList and not in sealer/observer list
        localNodes.stream()
                .filter(node -> !groupPeerList.contains(node.getNodeId())
                        && !checkSealerAndObserverListContains(groupId, node.getNodeId()))
                .forEach(n -> nodeService.deleteByNodeAndGroupId(n.getNodeId(), groupId));
    }

    private boolean checkSealerAndObserverListContains(int groupId, String nodeId) {
        log.debug("checkSealerAndObserverListNotContains nodeId:{},groupId:{}", nodeId, groupId);
        // get sealer and observer on chain
        List<PeerInfo> sealerAndObserverList = nodeService.getSealerAndObserverList(groupId);
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
    private void checkGroupStatusAndRemoveInvalidGroup(Set<Integer> allGroupOnChain) {
        if (CollectionUtils.isEmpty(allGroupOnChain)) {
            return;
        }

        List<TbGroup> allLocalGroup = getGroupList(null);
        if (CollectionUtils.isEmpty(allLocalGroup)) {
            return;
        }

        for (TbGroup localGroup : allLocalGroup) {
            int localGroupId = localGroup.getGroupId();
            long count = allGroupOnChain.stream().filter(id -> id == localGroupId).count();
            try {
                if (count > 0) {
                    log.info("group is valid, localGroupId:{}", localGroupId);
                    // update NORMAL
                    updateGroupStatus(localGroupId, DataStatus.NORMAL.getValue());
                    continue;
                }

                if (!NodeMgrTools.isDateTimeInValid(localGroup.getModifyTime(),
                        constants.getGroupInvalidGrayscaleValue())) {
                    log.warn("remove group, localGroup:{}", JSON.toJSONString(localGroup));
                    // remove group
                    removeByGroupId(localGroupId);
                    continue;
                }

                log.warn("group is invalid, localGroupId:{}", localGroupId);
                if (DataStatus.NORMAL.getValue() == localGroup.getGroupStatus()) {
                    // update invalid
                    updateGroupStatus(localGroupId, DataStatus.INVALID.getValue());
                    continue;
                }

            } catch (Exception ex) {
                log.info("fail check group. localGroup:{}", JSON.toJSONString(localGroup));
                continue;
            }

        }
    }

    /**
     * remove by groupId.
     */
    private void removeByGroupId(int groupId) {
        if (groupId == 0) {
            return;
        }
        // remove groupId.
        groupMapper.remove(groupId);
        // remove mapping.
        frontGroupMapService.removeByGroupId(groupId);
        // remove node
        nodeService.deleteByGroupId(groupId);
        // remove contract
        contractService.deleteByGroupId(groupId);
    }
}
