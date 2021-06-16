package com.webank.webase.chain.mgr.deploy.service;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.deploy.req.DeployHost;
import com.webank.webase.chain.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.chain.mgr.util.FileUtil;
import com.webank.webase.chain.mgr.util.SshUtil;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImageService {
    @Autowired
    private ConstantProperties constant;
    @Autowired
    private DeployShellService deployShellService;
    @Autowired
    private DockerOptions dockerOptions;

    /**
     * check offline image
     *
     * @param imageTypeEnum
     * @param imageVersion
     */
    public void checkLocalImageByDockerImageTypeEnum(DockerImageTypeEnum imageTypeEnum, String imageVersion) {
        log.error("start exec method[checkLocalImageByDockerImageTypeEnum] imageTypeEnum:{} imageVersion:{}.", JsonTools.objToString(imageTypeEnum), imageVersion);

        // check image tar file when install with offline
        if (imageTypeEnum == DockerImageTypeEnum.LOCAL_OFFLINE) {
            String localTarFile = String.format(constant.getImageTar(), imageVersion);
            if (FileUtil.notExists(localTarFile)) {
                log.error("Image tar file:[{}] not exists when use local offline.", localTarFile);
                throw new BaseException(ConstantCode.FILE_NOT_EXISTS.attach(localTarFile));
            }
        }

        // download image from cdn if download from cdn
        if (imageTypeEnum == DockerImageTypeEnum.DOWNLOAD_CDN) {
            String dockerTarFileName = constant.getDockerTarFileName(imageVersion);
            String cdnUrl = constant.getCdnUrl(imageVersion);
            try {
                FileUtil.download(false, cdnUrl, dockerTarFileName, constant.getDockerPullTimeout());
            } catch (IOException e) {
                log.warn("fail to download image with IOException,cdnUrl:{} dockerTarFileName:{}", cdnUrl, dockerTarFileName, e);
                throw new BaseException(ConstantCode.DOWNLOAD_FILE_ERROR);
            }
        }

        log.error("finish exec method[checkLocalImageByDockerImageTypeEnum] imageTypeEnum:{} imageVersion:{}.", JsonTools.objToString(imageTypeEnum), imageVersion);
    }


    /**
     * 1. check image exist in remote host
     * 2. download
     * @param host
     * @param imageVersion
     * @param dockerImageTypeEnum
     */
    public void pullHostImage(DeployHost host, String imageVersion, DockerImageTypeEnum dockerImageTypeEnum) {
        log.info("start pullImage .  host:[{}] imageVersion:[{}] dockerImageTypeEnum:[{}].", host.getIp(), imageVersion, dockerImageTypeEnum.getId());

        boolean exists = dockerOptions.checkImageExists(host.getIp(), host.getDockerDemonPort(),
                host.getSshUser(), host.getSshPort(), imageVersion);
        log.info("check docker image:[{}] exists:[{}] on host:[{}] first.", imageVersion, exists, host.getIp());

        if (!exists) {
            // only pull image when not exists remote host note
            log.info("Install image with option:[{}]", dockerImageTypeEnum.getDescription());
            switch (dockerImageTypeEnum) {
                case MANUAL:
                    throw new BaseException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.getCode(), String.format("Docker image:[%1s] not exists on host:[%1s].", imageVersion, host.getIp()));
                case PULL_OFFICIAL:
                    // pull from official registry
                    dockerOptions.pullImage(host.getIp(), host.getDockerDemonPort(), host.getSshUser(), host.getSshPort(), imageVersion);
                    break;

                case LOCAL_OFFLINE:
                case DOWNLOAD_CDN:
                    // scp tar file to remote host
                    String imageTarFileName = String.format(constant.getImageTar(), imageVersion);
                    String dst = String.format("~/%s", imageTarFileName);
                    deployShellService.scp(ScpTypeEnum.UP, host.getSshUser(), host.getIp(), host.getSshPort(),
                            FileUtil.getFilePath(imageTarFileName), dst);
                    // unzip tar file
                    String unzip = String.format("sudo docker load -i %s", dst);
                    SshUtil.execDocker(host.getIp(), unzip,
                            host.getSshUser(), host.getSshPort(), constant.getPrivateKey());

                    break;
                case HOST_DOWNLOAD_CDN:
                    String cdnUrl = constant.getCdnUrl(imageVersion);
                    String dockerImport = String.format("curl -sL %s | sudo docker load ", cdnUrl);
                    SshUtil.execDocker(host.getIp(), dockerImport,
                            host.getSshUser(), host.getSshPort(), constant.getPrivateKey());
                    break;
                default:
                    break;
            }
            exists = dockerOptions.checkImageExists(host.getIp(), host.getDockerDemonPort(),
                    host.getSshUser(), host.getSshPort(), imageVersion);
            if (!exists) {
                String message = String.format("Docker image:[%1s] not exists on host after execute installation:[%2s]", imageVersion, host.getIp());
                throw new BaseException(ConstantCode.IMAGE_NOT_EXISTS_ON_HOST.getCode(), message);
            }
        }
    }


}
