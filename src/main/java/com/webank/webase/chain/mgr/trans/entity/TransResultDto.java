/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.chain.mgr.trans.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

import java.math.BigInteger;
import org.fisco.bcos.sdk.model.MerkleProofUnit;
import org.fisco.bcos.sdk.model.TransactionReceipt.Logs;

/**
 * TransResultDto.
 * 
 */
@Data
public class TransResultDto {

    private String version;
    private String contractAddress;
    private String gasUsed;
    private int status;
    private String blockNumber;
    private String output;
    private String transactionHash;

    @JsonProperty("hash")
    private String receiptHash;

    private List<Logs> logEntries;
    private String input;
    private String from;
    private String to;
    private List<MerkleProofUnit> transactionProof;
    private List<MerkleProofUnit> receiptProof;
    private String message;

}
