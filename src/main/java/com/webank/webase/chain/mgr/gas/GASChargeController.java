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
package com.webank.webase.chain.mgr.gas;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.TbFront;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.entity.GasChargeManageHandle;
import com.webank.webase.chain.mgr.gas.entity.GasChargeManageParam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;

/**
 * Controller for processing group information.
 */
@Log4j2
@RestController
@RequestMapping("gas")
public class GASChargeController extends BaseController {

    @Autowired
    private FrontInterfaceService frontInterfaceService;

    @Autowired
    private FrontService frontService;


    /**
     * GasChargeManage.
     */
    @PostMapping(value = "GasChargeManage")
    public Object gasChargeManage(@RequestBody @Valid GasChargeManageParam gasChargeManageParam,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start gasChargeManage startTime:{} gasChargeManage:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(gasChargeManageParam));

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(gasChargeManageParam.getChainId(),
                gasChargeManageParam.getReqNodeId());
        if (tbFront == null) {
            log.error("fail gasChargeManage node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }
        GasChargeManageHandle gasChargeManageHandle = new GasChargeManageHandle();
        BeanUtils.copyProperties(gasChargeManageParam, gasChargeManageHandle);

        Object res = frontInterfaceService.gasChargeManage(tbFront.getFrontIp(),
                tbFront.getFrontPort(), gasChargeManageHandle);

        log.info("end gasChargeManage useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return res;
    }
}
