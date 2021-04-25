package com.webank.webase.chain.mgr.deploy.service.docker;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * TODO. 1. A host should exec only one command at the same time.
 */

public interface DockerOptions {

    /**
     * Get container's name for node.
     *
     * @param rootDirOnHost
     * @param chainName
     * @param hostIndex
     * @return delete all {@link File#separator} and blank of node path on host.
     */
    static String getContainerName(String rootDirOnHost, String chainName, int hostIndex) {
        return String.format("%s%snode%s",
                rootDirOnHost.replaceAll(File.separator, "").replaceAll(" ", ""), chainName, hostIndex);
    }


    /**
     *
     * @return
     */
    default String getImageRepositoryTag(String dockerRepository, String dockerRegistryMirror, String imageTag) {
        // image repository and tag
        String image = String.format("%s:%s", dockerRepository, imageTag);
        if (StringUtils.isNotBlank(dockerRegistryMirror)) {
            // image with mirror
            image = String.format("%s/%s", dockerRegistryMirror, image);
        }
        return image;
    }

    /**
     *
     * @param ip
     * @param dockerPort
     * @param sshUser
     * @param sshPort
     * @param imageTag
     */
    public boolean checkImageExists(String ip, int dockerPort, String sshUser, int sshPort, String imageTag);

    /**
     * Pull image, maybe same tag but newer.
     *
     * @param ip
     * @param dockerPort
     * @param sshPort
     * @param imageTag
     */
    public void pullImage(String ip, int dockerPort, String sshUser, int sshPort, String imageTag);



    /**
     * @param ip
     * @param dockerPort
     * @param sshPort
     * @param containerName
     * @param chainRootOnHost
     * @param nodeIndex
     * @return true if run success, false when run failed.
     */
    public void run(String ip, int dockerPort, String sshUser, int sshPort, String imageTag, String containerName, String chainRootOnHost, int nodeIndex) ;


    /**
     * @param ip
     * @param dockerPort
     * @param sshPort
     * @param containerName
     * @return
     */
    public void stop(String ip, int dockerPort, String sshUser, int sshPort, String containerName) ;

}

