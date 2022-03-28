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
import com.webank.webase.chain.mgr.base.enums.PrecompiledTypes;
import com.webank.webase.chain.mgr.base.exception.BaseException;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.model.tars.TransactionData;
import org.fisco.bcos.sdk.codec.ABICodec;
import org.fisco.bcos.sdk.codec.ABICodecException;
import org.fisco.bcos.sdk.codec.abi.FunctionEncoder;
import org.fisco.bcos.sdk.codec.datatypes.Function;
import org.fisco.bcos.sdk.codec.datatypes.Type;
import org.fisco.bcos.sdk.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinition.NamedType;
import org.fisco.bcos.sdk.codec.wrapper.ABIDefinitionFactory;
import org.fisco.bcos.sdk.codec.wrapper.ContractABIDefinition;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.signature.SignatureResult;
import org.fisco.bcos.sdk.model.NodeVersion.ClientVersion;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderInterface;
import org.fisco.bcos.sdk.transaction.builder.TransactionBuilderService;
import org.fisco.bcos.sdk.transaction.codec.encode.TransactionEncoderService;
import org.fisco.bcos.sdk.transaction.manager.TransactionProcessorFactory;
import org.fisco.bcos.sdk.utils.Hex;
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

    private BigInteger DEFAULT_BLOCK_NUMBER_INTERVAL = BigInteger.valueOf(3600 * 24 * 7);

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
        String chain = tbContract.getChainId();
        String group = tbContract.getGroupId();
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

        TransResultDto restRsp = handleTransaction(chain, group, user, address, abi, funcName,
            funcParam);

        log.info("finish exec method[send]. restRsp:{}", JsonTools.objToString(restRsp));
        return restRsp;

    }

    public Object sendQueryTransaction(String encodeStr, String contractAddress, String funcName,
        String functionAbi, String chainId, String groupId) {
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
    public String signMessage(String chainId, String groupId, String signUserId, int encryptType,
        String contractAddress, String data) throws BaseException {
        log.info("signMessage encryptType: {}", encryptType);
        TransactionEncoderService encoderService = new TransactionEncoderService(
            cryptoSuiteMap.get(encryptType));

        TransactionData extendedRawTransaction = new TransactionData();
        extendedRawTransaction.setBlockLimit(DEFAULT_BLOCK_NUMBER_INTERVAL.bitLength());
        extendedRawTransaction.setChainID(chainId);
        extendedRawTransaction.setGroupID(groupId);
        extendedRawTransaction.setTo(contractAddress);
        extendedRawTransaction.setInput(data.getBytes());

        byte[] encodedTransaction = encoderService.encode(extendedRawTransaction);
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
        byte[] signedMessage = encoderService.encodeToTransactionBytes(extendedRawTransaction,
            signData, 1);
        return Numeric.toHexString(signedMessage);
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
    public TransResultDto transHandleWithSignForPrecompile(String chainId, String groupId,
        String signUserId,
        PrecompiledTypes precompiledType, String funcName, List<Object> funcParams) {

        // get address and abi of precompiled contract
        String contractAddress = PrecompiledCommonInfo.getAddress(precompiledType);
        String abiStr = PrecompiledCommonInfo.getAbi(precompiledType);

        // trans handle
        return handleTransaction(chainId, groupId, signUserId, contractAddress, abiStr, funcName,
            funcParams);
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
    private TransResultDto handleTransaction(String chainId, String groupId, String signUserId,
        String contractAddress,
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

        byte[] encodedFunction = this.encodeFunction2ByteArr(contractAbi, funName, funcParams,
            groupId, rspUserInfo.getEncryptType());

        //send transaction
        TransResultDto transResultDto = new TransResultDto();
        String signMsg = signMessage(chainId, groupId, signUserId, rspUserInfo.getEncryptType(),
            contractAddress, Hex.toHexString(encodedFunction));
        if (StringUtils.isBlank(signMsg)) {
            throw new BaseException(ConstantCode.DATA_SIGN_ERROR);
        }
        // send transaction
        TransactionReceipt receipt = frontInterface.sendSignedTransaction(chainId, groupId, signMsg,
            true);
        BeanUtils.copyProperties(receipt, transResultDto);

        log.debug("finish exec method[handleTransaction], result:{}",
            JsonTools.objToString(transResultDto));
        return transResultDto;
    }

    /**
     * get encoded function for /trans/query-transaction
     *
     * @param abiStr
     * @param funcName
     * @param funcParam
     * @return
     */
    public byte[] encodeFunction2ByteArr(String abiStr, String funcName, List<Object> funcParam,
        String groupId, Integer encryptType) {

        funcParam = funcParam == null ? new ArrayList<>() : funcParam;
        this.validFuncParam(abiStr, funcName, funcParam, encryptType);
        log.debug("abiStr:{} ,funcName:{},funcParam {},groupID {}", abiStr, funcName,
            funcParam, groupId);
        ABICodec abiCodec = new ABICodec(getCryptoSuite(encryptType), false);
        byte[] encodeFunction;
        try {
            encodeFunction = abiCodec.encodeMethod(abiStr, funcName, funcParam);
        } catch (ABICodecException e) {
            log.error("transHandleWithSign encode fail:[]", e);
            throw new BaseException(ConstantCode.CONTRACT_TYPE_ENCODED_ERROR);
        }
        log.debug("encodeFunction2Str encodeFunction:{}", encodeFunction);
        return encodeFunction;
    }

    /**
     * check input
     */
    private void validFuncParam(String contractAbiStr, String funcName, List<Object> funcParam,
        Integer encryptType) {
        ABIDefinition abiDefinition = this.getABIDefinition(contractAbiStr, funcName, encryptType);
        List<NamedType> inputTypeList = abiDefinition.getInputs();
        if (inputTypeList.size() != funcParam.size()) {
            log.error("validFuncParam param not match");
            throw new BaseException(ConstantCode.FUNC_PARAM_SIZE_NOT_MATCH);
        }
        for (int i = 0; i < inputTypeList.size(); i++) {
            String type = inputTypeList.get(i).getType();
            if (type.startsWith("bytes")) {
                if (type.contains("[][]")) {
                    // todo bytes[][]
                    log.warn("validFuncParam param, not support bytes 2d array or more");
//                    throw new FrontException(ConstantCode.FUNC_PARAM_BYTES_NOT_SUPPORT_HIGH_D);
                    return;
                }
                // if not bytes[], bytes or bytesN
                if (!type.endsWith("[]")) {
                    // update funcParam
                    String bytesHexStr = (String) (funcParam.get(i));
                    byte[] inputArray = Numeric.hexStringToByteArray(bytesHexStr);
                    // bytesN: bytes1, bytes32 etc.
                    if (type.length() > "bytes".length()) {
                        int bytesNLength = Integer.parseInt(type.substring("bytes".length()));
                        if (inputArray.length != bytesNLength) {
                            log.error("validFuncParam param of bytesN size not match");
                            throw new BaseException(ConstantCode.FUNC_PARAM_BYTES_SIZE_NOT_MATCH);
                        }
                    }
                    // replace hexString with array
                    funcParam.set(i, inputArray);
                } else {
                    // if bytes[] or bytes32[]
                    List<String> hexStrArray = (List<String>) (funcParam.get(i));
                    List<byte[]> bytesArray = new ArrayList<>(hexStrArray.size());
                    for (int j = 0; j < hexStrArray.size(); j++) {
                        String bytesHexStr = hexStrArray.get(j);
                        byte[] inputArray = Numeric.hexStringToByteArray(bytesHexStr);
                        // check: bytesN: bytes1, bytes32 etc.
                        if (type.length() > "bytes[]".length()) {
                            // bytes32[] => 32[]
                            String temp = type.substring("bytes".length());
                            // 32[] => 32
                            int bytesNLength = Integer
                                .parseInt(temp.substring(0, temp.length() - 2));
                            if (inputArray.length != bytesNLength) {
                                log.error("validFuncParam param of bytesN size not match");
                                throw new BaseException(
                                    ConstantCode.FUNC_PARAM_BYTES_SIZE_NOT_MATCH);
                            }
                        }
                        bytesArray.add(inputArray);
                    }
                    // replace hexString with array
                    funcParam.set(i, bytesArray);
                }
            }
        }
    }

    private ABIDefinition getABIDefinition(String abiStr, String functionName,
        Integer encryptType) {
        ABIDefinitionFactory factory = new ABIDefinitionFactory(getCryptoSuite(encryptType));

        ContractABIDefinition contractABIDefinition = factory.loadABI(abiStr);
        List<ABIDefinition> abiDefinitionList = contractABIDefinition.getFunctions()
            .get(functionName);
        if (abiDefinitionList.isEmpty()) {
            throw new BaseException(ConstantCode.IN_FUNCTION_ERROR);
        }
        // abi only contain one function, so get first one
        ABIDefinition function = abiDefinitionList.get(0);
        return function;
    }

}
