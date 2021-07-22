package com.webank.webase.chain.mgr.deploy.req;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * host of deploy chain or add node
 */
@Data
@NoArgsConstructor
public class DeployHost {

    @ApiModelProperty(value = "主机所属公司 ID", example = "10", required = true)
    @Positive(message = "External company id error.")
    private int extCompanyId;

    @ApiModelProperty(value = "主机所属组织在 WebServer 中的ID", example = "10", required = true)
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