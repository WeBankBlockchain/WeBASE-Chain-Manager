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
package com.webank.webase.chain.mgr.data.datagroup;


import com.webank.webase.chain.mgr.base.entity.BaseQueryParam;
import com.webank.webase.chain.mgr.data.block.entity.BlockListParam;
import com.webank.webase.chain.mgr.data.block.entity.TbBlock;
import com.webank.webase.chain.mgr.data.datagroup.entity.ContractInfoDto;
import com.webank.webase.chain.mgr.data.datagroup.entity.GroupGeneral;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import com.webank.webase.chain.mgr.data.transaction.entity.TransListParam;
import com.webank.webase.chain.mgr.repository.bean.TbNode;
import java.math.BigInteger;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * mapper for table tb_group.
 */
@Repository
public interface DataGroupMapper {

    /**
     * remove by id.
     */
    int remove(@Param("chainId") String chainId, @Param("groupId") String groupId);

    /**
     * update status.
     */
    int updateStatus(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("groupStatus") Integer groupStatus);

    /**
     * query group count.
     */
    int getCount(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("groupStatus") Integer groupStatus);

    /**
     * query general info.
     */
    GroupGeneral getGeneral(@Param("chainId") String chainId, @Param("groupId") String groupId);

    /**
     * query all block counts.
     */
    BigInteger getBlockCounts();

    /**
     * query all transaction counts.
     */
    BigInteger getTxnCounts();

    /**
     * query block count.
     */
    Integer countOfBlock(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") BlockListParam param);

    /**
     * query list of block by page.
     */
    List<TbBlock> queryBlockList(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") BlockListParam param);

    /**
     * query trans count.
     */
    Integer countOfTrans(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") TransListParam param);

    /**
     * query list of trans by page.
     */
    List<TbTransaction> queryTransList(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") TransListParam param);


    /**
     * query count of trans by app
     */
    Integer queryTransCountByApp(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("appName") String appName);


    /**
     * query count of trans by contract
     */
    Integer queryTransCountByContract(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("contractAddress") String contractAddress);

    /**
     * query contract count.
     */
    Integer countOfContract(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") BaseQueryParam param);

    /**
     * query list of contract by page.
     */
    List<ContractInfoDto> queryContractList(@Param("chainId") String chainId, @Param("groupId") String groupId,
        @Param("param") BaseQueryParam param);

    /**
     * Query the number of node according to some conditions.
     */
    Integer countOfNode(BaseQueryParam param);

    /**
     * Query node list according to some conditions.
     */
    List<TbNode> queryNodeList(BaseQueryParam param);

}
