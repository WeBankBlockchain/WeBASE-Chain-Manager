package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReqSaveContractBatchVO {
    @NotNull
    private Integer chainId;
    @NotNull
    private Integer groupId;
    private Integer agencyId;
    @NotNull
    private List<BaseContract> contractList;
}
