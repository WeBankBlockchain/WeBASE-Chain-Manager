/**
 * Copyright 2014-2019  the original author or authors.
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

package com.webank.webase.chain.mgr.front.entity;

import java.math.BigInteger;
import org.fisco.bcos.web3j.utils.Numeric;

public class TransactionCount {

    private String txSum;
    private String blockNumber;

    public TransactionCount() {}

    public TransactionCount(String txSum, String blockNumber) {
        this.txSum = txSum;
        this.blockNumber = blockNumber;
    }

    public BigInteger getTxSum() {
        return Numeric.decodeQuantity(txSum);
    }

    public void setTxSum(String txSum) {
        this.txSum = txSum;
    }

    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(blockNumber);
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }
}
