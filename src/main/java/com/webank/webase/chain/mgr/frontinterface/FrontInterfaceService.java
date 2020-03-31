/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.frontinterface;

import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_CERT;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_CSYNC_STATUS;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_ENCRYPT_TYPE;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GET_OBSERVER_LIST;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GROUP_PEERS;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GROUP_PLIST;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_NODEID_LIST;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_PEERS;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.HttpRequestTools;
import com.webank.webase.chain.mgr.front.entity.TransactionCount;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.GroupHandleResult;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.entity.ReqSetSysConfig;
import com.webank.webase.chain.mgr.group.entity.SysConfigParam;
import com.webank.webase.chain.mgr.node.entity.ConsensusHandle;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock.Block;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;


@Log4j2
@Service
public class FrontInterfaceService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Autowired
    private ConstantProperties cproperties;

    /**
     * request from specific front.
     */
    private <T> T requestSpecificFront(Integer groupId, String frontIp, Integer frontPort,
            HttpMethod method, String uri, Object param, Class<T> clazz) {
        log.debug(
                "start requestSpecificFront. groupId:{} frontIp:{} frontPort:{} "
                        + "httpMethod:{} uri:{}",
                groupId, frontIp, frontPort, method.toString(), uri);

        uri = FrontRestTools.uriAddGroupId(groupId, uri);
        RestTemplate restTemplate = frontRestTools.caseRestemplate(uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("requestSpecificFront. url:{}", url);

        try {
            HttpEntity entity = FrontRestTools.buildHttpEntity(param);// build entity
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("requestSpecificFront. ResourceAccessException:{}", e);
            throw new BaseException(ConstantCode.REQUEST_FRONT_FAIL);
        } catch (HttpStatusCodeException e) {
            JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
            throw new BaseException(error.getInteger("code"), error.getString("errorMessage"));
        }
    }

    /**
     * get from specific front.
     */
    private <T> T getFromSpecificFront(Integer groupId, String frontIp, Integer frontPort,
            String uri, Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, null, clazz);
    }

    /**
     * post to specific front.
     */
    public <T> T postToSpecificFront(Integer groupId, String frontIp, Integer frontPort, String uri,
            Object param, Class<T> clazz) {
        log.debug("start postToSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.POST, uri, param,
                clazz);
    }

    /**
     * delete to specific front.
     */
    public <T> T deleteToSpecificFront(Integer groupId, String frontIp, Integer frontPort,
            String uri, Object param, Class<T> clazz) {
        log.debug("start deleteToSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}",
                groupId, frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.DELETE, uri, param,
                clazz);
    }

    /**
     * get map's Cert Content from specific front.
     */
    public Map<String, String> getCertMapFromSpecificFront(String nodeIp, Integer frontPort) {
        int groupId = 1;
        return getFromSpecificFront(groupId, nodeIp, frontPort, URI_CERT, Map.class);
    }


    /**
     * get group list from specific front.
     */
    public List<String> getGroupListFromSpecificFront(String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_GROUP_PLIST, List.class);
    }


    /**
     * get groupPeers from specific front.
     */
    public List<String> getGroupPeersFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_GROUP_PEERS, List.class);
    }

    /**
     * get NodeIDList from specific front.
     */
    public List<String> getNodeIDListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_NODEID_LIST, List.class);
    }

    /**
     * get peers from specific front.
     */
    public PeerInfo[] getPeersFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_PEERS, PeerInfo[].class);
    }

    /**
     * get peers from specific front.
     */
    public SyncStatus getSyncStatusFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_CSYNC_STATUS,
                SyncStatus.class);
    }

    /**
     * get sealer list from specific front
     */
    public List<String> getSealerListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_SEALER_LIST,
                List.class);
    }

    /**
     * get front's encryptType
     */
    public Integer getEncryptTypeFromSpecificFront(String nodeIp, Integer frontPort) {
        log.debug("start getEncryptTypeFromSpecificFront. nodeIp:{},frontPort:{}", nodeIp,
                frontPort);
        Integer groupId = Integer.MAX_VALUE;
        int encryptType =
                getFromSpecificFront(groupId, nodeIp, frontPort, URI_ENCRYPT_TYPE, Integer.class);
        log.debug("end getEncryptTypeFromSpecificFront. encryptType:{}", encryptType);
        return encryptType;
    }

    /**
     * get observer list from specific front
     */
    public List<String> getObserverListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
    }

    public BigInteger getBlockNumberFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_BLOCK_NUMBER,
                BigInteger.class);
    }

    public Block getBlockByNumberFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId, BigInteger blockNmber) {
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNmber);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, Block.class);
    }

    public TransactionCount getTotalTransactionCountFromSpecificFront(String frontIp,
            Integer frontPort, Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_TRANS_TOTAL,
                TransactionCount.class);
    }

    public Transaction getTransactionByHashFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId, String transHash) {
        String uri = String.format(FrontRestTools.URI_TRANS_BY_HASH, transHash);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, Transaction.class);
    }

    public TransactionReceipt getTransactionReceiptFromSpecificFront(String frontIp,
            Integer frontPort, Integer groupId, String transHash) {
        String uri = String.format(FrontRestTools.URI_TRANS_RECEIPT, transHash);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, TransactionReceipt.class);
    }

    /**
     * get peers.
     */
    public PeerInfo[] getPeers(Integer chainId, Integer groupId) {
        return frontRestTools.getForEntity(chainId, groupId, URI_PEERS, PeerInfo[].class);
    }

    /**
     * get contract code.
     */
    public String getContractCode(Integer chainId, Integer groupId, String address,
            BigInteger blockNumber) throws BaseException {
        log.debug("start getContractCode groupId:{} address:{} blockNumber:{}", groupId, address,
                blockNumber);
        String uri = String.format(FrontRestTools.URI_CODE, address, blockNumber);
        String contractCode = frontRestTools.getForEntity(chainId, groupId, uri, String.class);
        log.debug("end getContractCode. contractCode:{}", contractCode);
        return contractCode;
    }

    /**
     * get group peers
     */
    public List<String> getGroupPeers(Integer chainId, Integer groupId) {
        log.debug("start getGroupPeers. groupId:{}", groupId);
        List<String> groupPeers =
                frontRestTools.getForEntity(chainId, groupId, URI_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JSON.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group peers
     */
    public List<String> getObserverList(Integer chainId, Integer groupId) {
        log.debug("start getObserverList. groupId:{}", groupId);
        List<String> observers =
                frontRestTools.getForEntity(chainId, groupId, URI_GET_OBSERVER_LIST, List.class);
        log.info("end getObserverList. observers:{}", JSON.toJSONString(observers));
        return observers;
    }

    /**
     * get consensusStatus
     */
    public String getConsensusStatus(Integer chainId, Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        String consensusStatus = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_CONSENSUS_STATUS, String.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }

    /**
     * get syncStatus
     */
    public SyncStatus getSyncStatus(Integer chainId, Integer groupId) {
        log.debug("start getSyncStatus. groupId:{}", groupId);
        SyncStatus ststus = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_CSYNC_STATUS, SyncStatus.class);
        log.debug("end getSyncStatus. ststus:{}", JSON.toJSONString(ststus));
        return ststus;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(Integer chainId, Integer groupId) {
        log.debug("start getLatestBlockNumber. groupId:{}", groupId);
        BigInteger latestBlockNmber = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_BLOCK_NUMBER, BigInteger.class);
        log.debug("end getLatestBlockNumber. latestBlockNmber:{}", latestBlockNmber);
        return latestBlockNmber;
    }

    public List<String> getSealerList(Integer chainId, Integer groupId) {
        log.debug("start getSealerList. groupId:{}", groupId);
        List getSealerList = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_GET_SEALER_LIST, List.class);
        log.debug("end getSealerList. getSealerList:{}", JSON.toJSONString(getSealerList));
        return getSealerList;
    }

    public GroupHandleResult generateGroup(String frontIp, Integer frontPort,
            GenerateGroupInfo param) {
        log.debug("start generateGroup groupId:{} frontIp:{} frontPort:{} param:{}",
                param.getGenerateGroupId(), frontIp, frontPort, JSON.toJSONString(param));
        Integer groupId = Integer.MAX_VALUE;
        GroupHandleResult groupHandleResult = requestSpecificFront(groupId, frontIp, frontPort,
                HttpMethod.POST, FrontRestTools.URI_GENERATE_GROUP, param, GroupHandleResult.class);

        log.debug("end generateGroup groupId:{} param:{}", param.getGenerateGroupId(),
                JSON.toJSONString(param));
        return groupHandleResult;
    }

    public GroupHandleResult operateGroup(String frontIp, Integer frontPort, Integer groupId,
            String type) {
        log.debug("start operateGroup frontIp:{} frontPort:{} groupId:{}", frontIp, frontPort,
                groupId);
        String uri = String.format(FrontRestTools.URI_OPERATE_GROUP, type);
        GroupHandleResult groupHandleResult =
                getFromSpecificFront(groupId, frontIp, frontPort, uri, GroupHandleResult.class);

        log.debug("end operateGroup");
        return groupHandleResult;
    }

    public Object getConsensusList(String frontIp, Integer frontPort, Integer groupId,
            Integer pageSize, Integer pageNumber) {
        log.debug("start getConsensusList. groupId:{}" + groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CONSENSUS_LIST, map);
        Object response = getFromSpecificFront(groupId, frontIp, frontPort, uri, Object.class);
        log.debug("end getConsensusList. response:{}", JSON.toJSONString(response));
        return response;
    }

    public Object setConsensusStatus(String frontIp, Integer frontPort,
            ConsensusParam consensusParam) {
        log.debug("start setConsensusStatus. consensusParam:{}", JSON.toJSONString(consensusParam));
        if (Objects.isNull(consensusParam)) {
            log.error("fail setConsensusStatus. request param is null");
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO);
        }
        ConsensusHandle consensusHandle = new ConsensusHandle();
        BeanUtils.copyProperties(consensusParam, consensusHandle);

        Object response = postToSpecificFront(consensusParam.getGroupId(), frontIp, frontPort,
                FrontRestTools.URI_CONSENSUS, consensusHandle, Object.class);
        log.debug("end setConsensusStatus. response:{}", JSON.toJSONString(response));
        return response;
    }

    public Object getSysConfigList(String frontIp, Integer frontPort, Integer groupId,
            Integer pageSize, Integer pageNumber) {
        log.debug("start getSysConfigListService. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_SYS_CONFIG_LIST, map);

        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, Object.class);
        log.debug("end getSysConfigListService. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object setSysConfigByKey(String frontIp, Integer frontPort,
            ReqSetSysConfig reqSetSysConfig) {
        log.debug("start setSysConfigByKey. reqSetSysConfig:{}",
                JSON.toJSONString(reqSetSysConfig));
        if (Objects.isNull(reqSetSysConfig)) {
            log.error("fail setSysConfigByKey. request param is null");
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO);
        }

        SysConfigParam sysConfigParam = new SysConfigParam();
        BeanUtils.copyProperties(reqSetSysConfig, sysConfigParam);

        Object frontRsp = postToSpecificFront(reqSetSysConfig.getGroupId(), frontIp, frontPort,
                FrontRestTools.URI_SYS_CONFIG, sysConfigParam, Object.class);
        log.debug("end setSysConfigByKey. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object getNetWorkData(String frontIp, Integer frontPort, Integer groupId,
            Integer pageSize, Integer pageNumber, LocalDateTime beginDate, LocalDateTime endDate) {
        log.debug("start getNetWorkData. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        if (beginDate != null) {
            map.put("beginDate", String.valueOf(beginDate));
        }
        if (endDate != null) {
            map.put("endDate", String.valueOf(endDate));
        }

        String uri =
                HttpRequestTools.getQueryUri(FrontRestTools.URI_CHARGING_GET_NETWORK_DATA, map);

        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, Object.class);
        log.debug("end getNetWorkData. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object getTxGasData(String frontIp, Integer frontPort, Integer groupId, Integer pageSize,
            Integer pageNumber, LocalDateTime beginDate, LocalDateTime endDate, String transHash) {
        log.debug("start getTxGasData. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));
        if (beginDate != null) {
            map.put("beginDate", String.valueOf(beginDate));
        }
        if (endDate != null) {
            map.put("endDate", String.valueOf(endDate));
        }
        if (transHash != null) {
            map.put("transHash", transHash);
        }

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CHARGING_GET_TXGASDATA, map);

        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, Object.class);
        log.debug("end getTxGasData. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object deleteLogData(String frontIp, Integer frontPort, Integer groupId, Integer type,
            LocalDateTime keepEndDate) {
        log.debug("start deleteLogData. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("type", String.valueOf(type));
        map.put("keepEndDate", String.valueOf(keepEndDate));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CHARGING_DELETE_DATA, map);

        Object frontRsp =
                deleteToSpecificFront(groupId, frontIp, frontPort, uri, null, Object.class);
        log.debug("end deleteLogData. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object getNodeMonitorInfo(String frontIp, Integer frontPort, Integer groupId,
            Map<String, String> map) {
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CHAIN, map);
        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, Object.class);
        return frontRsp;
    }

    public Object getPerformanceRatio(String frontIp, Integer frontPort, Map<String, String> map) {
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.FRONT_PERFORMANCE_RATIO, map);
        Object frontRsp =
                getFromSpecificFront(Integer.MAX_VALUE, frontIp, frontPort, uri, Object.class);
        return frontRsp;
    }

    public Object getPerformanceConfig(String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.FRONT_PERFORMANCE_CONFIG, Object.class);
    }
    
    public Object checkNodeProcess(String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_CHECK_NODE_PROCESS, Object.class);
    }
    
    public Object getGroupSizeInfos(String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_GET_GROUP_SIZE_INFOS, Object.class);
    }
}
