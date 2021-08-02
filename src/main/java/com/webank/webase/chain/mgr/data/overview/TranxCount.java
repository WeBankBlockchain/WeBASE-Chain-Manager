package com.webank.webase.chain.mgr.data.overview;

import lombok.Data;

@Data
public class TranxCount {
    private Integer chainId;
    private Integer groupId;
    private Long startTime;
    private Long endTime;
    private int tranxCount;
}
