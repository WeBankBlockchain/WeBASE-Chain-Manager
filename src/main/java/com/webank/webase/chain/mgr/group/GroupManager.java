package com.webank.webase.chain.mgr.group;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbGroupExample;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GroupManager {
    @Autowired
    private TbGroupMapper tbGroupMapper;

    /**
     * appId统一取值group_name
     *
     * @param appId
     * @return
     */
    public TbGroup verifyAppId(String appId) {
        log.info("start exec method [verifyAppId]. appId:{}", appId);
        TbGroup tbGroup = null;

        TbGroupExample example = new TbGroupExample();
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andGroupNameEqualTo(appId);
        List<TbGroup> groupList = tbGroupMapper.selectByExample(example);
        if (CollectionUtils.size(groupList) > 1) {
            log.info("is too many data match by appId:{}", appId);
            throw new BaseException(ConstantCode.FOUND_TOO_MANY_DATA_BY_APP_ID);
        }
        tbGroup = groupList.get(0);

        if (Objects.isNull(tbGroup)) {
            log.warn("fail exec method [verifyAppId]. not found record by  appId:{}", appId);
            throw new BaseException(ConstantCode.INVALID_APP_ID);
        }
        log.info("success exec method [verifyAppId]. result:{}", JsonTools.objToString(tbGroup));
        return tbGroup;
    }
}
