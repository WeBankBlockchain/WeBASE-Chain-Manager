package com.webank.webase.chain.mgr.chain.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqDeleteChainVo {
    @NotNull
    private Integer chainId;
}
