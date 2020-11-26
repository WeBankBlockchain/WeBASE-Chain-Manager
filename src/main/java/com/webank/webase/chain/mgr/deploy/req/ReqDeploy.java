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
    @NotNull(message = "chain id error.")
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
    private byte dockerImageType = DockerImageTypeEnum.PULL_OFFICIAL.getId();

    @ApiModelProperty(value = "共识机制", example = "pbft")
    private String consensusType = "pbft";

    @ApiModelProperty(value = "存储方式", example = "rocksdb")
    private String storageType = "rocksdb";

    @ApiModelProperty(value = "链描述", example = "测试链")
    private String description;

    @ApiModelProperty(value = "链名称，默认等于 chain ID", example = "10")
    private String chainName;

    @Data
    @NoArgsConstructor
    public static class DeployHost {

        @ApiModelProperty(value = "主机所属公司 ID", example = "10", required = true)
        @Positive(message = "External company id error.")
        private int extCompanyId;

        @ApiModelProperty(value = "主机所属组织 ID", example = "10", required = true)
        @Positive(message = "External agency id error.")
        private int extOrgId;

        @ApiModelProperty(value = "主机在 WebServer 中的 ID", example = "10", required = true)
        @Positive(message = "External host id error.")
        private int extHostId;

        @ApiModelProperty(value = "主机 IP", example = "127.0.0.2", required = true)
        @NotBlank(message = "IP blank error.")
        private String ip;

        @ApiModelProperty(value = "主机 SSH 免密账号，默认 root", example = "root")
        private String sshUser = "root";

        @ApiModelProperty(value = "主机 SSH 端口，默认 22", example = "22")
        private int sshPort = 22;

        @ApiModelProperty(value = "Docker daemon 端口，默认 2375，暂不使用", example = "2375")
        private int dockerDemonPort = 2375;

        @ApiModelProperty(value = "主机部署节点数量", example = "1")
        private int num = 1;

        @ApiModelProperty(value = "主机存放节点数据目录", example = "1")
        private String rootDirOnHost = "/root/fisco";


        @ApiModelProperty(value = "默认的 JSON-RPC 端口", example = "8545")
        private int jsonrpcPort = 8545;
        @ApiModelProperty(value = "默认的 P2P 端口 ", example = "30300")
        private int p2pPort = 30300;
        @ApiModelProperty(value = "默认的 Channel 端口", example = "20200")
        private int channelPort = 20200;
        @ApiModelProperty(value = "默认的 Front 端口", example = "5002")
        private int frontPort = 5002;

    }
}

