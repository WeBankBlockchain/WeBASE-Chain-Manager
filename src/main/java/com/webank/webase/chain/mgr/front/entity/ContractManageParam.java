/**
 * Copyright 2014-2019 the original author or authors.
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
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * param of contract manage.
 */
@Data
@NoArgsConstructor
public class ContractManageParam {
    @NotNull
    private String chainId;
    @NotNull
    private String groupId;
    @NotBlank
    private String nodeId;
    @NotBlank
    private String signUserId;
    @NotBlank
    private String contractAddress;
    @NotBlank
    private String handleType;
    private String grantAddress;
}
