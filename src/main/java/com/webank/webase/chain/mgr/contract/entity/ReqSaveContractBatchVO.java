package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReqSaveContractBatchVO {
    @NotNull
    private String chainId;
    @NotNull
    private String groupId;
    private Integer agencyId;
    @NotNull
    private List<BaseContract> contractList;
}
