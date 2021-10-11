package com.webank.webase.chain.mgr.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Objects;

/**
 * build httpEntity.
 */
public class HttpEntityUtils {

    /**
     * @param host
     * @return
     */
    public static HttpEntity buildHttpEntityByHost(String host) {
        HttpHeaders headers = buildHttpHeaderByHost(host);
        return buildHttpEntity(headers);
    }


    /**
     * param or HttpHeaders
     *
     * @param param
     * @return
     */
    public static HttpEntity buildHttpEntity(Object param) {
        if (param instanceof HttpHeaders) {
            HttpHeaders headers = (HttpHeaders) param;
            return buildHttpEntity(headers, null);
        }
        return buildHttpEntity(null, param);
    }


    /**
     * build httpEntity
     */
    public static HttpEntity buildHttpEntity(HttpHeaders httpHeaders, Object param) {
        if (Objects.isNull(httpHeaders)) {
            httpHeaders = instantiateHttpHeaders();
        }
        if (Objects.isNull(httpHeaders.getContentType())) {
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        }
        String paramStr = null;
        if (Objects.nonNull(param)) {
            paramStr = JsonTools.toJSONString(param);
        }
        HttpEntity requestEntity = new HttpEntity(paramStr, httpHeaders);
        return requestEntity;
    }


    /**
     * @param host
     * @return
     */
    public static HttpHeaders buildHttpHeaderByHost(String host) {
        HttpHeaders headers = instantiateHttpHeaders();
        if (StringUtils.isNotBlank(host)) {
            headers.set(HttpHeaders.HOST, host);
        }
        return headers;
    }

    /**
     * @return
     */
    public static HttpHeaders instantiateHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");//支持自定义请求头
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

}
