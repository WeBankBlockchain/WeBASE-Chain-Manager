package com.webank.webase.chain.mgr.transaction;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.transaction.req.ReqContractDeploy;
import com.webank.webase.chain.mgr.transaction.req.ReqNewUser;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class TransactionService {
    @Autowired private TbContractMapper tbContractMapper;
    @Autowired private TransactionRestTools transactionRestTools;
    @Autowired private ConstantProperties properties;


    /**
     * @param appId
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws BaseException
     */
    public Object getUserListByAppId(String appId, Integer pageNumber, Integer pageSize)
            throws BaseException {
        String url = String.format(TransactionRestTools.URI_USER_LIST, transactionRestTools.getBaseUrl(),
                appId, pageNumber, pageSize);
        Object response = transactionRestTools.get(url, Object.class);
        return response;
    }

    /**
     *
     * @param contractId
     * @return
     * @throws BaseException
     */
    public Object deployContract(int contractId,String signUserId)
            throws BaseException {
        TbContract tbContract = this.tbContractMapper.selectByPrimaryKey(contractId);
        if (tbContract == null) {
            return new BaseResponse(ConstantCode.INVALID_CONTRACT_ID);
        }

        String url = String.format(TransactionRestTools.URI_CONTRACT_DEPLOY, transactionRestTools.getBaseUrl(tbContract.getChainId()));
        ReqContractDeploy contractDeploy = new ReqContractDeploy();
        BeanUtils.copyProperties(tbContract,contractDeploy);
        contractDeploy.setSignUserId(signUserId);
        log.info("Request transaction server:[{}]:[{}]",url, JsonTools.toJSONString(contractDeploy));
        return transactionRestTools.post(url, contractDeploy, Object.class);
    }

    /**
     *
     * @param reqNewUser
     * @return
     */
    public Object newUser(ReqNewUser reqNewUser) {
        String url = String.format(TransactionRestTools.URI_USER_NEW, transactionRestTools.getBaseUrl());
        log.info("Request transaction server:[{}]:[{}]",url, JsonTools.toJSONString(reqNewUser));
        return transactionRestTools.post(url, reqNewUser, Object.class);
    }
}