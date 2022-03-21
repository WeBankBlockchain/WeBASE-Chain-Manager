package com.webank.webase.chain.mgr.deploy.req;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */

@Data
@NoArgsConstructor
public class ReqAddNode {
    @Positive(message = "chain id error.")
    private String chainId;

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
}

