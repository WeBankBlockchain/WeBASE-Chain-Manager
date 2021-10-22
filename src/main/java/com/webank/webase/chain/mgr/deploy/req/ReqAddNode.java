package com.webank.webase.chain.mgr.deploy.req;

import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import io.swagger.annotations.ApiModelProperty;
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
    private int chainId;

    @ApiModelProperty(value = "链名称，默认等于 chain ID", example = "10")
    @NotBlank(message = "Chain name error.")
    private String chainName;

    @ApiModelProperty(value = "新节点默认群组", example = "1")
    private int groupId = 1;

    @ApiModelProperty(value = "镜像拉取方式, 0 : 手动拉取, 1: 官方自动拉取。默认 0", example = "0")
    private byte dockerImageType = DockerImageTypeEnum.MANUAL.getId();

    // agencyName一个机构，多个主机的节点，放在DeployHost里就是多个机构。用于生成节点的证书和SDK证书
    @ApiModelProperty(value = "机构名，默认等于 Org ID")
    @NotBlank(message = "Agency name error.")
    private String agencyName;

    /**
     * number of node in DeployHost is useless in adding nodes
     */
    @Size(min = 1,message = "At least 1 host.")
    private List<DeployHost> deployHostList;


    @ApiModelProperty(value = "存储方式", example = "rocksdb")
    private String storageType = "rocksdb";
    @ApiModelProperty(value = "链描述", example = "测试链")
    private String description;

}

