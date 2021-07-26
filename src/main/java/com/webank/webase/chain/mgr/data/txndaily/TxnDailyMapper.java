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
package com.webank.webase.chain.mgr.data.txndaily;

import com.webank.webase.chain.mgr.data.txndaily.entity.LatestTransCount;
import com.webank.webase.chain.mgr.data.txndaily.entity.TbTxnDaily;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * trans_daily data interface.
 */
@Repository
public interface TxnDailyMapper {

    /**
     * add new tb_txn_daily data.
     */
    Integer addTransDaily(TbTxnDaily tbTxnDaily);

    /**
     * listSeventDayOfTransDaily.
     */
    List<TbTxnDaily> listSeventDayOfTransDaily(@Param(value = "chainId") int chainId,
        @Param("groupId") int groupId);

    /**
     * queryLatestTransCount.
     */
    List<LatestTransCount> queryLatestTransCount(@Param(value = "chainId") int chainId,
        @Param(value = "groupId") int groupId);

    /**
     * delete by chainId.
     */
    Integer deleteByChainId(@Param(value = "chainId") int chainId);

    /**
     * delete by groupId.
     */
    Integer deleteByGroupId(@Param(value = "chainId") int chainId,
        @Param(value = "groupId") int groupId);
}
