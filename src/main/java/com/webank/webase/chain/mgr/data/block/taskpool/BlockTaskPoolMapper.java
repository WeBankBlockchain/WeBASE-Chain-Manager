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
package com.webank.webase.chain.mgr.data.block.taskpool;

import com.webank.webase.chain.mgr.data.block.entity.TbBlockTaskPool;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Block data interface.
 */
@Repository
public interface BlockTaskPoolMapper {

    Integer saveAll(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("list") List<TbBlockTaskPool> list);

    BigInteger getLatestBlockNumber(@Param("chainId") int chainId, @Param("groupId") int groupId);

    List<TbBlockTaskPool> findBySyncStatusOrderByBlockHeightLimit(
        @Param("tableName") String tableName,
        @Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("syncStatus") int syncStatus,
        @Param("limit") int limit);

    TbBlockTaskPool findByBlockNumber(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("blockNumber") long blockNumber);

    Integer setSyncStatusByBlockHeight(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("syncStatus") int syncStatus, @Param("blockNumber") long blockNumber);

    List<TbBlockTaskPool> findByCertainty(@Param("chainId") int chainId,
        @Param("groupId") int groupId,
        @Param("certainty") int certainty);

    Integer setCertaintyByBlockHeight(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("certainty") int certainty, @Param("blockNumber") long blockNumber);

    Integer setSyncStatusAndCertaintyByBlockHeight(@Param("chainId") int chainId,
        @Param("groupId") int groupId,
        @Param("syncStatus") int syncStatus, @Param("certainty") int certainty,
        @Param("blockNumber") long blockNumber);

    List<TbBlockTaskPool> findUnNormalRecords(@Param("chainId") int chainId,
        @Param("groupId") int groupId);

    List<TbBlockTaskPool> findByBlockHeightRange(@Param("chainId") int chainId,
        @Param("groupId") int groupId,
        @Param("startNumber") long startNumber, @Param("endNumber") long endNumber);

    List<TbBlockTaskPool> findBySyncStatusAndDepotUpdatetimeLessThan(@Param("chainId") int chainId,
        @Param("groupId") int groupId, @Param("syncStatus") int syncStatus,
        @Param("time") Date time);

    long countByBlockHeightRange(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("startNumber") long startNumber, @Param("endNumber") long endNumber);
}
