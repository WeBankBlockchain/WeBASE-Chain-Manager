package com.webank.webase.chain.mgr.contract;

import com.sun.javafx.PlatformUtil;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.OsNameEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.entity.RspContractCompileDto;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;


@Slf4j
@Service
public class CompileService {

    @Autowired
    private TbContractMapper tbContractMapper;
    @Autowired
    private ContractManager contractManager;


    /**
     * @param contractId
     * @return
     */
    public TbContract compileByContractId(int contractId) throws IOException {
        log.debug("start compileByContractId contractId:{}", contractId);
        //check contractId
        TbContract contract = contractManager.verifyContractId(contractId);
        //check contract status
        contractManager.verifyContractNotDeploy(contract.getChainId(), contract.getContractId(), contract.getGroupId());
        //check contractSource
        if (StringUtils.isBlank(contract.getContractSource()))
            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach("contract source is empty"));

        //request front for compile
        RspContractCompileDto restRsp = null;
        try {


            // decode
            byte[] contractSourceByteArr = Base64.getDecoder().decode(contract.getContractSource());
            Path contractFilePath = Paths.get(String.format(ConstantProperties.SOLIDITY_FILE_TEMP, contract.getContractName()));
            // save contract to file
            File contractFile = new File(contractFilePath.toUri());
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
            //get version from contract file TODO    PlatformUtil.isLinux();

            //build name of solc file
//            String solcFileName = buildNameOfSolc();
//            restRsp = frontInterface.compileSingleContractFile(contract.getChainId(), contract.getGroupId(), contract.getContractName(), contract.getContractSource());

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
     * @param solcVersion
     * @param encryptType
     * @return
     */
    private String buildNameOfSolc(String solcVersion, EncryptTypeEnum encryptType) {
        log.debug("start compileByContractId solcVersion:{} encryptType:{}", solcVersion, encryptType);
        StringBuffer stringBuffer = new StringBuffer("solc");

        //append version, example: solc-0.4.25
        stringBuffer.append("-");
        if (StringUtils.isNotBlank(solcVersion)) {
            stringBuffer.append(solcVersion);
        } else {
            stringBuffer.append(ConstantProperties.DEFAULT_SOLC_VERSION);
        }

        //append os name, example: solc-0.4.25-linux
        stringBuffer.append("-").append(getCurrentOsName());

        //append by encryptType
        if (encryptType.equals(EncryptTypeEnum.SM2_TYPE))
            stringBuffer.append("-").append("gm");

        //append by exe, example: solc-0.4.25-window.exe
        if (stringBuffer.indexOf(OsNameEnum.WINDOW.getValue()) > 0)
            stringBuffer.append(".exe");


        log.debug("start compileByContractId solcVersion:{} encryptType:{} stringBuffer:{}", solcVersion, encryptType, stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     * @return
     */
    private String getCurrentOsName() {
        log.debug("success getCurrentOsName");
        if (PlatformUtil.isLinux())
            return OsNameEnum.LINUX.name();
        if (PlatformUtil.isWindows())
            return OsNameEnum.WINDOW.name();
        if (PlatformUtil.isMac())
            return OsNameEnum.MAC.name();
        throw new BaseException(ConstantCode.SOLC_NOT_SUPPORT_OS.attach("not support:" + System.getProperty("os.name")));
    }

}
