package com.webank.webase.chain.mgr.data.overview;

import lombok.Data;

@Data
public class TranxCount {
    private String chainId;
    private String groupId;
    private Long startTime;
    private Long endTime;
    private int tranxCount;
}
