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
package com.webank.webase.chain.mgr.front;


import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.front.entity.FrontInfo;
import com.webank.webase.chain.mgr.front.entity.FrontParam;
import com.webank.webase.chain.mgr.front.entity.ReqAbandonedFrontByAgencyIdVO;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * front controller
 */
@Log4j2
@RestController
@RequestMapping("front")
public class FrontController extends BaseController {

    @Autowired
    private FrontService frontService;
    @Autowired
    private TbFrontMapper tbFrontMapper;
    @Autowired
    private FrontManager frontManager;

    /**
     * add new front
     */
    @PostMapping("/new")
    public BaseResponse newFront(@RequestBody @Valid FrontInfo frontInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start newFront startTime:{} frontInfo:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(frontInfo));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TbFront tbFront = frontService.newFront(frontInfo);
        baseResponse.setData(tbFront);
        log.info("end newFront useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * qurey front info list.
     */
    @GetMapping(value = "/find")
    public BasePageResponse queryFrontList(
            @RequestParam(value = "agencyId", required = false) Integer agencyId,
            @RequestParam(value = "chainId") String chainId,
            @RequestParam(value = "frontId", required = false) Integer frontId,
            @RequestParam(value = "groupId", required = false) String groupId)
            throws BaseException {
        Instant startTime = Instant.now();
        log.info("start queryFrontList startTime:{} agencyId:{} chainId:{} frontId:{} groupId:{}",
                startTime.toEpochMilli(), agencyId, chainId, frontId, groupId);

        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        if (Objects.isNull(groupId)) {
            //query from tb_Front
            FrontParam param = new FrontParam();
            param.setExtAgencyId(agencyId);
            param.setChainId(chainId);
            param.setFrontId(frontId);
            pageResponse.setTotalCount(Long.valueOf(frontManager.countByParam(param)).intValue());
            if (pageResponse.getTotalCount() > 0) {
                pageResponse.setData(frontManager.listByParam(param));
            }
        } else {
            List<TbFront> frontList = frontService.listFront(chainId, groupId, frontId, agencyId);
            pageResponse.setTotalCount(frontList.size());
            pageResponse.setData(frontList);
        }


        log.info("end queryFrontList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pageResponse));
        return pageResponse;
    }

    /**
     * delete by frontId
     */
    @DeleteMapping(value = "/{frontId}")
    public BaseResponse removeFront(@PathVariable("frontId") Integer frontId) {
        Instant startTime = Instant.now();
        log.info("start removeFront startTime:{} frontId:{}", startTime.toEpochMilli(), frontId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // remove
        frontService.removeByFrontId(frontId);

        log.info("end removeFront useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

//    @GetMapping(value = "/mointorInfo/{frontId}")
//    public BaseResponse getChainMoinntorInfo(@PathVariable("frontId") Integer frontId,
//                                             @RequestParam(required = false) @DateTimeFormat(
//                                                     iso = ISO.DATE_TIME) LocalDateTime beginDate,
//                                             @RequestParam(required = false) @DateTimeFormat(
//                                                     iso = ISO.DATE_TIME) LocalDateTime endDate,
//                                             @RequestParam(required = false) @DateTimeFormat(
//                                                     iso = ISO.DATE_TIME) LocalDateTime contrastBeginDate,
//                                             @RequestParam(required = false) @DateTimeFormat(
//                                                     iso = ISO.DATE_TIME) LocalDateTime contrastEndDate,
//                                             @RequestParam(required = false, defaultValue = "1") int gap,
//                                             @RequestParam(required = false, defaultValue = "1") String groupId) throws BaseException {
//        Instant startTime = Instant.now();
//        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
//        log.info(
//                "start getChainInfo. startTime:{} frontId:{} beginDate:{} endDate:{} "
//                        + "contrastBeginDate:{} contrastEndDate:{} gap:{} groupId:{}",
//                startTime.toEpochMilli(), frontId, beginDate, endDate, contrastBeginDate,
//                contrastEndDate, gap, groupId);
//        Object rspObj = frontService.getNodeMonitorInfo(frontId, beginDate, endDate,
//                contrastBeginDate, contrastEndDate, gap, groupId);
//
//        response.setData(rspObj);
//        log.info("end getChainInfo. endTime:{} response:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
//
//        return response;
//    }
//
//    /**
//     * get ratio of performance.
//     */
//    @GetMapping(value = "/ratio/{frontId}")
//    public BaseResponse getPerformanceRatio(@PathVariable("frontId") Integer frontId,
//                                            @RequestParam(required = false) @DateTimeFormat(
//                                                    iso = ISO.DATE_TIME) LocalDateTime beginDate,
//                                            @RequestParam(required = false) @DateTimeFormat(
//                                                    iso = ISO.DATE_TIME) LocalDateTime endDate,
//                                            @RequestParam(required = false) @DateTimeFormat(
//                                                    iso = ISO.DATE_TIME) LocalDateTime contrastBeginDate,
//                                            @RequestParam(required = false) @DateTimeFormat(
//                                                    iso = ISO.DATE_TIME) LocalDateTime contrastEndDate,
//                                            @RequestParam(value = "gap", required = false, defaultValue = "1") int gap)
//            throws BaseException {
//        Instant startTime = Instant.now();
//        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
//        log.info(
//                "start getPerformanceRatio. startTime:{} frontId:{} beginDate:{}"
//                        + " endDate:{} contrastBeginDate:{} contrastEndDate:{} gap:{}",
//                startTime.toEpochMilli(), frontId, beginDate, endDate, contrastBeginDate,
//                contrastEndDate, gap);
//
//        Object rspObj = frontService.getPerformanceRatio(frontId, beginDate, endDate,
//                contrastBeginDate, contrastEndDate, gap);
//        response.setData(rspObj);
//        log.info("end getPerformanceRatio. useTime:{} response:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
//
//        return response;
//    }

    /**
     * get config of performance.
     */
//    @GetMapping(value = "/config/{frontId}")
//    public BaseResponse getPerformanceConfig(@PathVariable("frontId") Integer frontId)
//            throws BaseException {
//        Instant startTime = Instant.now();
//        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
//        log.info("start getPerformanceConfig. startTime:{} frontId:{}", startTime.toEpochMilli(),
//                frontId);
//        Object frontRsp = frontService.getPerformanceConfig(frontId);
//        response.setData(frontRsp);
//        log.info("end getPerformanceConfig. useTime:{} response:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
//        return response;
//    }

    /**
     * check node process.
     */
//    @GetMapping(value = "/checkNodeProcess/{frontId}")
//    public BaseResponse checkNodeProcess(@PathVariable("frontId") Integer frontId)
//            throws BaseException {
//        Instant startTime = Instant.now();
//        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
//        log.info("start checkNodeProcess. startTime:{} frontId:{}", startTime.toEpochMilli(),
//                frontId);
//        Object frontRsp = frontService.checkNodeProcess(frontId);
//        response.setData(frontRsp);
//        log.info("end checkNodeProcess. useTime:{} response:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
//        return response;
//    }

    /**
     * get group size infos.
     */
//    @GetMapping(value = "/getGroupSizeInfos/{frontId}")
//    public BaseResponse getGroupSizeInfos(@PathVariable("frontId") Integer frontId)
//            throws BaseException {
//        Instant startTime = Instant.now();
//        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
//        log.info("start getGroupSizeInfos. startTime:{} frontId:{}", startTime.toEpochMilli(),
//                frontId);
//        Object frontRsp = frontService.getGroupSizeInfos(frontId);
//        response.setData(frontRsp);
//        log.info("end getGroupSizeInfos. useTime:{} response:{}",
//                Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(response));
//        return response;
//    }

    /**
     * @param param
     * @param result
     * @return
     */
    @PostMapping("/abandonedByAgencyId")
    public BaseResponse abandonedFrontByAgencyId(@RequestBody @Valid ReqAbandonedFrontByAgencyIdVO param, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start abandonedFrontByAgencyId. startTime:{} param:{}", startTime.toEpochMilli(), JsonTools.objToString(param));
        frontService.abandonedFrontByAgencyId(param.getAgencyId());
        log.info("end abandonedFrontByAgencyId. useTime:{}");
        return BaseResponse.success(null);
    }

}
