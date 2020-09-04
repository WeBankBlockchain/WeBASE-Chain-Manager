package com.webank.webase.chain.mgr.contract.entity;

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
    private String contractAddress;
}