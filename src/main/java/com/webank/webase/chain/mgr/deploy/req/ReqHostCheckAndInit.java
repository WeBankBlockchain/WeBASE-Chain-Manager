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
public class ReqHostCheckAndInit {
    @Positive(message = "External agency id error.")
    private int extAgencyId;

    @NotBlank(message = "External agency name  error.")
    private String extAgencyName;

    @Size(min = 1,message = "At least 1 host.")
    private List<Host> hostList;

    @Data
    @NoArgsConstructor
    public static class Host{
        @NotBlank(message = "IP blank error.")
        private String ip;

        @NotBlank(message = "SSH user error.")
        private String sshUser;

        @Positive(message = "SSH port error.")
        private int sshPort;

        @Positive(message = "Docker demon port error.")
        private int dockerDemonPort;

        @NotBlank(message = "Root dir on host blank error.")
        private String rootDir="/opt/fisco";
    }
}

