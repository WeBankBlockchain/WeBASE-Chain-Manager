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

import com.webank.webase.chain.mgr.agency.AgencyService;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.code.RetCode;
import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.FrontTypeEnum;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.config.NodeConfig;
import com.webank.webase.chain.mgr.deploy.req.DeployHost;
import com.webank.webase.chain.mgr.deploy.req.ReqAddNode;
import com.webank.webase.chain.mgr.deploy.service.DeployShellService;
import com.webank.webase.chain.mgr.deploy.service.HostService;
import com.webank.webase.chain.mgr.deploy.service.ImageService;
import com.webank.webase.chain.mgr.deploy.service.NodeAsyncService;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.PeerOfConsensusStatus;
import com.webank.webase.chain.mgr.frontinterface.entity.PeerOfSyncStatus;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbNodeMapper;
import com.webank.webase.chain.mgr.util.PrecompiledUtils;
import com.webank.webase.chain.mgr.util.SshUtil;
import com.webank.webase.chain.mgr.util.ValidateUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private FrontManager frontManager;
    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private PathService pathService;
    @Autowired
    private DockerOptions dockerOptions;
    @Autowired
    private FrontService frontService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private ChainService chainService;
    @Autowired
    private ImageService imageService;
    @Qualifier(value = "deployAsyncScheduler")
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private HostService hostService;
    @Autowired
    private AgencyService agencyService;
    @Autowired
    private NodeAsyncService nodeAsyncService;

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
            log.debug("finish exec method[addNodeInfo]. jump over, found record by node:{} chain:{} group:{}", peerInfo.getNodeId(), chainId, groupId);
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
    public TbNode queryByNodeId(int chainId, String nodeId) throws BaseException {
        log.debug("start queryNodechainId:{},nodeId:{}",chainId, nodeId);
        try {
            TbNode nodeRow = this.tbNodeMapper.getByNodeId(chainId, nodeId);
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
    public List<PeerInfo> getSealerAndObserverListFromSpecificFront(int groupId, String peerName, String frontIp, Integer frontPort) {
        log.debug("start getSealerAndObserverListFromSpecificFront groupId:{}", groupId);
        List<String> sealerList = frontInterface.getSealerListFromSpecificFront(peerName, frontIp, frontPort, groupId);
        List<String> observerList = frontInterface.getObserverListFromSpecificFront(peerName, frontIp, frontPort, groupId);
        List<PeerInfo> resList = new ArrayList<>();
        sealerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        observerList.stream().forEach(nodeId -> resList.add(new PeerInfo(nodeId)));
        log.debug("end getSealerAndObserverListFromSpecificFront resList:{}", resList);
        return resList;
    }


    /**
     * @param chainId
     * @param groupId
     * @return
     */
    public List<String> getSealerAndObserverList(int chainId, int groupId) {
        log.debug("start getSealerAndObserverList chainId:{} groupId:{}", chainId, groupId);
        List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
        List<String> observerList = frontInterface.getObserverList(chainId, groupId);

        //result
        List<String> resList = new ArrayList<>();
        resList.addAll(sealerList);
        resList.addAll(observerList);

        List<String> resultList = resList.stream().distinct().collect(Collectors.toList());
        log.debug("end getSealerAndObserverList resultList:{}", resultList);
        return resultList;
    }

    /**
     * @param chainId
     * @param groupId
     * @param agencyId
     * @return
     */
    public List<String> listSealerAndObserverByGroupAndAgency(int chainId, int groupId, int agencyId) {
        log.debug("start listSealerAndObserverByGroupAndAgency chainId:{} groupId:{} agencyId:{}", chainId, groupId, agencyId);

        List<String> sealerAndObserverOnGroup = getSealerAndObserverList(chainId, groupId);
        List<String> nodeIdByAgency = frontManager.listNodeIdByAgency(agencyId);

        List<String> result = sealerAndObserverOnGroup
                .stream()
                .filter(node -> nodeIdByAgency.contains(node))
                .distinct()
                .collect(Collectors.toList());

        log.debug("success listSealerAndObserverByGroupAndAgency chainId:{} groupId:{} agencyId:{} result:{}", chainId, groupId, agencyId, JsonTools.objToString(result));
        return result;
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

        if (PrecompiledUtils.NODE_TYPE_SEALER.equalsIgnoreCase(nodeType)) {
            List<String> sealerList = frontInterface.getSealerList(chainId, groupId);
            log.info("nodesOfSealerType:{}", JsonTools.objToString(sealerList));
            return sealerList;
        }

        if (PrecompiledUtils.NODE_TYPE_OBSERVER.equalsIgnoreCase(nodeType)) {
            List<String> observerList = frontInterface.getObserverList(chainId, groupId);
            log.info("nodesOfObserverType:{}", JsonTools.objToString(observerList));
            return observerList;
        }

        if (PrecompiledUtils.NODE_TYPE_REMOVE.equalsIgnoreCase(nodeType)) {
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
        if (CollectionUtils.isNotEmpty(sealerList) && sealerList.contains(nodeId)) {
            log.info("finish exec method [getNodeType]. nodeType:{}", PrecompiledUtils.NODE_TYPE_SEALER);
            return PrecompiledUtils.NODE_TYPE_SEALER;
        }

        List<String> observerList = frontInterface.getObserverList(chainId, groupId);
        if (CollectionUtils.isNotEmpty(observerList) && observerList.contains(nodeId)) {
            log.info("finish exec method [getNodeType]. nodeType:{}", PrecompiledUtils.NODE_TYPE_OBSERVER);
            return PrecompiledUtils.NODE_TYPE_OBSERVER;
        }

        List<String> nodeIdList = frontInterface.getNodeIdList(chainId, groupId);
        if (CollectionUtils.isNotEmpty(nodeIdList) && nodeIdList.contains(nodeId)) {
            log.info("finish exec method [getNodeType]. nodeType:{}", PrecompiledUtils.NODE_TYPE_REMOVE);
            return PrecompiledUtils.NODE_TYPE_REMOVE;
        }


        log.error("fail exec method [getNodeType].  not found record by chainId:{} groupId:{} nodeId:{}", chainId, groupId, nodeId);
        return null;
    }

    /* add node */

    /**
     * Add a node. 扩容节点，并重启链的所有节点
     * include: gen config & update other nodes & restart all node
     * after check host and init host(dependency,port,image)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Pair<RetCode, String> addNodes(ReqAddNode addNode,
        DockerImageTypeEnum dockerImageTypeEnum) throws BaseException, InterruptedException {

        String chainName = addNode.getChainName();
        int groupId = addNode.getGroupId();
        int chainId = addNode.getChainId();

        // todo check groupId exist, only support add new node in existed group
        int groupCount = this.tbGroupMapper.countByChainIdAndGroupId(chainId, groupId);
        log.debug("checkGroupIdExisted groupId:{} groupCount:{}", groupId, groupCount);
        if (groupCount <= 0) {
            throw new BaseException(ConstantCode.ADD_NEW_NODES_MUST_USING_EXISTED_GROUP_ID);
        }

        // validate chain
        log.info("Check chainId:[{}] exists....", addNode.getChainId());
        TbChain chain = tbChainMapper.selectByPrimaryKey(addNode.getChainId());
        if (chain == null) {
            throw new BaseException(ConstantCode.CHAIN_ID_NOT_EXISTS);
        }
        log.info("Check chainName:[{}] exists....", addNode.getChainName());
        chain = tbChainMapper.getByChainName(addNode.getChainName());
        if (chain == null) {
            throw new BaseException(ConstantCode.CHAIN_NAME_NOT_EXISTS_ERROR);
        }

        // get version from tb_chain
        String version = chain.getVersion();
        // check image tar file when install with offline
        imageService.checkLocalImageByDockerImageTypeEnum(dockerImageTypeEnum, version);

        // get encrypt type
        EncryptTypeEnum encryptType = EncryptTypeEnum.getById(chain.getChainType());

        // check host connect and docker image
        List<DeployHost> deployNodeInfoList = addNode.getDeployHostList();
        for (int i = 0; i < addNode.getDeployHostList().size(); i++) {
            DeployHost host = addNode.getDeployHostList().get(i);
            // check host connect
            SshUtil.verifyHostConnect(host.getIp(), host.getSshUser(), host.getSshPort(), constantProperties.getPrivateKey());

            // check docker image exists on host
            if (DockerImageTypeEnum.MANUAL == dockerImageTypeEnum) {
                boolean exists = this.dockerOptions.checkImageExists(host.getIp(),
                    host.getDockerDemonPort(), host.getSshUser(), host.getSshPort(), version);
                if (!exists) {
                    log.error("Docker image:[{}] not exists on host:[{}].", version, host.getIp());
                    throw new BaseException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.attach(host.getIp()));
                }
            }
            // todo check mem/cpu
        }

        // generate agency cert
        String agencyName = addNode.getAgencyName();
        agencyService.genNewAgencyCert(agencyName, chainName, encryptType);

        log.info("addNodes chainName:{},deployNodeInfoList:{},tagId:{},encrtypType:{},"
                + "webaseSignAddr:{},agencyName:{}", chainName, deployNodeInfoList,
            chain.getVersion(), chain.getChainType(), chain.getWebaseSignAddr(), agencyName);


        // deployNodeInfo group by host ip
        // todo verify same host list has same sshUser or rootDir
        Map<String, List<DeployHost>> hostIdAndInfoMap = new HashMap<>();
        for (DeployHost nodeInfo : deployNodeInfoList) {
            String hostIp = nodeInfo.getIp();
            List<DeployHost> value = hostIdAndInfoMap.get(hostIp);
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(nodeInfo);
            hostIdAndInfoMap.put(hostIp, value);
        }
        log.info("addNodes hostIdAndInfoMap:{}", hostIdAndInfoMap);
        List<String> hostIpList = new ArrayList<>(hostIdAndInfoMap.keySet());

        // new Front list record
        List<Integer> newFrontIdList = new ArrayList<>();
        List<TbFront> newFrontListStore = new ArrayList<>();
        final CountDownLatch configHostLatch = new CountDownLatch(CollectionUtils.size(hostIdAndInfoMap));
        // check success count
        AtomicInteger configSuccessCount = new AtomicInteger(0);
        Map<String, Future> taskMap = new HashedMap<>();
        // mark chain as adding ,to avoid refresh
        this.chainService.updateStatus(chain.getChainId(), ChainStatusEnum.NODE_ADDING, "adding new nodes");
        // concurrent add nodes in multi host
        for (final String hostIp : hostIdAndInfoMap.keySet()) {
            Instant startTime = Instant.now();
            log.info("batchAddNode hostIp:{}, startTime:{}", hostIp, startTime.toEpochMilli());
            List<DeployHost> nodeListOnSameHost = hostIdAndInfoMap.get(hostIp);
            TbChain finalChain = chain;
            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    // generate ip/agency/sdk cert and scp
                    log.info("batchAddNode generateHostSDKCertAndScp");
                    DeployHost deployHost = nodeListOnSameHost.get(0);
                    hostService.generateHostSDKCertAndScp(finalChain.getChainType(),
                        chainId, chainName, deployHost, agencyName);

                    // init front config files and db data
                    // include node cert
                    log.info("batchAddNode initFrontAndNode");
                    List<TbFront> newFrontResult = frontService.initFrontAndNode(nodeListOnSameHost,
                        finalChain, agencyName, hostIp, groupId, FrontStatusEnum.ADDING);
                    newFrontListStore.addAll(newFrontResult);
                    newFrontIdList.addAll(newFrontResult.stream().map(TbFront::getFrontId).collect(Collectors.toList()));
                    log.info("batchAddNode initFrontAndNode newFrontIdList:{}", newFrontIdList);

                    // generate(actual copy same old group of group1)
                    // and scp to target new Front
                    log.info("batchAddNode generateNewNodesGroupConfigsAndScp");
                    // generate group config.ini and scp
                    groupService.generateNewNodesGroupConfigsAndScp(finalChain, groupId, newFrontResult);
                    configSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("batchAddNode Exception:[].", e);
                    newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
                    // update in each config process
                    chainService.updateStatus(chainId, ChainStatusEnum.RUNNING, "batchAddNode failed" + e.getMessage());
                } finally {
                    configHostLatch.countDown();
                }
            });
            taskMap.put(hostIp, task);
        }
        // await and check time out
        configHostLatch.await(constantProperties.getExecAddNodeTimeout(), TimeUnit.MILLISECONDS);
        log.info("Verify batchAddNode timeout");
        taskMap.forEach((key, value) -> {
            String hostIp = key;
            Future<?> task = value;
            if (!task.isDone()) {
                log.error("batchAddNode:[{}] timeout, cancel the task.", hostIp);
                newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
                chainService.updateStatus(chainId, ChainStatusEnum.RUNNING, "batchAddNode failed for timeout");
                task.cancel(false);
            }
        });

        boolean hostConfigSuccess = configSuccessCount.get() == CollectionUtils.size(hostIpList);
        // check if all host init success
        log.log(hostConfigSuccess ? Level.INFO: Level.ERROR,
            "batchAddNode result, total:[{}], success:[{}]",
            CollectionUtils.size(hostIdAndInfoMap.keySet()), configSuccessCount.get());

        // update after all host config finish
        // select all node list into config.ini
        log.info("batchAddNode updateNodeConfigIniByGroupId");
        try {
            frontService.updateConfigIniByGroupIdAndNewFront(chain, groupId, newFrontListStore);
        } catch (IOException e) {
            log.error("batchAddNode updateNodeConfigIniByGroupId io Exception:[].", e);
            newFrontIdList.forEach((id -> frontService.updateStatus(id, FrontStatusEnum.ADD_FAILED)));
        }

        log.info("batchAddNode asyncStartAddedNode");
        // restart new node
        nodeAsyncService.asyncStartAddedNode(chain.getChainId(), OptionType.MODIFY_CHAIN, newFrontIdList);

        return Pair.of(ConstantCode.SUCCESS, "success");
    }

    /**
     * Find the first node for coping group config files.
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public TbNode getOldestNodeByChainIdAndGroupId(int chainId, int groupId) {
        List<TbNode> tbNodeList = this.selectNodeListByChainIdAndGroupId(chainId, groupId);
        if (CollectionUtils.isEmpty(tbNodeList)) {
            return null;
        }
        TbNode oldest = null;

        for (TbNode tbNode : tbNodeList) {
            if (oldest == null){
                oldest = tbNode;
                continue;
            }
            if (tbNode.getCreateTime().before(oldest.getCreateTime())){
                oldest = tbNode;
            }
        }
        return oldest;
    }

    /**
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public List<TbNode> selectNodeListByChainIdAndGroupId(Integer chainId, final int groupId){
        // select all fronts by all agencies
        List<TbFront> tbFrontList = this.frontService.selectFrontListByChainId(chainId);
        log.info("selectNodeListByChainIdAndGroupId tbFrontList:{}", tbFrontList);

        // filter only not removed node will be added
        List<TbNode> tbNodeList = tbFrontList.stream()
            .map((front) -> tbNodeMapper.getByNodeIdAndGroupId(chainId, front.getNodeId(), groupId))
            .filter(Objects::nonNull)
            .filter((node) -> node.getGroupId() == groupId)
            .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tbNodeList)) {
            log.error("Group of:[{}] chain:[{}] has no node.", groupId, chainId);
            return Collections.emptyList();
        }
        return tbNodeList;
    }


    /**
     * @param nodeId
     * @return
     */
    @Transactional
    public void stopNode(int chainId, String nodeId) {
        log.info("stopNode chainId:{},nodeId:{}", chainId, nodeId);
        // get front
        TbFront front = this.frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (front == null){
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        int runningTotal = 0;
        List<TbFront> frontList = this.frontService.selectFrontListByChainId(chainId);
        for (TbFront tbFront : frontList) {
            if (FrontStatusEnum.isRunning(tbFront.getFrontStatus())) {
                runningTotal ++;
            }
        }

        if (runningTotal < 2) {
            log.error("Two running nodes at least of chain:[{}]", chainId);
            throw new BaseException(ConstantCode.TWO_NODES_AT_LEAST);
        }

        if (!FrontStatusEnum.isRunning(front.getFrontStatus())) {
            log.warn("Node:[{}:{}] is already stopped.",front.getFrontIp(),front.getHostIndex());
            return;
        }

        // check front(node) type, only deploy added nodes could be deleted
        if (!FrontTypeEnum.isDeployAdded(front.getFrontType())) {
            log.error("only support delete deploy added nodes");
            throw new BaseException(ConstantCode.ONLY_SUPPORT_STOP_CHAIN_DEPLOY_NODE_ERROR);
        }

        // select node list and check if removed node
        List<TbNode> nodeList = this.tbNodeMapper.selectByNodeId(chainId, nodeId);
        log.info("stopNode nodeList:{}", nodeList);
        // node is removed and doesn't belong to any group. then local tb_node table would delete removed node
        // if observer to removed, this observer would still return groupId(as a observer)
        boolean nodeRemovable = CollectionUtils.isEmpty(nodeList);

        if (!nodeRemovable) {
            // node belongs to some groups, check if it is the last one of each group.
            Set<Integer> groupIdSet = nodeList.stream().map(TbNode::getGroupId)
                .collect(Collectors.toSet());

            for (Integer groupId : groupIdSet) {
                int nodeCountOfGroup = CollectionUtils.size(this.tbNodeMapper.selectByGroupId(chainId, groupId));
                if (nodeCountOfGroup != 1) { // group has another node.
                    throw new BaseException(ConstantCode.NODE_NEED_REMOVE_FROM_GROUP_ERROR.attach(groupId));
                }
            }
        }

        log.info("Docker stop and remove container front id:[{}:{}].", front.getFrontId(), front.getContainerName());
        this.dockerOptions.stop(front.getFrontIp(), front.getDockerPort(), front.getSshUser(),
            front.getSshPort(), front.getContainerName());
        try {
            Thread.sleep(constantProperties.getDockerRestartPeriodTime());
        } catch (InterruptedException e) {
            log.warn("Docker stop and remove container sleep Interrupted");
            Thread.currentThread().interrupt();
        }

        // update front
        this.frontService.updateStatus(front.getFrontId(), FrontStatusEnum.STOPPED);
    }

    /**
     *  @param nodeId
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteNode(int chainId, String nodeId) {
        log.info("deleteNode chainId:{},nodeId:{}", chainId, nodeId);
        int errorFlag = 0;
        // remove front
        TbFront front = this.frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (front == null) {
            throw new BaseException(ConstantCode.NODE_ID_NOT_EXISTS_ERROR);
        }

        // check front(node) type, only deploy added nodes could be deleted
        if (!FrontTypeEnum.isDeployAdded(front.getFrontType())) {
            log.error("only support delete deploy added nodes");
            throw new BaseException(ConstantCode.ONLY_SUPPORT_DELETE_ADDED_NODE_ERROR);
        }
        // check front status, only not running node could be deleted
        if (FrontStatusEnum.isRunning(front.getFrontStatus())) {
            log.error("only support delete stopped nodes");
            throw new BaseException(ConstantCode.NODE_RUNNING_ERROR);
        }

        TbChain chain = this.tbChainMapper.selectByPrimaryKey(chainId);
        final byte encryptType = chain.getChainType();
        String ip = front.getFrontIp();
        // get delete node's group id list from ./NODES_ROOT/default_chain/ip/node[x]/conf/group.[groupId].genesis
        Path nodePath = this.pathService.getNodeRoot(chain.getChainName(), ip, front.getHostIndex());
        Set<Integer> groupIdSet = NodeConfig.getGroupIdSet(nodePath, EncryptTypeEnum.getById(encryptType));
        log.info("deleteNode updateNodeConfigIniByGroupList chain:{}, groupIdSet:{}", chain, groupIdSet);
        // update related node's config.ini file, e.g. p2p
        try {
            log.info("deleteNode updateNodeConfigIniByGroupList chain:{}, groupIdSet:{}", chain, groupIdSet);
            // update related node's config.ini file, e.g. p2p
            this.frontService.updateNodeConfigIniByGroupList(chain, nodeId, groupIdSet);
        } catch (IOException e) {
            errorFlag++;
            log.error("Delete node, update related group:[{}] node's config error ", groupIdSet, e);
            log.error("Please update related node's group config manually");
        }

        // move node directory to tmp
        try {
            this.pathService.deleteNode(chain.getChainName(), ip, front.getHostIndex(), front.getNodeId());
        } catch (IOException e) {
            errorFlag++;
            log.error("Delete node's config files:[{}:{}:{}] error.",
                chain.getChainName(), ip, front.getHostIndex(), e);
            log.error("Please move/rm node's config files manually");
        }

        // move node of remote host files to temp directory, e.g./opt/fisco/delete-tmp
        NodeService.mvNodeOnRemoteHost(ip, front.getRootOnHost(), chain.getChainName(), front.getHostIndex(),
            front.getNodeId(), front.getSshUser(), front.getSshPort(), constantProperties.getPrivateKey());

        // delete front, node in db
        this.frontService.removeByFrontId(front.getFrontId());

        // if error occur, throw out finally
        if (errorFlag != 0) {
            log.error("Update related group OR delete node's config files error. Check out upper error log");
            throw new BaseException(ConstantCode.DELETE_NODE_DIR_ERROR);
        }
    }
}
