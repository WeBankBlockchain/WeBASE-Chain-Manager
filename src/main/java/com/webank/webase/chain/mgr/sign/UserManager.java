package com.webank.webase.chain.mgr.sign;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbUser;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
     */
    public void requireUserNameNotFound(int chainId, int groupId, String userName) {
        log.info("start method[requireUserNameNotFound] chain:{} group:{} user:{}", chainId, groupId, userName);
        TbUserExample example = new TbUserExample();
        TbUserExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andUserNameEqualTo(userName);
        if (userMapper.getOneByExample(example).isPresent()) {
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
