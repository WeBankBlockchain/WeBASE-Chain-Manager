package com.webank.webase.chain.mgr.data.txndaily.entity;

import java.math.BigInteger;
import java.time.LocalDate;
import lombok.Data;

/**
 * result of latest transCount.
 */
@Data
public class LatestTransCount {
    private Integer txn;
    private BigInteger blockNumber;
    private LocalDate statDate;
}
