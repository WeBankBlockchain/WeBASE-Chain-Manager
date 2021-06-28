package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReqSaveContractListVO {
    @NotNull
    private Integer chainId;
    @NotNull
    private Integer groupId;
    @NotNull
    private List<Contract> contractList;
}
