/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.agency;


import com.webank.webase.chain.mgr.agency.entity.RspAgencyVo;
import com.webank.webase.chain.mgr.agency.entity.RspAllOwnedDataOfAgencyVO;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * chain controller
 */
@Log4j2
@RestController
@RequestMapping("agency")
@Api(value = "agency")
public class AgencyController extends BaseController {
    @Autowired
    private AgencyService agencyService;


    /**
     * add new chain
     */
    @ApiOperation(value = "查看机构下的所以资源")
    @GetMapping("/{agencyId}/owned")
    public BaseResponse owned(@PathVariable("agencyId") Integer agencyId) {
        Instant startTime = Instant.now();
        log.info("start owned startTime:{} agencyId:{}", startTime.toEpochMilli(), agencyId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        RspAllOwnedDataOfAgencyVO data = agencyService.getAllByAgencyId(agencyId);
        baseResponse.setData(data);
        log.info("end owned useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    @ApiOperation(value = "查询机构列表")
    @GetMapping("/list")
    public BaseResponse queryAgencyList(@RequestParam("chainId") Integer chainId,
                                        @RequestParam(name = "groupId", required = false) Integer groupId,
                                        @RequestParam(value = "nodeTypes", required = false) List<String> nodeTypes) {
        Instant startTime = Instant.now();
        log.info("start queryAgencyList startTime:{} chainId:{} groupId:{} nodeTypes:{}", startTime.toEpochMilli(), chainId, groupId, JsonTools.objToString(nodeTypes));

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        List<RspAgencyVo> agencyList = agencyService.queryAgencyList(chainId, groupId, nodeTypes);
        if (CollectionUtils.isNotEmpty(agencyList)) {
            baseResponse.setData(agencyList.stream().map(agency -> agency.getAgencyId()).distinct().collect(Collectors.toList()));
        }


        log.info("end queryAgencyList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * 查询链下机构数
     *
     * @param chainId
     * @param groupId
     * @param nodeTypes
     * @return
     */
    @GetMapping(value = "/count")
    public BaseResponse getAgencyCount(@RequestParam("chainId") Integer chainId,
                                       @RequestParam(name = "groupId", required = false) Integer groupId,
                                       @RequestParam(value = "nodeTypes", required = false) List<String> nodeTypes) {
        Instant startTime = Instant.now();
        log.info("start getAgencyCount startTime:{}, chainId:{} groupId:{} nodeTypes:{}",
                startTime.toEpochMilli(), chainId, groupId, JsonTools.objToString(nodeTypes));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS, 0);
        List<RspAgencyVo> agencyList = agencyService.queryAgencyList(chainId, groupId, nodeTypes);
        if (CollectionUtils.isNotEmpty(agencyList))
            baseResponse.setData(agencyList.stream().map(agency -> agency.getAgencyId()).distinct().count());

        log.info("end getAgencyCount useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.objToString(baseResponse));
        return baseResponse;
    }
}
