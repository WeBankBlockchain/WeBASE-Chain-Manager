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
package com.webank.webase.chain.mgr.contract;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.webank.webase.chain.mgr.base.enums.SqlSortType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.entity.CompileInputParam;
import com.webank.webase.chain.mgr.contract.entity.Contract;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.contract.entity.DeployInputParam;
import com.webank.webase.chain.mgr.contract.entity.QueryContractParam;
import com.webank.webase.chain.mgr.contract.entity.RspContractCompile;
import com.webank.webase.chain.mgr.contract.entity.TransactionInputParam;
import com.webank.webase.chain.mgr.front.entity.ContractManageParam;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.transaction.TransactionService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("contract")
public class ContractController extends BaseController {

    @Autowired private ContractService contractService;
    @Autowired private TransactionService transactionService;

    /**
     * compile deployInputParam.
     */
    @PostMapping(value = "/compile")
    public BaseResponse compileContract(@RequestBody @Valid CompileInputParam compileInputParam,
            BindingResult result) throws BaseException, IOException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start compileContract startTime:{} compileInputParam:{}",
                startTime.toEpochMilli(), JsonTools.toJSONString(compileInputParam));

        List<RspContractCompile> rspContractCompile =
                contractService.compileContract(compileInputParam);
        baseResponse.setData(rspContractCompile);

        log.info("end compileContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());

        return baseResponse;
    }

    /**
     * add new contract info.
     */
    @PostMapping(value = "/save")
    public BaseResponse saveContract(@RequestBody @Valid Contract contract, BindingResult result)
            throws BaseException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start saveContract startTime:{} contract:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(contract));

        // add contract row
        TbContract tbContract = contractService.saveContract(contract);

        baseResponse.setData(tbContract);

        log.info("end saveContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }


    /**
     * delete contract by id.
     */
    @DeleteMapping(value = "/{chainId}/{groupId}/{contractId}")
    public BaseResponse deleteContract(@PathVariable("chainId") Integer chainId,
            @PathVariable("groupId") Integer groupId,
            @PathVariable("contractId") Integer contractId) throws BaseException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start deleteContract startTime:{} contractId:{} groupId:{}",
                startTime.toEpochMilli(), contractId, groupId);

        contractService.deleteContract(chainId, contractId, groupId);

        log.info("end deleteContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }


    /**
     * qurey contract info list.
     */
    @PostMapping(value = "/contractList")
    public BasePageResponse queryContractList(@RequestBody @Valid QueryContractParam inputParam,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start contractList. startTime:{} inputParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(inputParam));

        // param
        ContractParam queryParam = new ContractParam();
        BeanUtils.copyProperties(inputParam, queryParam);

        int count = contractService.countOfContract(queryParam);
        if (count > 0) {
            Integer start = Optional.ofNullable(inputParam.getPageNumber())
                    .map(page -> (page - 1) * inputParam.getPageSize()).orElse(0);
            queryParam.setStart(start);
            queryParam.setFlagSortedByTime(SqlSortType.DESC.getValue());
            // query list
            List<TbContract> listOfContract = contractService.qureyContractList(queryParam);

            pagesponse.setData(listOfContract);
            pagesponse.setTotalCount(count);
        }

        log.info("end contractList. useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return pagesponse;
    }


    /**
     * query by contract id.
     */
    @GetMapping(value = "/{contractId}")
    public BaseResponse queryContract(@PathVariable("contractId") Integer contractId)
            throws BaseException, Exception {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} contractId:{}", startTime.toEpochMilli(),
                contractId);

        TbContract contractRow = contractService.getByContractId(contractId);
        baseResponse.setData(contractRow);

        log.info("end queryContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return baseResponse;
    }

    /**
     * deploy deployInputParam.
     */
    @PostMapping(value = "/deploy")
    public BaseResponse deployContract(@RequestBody @Valid DeployInputParam deployInputParam,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryContract startTime:{} deployInputParam:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(deployInputParam));

        TbContract tbContract = contractService.deployContract(deployInputParam);
        baseResponse.setData(tbContract);

        log.info("end deployContract useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());

        return baseResponse;
    }


    /**
     * send transaction.
     */
    @PostMapping(value = "/transaction")
    public BaseResponse sendTransaction(@RequestBody @Valid TransactionInputParam param,
            BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start sendTransaction startTime:{} param:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(param));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object transRsp = contractService.sendTransaction(param);
        baseResponse.setData(transRsp);
        log.info("end sendTransaction useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());

        return baseResponse;
    }

    /**
     * contract status manage.
     */
    @PostMapping(value = "/statusManage")
    public Object statusManage(@RequestBody @Valid ContractManageParam param, BindingResult result)
            throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start statusManage startTime:{} param:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(param));

        Object contractStatusManageResult = contractService.statusManage(param);

        log.info("end statusManage useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());
        return contractStatusManageResult;
    }

    /**
     * deploy deployInputParam.
     */
    @GetMapping(value = "/deployByTransaction/{contractId}")
    public Object deployByTransaction(
            @PathVariable int contractId,
            @RequestParam String signUserId
    ) throws BaseException {
        Instant startTime = Instant.now();
        log.info("start deployByTransaction startTime:{}, signUserId:{}, contractId:{}",
                startTime.toEpochMilli(), signUserId, contractId);

        Object result = transactionService.deployContract(contractId, signUserId);
        log.info("end deployByTransaction useTime:{}", Duration.between(startTime, Instant.now()).toMillis());

        return result;
    }
}
