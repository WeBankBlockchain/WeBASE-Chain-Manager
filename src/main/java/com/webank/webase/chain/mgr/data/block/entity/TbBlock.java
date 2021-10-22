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
package com.webank.webase.chain.mgr.data.block.entity;

import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_block.
 */
@Data
@NoArgsConstructor
public class TbBlock {
    private long id;
    private String blockHash;
    private BigInteger blockNumber = BigInteger.ZERO;
    private LocalDateTime blockTimestamp;
    private int transCount;
    private int sealerIndex;
    private String sealer;
    private String blockDetail;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    /**
     * init by blockHash、blockNumber、blockTimestamp、transCount.
     */
    public TbBlock(String blockHash, BigInteger blockNumber, LocalDateTime blockTimestamp,
            Integer transCount, int sealerIndex, String sealer, String blockDetail) {
        super();
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.blockTimestamp = blockTimestamp;
        this.transCount = transCount;
        this.sealerIndex = sealerIndex;
        this.sealer = sealer;
        this.blockDetail = blockDetail;
    }
}
