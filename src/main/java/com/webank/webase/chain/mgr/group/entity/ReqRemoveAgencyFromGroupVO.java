package com.webank.webase.chain.mgr.group.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqRemoveAgencyFromGroupVO {
    @NotNull
    private Integer agencyId;
    @NotNull
    private Integer chainId;
    @NotNull
    private Integer groupId;
}
