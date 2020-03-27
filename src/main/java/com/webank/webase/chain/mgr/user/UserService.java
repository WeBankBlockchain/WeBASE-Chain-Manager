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
package com.webank.webase.chain.mgr.user;

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.frontinterface.FrontRestTools;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.user.entity.KeyPair;
import com.webank.webase.chain.mgr.user.entity.NewUserInputParam;
import com.webank.webase.chain.mgr.user.entity.TbUser;
import com.webank.webase.chain.mgr.user.entity.UpdateUserInputParam;
import com.webank.webase.chain.mgr.user.entity.UserParam;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * services for user data.
 */
@Log4j2
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontRestTools frontRestTools;

    /**
     * add new user data.
     */
    @Transactional
    public Integer addUserInfo(NewUserInputParam user) throws BaseException {
        log.debug("start addUserInfo User:{}", JSON.toJSONString(user));

        Integer chainId = user.getChainId();
        Integer groupId = user.getGroupId();

        // check group id
        groupService.checkGroupIdValid(chainId, groupId);

        // check signUserId
        TbUser userRow = queryBySignUserId(chainId, groupId, user.getSignUserId());
        if (userRow != null) {
            log.warn("fail addUserInfo. user info already exists");
            throw new BaseException(ConstantCode.USER_EXISTS);
        }
        String keyUri =
                String.format(FrontRestTools.URI_KEY_PAIR, user.getSignUserId(), user.getAppId());
        KeyPair keyPair = frontRestTools.getForEntity(chainId, groupId, keyUri, KeyPair.class);
        String publicKey = Optional.ofNullable(keyPair).map(k -> k.getPublicKey()).orElse(null);
        String address = Optional.ofNullable(keyPair).map(k -> k.getAddress()).orElse(null);

        if (StringUtils.isAnyBlank(publicKey, address)) {
            log.warn("get key pair fail. publicKey:{} address:{}", publicKey, address);
            throw new BaseException(ConstantCode.SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL);
        }

        // add row
        TbUser newUserRow = new TbUser(chainId, groupId, user.getSignUserId(), user.getAppId(),
                address, publicKey, user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_user");
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();
        log.debug("end addNodeInfo userId:{}", userId);
        return userId;
    }

    /**
     * query count of user.
     */
    public Integer countOfUser(UserParam userParam) throws BaseException {
        log.debug("start countOfUser. userParam:{}", JSON.toJSONString(userParam));

        try {
            Integer count = userMapper.countOfUser(userParam);
            log.debug("end countOfUser userParam:{} count:{}", JSON.toJSONString(userParam), count);
            return count;
        } catch (RuntimeException ex) {
            log.error("fail countOfUser userParam:{}", JSON.toJSONString(userParam), ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query user list by page.
     */
    public List<TbUser> qureyUserList(UserParam userParam) throws BaseException {
        log.debug("start qureyUserList userParam:{}", JSON.toJSONString(userParam));
        // query user list
        List<TbUser> listOfUser = userMapper.listOfUser(userParam);
        log.debug("end qureyUserList listOfUser:{}", JSON.toJSONString(listOfUser));
        return listOfUser;
    }

    /**
     * query user row.
     */
    public TbUser queryUser(Integer userId, Integer chainId, Integer groupId, String signUserId,
            String address) throws BaseException {
        log.debug("start queryUser userId:{} groupId:{} signUserId:{} address:{}", userId, groupId,
                signUserId, address);
        try {
            TbUser userRow = userMapper.queryUser(userId, chainId, groupId, signUserId, address);
            log.debug("end queryUser userId:{} groupId:{} signUserId:{}  address:{} TbUser:{}",
                    userId, groupId, signUserId, address, JSON.toJSONString(userRow));
            return userRow;
        } catch (RuntimeException ex) {
            log.error("fail queryUser userId:{} groupId:{} signUserId:{}  address:{}", userId,
                    groupId, signUserId, address, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query by signUserId.
     */
    public TbUser queryBySignUserId(int chainId, int groupId, String signUserId)
            throws BaseException {
        return queryUser(null, chainId, groupId, signUserId, null);
    }

    /**
     * query by userId.
     */
    public TbUser queryByUserId(Integer userId) throws BaseException {
        return queryUser(userId, null, null, null, null);
    }

    /**
     * update user info.
     */
    public void updateUser(UpdateUserInputParam user) throws BaseException {
        TbUser tbUser = queryByUserId(user.getUserId());
        tbUser.setDescription(user.getDescription());
        updateUser(tbUser);
    }

    /**
     * update user info.
     */
    public void updateUser(TbUser user) throws BaseException {
        log.debug("start updateUser user", JSON.toJSONString(user));
        Integer userId = Optional.ofNullable(user).map(u -> u.getUserId()).orElse(null);
        String description = Optional.ofNullable(user).map(u -> u.getDescription()).orElse(null);
        if (userId == null) {
            log.warn("fail updateUser. user id is null");
            throw new BaseException(ConstantCode.USER_ID_NULL);
        }
        TbUser tbUser = queryByUserId(userId);
        tbUser.setDescription(description);

        try {
            Integer affectRow = userMapper.updateUser(tbUser);
            if (affectRow == 0) {
                log.warn("affect 0 rows of tb_user");
                throw new BaseException(ConstantCode.DB_EXCEPTION);
            }
        } catch (RuntimeException ex) {
            log.error("fail updateUser  userId:{} description:{}", userId, description, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end updateOrtanization");
    }

    /**
     * delete by groupId.
     */
    public void deleteByChainId(int chainId) {
        if (chainId == 0) {
            return;
        }
        // delete user
        userMapper.deleteUser(chainId);
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        // delete user
        userMapper.deleteUserByGroupId(chainId, groupId);
    }
}
