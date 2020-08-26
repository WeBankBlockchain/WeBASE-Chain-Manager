package com.webank.webase.chain.mgr.contract.entity;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Data
@NoArgsConstructor
public class RespContractDeploy {
    private String transactionHash;
    private long transactionIndex;
    private String blockHash;
    private long blockNumber;
    private long gasUsed;
    private String contractAddress;
    private String root;
    private String status;
    private String message;
    private String from;
    private String to;
    private String input;
    private String output;
    private List<String> logs = null;
    private String logsBloom;
    private String txProof;
    private String receiptProof;
}