package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class BaseContract {
    private Integer contractId;
    @NotBlank
    private String contractName;
    @NotBlank
    private String contractPath;
    private String contractSource;
    private String contractAbi;
    private String contractBin;
    private String bytecodeBin;
}
