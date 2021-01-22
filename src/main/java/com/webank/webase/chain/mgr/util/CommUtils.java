package com.webank.webase.chain.mgr.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class CommUtils {
    private static final Integer HTTP_SUCCESS_RESPONSE_CODE = 0;


    public static <T> T getResultData(String responseJson, Class<T> dataClass) {
        BaseResponse response = JsonTools.toJavaObject(responseJson, BaseResponse.class);
        return getResultData(response, dataClass);
    }

    /**
     * @param response
     * @param dataClass
     * @param <T>
     * @return
     */
    public static <T> T getResultData(BaseResponse response, Class<T> dataClass) {
        if (dataClass == null) {
            log.info("finish exec method [getResultData]. dataClass is null,return null");
            return null;
        }
        if (Objects.isNull(response)) {
            log.info("finish exec method [getResultData]. response is null,return null");
            return null;
        }
        if (HTTP_SUCCESS_RESPONSE_CODE != response.getCode()) {
            log.info("finish exec method [getResultData]. response code:{} message:{}", response.getCode(), response.getMessage());
            throw new BaseException(response.getCode(), response.getMessage());
        }

        T t = JsonTools.toJavaObject(response.getData(), dataClass);
        log.info("success exec method [getResultData]. data:{}", JsonTools.objToString(response.getData()));
        return t;
    }


    public static <T> T getResultData(BaseResponse response, TypeReference<T> tTypeReference) {
        if (tTypeReference == null) {
            log.info("finish exec method [getResultData]. tTypeReference is null,return null");
            return null;
        }
        if (Objects.isNull(response)) {
            log.info("finish exec method [getResultData]. response is null,return null");
            return null;
        }
        if (HTTP_SUCCESS_RESPONSE_CODE != response.getCode()) {
            log.info("finish exec method [getResultData]. response code:{} message:{}", response.getCode(), response.getMessage());
            throw new BaseException(response.getCode(), response.getMessage());
        }

        T t = JsonTools.stringToObj(JsonTools.objToString(response.getData()), tTypeReference);
        log.info("success exec method [getResultData]. data:{}", JsonTools.objToString(response.getData()));
        return t;
    }


}
