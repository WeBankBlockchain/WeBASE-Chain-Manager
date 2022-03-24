package com.webank.webase.chain.mgr.sign;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbUser;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class UserManager {
    @Autowired
    private TbUserMapper userMapper;
    @Autowired
    private TbGroupMapper groupMapper;


    /**
     * @param chainId
     * @param groupId
     * @param userName
     * @return
     */
    public TbUser queryByChainAndGroupAndName(String chainId, String groupId, String userName) {
        log.info("start method[queryByChainAndGroupAndName] chain:{} group:{} user:{}", chainId, groupId, userName);
        //param
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andUserNameEqualTo(userName);

        //query
        TbUser tbUser = userMapper.getOneByExample(example).orElse(null);
        log.info("success method[queryByChainAndGroupAndName] chain:{} group:{} user:{} result:{}", chainId, groupId, userName, JsonTools.objToString(tbUser));
        return tbUser;
    }


    /**
     * @param chainId
     * @param groupId
     * @param userName
     */
    public void requireUserNameNotFound(String chainId, String groupId, String userName) {
        log.info("start method[requireUserNameNotFound] chain:{} group:{} user:{}", chainId, groupId, userName);
        TbUser tbUser = queryByChainAndGroupAndName(chainId, groupId, userName);
        if (Objects.nonNull(tbUser)) {
            log.warn("fail exec [requireUserNameNotFound],found record by userName:{}", userName);
            throw new BaseException(ConstantCode.USER_EXISTS);
        }
        log.info("success method[requireUserNameNotFound] chain:{} group:{} user:{}", chainId, groupId, userName);
    }

    /**
     * @param signUserId
     * @return
     */
    public TbUser verifySignUserId(String signUserId) {
        log.debug("start method[verifySignUserId] signUserId:{}", signUserId);
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andSignUserIdEqualTo(signUserId);
        TbUser tbUser = userMapper.getOneByExample(example).orElse(null);
        if (Objects.isNull(tbUser)) {
            log.warn("fail exec [verifySignUserId],found record by signUserId:{}", signUserId);
            throw new BaseException(ConstantCode.INVALID_USER);
        }
        log.info("success method[verifySignUserId] signUserId:{}", signUserId);
        return tbUser;
    }

}
