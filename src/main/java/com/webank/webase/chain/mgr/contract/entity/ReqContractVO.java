package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 *
 */
@Data
public class ReqContractVO {
    @NotNull
    private Integer contractId;
}
