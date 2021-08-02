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

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroup;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.frontinterface.entity.FailInfo;
import com.webank.webase.chain.mgr.util.HttpEntityUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * about http request for WeBASE-Front.
 */
@Log4j2
@Service
public class FrontRestTools {

    // public static final String FRONT_URL = "http://%1s:%2d/WeBASE-Front/%3s";
    public static final String URI_BLOCK_NUMBER = "web3/blockNumber";
    public static final String URI_BLOCK_BY_NUMBER = "web3/blockByNumber/%1d";
    public static final String URI_BLOCK_BY_HASH = "web3/blockByHash/%1s";
    public static final String URI_TRANS_TOTAL = "web3/transaction-total";
    public static final String URI_TRANS_BY_HASH = "web3/transaction/%1s";
    public static final String URI_TRANS_RECEIPT = "web3/transactionReceipt/%1s";
    public static final String URI_SIGNED_TRANSACTION = "trans/signed-transaction";
    public static final String URI_QUERY_TRANSACTION = "trans/query-transaction";
    public static final String URI_CODE = "web3/code/%1s/%2s";
    public static final String URI_GROUP_PLIST = "web3/groupList";
    public static final String URI_NODEID_LIST = "web3/nodeIdList";
    public static final String URI_GET_SEALER_LIST = "web3/sealerList";
    public static final String URI_GET_OBSERVER_LIST = "web3/observerList";
    public static final String URI_GROUP_PEERS = "web3/groupPeers";
    public static final String URI_PEERS = "web3/peers";
    public static final String URI_CONSENSUS_STATUS = "web3/consensusStatus";
    public static final String URI_CSYNC_STATUS = "web3/syncStatus";
    public static final String URI_SYSTEMCONFIG_BY_KEY = "web3/systemConfigByKey/%1s";
    public static final String URI_GENERATE_GROUP = "web3/generateGroup";
    public static final String URI_OPERATE_GROUP = "web3/operateGroup/%1s";
    public static final String URI_CLIENT_VERSION = "web3/clientVersion";
    public static final String URI_GET_GROUP_STATUS = "web3/queryGroupStatus";
    public static final String FRONT_PERFORMANCE_RATIO = "performance";
    public static final String FRONT_PERFORMANCE_CONFIG = "performance/config";
    public static final String URI_MULTI_CONTRACT_COMPILE = "contract/multiContractCompile";
    public static final String URI_CONTRACT_COMPILE = "contract/contractCompile";
    public static final String URI_CONTRACT_DEPLOY = "contract/deployWithSign";
    public static final String URI_SEND_TRANSACTION = "trans/handleWithSign";
    public static final String URI_CHAIN = "chain";
    public static final String URI_CHECK_NODE_PROCESS = "chain/checkNodeProcess";
    public static final String URI_GET_GROUP_SIZE_INFOS = "chain/getGroupSizeInfos";
    public static final String URI_CHARGING_GET_NETWORK_DATA = "charging/getNetWorkData";
    public static final String URI_CHARGING_GET_TXGASDATA = "charging/getTxGasData";
    public static final String URI_CHARGING_DELETE_DATA = "charging/deleteData";


    public static final String URI_SYS_CONFIG_LIST = "sys/config/list";
    public static final String URI_SYS_CONFIG = "sys/config";
    public static final String URI_CONSENSUS_LIST = "precompiled/consensus/list";
    public static final String URI_CONSENSUS = "precompiled/consensus";
    public static final String URI_CONTRACT_STATUS_MANAGE = "precompiled/contractStatusManage";

    public static final String URI_CERT = "cert";
    public static final String URI_ENCRYPT_TYPE = "encrypt";

    // 不需要在url中包含groupId的
    private static final List<String> URI_NOT_CONTAIN_GROUP_ID =
            Arrays.asList(URI_MULTI_CONTRACT_COMPILE, URI_CONTRACT_DEPLOY, URI_SEND_TRANSACTION,
                    URI_SYS_CONFIG_LIST, URI_SYS_CONFIG, URI_CONSENSUS_LIST, URI_CONSENSUS,
                    URI_CONTRACT_STATUS_MANAGE, URI_CERT, URI_ENCRYPT_TYPE,
                    URI_CHARGING_GET_NETWORK_DATA, URI_CHARGING_GET_TXGASDATA,
                    URI_CHARGING_DELETE_DATA, URI_CHAIN, FRONT_PERFORMANCE_RATIO,
                    FRONT_PERFORMANCE_CONFIG, URI_CHECK_NODE_PROCESS, URI_GET_GROUP_SIZE_INFOS,
                    URI_SIGNED_TRANSACTION, URI_QUERY_TRANSACTION, URI_CONTRACT_COMPILE);


    @Qualifier(value = "genericRestTemplate")
    @Autowired
    private RestTemplate genericRestTemplate;
    @Qualifier(value = "deployRestTemplate")
    @Autowired
    private RestTemplate deployRestTemplate;
    @Autowired
    private ConstantProperties cproperties;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;

    private static Map<String, FailInfo> failRequestMap = new HashMap<>();


    /**
     * append groupId to uri.
     */
    public static String uriAddGroupId(Integer groupId, String uri) {
        if (groupId == null || StringUtils.isBlank(uri)) {
            return null;
        }

        final String tempUri = uri.contains("?") ? uri.substring(0, uri.indexOf("?")) : uri;

        long count = URI_NOT_CONTAIN_GROUP_ID.stream().filter(u -> u.contains(tempUri)).count();
        if (count > 0) {
            return uri;
        }
        return groupId + "/" + uri;
    }

    /**
     * check url status.
     */
    private boolean isServiceSleep(String url, String methType) {
        // get failInfo
        String key = buildKey(url, methType);
        FailInfo failInfo = failRequestMap.get(key);

        // cehck server status
        if (failInfo == null) {
            return false;
        }
        int failCount = failInfo.getFailCount();
        Long subTime = Duration.between(failInfo.getLatestTime(), Instant.now()).toMillis();
        if (failCount > cproperties.getMaxRequestFail()
                && subTime < cproperties.getSleepWhenHttpMaxFail()) {
            return true;
        } else if (subTime > cproperties.getSleepWhenHttpMaxFail()) {
            // service is sleep
            deleteKeyOfMap(failRequestMap, key);
        }
        return false;

    }

    /**
     * set request fail times.
     */
    private void setFailCount(String url, String methodType) {
        // get failInfo
        String key = buildKey(url, methodType);
        FailInfo failInfo = failRequestMap.get(key);
        if (failInfo == null) {
            failInfo = new FailInfo();
            failInfo.setFailUrl(url);
        }

        // reset failInfo
        failInfo.setLatestTime(Instant.now());
        failInfo.setFailCount(failInfo.getFailCount() + 1);
        failRequestMap.put(key, failInfo);
        log.info("the latest failInfo:{}", JsonTools.toJSONString(failRequestMap));
    }


    /**
     * build key description: frontIp$frontPort example: 2651654951545$8081
     */
    private String buildKey(String url, String methodType) {
        return url.hashCode() + "$" + methodType;
    }


    /**
     * delete key of map
     */
    private static void deleteKeyOfMap(Map<String, FailInfo> map, String rkey) {
        log.info("start deleteKeyOfMap. rkey:{} map:{}", rkey, JsonTools.toJSONString(map));
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            if (rkey.equals(key)) {
                iter.remove();
            }
        }
        log.info("end deleteKeyOfMap. rkey:{} map:{}", rkey, JsonTools.toJSONString(map));
    }


    /**
     * build url of front service.
     */
    private Pair<String, FrontGroup> buildFrontUrl(ArrayList<FrontGroup> list, String uri, HttpMethod httpMethod) {
        Collections.shuffle(list);// random one
        log.debug("====================map list:{}", JsonTools.toJSONString(list));
        Iterator<FrontGroup> iterator = list.iterator();
        while (iterator.hasNext()) {
            FrontGroup frontGroup = iterator.next();
            log.info("============frontGroup:{} uri:{}", JsonTools.toJSONString(frontGroup), uri);

            uri = uriAddGroupId(frontGroup.getGroupId(), uri);// append groupId to uri
            String url = String.format(cproperties.getFrontUrl(), frontGroup.getFrontIp(),
                    frontGroup.getFrontPort(), uri).replaceAll(" ", "");
            iterator.remove();

            if (isServiceSleep(url, httpMethod.toString())) {
                log.warn("front url[{}] is sleep,jump over", url);
                continue;
            }

            return Pair.of(url, frontGroup);
        }
        log.info("end buildFrontUrl. url is null");
        return null;
    }


    /**
     * case restTemplate by uri.
     */
    public RestTemplate caseRestemplate(String uri) {
        if (StringUtils.isBlank(uri)) {
            return null;
        }
        if (uri.contains(URI_CONTRACT_DEPLOY) || uri.contains(URI_MULTI_CONTRACT_COMPILE)
                || uri.contains(URI_CHARGING_GET_TXGASDATA) || uri.contains(URI_SIGNED_TRANSACTION)) {
            return deployRestTemplate;
        }
        return genericRestTemplate;
    }


    /**
     * get from front for entity.
     */
    public <T> T getForEntity(Integer chainId, Integer groupId, String uri, Class<T> clazz) {
        return restTemplateExchange(chainId, groupId, uri, HttpMethod.GET, null, clazz);
    }

    /**
     * post from front for entity.
     */
    public <T> T postForEntity(Integer chainId, Integer groupId, String uri, Object params,
                               Class<T> clazz) {
        return restTemplateExchange(chainId, groupId, uri, HttpMethod.POST, params, clazz);
    }

    /**
     * delete from front for entity.
     */
    public <T> T deleteForEntity(Integer chainId, Integer groupId, String uri, Object params,
                                 Class<T> clazz) {
        return restTemplateExchange(chainId, groupId, uri, HttpMethod.DELETE, params, clazz);
    }

    /**
     * restTemplate exchange.
     */
    private <T> T restTemplateExchange(Integer chainId, Integer groupId, String uri,
                                       HttpMethod method, Object param, Class<T> clazz) {
        List<FrontGroup> frontList = frontGroupMapCache.getMapListByChainId(chainId, groupId);
        if (frontList == null || frontList.size() == 0) {
            log.error("fail restTemplateExchange. frontList is empty");
            throw new BaseException(ConstantCode.FRONT_LIST_NOT_FOUNT);
        }
        ArrayList<FrontGroup> list = new ArrayList<>(frontList);
        RestTemplate restTemplate = caseRestemplate(uri);

        while (list != null && list.size() > 0) {
            Pair<String, FrontGroup> pair = buildFrontUrl(list, uri, method);// build url
            String url = Optional.ofNullable(pair).map(p -> p.getLeft()).orElse(null);

            try {
                if (Objects.isNull(restTemplate) || Objects.isNull(url)) {
                    log.error("fail restTemplateExchange, rest or url is null. groupId:{} url:{}", chainId, url);
                    throw new BaseException(ConstantCode.SYSTEM_EXCEPTION);
                }

                FrontGroup frontGroup = pair.getRight();
                HttpHeaders headers = HttpEntityUtils.instantiateHttpHeaders();
                if (Objects.nonNull(frontGroup) && StringUtils.isNotBlank(frontGroup.getFrontPeerName())) {
                    headers.set(HttpHeaders.HOST, frontGroup.getFrontPeerName());
                }
                HttpEntity entity = HttpEntityUtils.buildHttpEntity(headers, param);// build entity
                log.debug("restful request. url:{}", url);
                log.debug("restful request. entity:{}", JsonTools.objToString(entity));
                ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
                log.debug("response:{}", JsonTools.objToString(response));
                return response.getBody();
            } catch (ResourceAccessException ex) {
                log.warn("fail restTemplateExchange", ex);
                setFailCount(url, method.toString());
                if (isServiceSleep(url, method.toString())) {
                    throw ex;
                }
                log.info("continue next front", ex);
                continue;
            } catch (HttpStatusCodeException e) {
                errorFormat(JsonTools.stringToJsonNode(e.getResponseBodyAsString()));
            }
        }
        return null;
    }

    /**
     * front error format
     *
     * @param error
     */
    public static void errorFormat(JsonNode error) {
        log.error("http request fail. error:{}", JsonTools.toJSONString(error));
        String errorMessage = error.get("errorMessage").asText();
        if (StringUtils.isBlank(errorMessage)) {
            throw new BaseException(ConstantCode.REQUEST_FRONT_FAIL);
        }
        if (errorMessage.contains("code")) {
            JsonNode errorInside = JsonTools.stringToJsonNode(errorMessage).get("error");
            throw new BaseException(ConstantCode.REQUEST_NODE_EXCEPTION.getCode(),
                    errorInside.get("message").asText());
        }
        throw new BaseException(error.get("code").asInt(), errorMessage);
    }
}
