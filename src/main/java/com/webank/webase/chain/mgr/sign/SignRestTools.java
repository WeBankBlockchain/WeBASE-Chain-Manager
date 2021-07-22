/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.sign;

import com.fasterxml.jackson.databind.JsonNode;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.util.JsonTools;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

/**
 * about http request for WeBASE-Transaction.
 */
@Log4j2
@Service
public class SignRestTools {

    public static final String SIGN_BASE_URL = "http://%s/WeBASE-Sign";

    public static final String URI_USER_LIST = "%s/user/list/%s/%s/%s?signUserIdList=%s";
    public static final String URI_USER_NEW = "%s/user/newUser";
    public static final String URI_USER_INFO = "%s/user/%s/userInfo";
    public static final String URI_SIGN = "%s/sign";


    @Autowired
    private ConstantProperties constantProperties;
    @Autowired
    private RestTemplate restTemplate;

    /**
     *
     * @return
     */
    public String getBaseUrl() {
        String webaseSignAddress = constantProperties.getWebaseSignAddress();
        if (StringUtils.isBlank(webaseSignAddress)) {
            return null;
        }
        return String.format(SIGN_BASE_URL, webaseSignAddress);
    }

    /**
     * get from sign.
     */
    public <T> T getFromSign(String url, Class<T> clazz) throws BaseException {
        return requestSign(HttpMethod.GET, url, null, clazz);
    }

    /**
     * post to sign.
     */
    public <T> T postToSign(String url, Object param, Class<T> clazz) throws BaseException {
        return requestSign(HttpMethod.POST, url, param, clazz);
    }

    /**
     * delete to sign.
     */
    public <T> T deleteToSign(String url, Object param, Class<T> clazz) throws BaseException {
        return requestSign(HttpMethod.DELETE, url, param, clazz);
    }

    /**
     * request from sign.
     */
    private <T> T requestSign(HttpMethod method, String url, Object param, Class<T> clazz)
            throws BaseException {
        try {
            HttpEntity<?> entity = buildHttpEntity(param);// build entity
            ResponseEntity<T> response = restTemplate.exchange(url, method, entity, clazz);
            return response.getBody();
        } catch (ResourceAccessException e) {
            log.error("requestSign. ResourceAccessException:", e);
            throw new BaseException(ConstantCode.REQUEST_SIGN_EXCEPTION);
        } catch (HttpStatusCodeException e) {
            errorFormat(e.getResponseBodyAsString());
        }
        throw new BaseException(ConstantCode.REQUEST_SIGN_EXCEPTION);
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
     */
    private static void errorFormat(String str) throws BaseException {
        JsonNode error = JsonTools.stringToJsonNode(str);
        log.error("requestSign fail. error:{}", error);
        if (ObjectUtils.isEmpty(error.get("errorMessage"))) {
            throw new BaseException(ConstantCode.REQUEST_SIGN_EXCEPTION);
        }
        String errorMessage = error.get("errorMessage").asText();
        throw new BaseException(ConstantCode.REQUEST_SIGN_EXCEPTION.getCode(), errorMessage);
    }
}
