/**
 * Copyright 2014-2019  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.chain.entity;

import com.webank.webase.chain.mgr.front.entity.FrontInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ChainInfo {
    @NotNull
    @ApiModelProperty(value = "链id")
    private Integer chainId;
    @NotBlank
    @ApiModelProperty(value = "链名称")
    private String chainName;
    private String description;
    @ApiModelProperty(value = "要求部署的webase-front信息列表，最少两台", required = true)
    @Size(min = 1, message = "At least 1 host.")
    List<FrontInfo> frontList;

    @ApiModelProperty(value = "共识机制", example = "pbft")
    private String consensusType;

    @ApiModelProperty(value = "存储方式", example = "rocksdb")
    private String storageType;
    // guomi or ecdsa
    private Integer chainType;
    // fix add chain error
    private Integer deployType;

}
