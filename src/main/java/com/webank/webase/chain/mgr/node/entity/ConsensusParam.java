/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.webank.webase.chain.mgr.node.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class ConsensusParam {
    @ApiModelProperty(value = "链Id", example = "1", required = true)
    @NotNull
    private Integer chainId;
    @ApiModelProperty(value = "当前群组编号", example = "1", required = true)
    @NotNull
    private Integer groupId;
    //    @NotBlank
    @ApiModelProperty(value = "指定请求的节点id", example = "53060c93c5c7bfdc2b35ffae766e5e9f0ca16340f8e4ed09421cbbdb86cc974d57eb6460d41c33a71634f033a898d92486dd5081e2db1672bd426fff6e4af5f8")
    private String reqNodeId;
    @NotBlank
    @ApiModelProperty(value = "节点类型", example = "sealer/observer/remove", required = true)
    private String nodeType;
    //    @NotBlank
    @ApiModelProperty(value = "私钥用户id,如果不传就用默认的", example = "sealer/observer/remove", required = true)
    private String signUserId;
    //    @NotBlank
    @ApiModelProperty(value = "需要更改状态的节点", example = "120j7c93c5c7bfdc2b35ffae766e5e9f0ca16340f8e4ed09421cbbdb46cc974d57e76h60d41c33a71634f033a898d92486dd5081e2db1672bd426fff6e4bgh7k")
    private String nodeId;
    @ApiModelProperty(value = "需要更改状态的节点列表(此字段为后面追加，与nodeId不能同时为空)")
    private List<String> nodeIdList;

}
