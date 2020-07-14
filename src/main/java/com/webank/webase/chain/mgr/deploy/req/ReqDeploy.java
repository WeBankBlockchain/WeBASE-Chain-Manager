package com.webank.webase.chain.mgr.deploy.req;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

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
    @Positive(message = "Tag id error.")
    private int tagId;

    @NotBlank(message = "WeBASE sign address error.")
    private String webaseSignAddr;

    @NotBlank(message = "Chain name error.")
    private String chainName;

    @Size(min = 1,message = "At least 1 host.")
    private List<DeployHost> deployHostList;

    @Data
    @NoArgsConstructor
    public static class DeployHost{
        @NotBlank(message = "IP blank error.")
        private int hostId;



        @NotBlank(message = "SSH user error.")
        private int num;
    }
}

