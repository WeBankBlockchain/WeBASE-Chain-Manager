package com.webank.webase.chain.mgr.deploy.req;

import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

public class ReqInitHost {
    @ApiModelProperty(value = "Docker 镜像版本", example = "v2.5.0", required = true)
    @NotBlank(message = "Image tag version error.")
    private String version;

    @ApiModelProperty(value = "部署的主机信息列表，最少两台", required = true)
    @Size(min = 1, message = "At least 1 host.")
    private List<ReqDeploy.DeployHost> deployHostList;

    @ApiModelProperty(value = "加密方式：0，非过密；1，国密", example = "0")
    @Min(0)
    @Max(1)
    private int encryptType;

    @ApiModelProperty(value = "镜像拉取方式, 0 : 手动拉取, 1: 官方自动拉取。默认 0", example = "0")
    private byte dockerImageType = DockerImageTypeEnum.PULL_OFFICIAL.getId();

}
