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
package com.webank.webase.chain.mgr.trans;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.PrecompiledTypes;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.contract.ContractManager;
import com.webank.webase.chain.mgr.contract.entity.ContractFunction;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.precompiledapi.PrecompiledCommonInfo;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.sign.req.EncodeInfo;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.trans.entity.ReqSendByContractIdVO;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.util.Web3Tools;
import com.webank.webase.chain.mgr.util.web3.ContractAbiUtil;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.FunctionEncoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.RetCode;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.codec.decode.ReceiptParser;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.transaction.model.po.RawTransaction;
import org.fisco.bcos.sdk.utils.Numeric;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * services for contract data.
 */
@Log4j2
@Service
public class TransService {
    @Autowired
    private ContractManager contractManager;
    @Autowired
    private FrontInterfaceService frontInterface;
    @Autowired
    private UserService userService;
    @Autowired
    Map<Integer, CryptoSuite> cryptoSuiteMap;


    /**
     * send transaction.
     *
     * @param req parameter
     * @return
     */
    public TransResultDto send(ReqSendByContractIdVO req) {
        log.info("start exec method[send]. param:{}", JsonTools.objToString(req));
        //check contractId
        TbContract tbContract = contractManager.verifyContractId(req.getContractId());

        //handle transaction
        int chain = tbContract.getChainId();
        int group = tbContract.getGroupId();
        String user = req.getSignUserId();
        String address = tbContract.getContractAddress();
        String abi = tbContract.getContractAbi();
        String funcName = req.getFuncName();
        List<Object> funcParam = req.getFuncParam();
        if (CollectionUtils.isEmpty(funcParam) && StringUtils.isNotBlank(req.getFuncParamJson())) {
            funcParam = JsonTools.toJavaObjectList(req.getFuncParamJson(), Object.class);
        }
        if (CollectionUtils.isEmpty(funcParam))
            funcParam = Arrays.asList();

        TransResultDto restRsp = handleTransaction(chain, group, user, address, abi, funcName, funcParam);

        log.info("finish exec method[send]. restRsp:{}", JsonTools.objToString(restRsp));
        return restRsp;

    }

    public Object sendQueryTransaction(String encodeStr, String contractAddress, String funcName,
                                       String functionAbi, int chainId, int groupId) {
        // transaction param
        Map<String, Object> params = new HashMap<>();
        params.put("groupId", groupId);
        params.put("contractAddress", contractAddress);
        params.put("funcName", funcName);
        params.put("contractAbi", functionAbi);
        params.put("encodeStr", encodeStr);
        return frontInterface.sendQueryTransaction(chainId, groupId, params);
    }


    /**
     * data sign.
     *
     * @param groupId         id
     * @param signUserId      type
     * @param contractAddress info
     * @param data            info
     * @return
     */
    public String signMessage(int chainId, int groupId, String signUserId, int encryptType,
                              String contractAddress, String data) throws BaseException {
        Random r = new SecureRandom();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = frontInterface.getLatestBlockNumber(chainId, groupId)
                .add(ConstantProperties.LIMIT_VALUE);
        ClientVersion version = frontInterface.getClientVersion(chainId, groupId);
        String signMsg;
        log.info("signMessage encryptType: {}", encryptType);
//        if (version.getVersion().contains("2.0.0-rc1")
//                || version.getVersion().contains("release-2.0.1")) {
        TransactionEncoderService encoderService = new TransactionEncoderService(cryptoSuiteMap.get(encryptType));

        String chainID = version.getChainId();
        RawTransaction extendedRawTransaction =
            RawTransaction.createTransaction(randomid, ConstantProperties.GAS_PRICE,
                        ConstantProperties.GAS_LIMIT, blockLimit, contractAddress, BigInteger.ZERO, data,
                        new BigInteger(chainID), BigInteger.valueOf(groupId), "");
        byte[] encodedTransaction = encoderService.encode(extendedRawTransaction, null);
        String encodedDataStr = Numeric.toHexString(encodedTransaction);

        EncodeInfo encodeInfo = new EncodeInfo();
        encodeInfo.setSignUserId(signUserId);
        encodeInfo.setEncodedDataStr(encodedDataStr);

        Instant startTime = Instant.now();
        String signDataStr = userService.getSignData(encodeInfo);
        log.info("getSignData from sign useTime: {}",
                Duration.between(startTime, Instant.now()).toMillis());

        if (StringUtils.isBlank(signDataStr)) {
            log.warn("deploySend get sign data error.");
            return null;
        }

        SignatureResult signData = CommonUtils.stringToSignatureData(signDataStr, encryptType);
        byte[] signedMessage = encoderService.encode(extendedRawTransaction, signData);
        signMsg = Numeric.toHexString(signedMessage);

        return signMsg;
    }

    /**
     * build Function with abi.
     */
    private ContractFunction buildContractFunction(List<Object> functionAbi, String funcName,
                                                   List<Object> params) throws BaseException {
        // check function name
        ABIDefinition abiDefinition = null;
        try {
            abiDefinition =
                    ContractAbiUtil.getAbiDefinition(funcName, JsonTools.toJSONString(functionAbi));
        } catch (Exception e) {
            log.error("abi parse error. abi:{}", JsonTools.toJSONString(functionAbi));
            throw new BaseException(ConstantCode.ABI_PARSE_ERROR);
        }
        if (Objects.isNull(abiDefinition)) {
            log.warn("transaction fail. func:{} is not existed", funcName);
            throw new BaseException(ConstantCode.FUNCTION_NOT_EXISTS);
        }

        // input format
        List<String> funcInputTypes = ContractAbiUtil.getFuncInputType(abiDefinition);
        // check param match inputs
        if (funcInputTypes.size() != params.size()) {
            log.error("load contract function error for function params not fit");
            throw new BaseException(ConstantCode.IN_FUNCPARAM_ERROR);
        }
        List<Type> finalInputs = ContractAbiUtil.inputFormat(funcInputTypes, params);
        // output format
        List<String> funOutputTypes = ContractAbiUtil.getFuncOutputType(abiDefinition);
        List<TypeReference<?>> finalOutputs = ContractAbiUtil.outputFormat(funOutputTypes);

        // build ContractFunction
        ContractFunction cf =
                ContractFunction.builder().funcName(funcName).constant(abiDefinition.isConstant())
                        .inputList(funcInputTypes).outputList(funOutputTypes)
                        .finalInputs(finalInputs).finalOutputs(finalOutputs).build();
        return cf;
    }


    /**
     * get EncoderUtil.
     */
    public CryptoSuite getCryptoSuite(int encryptType) throws BaseException {
        if (!EncryptTypeEnum.isInclude(encryptType)) {
            throw new BaseException(ConstantCode.INVALID_ENCRYPT_TYPE);
        }
        return cryptoSuiteMap.get(encryptType);
    }


    /**
     * send tx with sign for precomnpiled contract
     *
     * @param precompiledType enum of precompiled contract
     * @param funcName        precompiled contract function name
     */
    public TransResultDto transHandleWithSignForPrecompile(int chainId, int groupId, String signUserId,
                                                   PrecompiledTypes precompiledType, String funcName, List<Object> funcParams) {

        // get address and abi of precompiled contract
        String contractAddress = PrecompiledCommonInfo.getAddress(precompiledType);
        String abiStr = PrecompiledCommonInfo.getAbi(precompiledType);

        // trans handle
        return handleTransaction(chainId, groupId, signUserId, contractAddress, abiStr, funcName, funcParams);
    }


    /**
     * @param chainId
     * @param groupId
     * @param signUserId
     * @param contractAddress
     * @param contractAbi
     * @param funName
     * @param funcParams
     * @return
     */
    private TransResultDto handleTransaction(int chainId, int groupId, String signUserId, String contractAddress,
                                             String contractAbi, String funName, List<Object> funcParams) {
        log.debug("start exec method[handleTransaction],chainId:{} groupId:{} signUserId:{}" +
                        " contractAddress:{} contractAbi:{} funcName:{} funcParams:{}", chainId, groupId,
                signUserId, contractAddress, contractAbi, funName, JsonTools.objToString(funcParams));
        //checkSignUserId
        RspUserInfo rspUserInfo = userService.checkSignUserId(signUserId);
        if (rspUserInfo == null) {
            log.error("checkSignUserId fail.");
            throw new BaseException(ConstantCode.SIGN_USERID_ERROR);
        }

        // check param ,get function of abi
        List<Object> functionAbi = Arrays.asList(Web3Tools.getAbiDefinition(funName, contractAbi));
        ContractFunction contractFunction = buildContractFunction(functionAbi, funName, funcParams);

        // encode function
        Function function = new Function(funName, contractFunction.getFinalInputs(), contractFunction.getFinalOutputs());
        FunctionEncoder functionEncoder = new FunctionEncoder(cryptoSuiteMap.get(rspUserInfo.getEncryptType()));
        String encodedFunction = functionEncoder.encode(function);

        //send transaction
        TransResultDto transResultDto = new TransResultDto();
        if (contractFunction.getConstant()) {
            Object response = sendQueryTransaction(encodedFunction, contractAddress, funName, JsonTools.toJSONString(functionAbi),
                chainId, groupId);
            transResultDto.setQueryInfo(JsonTools.objToString(response));
            transResultDto.setConstant(true);
        } else {
            // data sign
            String signMsg = signMessage(chainId, groupId, signUserId, rspUserInfo.getEncryptType(),
                    contractAddress, encodedFunction);
            if (StringUtils.isBlank(signMsg)) {
                throw new BaseException(ConstantCode.DATA_SIGN_ERROR);
            }
            // send transaction
            TransactionReceipt receipt = frontInterface.sendSignedTransaction(chainId, groupId, signMsg, true);
            BeanUtils.copyProperties(receipt, transResultDto);
            transResultDto.setConstant(false);
        }

        log.debug("finish exec method[handleTransaction], result:{}", JsonTools.objToString(transResultDto));
        return transResultDto;
    }


}
