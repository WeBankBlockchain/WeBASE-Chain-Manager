package com.webank.webase.chain.mgr.deploy.controller;

import java.time.Instant;

import javax.validation.Valid;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("deploy/chain")
public class ChainController extends BaseController {

    @PostMapping(value = "deploy")
    public BaseResponse checkAndInit(
            @RequestBody @Valid ReqDeploy reqDeploy,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] deploy chain:[{}] ", startTime, JsonTools.toJSONString(reqDeploy));

        try {
            // generate node config and return shell execution log

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @GetMapping("/get/{chainId}}")
    public BaseResponse getChain(@PathVariable("chainId") int chainId)
            throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] get chain:[{}] ", startTime, chainId);

        try {
            // generate node config and return shell execution log

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }


}