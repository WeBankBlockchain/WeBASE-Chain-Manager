package com.webank.webase.chain.mgr.transaction;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.transaction.req.ReqContractDeploy;

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
     * @param chainId
     * @param appId
     * @param pageNumber
     * @param pageSize
     * @return
     * @throws BaseException
     */
    public Object getUserListByAppId(int chainId, String appId, Integer pageNumber, Integer pageSize)
            throws BaseException {
        String url = String.format(TransactionRestTools.URI_USER_LIST, transactionRestTools.getBaseUrl(chainId),
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
        return transactionRestTools.post(url, contractDeploy, Object.class);
    }

}