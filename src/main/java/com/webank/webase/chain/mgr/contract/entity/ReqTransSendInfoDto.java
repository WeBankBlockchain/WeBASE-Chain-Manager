/*
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.contract.entity;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * transaction request parameters.
 *
 */
@Data
public class ReqTransSendInfoDto {
    @NotNull
    private String chainId;
    @NotNull
    private String groupId;
    @NotBlank
    private String contractAddress;
    @NotBlank
    private String signUserId;
    @NotBlank
    private String funcName;
    @NotEmpty
    private List<Object> functionAbi = new ArrayList<>();
    private List<Object> funcParam = new ArrayList<>();
}
