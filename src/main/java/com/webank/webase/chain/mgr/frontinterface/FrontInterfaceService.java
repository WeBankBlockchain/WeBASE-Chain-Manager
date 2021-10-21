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
package com.webank.webase.chain.mgr.frontinterface;

import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_CLIENT_VERSION;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_CSYNC_STATUS;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_ENCRYPT_TYPE;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GET_OBSERVER_LIST;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GROUP_PEERS;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_GROUP_PLIST;
import static com.webank.webase.chain.mgr.frontinterface.FrontRestTools.URI_PEERS;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.contract.entity.ReqContractCompileDto;
import com.webank.webase.chain.mgr.contract.entity.RspContractCompileDto;
import com.webank.webase.chain.mgr.front.entity.ClientVersionDTO;
import com.webank.webase.chain.mgr.front.entity.TransactionCount;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.group.entity.ReqSetSysConfig;
import com.webank.webase.chain.mgr.group.entity.SysConfigParam;
import com.webank.webase.chain.mgr.node.entity.ConsensusHandle;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import com.webank.webase.chain.mgr.util.HttpEntityUtils;
import com.webank.webase.chain.mgr.util.HttpRequestTools;
import com.webank.webase.chain.mgr.util.JsonTools;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock.Block;
import org.fisco.bcos.sdk.client.protocol.response.ConsensusStatus.ConsensusInfo;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
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
                                       HttpMethod method, String uri, HttpEntity entity, Class<T> clazz) {
        log.debug(
                "start requestSpecificFront. groupId:{} frontIp:{} frontPort:{} "
                        + "httpMethod:{} uri:{}",
                groupId, frontIp, frontPort, method.toString(), uri);

        uri = FrontRestTools.uriAddGroupId(groupId, uri);
        RestTemplate restTemplate = frontRestTools.caseRestemplate(uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("requestSpecificFront. url:{}", url);
        log.debug("requestSpecificFront. entity:{}", JsonTools.objToString(entity));

        try {
//            HttpEntity entity = FrontRestTools.buildHttpEntity(httpHeaders,param);// build entity
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            log.debug("url:{} response:{}", url, JsonTools.objToString(response));
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("requestSpecificFront. ResourceAccessException:{}", e);
            throw new BaseException(ConstantCode.REQUEST_FRONT_FAIL);
        } catch (HttpStatusCodeException e) {
            FrontRestTools.errorFormat(JsonTools.stringToJsonNode(e.getResponseBodyAsString()));
        }
        return null;
    }

    /**
     * get from specific front.
     */
    private <T> T getFromSpecificFront(Integer groupId, String frontIp, Integer frontPort,
                                       String uri, HttpEntity entity, Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, entity, clazz);
    }

    /**
     * post to specific front.
     */
    public <T> T postToSpecificFront(Integer groupId, String frontIp, Integer frontPort, String uri, HttpEntity entity, Class<T> clazz) {
        log.debug("start postToSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.POST, uri, entity,
                clazz);
    }

    /**
     * delete to specific front.
     */
    public <T> T deleteToSpecificFront(Integer groupId, String frontIp, Integer frontPort,
                                       String uri, HttpEntity entity, Class<T> clazz) {
        log.debug("start deleteToSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}",
                groupId, frontIp, frontPort.toString(), uri);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.DELETE, uri, entity,
                clazz);
    }


    /**
     * get group list from specific front.
     */
    public List<String> getGroupListFromSpecificFront(String peerName, String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_GROUP_PLIST, entity, List.class);
    }


    /**
     * get groupPeers from specific front.
     */
    public List<String> getGroupPeersFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_GROUP_PEERS, entity, List.class);
    }


    /**
     * get peers from specific front.
     */
    public PeerInfo[] getPeersFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_PEERS, entity, PeerInfo[].class);
    }

    /**
     * get peers from specific front.
     */
    public SyncStatus getSyncStatusFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, URI_CSYNC_STATUS, entity,
                SyncStatus.class);
    }

    /**
     * get sealer list from specific front
     */
    public List<String> getSealerListFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_SEALER_LIST, entity,
                List.class);
    }

    /**
     * get front's encryptType
     */
    public Integer getEncryptTypeFromSpecificFront(String peerName, String nodeIp, Integer frontPort) {
        log.debug("start getEncryptTypeFromSpecificFront. nodeIp:{},frontPort:{}", nodeIp,
                frontPort);
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        int encryptType =
                getFromSpecificFront(groupId, nodeIp, frontPort, URI_ENCRYPT_TYPE, entity, Integer.class);
        log.debug("end getEncryptTypeFromSpecificFront. encryptType:{}", encryptType);
        return encryptType;
    }

    /**
     * @param nodeIp
     * @param frontPort
     * @return
     */
    public ClientVersionDTO getClientVersionFromSpecificFront(String peerName, String nodeIp, Integer frontPort) {
        log.debug("start getClientVersionFromSpecificFront. nodeIp:{},frontPort:{}", nodeIp,
                frontPort);
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        ClientVersionDTO clientVersionDTO = getFromSpecificFront(groupId, nodeIp, frontPort, URI_CLIENT_VERSION, entity, ClientVersionDTO.class);
        log.debug("end getClientVersionFromSpecificFront. clientVersionDTO:{}", JsonTools.objToString(clientVersionDTO));
        return clientVersionDTO;
    }


    /**
     * get observer list from specific front
     */
    public List<String> getObserverListFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_OBSERVER_LIST, entity, List.class);
    }

    public BigInteger getBlockNumberFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_BLOCK_NUMBER, entity,
                BigInteger.class);
    }

    public Block getBlockByNumberFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId, BigInteger blockNmber) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNmber);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Block.class);
    }

    public TransactionCount getTotalTransactionCountFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_TRANS_TOTAL, entity,
                TransactionCount.class);
    }

    public JsonTransactionResponse getTransactionByHashFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId, String transHash) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        String uri = String.format(FrontRestTools.URI_TRANS_BY_HASH, transHash);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, JsonTransactionResponse.class);
    }

    public TransactionReceipt getTransactionReceiptFromSpecificFront(String peerName, String frontIp, Integer frontPort, Integer groupId, String transHash) {
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        String uri = String.format(FrontRestTools.URI_TRANS_RECEIPT, transHash);
        return getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, TransactionReceipt.class);
    }


    /**
     * get client version.
     */
    public ClientVersion getClientVersion(Integer chainId, Integer groupId) {
        log.debug("start getClientVersion. groupId:{}", groupId);
        ClientVersion clientVersionDTO = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_CLIENT_VERSION, ClientVersion.class);
        log.debug("end getClientVersion. clientVersionDTO:{}", JsonTools.objToString(clientVersionDTO));
        return clientVersionDTO;
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
        log.debug("end getGroupPeers. groupPeers:{}", JsonTools.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group peers
     */
    public List<String> getObserverList(Integer chainId, Integer groupId) {
        log.debug("start getObserverList. groupId:{}", groupId);
        List<String> observers =
                frontRestTools.getForEntity(chainId, groupId, URI_GET_OBSERVER_LIST, List.class);
        log.info("end getObserverList. observers:{}", JsonTools.toJSONString(observers));
        return observers;
    }

    /**
     * get consensusStatus
     */
    public ConsensusInfo getConsensusStatus(Integer chainId, Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        ConsensusInfo consensusStatus = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_CONSENSUS_STATUS, ConsensusInfo.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }


    public List<String> getNodeIdList(Integer chainId, Integer groupId) {
        log.debug("start getNodeIdList. groupId:{}", groupId);
        List<String> nodeIdList = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_NODEID_LIST, List.class);
        log.debug("end getNodeIdList. nodeIdList:{}", JsonTools.toJSONString(nodeIdList));
        return nodeIdList;
    }

    /**
     * get syncStatus
     */
    public SyncStatus getSyncStatus(Integer chainId, Integer groupId) {
        log.debug("start getSyncStatus. groupId:{}", groupId);
        SyncStatus ststus = frontRestTools.getForEntity(chainId, groupId,
                FrontRestTools.URI_CSYNC_STATUS, SyncStatus.class);
        log.debug("end getSyncStatus. ststus:{}", JsonTools.toJSONString(ststus));
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
        log.debug("end getSealerList. getSealerList:{}", JsonTools.toJSONString(getSealerList));
        return getSealerList;
    }

    public Object generateGroup(String peerName, String frontIp, Integer frontPort, GenerateGroupInfo param) {
        log.debug("start generateGroup groupId:{} frontIp:{} frontPort:{} param:{}",
                param.getGenerateGroupId(), frontIp, frontPort, JsonTools.toJSONString(param));
        Integer groupId = Integer.MAX_VALUE;
        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(peerName);
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, param);
        Object groupHandleResult = postToSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_GENERATE_GROUP, httpEntity, Object.class);


        log.debug("end generateGroup groupId:{} param:{}", param.getGenerateGroupId(),
                JsonTools.toJSONString(param));
        return groupHandleResult;
    }

    public Object operateGroup(String peerName, String frontIp, Integer frontPort, Integer groupId, String type) {
        log.debug("start operateGroup frontIp:{} frontPort:{} groupId:{}", frontIp, frontPort,
                groupId);
        String uri = String.format(FrontRestTools.URI_OPERATE_GROUP, type);
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object groupHandleResult =
                getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);

        log.debug("end operateGroup");
        return groupHandleResult;
    }

    /**
     * @param peerName
     * @param frontIp
     * @param frontPort
     * @param groupList
     * @return
     */
    public Map<Integer, String> queryGroupStatus(String peerName, String frontIp, Integer frontPort, List<Integer> groupList) {
        log.debug("start queryGroupStatus peerName:{} frontIp:{} frontPort:{} groupList:{}", peerName, frontPort, frontIp, JsonTools.objToString(groupList));

        //param
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("groupIdList", groupList);

        //http entity
        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(peerName);
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, map);

        //rest request
        Integer groupId = Integer.MAX_VALUE;
        BaseResponse baseResponse = postToSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_GROUP_STATUS, httpEntity, BaseResponse.class);
        if (ConstantProperties.HTTP_SUCCESS_RESPONSE_CODE != baseResponse.getCode())
            throw new BaseException(baseResponse.getCode(), baseResponse.getMessage());

        //response data
        Map<Integer, String> restResultMap = JsonTools.stringToObj(JsonTools.objToString(baseResponse.getData()), new TypeReference<Map<Integer, String>>() {
        });

        log.debug("end operateGroup restResultMap:{}", JsonTools.objToString(restResultMap));
        return restResultMap;
    }


    public Object getConsensusList(String peerName, String frontIp, Integer frontPort, Integer groupId,
                                   Integer pageSize, Integer pageNumber) {
        log.debug("start getConsensusList. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CONSENSUS_LIST, map);
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object response = getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);
        log.debug("end getConsensusList. response:{}", JsonTools.toJSONString(response));
        return response;
    }

    public Object setConsensusStatus(String peerName, String frontIp, Integer frontPort,
                                     ConsensusParam consensusParam) {
        log.debug("start setConsensusStatus. consensusParam:{}", JsonTools.toJSONString(consensusParam));
        if (Objects.isNull(consensusParam)) {
            log.error("fail setConsensusStatus. request param is null");
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO);
        }
        ConsensusHandle consensusHandle = new ConsensusHandle();
        BeanUtils.copyProperties(consensusParam, consensusHandle);


        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(peerName);
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, consensusHandle);
        Object response = postToSpecificFront(consensusParam.getGroupId(), frontIp, frontPort,
                FrontRestTools.URI_CONSENSUS, httpEntity, Object.class);
        log.debug("end setConsensusStatus. response:{}", JsonTools.toJSONString(response));
        return response;
    }

    public Object getSysConfigList(String peerName, String frontIp, Integer frontPort, Integer groupId,
                                   Integer pageSize, Integer pageNumber) {
        log.debug("start getSysConfigListService. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("pageSize", String.valueOf(pageSize));
        map.put("pageNumber", String.valueOf(pageNumber));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_SYS_CONFIG_LIST, map);

        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);
        log.debug("end getSysConfigListService. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object setSysConfigByKey(String peerName, String frontIp, Integer frontPort,
                                    ReqSetSysConfig reqSetSysConfig) {
        log.debug("start setSysConfigByKey. reqSetSysConfig:{}",
                JsonTools.toJSONString(reqSetSysConfig));
        if (Objects.isNull(reqSetSysConfig)) {
            log.error("fail setSysConfigByKey. request param is null");
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO);
        }

        SysConfigParam sysConfigParam = new SysConfigParam();
        BeanUtils.copyProperties(reqSetSysConfig, sysConfigParam);

        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(peerName);
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, sysConfigParam);
        Object frontRsp = postToSpecificFront(reqSetSysConfig.getGroupId(), frontIp, frontPort,
                FrontRestTools.URI_SYS_CONFIG, httpEntity, Object.class);
        log.debug("end setSysConfigByKey. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object getNetWorkData(String peerName, String frontIp, Integer frontPort, Integer groupId,
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

        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);
        log.debug("end getNetWorkData. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object getTxGasData(String peerName, String frontIp, Integer frontPort, Integer groupId, Integer pageSize,
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

        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);
        log.debug("end getTxGasData. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    public Object deleteLogData(String peerName, String frontIp, Integer frontPort, Integer groupId, Integer type,
                                LocalDateTime keepEndDate) {
        log.debug("start deleteLogData. groupId:{}", groupId);
        Map<String, String> map = new HashMap<>();
        map.put("groupId", String.valueOf(groupId));
        map.put("type", String.valueOf(type));
        map.put("keepEndDate", String.valueOf(keepEndDate));

        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CHARGING_DELETE_DATA, map);

        Object frontRsp =
                deleteToSpecificFront(groupId, frontIp, frontPort, uri, null, Object.class);
        log.debug("end deleteLogData. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }


    public TransactionReceipt sendSignedTransaction(Integer chainId, Integer groupId,
                                                    String signMsg, Boolean sync) {
        Instant startTime = Instant.now();
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signedStr", signMsg);
        params.put("sync", sync);
        TransactionReceipt receipt = frontRestTools.postForEntity(chainId, groupId,
                FrontRestTools.URI_SIGNED_TRANSACTION, params, TransactionReceipt.class);
        log.info("sendSignedTransaction to front useTime: {}",
                Duration.between(startTime, Instant.now()).toMillis());
        return receipt;
    }

    public Object sendQueryTransaction(Integer chainId, Integer groupId, Object params) {
        Object result = frontRestTools.postForEntity(chainId, groupId,
                FrontRestTools.URI_QUERY_TRANSACTION, params, Object.class);
        return result;
    }

    /**
     * @param chainId
     * @param groupId
     * @param contractName
     * @param contractSource
     * @return
     */
    public RspContractCompileDto compileSingleContractFile(Integer chainId, Integer groupId, String contractName, String contractSource) {
        ReqContractCompileDto param = new ReqContractCompileDto(contractName, contractSource);
        return frontRestTools.postForEntity(chainId, groupId, FrontRestTools.URI_CONTRACT_COMPILE, param, RspContractCompileDto.class);
    }


    public Object getNodeMonitorInfo(String peerName, String frontIp, Integer frontPort, Integer groupId,
                                     Map<String, String> map) {
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.URI_CHAIN, map);
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object frontRsp = getFromSpecificFront(groupId, frontIp, frontPort, uri, entity, Object.class);
        return frontRsp;
    }

    public Object getPerformanceRatio(String peerName, String frontIp, Integer frontPort, Map<String, String> map) {
        String uri = HttpRequestTools.getQueryUri(FrontRestTools.FRONT_PERFORMANCE_RATIO, map);
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        Object frontRsp =
                getFromSpecificFront(Integer.MAX_VALUE, frontIp, frontPort, uri, entity, Object.class);
        return frontRsp;
    }

    public Object getPerformanceConfig(String peerName, String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.FRONT_PERFORMANCE_CONFIG, entity, Object.class);
    }

    public Object checkNodeProcess(String peerName, String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_CHECK_NODE_PROCESS, entity, Object.class);
    }

    public Object getGroupSizeInfos(String peerName, String frontIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        HttpEntity entity = HttpEntityUtils.buildHttpEntityByHost(peerName);
        return getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_GET_GROUP_SIZE_INFOS, entity, Object.class);
    }


    public JsonTransactionResponse getTransaction(Integer chainId, Integer groupId, String transHash)
        throws BaseException {
        if (StringUtils.isBlank(transHash)) {
            return null;
        }
        String uri = String.format(FrontRestTools.URI_TRANS_BY_HASH, transHash);
        JsonTransactionResponse transInfo =
            frontRestTools.getForEntity(chainId, groupId, uri, JsonTransactionResponse.class);
        return transInfo;
    }

    public Block getBlockByNumber(Integer chainId, Integer groupId, BigInteger blockNumber)
        throws BaseException {
        String uri = String.format(FrontRestTools.URI_BLOCK_BY_NUMBER, blockNumber);
        Block block = null;
        try {
            block = frontRestTools.getForEntity(chainId, groupId, uri, Block.class);
        } catch (Exception ex) {
            log.info("fail getBlockByNumber,exception:{}", ex);
        }
        return block;
    }

    public TransactionReceipt getTransReceipt(Integer chainId, Integer groupId, String transHash)
        throws BaseException {
        String uri = String.format(FrontRestTools.URI_TRANS_RECEIPT, transHash);
        TransactionReceipt transReceipt =
            frontRestTools.getForEntity(chainId, groupId, uri, TransactionReceipt.class);
        return transReceipt;
    }
}
