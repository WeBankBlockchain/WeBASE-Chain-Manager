package com.webank.webase.chain.mgr.deploy.controller;

import java.time.Instant;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.webank.webase.chain.mgr.deploy.req.ReqHostCheckAndInit;
import com.webank.webase.chain.mgr.deploy.service.AsyncService;
import com.webank.webase.chain.mgr.deploy.service.HostService;
import com.webank.webase.chain.mgr.repository.bean.TbHost;
import com.webank.webase.chain.mgr.repository.mapper.TbHostMapper;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("deploy/host")
public class HostController extends BaseController {

    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private HostService hostService;
    @Autowired private AsyncService asyncService;

    @PostMapping(value = "checkAndInit")
    public BaseResponse checkAndInit(
            @RequestBody @Valid ReqHostCheckAndInit reqHostCheckAndInit,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] init host:[{}] ", startTime, JsonTools.toJSONString(reqHostCheckAndInit));

        try {
            // 1. check host connectable
            // 2. save host to db
            List<TbHost> tbHostList = this.hostService.checkAndSave(reqHostCheckAndInit);

            // init host asynchronous
            this.asyncService.initHostList(tbHostList);
            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @PostMapping(value = "select")
    public BaseResponse selectHostList(
            @RequestBody @Valid @Size(min = 1, message = "Ext agency id list error.") List<Integer> extAgencyIdList) throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] select host for ext agency id list:[{}] ", startTime, JsonTools.toJSONString(extAgencyIdList));

        try {
            // generate node config and return shell execution log
            List<TbHost> tbHosts = this.hostService.selectHosByExtAgencyIdList(extAgencyIdList);
            return BaseResponse.success(tbHosts);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @GetMapping("/get/{extAgencyId}/{ip}")
    public BaseResponse getHost(@PathVariable("extAgencyId") int extAgencyId, @PathVariable("ip") String ip)
            throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] get host:[{}:{}] ", startTime, extAgencyId, ip);

        try {
            // generate node config and return shell execution log
            TbHost host = this.tbHostMapper.getByAgencyIdAndIp(extAgencyId, ip);

            return BaseResponse.success(host);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }
}