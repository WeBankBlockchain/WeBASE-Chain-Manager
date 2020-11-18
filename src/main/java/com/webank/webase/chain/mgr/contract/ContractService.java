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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.entity.CompileInputParam;
import com.webank.webase.chain.mgr.contract.entity.Contract;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.contract.entity.DeployInputParam;
import com.webank.webase.chain.mgr.contract.entity.ReqContractDeploy;
import com.webank.webase.chain.mgr.contract.entity.RespContractDeploy;
import com.webank.webase.chain.mgr.contract.entity.RspContractCompile;
import com.webank.webase.chain.mgr.contract.entity.TransactionInputParam;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.ContractManageParam;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.FrontRestTools;
import com.webank.webase.chain.mgr.method.MethodService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;

import lombok.extern.log4j.Log4j2;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class ContractService {

    @Autowired
    private TbContractMapper tbContractMapper;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontService frontService;
    @Autowired
    private TransactionRestTools transactionRestTools;
    @Autowired
    private MethodService methodService;

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
        log.debug("start addContractInfo Contract:{}", JsonTools.toJSONString(contract));
        TbContract tbContract;
        if (contract.getContractId() == null) {
            tbContract = newContract(contract);// new
        } else {
            tbContract = updateContract(contract);// update
        }
        // Async save method
        methodService.saveMethodFromContract(tbContract);
        return tbContract;
    }


    /**
     * save new contract.
     */
    private TbContract newContract(Contract contract) {
        // check contract not exist.
        verifyContractNotExistByName(contract.getChainId(), contract.getGroupId(),
                contract.getContractName(), contract.getContractPath());

        // add to database.
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(contract, tbContract);
        Date now = new Date();
        tbContract.setCreateTime(now);
        tbContract.setModifyTime(now);
        tbContractMapper.insertSelective(tbContract);
        return this.tbContractMapper.selectByPrimaryKey(tbContract.getContractId());
    }


    /**
     * update contract.
     */
    private TbContract updateContract(Contract contract) {
        // check not deploy
        TbContract tbContract = verifyContractNotDeploy(contract.getChainId(),
                contract.getContractId(), contract.getGroupId());
        // check contractName
        verifyContractNotExistByName(contract.getChainId(), contract.getGroupId(),
                contract.getContractPath(), contract.getContractName());
        BeanUtils.copyProperties(contract, tbContract);
        tbContract.setModifyTime(new Date());
        tbContractMapper.updateByPrimaryKeySelective(tbContract);
        return getByContractId(tbContract.getContractId());
    }

    /**
     * delete contract by contractId.
     */
    public void deleteContract(int chainId, int contractId, int groupId) throws BaseException {
        log.debug("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        verifyContractNotDeploy(chainId, contractId, groupId);
        // remove
        this.tbContractMapper.deleteByPrimaryKey(contractId);
        // delete method
        methodService.deleteByContractId(contractId);
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
        this.tbContractMapper.deleteByChainId(chainId);
        log.debug("end deleteContractByChainId");
    }

    /**
     * delete by groupId
     */
    public void deleteByGroupId(int chainId, int groupId) {
        if (chainId == 0 || groupId == 0) {
            return;
        }
        this.tbContractMapper.deleteByChainIdAndGroupId(chainId, groupId);
    }

    /**
     * query contract list.
     */
    public List<TbContract> qureyContractList(ContractParam param) throws BaseException {
        log.debug("start qureyContractList ContractListParam:{}", JsonTools.toJSONString(param));

        // query contract list
        List<TbContract> listOfContract = this.tbContractMapper.selectByParam(param);

        log.debug("end qureyContractList listOfContract:{}", JsonTools.toJSONString(listOfContract));
        return listOfContract;
    }


    /**
     * query count of contract.
     */
    public int countOfContract(ContractParam param) throws BaseException {
        log.debug("start countOfContract ContractListParam:{}", JsonTools.toJSONString(param));
        try {
            return this.tbContractMapper.countByParam(param);
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }

    /**
     * query contract by contract id.
     */
    public TbContract getByContractId(Integer contractId) throws BaseException {
        log.debug("start queryContract contractId:{}", contractId);
        try {
            TbContract contractRow = tbContractMapper.selectByPrimaryKey(contractId);
            log.debug("start queryContract contractId:{} contractRow:{}", contractId,
                    JsonTools.toJSONString(contractRow));
            return contractRow;
        } catch (RuntimeException ex) {
            log.error("fail countOfContract", ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }

    }

    /**
     * query DeployInputParam By Address.
     */
//    public List<TbContract> queryContractByBin(Integer groupId, String contractBin)
//            throws BaseException {
//        try {
//            if (StringUtils.isEmpty(contractBin)) {
//                return null;
//            }
//            List<TbContract> contractRow = this.tbContractMapper.selectByBin(groupId, contractBin);
//            log.debug("start queryContractByBin:{}", contractBin, JsonTools.toJSONString(contractRow));
//            return contractRow;
//        } catch (RuntimeException ex) {
//            log.error("fail queryContractByBin", ex);
//            throw new BaseException(ConstantCode.DB_EXCEPTION);
//        }
//    }

    /**
     * query contract info.
     */
    public TbContract queryContract(ContractParam queryParam) {
        log.debug("start queryContract. queryParam:{}", JsonTools.toJSONString(queryParam));
        TbContract tbContract = this.tbContractMapper.getByParam(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JsonTools.toJSONString(queryParam),
                JsonTools.toJSONString(tbContract));
        return tbContract;
    }

    /**
     * deploy contract.
     */
    public TbContract deployContract(DeployInputParam inputParam) throws BaseException {
        log.info("start deployContract. inputParam:{}", JsonTools.toJSONString(inputParam));
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

        List<AbiDefinition> abiArray = JsonTools.toJavaObjectList(inputParam.getContractAbi(), AbiDefinition.class);
        if (abiArray == null || abiArray.isEmpty()) {
            log.info("fail deployContract. abi is empty");
            throw new BaseException(ConstantCode.CONTRACT_ABI_EMPTY);
        }

        // deploy param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("signUserId", inputParam.getSignUserId());
        params.put("contractName", contractName);
        params.put("abiInfo", abiArray);
        params.put("bytecodeBin", inputParam.getBytecodeBin());
        params.put("funcParam", inputParam.getConstructorParams());

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
        tbContract.setDeployTime(new Date());
        tbContract.setContractId(inputParam.getContractId());
        this.tbContractMapper.updateByPrimaryKeySelective(tbContract);

        log.debug("end deployContract. contractId:{} groupId:{} contractAddress:{}",
                tbContract.getContractId(), groupId, contractAddress);
        return getByContractId(tbContract.getContractId());
    }

    /**
     * send transaction.
     */
    public Object sendTransaction(TransactionInputParam inputParam) throws BaseException {
        log.debug("start sendTransaction. param:{}", JsonTools.toJSONString(inputParam));
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
        params.put("signUserId", inputParam.getSignUserId());
        params.put("contractName", inputParam.getContractName());
        params.put("contractAddress", tbContract.getContractAddress());
        params.put("contractAbi", JsonTools.toJavaObjectList(inputParam.getContractAbi(), Object.class));
        params.put("funcName", inputParam.getFuncName());
        params.put("funcParam", inputParam.getFuncParam());

        // send transaction
        Object frontRsp = frontInterface.postToSpecificFront(inputParam.getGroupId(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), FrontRestTools.URI_SEND_TRANSACTION,
                params, Object.class);
        log.debug("end sendTransaction. frontRsp:{}", JsonTools.toJSONString(frontRsp));
        return frontRsp;
    }

    /**
     * contract manage.
     */
    public Object statusManage(ContractManageParam inputParam) throws BaseException {
        log.debug("start statusManage. param:{}", JsonTools.toJSONString(inputParam));
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
        params.put("signUserId", inputParam.getSignUserId());
        params.put("grantAddress", inputParam.getGrantAddress());

        // send transaction
        Object contractStatusManageResult =
                frontInterface.postToSpecificFront(inputParam.getGroupId(), tbFront.getFrontIp(),
                        tbFront.getFrontPort(), FrontRestTools.URI_CONTRACT_STATUS_MANAGE, params,
                        Object.class);

        log.debug("end statusManage. contractStatusManageResult:{}",
                JsonTools.toJSONString(contractStatusManageResult));
        return contractStatusManageResult;
    }


    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExistByName(int chainId, int groupId, String name, String path) {
        TbContract contract = tbContractMapper.getContract(chainId,groupId,name,path);
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
    private void verifyContractNameNotExist(int chainId, int groupId, String path, String name, int contractId) {

    }

    /**
     * @param contractId
     * @param signUserId
     * @return
     */
    public Object deployByTransactionServer(int contractId, String signUserId,List<Object> constructorParams) {
        TbContract tbContract = this.tbContractMapper.selectByPrimaryKey(contractId);
        if (tbContract == null) {
            return new BaseResponse(ConstantCode.INVALID_CONTRACT_ID);
        }

        String url = String.format(TransactionRestTools.URI_CONTRACT_DEPLOY, transactionRestTools.getBaseUrl(tbContract.getChainId()));
        ReqContractDeploy contractDeploy = new ReqContractDeploy();
        BeanUtils.copyProperties(tbContract, contractDeploy);
        contractDeploy.setSignUserId(signUserId);
        contractDeploy.setContractAbi(JsonTools.toJavaObjectList(tbContract.getContractAbi(), Object.class));
        contractDeploy.setFuncParam(constructorParams);
        log.info("Request transaction server:[{}]:[{}]", url, JsonTools.toJSONString(contractDeploy));
        BaseResponse response = transactionRestTools.post(url, contractDeploy, BaseResponse.class);

        Date now = new Date();
        TbContract contractToUpdate = new TbContract();
        contractToUpdate.setContractId(contractId);
        contractToUpdate.setDeployTime(now);
        contractToUpdate.setModifyTime(now);
        if (response.isSuccess()) {
            RespContractDeploy deploy = JsonTools.stringToObj(JsonTools.objToString(response.getData()),
                    new TypeReference<RespContractDeploy>() {
                    });
            contractToUpdate.setContractAddress(deploy.getContractAddress());
            contractToUpdate.setContractStatus(ContractStatus.DEPLOYED.getValue());
        }else{
            contractToUpdate.setContractStatus(ContractStatus.DEPLOYMENTFAILED.getValue());
        }
        this.update(contractToUpdate);
        return response;
    }

    public boolean update(TbContract tbContract) {
        return this.tbContractMapper.updateByPrimaryKeySelective(tbContract) == 1;
    }
}
