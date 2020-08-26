package com.webank.webase.chain.mgr.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
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
    @Autowired private ContractService contractService;


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
     * @param reqNewUser
     * @return
     */
    public Object newUser(ReqNewUser reqNewUser) {
        String url = String.format(TransactionRestTools.URI_USER_NEW, transactionRestTools.getBaseUrl());
        log.info("Request transaction server:[{}]:[{}]",url, JsonTools.toJSONString(reqNewUser));
        return transactionRestTools.post(url, reqNewUser, Object.class);
    }
}