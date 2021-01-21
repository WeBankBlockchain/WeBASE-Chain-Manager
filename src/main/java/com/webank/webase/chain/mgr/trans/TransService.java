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
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.base.tools.Web3Tools;
import com.webank.webase.chain.mgr.contract.ContractManager;
import com.webank.webase.chain.mgr.contract.entity.ContractFunction;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import com.webank.webase.chain.mgr.frontinterface.FrontInterfaceService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.sign.UserService;
import com.webank.webase.chain.mgr.sign.req.EncodeInfo;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import com.webank.webase.chain.mgr.trans.entity.ReqSendByContractIdVO;
import com.webank.webase.chain.mgr.util.ContractAbiUtil;
import com.webank.webase.chain.mgr.util.EncoderUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.RawTransaction;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.NodeVersion;
import org.fisco.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.fisco.bcos.web3j.utils.Numeric;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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
    Map<Integer, EncoderUtil> encoderMap;


    /**
     * send transaction.
     *
     * @param req parameter
     * @return
     */
    public TransResultDto send(ReqSendByContractIdVO req) {

        // check sign user id
        String signUserId = req.getSignUserId();
        RspUserInfo rspUserInfo = userService.checkSignUserId(signUserId);
        if (rspUserInfo == null) {
            log.error("checkSignUserId fail.");
            throw new BaseException(ConstantCode.SIGN_USERID_ERROR);
        }

        //check contractId
        TbContract tbContract = contractManager.verifyContractId(req.getContractId());
        int chainId = tbContract.getChainId();
        int groupId  = tbContract.getGroupId();

        // check param ,get function of abi
        List<Object> functionAbi = Arrays.asList(Web3Tools.getAbiDefinition(req.getFuncName(), tbContract.getContractAbi()));
        ContractFunction contractFunction = buildContractFunction(functionAbi, req.getFuncName(), req.getFuncParam());

        // encode function
        Function function = new Function(req.getFuncName(), contractFunction.getFinalInputs(),
                contractFunction.getFinalOutputs());
        String encodedFunction = getEncoderUtil(rspUserInfo.getEncryptType()).encode(function);

        TransResultDto transResultDto = new TransResultDto();
        String contractAddress = tbContract.getContractAddress();
        if (contractFunction.getConstant()) {
            Object response =
                    sendQueryTransaction(encodedFunction, contractAddress, req.getFuncName(),
                            JsonTools.toJSONString(functionAbi), chainId, groupId);
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
            TransactionReceipt receipt =
                    frontInterface.sendSignedTransaction(chainId, groupId, signMsg, true);
            BeanUtils.copyProperties(receipt, transResultDto);
            transResultDto.setConstant(false);
        }
        return transResultDto;
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
        Random r = new Random();
        BigInteger randomid = new BigInteger(250, r);
        BigInteger blockLimit = frontInterface.getLatestBlockNumber(chainId, groupId)
                .add(ConstantProperties.LIMIT_VALUE);
        NodeVersion.Version version = frontInterface.getClientVersion(chainId, groupId);
        String signMsg;
        log.info("signMessage encryptType: {}", encryptType);
        EncoderUtil encoderUtil = getEncoderUtil(encryptType);
        if (version.getVersion().contains("2.0.0-rc1")
                || version.getVersion().contains("release-2.0.1")) {
            RawTransaction rawTransaction = RawTransaction.createTransaction(randomid,
                    ConstantProperties.GAS_PRICE, ConstantProperties.GAS_LIMIT, blockLimit, contractAddress,
                    BigInteger.ZERO, data);
            byte[] encodedTransaction = encoderUtil.encode(rawTransaction);
            String encodedDataStr = Numeric.toHexString(encodedTransaction);

            EncodeInfo encodeInfo = new EncodeInfo();
            encodeInfo.setSignUserId(signUserId);
            encodeInfo.setEncodedDataStr(encodedDataStr);
            String signDataStr = userService.getSignData(encodeInfo);
            if (StringUtils.isBlank(signDataStr)) {
                log.warn("deploySend get sign data error.");
                return null;
            }

            Sign.SignatureData signData = CommonUtils.stringToSignatureData(signDataStr, encryptType);
            byte[] signedMessage = encoderUtil.encode(rawTransaction, signData);
            signMsg = Numeric.toHexString(signedMessage);
        } else {
            String chainID = version.getChainID();
            ExtendedRawTransaction extendedRawTransaction =
                    ExtendedRawTransaction.createTransaction(randomid, ConstantProperties.GAS_PRICE,
                            ConstantProperties.GAS_LIMIT, blockLimit, contractAddress, BigInteger.ZERO, data,
                            new BigInteger(chainID), BigInteger.valueOf(groupId), "");
            byte[] encodedTransaction = encoderUtil.encode(extendedRawTransaction);
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

            Sign.SignatureData signData = CommonUtils.stringToSignatureData(signDataStr, encryptType);
            byte[] signedMessage = encoderUtil.encode(extendedRawTransaction, signData);
            signMsg = Numeric.toHexString(signedMessage);
        }
        return signMsg;
    }

    /**
     * build Function with abi.
     */
    private ContractFunction buildContractFunction(List<Object> functionAbi, String funcName,
                                                   List<Object> params) throws BaseException {
        // check function name
        AbiDefinition abiDefinition = null;
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
    public EncoderUtil getEncoderUtil(int encryptType) throws BaseException {
        if (!EncryptTypeEnum.isInclude(encryptType)) {
            throw new BaseException(ConstantCode.INVALID_ENCRYPT_TYPE);
        }
        return encoderMap.get(encryptType);
    }
}
