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
package com.webank.webase.chain.mgr.contract;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.base.tools.Web3Tools;
import com.webank.webase.chain.mgr.contract.entity.*;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.front.entity.ContractManageParam;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.frontinterface.FrontRestTools;
import com.webank.webase.chain.mgr.method.MethodService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.trans.TransService;
import com.webank.webase.chain.mgr.util.ContractAbiUtil;
import com.webank.webase.chain.mgr.util.EncoderUtil;
import com.webank.webase.chain.mgr.util.HttpEntityUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.datatypes.Address;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class ContractService {

    @Autowired
    private TbContractMapper tbContractMapper;
    @Autowired
    private ContractManager contractManager;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private FrontService frontService;
    @Autowired
    private TransactionRestTools transactionRestTools;
    @Autowired
    private MethodService methodService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransService transService;


    /**
     * compile contract.
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

        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(tbFront.getFrontPeerName());
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, params);
        List<RspContractCompile> compileInfos = frontInterface.postToSpecificFront(
                Integer.MIN_VALUE, tbFront.getFrontIp(), tbFront.getFrontPort(),
                FrontRestTools.URI_MULTI_CONTRACT_COMPILE, httpEntity, List.class);

        log.debug("end compileContract.");
        return compileInfos;
    }

    /**
     * @param contractId
     * @return
     */
    public TbContract compileByContractId(int contractId) {
        log.debug("start compileByContractId contractId:{}", contractId);
        //check contractId
        TbContract contract = contractManager.verifyContractId(contractId);
        //check contract status
        verifyContractNotDeploy(contract.getChainId(), contract.getContractId(), contract.getGroupId());
        //check contractSource
        if (StringUtils.isBlank(contract.getContractSource()))
            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach("contract source is empty"));

        //request front for compile
        RspContractCompileDto restRsp = null;
        try {
            restRsp = frontInterface.compileSingleContractFile(contract.getChainId(), contract.getGroupId(), contract.getContractName(), contract.getContractSource());

            if (Objects.isNull(restRsp))
                throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach("compile result is null"));

            if (StringUtils.isAnyBlank(restRsp.getBytecodeBin(), restRsp.getContractAbi()))
                throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach(restRsp.getErrors()));

        } catch (BaseException baseException) {
            contract.setModifyTime(new Date());
            contract.setContractStatus(ContractStatus.COMPILE_FAILED.getValue());
            String message = baseException.getRetCode().getMessage();
            String attachment = baseException.getRetCode().getAttachment();
            contract.setDescription(StringUtils.isBlank(message) ? attachment : message);
            tbContractMapper.updateByPrimaryKeyWithBLOBs(contract);
            throw baseException;
        } catch (Exception ex) {
            log.error("compile not success", ex);
            contract.setModifyTime(new Date());
            contract.setContractStatus(ContractStatus.COMPILE_FAILED.getValue());
            contract.setDescription(ex.getMessage());
            tbContractMapper.updateByPrimaryKeyWithBLOBs(contract);
            throw ex;
        }

        //success
        contract.setBytecodeBin(restRsp.getBytecodeBin());
        contract.setContractAbi(restRsp.getContractAbi());
        contract.setContractStatus(ContractStatus.COMPILED.getValue());
        contract.setDescription("");
        tbContractMapper.updateByPrimaryKeyWithBLOBs(contract);

        TbContract result = tbContractMapper.selectByPrimaryKey(contractId);
        log.debug("success compileByContractId contractId:{} result:{}", contractId, JsonTools.objToString(result));
        return result;
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
     * deploy by contractId.
     *
     * @param req
     * @return
     */
    public TbContract deployByContractId(ReqDeployByContractIdVO req) {
        log.debug("start deployByContractId. param:{}", JsonTools.objToString(req));

        // check sign user id
        String signUserId = req.getSignUserId();
        RspUserInfo rspUserInfo = userService.checkSignUserId(signUserId);
        if (rspUserInfo == null) {
            throw new BaseException(ConstantCode.SIGN_USERID_ERROR);
        }
        // check parameters
//        TbContract tbContract = contractManager.verifyContractId(req.getContractId());
        String json = "{\"contractId\":400010,\"contractPath\":\"/\",\"contractName\":\"HelloWorld\",\"chainId\":493,\"groupId\":2,\"contractAddress\":null,\"deployTime\":null,\"contractStatus\":4,\"contractType\":0,\"description\":\"\",\"createTime\":\"2021-02-03 11:11:20\",\"modifyTime\":\"2021-02-03 11:11:20\",\"contractSource\":\"cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsKCmNvbnRyYWN0IEhlbGxvV29ybGR7CiAgICBzdHJpbmcgbmFtZTsKCiAgICBmdW5jdGlvbiBIZWxsb1dvcmxkKCl7CiAgICAgICBuYW1lID0gIkhlbGxvLCBXb3JsZCEiOwogICAgfQoKICAgIGZ1bmN0aW9uIGdldCgpY29uc3RhbnQgcmV0dXJucyhzdHJpbmcpewogICAgICAgIHJldHVybiBuYW1lOwogICAgfQoKICAgIGZ1bmN0aW9uIHNldChzdHJpbmcgbil7CiAgICAJbmFtZSA9IG47CiAgICB9Cn0=\",\"contractAbi\":\"[{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"n\\\",\\\"type\\\":\\\"string\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"string\\\"}],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"view\\\",\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[],\\\"payable\\\":false,\\\"stateMutability\\\":\\\"nonpayable\\\",\\\"type\\\":\\\"constructor\\\"}]\",\"contractBin\":null,\"bytecodeBin\":\"608060405234801561001057600080fd5b506040805190810160405280600d81526020017f48656c6c6f2c20576f726c6421000000000000000000000000000000000000008152506000908051906020019061005c929190610062565b50610107565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106100a357805160ff19168380011785556100d1565b828001600101855582156100d1579182015b828111156100d05782518255916020019190600101906100b5565b5b5090506100de91906100e2565b5090565b61010491905b808211156101005760008160009055506001016100e8565b5090565b90565b6102d7806101166000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610164565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b8060009080519060200190610160929190610206565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156101fc5780601f106101d1576101008083540402835291602001916101fc565b820191906000526020600020905b8154815290600101906020018083116101df57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061024757805160ff1916838001178555610275565b82800160010185558215610275579182015b82811115610274578251825591602001919060010190610259565b5b5090506102829190610286565b5090565b6102a891905b808211156102a457600081600090555060010161028c565b5090565b905600a165627a7a72305820b929035851bba6baf688e34c8b2ad16f8c8ace25d7b4f26a1cf32d690cf444ee0029\"}";
        TbContract tbContract = JsonTools.toJavaObject(json, TbContract.class);
        // check contract
//        verifyContractNotDeploy(tbContract.getChainId(), tbContract.getContractId(), tbContract.getGroupId());

        List<Object> params = req.getConstructorParams();
        if (CollectionUtils.isEmpty(params) && StringUtils.isNotBlank(req.getConstructorParamsJson())) {
            params = JsonTools.toJavaObjectList(req.getConstructorParamsJson(), Object.class);
        }
        if (CollectionUtils.isEmpty(params))
            params = Arrays.asList();

        AbiDefinition abiDefinition = null;
        try {
            abiDefinition = ContractAbiUtil.getAbiDefinition(tbContract.getContractAbi());
        } catch (Exception e) {
            log.error("abi parse error. abi:{}", tbContract.getContractAbi());
            throw new BaseException(ConstantCode.ABI_PARSE_ERROR);
        }
        List<String> funcInputTypes = ContractAbiUtil.getFuncInputType(abiDefinition);
        if (funcInputTypes.size() != params.size()) {
            log.warn("deploy fail. funcInputTypes:{}, params:{}", funcInputTypes, params);
            throw new BaseException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        // Constructor encode
        String encodedConstructor = "";
        if (funcInputTypes.size() > 0) {
            List<Type> finalInputs = ContractAbiUtil.inputFormat(funcInputTypes, params);
            encodedConstructor = EncoderUtil.encodeConstructor(finalInputs);
        }
        // data sign
        String data = tbContract.getBytecodeBin() + encodedConstructor;
        String signMsg = transService.signMessage(tbContract.getChainId(), tbContract.getGroupId(), req.getSignUserId(), rspUserInfo.getEncryptType(), "", data);
        if (StringUtils.isBlank(signMsg)) {
            throw new BaseException(ConstantCode.DATA_SIGN_ERROR);
        }
        // send transaction
        TransactionReceipt receipt = frontInterface.sendSignedTransaction(tbContract.getChainId(), tbContract.getGroupId(), signMsg, true);
        String contractAddress = receipt.getContractAddress();
        if (StringUtils.isBlank(contractAddress)
                || Address.DEFAULT.getValue().equals(contractAddress)) {
            log.error("fail deploy, receipt:{}", JsonTools.toJSONString(receipt));
            throw new BaseException(ConstantCode.CONTRACT_DEPLOY_FAIL);
        }

        //update
        tbContract.setContractAddress(contractAddress);
        tbContract.setContractStatus(ContractStatus.DEPLOYED.getValue());
        tbContract.setDeployTime(new Date());
        tbContractMapper.updateByPrimaryKeySelective(tbContract);

        log.debug("end deployByContractId. contractId:{}  contractAddress:{}", tbContract.getContractId(), contractAddress);
        return getByContractId(tbContract.getContractId());

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

        //httpEntity
        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(tbFront.getFrontPeerName());
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, params);

        // deploy
        String contractAddress = frontInterface.postToSpecificFront(groupId, tbFront.getFrontIp(),
                tbFront.getFrontPort(), FrontRestTools.URI_CONTRACT_DEPLOY, httpEntity, String.class);
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

        //httpEntity
        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(tbFront.getFrontPeerName());
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, params);

        // send transaction
        Object frontRsp = frontInterface.postToSpecificFront(inputParam.getGroupId(),
                tbFront.getFrontIp(), tbFront.getFrontPort(), FrontRestTools.URI_SEND_TRANSACTION,
                httpEntity, Object.class);
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

        //httpEntity
        HttpHeaders httpHeaders = HttpEntityUtils.buildHttpHeaderByHost(tbFront.getFrontPeerName());
        HttpEntity httpEntity = HttpEntityUtils.buildHttpEntity(httpHeaders, params);

        // send transaction
        Object contractStatusManageResult =
                frontInterface.postToSpecificFront(inputParam.getGroupId(), tbFront.getFrontIp(),
                        tbFront.getFrontPort(), FrontRestTools.URI_CONTRACT_STATUS_MANAGE, httpEntity,
                        Object.class);

        log.debug("end statusManage. contractStatusManageResult:{}",
                JsonTools.toJSONString(contractStatusManageResult));
        return contractStatusManageResult;
    }


    /**
     * verify that the contract does not exist.
     */
    private void verifyContractNotExistByName(int chainId, int groupId, String name, String path) {
        TbContract contract = tbContractMapper.getContract(chainId, groupId, name, path);
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
    public Object deployByTransactionServer(int contractId, String signUserId, List<Object> constructorParams) {
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
        } else {
            contractToUpdate.setContractStatus(ContractStatus.DEPLOYMENTFAILED.getValue());
        }
        this.update(contractToUpdate);
        return response;
    }


    /**
     * @param contractId
     * @param signUserId
     * @param funcName
     * @param funcParams
     * @return
     */
    public Object sendByTransactionServer(int contractId, String signUserId, String funcName, List<Object> funcParams) {
        //check
        TbContract tbContract = this.tbContractMapper.selectByPrimaryKey(contractId);
        if (tbContract == null) {
            return new BaseResponse(ConstantCode.INVALID_CONTRACT_ID);
        }
        if (ContractStatus.DEPLOYED.getValue() != tbContract.getContractStatus()) {
            return new BaseException(ConstantCode.CONTRACT_NOT_DEPLOY);
        }

        String url = String.format(TransactionRestTools.URI_SEND_TRANSACTION, transactionRestTools.getBaseUrl(tbContract.getChainId()));

        //param
        ReqTransSendInfoDto transParam = new ReqTransSendInfoDto();
        BeanUtils.copyProperties(tbContract, transParam);
        transParam.setSignUserId(signUserId);
        transParam.setFuncName(funcName);
        transParam.setFuncParam(funcParams);
        transParam.setFunctionAbi(Arrays.asList(Web3Tools.getAbiDefinition(funcName, tbContract.getContractAbi())));

        //send
        log.info("Request transaction server:[{}]:[{}]", url, JsonTools.toJSONString(transParam));
        BaseResponse response = transactionRestTools.post(url, transParam, BaseResponse.class);
        log.info("transaction result:{}", JsonTools.objToString(response));
        return response;
    }

    public boolean update(TbContract tbContract) {
        return this.tbContractMapper.updateByPrimaryKeySelective(tbContract) == 1;
    }


}
