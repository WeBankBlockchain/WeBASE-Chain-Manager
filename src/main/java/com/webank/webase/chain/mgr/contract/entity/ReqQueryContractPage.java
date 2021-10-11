package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import java.util.List;

@Data
public class ReqQueryContractPage {
    private Integer pageNumber;
    private Integer pageSize;
    private Byte contractStatus;
    private Boolean containDetailFields;
    private List<Integer> chainIds;
    private List<String> appIds;
}
