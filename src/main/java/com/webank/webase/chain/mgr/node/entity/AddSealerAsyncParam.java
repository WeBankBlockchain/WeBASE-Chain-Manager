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
public class AddSealerAsyncParam {
    @ApiModelProperty(value = "链Id", example = "1", required = true)
    @NotNull
    private Integer chainId;
    @ApiModelProperty(value = "当前群组编号", example = "1", required = true)
    @NotNull
    private Integer groupId;
    @ApiModelProperty(value = "需要更改状态的节点列表")
    @NotNull
    private List<String> nodeIdList;

}
