/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.chain;


import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.chain.entity.TbChain;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * chain controller
 */
@Log4j2
@RestController
@RequestMapping("chain")
public class ChainController extends BaseController {

    @Autowired
    private ChainService chainService;

    /**
     * add new chain
     */
    @PostMapping("/new")
    public BaseResponse newChain(@RequestBody @Valid ChainInfo chainInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start newChain startTime:{} chainInfo:{}", startTime.toEpochMilli(),
                JSON.toJSONString(chainInfo));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TbChain tbChain = chainService.newChain(chainInfo);
        baseResponse.setData(tbChain);
        log.info("end newChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query chain info list.
     */
    @GetMapping("/all")
    public BasePageResponse queryChainList() {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryChainList startTime:{}", startTime.toEpochMilli());

        // query chain info
        int count = chainService.getChainCount(null);
        pagesponse.setTotalCount(count);
        if (count > 0) {
            List<TbChain> list = chainService.getChainList(null);
            pagesponse.setData(list);
        }

        log.info("end queryChainList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete by chainId
     */
    @DeleteMapping("/{chainId}")
    public BaseResponse removeChain(@PathVariable("chainId") Integer chainId) {
        Instant startTime = Instant.now();
        log.info("start removeChain startTime:{} chainId:{}", startTime.toEpochMilli(), chainId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // remove
        chainService.removeChain(chainId);

        log.info("end removeChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}
