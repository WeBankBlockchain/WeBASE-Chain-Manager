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
package com.webank.webase.chain.mgr.data.group.entity;

import java.math.BigInteger;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GroupGeneral {

    private Integer chainId;
    private Integer groupId;
    private int nodeCount = 0;
    private int userCount = 0;
    private int contractCount = 0;
    private BigInteger txnCount = BigInteger.ZERO;
    private BigInteger blockNumber = BigInteger.ZERO;

    public GroupGeneral(Integer chainId, Integer groupId, int nodeCount, int contractCount) {
        this.chainId = chainId;
        this.groupId = groupId;
        this.nodeCount = nodeCount;
        this.contractCount = contractCount;
    }

}
