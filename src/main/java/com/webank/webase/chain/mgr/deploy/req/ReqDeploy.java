package com.webank.webase.chain.mgr.deploy.req;

import java.util.List;

import javax.validation.constraints.*;

import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 */

@Data
@NoArgsConstructor
@ApiModel(description = "部署链请求")
public class ReqDeploy {

    @ApiModelProperty(value = "Chain id，由 WebServer 生成", example = "10", required = true)
    private int chainId;

    @ApiModelProperty(value = "Docker 镜像版本", example = "v2.5.0", required = true)
    @NotBlank(message = "Image tag version error.")
    private String version;

    @ApiModelProperty(value = "部署的主机信息列表，最少两台", required = true)
    @Size(min = 1, message = "At least 1 host.")
    private List<DeployHost> deployHostList;

    @ApiModelProperty(value = "加密方式：0，非过密；1，国密", example = "0")
    @Min(0)
    @Max(1)
    private int encryptType;

    @ApiModelProperty(value = "镜像拉取方式, 0 : 手动拉取, 1: 官方自动拉取。默认 0", example = "0")
    private byte dockerImageType = DockerImageTypeEnum.MANUAL.getId();

    @ApiModelProperty(value = "共识机制", example = "pbft")
    private String consensusType = "pbft";

    @ApiModelProperty(value = "存储方式", example = "rocksdb")
    private String storageType = "rocksdb";

    @ApiModelProperty(value = "链描述", example = "测试链")
    private String description;

    @ApiModelProperty(value = "链名称，默认等于 chain ID", example = "10")
    private String chainName;


}

