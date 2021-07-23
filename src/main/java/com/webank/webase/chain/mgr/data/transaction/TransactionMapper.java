/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.data.transaction;

import com.webank.webase.chain.mgr.data.block.entity.MinMaxBlock;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.data.transaction.entity.TransListParam;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper about trans hash.
 */
@Repository
public interface TransactionMapper {

    Integer add(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("trans") TbTransaction tbTransaction);

    Integer getCount(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("param") TransListParam param);

    List<String> getAllTranxTables();

    Integer getAllTranxCount(@Param("list") List<String> list, @Param("startTime") long startTime,
        @Param("endTime") long endTime);

    List<TbTransaction> getAllTranxList(@Param("list") List<String> list,
        @Param("startTime") long startTime, @Param("endTime") long endTime);

    Integer getCountByMinMax(@Param("chainId") int chainId, @Param("groupId") int groupId);

    TbTransaction getByHash(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("transHash") String transHash);

    List<TbTransaction> getList(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("param") TransListParam param);

    List<MinMaxBlock> queryMinMaxBlock(@Param("chainId") int chainId, @Param("groupId") int groupId);

    List<String> listOfUnStatTransHash(@Param("chainId") int chainId, @Param("groupId") int groupId);

    void updateTransStatFlag(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("transHash") String transHash);

    void rollback(@Param("chainId") int chainId, @Param("groupId") int groupId,
        @Param("blockNumber") long blockNumber);

    Integer remove(@Param("chainId") int chainId, @Param("groupId") Integer groupId, @Param("subTransNum") Integer subTransNum);


}
