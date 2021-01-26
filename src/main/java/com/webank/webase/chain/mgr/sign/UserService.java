package com.webank.webase.chain.mgr.sign;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbUser;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample;
import com.webank.webase.chain.mgr.repository.mapper.TbUserMapper;
import com.webank.webase.chain.mgr.sign.req.EncodeInfo;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.sign.rsp.SignInfo;
import com.webank.webase.chain.mgr.util.CommUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */

@Slf4j
@Component
public class UserService {
    @Autowired
    private SignRestTools signRestTools;
    @Autowired
    private GroupManager groupManager;
    @Autowired
    private UserManager userManager;
    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private ChainService chainService;


    /**
     * @param appId
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws BaseException
     */
    public BasePageResponse getUserListByAppId(String appId, Integer pageNumber, Integer pageSize)
            throws BaseException {
        //check appId
        TbGroup tbGroup = groupManager.verifyAppId(appId);
        //query user by page
        TbUserExample example = new TbUserExample();
        example.setStart(Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).filter(p -> p >= 0).orElse(1));
        example.setCount(pageSize);
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(tbGroup.getChainId());
        criteria.andGroupIdEqualTo(tbGroup.getGroupId());
        List<TbUser> userList = userMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(userList)) {
            log.info("finish exec method[getUserListByAppId], not found record");
            return new BasePageResponse(ConstantCode.SUCCESS);
        }

        //query remote server
        String signUserIdList = userList.stream().map(user -> user.getSignUserId()).collect(Collectors.joining(","));
        String url = String.format(SignRestTools.URI_USER_LIST, signRestTools.getBaseUrl(), appId, pageNumber, pageSize, signUserIdList);
        log.info("Request webase sign server:[{}]", url);
        BaseResponse restResponse = signRestTools.getFromSign(url, BaseResponse.class);
        List<RspUserInfo> restRspList = CommUtils.getResultData(restResponse, new TypeReference<List<RspUserInfo>>() {
        });

        //set userName
        for (RspUserInfo rspUserInfo : restRspList) {
            userList.stream()
                    .filter(user -> user.getSignUserId().equals(rspUserInfo.getSignUserId()))
                    .findFirst()
                    .ifPresent(u -> {
                        rspUserInfo.setSignUserName(u.getUserName());
                        rspUserInfo.setDescription(u.getDescription());
                    });
        }

        //return
        BasePageResponse response = new BasePageResponse(ConstantCode.SUCCESS);
        response.setTotalCount(Long.valueOf(userMapper.countByExample(example)).intValue());
        response.setData(restRspList);
        return response;
    }


    /**
     * @param reqNewUser
     * @return
     */
    @Transactional
    public RspUserInfo newUser(ReqNewUser reqNewUser) {
        //check appId
        TbGroup tbGroup = groupManager.verifyAppId(reqNewUser.getAppId());
        //check signUserId
        if (StringUtils.isBlank(reqNewUser.getSignUserId())) {
            log.info("signUserId is null,generate new");
            reqNewUser.setSignUserId(buildUserId(tbGroup.getChainId(), tbGroup.getGroupId()));
        }
        //check userName
        userManager.requireUserNameNotFound(reqNewUser.getChainId(), tbGroup.getGroupId(), reqNewUser.getSignUserName());
        //check chainId
        TbChain tbChain = chainService.verifyChainId(reqNewUser.getChainId());
        //check encrypt type
        if (Objects.isNull(reqNewUser.getEncryptType())) {
            reqNewUser.setEncryptType(Integer.valueOf(String.valueOf(tbChain.getChainType())));
        }

        //add to db
        TbUser tbUser = new TbUser();
        BeanUtils.copyProperties(reqNewUser, tbUser);
        tbUser.setGroupId(tbGroup.getGroupId());
        tbUser.setUserName(reqNewUser.getSignUserName());
        tbUser.setUserStatus(Integer.valueOf(String.valueOf(DataStatus.NORMAL.getValue())));
        tbUser.setGmtCreate(new Date());
        tbUser.setGmtModified(new Date());
        userMapper.insert(tbUser);

        //request remote server
        String url = String.format(SignRestTools.URI_USER_NEW, signRestTools.getBaseUrl());
        log.info("Request webase sign server:[{}]:[{}]", url, JsonTools.toJSONString(reqNewUser));
        BaseResponse restResponse = signRestTools.postToSign(url, reqNewUser, BaseResponse.class);
        RspUserInfo rspUserInfo = CommUtils.getResultData(restResponse, RspUserInfo.class);
        rspUserInfo.setSignUserName(reqNewUser.getSignUserName());
        rspUserInfo.setDescription(tbUser.getDescription());
        log.debug("restResponse:{}", JsonTools.objToString(rspUserInfo));
        return rspUserInfo;
    }


    /**
     * @param signUserId
     * @param description
     * @return
     */
    public void updateUserDescription(String signUserId, String description) {
        //check userId
        checkSignUserId(signUserId);
        TbUser tbUser = userManager.verifySignUserId(signUserId);
        tbUser.setDescription(description);
        tbUser.setGmtModified(new Date());
        userMapper.updateByPrimaryKey(tbUser);
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
        BaseResponse baseResponse = signRestTools.postToSign(url, param, BaseResponse.class);
        SignInfo signInfo = CommUtils.getResultData(baseResponse, SignInfo.class);
        return signInfo.getSignDataStr();
    }

    /**
     * @param chainId
     * @param groupId
     * @return
     */
    private String buildUserId(int chainId, int groupId) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String signUserId = String.join(ConstantProperties.SEPARATOR, String.valueOf(chainId), String.valueOf(groupId), uuid);
        log.info("userId:{}", signUserId);
        return signUserId;
    }
}