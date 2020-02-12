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

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.NodeMgrException;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.group.entity.ReqGenerateGroup;
import com.webank.webase.chain.mgr.group.entity.ReqStartGroup;
import com.webank.webase.chain.mgr.group.entity.TbGroup;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing group information.
 */
@Log4j2
@RestController
@RequestMapping("group")
public class GroupController extends BaseController {

    @Autowired
    private GroupService groupService;
    @Autowired
    private ResetGroupListTask resetGroupListTask;

    /**
     * generate group to single node.
     */
    @PostMapping("/generate/{nodeId}")
    public BaseResponse generateToSingleNode(@PathVariable("nodeId") String nodeId,
            @RequestBody @Valid ReqGenerateGroup req, BindingResult result)
            throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateToSingleNode startTime:{} nodeId:{}", startTime.toEpochMilli(),
                nodeId);
        TbGroup tbGroup = groupService.generateToSingleNode(nodeId, req);
        baseResponse.setData(tbGroup);
        log.info("end generateToSingleNode useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * generate group.
     */
    @PostMapping("/generate")
    public BaseResponse generateGroup(@RequestBody @Valid ReqGenerateGroup req,
            BindingResult result) throws NodeMgrException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start generateGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        TbGroup tbGroup = groupService.generateGroup(req);
        baseResponse.setData(tbGroup);
        log.info("end generateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * start group.
     */
    @GetMapping("/start/{chainId}/{startGroupId}/{nodeId}")
    public BaseResponse startGroup(@PathVariable("chainId") Integer chainId,
            @PathVariable("nodeId") String nodeId,
            @PathVariable("startGroupId") Integer startGroupId) throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start startGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                startGroupId);
        groupService.startGroup(chainId, nodeId, startGroupId);
        log.info("end startGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * batch start group.
     */
    @PostMapping("/batchStart")
    public BaseResponse batchStartGroup(@RequestBody @Valid ReqStartGroup req, BindingResult result)
            throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start batchStartGroup startTime:{} groupId:{}", startTime.toEpochMilli(),
                req.getGenerateGroupId());
        groupService.batchStartGroup(req);
        log.info("end batchStartGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * update group.
     */
    @GetMapping("/update")
    public BaseResponse updateGroup() throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start updateGroup startTime:{}", startTime.toEpochMilli());
        groupService.resetGroupList();
        log.info("end updateGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get group general.
     */
    @GetMapping("/general/{chainId}/{groupId}")
    public BaseResponse getGroupGeneral(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId) throws NodeMgrException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupGeneral startTime:{} groupId:{}", startTime.toEpochMilli(),
                groupId);
        GroupGeneral groupGeneral = groupService.queryGroupGeneral(chainId, groupId);
        baseResponse.setData(groupGeneral);
        log.info("end getGroupGeneral useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * query all group.
     */
    @GetMapping("/all/{chainId}")
    public BasePageResponse getAllGroup(@PathVariable("chainId") Integer chainId)
            throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getAllGroup startTime:{}", startTime.toEpochMilli());

        // get group list
        Integer count = groupService.countOfGroup(chainId, null, DataStatus.NORMAL.getValue());
        if (count != null && count > 0) {
            List<TbGroup> groupList =
                    groupService.getGroupList(chainId, DataStatus.NORMAL.getValue());
            pagesponse.setTotalCount(count);
            pagesponse.setData(groupList);
        }

        // reset group
        resetGroupListTask.asyncResetGroupList();

        log.info("end getAllGroup useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(pagesponse));
        return pagesponse;
    }
}
