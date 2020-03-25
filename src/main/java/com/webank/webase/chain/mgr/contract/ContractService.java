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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.contract.entity.CompileInputParam;
import com.webank.webase.chain.mgr.contract.entity.Contract;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.contract.entity.DeployInputParam;
import com.webank.webase.chain.mgr.contract.entity.RspContractCompile;
import com.webank.webase.chain.mgr.contract.entity.TbContract;
import com.webank.webase.chain.mgr.contract.entity.TransactionInputParam;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.ContractManageParam;
import com.webank.webase.chain.mgr.front.entity.TbFront;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.FrontRestTools;
import com.webank.webase.chain.mgr.frontinterface.entity.ContractStatusManageResult;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class ContractService {

    @Autowired
    private ContractMapper contractMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontService frontService;
    @Autowired
    private ConstantProperties constants;

    /**
     * compile contract.
     * 
     */
    @SuppressWarnings("unchecked")
    public List<RspContractCompile> compileContract(CompileInputParam inputParam)
            throws BaseException, IOException {

        // check front
        TbFront tbFront =
                frontService.getByChainIdAndNodeId(inputParam.getChainId(), inputParam.getNodeId());
        if (tbFront == null) {
            log.error("fail deployContract node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("contractZipBase64", inputParam.getContractZipBase64());
        List<RspContractCompile> compileInfos = frontInterface.postToSpecificFront(
                Integer.MIN_VALUE, tbFront.getFrontIp(), tbFront.getFrontPort(),
                FrontRestTools.URI_MULTI_CONTRACT_COMPILE, params, List.class);

        log.debug("end compileContract.");
        return compileInfos;
    }

    /**
     * add new contract data.
     */
    public TbContract saveContract(Contract contract) throws BaseException {
        log.debug("start addContractInfo Contract:{}", JSON.toJSONString(contract));
        TbContract tbContract;
        if (contract.getContractId() == null) {
            tbContract = newContract(contract);// new
        } else {
            tbContract = updateContract(contract);// update
        }
        return tbContract;
    }


    /**
     * save new contract.
     */
    private TbContract newContract(Contract contract) {
        // check contract not exist.
        verifyContractNotExist(contract.getChainId(), contract.getGroupId(),
                contract.getContractName(), contract.getContractPath());

        // add to database.
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(contract, tbContract);
        contractMapper.add(tbContract);
        return queryByContractId(tbContract.getContractId());
    }


    /**
     * update contract.
     */
    private TbContract updateContract(Contract contract) {
        // check not deploy
        TbContract tbContract = verifyContractNotDeploy(contract.getChainId(),
                contract.getContractId(), contract.getGroupId());
        // check contractName
        verifyContractNameNotExist(contract.getChainId(), contract.getGroupId(),
                contract.getContractPath(), contract.getContractName(), contract.getContractId());
        BeanUtils.copyProperties(contract, tbContract);
        contractMapper.update(tbContract);
        return queryByContractId(tbContract.getContractId());
    }

    /**
     * delete contract by contractId.
     */
    public void deleteContract(int chainId, int contractId, int groupId) throws BaseException {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        verifyContractNotDeploy(chainId, contractId, groupId);
        // remove
        contractMapper.remove(contractId);
        log.debug("end deleteContract");
    }

    /**
     * delete contract by chainId.
     */
    public void deleteContractByChainId(int chainId) throws BaseException {
        log.debug("start deleteContractByChainId chainId:{}", chainId);
        if (chainId == 0) {
            return;
        }
        // remove
        contractMapper.removeByChainId(chainId);
        log.debug("end deleteContractByChainId");
    }

    /**
     * delete by groupId
     */
    public void deleteByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        contractMapper.removeByGroupId(chainId, groupId);
    }

    /**
     * query contract list.
     */
    public List<TbContract> qureyContractList(ContractParam param) throws BaseException {
        log.debug("start qureyContractList ContractListParam:{}", JSON.toJSONString(param));

        // query contract list
        List<TbContract> listOfContract = contractMapper.listOfContract(param);

        log.debug("end qureyContractList listOfContract:{}", JSON.toJSONString(listOfContract));
        return listOfContract;
    }


    /**
     * query count of contract.
     */
    public int countOfContract(ContractParam param) throws BaseException {
        log.debug("start countOfContract ContractListParam:{}", JSON.toJSONString(param));
        try {
            return contractMapper.countOfContract(param);
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract by contract id.
     */
    public TbContract queryByContractId(Integer contractId) throws BaseException {
        log.debug("start queryContract contractId:{}", contractId);
        try {
            TbContract contractRow = contractMapper.queryByContractId(contractId);
            log.debug("start queryContract contractId:{} contractRow:{}", contractId,
                    JSON.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

    }

    /**
     * query DeployInputParam By Address.
     */
    public List<TbContract> queryContractByBin(Integer groupId, String contractBin)
            throws BaseException {
        try {
            if (StringUtils.isEmpty(contractBin)) {
                return null;
            }
            List<TbContract> contractRow = contractMapper.queryContractByBin(groupId, contractBin);
            log.debug("start queryContractByBin:{}", contractBin, JSON.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail queryContractByBin", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract info.
     */
    public TbContract queryContract(ContractParam queryParam) {
        log.debug("start queryContract. queryParam:{}", JSON.toJSONString(queryParam));
        TbContract tbContract = contractMapper.queryContract(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JSON.toJSONString(queryParam),
                JSON.toJSONString(tbContract));
        return tbContract;
    }

    /**
     * deploy contract.
     */
    public TbContract deployContract(DeployInputParam inputParam) throws BaseException {
        log.info("start deployContract. inputParam:{}", JSON.toJSONString(inputParam));
        int groupId = inputParam.getGroupId();
        String contractName = inputParam.getContractName();
        // check contract
        verifyContractNotDeploy(inputParam.getChainId(), inputParam.getContractId(),
                inputParam.getGroupId());
        // check contractName
        verifyContractNameNotExist(inputParam.getChainId(), inputParam.getGroupId(),
                inputParam.getContractPath(), contractName, inputParam.getContractId());
        // check front
        TbFront tbFront =
                frontService.getByChainIdAndNodeId(inputParam.getChainId(), inputParam.getNodeId());
        if (tbFront == null) {
            log.error("fail deployContract node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        JSONArray abiArray = JSONArray.parseArray(inputParam.getContractAbi());
        if (abiArray == null || abiArray.isEmpty()) {
            log.info("fail deployContract. abi is empty");
            throw new BaseException(ConstantCode.CONTRACT_ABI_EMPTY);
        }

        // deploy param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("user", inputParam.getUser());
        params.put("contractName", contractName);
        params.put("abiInfo", JSONArray.parseArray(inputParam.getContractAbi()));
        params.put("bytecodeBin", inputParam.getBytecodeBin());
        params.put("funcParam", inputParam.getConstructorParams());
        params.put("useAes", constants.getIsPrivateKeyEncrypt());

        // deploy
        String contractAddress = frontInterface.postToSpecificFront(groupId, tbFront.getFrontIp(),
                tbFront.getFrontPort(), FrontRestTools.URI_CONTRACT_DEPLOY, params, String.class);
        if (StringUtils.isBlank(contractAddress)) {
            log.error("fail deploy, contractAddress is empty");
            throw new BaseException(ConstantCode.CONTRACT_DEPLOY_FAIL);
        }

        // save contract
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(inputParam, tbContract);
        tbContract.setContractAddress(contractAddress);
        tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        tbContract.setDeployTime(LocalDateTime.now());
        contractMapper.update(tbContract);

        log.debug("end deployContract. contractId:{} groupId:{} contractAddress:{}",
                tbContract.getContractId(), groupId, contractAddress);
        return queryByContractId(tbContract.getContractId());
    }

    /**
     * send transaction.
     */
    public Object sendTransaction(TransactionInputParam inputParam) throws BaseException {
        log.debug("start sendTransaction. param:{}", JSON.toJSONString(inputParam));
        if (Objects.isNull(inputParam)) {
            log.info("fail sendTransaction. request param is null");
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO);
        }
        // check front
        TbFront tbFront =
                frontService.getByChainIdAndNodeId(inputParam.getChainId(), inputParam.getNodeId());
        if (tbFront == null) {
            log.error("fail sendTransaction node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }
        // check contract deploy
        TbContract tbContract = verifyContractDeploy(inputParam.getChainId(),
                inputParam.getContractId(), inputParam.getGroupId());

        // transaction param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", inputParam.getGroupId());
        params.put("user", inputParam.getUser());
        params.put("contractName", inputParam.getContractName());
        params.put("contractAddress", tbContract.getContractAddress());
        params.put("contractAbi", JSONArray.parseArray(inputParam.getContractAbi()));
        params.put("funcName", inputParam.getFuncName());
        params.put("funcParam", inputParam.getFuncParam());
        params.put("useAes", constants.getIsPrivateKeyEncrypt());

        // send transaction
        Object frontRsp = frontInterface.postToSpecificFront(inputParam.getGroupId(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), FrontRestTools.URI_SEND_TRANSACTION,
                params, Object.class);
        log.debug("end sendTransaction. frontRsp:{}", JSON.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * contract manage.
     */
    public BaseResponse statusManage(ContractManageParam inputParam) throws BaseException {
        log.debug("start statusManage. param:{}", JSON.toJSONString(inputParam));
        // check front
        TbFront tbFront =
                frontService.getByChainIdAndNodeId(inputParam.getChainId(), inputParam.getNodeId());
        if (tbFront == null) {
            log.error("fail statusManage node front not exists.");
            throw new BaseException(ConstantCode.NODE_NOT_EXISTS);
        }

        // transaction param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", inputParam.getGroupId());
        params.put("contractAddress", inputParam.getContractAddress());
        params.put("handleType", inputParam.getHandleType());
        params.put("fromAddress", inputParam.getUser());
        params.put("grantAddress", inputParam.getGrantAddress());
        params.put("useAes", constants.getIsPrivateKeyEncrypt());

        // send transaction
        ContractStatusManageResult contractStatusManageResult = frontInterface.postToSpecificFront(inputParam.getGroupId(),
                tbFront.getFrontIp(), tbFront.getFrontPort(),
                FrontRestTools.URI_CONTRACT_STATUS_MANAGE, params, ContractStatusManageResult.class);
        
        if (contractStatusManageResult.getCode() != 0) {
            log.error("fail statusManage message:{}.", contractStatusManageResult.getMsg());
            throw new BaseException(contractStatusManageResult.getCode(),
                    contractStatusManageResult.getMsg());
        }
        
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(contractStatusManageResult.getData());
        log.debug("end statusManage. baseResponse:{}", JSON.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExist(int chainId, int groupId, String name, String path) {
        ContractParam param = new ContractParam(chainId, groupId, path, name);
        TbContract contract = queryContract(param);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new BaseException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    private TbContract verifyContractNotDeploy(int chainId, int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(chainId, contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() == contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new BaseException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }
        return contract;
    }

    /**
     * verify that the contract had bean deployed.
     */
    private TbContract verifyContractDeploy(int chainId, int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(chainId, contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() != contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new BaseException(ConstantCode.CONTRACT_NOT_DEPLOY);
        }
        return contract;
    }

    /**
     * verify that the contractId is exist.
     */
    private TbContract verifyContractIdExist(int chainId, int contractId, int groupId) {
        ContractParam param = new ContractParam(chainId, contractId, groupId);
        TbContract contract = queryContract(param);
        if (Objects.isNull(contract)) {
            log.info("contractId is invalid. contractId:{}", contractId);
            throw new BaseException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }

    /**
     * contract name can not be repeated.
     */
    private void verifyContractNameNotExist(int chainId, int groupId, String path, String name,
            int contractId) {
        ContractParam param = new ContractParam(chainId, groupId, path, name);
        TbContract localContract = queryContract(param);
        if (Objects.isNull(localContract)) {
            return;
        }
        if (contractId != localContract.getContractId()) {
            throw new BaseException(ConstantCode.CONTRACT_NAME_REPEAT);
        }
    }
}
