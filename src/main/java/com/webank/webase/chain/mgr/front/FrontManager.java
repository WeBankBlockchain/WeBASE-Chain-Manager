package com.webank.webase.chain.mgr.front;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.entity.FrontParam;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbFrontExample;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FrontManager {
    @Autowired
    private TbFrontMapper tbFrontMapper;

    /**
     * @param agencyId
     * @param frontPeerName
     * @return
     */
    public List<TbFront> queryFrontByAgencyIdAndFrontPeerNameAndNodeId(Integer agencyId, String frontPeerName, String nodeId) {
        log.debug("start exec method [listFrontByAgencyIdAndFrontPeerName]. agencyId:{} frontPeerName:{} nodeId:{}", agencyId, frontPeerName, nodeId);
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        if (Objects.nonNull(agencyId)) {
            criteria.andExtAgencyIdEqualTo(agencyId);
        }

        if (StringUtils.isNotBlank(frontPeerName)) {
            criteria.andFrontPeerNameEqualTo(frontPeerName);
        }

        if (StringUtils.isNotBlank(nodeId)) {
            criteria.andNodeIdEqualTo(nodeId);
        }
        criteria.andFrontStatusNotEqualTo(FrontStatusEnum.ABANDONED.getId());


        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.debug("success exec method [listFrontByAgencyIdAndFrontPeerName]. agencyId:{} frontPeerName:{} nodeId:{} result:{}", agencyId, frontPeerName, nodeId, JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param chainId
     * @return
     */
    public List<TbFront> listByChain(int chainId) {
        log.info("start exec method [listByChain]. chainId:{}", chainId);

        //param
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andFrontStatusNotEqualTo(FrontStatusEnum.ABANDONED.getId());

        //query
        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.info("success exec method [listByChain]. frontList:{}", JsonTools.objToString(frontList));
        return frontList;
    }

    /**
     * @param chainId
     * @param nodeIdList
     * @return
     */
    public List<TbFront> listByChainAndNodeIds(int chainId, List<String> nodeIdList) {
        log.info("start exec method [listByChainAndNodeIds]. chainId:{} nodeIds:{}", chainId, JsonTools.objToString(nodeIdList));

        //param
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andNodeIdIn(nodeIdList);
        criteria.andFrontStatusNotEqualTo(FrontStatusEnum.ABANDONED.getId());

        //query
        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.info("success exec method [listByChainAndNodeIds]. frontList:{}", JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param param
     * @return
     */
    public List<TbFront> listByParam(FrontParam param) {
        log.info("start exec method [listByParam]. param:{}", JsonTools.objToString(param));

        TbFrontExample example = buildExampleByParam(param);
        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.info("success exec method [listByParam]. result:{}", JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param param
     * @return
     */
    public long countByParam(FrontParam param) {
        log.info("start exec method [countByParam]. param:{}", JsonTools.objToString(param));

        TbFrontExample example = buildExampleByParam(param);
        long count = tbFrontMapper.countByExample(example);
        log.info("success exec method [countByParam]. result:{}", JsonTools.objToString(count));
        return count;
    }


    /**
     * @param param
     * @return
     */
    public TbFrontExample buildExampleByParam(FrontParam param) {
        TbFrontExample example = new TbFrontExample();
        if (Objects.isNull(param))
            return example;

        TbFrontExample.Criteria criteria = example.createCriteria();

        if (Objects.nonNull(param.getChainId()))
            criteria.andChainIdEqualTo(param.getChainId());

        if (Objects.nonNull(param.getExtAgencyId()))
            criteria.andExtAgencyIdEqualTo(param.getExtAgencyId());

        if (Objects.nonNull(param.getFrontId()))
            criteria.andFrontIdEqualTo(param.getFrontId());

        if (Objects.nonNull(param.getFrontId()))
            criteria.andFrontIdEqualTo(param.getFrontId());

        if (Objects.nonNull(param.getNodeId()))
            criteria.andNodeIdEqualTo(param.getNodeId());

        if (CollectionUtils.isNotEmpty(param.getNodeIdList()))
            criteria.andNodeIdIn(param.getNodeIdList());

        if (Objects.nonNull(param.getFrontStatus())){
            criteria.andFrontStatusEqualTo(param.getFrontStatus());
        }else {
            criteria.andFrontStatusNotEqualTo(FrontStatusEnum.ABANDONED.getId());
        }

        return example;

    }


    /**
     * @param agencyId
     * @return
     */
    public List<TbFront> listFrontByAgency(int agencyId) {
        log.debug("start exec method [listFrontByAgency]. agencyId:{}", agencyId);
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andExtAgencyIdEqualTo(agencyId);
        List<TbFront> frontList = tbFrontMapper.selectByExample(example);
        log.debug("success exec method [listFrontByAgency]. agencyId:{} result:{}", agencyId, JsonTools.objToString(frontList));
        return frontList;
    }


    /**
     * @param chainId
     * @param agencyId
     * @return
     */
    public List<TbFront> listFrontByChainAndAgency(int chainId, int agencyId) {
        log.debug("start exec method [listFrontByChainAndAgency]. chainId:{} agencyId:{}", chainId, agencyId);
        FrontParam frontParam = new FrontParam();
        frontParam.setChainId(chainId);
        frontParam.setExtAgencyId(agencyId);
        List<TbFront> frontList = listByParam(frontParam);
        log.debug("success exec method [listFrontByChainAndAgency]. chainId:{} agencyId:{} result:{}", chainId, agencyId, JsonTools.objToString(frontList));
        return frontList;
    }

    /**
     * @param chainId
     * @param agencyId
     * @return
     */
    public List<Integer> listFrontIdByChainAndAgency(int chainId, int agencyId) {
        log.debug("start exec method [listFrontIdByAgency]. chainId:{} agencyId:{}", chainId, agencyId);
        List<TbFront> frontList = listFrontByChainAndAgency(chainId, agencyId);
        if (CollectionUtils.isEmpty(frontList))
            return new ArrayList<>();

        List<Integer> idList = frontList.stream().map(TbFront::getFrontId).distinct().collect(Collectors.toList());
        log.debug("success exec method [listFrontIdByAgency]. agencyId:{} result:{}", agencyId, JsonTools.objToString(idList));
        return idList;
    }


    /**
     * @param agencyId
     * @return
     */
    public List<String> listNodeIdByAgency(int agencyId) {
        log.debug("start exec method [listNodeIdByAgency]. agencyId:{}", agencyId);
        List<TbFront> frontList = listFrontByAgency(agencyId);
        List<String> nodeIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(frontList))
            nodeIdList = frontList.stream().map(TbFront::getNodeId).collect(Collectors.toList());
        log.debug("success exec method [listNodeIdByAgency]. agencyId:{} result:{}", agencyId, JsonTools.objToString(nodeIdList));
        return nodeIdList;
    }


    /**
     * @param chainId
     * @param nodeId
     */
    public void requireNotFoundFront(int chainId, String nodeId, String frontPeerName) {
        log.info("start exec method[requireNotFoundFront] chainId:{} nodeId:{} frontPeerName:{}", chainId, nodeId, frontPeerName);
        TbFrontExample example = new TbFrontExample();
        TbFrontExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andNodeIdEqualTo(nodeId);

        tbFrontMapper.getOneByExample(example).ifPresent(front -> {
            if (Objects.equals(frontPeerName, front.getFrontPeerName()))
                throw new BaseException(ConstantCode.FRONT_EXISTS.attach(String.format("found front:%s record by chainId:%s nodeId:%s", front.getFrontPeerName(), chainId, nodeId)));
            throw new BaseException(ConstantCode.SAVE_FRONT_FAIL.attach(String.format("found front:%s record by chainId:%s nodeId:%s, but new:%s", front.getFrontPeerName(), chainId, nodeId, frontPeerName)));
        });


        if (StringUtils.isNotBlank(frontPeerName)) {
            TbFrontExample example1 = new TbFrontExample();
            TbFrontExample.Criteria criteria1 = example1.createCriteria();
            criteria1.andChainIdEqualTo(chainId);
            criteria1.andFrontPeerNameEqualTo(frontPeerName);

            tbFrontMapper.getOneByExample(example1).ifPresent(front -> {
                if (Objects.equals(nodeId, front.getNodeId()))
                    throw new BaseException(ConstantCode.FRONT_EXISTS.attach(String.format("found node:%s record by chainId:%s front:%s", front.getNodeId(), chainId, frontPeerName)));
                throw new BaseException(ConstantCode.SAVE_FRONT_FAIL.attach(String.format("found node:%s record by chainId:%s front:%s, but new:%s", front.getNodeId(), chainId, frontPeerName, nodeId)));
            });
        }

        log.info("finish exec method[requireNotFoundFront] chainId:{} nodeId:{}", chainId, nodeId);
    }


}
