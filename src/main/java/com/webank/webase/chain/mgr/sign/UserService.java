package com.webank.webase.chain.mgr.sign;

import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.sign.req.EncodeInfo;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.sign.rsp.SignInfo;
import com.webank.webase.chain.mgr.util.CommUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */

@Slf4j
@Component
public class UserService {
    @Autowired
    private SignRestTools signRestTools;


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
        log.info("Request webase sign server:[{}]", url);
        Object response = signRestTools.getFromSign(url, Object.class);
        return response;
    }


    /**
     * @param reqNewUser
     * @return
     */
    public Object newUser(ReqNewUser reqNewUser) {
        String url = String.format(SignRestTools.URI_USER_NEW, signRestTools.getBaseUrl());
        log.info("Request webase sign server:[{}]:[{}]", url, JsonTools.toJSONString(reqNewUser));
        return signRestTools.postToSign(url, reqNewUser, Object.class);
    }

    /**
     * checkSignUserId.
     *
     * @param signUserId business id of user in sign
     * @return
     */
    public RspUserInfo checkSignUserId(String signUserId) throws BaseException {
        if (StringUtils.isBlank(signUserId)) {
            log.error("signUserId is null");
            return null;
        }
        String url = String.format(SignRestTools.URI_USER_INFO, signRestTools.getBaseUrl(), signUserId);
        log.debug("checkSignUserId url:{}", url);
        BaseResponse baseResponse = signRestTools.getFromSign(url, BaseResponse.class);
        return CommUtils.getResultData(baseResponse, RspUserInfo.class);
    }


    /**
     * getSignDatd from webase-sign service.
     *
     * @param param
     * @return
     * @throws BaseException
     */
    public String getSignData(EncodeInfo param) throws BaseException {
        String url = String.format(SignRestTools.URI_SIGN, signRestTools.getBaseUrl());
        log.debug("getSignData url:{}", url);
        BaseResponse baseResponse = signRestTools.postToSign(url, param,BaseResponse.class);
        SignInfo signInfo =  CommUtils.getResultData(baseResponse, SignInfo.class);
        return signInfo.getSignDataStr();
    }
}