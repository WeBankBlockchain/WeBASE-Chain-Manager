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

import lombok.Data;

import java.math.BigInteger;

/**
 * TransResultDto.
 * 
 */
@Data
public class TransResultDto {
    private boolean constant;
    private String queryInfo;
    private String transactionHash;
    private String blockHash;
    private BigInteger blockNumber;
    private BigInteger gasUsed;
    private String status;
    private String from;
    private String to;
    private String input;
    private String output;
    private String message;
}
