/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.chain.mgr.node;

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.TbFront;
import com.webank.webase.chain.mgr.front.entity.TransactionCount;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.node.entity.NodeParam;
import com.webank.webase.chain.mgr.node.entity.TbNode;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.fisco.bcos.web3j.protocol.core.methods.response.BcosBlock.Block;
import org.fisco.bcos.web3j.protocol.core.methods.response.Transaction;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
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
    private FrontInterfaceService frontInterfaceService;

    /**
     * qurey node info list.
     */
    @GetMapping(value = "/nodeList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryNodeList(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId,
            @PathVariable("pageNumber") Integer pageNumber,
            @PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "nodeId", required = false) String nodeId) throws BaseException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start queryNodeList startTime:{} groupId:{} pageNumber:{} pageSize:{} nodeName:{}",
                startTime.toEpochMilli(), groupId, pageNumber, pageSize, nodeId);

        // check node status before query
        nodeService.checkAndUpdateNodeStatus(chainId, groupId);

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        queryParam.setNodeId(nodeId);
        queryParam.setPageSize(pageSize);
        Integer count = nodeService.countOfNode(queryParam);
        if (count != null && count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);

            List<TbNode> listOfnode = nodeService.qureyNodeList(queryParam);
            pagesponse.setData(listOfnode);
            pagesponse.setTotalCount(count);
        }

        log.info("end queryNodeList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(pagesponse));
        return pagesponse;
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
        BigInteger blockNumber = frontInterfaceService.getBlockNumberFromSpecificFront(
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId);
        baseResponse.setData(blockNumber);

        log.info("end getBlockNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
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
        Block blockInfo = frontInterfaceService.getBlockByNumberFromSpecificFront(
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId, blockNumber);
        baseResponse.setData(blockInfo);

        log.info("end getBlockByNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
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
                frontInterfaceService.getTotalTransactionCountFromSpecificFront(
                        tbFront.getFrontIp(), tbFront.getFrontPort(), groupId);
        baseResponse.setData(transactionCount);

        log.info("end getTotalTransactionCount useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
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
        Transaction transaction = frontInterfaceService.getTransactionByHashFromSpecificFront(
                tbFront.getFrontIp(), tbFront.getFrontPort(), groupId, transHash);
        baseResponse.setData(transaction);

        log.info("end getTransactionByHash useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
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
                frontInterfaceService.getTransactionReceiptFromSpecificFront(tbFront.getFrontIp(),
                        tbFront.getFrontPort(), groupId, transHash);
        baseResponse.setData(transactionReceipt);

        log.info("end getTransactionReceipt useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}
