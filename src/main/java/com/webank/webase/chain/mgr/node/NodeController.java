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
package com.webank.webase.chain.mgr.node;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.TransactionCount;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.node.entity.RspNodeInfoVo;
import com.webank.webase.chain.mgr.node.entity.RspSimpleNodeInfoVo;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock.Block;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for node data.
 */
@Log4j2
@RestController
@RequestMapping("node")
public class NodeController extends BaseController {

    @Autowired
    private NodeService nodeService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontManager frontManager;
    @Autowired
    private TbFrontMapper frontMapper;
    @Autowired
    private FrontInterfaceService frontInterfaceService;


    @GetMapping(value = "/all")
    public BaseResponse allNode() throws BaseException {
        Instant startTime = Instant.now();
        log.info("start get all node startTime:{} ", startTime.toEpochMilli());

        // param
        List<TbNode> nodeList = nodeService.qureyNodeList(new NodeParam());

        log.info("end get all node useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(nodeList));
        return BaseResponse.success(nodeList);
    }

    /**
     * qurey node info list.
     */
    @GetMapping(value = "/nodeList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryNodeList(@PathVariable("chainId") Integer chainId,
                                          @PathVariable("groupId") Integer groupId,
                                          @PathVariable("pageNumber") Integer pageNumber,
                                          @PathVariable("pageSize") Integer pageSize,
                                          @RequestParam(value = "agencyId", required = false) Integer agencyId,
                                          @RequestParam(value = "frontPeerName", required = false) String frontPeerName,
                                          @RequestParam(value = "nodeId", required = false) String nodeId) throws BaseException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start queryNodeList startTime:{} groupId:{} pageNumber:{} pageSize:{} agencyId:{} frontPeerName:{} nodeId:{}",
                startTime.toEpochMilli(), groupId, pageNumber, pageSize, agencyId, frontPeerName, nodeId);

        int newGroupId = groupId == null || groupId <= 0 ? ConstantProperties.DEFAULT_GROUP_ID : groupId;

        // check node status before query
        try {
            nodeService.checkAndUpdateNodeStatus(chainId);
        } catch (Exception ex) {
            log.error("fail to update node status for exception.", ex);
        }

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(newGroupId);
        queryParam.setNodeId(nodeId);
        queryParam.setPageSize(pageSize);
        if (Objects.nonNull(agencyId) || StringUtils.isNotBlank(frontPeerName) || StringUtils.isNotBlank(nodeId)) {
            List<TbFront> frontList = frontManager.queryFrontByAgencyIdAndFrontPeerNameAndNodeId(agencyId, frontPeerName, nodeId);
            if (CollectionUtils.isEmpty(frontList)) {
                log.info("finish queryNodeList. not fount front by agencyId:{} frontPeerName:{} nodeId:{}", agencyId, frontPeerName, nodeId);
                return pagesponse;
            }

            Set<String> nodeIds = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
            queryParam.setNodeIds(nodeIds);
        }

        Integer count = nodeService.countOfNode(queryParam);
        if (count != null && count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);

            List<TbNode> listOfNode = nodeService.qureyNodeList(queryParam);
            List<String> nodeIdList = listOfNode.stream().map(node -> node.getNodeId()).distinct().collect(Collectors.toList());
            List<TbFront> frontList = frontService.selectFrontByNodeIdListAndChain(chainId, nodeIdList);
            List<RspNodeInfoVo> rspNodeInfoVoList = new ArrayList<>();
            for (TbNode tbNode : listOfNode) {
                RspNodeInfoVo rspNodeInfoVo = new RspNodeInfoVo();
                BeanUtils.copyProperties(tbNode, rspNodeInfoVo);
                if (CollectionUtils.isNotEmpty(frontList)) {
                    frontList.stream()
                            .filter(front -> front.getNodeId().equals(tbNode.getNodeId()))
                            .findFirst()
                            .ifPresent(front -> {
                                rspNodeInfoVo.setFrontPeerName(front.getFrontPeerName());
                                rspNodeInfoVo.setNodeIp(front.getFrontIp());
                                rspNodeInfoVo.setAgency(front.getExtAgencyId());
                                rspNodeInfoVo.setAgencyName(front.getAgency());
                            });
                    rspNodeInfoVoList.add(rspNodeInfoVo);
                }
            }
            pagesponse.setData(rspNodeInfoVoList);
            pagesponse.setTotalCount(count);
        }


        log.info("end queryNodeList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }


    /**
     * @param chainId
     * @param groupId
     * @param agencyId
     * @param nodeTypes
     * @return
     * @throws BaseException
     */
    @GetMapping(value = "/nodeIdList/{chainId}/{groupId}")
    public BaseResponse queryNodeIdList(@PathVariable("chainId") Integer chainId,
                                        @PathVariable("groupId") Integer groupId,
                                        @RequestParam(value = "agencyId", required = false) Integer agencyId,
                                        @RequestParam(value = "nodeTypes", required = false) List<String> nodeTypes) throws BaseException {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start queryNodeIdList startTime:{} chainId:{} groupId:{} agencyId:{} nodeTypes:{}", startTime.toEpochMilli(), chainId, groupId, agencyId, JsonTools.objToString(nodeTypes));

        //list nodeId by type
        List<String> nodeIds = nodeService.getNodeIdByTypes(chainId, groupId, nodeTypes);
        if (CollectionUtils.isEmpty(nodeIds)) {
            log.info("finish exec method [queryNodeIdList]. not found record by chain:{} group:{} nodeTypes:{}", chainId, groupId, JsonTools.objToString(nodeTypes));
            return baseResponse;
        }

        //nodeId of agency
        Set<String> finalNodes = nodeIds.stream().collect(Collectors.toSet());
        if (Objects.nonNull(agencyId)) {
            List<TbFront> frontList = frontManager.listFrontByAgency(agencyId);
            if (CollectionUtils.isEmpty(frontList)) {
                log.info("finish queryNodeIdList. not fount front by agencyId:{}", agencyId);
                return baseResponse;
            }

            Set<String> nodeIdOfFronts = frontList.stream().map(front -> front.getNodeId()).collect(Collectors.toSet());
            finalNodes = nodeIds.stream().filter(id -> nodeIdOfFronts.contains(id)).collect(Collectors.toSet());
        }

        List<TbFront> frontList = frontMapper.selectByChainId(chainId);
        List<RspSimpleNodeInfoVo> rspList = new ArrayList<>();
        for (String nodeId : finalNodes) {
            RspSimpleNodeInfoVo rspNodeInfo = new RspSimpleNodeInfoVo();
            rspNodeInfo.setNodeId(nodeId);
            rspNodeInfo.setFrontPeerName(frontList.stream().filter(front -> nodeId.equals(front.getNodeId())).findFirst().map(fn -> fn.getFrontPeerName()).orElse(null));
            rspList.add(rspNodeInfo);
        }
        baseResponse.setData(rspList);

        log.info("end queryNodeList useTime:{} result:{}", Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;

    }


    /**
     * get block number.
     */
    @GetMapping("/getBlockNumber/{chainId}/{groupId}/{nodeId}")
    public BaseResponse getBlockNumber(@PathVariable("chainId") Integer chainId,
                                       @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId)
            throws BaseException {
        Instant startTime = Instant.now();
        log.info("start getBlockNumber startTime:{} chainId:{} groupId:{}",
                startTime.toEpochMilli(), groupId, groupId);

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getBlockNumber node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        BigInteger blockNumber = frontInterfaceService.getBlockNumberFromSpecificFront(tbFront.getFrontPeerName(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId);
        baseResponse.setData(blockNumber);

        log.info("end getBlockNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get block by number.
     */
    @GetMapping("/getBlockByNumber/{chainId}/{groupId}/{nodeId}/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable("chainId") Integer chainId,
                                         @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
                                         @PathVariable("blockNumber") BigInteger blockNumber) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start getBlockByNumber startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, blockNumber);

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getBlockByNumber node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Block blockInfo = frontInterfaceService.getBlockByNumberFromSpecificFront(tbFront.getFrontPeerName(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId, blockNumber);
        baseResponse.setData(blockInfo);

        log.info("end getBlockByNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get total transaction count.
     */
    @GetMapping("/getTotalTransactionCount/{chainId}/{groupId}/{nodeId}")
    public BaseResponse getTotalTransactionCount(@PathVariable("chainId") Integer chainId,
                                                 @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId)
            throws BaseException {
        Instant startTime = Instant.now();
        log.info("start getTotalTransactionCount startTime:{} chainId:{} groupId:{}",
                startTime.toEpochMilli(), groupId, groupId);

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getTotalTransactionCount node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionCount transactionCount =
                frontInterfaceService.getTotalTransactionCountFromSpecificFront(tbFront.getFrontPeerName(),
                        tbFront.getFrontIp(), tbFront.getFrontPort(), groupId);
        baseResponse.setData(transactionCount);

        log.info("end getTotalTransactionCount useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction by hash.
     */
    @GetMapping("/getTransactionByHash/{chainId}/{groupId}/{nodeId}/{transHash}")
    public BaseResponse getTransactionByHash(@PathVariable("chainId") Integer chainId,
                                             @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
                                             @PathVariable("transHash") String transHash) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start getTransactionByHash startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, transHash);

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getTransactionByHash node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Transaction transaction = frontInterfaceService.getTransactionByHashFromSpecificFront(tbFront.getFrontPeerName(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId, transHash);
        baseResponse.setData(transaction);

        log.info("end getTransactionByHash useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction receipt by hash.
     */
    @GetMapping("/getTransactionReceipt/{chainId}/{groupId}/{nodeId}/{transHash}")
    public BaseResponse getTransactionReceipt(@PathVariable("chainId") Integer chainId,
                                              @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId,
                                              @PathVariable("transHash") String transHash) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start getTransactionReceipt startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, transHash);

        // get front
        TbFront tbFront = frontService.getByChainIdAndNodeId(chainId, nodeId);
        if (tbFront == null) {
            log.error("fail getTransactionReceipt node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionReceipt transactionReceipt =
                frontInterfaceService.getTransactionReceiptFromSpecificFront(tbFront.getFrontPeerName(), tbFront.getFrontIp(),
                        tbFront.getFrontPort(), groupId, transHash);
        baseResponse.setData(transactionReceipt);

        log.info("end getTransactionReceipt useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}
