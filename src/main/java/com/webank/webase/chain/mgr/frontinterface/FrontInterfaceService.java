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
import com.webank.webase.chain.mgr.base.exception.NodeMgrException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.frontinterface.entity.GenerateGroupInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.GroupHandleResult;
import com.webank.webase.chain.mgr.frontinterface.entity.PostAbiInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.SyncStatus;
import com.webank.webase.chain.mgr.node.entity.PeerInfo;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


@Log4j2
@Service
public class FrontInterfaceService {

    @Autowired
    private FrontRestTools frontRestTools;
    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Autowired
    private ConstantProperties cproperties;

    /**
     * request from specific front.
     */
    private <T> T requestSpecificFront(int groupId, String frontIp, Integer frontPort,
            HttpMethod method, String uri, Object param, Class<T> clazz) {
        log.debug(
                "start requestSpecificFront. groupId:{} frontIp:{} frontPort:{} "
                        + "httpMethod:{} uri:{}",
                groupId, frontIp, frontPort, method.toString(), uri);

        uri = FrontRestTools.uriAddGroupId(groupId, uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("requestSpecificFront. url:{}", url);

        try {
            HttpEntity entity = FrontRestTools.buildHttpEntity(param);// build entity
            ResponseEntity<T> response = genericRestTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            JSONObject error = JSONObject.parseObject(e.getResponseBodyAsString());
            throw new NodeMgrException(error.getInteger("code"), error.getString("errorMessage"));
        }
    }


    /**
     * get from specific front.
     */
    private <T> T getFromSpecificFront(int groupId, String frontIp, Integer frontPort, String uri,
            Class<T> clazz) {
        log.debug("start getFromSpecificFront. groupId:{} frontIp:{} frontPort:{}  uri:{}", groupId,
                frontIp, frontPort.toString(), uri);
        String url = String.format(cproperties.getFrontUrl(), frontIp, frontPort, uri);
        log.debug("getFromSpecificFront. url:{}", url);
        return requestSpecificFront(groupId, frontIp, frontPort, HttpMethod.GET, uri, null, clazz);
    }

    /**
     * send contract abi
     */
    public void sendAbi(int groupId, PostAbiInfo param) {
        log.debug("start sendAbi groupId:{} param:{}", groupId, JSON.toJSONString(param));

        frontRestTools.postForEntity(groupId, FrontRestTools.URI_CONTRACT_SENDABI, param,
                Object.class);
        log.debug("end sendAbi groupId:{} param:{}", groupId, JSON.toJSONString(param));

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
    public List<String> getGroupListFromSpecificFront(String nodeIp, Integer frontPort) {
        Integer groupId = Integer.MAX_VALUE;
        return getFromSpecificFront(groupId, nodeIp, frontPort, URI_GROUP_PLIST, List.class);
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
     * get peers.
     */
    public PeerInfo[] getPeers(Integer groupId) {
        return frontRestTools.getForEntity(groupId, URI_PEERS, PeerInfo[].class);
    }

    /**
     * get contract code.
     */
    public String getContractCode(Integer groupId, String address, BigInteger blockNumber)
            throws NodeMgrException {
        log.debug("start getContractCode groupId:{} address:{} blockNumber:{}", groupId, address,
                blockNumber);
        String uri = String.format(FrontRestTools.URI_CODE, address, blockNumber);
        String contractCode = frontRestTools.getForEntity(groupId, uri, String.class);
        log.debug("end getContractCode. contractCode:{}", contractCode);
        return contractCode;
    }

    /**
     * get group peers
     */
    public List<String> getGroupPeers(Integer groupId) {
        log.debug("start getGroupPeers. groupId:{}", groupId);
        List<String> groupPeers = frontRestTools.getForEntity(groupId, URI_GROUP_PEERS, List.class);
        log.debug("end getGroupPeers. groupPeers:{}", JSON.toJSONString(groupPeers));
        return groupPeers;
    }

    /**
     * get group peers
     */
    public List<String> getObserverList(Integer groupId) {
        log.debug("start getObserverList. groupId:{}", groupId);
        List<String> observers =
                frontRestTools.getForEntity(groupId, URI_GET_OBSERVER_LIST, List.class);
        log.info("end getObserverList. observers:{}", JSON.toJSONString(observers));
        return observers;
    }

    /**
     * get observer list from specific front
     */
    public List<String> getObserverListFromSpecificFront(String frontIp, Integer frontPort,
            Integer groupId) {
        return getFromSpecificFront(groupId, frontIp, frontPort,
                FrontRestTools.URI_GET_OBSERVER_LIST, List.class);
    }

    /**
     * get consensusStatus
     */
    public String getConsensusStatus(Integer groupId) {
        log.debug("start getConsensusStatus. groupId:{}", groupId);
        String consensusStatus = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_CONSENSUS_STATUS, String.class);
        log.debug("end getConsensusStatus. consensusStatus:{}", consensusStatus);
        return consensusStatus;
    }

    /**
     * get syncStatus
     */
    public SyncStatus getSyncStatus(Integer groupId) {
        log.debug("start getSyncStatus. groupId:{}", groupId);
        SyncStatus ststus = frontRestTools.getForEntity(groupId, FrontRestTools.URI_CSYNC_STATUS,
                SyncStatus.class);
        log.debug("end getSyncStatus. ststus:{}", JSON.toJSONString(ststus));
        return ststus;
    }

    /**
     * get latest block number
     */
    public BigInteger getLatestBlockNumber(Integer groupId) {
        log.debug("start getLatestBlockNumber. groupId:{}", groupId);
        BigInteger latestBlockNmber = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_BLOCK_NUMBER, BigInteger.class);
        log.debug("end getLatestBlockNumber. latestBlockNmber:{}", latestBlockNmber);
        return latestBlockNmber;
    }

    /**
     * get sealerList.
     */
    public List<String> getSealerList(Integer groupId) {
        log.debug("start getSealerList. groupId:{}", groupId);
        List getSealerList = frontRestTools.getForEntity(groupId,
                FrontRestTools.URI_GET_SEALER_LIST, List.class);
        log.debug("end getSealerList. getSealerList:{}", JSON.toJSONString(getSealerList));
        return getSealerList;
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
     * generate group.
     */
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

    /**
     * start group.
     */
    public GroupHandleResult startGroup(String frontIp, Integer frontPort, Integer startGroupId) {
        log.debug("start startGroup frontIp:{} frontPort:{} startGroupId:{}", frontIp, frontPort,
                startGroupId);
        Integer groupId = Integer.MAX_VALUE;
        String uri = String.format(FrontRestTools.URI_START_GROUP, startGroupId);
        GroupHandleResult groupHandleResult =
                getFromSpecificFront(groupId, frontIp, frontPort, uri, GroupHandleResult.class);

        log.debug("end startGroup");
        return groupHandleResult;
    }

    /**
     * refresh front.
     */
    public void refreshFront(String frontIp, Integer frontPort) {
        log.debug("start refreshFront groupId:{} frontIp:{} frontPort:{} ", frontIp, frontPort);
        Integer groupId = Integer.MAX_VALUE;
        getFromSpecificFront(groupId, frontIp, frontPort, FrontRestTools.URI_REFRESH_FRONT, Object.class);
        log.debug("end refreshFront");
    }
}
