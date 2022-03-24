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
package com.webank.webase.chain.mgr.data.block;

import com.webank.webase.chain.mgr.data.block.entity.BlockListParam;
import com.webank.webase.chain.mgr.data.block.entity.TbBlock;
import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * Block data interface.
 */
@Repository
public interface BlockMapper {

    /**
     * Add new block data.
     */
    Integer add(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("block") TbBlock tbBlock);

    /**
     * query latest block number
     */
    BigInteger getLatestBlockNumber(@Param("chainId") String chainId, @Param("groupId") String groupId);

    /**
     * findByBlockNumber.
     */
    TbBlock findByBlockNumber(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("blockNumber") long blockNumber);

    /**
     * query list of block by page.
     */
    List<TbBlock> getList(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") BlockListParam param);

    /**
     * query block count.
     */
    Integer getCount(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("blockHash") String blockHash,
        @Param("blockNumber") BigInteger blockNumber);

    /**
     * get block count by max minux min
     */
    Integer getBlockCountByMinMax(@Param("chainId") String chainId, @Param("groupId") String groupId);

    /**
     * Delete block height.
     */
    Integer remove(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("blockRetainMax") BigInteger blockRetainMax);

    /**
     * rollback.
     */
    void rollback(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("blockNumber") long blockNumber);
}
