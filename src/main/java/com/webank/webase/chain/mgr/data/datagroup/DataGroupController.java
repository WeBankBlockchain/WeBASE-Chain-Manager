/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.chain.mgr.data.datagroup;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseQueryParam;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DataStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.contract.ContractManager;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.data.block.entity.BlockListParam;
import com.webank.webase.chain.mgr.data.block.entity.TbBlock;
import com.webank.webase.chain.mgr.data.datagroup.entity.GroupGeneral;
import com.webank.webase.chain.mgr.data.datagroup.entity.ToggleInfo;
import com.webank.webase.chain.mgr.data.datagroup.entity.TranxCount;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.data.transaction.entity.TransListParam;
import com.webank.webase.chain.mgr.data.txndaily.entity.TbTxnDaily;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import com.webank.webase.chain.mgr.util.JsonTools;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing group information.
 */
@Log4j2
@RestController
@RequestMapping("datagroup")
public class DataGroupController extends BaseController {

    @Autowired
    private DataGroupService dataGroupService;
    @Autowired
    private ContractManager contractManager;
    @Autowired
    private GroupManager groupManager;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private ConstantProperties cProperties;
    @Autowired
    private NodeService nodeService;

    /**
     * toggle of pull data
     */
    @GetMapping("/togglePullData")
    public BaseResponse getTogglePullData() {
        Instant startTime = Instant.now();
        log.info("start getTogglePullData.");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // remove
        baseResponse.setData(cProperties.isIfPullData());

        log.info("end getTogglePullData useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * update toggle of pull data
     */
    @PostMapping("/togglePullData")
    public BaseResponse updateTogglePullData(@RequestBody ToggleInfo toggleInfo) {
        Instant startTime = Instant.now();
        log.info("start updateTogglePullData.");
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // update
        cProperties.setIfPullData(toggleInfo.isEnable());

        log.info("end updateTogglePullData useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * refresh sub table.
     */
    @GetMapping("/refresh")
    public BaseResponse refreshSubTable() {
        BaseResponse response = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start refreshSubTable.");
        dataGroupService.refreshSubTable();
        log.info("end refreshSubTable useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return response;
    }

    /**
     * query all group.
     */
    @GetMapping("/list")
    public BasePageResponse getGroupList(
        @RequestParam(value = "chainId", required = false) Integer chainId)
        throws BaseException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getGroupList.");

        // get group list
        int count = groupManager.countByChainIdAndGroupStatus(chainId, DataStatus.NORMAL.getValue());
        if (count > 0) {
            List<TbGroup> groupList =
                groupService.getGroupList(chainId, DataStatus.NORMAL.getValue());
            pagesponse.setTotalCount(count);
            pagesponse.setData(groupList);
        }

        log.info("end getGroupList useTime:{}",
            Duration.between(startTime, Instant.now()).toMillis());
        return pagesponse;
    }


    /**
     * query node info list.
     */
    @GetMapping(value = "/nodeList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryNodeList(@PathVariable("chainId") Integer chainId,
                                          @PathVariable("groupId") Integer groupId,
                                          @PathVariable("pageNumber") Integer pageNumber,
                                          @PathVariable("pageSize") Integer pageSize) throws BaseException {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryNodeList.");

        // check node status before query
        try {
            nodeService.checkAndUpdateNodeStatus(chainId);
        } catch (Exception ex) {
            log.error("fail to update node status for exception.", ex);
        }

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        // param
        BaseQueryParam queryParam = new BaseQueryParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        int count = dataGroupService.countOfNode(queryParam);
        if (count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);
            queryParam.setPageSize(pageSize);
            List<TbNode> listOfnode = dataGroupService.queryNodeList(queryParam);
            pagesponse.setData(listOfnode);
            pagesponse.setTotalCount(count);
        }

        log.info("end queryNodeList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pagesponse;
    }


    /**
     * get group general.
     */
    @GetMapping("/general/{chainId}/{groupId}")
    public BaseResponse getGroupGeneral(@PathVariable("chainId") Integer chainId,
                                        @PathVariable("groupId") Integer groupId) throws BaseException {
        Instant startTime = Instant.now();
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start getGroupGeneral. groupId:{}", groupId);

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        GroupGeneral groupGeneral = dataGroupService.queryGroupGeneral(chainId, groupId);

        baseResponse.setData(groupGeneral);
        log.info("end getGroupGeneral useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * get trans daily.
     */
    @GetMapping("/txnDaily/{chainId}/{groupId}")
    public BaseResponse getTransDaily(@PathVariable("chainId") Integer chainId,
                                      @PathVariable("groupId") Integer groupId) {
        BaseResponse pagesponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start getTransDaily.");

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        // query txn daily
        List<TbTxnDaily> listTrans = dataGroupService.getTransDaily(chainId, groupId);
        pagesponse.setData(listTrans);

        log.info("end getTransDaily useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pagesponse;
    }

    /**
     * query block list.
     */
    @GetMapping(value = "/blockList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryBlockList(@PathVariable("chainId") Integer chainId,
                                           @PathVariable("groupId") Integer groupId,
                                           @PathVariable("pageNumber") Integer pageNumber,
                                           @PathVariable("pageSize") Integer pageSize,
                                           @RequestParam(value = "blockHash", required = false) String blockHash,
                                           @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber)
            throws BaseException {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryBlockList.");

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        BlockListParam queryParam = new BlockListParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        queryParam.setBlockHash(blockHash);
        queryParam.setBlockNumber(blockNumber);
        int count = dataGroupService.countOfBlock(queryParam);
        if (count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);
            queryParam.setPageSize(pageSize);
            List<TbBlock> blockList = dataGroupService.queryBlockList(queryParam);
            pageResponse.setData(blockList);
            pageResponse.setTotalCount(count);
        }
        log.info("end queryBlockList useTime:{}.",
                Duration.between(startTime, Instant.now()).toMillis());
        return pageResponse;
    }

    /**
     * query trans list.
     */
    @GetMapping(value = "/transList/{chainId}/{groupId}/{pageNumber}/{pageSize}")
    public BasePageResponse queryTransList(@PathVariable("chainId") Integer chainId,
                                           @PathVariable("groupId") Integer groupId,
                                           @PathVariable("pageNumber") Integer pageNumber,
                                           @PathVariable("pageSize") Integer pageSize,
                                           @RequestParam(value = "transHash", required = false) String transHash,
                                           @RequestParam(value = "blockNumber", required = false) BigInteger blockNumber) {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryTransList.");

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        TransListParam queryParam = new TransListParam();
        queryParam.setChainId(chainId);
        queryParam.setGroupId(groupId);
        queryParam.setTransHash(transHash);
        queryParam.setBlockNumber(blockNumber);
        int count = dataGroupService.countOfTrans(queryParam);
        if (count > 0) {
            Integer start =
                    Optional.ofNullable(pageNumber).map(page -> (page - 1) * pageSize).orElse(null);
            queryParam.setStart(start);
            queryParam.setPageSize(pageSize);
            List<TbTransaction> transList = dataGroupService.queryTransList(queryParam);
            pageResponse.setData(transList);
            pageResponse.setTotalCount(count);
        }

        log.info("end queryTransList useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pageResponse;
    }


    /**
     * get transaction receipt.
     */
    @GetMapping("/transactionReceipt/{chainId}/{groupId}/{transHash}")
    public BaseResponse getTransReceipt(@PathVariable("chainId") Integer chainId,
        @PathVariable("groupId") Integer groupId,
        @PathVariable("transHash") String transHash) {
        Instant startTime = Instant.now();
        log.info("start getTransReceipt startTime:{} groupId:{} transaction:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TransactionReceipt transReceipt = frontInterface.getTransReceipt(chainId, groupId, transHash);
        baseResponse.setData(transReceipt);
        log.info("end getTransReceipt useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get transaction info by hash.
     */
    @GetMapping("/transInfo/{chainId}/{groupId}/{transHash}")
    public BaseResponse getTransaction(@PathVariable("chainId") Integer chainId,
        @PathVariable("groupId") Integer groupId,
        @PathVariable("transHash") String transHash) {
        Instant startTime = Instant.now();
        log.info("start getTransaction startTime:{} groupId:{} transaction:{}",
            startTime.toEpochMilli(), groupId, transHash);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Transaction transInfo = frontInterface.getTransaction(chainId, groupId, transHash);
        baseResponse.setData(transInfo);
        log.info("end getTransaction useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * get block by number.
     */
    @GetMapping("/blockByNumber/{chainId}/{groupId}/{blockNumber}")
    public BaseResponse getBlockByNumber(@PathVariable("chainId") Integer chainId,
        @PathVariable("groupId") Integer groupId,
        @PathVariable("blockNumber") BigInteger blockNumber) {

        Instant startTime = Instant.now();
        log.info("start getBlockByNumber startTime:{} groupId:{} blockNumber:{}",
            startTime.toEpochMilli(), groupId, blockNumber);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Block blockInfo = frontInterface.getBlockByNumber(chainId, groupId, blockNumber);
        baseResponse.setData(blockInfo);
        log.info("end getBlockByNumber useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(), JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query count of trans by contract
     */
    @GetMapping(value = "/transCountByContract/{chainId}/{groupId}/{contractName}")
    public BaseResponse queryTransCountByContract(@PathVariable("chainId") Integer chainId,
                                                  @PathVariable("groupId") Integer groupId,
                                                  @PathVariable("contractName") String contractName) {
        BaseResponse pageResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryTransCountByContract.");

        // check groupId
        groupManager.requireGroupExist(chainId, groupId);

        ContractParam param = new ContractParam();
        param.setChainId(chainId);
        param.setGroupId(groupId);
        param.setContractName(contractName);
        log.info("get params --> " + chainId + "-->" + groupId + "-->" + contractName);
        TbContract contract = contractManager.queryContract(param);
        if (contract == null) {
            throw new BaseException(ConstantCode.CONTRACT_NOT_EXISTS);
        }

        TranxCount count = new TranxCount();
        count.setChainId(chainId);
        count.setGroupId(groupId);
        count.setContractName(contractName);
        count.setTranxCount(dataGroupService.queryTransCountByContract(chainId, groupId, contract.getContractAddress()));
        pageResponse.setData(count);


        log.info("end queryTransCountByContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pageResponse;
    }


}
