/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.sign.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * import private key entity
 *
 * @author marsli
 */
@Data
public class ReqNewUser {
    @ApiModelProperty(value = "用户名称", example = "testUser", required = true)
    @NotBlank
    private String signUserName;
    @ApiModelProperty(value = "用户id", example = "1SSSaFN1NXH9tfb5")
    private String signUserId;
    @Deprecated
    @ApiModelProperty(value = "所属链Id", example = "1", required = true)
    private String chainId;
	@ApiModelProperty(value = "所属应用id（群组名称或群组id）", example = "group_1_1", required = true)
    @NotBlank
    private String appId;
	@ApiModelProperty(value = "链加密类型（0-ECDS，1-国密）", example = "0")
//    @NotNull
    private Integer encryptType;
	@ApiModelProperty(value = "私钥（导入私钥时用）")
    private String privateKey="";
    private String description;
}
