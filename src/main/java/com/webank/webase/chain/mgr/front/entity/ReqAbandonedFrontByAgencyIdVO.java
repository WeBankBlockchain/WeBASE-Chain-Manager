package com.webank.webase.chain.mgr.front.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReqAbandonedFrontByAgencyIdVO {
    @NotNull
    private Integer agencyId;
}
