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
package com.webank.webase.chain.mgr.group;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.entity.*;
import com.webank.webase.chain.mgr.node.entity.AddSealerAsyncParam;
import com.webank.webase.chain.mgr.node.entity.ConsensusParam;
import com.webank.webase.chain.mgr.node.entity.RspAddSealerAsyncVO;
import com.webank.webase.chain.mgr.precompiledapi.PrecompiledService;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbFrontGroupMap;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
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
    @Autowired
    private PrecompiledService precompiledService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private GroupManager groupManager;


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
        if (Objects.equals(ConstantProperties.DEFAULT_GROUP_ID, req.getGenerateGroupId())) {
            throw new BaseException(ConstantCode.CANNOT_USE_DEFAULT_GROUP_ID);
        }
        TbGroup tbGroup = groupService.generateGroup(req);
        baseResponse.setData(tbGroup);

        resetGroupListTask.asyncResetGroupList();
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
        resetGroupListTask.asyncResetGroupList();
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

        Object result = frontInterfaceService.getConsensusList(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber);

        log.info("end getConsensusList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * set node consensus status.
     */
    @PostMapping(value = "setConsensusStatus")
    public BaseResponse setConsensusStatus(@RequestBody @Valid ConsensusParam consensusParam,
                                           BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start setConsensusStatus startTime:{} consensusParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(consensusParam));

        precompiledService.setConsensusStatus(consensusParam);

        resetGroupListTask.asyncResetGroupList();

        log.info("end setConsensusStatus useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return BaseResponse.success(null);
    }

    /**
     * @param param
     * @param result
     * @return
     * @throws BaseException
     */
    @PostMapping(value = "addSealerAsync")
    public BaseResponse addSealerAsync(@RequestBody @Valid AddSealerAsyncParam param,
                                       BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start addSealerAsync startTime:{} param:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(param));

        RspAddSealerAsyncVO rsp = precompiledService.addSealerAsync(param);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        if (!rsp.getAllSuccessFlag()) {
            baseResponse.setCode(ConstantCode.ADD_SEALER_ASYNC_FAIL.getCode());
            baseResponse.setMessage(ConstantCode.ADD_SEALER_ASYNC_FAIL.getMessage());
            baseResponse.setAttachment(JsonTools.objToString(rsp.getErrorMessages()));
        }
        baseResponse.setData(rsp);
        log.info("end addSealerAsync useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.objToString(baseResponse));
        return baseResponse;
    }


    /**
     * getSysConfigList.
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

        Object result = frontInterfaceService.getSysConfigList(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
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

        Object res = frontInterfaceService.setSysConfigByKey(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), reqSetSysConfig);

        log.info("end setSysConfigByKey useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return res;
    }

    /**
     * getNetWorkData.
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

        Object result = frontInterfaceService.getNetWorkData(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), groupId, pageSize, pageNumber, beginDate, endDate);

        log.info("end getNetWorkData useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * getNetWorkData.
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
                frontInterfaceService.getTxGasData(tbFront.getFrontPeerName(), tbFront.getFrontIp(), tbFront.getFrontPort(),
                        groupId, pageSize, pageNumber, beginDate, endDate, transHash);

        log.info("end getTxGasData useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }

    /**
     * delete charging Data.
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

        Object result = frontInterfaceService.deleteLogData(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
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
        log.info("sta   rt consensus list startTime:{}", startTime.toEpochMilli());

        int newGroupId = groupId == null || groupId <= 0 ? ConstantProperties.DEFAULT_GROUP_ID : groupId;

        // get front
        List<TbFrontGroupMap> frontGroupMapList = frontGroupMapService.listByChainAndGroup(chainId, newGroupId);
        if (CollectionUtils.isEmpty(frontGroupMapList)) {
            log.error("fail getConsensusList node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        TbFrontGroupMap map = frontGroupMapList.get(0);
        TbFront tbFront = frontService.getById(map.getFrontId());
        Object result = frontInterfaceService.getConsensusList(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                tbFront.getFrontPort(), newGroupId, pageSize, pageNumber);

        log.info("end getConsensusList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return result;
    }


    @GetMapping("page/{chainId}")
    public BasePageResponse queryGroupByPage(@PathVariable("chainId") Integer chainId,
                                             @RequestParam(name = "agency", required = false) Integer agencyId,
                                             @RequestParam(defaultValue = "10") Integer pageSize,
                                             @RequestParam(defaultValue = "1") Integer pageNumber,
                                             @RequestParam(name = "status", required = false) Byte status) {
        Instant startTime = Instant.now();
        log.info("start queryGroupByPage startTime:{} chainId:{} agencyId:{} pageNumber:{} pageSize:{} status:{}", startTime.toEpochMilli(), chainId, agencyId, pageSize, pageNumber, status);
        BasePageResponse basePageResponse = groupService.queryGroupByPage(chainId, agencyId, pageSize, pageNumber, status);
        log.info("end queryGroupByPage useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.objToString(basePageResponse));
        return basePageResponse;
    }


    /**
     * @param chainId
     * @param groupId
     * @return
     */
    @GetMapping("{chainId}/{groupId}/detail")
    public BaseResponse detail(@PathVariable("chainId") Integer chainId,
                               @PathVariable("groupId") Integer groupId) {
        Instant startTime = Instant.now();
        log.info("start queryGroupDetail startTime:{} chainId:{} groupId:{}", startTime.toEpochMilli(), chainId, groupId);

        RspGroupDetailVo rspGroupDetailVo = groupService.queryGroupDetail(chainId, groupId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(rspGroupDetailVo);
        log.info("end queryGroupDetail useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.objToString(baseResponse));
        return baseResponse;
    }


    /**
     * @param param
     * @param result
     * @return
     * @throws BaseException
     */
    @GetMapping("changeDescription")
    public BaseResponse updateDescription(@RequestBody @Valid ReqUpdateGroupVo param,
                                          BindingResult result) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start updateDescription startTime:{} param:{}", startTime.toEpochMilli(), JsonTools.objToString(param));

        checkBindResult(result);
        groupManager.updateDescription(param.getChainId(), param.getGroupId(), param.getDescription());
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        log.info("end updateDescription useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.objToString(baseResponse));
        return baseResponse;
    }

}
