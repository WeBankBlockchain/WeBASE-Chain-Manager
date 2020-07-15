package com.webank.webase.chain.mgr.deploy.req;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.webank.webase.chain.mgr.base.tools.JsonTools;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Data
@NoArgsConstructor
public class ReqDeploy {
    @Positive(message = "chain id error.")
    private int chainId;

    @Positive(message = "Tag id error.")
    private int tagId;

    @NotBlank(message = "WeBASE sign address error.")
    private String webaseSignAddr;

    @NotBlank(message = "Chain name error.")
    private String chainName;

    @Size(min = 1,message = "At least 1 host.")
    private List<DeployHost> deployHostList;

    @NotBlank(message = "Consensus Type error.")
    private String consensusType;

    @NotBlank(message = "Storage type error.")
    private String storageType;

    private String description;

    @Data
    @NoArgsConstructor
    public static class DeployHost{
        @Positive(message = "External company id error.")
        private int extCompanyId;

        @Positive(message = "External agency id error.")
        private int extAgencyId;

        @NotBlank(message = "External agency name error.")
        private String extAgencyName;

        @Positive(message = "External host id error.")
        private int extHostId;

        @NotBlank(message = "IP blank error.")
        private String ip;

        @NotBlank(message = "SSH user blank error.")
        private String sshUser;

        @Positive(message = "SSH port error.")
        private int sshPort;

        @Positive(message = "Docker demon port error.")
        private int dockerDemonPort;

        @Positive(message = "Deploy num error.")
        private int num;

        @NotBlank(message = "Root dir blank error.")
        private String rootDirOnHost;
    }

    // TODO. delete
    public static void main(String[] args) {
        ReqDeploy reqDeploy =new ReqDeploy();
        reqDeploy.setTagId(22);
        reqDeploy.setWebaseSignAddr("127.0.0.1:6004");
        reqDeploy.setChainName("default_chain");
        reqDeploy.setConsensusType("pbft");
        reqDeploy.setStorageType("rocksdb");
        reqDeploy.setDescription("测试链");
        List<DeployHost> deployHostList = new ArrayList<>();
        DeployHost deployHost1 = new DeployHost();
        deployHost1.setExtCompanyId(1);
        deployHost1.setExtAgencyId(1);
        deployHost1.setExtAgencyName("Org1");
        deployHost1.setExtHostId(1);
        deployHost1.setIp("106.55.28.72");
        deployHost1.setSshUser("root");
        deployHost1.setSshPort(22);
        deployHost1.setDockerDemonPort(3000);
        deployHost1.setNum(2);
        deployHost1.setRootDirOnHost("/opt/fisco/3tx");

        DeployHost deployHost2 = new DeployHost();
        deployHost2.setExtCompanyId(1);
        deployHost2.setExtAgencyId(2);
        deployHost2.setExtAgencyName("Org2");
        deployHost2.setExtHostId(2);
        deployHost2.setIp("139.9.222.236");
        deployHost2.setSshUser("root");
        deployHost2.setSshPort(22);
        deployHost2.setDockerDemonPort(3000);
        deployHost2.setNum(3);
        deployHost2.setRootDirOnHost("/opt/fisco/3hw");

        deployHostList.add(deployHost1);
        deployHostList.add(deployHost2);

        reqDeploy.setDeployHostList(deployHostList);
        System.out.println(JsonTools.toJSONString(reqDeploy));
    }
}

