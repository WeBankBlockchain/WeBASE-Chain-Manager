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
package com.webank.webase.chain.mgr.group;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.chain.mgr.group.entity.ReqSetSysConfig;
import com.webank.webase.chain.mgr.group.entity.ReqStartGroup;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;

import lombok.extern.log4j.Log4j2;

/**
 * Controller for processing group information.
 */
@Log4j2
@RestController
@RequestMapping("group")
public class GroupController extends BaseController {

    @Autowired
    private TbGroupMapper tbGroupMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ResetGroupListTask resetGroupListTask;
    @Autowired
    private FrontInterfaceService frontInterfaceService;
    @Autowired
    private FrontService frontService;


    /**
     * generate group to single node.
     */
    @PostMapping("/generate/{nodeId}")
    public BaseResponse generateToSingleNode(@PathVariable("nodeId") String nodeId,
            @RequestBody @Valid ReqGenerateGroup req, BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateToSingleNode startTime:{} nodeId:{}", startTime.toEpochMilli(),
                nodeId);
        TbGroup tbGroup = groupService.generateToSingleNode(nodeId, req);
        baseResponse.setData(tbGroup);
        log.info("end generateToSingleNode useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * generate group.
     */
    @PostMapping("/generate")
    public BaseResponse generateGroup(@RequestBody @Valid ReqGenerateGroup req,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        if (req.getGenerateGroupId() == ConstantProperties.DEFAULT_GROUP_ID){
            throw new BaseException(ConstantCode.CANNOT_USE_DEFAULT_GROUP_ID);
        }
        TbGroup tbGroup = groupService.generateGroup(req);
        baseResponse.setData(tbGroup);
        log.info("end generateGroup useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * operate group.
     */
    @GetMapping("/operate/{chainId}/{groupId}/{nodeId}/{type}")
    public Object operateGroup(@PathVariable("chainId") Integer chainId,
            @PathVariable("nodeId") String nodeId, @PathVariable("groupId") Integer groupId,
            @PathVariable("type") String type) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start operateGroup startTime:{} groupId:{}", startTime.toEpochMilli(), groupId);
        Object groupHandleResult = groupService.operateGroup(chainId, nodeId, groupId, type);
        log.info("end operateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(groupHandleResult));
        return groupHandleResult;
    }

    /**
     * batch start group.
     */
    @PostMapping("/batchStart")
    public BaseResponse batchStartGroup(@RequestBody @Valid ReqStartGroup req, BindingResult result)
            throws BaseException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start batchStartGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        groupService.batchStartGroup(req);
        log.info("end batchStartGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update group.
     */
    @GetMapping("/update")
    public BaseResponse updateGroup() throws BaseException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start updateGroup startTime:{}", startTime.toEpochMilli());
        groupService.resetGroupList();
        log.info("end updateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get group general.
     */
    @GetMapping("/general/{chainId}/{groupId}")
    public BaseResponse getGroupGeneral(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId) throws BaseException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupGeneral startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);
        GroupGeneral groupGeneral = groupService.queryGroupGeneral(chainId, groupId);
        baseResponse.setData(groupGeneral);
        log.info("end getGroupGeneral useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query all group.
     */
    @GetMapping("/all/{chainId}")
    public BasePageResponse getAllGroup(@PathVariable("chainId") Integer chainId)
            throws BaseException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroup startTime:{}", startTime.toEpochMilli());

        // get group list
        int count = this.tbGroupMapper.countByChainIdAndGroupStatus(chainId, DataStatus.NORMAL.getValue());
        if (count > 0) {
            List<TbGroup> groupList =
                    groupService.getGroupList(chainId, DataStatus.NORMAL.getValue());
            pagesponse.setTotalCount(count);
            pagesponse.setData(groupList);
        }

        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroup useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pagesponse;
    }

    /**
     * get node consensus list.
     */
    @GetMapping("getConsensusList/{chainId}/{groupId}/{nodeId}")
    public Object getConsensusList(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "1") Integer pageNumber) {

        Instant startTime = Instant.now();
        log.info("start getConsensusList startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getConsensusList node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result = frontInterfaceService.getConsensusList(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber);

        log.info("end getConsensusList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * set node consensus status.
     */
    @PostMapping(value = "setConsensusStatus")
    public Object setConsensusStatus(@RequestBody @Valid ConsensusParam consensusParam,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setConsensusStatus startTime:{} consensusParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(consensusParam));

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(consensusParam.getChainId(),
                consensusParam.getReqNodeId());
        if (tbFront == null) {
            log.error("fail setConsensusStatus node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object res = frontInterfaceService.setConsensusStatus(tbFront.getFrontIp(),
                tbFront.getFrontPort(), consensusParam);

        log.info("end setConsensusStatus useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return res;
    }

    /**
     * getSysConfigList.
     * 
     */
    @GetMapping("getSysConfigList/{chainId}/{groupId}/{nodeId}")
    public Object getSysConfigList(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber) {

        Instant startTime = Instant.now();
        log.info("start getSysConfigList startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getSysConfigList node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result = frontInterfaceService.getSysConfigList(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber);

        log.info("end getSysConfigList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * set system config by key.
     */
    @PostMapping(value = "setSysConfig")
    public Object setSysConfigByKey(@RequestBody @Valid ReqSetSysConfig reqSetSysConfig,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setSysConfigByKey startTime:{} reqSetSysConfig:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(reqSetSysConfig));

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(reqSetSysConfig.getChainId(),
                reqSetSysConfig.getNodeId());
        if (tbFront == null) {
            log.error("fail setSysConfigByKey node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object res = frontInterfaceService.setSysConfigByKey(tbFront.getFrontIp(),
                tbFront.getFrontPort(), reqSetSysConfig);

        log.info("end setSysConfigByKey useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return res;
    }

    /**
     * getNetWorkData.
     * 
     */
    @GetMapping("/charging/getNetWorkData/{chainId}/{groupId}/{nodeId}")
    public Object getNetWorkData(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = ISO.DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = ISO.DATE_TIME) LocalDateTime endDate) {

        Instant startTime = Instant.now();
        log.info("start getNetWorkData startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getNetWorkData node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result = frontInterfaceService.getNetWorkData(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber, beginDate, endDate);

        log.info("end getNetWorkData useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * getNetWorkData.
     * 
     */
    @GetMapping("/charging/getTxGasData/{chainId}/{groupId}/{nodeId}")
    public Object getTxGasData(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = ISO.DATE_TIME) LocalDateTime beginDate,
            @RequestParam(required = false) @DateTimeFormat(
                    iso = ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String transHash) {

        Instant startTime = Instant.now();
        log.info("start getTxGasData startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getTxGasData node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result =
                frontInterfaceService.getTxGasData(tbFront.getFrontIp(), tbFront.getFrontPort(),
                        groupId, pageSize, pageNumber, beginDate, endDate, transHash);

        log.info("end getTxGasData useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * delete charging Data.
     * 
     */
    @DeleteMapping("/charging/deleteData/{chainId}/{groupId}/{nodeId}")
    public Object deleteData(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
            @RequestParam(required = true) int type, @RequestParam(required = true) @DateTimeFormat(
                    iso = ISO.DATE_TIME) LocalDateTime keepEndDate) {

        Instant startTime = Instant.now();
        log.info("start deleteData startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail deleteData node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result = frontInterfaceService.deleteLogData(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, type, keepEndDate);

        log.info("end deleteData useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }


    /**
     * get node consensus list.
     */
    @GetMapping("consensus/list/{chainId}/{groupId}")
    public Object getConsensusList(@PathVariable("chainId") Integer chainId,
                                   @PathVariable("groupId") Integer groupId,
                                   @RequestParam(defaultValue = "10") Integer pageSize,
                                   @RequestParam(defaultValue = "1") Integer pageNumber) {

        Instant startTime = Instant.now();
        log.info("start consensus list startTime:{}", startTime.toEpochMilli());

        // get front
        TbFront tbFront = frontService.getByChainIdAndGroupId(chainId,groupId);
        if (tbFront == null) {
            log.error("fail getConsensusList node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Object result = frontInterfaceService.getConsensusList(tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber);

        log.info("end getConsensusList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }
}
