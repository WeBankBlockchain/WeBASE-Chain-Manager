package com.webank.webase.chain.mgr.group.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReqUpdateGroupVo {
    @NotNull
    private Integer chainId;
    @NotNull
    private Integer groupId;
    @NotBlank
    private String description;
}
