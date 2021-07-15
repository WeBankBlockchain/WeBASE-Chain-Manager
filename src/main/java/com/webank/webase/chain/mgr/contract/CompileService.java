package com.webank.webase.chain.mgr.contract;

import com.sun.javafx.PlatformUtil;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.OsNameEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.CommonUtils;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainManager;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.util.CommUtils;
import com.webank.webase.chain.mgr.util.DateUtil;
import com.webank.webase.chain.mgr.util.cmd.ExecuteResult;
import com.webank.webase.chain.mgr.util.cmd.JavaCommandExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.webank.webase.chain.mgr.base.properties.ConstantProperties.SOLIDITY_FILE_NAME_FORMAT;


@Slf4j
@Service
public class CompileService {

    @Autowired
    private TbContractMapper tbContractMapper;
    @Autowired
    private ContractManager contractManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ConstantProperties constant;


    /**
     * @param contractId
     * @return
     */
    public TbContract compileByContractId(int contractId) {
        log.info("start compileByContractId contractId:{}", contractId);
        //check contractId
        TbContract contract = contractManager.verifyContractId(contractId);
        TbChain tbChain = chainManager.requireChainIdExist(contract.getChainId());
        //check contract status
        contractManager.verifyContractNotDeploy(contract.getChainId(), contract.getContractId(), contract.getGroupId());
        //check contractSource
        if (StringUtils.isBlank(contract.getContractSource()))
            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach("contract source is empty"));

        File contractDirectory = null;
        try {
            //left: base directory  right: solidity file
            Pair<File, File> filePair = buildFilePair(CommUtils.replaceBlank(contract.getContractName()));
            contractDirectory = filePair.getLeft();

            // decode and save contract to file
            byte[] contractSourceByteArr = CommUtils.base64Decode(contract.getContractSource());

            FileUtils.writeByteArrayToFile(filePair.getRight(), contractSourceByteArr);

            //Write the contract in the specified directory to the same folder
            writeContractToFileByContractPath(contract.getContractPath(), contractDirectory);

            //compile ExecuteResult execCompile
            execCompile(tbChain.getChainType(), contractDirectory.toString(), filePair.getRight().toString());

            //save compile result
            saveCompileResultFile(contract, contractDirectory);
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
            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach(ex.getMessage()));
        } finally {
            //delete directory
            if (Objects.nonNull(contractDirectory)) {
                log.info("remove file");
                try {
                    FileUtils.deleteDirectory(contractDirectory);
                } catch (IOException e) {
                    log.error("delete directory exception", e);
                }
            }
        }

        TbContract result = tbContractMapper.selectByPrimaryKey(contractId);
        log.info("success compileByContractId contractId:{} result:{}", contractId, JsonTools.objToString(result));
        return result;
    }


//    /**
//     * @param chainId
//     * @param groupId
//     * @param directory
//     * @throws IOException
//     */
//    private void writeContractToFileByGroup(int chainId, int groupId, File directory) throws IOException {
//        List<TbContract> contractList = contractManager.listToolingContractByChainAndGroup(chainId, groupId);
//        if (CollectionUtils.isEmpty(contractList))
//            return;
//
//        for (TbContract contract : contractList) {
//            if (StringUtils.isBlank(contract.getContractSource()))
//                continue;
//            byte[] contractSourceByteArr = Base64.getDecoder().decode(contract.getContractSource());
//            String contractNameWithSuffix = String.format(SOLIDITY_FILE_NAME_FORMAT, contract.getContractName());
//            File contractFile = Paths.get(directory.toString(), contractNameWithSuffix).toFile();
//            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
//        }
//    }


    /**
     * @param contractPath
     * @param directory
     * @throws IOException
     */
    private void writeContractToFileByContractPath(String contractPath, File directory) throws IOException {
        List<TbContract> contractList = contractManager.listContractByPath(contractPath);
        if (CollectionUtils.isEmpty(contractList))
            return;

        for (TbContract contract : contractList) {
            if (StringUtils.isBlank(contract.getContractSource()))
                continue;
            byte[] contractSourceByteArr = CommUtils.base64Decode(contract.getContractSource());
            String contractNameWithSuffix = String.format(SOLIDITY_FILE_NAME_FORMAT, contract.getContractName());
            File contractFile = Paths.get(directory.toString(), contractNameWithSuffix).toFile();
            FileUtils.writeByteArrayToFile(contractFile, contractSourceByteArr);
        }
    }


    /**
     * @param contract
     * @param compileOutDir
     * @throws IOException
     */
    private void saveCompileResultFile(TbContract contract, File compileOutDir) throws IOException {
        log.info("start saveCompileResultFile");

        for (File compileResultFile : compileOutDir.listFiles()) {
            String fileName = compileResultFile.getName();
            int index;
            if ((index = fileName.lastIndexOf(".")) <= 0)
                continue;
            if (!contract.getContractName().equals(fileName.substring(0, index)))
                continue;

            String constant = FileUtils.readFileToString(compileResultFile, StandardCharsets.UTF_8);
            if (compileResultFile.getPath().endsWith(ConstantProperties.ABI_FILE_SUFFIX))
                contract.setContractAbi(constant);

            if (compileResultFile.getPath().endsWith(ConstantProperties.BINARY_FILE_SUFFIX))
                contract.setBytecodeBin(constant);

            if (compileResultFile.getPath().endsWith(ConstantProperties.RUNTIME_BINARY_FILE_SUFFIX))
                contract.setContractBin(constant);
        }

//        if (StringUtils.isAnyBlank(contract.getBytecodeBin(), contract.getContractBin(), contract.getContractAbi()))
//            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach("compile result is not found"));

        //success
        contract.setContractStatus(ContractStatus.COMPILED.getValue());
        contract.setDescription("");
        tbContractMapper.updateByPrimaryKeyWithBLOBs(contract);
        log.info("success saveCompileResultFile  contract:{}", JsonTools.objToString(contract));
    }


    /**
     * @param chainType
     * @param contractFile
     * @return
     */
    private void execCompile(byte chainType, String compileOutDir, String contractFile) {
        log.debug("start execCompile chainType:{} compileOutDir:{} contractFile:{}", chainType, compileOutDir, contractFile);

        //build name of solc file
        String solcFileName = buildNameOfSolc(EncryptTypeEnum.getById(chainType));
        String solcFullFile = Paths.get(ConstantProperties.SOLC_FILE_PATH, solcFileName).toString();

        //set execution authority
        String addAuthorityCommand = String.format("chmod +x %s", solcFullFile);
        JavaCommandExecutor.executeCommand(addAuthorityCommand, constant.getExecShellTimeout());

        //compile command
        String compileCommand = String.format("%s -o %s --bin --abi --bin-runtime %s", solcFullFile, compileOutDir, contractFile);
        ExecuteResult result = JavaCommandExecutor.executeCommand(compileCommand, constant.getSolidityCompileTimeOut());
        log.info("ExecuteResult:{}", JsonTools.objToString(result));
        if (result.failed())
            throw new BaseException(ConstantCode.CONTRACT_COMPILE_ERROR.attach(result.getExecuteOut()));

    }


    /**
     * @param contractName
     * @return
     */
    private Pair<File, File> buildFilePair(String contractName) throws IOException {
        log.debug("start buildFilePair contractName:{}", contractName);

//        Long timestampNow = Instant.now().toEpochMilli();
        String nowFormat = DateUtil.formatNow(DateUtil.YYYYMMDDHHMMSS);

        //create directory
        String contractBaseDirStr = String.format(ConstantProperties.SOLIDITY_BASE_PATH, nowFormat);
        File contractBaseDir = new File(contractBaseDirStr);
        if (!contractBaseDir.exists() && !contractBaseDir.mkdirs())
            throw new BaseException(ConstantCode.FAIL_TO_CREATE_TEMP_FILE.attach("create contract directory not success"));


        //create new file
        String contractNameWithSuffix = String.format(SOLIDITY_FILE_NAME_FORMAT, contractName);
        File contractFile = Paths.get(contractBaseDirStr, contractNameWithSuffix).toFile();
        if (!contractFile.exists() && !contractFile.createNewFile())
            throw new BaseException(ConstantCode.FAIL_TO_CREATE_TEMP_FILE.attach("create contract file not success"));

        Pair<File, File> filePair = Pair.of(contractBaseDir, contractFile);
        log.info("success buildFilePair filePair:{}", JsonTools.objToString(filePair));
        return filePair;
    }


    /**
     * @param encryptType
     * @return
     */
    private String buildNameOfSolc(EncryptTypeEnum encryptType) {
        return buildNameOfSolc(ConstantProperties.DEFAULT_SOLC_VERSION, encryptType);
    }

    /**
     * @param solcVersion
     * @param encryptType
     * @return
     */
    private String buildNameOfSolc(String solcVersion, EncryptTypeEnum encryptType) {
        log.debug("start buildNameOfSolc solcVersion:{} encryptType:{}", solcVersion, encryptType);
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
            stringBuffer.append(ConstantProperties.WINDOW_EXEC_FILE_SUFFIX);


        log.debug("success buildNameOfSolc solcVersion:{} encryptType:{} stringBuffer:{}", solcVersion, encryptType, stringBuffer.toString());
        return stringBuffer.toString();
    }

    /**
     * @return
     */
    private String getCurrentOsName() {
        log.debug("success getCurrentOsName");
        if (PlatformUtil.isLinux())
            return OsNameEnum.LINUX.getValue();
        if (PlatformUtil.isWindows())
            return OsNameEnum.WINDOW.getValue();
        if (PlatformUtil.isMac())
            return OsNameEnum.MAC.getValue();
        throw new BaseException(ConstantCode.SOLC_NOT_SUPPORT_OS.attach("not support:" + System.getProperty("os.name")));
    }

}
