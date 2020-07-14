/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.base.properties;

import java.io.File;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * constants.
 */
@Data
@Component
@ConfigurationProperties(prefix = ConstantProperties.CONSTANT_PREFIX)
public class ConstantProperties {
    // constant
    public static final String CONSTANT_PREFIX = "constant";
    public static final String PREFIX_RESULT_CODE = "0x";

    private String groupInvalidGrayscaleValue; // y:year, M:month, d:day of month, h:hour, m:minute,
                                               // n:forever valid
    // front http request
    private String frontUrl;
    private Integer httpTimeOut = 5000;
    private Integer contractDeployTimeOut = 30000;
    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L; // default 1min


    //************************8 add by deploy
    public static final int DEFAULT_GROUP_ID = 1;

    private boolean useDockerSDK = false;
    public int dockerDaemonPort = 3000;
    public String sshDefaultUser = "root";
    public int sshDefaultPort = 22;


    // TODO. write tbchain's id in db into config.ini
    private int defaultJsonrpcPort = 8545;
    private int defaultP2pPort = 30300;
    private int defaultChannelPort = 20200;
    private int defaultFrontPort = 5002;

    // timeout config
    private long execHostInitTimeout = 2 * 60 * 60 * 1000L;
    private long startNodeTimeout = 5 * 60 * 1000L;
    private long execBuildChainTimeout = 10 * 60 * 1000L;
    private long execShellTimeout = 2 * 60 * 1000L;
    private long dockerRestartPeriodTime = 60 * 1000L;
    private int dockerClientConnectTimeout = 10 * 60 * 1000;
    private int dockerPullTimeout = 10 * 60 * 1000;
    private int dockerClientReadTimeout = 10 * 60 * 1000;

    private String dockerRepository= "fiscoorg/front";
    private String imageTagUpdateUrl = "https://registry.hub.docker.com/v1/repositories/%s/tags";
    private String dockerRegistryMirror = "";
    private String nodesRootDir = "NODES_ROOT";
    private String nodesRootTmpDir = "NODES_ROOT_TMP";

    // shell script
    private String nodeOperateShell = "./script/deploy/host_operate.sh";
    private String buildChainShell = "./script/deploy/build_chain.sh";
    private String genAgencyShell = "./script/deploy/gen_agency_cert.sh";
    private String genNodeShell = "./script/deploy/gen_node_cert.sh";
    private String scpShell =        "./script/deploy/file_trans_util.sh";
    private String privateKey = System.getProperty("user.home") + File.separator + ".ssh" + File.separator + "id_rsa";
    private String fiscoBcosBinary =  "";
}