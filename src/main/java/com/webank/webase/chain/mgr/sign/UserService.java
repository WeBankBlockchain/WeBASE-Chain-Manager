package com.webank.webase.chain.mgr.sign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class UserService {
    @Autowired private SignRestTools signRestTools;


    /**
     * @param appId
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws BaseException
     */
    public Object getUserListByAppId(String appId, Integer pageNumber, Integer pageSize)
            throws BaseException {
        String url = String.format(SignRestTools.URI_USER_LIST, signRestTools.getBaseUrl(), appId, pageNumber, pageSize);
        log.info("Request webase sign server:[{}]",url);
        Object response = signRestTools.getFromSign(url, Object.class);
        return response;
    }


    /**
     *
     * @param reqNewUser
     * @return
     */
    public Object newUser(ReqNewUser reqNewUser) {
        String url = String.format(SignRestTools.URI_USER_NEW, signRestTools.getBaseUrl());
        log.info("Request webase sign server:[{}]:[{}]",url, JsonTools.toJSONString(reqNewUser));
        return signRestTools.postToSign(url, reqNewUser, Object.class);
    }
}