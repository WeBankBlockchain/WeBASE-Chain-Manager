/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.deploy.service;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.config.NodeConfig;
import com.webank.webase.chain.mgr.deploy.req.DeployHost;
import com.webank.webase.chain.mgr.util.cmd.ExecuteResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class HostService {

    @Autowired
    private PathService pathService;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private ChainService chainService;
    /**
     * generate sdk files(crt files and node.[key,crt]) and scp to same host
     *
     * when add node
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void generateHostSDKCertAndScp(byte encryptType, int chainId, String chainName, DeployHost deployHost, String agencyName)
        throws BaseException {
        log.info("start generateHostSDKCertAndScp encryptType:{},chainName:{},deployHost:{},agencyName:{}",
            encryptType, chainName, deployHost, agencyName);
        String hostIp = deployHost.getIp();
        // new host, generate sdk dir first
        Path sdkPath = this.pathService.getSdk(chainName, hostIp);

        if (Files.exists(sdkPath)) {
            log.warn("generateHostSDKCertAndScp Exists sdk dir of host:[{}:{}], delete first.", hostIp,
                sdkPath.toAbsolutePath().toAbsolutePath());
            try {
                FileUtils.deleteDirectory(sdkPath.toFile());
            } catch (IOException e) {
                throw new BaseException(ConstantCode.DELETE_OLD_SDK_DIR_ERROR);
            }
        }
        log.info("generateHostSDKCertAndScp execGenNode");
        // call shell to generate new node config(private key and crt)
        ExecuteResult executeResult = this.deployShellService.execGenNode(
            EncryptTypeEnum.getById(encryptType), chainName, agencyName, sdkPath.toAbsolutePath().toString());
        if (executeResult.failed()) {
            log.error("exec gen node cert shell error!");
            chainService.updateStatus(chainId, ChainStatusEnum.RUNNING, executeResult.getExecuteOut());
            throw new BaseException(ConstantCode.EXEC_GEN_SDK_ERROR);
        }

        // init sdk dir
        NodeConfig.initSdkDir(encryptType, sdkPath);

        this.scpHostSdkCert(chainName, deployHost);
        log.info("end generateHostSDK");

    }

    /**
     * separated scp from generate sdk cert
     * @param chainName
     * @param deployHost
     */
    @Transactional(propagation = Propagation.REQUIRED)
    private void scpHostSdkCert(String chainName, DeployHost deployHost) {
        log.info("start scpHostSdkCert chainName:{},deployHost:{}", chainName, deployHost);
        String ip = deployHost.getIp();
        // host's sdk path
        Path sdkPath = this.pathService.getSdk(chainName, ip);
        String sshUser = deployHost.getSshUser();
        int sshPort = deployHost.getSshPort();
        // scp sdk to remote
        String src = String.format("%s", sdkPath.toAbsolutePath().toString());
        String dst = PathService.getChainRootOnHost(deployHost.getRootDirOnHost(), chainName);

        log.info("scpHostSdkCert scp: Send files from:[{}] to:[{}:{}].", src, ip, dst);
        try {
            deployShellService.scp(ScpTypeEnum.UP, sshUser, ip, sshPort, src, dst);
            log.info("Send files from:[{}] to:[{}:{}] success.", src, ip, dst);
        } catch (BaseException e) {
            log.error("scpHostSdkCert Send file to host:[{}] failed", ip, e);
//            this.updateStatus(host.getId(), HostStatusEnum.CONFIG_FAIL, e.getRetCode().getAttachment());
            return;
        } catch (Exception e) {
            log.error("scpHostSdkCert Send file to host :[{}] failed", ip, e);
//            this.updateStatus(host.getId(), HostStatusEnum.CONFIG_FAIL,
//                "scpHostSdkCert Scp configuration files to host failed, please check the host's network or disk usage.");
            return;
        }

//        this.updateStatus(host.getId(), HostStatusEnum.CONFIG_SUCCESS, "");
        log.info("end scpHostSdkCert");

    }
}
