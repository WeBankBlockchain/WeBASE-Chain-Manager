/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.chain.mgr.deploy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.ConfigTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.deploy.service.ConfigService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping(value = "/deploy/config")
public class ConfigController extends BaseController {

    @Autowired private ConfigService configService;

    /**
     * 获取配置类型
     */
    @GetMapping(value = "list/{type}/{update}")
    public BaseResponse listDockerTag(
            @PathVariable(value = "type", required = true) int type,
            @PathVariable(value = "update", required = false) boolean update
    ) throws Exception {
        log.info("list config, type: [{}], update: [{}]",type, update);

        ConfigTypeEnum configTypeEnum = ConfigTypeEnum.getById(type);
        if (configTypeEnum == null){
            throw new BaseException(ConstantCode.UNKNOWN_CONFIG_TYPE_ERROR);
        }

        // TODO. log
        return new BaseResponse(ConstantCode.SUCCESS, configService.selectConfigList(update, configTypeEnum));
    }
}

