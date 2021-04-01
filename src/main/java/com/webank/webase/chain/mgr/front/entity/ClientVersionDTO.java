package com.webank.webase.chain.mgr.front.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClientVersionDTO {
    @JsonProperty("Build Time")
    private String buildTime;
    @JsonProperty("Build Type")
    private String buildType;
    @JsonProperty("Chain Id")
    private Integer chainId;
    @JsonProperty("FISCO-BCOS Version")
    private String version;
    @JsonProperty("Git Branch")
    private String gitBranch;
    @JsonProperty("Git Commit Hash")
    private String gitCommitHash;
    @JsonProperty("Supported Version")
    private String supportedVersion;
}