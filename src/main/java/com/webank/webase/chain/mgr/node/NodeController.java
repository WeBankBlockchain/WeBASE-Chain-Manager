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
package com.webank.webase.chain.mgr.node;

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.NodeMgrException;
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
    private FrontInterfaceService frontInterfaceService;

    /**
     * qurey node info list.
     */
    @GetMapping(value = "/nodeList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryNodeList(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId,
            @PathVariable("pageNumber") Integer pageNumber,
            @PathVariable("pageSize") Integer pageSize,
            @RequestParam(value = "nodeName", required = false) String nodeName)
            throws NodeMgrException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info(
                "start queryNodeList startTime:{} groupId:{}  pageNumber:{} pageSize:{} nodeName:{}",
                startTime.toEpochMilli(), groupId, pageNumber, pageSize, nodeName);

        // param
        NodeParam queryParam = new NodeParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        queryParam.setNodeName(nodeName);
        queryParam.setPageSize(pageSize);

        // check node status before query
        nodeService.checkAndUpdateNodeStatus(chainId, groupId);
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
     * get node info.
     */
    @GetMapping(value = "/nodeInfo/{chainId}/{groupId}/{nodeId}")
    public BaseResponse getNodeInfo(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("nodeId") String nodeId)
            throws NodeMgrException {

        Instant startTime = Instant.now();
        log.info("start getNodeInfo startTime:{} nodeId:{}", startTime.toEpochMilli(), nodeId);

        // param
        NodeParam param = new NodeParam();
        param.setChainId(chainId);
        param.setGroupId(groupId);
        param.setNodeId(nodeId);;

        // query node row
        TbNode tbNode = nodeService.queryNodeInfo(param);

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(tbNode);

        log.info("end getNodeInfo useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get block number.
     */
    @GetMapping("/getBlockNumber/{chainId}/{groupId}")
    public BaseResponse getBlockNumber(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getBlockNumber startTime:{} chainId:{} groupId:{}",
                startTime.toEpochMilli(), groupId, groupId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        BigInteger blockNumber = frontInterfaceService.getLatestBlockNumber(chainId, groupId);
        baseResponse.setData(blockNumber);
        log.info("end getBlockNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get block by number.
     */
    @GetMapping("/getBlockByNumber/{chainId}/{groupId}/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId,
            @PathVariable("blockNumber") BigInteger blockNumber) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getBlockByNumber startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, blockNumber);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Block blockInfo = frontInterfaceService.getBlockByNumber(chainId, groupId, blockNumber);
        baseResponse.setData(blockInfo);
        log.info("end getBlockByNumber useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get total transaction count.
     */
    @GetMapping("/getTotalTransactionCount/{chainId}/{groupId}")
    public BaseResponse getTotalTransactionCount(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId) throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTotalTransactionCount startTime:{} chainId:{} groupId:{}",
                startTime.toEpochMilli(), groupId, groupId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionCount transactionCount =
                frontInterfaceService.getTotalTransactionCount(chainId, groupId);
        baseResponse.setData(transactionCount);
        log.info("end getTotalTransactionCount useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction by hash.
     */
    @GetMapping("/getTransactionByHash/{chainId}/{groupId}/{transHash}")
    public BaseResponse getTransactionByHash(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("transHash") String transHash)
            throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransactionByHash startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Transaction transaction =
                frontInterfaceService.getTransactionByHash(chainId, groupId, transHash);
        baseResponse.setData(transaction);
        log.info("end getTransactionByHash useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction receipt by hash.
     */
    @GetMapping("/getTransactionReceipt/{chainId}/{groupId}/{transHash}")
    public BaseResponse getTransactionReceipt(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId, @PathVariable("transHash") String transHash)
            throws NodeMgrException {
        Instant startTime = Instant.now();
        log.info("start getTransactionReceipt startTime:{} groupId:{} blockNumber:{}",
                startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionReceipt transactionReceipt =
                frontInterfaceService.getTransactionReceipt(chainId, groupId, transHash);
        baseResponse.setData(transactionReceipt);
        log.info("end getTransactionReceipt useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JSON.toJSONString(baseResponse));
        return baseResponse;
    }
}
