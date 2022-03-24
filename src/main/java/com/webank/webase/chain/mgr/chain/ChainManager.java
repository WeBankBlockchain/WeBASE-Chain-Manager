package com.webank.webase.chain.mgr.chain;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class ChainManager {
    @Autowired
    private TbChainMapper chainMapper;

    /**
     * @param chainId
     * @return
     */
    public TbChain requireChainIdExist(String chainId) {
        log.info("start exec method [requireChainIdExist]. chainId:{}", chainId);
        TbChain tbChain = chainMapper.selectByPrimaryKey(chainId);
        if (Objects.isNull(tbChain)) {
            log.warn("fail exec method [requireChainIdExist]. not found record by chainId:{}", chainId);
            throw new BaseException(ConstantCode.INVALID_CHAIN_ID);
        }
        log.info("success exec method [requireChainIdExist]. result:{}", JsonTools.objToString(tbChain));
        return tbChain;
    }


}
