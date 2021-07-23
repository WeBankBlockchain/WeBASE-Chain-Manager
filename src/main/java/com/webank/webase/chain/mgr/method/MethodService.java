/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.method;

import com.webank.webase.chain.mgr.method.entity.Method;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbMethod;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbMethodMapper;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Log4j2
@Service
public class MethodService {

    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private TbMethodMapper tbMethodMapper;

    /**
     * save method info from Contract.
     */
    @Async(value = "asyncExecutor")
    public void saveMethodFromContract(TbContract tbContract) {
        Instant startTime = Instant.now();
        log.info("start saveMethodFromContract startTime:{}", startTime.toEpochMilli());
        
        TbChain tbChain = tbChainMapper.selectByPrimaryKey(tbContract.getChainId());
        if (ObjectUtils.isEmpty(tbChain)) {
            return;
        }
        TransactionDecoder transactionDecoder =
                new TransactionDecoder(tbContract.getContractAbi(), tbChain.getChainType());
        List<Method> methodList = transactionDecoder.methodInfo();
        TbMethod tbMethod = new TbMethod();
        BeanUtils.copyProperties(tbContract, tbMethod);
        // save each method
        for (Method method : methodList) {
            BeanUtils.copyProperties(method, tbMethod);
            tbMethodMapper.replaceInto(tbMethod);
        }
        
        log.info("end saveMethodFromContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
    }

    /**
     * deleteByContractId.
     */
    public void deleteByContractId(Integer contractId) {
        tbMethodMapper.deleteByContractId(contractId);
    }
}
