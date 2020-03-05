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
import com.webank.webase.chain.mgr.base.enums.HasPk;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.Web3Tools;
import com.webank.webase.chain.mgr.frontinterface.FrontRestTools;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.user.entity.BindUserInputParam;
import com.webank.webase.chain.mgr.user.entity.KeyPair;
import com.webank.webase.chain.mgr.user.entity.NewUserInputParam;
import com.webank.webase.chain.mgr.user.entity.PrivateKeyInfo;
import com.webank.webase.chain.mgr.user.entity.TbUser;
import com.webank.webase.chain.mgr.user.entity.TbUserKeyMap;
import com.webank.webase.chain.mgr.user.entity.UpdateUserInputParam;
import com.webank.webase.chain.mgr.user.entity.UserParam;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private ConstantProperties constants;

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

        // check userName
        TbUser userRow = queryByName(chainId, groupId, user.getUserName());
        if (userRow != null) {
            log.warn("fail addUserInfo. user info already exists");
            throw new BaseException(ConstantCode.USER_EXISTS);
        }

        String keyUri = String.format(FrontRestTools.URI_KEY_PAIR,
                constants.getIsPrivateKeyEncrypt(), user.getUserName());
        KeyPair keyPair = frontRestTools.getForEntity(chainId, groupId, keyUri, KeyPair.class);
        String privateKey = Optional.ofNullable(keyPair).map(k -> k.getPrivateKey()).orElse(null);
        String publicKey = Optional.ofNullable(keyPair).map(k -> k.getPublicKey()).orElse(null);
        String address = Optional.ofNullable(keyPair).map(k -> k.getAddress()).orElse(null);

        if (StringUtils.isAnyBlank(privateKey, publicKey, address)) {
            log.warn("get key pair fail. privateKey:{} publicKey:{} address:{}", privateKey,
                    publicKey, address);
            throw new BaseException(ConstantCode.SYSTEM_EXCEPTION);
        }

        // add row
        TbUser newUserRow = new TbUser(HasPk.HAS.getValue(), user.getUserType(), user.getUserName(),
                chainId, groupId, address, publicKey, user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("affect 0 rows of tb_user");
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();

        // add user_key_mapping info
        TbUserKeyMap newMapRow = new TbUserKeyMap(userId, privateKey);
        Integer affectMapRow = userMapper.addUserKeyMapRow(newMapRow);
        if (affectMapRow == 0) {
            log.warn("affect 0 rows of tb_user_key_map");
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        log.debug("end addNodeInfo userId:{}", userId);
        return userId;
    }

    /**
     * bind user info.
     */
    @Transactional
    public Integer bindUserInfo(BindUserInputParam user) throws BaseException {
        log.debug("start bindUserInfo User:{}", JSON.toJSONString(user));

        String publicKey = user.getPublicKey();
        if (StringUtils.isBlank(publicKey)) {
            log.info("fail bindUserInfo. publicKey cannot be empty");
            throw new BaseException(ConstantCode.PUBLICKEY_NULL);
        }

        if (publicKey.length() != ConstantProperties.PUBLICKEY_LENGTH
                && publicKey.length() != ConstantProperties.ADDRESS_LENGTH) {
            log.info("fail bindUserInfo. publicKey length error");
            throw new BaseException(ConstantCode.PUBLICKEY_LENGTH_ERROR);
        }

        // check group id
        groupService.checkGroupIdValid(user.getChainId(), user.getGroupId());

        // check userName
        TbUser userRow = queryByName(user.getChainId(), user.getGroupId(), user.getUserName());
        if (Objects.nonNull(userRow)) {
            log.warn("fail bindUserInfo. userName is already exists");
            throw new BaseException(ConstantCode.USER_EXISTS);
        }
        String address = publicKey;
        if (publicKey.length() == ConstantProperties.PUBLICKEY_LENGTH) {
            address = Web3Tools.getAddressByPublicKey(publicKey);
        }

        // check address
        TbUser addressRow = queryUser(null, user.getChainId(), user.getGroupId(), null, address);
        if (Objects.nonNull(addressRow)) {
            log.warn("fail bindUserInfo. address is already exists");
            throw new BaseException(ConstantCode.USER_EXISTS);
        }

        // add row
        TbUser newUserRow = new TbUser(HasPk.NONE.getValue(), user.getUserType(),
                user.getUserName(), user.getChainId(), user.getGroupId(), address, publicKey,
                user.getDescription());
        Integer affectRow = userMapper.addUserRow(newUserRow);
        if (affectRow == 0) {
            log.warn("bindUserInfo affect 0 rows of tb_user");
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

        Integer userId = newUserRow.getUserId();

        log.debug("end bindUserInfo userId:{}", userId);
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
    public TbUser queryUser(Integer userId, Integer chainId, Integer groupId, String userName,
            String address) throws BaseException {
        log.debug("start queryUser userId:{} groupId:{} userName:{} address:{}", userId, groupId,
                userName, address);
        try {
            TbUser userRow = userMapper.queryUser(userId, chainId, groupId, userName, address);
            log.debug("end queryUser userId:{} groupId:{} userName:{}  address:{} TbUser:{}",
                    userId, groupId, userName, address, JSON.toJSONString(userRow));
            return userRow;
        } catch (RuntimeException ex) {
            log.error("fail queryUser userId:{} groupId:{} userName:{}  address:{}", userId,
                    groupId, userName, address, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query by userName.
     */
    public TbUser queryByName(int chainId, int groupId, String userName) throws BaseException {
        return queryUser(null, chainId, groupId, userName, null);
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
     * get private key.
     */
    public PrivateKeyInfo getPrivateKey(String userAddress) throws BaseException {
        log.debug("start getPrivateKey ");
        PrivateKeyInfo privateKeyInfoInfo = userMapper.queryPrivateKey(userAddress);
        if (Objects.isNull(privateKeyInfoInfo)) {
            log.error("fail getPrivateKey, invalid userAddress:{}", userAddress);
            throw new BaseException(ConstantCode.INVALID_USER);
        }

        privateKeyInfoInfo.setPrivateKey(privateKeyInfoInfo.getPrivateKey());
        log.debug("end getPrivateKey,privateKeyInfoInfo:{}", JSON.toJSONString(privateKeyInfoInfo));
        return privateKeyInfoInfo;
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
        // delete map
        userMapper.deleteUserKeyMap(chainId);
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
        // delete map
        userMapper.deleteUserKeyMapByGroupId(chainId, groupId);
    }
}
