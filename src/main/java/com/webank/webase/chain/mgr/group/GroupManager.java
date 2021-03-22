package com.webank.webase.chain.mgr.group;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbGroupExample;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class GroupManager {
    @Autowired
    private TbGroupMapper tbGroupMapper;

    /**
     * appId值等于group_name
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

        if (CollectionUtils.size(groupList) == 0) {
            log.warn("fail exec method [verifyAppId]. not found record by  appId:{}", appId);
            throw new BaseException(ConstantCode.INVALID_APP_ID);
        }

        tbGroup = groupList.get(0);
        log.info("success exec method [verifyAppId]. result:{}", JsonTools.objToString(tbGroup));
        return tbGroup;
    }

    /**
     * @param groupName
     */
    public void requireGroupNameNotFound(String groupName) {
        log.info("start exec method[requireGroupNameNotFound] groupName:{}", groupName);
        if (StringUtils.isBlank(groupName)) {
            log.warn("fail exec method [requireGroupNameNotFound]. groupName is empty");
            throw new BaseException(ConstantCode.GROUP_NAME_EMPTY);
        }

        //param
        TbGroupExample example = new TbGroupExample();
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andGroupNameEqualTo(groupName);

        //query
        long count = tbGroupMapper.countByExample(example);
        log.info("count:{}", count);
        if (count > 0) {
            log.warn("fail exec method [requireGroupNameNotFound]. found group record by groupName:{}", groupName);
            throw new BaseException(ConstantCode.DUPLICATE_GROUP_NAME);
        }

        log.info("finish exec method[listGroupByAgencyId] groupName:{}", groupName);
    }


    /**
     * @param chainId
     * @param groupId
     * @param description
     */
    public void updateDescription(int chainId, int groupId, String description) {
        log.info("start exec method[updateDescription] chainId:{} groupId:{} description:{}", chainId, groupId, description);
        TbGroup tbGroup = requireGroupExist(chainId, groupId);
        tbGroup.setDescription(description);
        tbGroup.setModifyTime(new Date());
        tbGroupMapper.updateByPrimaryKey(tbGroup);
        log.info("finish exec method[updateDescription] ");
    }

    /**
     * @param chainId
     * @param groupId
     * @return
     */
    public TbGroup requireGroupExist(int chainId, int groupId) {
        log.info("start exec method[requireGroupExist] chainId:{} groupId:{}", chainId, groupId);
        TbGroup exist = tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (Objects.isNull(exist))
            throw new BaseException(ConstantCode.INVALID_GROUP_ID.attach(String.format("not found group by chainId:%d groupId:%d", chainId, groupId)));

        log.debug("finish exec method[requireGroupExist] result:{}", JsonTools.objToString(exist));
        return exist;
    }


    /**
     * save group id
     */
    @Transactional
    public TbGroup saveGroup(String groupName, BigInteger timestamp, int groupId, int chainId, List<String> genesisNodeList, int nodeCount, String description,
                             int groupType) {
        if (groupId == 0) {
            return null;
        }

        TbGroup exists = this.tbGroupMapper.selectByPrimaryKey(groupId, chainId);
        if (exists == null) {
            // save group id
            if (StringUtils.isBlank(groupName)) {
                groupName = String.format("chain_%s_group_%s", chainId, groupId);
            } else {
                requireGroupNameNotFound(groupName);
            }
            TbGroup tbGroup = new TbGroup(timestamp, groupId, chainId, groupName, nodeCount, description, groupType);
            tbGroup.setNodeIdList(JsonTools.objToString(genesisNodeList));
            try {
                this.tbGroupMapper.insertSelective(tbGroup);
            } catch (Exception e) {
                log.error("Insert group error", e);
                throw e;
            }
            return tbGroup;
        } else if (!Objects.equals(nodeCount, exists.getNodeCount())) {
            log.info("group:{} oldNodeCount:{} newNodeCount:{} ", groupId, exists.getNodeCount(), nodeCount);
            exists.setNodeCount(nodeCount);
            exists.setModifyTime(new Date());
            tbGroupMapper.updateByPrimaryKey(exists);
        }
        return exists;
    }

    /**
     * @param appIdList
     * @return
     */
    public List<TbGroup> listGroupByAppIdList(List<String> appIdList) {
        log.info("start exec method[listGroupByAppIdList] appIdList:{}", JsonTools.objToString(appIdList));
        TbGroupExample example = new TbGroupExample();
        TbGroupExample.Criteria criteria = example.createCriteria();
        criteria.andGroupNameIn(appIdList);
        List<TbGroup> groupList = tbGroupMapper.selectByExample(example);
        log.info("success exec method[listGroupByAppIdList] result:{}", JsonTools.objToString(groupList));
        return groupList;
    }
}
