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
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
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
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.method.MethodService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbContractExample;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbGroupMapper;
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
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private TbGroupMapper groupMapper;
    @Autowired
    private GroupManager groupManager;


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
     * @param inputParam
     * @return
     */
    public BasePageResponse queryContractPage(ReqQueryContractPage inputParam) {
        log.info("start exec method [queryContractPage]");

        TbContractExample example = new TbContractExample();
        example.setStart(Optional.ofNullable(inputParam.getPageNumber()).map(page -> (page - 1) * inputParam.getPageSize()).filter(p -> p >= 0).orElse(1));
        example.setCount(inputParam.getPageSize());

        if (CollectionUtils.isNotEmpty(inputParam.getAppIds())) {
            List<TbGroup> groupList = groupManager.listGroupByAppIdList(inputParam.getAppIds());
            if (CollectionUtils.isEmpty(groupList))
                return new BasePageResponse(ConstantCode.SUCCESS);

            for (TbGroup tbGroup : groupList) {
                TbContractExample.Criteria criteria = example.createCriteria();
                criteria.andChainIdEqualTo(tbGroup.getChainId());
                criteria.andGroupIdEqualTo(tbGroup.getGroupId());
                if (CollectionUtils.isNotEmpty(inputParam.getChainIds()))
                    criteria.andChainIdIn(inputParam.getChainIds());
                if (null != inputParam.getContractStatus())
                    criteria.andContractStatusEqualTo(inputParam.getContractStatus());
                example.or(criteria);
            }
        } else {
            TbContractExample.Criteria criteriaComm = example.createCriteria();
            if (CollectionUtils.isNotEmpty(inputParam.getChainIds()))
                criteriaComm.andChainIdIn(inputParam.getChainIds());
            if (null != inputParam.getContractStatus())
                criteriaComm.andContractStatusEqualTo(inputParam.getContractStatus());
        }

        BasePageResponse basePageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        basePageResponse.setTotalCount(new Long(tbContractMapper.countByExample(example)).intValue());
        if (basePageResponse.getTotalCount() > 0)
            if (inputParam.getContainDetailFields()) {
                basePageResponse.setData(tbContractMapper.selectByExampleWithBLOBs(example));
            } else {
                basePageResponse.setData(tbContractMapper.selectByExample(example));
            }


        log.info("success exec method [queryContractPage] result:{}", JsonTools.objToString(basePageResponse));
        return basePageResponse;
    }


    /**
     * add new contract data.
     */
    @Transactional
    public TbContract saveContract(Contract contract) throws BaseException {
        log.debug("start addContractInfo Contract:{}", JsonTools.toJSONString(contract));
        groupManager.requireGroupExist(contract.getChainId(), contract.getGroupId());
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
     * @param param
     * @return
     */
    @Transactional
    public List<TbContract> saveContractBatch(ReqSaveContractBatchVO param) {

        List<TbContract> tbContractList = new ArrayList<>();
        for (BaseContract baseContract : param.getContractList()) {
            Contract contract = new Contract();
            BeanUtils.copyProperties(baseContract, contract);
            contract.setChainId(param.getChainId());
            contract.setGroupId(param.getGroupId());
            contract.setAgencyId(param.getAgencyId());
            tbContractList.add(saveContract(contract));
        }
        return tbContractList;
    }


    /**
     * save new contract.
     */
    @Transactional
    public TbContract newContract(Contract contract) {
        // check contract not exist.
        contractManager.verifyContractNotExistByName(contract.getChainId(), contract.getGroupId(),
                contract.getContractName(), contract.getContractPath());

        // add to database.
        TbContract tbContract = new TbContract();
        BeanUtils.copyProperties(contract, tbContract);
        tbContract.setSaveByAgency(contract.getAgencyId());
        Date now = new Date();
        tbContract.setCreateTime(now);
        tbContract.setModifyTime(now);
        tbContractMapper.insertSelective(tbContract);
        return this.tbContractMapper.selectByPrimaryKey(tbContract.getContractId());
    }


    /**
     * update contract.
     */
    @Transactional
    public TbContract updateContract(Contract contract) {
        // check not deploy
        TbContract tbContract = contractManager.verifyContractNotDeploy(contract.getChainId(),
                contract.getContractId(), contract.getGroupId());
        // check contractName
        contractManager.verifyContractNotExistByName(contract.getChainId(), contract.getGroupId(),
                contract.getContractPath(), contract.getContractName());

        Integer belongAgency = tbContract.getSaveByAgency();
        BeanUtils.copyProperties(contract, tbContract);
        tbContract.setContractStatus(ContractStatus.NOTDEPLOYED.getValue());
        tbContract.setSaveByAgency(belongAgency);
        tbContract.setModifyTime(new Date());
        tbContractMapper.updateByPrimaryKeyWithBLOBs(tbContract);
        return getByContractId(tbContract.getContractId());
    }


    /**
     * @param contractId
     * @throws BaseException
     */
    public void deleteByContractId(int contractId) throws BaseException {
        log.info("start deleteByContractId contractId:{}", contractId);
        TbContract tbContract = contractManager.verifyContractId(contractId);
        deleteContract(tbContract.getChainId(), tbContract.getContractId(), tbContract.getGroupId());
        log.info("finish deleteByContractId contractId:{}", contractId);

    }

    /**
     * delete contract by contractId.
     */
    public void deleteContract(int chainId, int contractId, int groupId) throws BaseException {
        log.info("start deleteContract contractId:{} groupId:{}", contractId, groupId);
        // check contract id
        contractManager.verifyContractNotDeploy(chainId, contractId, groupId);
        // remove
        this.tbContractMapper.deleteByPrimaryKey(contractId);
        // delete method
        methodService.deleteByContractId(contractId);
        log.info("end deleteContract");
    }

    /**
     * delete contract by chainId.
     */
    public void deleteContractByChainId(int chainId) throws BaseException {
        log.info("start deleteContractByChainId chainId:{}", chainId);
        if (chainId == 0) {
            return;
        }
        // remove
        this.tbContractMapper.deleteByChainId(chainId);
        log.info("end deleteContractByChainId");
    }

    /**
     * delete by groupId
     */
    public void deleteByGroupId(int chainId, int groupId) {
        log.info("start deleteByGroupId chainId:{} groupId:{}", chainId, groupId);

        if (chainId == 0 || groupId == 0) {
            return;
        }
        this.tbContractMapper.deleteByChainIdAndGroupId(chainId, groupId);
        log.info("finish deleteByGroupId chainId:{} groupId:{}", chainId, groupId);
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
        TbContract tbContract = contractManager.verifyContractId(req.getContractId());

        // check contract
        contractManager.verifyContractNotDeploy(tbContract.getChainId(), tbContract.getContractId(), tbContract.getGroupId());

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
        contractManager.verifyContractNotDeploy(inputParam.getChainId(), inputParam.getContractId(),
                inputParam.getGroupId());
        // check contractName
//        contractManager.verifyContractNameNotExist(inputParam.getChainId(), inputParam.getGroupId(),
//                inputParam.getContractPath(), contractName, inputParam.getContractId());
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
        TbContract tbContract = contractManager.verifyContractDeploy(inputParam.getChainId(),
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
