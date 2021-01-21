package com.webank.webase.chain.mgr.contract;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class ContractManager {
    @Autowired
    private TbContractMapper tbContractMapper;

    /**
     * @param contractId
     * @return
     */
    public TbContract verifyContractId(int contractId) {
        log.info("start exec method [verifyContractId]. contractId:{}", contractId);
        TbContract tbContract = tbContractMapper.selectByPrimaryKey(contractId);
        if (Objects.isNull(tbContract)) {
            log.warn("fail exec method [verifyContractId]. not found record by contractId:{}", contractId);
            throw new BaseException(ConstantCode.INVALID_CONTRACT_ID);
        }
        log.info("success exec method [verifyContractId]. result:{}", JsonTools.objToString(tbContract));
        return tbContract;
    }
}
