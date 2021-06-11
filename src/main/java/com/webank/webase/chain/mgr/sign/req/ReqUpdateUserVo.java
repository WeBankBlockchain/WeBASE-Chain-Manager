package com.webank.webase.chain.mgr.sign.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ReqUpdateUserVo {

    @ApiModelProperty(value = "用户id", example = "1SSSaFN1NXH9tfb5", required = true)
    @NotBlank
    private String signUserId;
    @ApiModelProperty(value = "用户描述信息", example = "test user")
    private String description;
}
