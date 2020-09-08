package com.webank.webase.chain.mgr.deploy.service.docker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;

import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.deploy.service.PathService;
import com.webank.webase.chain.mgr.util.SshUtil;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DockerOptionsCmdImpl implements DockerOptions{

    @Autowired private ConstantProperties constant;

    @Override
    public boolean checkImageExists(String ip, int dockerPort, String sshUser, int sshPort, String imageTag) {
        String image = getImageRepositoryTag(constant.getDockerRepository(),constant.getDockerRegistryMirror(),imageTag);

        String dockerListImageCommand = String.format("sudo docker images -a %s | grep -v 'IMAGE ID'",image);

        Pair<Boolean, String> result = SshUtil.execDocker(ip, dockerListImageCommand, sshUser, sshPort, constant.getPrivateKey());
        if (result.getKey() && StringUtils.isNotBlank(result.getValue())){
            return true;
        }
        return false;
    }

    /**
     * Pull image, maybe same tag but newer.
     *
     * @param ip
     * @param dockerPort
     * @param sshPort
     * @param imageTag
     * @return
     */
    @Override
    public void pullImage(String ip, int dockerPort,String sshUser, int sshPort, String imageTag) {
        String image = getImageRepositoryTag(constant.getDockerRepository(),constant.getDockerRegistryMirror(),imageTag);
        String dockerPullCommand = String.format("sudo docker pull %s",image);

        // kill exists docker pull process
        SshUtil.killCommand(ip,dockerPullCommand,sshUser,sshPort,constant.getPrivateKey());

        SshUtil.execDocker(ip,dockerPullCommand,sshUser,sshPort,constant.getPrivateKey());
    }

    @Override
    public void run(String ip, int dockerPort, String sshUser, int sshPort, String imageTag, String containerName, String chainRootOnHost, int nodeIndex) {
        String image = getImageRepositoryTag(constant.getDockerRepository(),constant.getDockerRegistryMirror(),imageTag);
        this.stop(ip,dockerPort,sshUser,sshPort,containerName);

        String nodeRootOnHost = PathService.getNodeRootOnHost(chainRootOnHost, nodeIndex);
        String yml = String.format("%s/application.yml", nodeRootOnHost);
        String sdk = String.format("%s/sdk", chainRootOnHost);
        String front_log = String.format("%s/front-log", nodeRootOnHost);

        String dockerCreateCommand = String.format("sudo docker run -d --rm --name %s " +
                "-v %s:/data " +
                "-v %s:/front/conf/application-docker.yml " +
                "-v %s:/data/sdk " +
                "-v %s:/front/log " +
                "-e SPRING_PROFILES_ACTIVE=docker " +
                "--network=host -w=/data %s ", containerName , nodeRootOnHost, yml,sdk,front_log, image);
        log.info("Host:[{}] run container:[{}].", ip, containerName);
        SshUtil.execDocker(ip,dockerCreateCommand,sshUser,sshPort,constant.getPrivateKey());
    }

    @Override
    public void stop(String ip, int dockerPort, String sshUser, int sshPort, String containerName) {
        String dockerRmCommand = String.format("sudo docker rm -f %s ", containerName);
        SshUtil.execDocker(ip,dockerRmCommand,sshUser,sshPort,constant.getPrivateKey());
    }
}

