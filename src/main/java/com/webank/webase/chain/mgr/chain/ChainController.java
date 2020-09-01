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


import java.time.Duration;
import java.time.Instant;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.deploy.req.ReqAddNode;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.deploy.service.DeployService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

/**
 * chain controller
 */
@Log4j2
@RestController
@RequestMapping("chain")
@Api(value = "chain")
public class ChainController extends BaseController {
    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private ChainService chainService;

    @Autowired private DeployService deployService;

    /**
     * add new chain
     */
    @ApiOperation(value = "添加链", hidden = true)
    @PostMapping("/new")
    public BaseResponse newChain(@RequestBody @Valid ChainInfo chainInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start newChain startTime:{} chainInfo:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(chainInfo));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TbChain tbChain = chainService.newChain(chainInfo);
        baseResponse.setData(tbChain);
        log.info("end newChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query chain info list.
     */
    @ApiOperation(value = "查询所有链")
    @GetMapping("/all")
    public BasePageResponse queryChainList() {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryChainList startTime:{}", startTime.toEpochMilli());

        // query chain info
        int count = tbChainMapper.countAll();
        pagesponse.setTotalCount(count);
        if (count > 0) {
            List<TbChain> list = tbChainMapper.selectAll();
            pagesponse.setData(list);
        }

        log.info("end queryChainList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete by chainId
     */
    @ApiOperation(value = "删除链")
    @DeleteMapping("{chainId}")
    public BaseResponse removeChain(@PathVariable("chainId") Integer chainId) {
        Instant startTime = Instant.now();
        log.info("start removeChain startTime:{} chainId:{}", startTime.toEpochMilli(), chainId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // remove
        chainService.removeChain(chainId);

        log.info("end removeChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    @ApiOperation(value = "部署链")
    @PostMapping(value = "deploy")
    public BaseResponse deploy(
            @RequestBody @Valid ReqDeploy reqDeploy,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] deploy chain:[{}] ", startTime, JsonTools.toJSONString(reqDeploy));

        try {
            // check chain name
            if(StringUtils.isBlank(reqDeploy.getChainName())){
                reqDeploy.setChainName(String.valueOf(reqDeploy.getChainId()));
            }

            // generate node config and return shell execution log
            this.deployService.deployChain(reqDeploy);

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @ApiOperation(value = "新增节点",hidden = true)
    @PostMapping(value = "addNode")
    public BaseResponse addNode(
            @RequestBody @Valid ReqAddNode reqAddNode,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] add node:[{}] ", startTime, JsonTools.toJSONString(reqAddNode));

        try {
            // generate node config and return shell execution log
//            this.deployService.addNode(reqAddNode);

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @ApiOperation(value = "查询单链信息")
    @GetMapping("/get/{chainId}")
    public BaseResponse getChain(@PathVariable("chainId") int chainId)
            throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] get chain:[{}] ", startTime, chainId);

        TbChain chain = this.tbChainMapper.selectByPrimaryKey(chainId);
        if (chain != null){
            int progress = chainService.progress(chain);
            chain.setProgress(progress);
            return BaseResponse.success(chain);
        }
        return new BaseResponse(ConstantCode.CHAIN_ID_NOT_EXISTS);
    }
}
