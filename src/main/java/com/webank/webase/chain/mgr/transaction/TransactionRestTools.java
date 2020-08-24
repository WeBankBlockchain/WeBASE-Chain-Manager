/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.transaction;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;

import lombok.extern.log4j.Log4j2;

/**
 * about http request for WeBASE-Transaction.
 */
@Log4j2
@Service
public class TransactionRestTools {

    public static final String TRANSACTION_BASE_URL = "http://%s/WeBASE-Transaction";

    public static final String URI_USER_LIST = "%s/user/list/%s/%s/%s";
    public static final String URI_CONTRACT_DEPLOY = "%s/contract/deploy";

    @Autowired private ConstantProperties constantProperties;
    @Autowired private RestTemplate restTemplate;

    /**
     *
     * @param chainId
     * @return
     */
    public String getBaseUrl(int chainId){
        String transactionServer = constantProperties.getTransactionMap().get(chainId);
        if (StringUtils.isBlank(transactionServer)) {
            return null;
        }
        return String.format(TRANSACTION_BASE_URL,transactionServer );
    }


    /**
     * get from WeBASE-Transaction.
     */
    public <T> T get(String url, Class<T> clazz) throws BaseException {
        return request(HttpMethod.GET, url, null, clazz);
    }

    /**
     * post to WeBASE-Transaction.
     */
    public <T> T post(String url, Object param, Class<T> clazz) throws BaseException {
        return request(HttpMethod.POST, url, param, clazz);
    }

    /**
     * delete to WeBASE-Transaction.
     */
    public <T> T delete(String url, Object param, Class<T> clazz) throws BaseException {
        return request(HttpMethod.DELETE, url, param, clazz);
    }

    /**
     * request from trasaction.
     */
    private <T> T request(HttpMethod method, String url, Object param, Class<T> clazz)
            throws BaseException {
        try {
            HttpEntity<?> entity = buildHttpEntity(param);// build entity
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("request transaction. ResourceAccessException:", e);
            throw new BaseException(ConstantCode.REQUEST_TRANSACTION_EXCEPTION);
        } catch (HttpStatusCodeException e) {
            errorFormat(e.getResponseBodyAsString());
        }
        throw new BaseException(ConstantCode.REQUEST_TRANSACTION_EXCEPTION);
    }

    /**
     * build httpEntity.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static HttpEntity buildHttpEntity(Object param) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JsonTools.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, headers);
        return requestEntity;
    }

    /**
     * front error format
     * 
     * @param str
     * @throws BaseException
     */
    private static void errorFormat(String str) throws BaseException {
        JsonNode error = JsonTools.stringToJsonNode(str);
        log.error("request transaction fail. error:{}", error);
        if (ObjectUtils.isEmpty(error.get("errorMessage"))) {
            throw new BaseException(ConstantCode.REQUEST_TRANSACTION_EXCEPTION);
        }
        String errorMessage = error.get("errorMessage").asText();
        throw new BaseException(ConstantCode.REQUEST_TRANSACTION_EXCEPTION.getCode(), errorMessage);
    }
}
