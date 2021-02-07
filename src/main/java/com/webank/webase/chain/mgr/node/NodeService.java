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
package com.webank.webase.chain.mgr.node;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.chain.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbNodeMapper;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import com.webank.webase.chain.mgr.util.SshUtil;
import com.webank.webase.chain.mgr.util.ValidateUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * services for node data.
 */
@Log4j2
@Service
public class NodeService {

    @Autowired
    private TbNodeMapper tbNodeMapper;
    @Autowired
    private TbGroupMapper tbGroupMapper;
    @Autowired
    private FrontInterfaceService frontInterface;

    private static final Long CHECK_NODE_WAIT_MIN_MILLIS = 5000L;
    private static final Long EXT_CHECK_NODE_WAIT_MIN_MILLIS = 3500L;

    /**
     * add new node data.
     */
    public void addNodeInfo(Integer chainId, Integer groupId, PeerInfo peerInfo)
            throws BaseException {

        //add db
        TbNode tbNode = this.tbNodeMapper.selectByPrimaryKey(peerInfo.getNodeId(), chainId, groupId);
        if (Objects.nonNull(tbNode)) {
            log.info("finish exec method[addNodeInfo]. jump over, found record by node:{} chain:{} group:{}", peerInfo.getNodeId(), chainId, groupId);
            return;
        }

        String nodeIp = null;
        Integer nodeP2PPort = null;

        if (StringUtils.isNotBlank(peerInfo.getIPAndPort())) {
            String[] ipPort = peerInfo.getIPAndPort().split(":");
            nodeIp = ipPort[0];
            nodeP2PPort = Integer.valueOf(ipPort[1]);
        }
        String nodeName = groupId + "_" + peerInfo.getNodeId();

        // add row
        tbNode = new TbNode();
        tbNode.setNodeId(peerInfo.getNodeId());
        tbNode.setChainId(chainId);
        tbNode.setGroupId(groupId);
        tbNode.setNodeIp(nodeIp);
        tbNode.setNodeName(nodeName);
        tbNode.setP2pPort(nodeP2PPort);
        Date now = new Date();
        tbNode.setCreateTime(now);
        tbNode.setModifyTime(now);
        this.tbNodeMapper.insertSelective(tbNode);
    }

    /**
     * query count of node.
     */
    public Integer countOfNode(NodeParam queryParam) throws BaseException {
        log.debug("start countOfNode queryParam:{}", JsonTools.toJSONString(queryParam));
        try {
            Integer nodeCount = this.tbNodeMapper.countByParam(queryParam);
            log.debug("end countOfNode nodeCount:{} queryParam:{}", nodeCount,
                    JsonTools.toJSONString(queryParam));
            return nodeCount;
        } catch (RuntimeException ex) {
            log.error("fail countOfNode . queryParam:{}", queryParam, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query node list by page.
     */
    public List<TbNode> qureyNodeList(NodeParam queryParam) throws BaseException {
        log.debug("start qureyNodeList queryParam:{}", JsonTools.toJSONString(queryParam));

        // query node list
        List<TbNode> listOfNode = this.tbNodeMapper.selectByParam(queryParam);

        log.debug("end qureyNodeList listOfNode:{}", JsonTools.toJSONString(listOfNode));
        return listOfNode;
    }

    /**
     * query node by groupId
     */
    public List<TbNode> queryByGroupId(int chainId, int groupId) {
        NodeParam nodeParam = new NodeParam();
        nodeParam.setChainId(chainId);
        nodeParam.setGroupId(groupId);
        return qureyNodeList(nodeParam);
    }

    /**
     * query all node list
     */
    public List<TbNode> getAll() {
        return qureyNodeList(new NodeParam());
    }

    /**
     * query node info.
     */
    public TbNode queryByNodeId(String nodeId) throws BaseException {
        log.debug("start queryNode nodeId:{}", nodeId);
        try {
            TbNode nodeRow = this.tbNodeMapper.getByNodeId(nodeId);
            log.debug("end queryNode nodeId:{} TbNode:{}", nodeId, JsonTools.toJSONString(nodeRow));
            return nodeRow;
        } catch (RuntimeException ex) {
            log.error("fail queryNode . nodeId:{}", nodeId, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }


    /**
     * update node info.
     */
    public void updateNode(TbNode tbNode) throws BaseException {
        log.debug("start updateNodeInfo  param:{}", JsonTools.toJSONString(tbNode));
        Integer affectRow = 0;
        try {
            tbNode.setModifyTime(new Date());

            affectRow = tbNodeMapper.update(tbNode);
        } catch (RuntimeException ex) {
            log.error("updateNodeInfo exception", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_node");
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
        log.debug("end updateNodeInfo");
    }

    /**
     * delete by node and group.
     */
    public void deleteByNodeAndGroupId(String nodeId, int groupId) throws BaseException {
        log.debug("start deleteByNodeAndGroupId nodeId:{} groupId:{}", nodeId, groupId);
        tbNodeMapper.deleteByNodeIdAndGroupId(nodeId, groupId);
        log.debug("end deleteByNodeAndGroupId");
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        tbNodeMapper.deleteByChainIdAndGroupId(chainId, groupId);
    }

    /**
     * delete by chainId.
     */
    public void deleteByChainId(int chainId) {
        if (chainId == 0) {
            return;
        }
        this.tbNodeMapper.deleteByChainId(chainId);
    }

    /**
     * check node status
     */
    public void checkAndUpdateNodeStatus(int chainId) {

        List<TbGroup> tbGroups = tbGroupMapper.selectByChainId(chainId);
        if (CollectionUtils.isEmpty(tbGroups)) {
            return;
        }

        for (TbGroup tbGroup : tbGroups) {
            this.checkAndUpdateNodeStatus(chainId, tbGroup.getGroupId());
        }
    }

    public void checkAndUpdateNodeStatus(int chainId, int groupId) {
        // get local node list
        List<TbNode> nodeList = queryByGroupId(chainId, groupId);

        // getPeerOfConsensusStatus
        List<PeerOfConsensusStatus> consensusList = getPeerOfConsensusStatus(chainId, groupId);
        if (Objects.isNull(consensusList)) {
            log.error("fail checkNodeStatus, consensusList is null");
            return;
        }

        // getObserverList
        List<String> observerList = frontInterface.getObserverList(chainId, groupId);
        int nodeCount = CollectionUtils.size(consensusList) + CollectionUtils.size(observerList);

        for (TbNode tbNode : nodeList) {
            String nodeId = tbNode.getNodeId();
            BigInteger localBlockNumber = BigInteger.valueOf(tbNode.getBlockNumber());
            BigInteger localPbftView = BigInteger.valueOf(tbNode.getPbftView());
            LocalDateTime modifyTime = CommonUtils.timestamp2LocalDateTime(tbNode.getModifyTime().getTime());
            LocalDateTime createTime = CommonUtils.timestamp2LocalDateTime(tbNode.getCreateTime().getTime());

            Duration duration = Duration.between(modifyTime, LocalDateTime.now());
            Long subTime = duration.toMillis();
            if (subTime < (nodeCount * 1000 + EXT_CHECK_NODE_WAIT_MIN_MILLIS) && createTime.isBefore(modifyTime)) {
                log.warn("checkNodeStatus jump over. for time internal subTime:{}", subTime);
                return;
            }

            int nodeType = 0; // 0-consensus;1-observer
            if (observerList != null) {
                nodeType = observerList.stream()
                        .filter(observer -> observer.equals(tbNode.getNodeId())).map(c -> 1)
                        .findFirst().orElse(0);
            }

            BigInteger latestNumber = getBlockNumberOfNodeOnChain(chainId, groupId, nodeId);// blockNumber
            BigInteger latestView =
                    consensusList.stream().filter(cl -> nodeId.equals(cl.getNodeId()))
                            .map(c -> c.getView()).findFirst().orElse(BigInteger.ZERO);// pbftView

            if (nodeType == 0) { // 0-consensus;1-observer
                if (localBlockNumber.equals(latestNumber) && localPbftView.equals(latestView)) {
                    log.warn(
                            "node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber.longValue());
                    tbNode.setPbftView(latestView.longValue());
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            } else { // observer
                if (!latestNumber.equals(frontInterface.getLatestBlockNumber(chainId, groupId))) {
                    log.warn(
                            "node[{}] is invalid. localNumber:{} chainNumber:{} localView:{} chainView:{}",
                            nodeId, localBlockNumber, latestNumber, localPbftView, latestView);
                    tbNode.setNodeActive(DataStatus.INVALID.getValue());
                } else {
                    tbNode.setBlockNumber(latestNumber.longValue());
                    tbNode.setPbftView(latestView.longValue());
                    tbNode.setNodeActive(DataStatus.NORMAL.getValue());
                }
            }

            // update node
            updateNode(tbNode);
        }
    }

    /**
     * get latest number of peer on chain.
     */
    public BigInteger getBlockNumberOfNodeOnChain(int chainId, int groupId, String nodeId) {
        SyncStatus syncStatus = frontInterface.getSyncStatus(chainId, groupId);
        if (nodeId.equals(syncStatus.getNodeId())) {
            return syncStatus.getBlockNumber();
        }
        List<PeerOfSyncStatus> peerList = syncStatus.getPeers();
        BigInteger latestNumber = peerList.stream().filter(peer -> nodeId.equals(peer.getNodeId()))
                .map(s -> s.getBlockNumber()).findFirst().orElse(BigInteger.ZERO);// blockNumber
        return latestNumber;
    }

    /**
     * get peer of consensusStatus
     */
    private List<PeerOfConsensusStatus> getPeerOfConsensusStatus(int chainId, int groupId) {
        String consensusStatusJson = frontInterface.getConsensusStatus(chainId, groupId);
        if (StringUtils.isBlank(consensusStatusJson)) {
            return null;
        }
        List jsonArr = JsonTools.toJavaObject(consensusStatusJson, List.class);
        if (jsonArr == null) {
            log.error("getPeerOfConsensusStatus error");
            throw new BaseException(ConstantCode.FAIL_PARSE_JSON);
        }
        List<PeerOfConsensusStatus> dataIsList = new ArrayList<>();
        for (int i = 0; i < jsonArr.size(); i++) {
            if (jsonArr.get(i) instanceof List) {
                List<PeerOfConsensusStatus> tempList = JsonTools.toJavaObjectList(
                        JsonTools.toJSONString(jsonArr.get(i)), PeerOfConsensusStatus.class);
                if (tempList != null) {
                    dataIsList.addAll(tempList);
                } else {
                    throw new BaseException(ConstantCode.FAIL_PARSE_JSON);
                }
            }
        }
        return dataIsList;
    }

    /**
     * add sealer and observer in NodeList return: List<String> nodeIdList
     */
    public List<PeerInfo> getSealerAndObserverList(int chainId, int groupId) {
        log.debug("start getSealerAndObserverList groupId:{}", groupId);
        List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
        List<String> observerList = frontInterface.getObserverList(chainId, groupId);
        List<PeerInfo> resList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        log.debug("end getSealerAndObserverList resList:{}", resList);
        return resList;
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeId
     * @return
     */
    public static String getNodeName(int chainId, int groupId, String nodeId) {
        return String.format("%s_%s_%s", chainId, groupId, nodeId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public TbNode insert(int chainId, String nodeId, String nodeName, int groupId, String ip, int p2pPort,
                         String description, final DataStatus dataStatus) throws BaseException {
        if (!ValidateUtil.ipv4Valid(ip)) {
            throw new BaseException(ConstantCode.IP_FORMAT_ERROR);
        }

        DataStatus newDataStatus = dataStatus == null ? DataStatus.INVALID : dataStatus;

        TbNode node = TbNode.init(chainId, nodeId, nodeName, groupId, ip, p2pPort, description, newDataStatus);

        if (tbNodeMapper.insertSelective(node) != 1) {
            throw new BaseException(ConstantCode.INSERT_NODE_ERROR);
        }
        return node;
    }

    /**
     * @param ip
     * @param rooDirOnHost
     * @param chainName
     * @param hostIndex
     * @param nodeId
     */
    public static void mvNodeOnRemoteHost(String ip, String rooDirOnHost, String chainName, int hostIndex, String nodeId,
                                          String sshUser, int sshPort, String privateKey) {
        // create /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss as a parent
        String chainDeleteRootOnHost = PathService.getChainDeletedRootOnHost(rooDirOnHost, chainName);
        SshUtil.createDirOnRemote(ip, chainDeleteRootOnHost, sshUser, sshPort, privateKey);

        // e.g. /opt/fisco/default_chain
        String chainRootOnHost = PathService.getChainRootOnHost(rooDirOnHost, chainName);
        // e.g. /opt/fisco/default_chain/node[x]
        String src_nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, hostIndex);

        // move to /opt/fisco/deleted-tmp/default_chain-yyyyMMdd_HHmmss/[nodeid(128)]
        String dst_nodeDeletedRootOnHost =
                PathService.getNodeDeletedRootOnHost(chainDeleteRootOnHost, nodeId);
        // move
        SshUtil.mvDirOnRemote(ip, src_nodeRootOnHost, dst_nodeDeletedRootOnHost, sshUser, sshPort, privateKey);
    }


    /**
     * @param chainId
     * @param nodeId
     * @return
     */
    public void requireNodeIdValid(int chainId, int groupId, String nodeId) {
        log.debug("start exec method[requireNodeExist] chainId:{} groupId:{} nodeId:{}", chainId, groupId, nodeId);

        List<String> nodeIdList = frontInterface.getNodeIdList(chainId, groupId);
        if (CollectionUtils.isEmpty(nodeIdList))
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR.attach("query node list,but result is empty"));

        if (!nodeIdList.contains(nodeId)) {
            log.warn("fail exec method[requireNodeExist]. not found nodeId:{} but found:{}", nodeId, JsonTools.objToString(nodeIdList));
            throw new BaseException(ConstantCode.INVALID_NODE_ID);
        }
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeTypes
     * @return
     */
    public List<String> getNodeIdByTypes(int chainId, int groupId, List<String> nodeTypes) {

        if (CollectionUtils.isEmpty(nodeTypes))
            return getNodeIds(chainId, groupId, null);


        List<String> nodeIds = new ArrayList<>();
        for (String type : nodeTypes) {
            nodeIds.addAll(getNodeIds(chainId, groupId, type));
        }

        return nodeIds.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @param chainId
     * @param groupId
     * @param nodeType
     * @return
     */
    public List<String> getNodeIds(int chainId, int groupId, String nodeType) {

        if (StringUtils.isBlank(nodeType)) {
            List<String> nodeIdList = frontInterface.getNodeIdList(chainId, groupId);
            log.info("allNodeId:{}", JsonTools.objToString(nodeIdList));
            return nodeIdList;
        }

        if (PrecompiledUtils.NODE_TYPE_SEALER.equals(nodeType)) {
            List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
            log.info("nodesOfSealerType:{}", JsonTools.objToString(sealerList));
            return sealerList;
        }

        if (PrecompiledUtils.NODE_TYPE_OBSERVER.equals(nodeType)) {
            List<String> observerList = frontInterface.getObserverList(chainId, groupId);
            log.info("nodesOfObserverType:{}", JsonTools.objToString(observerList));
            return observerList;
        }

        if (PrecompiledUtils.NODE_TYPE_REMOVE.equals(nodeType)) {
            List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
            List<String> observerList = frontInterface.getObserverList(chainId, groupId);
            List<String> nodeIdList = frontInterface.getNodeIdList(chainId, groupId);
            List<String> sealerOrObserverList = Stream.of(sealerList, observerList).flatMap(x -> x.stream()).collect(Collectors.toList());
            List<String> nodesOfRemoveType = nodeIdList.stream().filter(node -> !sealerOrObserverList.contains(node)).distinct().collect(Collectors.toList());
            log.info("nodesOfRemoveType:{}", JsonTools.objToString(nodesOfRemoveType));
            return nodesOfRemoveType;
        }

        log.warn("fail exec method[getNodeIds]. not support nodeType:{}", nodeType);

        return new ArrayList<>();
    }


    /**
     * @param chainId
     * @param groupId
     * @param nodeId
     * @return
     */
    public String getNodeType(int chainId, int groupId, String nodeId) {
        log.info("start exec method[getNodeType]. chainId:{} groupId:{} nodeId:{}", chainId, groupId, nodeId);

        List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
        if (CollectionUtils.isNotEmpty(sealerList) && sealerList.contains(nodeId))
            return PrecompiledUtils.NODE_TYPE_SEALER;

        List<String> observerList = frontInterface.getObserverList(chainId, groupId);
        if (CollectionUtils.isNotEmpty(observerList) && observerList.contains(nodeId))
            return PrecompiledUtils.NODE_TYPE_OBSERVER;

        List<String> nodeIdList = frontInterface.getNodeIdList(chainId, groupId);
        if (CollectionUtils.isNotEmpty(nodeIdList) && nodeIdList.contains(nodeId))
            return PrecompiledUtils.NODE_TYPE_REMOVE;


        log.error("fail exec method [getNodeType].  not found record by chainId:{} groupId:{} nodeId:{}", chainId, groupId, nodeId);
        return null;
    }

}
