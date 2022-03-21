package com.webank.webase.chain.mgr.data.datagroup.entity;

import lombok.Data;

@Data
public class TranxCount {
    private String chainId;
    private String groupId;
    private String appName;
    private Integer tranxCount;
    private String contractName;
}
