package com.webank.webase.chain.mgr.repository.mapper;

import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import org.apache.ibatis.jdbc.SQL;

public class TbGroupSqlProvider {

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    public static final String ALL_COLUMN_FIELDS = "group_id,chain_id,group_name,group_status,node_count,description,group_type,create_time,modify_time,group_timestamp,epoch_sealer_num,node_id_list";

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    public String insertSelective(TbGroup record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("tb_group");
        if (record.getGroupId() != null) {
            sql.VALUES("group_id", "#{groupId,jdbcType=INTEGER}");
        }
        if (record.getChainId() != null) {
            sql.VALUES("chain_id", "#{chainId,jdbcType=INTEGER}");
        }
        if (record.getGroupName() != null) {
            sql.VALUES("group_name", "#{groupName,jdbcType=VARCHAR}");
        }
        if (record.getGroupStatus() != null) {
            sql.VALUES("group_status", "#{groupStatus,jdbcType=TINYINT}");
        }
        if (record.getNodeCount() != null) {
            sql.VALUES("node_count", "#{nodeCount,jdbcType=INTEGER}");
        }
        if (record.getDescription() != null) {
            sql.VALUES("description", "#{description,jdbcType=VARCHAR}");
        }
        if (record.getGroupType() != null) {
            sql.VALUES("group_type", "#{groupType,jdbcType=TINYINT}");
        }
        if (record.getCreateTime() != null) {
            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
        }
        if (record.getModifyTime() != null) {
            sql.VALUES("modify_time", "#{modifyTime,jdbcType=TIMESTAMP}");
        }
        if (record.getGroupTimestamp() != null) {
            sql.VALUES("group_timestamp", "#{groupTimestamp,jdbcType=VARCHAR}");
        }
        if (record.getEpochSealerNum() != null) {
            sql.VALUES("epoch_sealer_num", "#{epochSealerNum,jdbcType=INTEGER}");
        }
        if (record.getNodeIdList() != null) {
            sql.VALUES("node_id_list", "#{nodeIdList,jdbcType=LONGVARCHAR}");
        }
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_group
     *
     * @mbg.generated
     */
    public String updateByPrimaryKeySelective(TbGroup record) {
        SQL sql = new SQL();
        sql.UPDATE("tb_group");
        if (record.getGroupName() != null) {
            sql.SET("group_name = #{groupName,jdbcType=VARCHAR}");
        }
        if (record.getGroupStatus() != null) {
            sql.SET("group_status = #{groupStatus,jdbcType=TINYINT}");
        }
        if (record.getNodeCount() != null) {
            sql.SET("node_count = #{nodeCount,jdbcType=INTEGER}");
        }
        if (record.getDescription() != null) {
            sql.SET("description = #{description,jdbcType=VARCHAR}");
        }
        if (record.getGroupType() != null) {
            sql.SET("group_type = #{groupType,jdbcType=TINYINT}");
        }
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
        }
        if (record.getModifyTime() != null) {
            sql.SET("modify_time = #{modifyTime,jdbcType=TIMESTAMP}");
        }
        if (record.getGroupTimestamp() != null) {
            sql.SET("group_timestamp = #{groupTimestamp,jdbcType=VARCHAR}");
        }
        if (record.getEpochSealerNum() != null) {
            sql.SET("epoch_sealer_num = #{epochSealerNum,jdbcType=INTEGER}");
        }
        if (record.getNodeIdList() != null) {
            sql.SET("node_id_list = #{nodeIdList,jdbcType=LONGVARCHAR}");
        }
        sql.WHERE("group_id = #{groupId,jdbcType=INTEGER}");
        sql.WHERE("chain_id = #{chainId,jdbcType=INTEGER}");
        return sql.toString();
    }
}