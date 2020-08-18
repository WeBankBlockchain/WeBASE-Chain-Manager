package com.webank.webase.chain.mgr.deploy.req;

import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */

@Data
@NoArgsConstructor
public class ReqDeploy {

    @Positive(message = "chain id error.")
    private int chainId;

    @NotBlank(message = "Image tag version error.")
    private String version;

    @Size(min = 1, message = "At least 1 host.")
    private List<DeployHost> deployHostList;

    @Min(0)
    @Max(1)
    private int encryptType;

    private byte dockerImageType = DockerImageTypeEnum.PULL_OFFICIAL.getId();

    private String consensusType = "pbft";

    private String storageType = "rocksdb";

    private String description;

    private String webaseSignAddr;

    private String chainName;

    @Data
    @NoArgsConstructor
    public static class DeployHost {
        @Positive(message = "External company id error.")
        private int extCompanyId;

        @Positive(message = "External agency id error.")
        private int extOrgId;

        @Positive(message = "External host id error.")
        private int extHostId;

        @NotBlank(message = "IP blank error.")
        private String ip;

        private String sshUser="root";
        private int sshPort=22;
        private int dockerDemonPort=2375;
        private int num = 1;
        private String rootDirOnHost = "/root/fisco";
    }
}

