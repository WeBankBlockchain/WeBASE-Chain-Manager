package com.webank.webase.chain.mgr.data.datagroup.entity;

import lombok.Data;

@Data
public class TranxCount {
    private Integer chainId;
    private Integer groupId;
    private String appName;
    private Integer tranxCount;
    private String contractName;
}
