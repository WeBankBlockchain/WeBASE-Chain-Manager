package com.webank.webase.chain.mgr.repository.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import com.webank.webase.chain.mgr.group.entity.GroupGeneral;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import com.webank.webase.chain.mgr.repository.bean.TbGroupExample;
import java.util.Optional;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.SelectProvider;

public interface TbGroupMapper {

    @Select({ "select max(group_id) from tb_group  where chain_id = #{chainId}" })
    int getMaxGroup(@Param("chainId") int chainId);

    @Delete({ "delete from tb_group  where chain_id = #{chainId}" })
    int deleteByChainId(@Param("chainId") int chainId);

    @Delete({ "delete from tb_group  where chain_id = #{chainId} and group_id = #{groupId}" })
    int deleteByChainIdANdGroupId(@Param("chainId") int chainId, @Param("groupId") int groupId);

    @Update({ "update tb_group set group_status=#{groupStatus},modify_time=NOW() where group_id = #{groupId} and chain_id = #{chainId}" })
    int updateStatus(@Param("chainId") Integer chainId, @Param("groupId") Integer groupId, @Param("groupStatus") Integer groupStatus);

    @Select({ "select count(1) from tb_group where chain_id = #{chainId} and group_status = #{groupStatus}" })
    int countByChainIdAndGroupStatus(@Param("chainId") Integer chainId, @Param("groupStatus") Byte groupStatus);

    @Select({ "select count(1) from tb_group where chain_id = #{chainId} and group_id = #{groupId}" })
    int countByChainIdAndGroupId(@Param("chainId") Integer chainId, @Param("groupId") Integer groupId);

    @Select({ "select * from tb_group where chain_id = #{chainId} and group_status = #{groupStatus} order by group_id asc" })
    List<TbGroup> selectByChainIdAndGroupStatus(@Param("chainId") Integer chainId, @Param("groupStatus") Byte groupStatus);

    @Select({ "select * from tb_group where chain_id = #{chainId} order by group_id asc" })
    List<TbGroup> selectByChainId(@Param("chainId") Integer chainId);

    @Select({ "select * from tb_group where group_status = #{groupStatus} order by group_id asc" })
    List<TbGroup> selectByGroupStatus(@Param("groupStatus") Byte groupStatus);

    @Select({ " SELECT a.group_id as group_id,a.chain_id as chain_id," + " a.node_count as node_count,b.contractCount as contract_count" + " FROM tb_group a" + " LEFT 	JOIN" + "(" + "        select group_id,count(1) contractCount" + "        from tb_contract" + "        where contract_type= 0" + "        and contract_status=2" + "        and chain_id = #{chainId}" + " GROUP BY group_id" + ")b on(a.group_id = b.group_id)" + " where a.group_id = #{groupId}" + " and a.chain_id = #{chainId}" })
    GroupGeneral getGeneral(@Param("chainId") Integer chainId, @Param("groupId") Integer groupId);

    @Update({ "update tb_group set node_count=#{nodeCount},modify_time=NOW() where chain_id=#{chainId} and group_id=#{groupId}" })
    int updateNodeCount(@Param("chainId") int chainId, @Param("groupId") int groupId, @Param("nodeCount") int nodeCount);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @SelectProvider(type = TbGroupSqlProvider.class, method = "countByExample")
    long countByExample(TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @DeleteProvider(type = TbGroupSqlProvider.class, method = "deleteByExample")
    int deleteByExample(TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Delete({ "delete from tb_group", "where group_id = #{groupId,jdbcType=INTEGER}", "and chain_id = #{chainId,jdbcType=INTEGER}" })
    int deleteByPrimaryKey(@Param("groupId") Integer groupId, @Param("chainId") Integer chainId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Insert({ "insert into tb_group (group_id, chain_id, ", "group_name, group_status, ", "node_count, description, ", "group_type, create_time, ", "modify_time, group_timestamp, ", "epoch_sealer_num, node_id_list)", "values (#{groupId,jdbcType=INTEGER}, #{chainId,jdbcType=INTEGER}, ", "#{groupName,jdbcType=VARCHAR}, #{groupStatus,jdbcType=TINYINT}, ", "#{nodeCount,jdbcType=INTEGER}, #{description,jdbcType=VARCHAR}, ", "#{groupType,jdbcType=TINYINT}, #{createTime,jdbcType=TIMESTAMP}, ", "#{modifyTime,jdbcType=TIMESTAMP}, #{groupTimestamp,jdbcType=VARCHAR}, ", "#{epochSealerNum,jdbcType=INTEGER}, #{nodeIdList,jdbcType=LONGVARCHAR})" })
    int insert(TbGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @InsertProvider(type = TbGroupSqlProvider.class, method = "insertSelective")
    int insertSelective(TbGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @SelectProvider(type = TbGroupSqlProvider.class, method = "selectByExampleWithBLOBs")
    @Results({ @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "group_name", property = "groupName", jdbcType = JdbcType.VARCHAR), @Result(column = "group_status", property = "groupStatus", jdbcType = JdbcType.TINYINT), @Result(column = "node_count", property = "nodeCount", jdbcType = JdbcType.INTEGER), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "group_type", property = "groupType", jdbcType = JdbcType.TINYINT), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "group_timestamp", property = "groupTimestamp", jdbcType = JdbcType.VARCHAR), @Result(column = "epoch_sealer_num", property = "epochSealerNum", jdbcType = JdbcType.INTEGER), @Result(column = "node_id_list", property = "nodeIdList", jdbcType = JdbcType.LONGVARCHAR) })
    List<TbGroup> selectByExampleWithBLOBs(TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @SelectProvider(type = TbGroupSqlProvider.class, method = "selectByExample")
    @Results({ @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "group_name", property = "groupName", jdbcType = JdbcType.VARCHAR), @Result(column = "group_status", property = "groupStatus", jdbcType = JdbcType.TINYINT), @Result(column = "node_count", property = "nodeCount", jdbcType = JdbcType.INTEGER), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "group_type", property = "groupType", jdbcType = JdbcType.TINYINT), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "group_timestamp", property = "groupTimestamp", jdbcType = JdbcType.VARCHAR), @Result(column = "epoch_sealer_num", property = "epochSealerNum", jdbcType = JdbcType.INTEGER) })
    List<TbGroup> selectByExample(TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Select({ "select", "group_id, chain_id, group_name, group_status, node_count, description, group_type, ", "create_time, modify_time, group_timestamp, epoch_sealer_num, node_id_list", "from tb_group", "where group_id = #{groupId,jdbcType=INTEGER}", "and chain_id = #{chainId,jdbcType=INTEGER}" })
    @Results({ @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "group_name", property = "groupName", jdbcType = JdbcType.VARCHAR), @Result(column = "group_status", property = "groupStatus", jdbcType = JdbcType.TINYINT), @Result(column = "node_count", property = "nodeCount", jdbcType = JdbcType.INTEGER), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "group_type", property = "groupType", jdbcType = JdbcType.TINYINT), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "group_timestamp", property = "groupTimestamp", jdbcType = JdbcType.VARCHAR), @Result(column = "epoch_sealer_num", property = "epochSealerNum", jdbcType = JdbcType.INTEGER), @Result(column = "node_id_list", property = "nodeIdList", jdbcType = JdbcType.LONGVARCHAR) })
    TbGroup selectByPrimaryKey(@Param("groupId") Integer groupId, @Param("chainId") Integer chainId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbGroupSqlProvider.class, method = "updateByExampleSelective")
    int updateByExampleSelective(@Param("record") TbGroup record, @Param("example") TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbGroupSqlProvider.class, method = "updateByExampleWithBLOBs")
    int updateByExampleWithBLOBs(@Param("record") TbGroup record, @Param("example") TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbGroupSqlProvider.class, method = "updateByExample")
    int updateByExample(@Param("record") TbGroup record, @Param("example") TbGroupExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbGroupSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Update({ "update tb_group", "set group_name = #{groupName,jdbcType=VARCHAR},", "group_status = #{groupStatus,jdbcType=TINYINT},", "node_count = #{nodeCount,jdbcType=INTEGER},", "description = #{description,jdbcType=VARCHAR},", "group_type = #{groupType,jdbcType=TINYINT},", "create_time = #{createTime,jdbcType=TIMESTAMP},", "modify_time = #{modifyTime,jdbcType=TIMESTAMP},", "group_timestamp = #{groupTimestamp,jdbcType=VARCHAR},", "epoch_sealer_num = #{epochSealerNum,jdbcType=INTEGER},", "node_id_list = #{nodeIdList,jdbcType=LONGVARCHAR}", "where group_id = #{groupId,jdbcType=INTEGER}", "and chain_id = #{chainId,jdbcType=INTEGER}" })
    int updateByPrimaryKeyWithBLOBs(TbGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Update({ "update tb_group", "set group_name = #{groupName,jdbcType=VARCHAR},", "group_status = #{groupStatus,jdbcType=TINYINT},", "node_count = #{nodeCount,jdbcType=INTEGER},", "description = #{description,jdbcType=VARCHAR},", "group_type = #{groupType,jdbcType=TINYINT},", "create_time = #{createTime,jdbcType=TIMESTAMP},", "modify_time = #{modifyTime,jdbcType=TIMESTAMP},", "group_timestamp = #{groupTimestamp,jdbcType=VARCHAR},", "epoch_sealer_num = #{epochSealerNum,jdbcType=INTEGER}", "where group_id = #{groupId,jdbcType=INTEGER}", "and chain_id = #{chainId,jdbcType=INTEGER}" })
    int updateByPrimaryKey(TbGroup record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @Insert({ "<script>", "insert into tb_group (group_id, ", "chain_id, group_name, ", "group_status, node_count, ", "description, group_type, ", "create_time, modify_time, ", "group_timestamp, epoch_sealer_num, ", "node_id_list)", "values<foreach collection=\"list\" item=\"detail\" index=\"index\" separator=\",\">(#{detail.groupId,jdbcType=INTEGER}, ", "#{detail.chainId,jdbcType=INTEGER}, #{detail.groupName,jdbcType=VARCHAR}, ", "#{detail.groupStatus,jdbcType=TINYINT}, #{detail.nodeCount,jdbcType=INTEGER}, ", "#{detail.description,jdbcType=VARCHAR}, #{detail.groupType,jdbcType=TINYINT}, ", "#{detail.createTime,jdbcType=TIMESTAMP}, #{detail.modifyTime,jdbcType=TIMESTAMP}, ", "#{detail.groupTimestamp,jdbcType=VARCHAR}, #{detail.epochSealerNum,jdbcType=INTEGER}, ", "#{detail.nodeIdList,jdbcType=LONGVARCHAR})</foreach></script>" })
    int batchInsert(List<TbGroup> list);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    @SelectProvider(type = TbGroupSqlProvider.class, method = "getOneByExample")
    @Results({ @Result(column = "group_id", property = "groupId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "group_name", property = "groupName", jdbcType = JdbcType.VARCHAR), @Result(column = "group_status", property = "groupStatus", jdbcType = JdbcType.TINYINT), @Result(column = "node_count", property = "nodeCount", jdbcType = JdbcType.INTEGER), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "group_type", property = "groupType", jdbcType = JdbcType.TINYINT), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "group_timestamp", property = "groupTimestamp", jdbcType = JdbcType.VARCHAR), @Result(column = "epoch_sealer_num", property = "epochSealerNum", jdbcType = JdbcType.INTEGER), @Result(column = "node_id_list", property = "nodeIdList", jdbcType = JdbcType.LONGVARCHAR) })
    Optional<TbGroup> getOneByExample(TbGroupExample example);
}
