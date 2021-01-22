/**
 * Copyright 2014-2019  the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.chain.mgr.front.entity;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FrontInfo {

    @ApiModelProperty(value = "链id")
    private Integer chainId;

    @ApiModelProperty(value = "k8s节点peerName", example = "127.0.0.2", required = true)
    @NotBlank
    private String frontPeerName;

    @ApiModelProperty(value = "front的ip地址", example = "127.0.0.2", required = true)
    @NotBlank
    private String frontIp;

    @ApiModelProperty(value = "主机所属公司 ID", example = "10")
//    @Positive(message = "External company id error.")
    private int extCompanyId;

    @ApiModelProperty(value = "主机所属组织名称", required = true)
    @NotBlank
    private String agency;

    @ApiModelProperty(value = "主机所属组织 ID", example = "10", required = true)
    @Positive(message = "External agency id error.")
    private int extAgencyId;

    @ApiModelProperty(value = "主机ID", example = "10", required = true)
//    @Positive(message = "External host id error.")
    private int extHostId;

    @ApiModelProperty(value = "主机 SSH 免密账号，默认 root", example = "root")
    private String sshUser;

    @ApiModelProperty(value = "主机 SSH 端口，默认 22", example = "22")
    private Integer sshPort;

    @ApiModelProperty(value = "front的端口号", example = "5002", required = true)
    @NotNull
    private Integer frontPort;

    @ApiModelProperty(value = "默认的 JSON-RPC 端口", example = "8545")
    private Integer jsonrpcPort;

    @ApiModelProperty(value = "默认的 P2P 端口 ", example = "30300")
    private Integer p2pPort;

    @ApiModelProperty(value = "默认的 Channel 端口", example = "20200")
    private Integer channelPort;
}
