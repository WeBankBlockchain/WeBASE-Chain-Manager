package com.webank.webase.chain.mgr.repository.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;

import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.repository.bean.TbContract;

public interface TbContractMapper {

//    @Select({ "select * from tb_contract where group_id = #{groupId} and contract_bin like CONCAT(#{contractBin},'%')" })
//    List<TbContract> selectByBin(@Param("groupId") Integer groupId, @Param("contractBin") String contractBin);

//    @Select({ "select contract_bin from tb_contract where group_id = #{groupId} and contract_name = #{contractName}" })
//    String getSystemContractBin(@Param("groupId") Integer groupId, @Param("contractName") String contractName);

    @SelectProvider(type = TbContractSqlProvider.class, method = "getByParam")
    TbContract getByParam(ContractParam param);

    @SelectProvider(type = TbContractSqlProvider.class, method = "selectByParam")
    List<TbContract> selectByParam(ContractParam param);

    @UpdateProvider(type = TbContractSqlProvider.class, method = "countByParam")
    int countByParam(ContractParam param);

    @Update({ "update tb_contract set contract_bin = #{contractBin}, " + "contract_address = #{contractAddress} " + "where group_id = #{groupId} and contract_name = #{contractName}" })
    int updateSystemContract(@Param("groupId") Integer groupId, @Param("contractName") String contractName, @Param("contractBin") String contractBin, @Param("contractAddress") String contractAddress);

    @Delete({ "delete from tb_contract where chain_id = #{chainId}" })
    int deleteByChainId(@Param("chainId") Integer chainId);

    @Delete({ "delete from tb_contract where chain_id = #{chainId} and group_id = #{groupId}" })
    int deleteByChainIdAndGroupId(@Param("chainId") Integer chainId, @Param("groupId") Integer groupId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_contract
     *
     * @mbg.generated
     */
    @Delete({ "delete from tb_contract", "where contract_id = #{contractId,jdbcType=INTEGER}" })
    int deleteByPrimaryKey(Integer contractId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_contract
     *
     * @mbg.generated
     */
    @InsertProvider(type = TbContractSqlProvider.class, method = "insertSelective")
    int insertSelective(TbContract record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_contract
     *
     * @mbg.generated
     */
    @Select({ "select", "contract_id, contract_path, contract_name, chain_id, group_id, contract_address, ", "deploy_time, contract_status, contract_type, description, create_time, modify_time, ", "contract_source, contract_abi, contract_bin, bytecode_bin", "from tb_contract", "where contract_id = #{contractId,jdbcType=INTEGER}" })
    @Results({ @Result(column = "contract_id", property = "contractId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "contract_path", property = "contractPath", jdbcType = JdbcType.VARCHAR), @Result(column = "contract_name", property = "contractName", jdbcType = JdbcType.VARCHAR), @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER), @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER), @Result(column = "contract_address", property = "contractAddress", jdbcType = JdbcType.VARCHAR), @Result(column = "deploy_time", property = "deployTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "contract_status", property = "contractStatus", jdbcType = JdbcType.TINYINT), @Result(column = "contract_type", property = "contractType", jdbcType = JdbcType.TINYINT), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "contract_source", property = "contractSource", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "contract_abi", property = "contractAbi", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "contract_bin", property = "contractBin", jdbcType = JdbcType.LONGVARCHAR), @Result(column = "bytecode_bin", property = "bytecodeBin", jdbcType = JdbcType.LONGVARCHAR) })
    TbContract selectByPrimaryKey(Integer contractId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_contract
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbContractSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbContract record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_contract
     *
     * @mbg.generated
     */
    @Options(useGeneratedKeys = true, keyProperty = "contractId", keyColumn = "contract_id")
    @Insert({ "<script>", "insert into tb_contract (contract_id, ", "contract_path, contract_name, ", "chain_id, group_id, ", "contract_address, deploy_time, ", "contract_status, contract_type, ", "description, create_time, ", "modify_time, contract_source, ", "contract_abi, contract_bin, ", "bytecode_bin)", "values<foreach collection=\"list\" item=\"detail\" index=\"index\" separator=\",\">(#{detail.contractId,jdbcType=INTEGER}, ", "#{detail.contractPath,jdbcType=VARCHAR}, #{detail.contractName,jdbcType=VARCHAR}, ", "#{detail.chainId,jdbcType=INTEGER}, #{detail.groupId,jdbcType=INTEGER}, ", "#{detail.contractAddress,jdbcType=VARCHAR}, #{detail.deployTime,jdbcType=TIMESTAMP}, ", "#{detail.contractStatus,jdbcType=TINYINT}, #{detail.contractType,jdbcType=TINYINT}, ", "#{detail.description,jdbcType=VARCHAR}, #{detail.createTime,jdbcType=TIMESTAMP}, ", "#{detail.modifyTime,jdbcType=TIMESTAMP}, #{detail.contractSource,jdbcType=LONGVARCHAR}, ", "#{detail.contractAbi,jdbcType=LONGVARCHAR}, #{detail.contractBin,jdbcType=LONGVARCHAR}, ", "#{detail.bytecodeBin,jdbcType=LONGVARCHAR})</foreach></script>" })
    int batchInsert(java.util.List<TbContract> list);
}
