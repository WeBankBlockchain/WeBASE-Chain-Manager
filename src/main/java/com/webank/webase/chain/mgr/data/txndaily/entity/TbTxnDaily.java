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
package com.webank.webase.chain.mgr.data.txndaily.entity;

import java.math.BigInteger;
import java.time.LocalDate;
import lombok.Data;

/**
 * Entity class of table tb_txn_daily.
 */
@Data
public class TbTxnDaily {
    private String chainId;
    private String groupId;
    private LocalDate statDate;
    private Integer txn;
    private BigInteger blockNumber;
}
