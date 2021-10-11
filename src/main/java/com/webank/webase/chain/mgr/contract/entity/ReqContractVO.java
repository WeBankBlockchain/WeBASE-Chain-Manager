package com.webank.webase.chain.mgr.contract.entity;

import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 *
 */
@Data
public class ReqContractVO {
    @NotNull
    private Integer contractId;
    /**
     * if force, delete no matter deployed
     */
    private Boolean force = false;
}
