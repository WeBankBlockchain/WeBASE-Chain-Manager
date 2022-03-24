/**
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
package com.webank.webase.chain.mgr.data.transaction.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_transaction
 */
@Data
@NoArgsConstructor
public class TbTransaction {

    private long id;
    private Integer chainType = 0;
    private String chainId;
    private String groupId;
    private String chainName;
    private String appName;
    private String transHash;
    private BigInteger blockNumber;
    private LocalDateTime blockTimestamp;
    private String transDetail;
    private String receiptDetail;
    private Integer auditFlag;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbTransaction(String chainId, String groupId, String transHash, BigInteger blockNumber,
            LocalDateTime blockTimestamp, String transDetail) {
        this.chainId = chainId;
        this.groupId = groupId;
        this.transHash = transHash;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
        this.transDetail = transDetail;
    }

}
