package com.webank.webase.chain.mgr.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.trans.entity.TransResultDto;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.web3j.precompile.common.PrecompiledCommon;
import org.fisco.bcos.web3j.protocol.channel.StatusCode;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Formatter;
import java.util.Locale;
import java.util.Objects;

import static org.fisco.bcos.web3j.precompile.common.PrecompiledCommon.Success;

@Slf4j
public class CommUtils {
    private static final Integer HTTP_SUCCESS_RESPONSE_CODE = 0;


    public static <T> T getResultData(String responseJson, Class<T> dataClass) {
        BaseResponse response = JsonTools.toJavaObject(responseJson, BaseResponse.class);
        return getResultData(response, dataClass);
    }

    /**
     * @param response
     * @param dataClass
     * @param <T>
     * @return
     */
    public static <T> T getResultData(BaseResponse response, Class<T> dataClass) {
        if (dataClass == null) {
            log.info("finish exec method [getResultData]. dataClass is null,return null");
            return null;
        }
        if (Objects.isNull(response)) {
            log.info("finish exec method [getResultData]. response is null,return null");
            return null;
        }
        if (HTTP_SUCCESS_RESPONSE_CODE != response.getCode()) {
            log.info("finish exec method [getResultData]. response code:{} message:{}", response.getCode(), response.getMessage());
            throw new BaseException(response.getCode(), response.getMessage());
        }

        T t = JsonTools.toJavaObject(response.getData(), dataClass);
        log.info("success exec method [getResultData]. data:{}", JsonTools.objToString(response.getData()));
        return t;
    }


    public static <T> T getResultData(BaseResponse response, TypeReference<T> tTypeReference) {
        if (tTypeReference == null) {
            log.info("finish exec method [getResultData]. tTypeReference is null,return null");
            return null;
        }
        if (Objects.isNull(response)) {
            log.info("finish exec method [getResultData]. response is null,return null");
            return null;
        }
        if (HTTP_SUCCESS_RESPONSE_CODE != response.getCode()) {
            log.info("finish exec method [getResultData]. response code:{} message:{}", response.getCode(), response.getMessage());
            throw new BaseException(response.getCode(), response.getMessage());
        }

        T t = JsonTools.stringToObj(JsonTools.objToString(response.getData()), tTypeReference);
        log.info("success exec method [getResultData]. data:{}", JsonTools.objToString(response.getData()));
        return t;
    }


    /**
     * handle receipt of precompiled
     *
     * @throws TransactionException
     * @throws IOException
     */
    public static void handleTransResultDto(TransResultDto receipt) throws BaseException {
        log.debug("handle tx receipt of precompiled");
        String status = receipt.getStatus();
        if (!"0x0".equals(status)) {
            throw new BaseException(ConstantCode.TX_RECEIPT_CODE_ERROR.getCode(),
                    StatusCode.getStatusMessage(receipt.getStatus(), receipt.getMessage()));
        } else {
            if (receipt.getOutput() != null) {
                try {
                    String codeMsgFromOutput = getJsonStr(receipt.getOutput());
                    String resultMsg = PrecompiledUtils.handleReceiptOutput(codeMsgFromOutput);
                    if (!String.valueOf(Success).equals(resultMsg)) {
                        throw new BaseException(ConstantCode.TX_RECEIPT_CODE_ERROR.attach(resultMsg));
                    }

                } catch (IOException e) {
                    log.error("handleTransactionReceipt getJsonStr of error tx receipt fail:[]", e);
                    throw new BaseException(ConstantCode.TX_RECEIPT_OUTPUT_PARSE_JSON_FAIL.getCode(), e.getMessage());
                }
            } else {
                throw new BaseException(ConstantCode.TX_RECEIPT_OUTPUT_NULL);
            }
        }
    }


    public static String getJsonStr(String output) throws IOException {
        try {
            int code = new BigInteger(output.substring(2), 16).intValue();
            if (code == 1) {
                code = Success;
            }
            return PrecompiledCommon.transferToJson(code);
        } catch (NumberFormatException e) {
            return "The call function does not exist.";
        }
    }


    /**
     * 得到mac地址
     *
     * @return
     */
    public static String getCurrentMAC() {
        log.debug("start method getCurrentMAC");

        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            byte[] mac = ni.getHardwareAddress();
            String sMAC = "";
            Formatter formatter = new Formatter();
            for (int i = 0; i < mac.length; i++) {
                sMAC = formatter.format(Locale.getDefault(), "%02X%s", mac[i],
                        (i < mac.length - 1) ? "-" : "").toString();
            }

            log.debug("success method getCurrentMAC, mac:{}", sMAC);
            return sMAC;
        } catch (Exception e) {
            log.warn("fail getCurrentMAC", e);
            throw new BaseException(ConstantCode.SYSTEM_EXCEPTION.attach(e.getMessage()));
        }

    }

    /**
     * @return
     */
    public static String getCurrentProcessId() {
        log.debug("start method getCurrentProcessId");
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName();
        String processId = name.substring(0, name.indexOf("@"));
        log.debug("success exec  method getCurrentProcessId, result:{}", processId);
        return processId;
    }

}
