package com.webank.webase.chain.mgr.repository.mapper;

import org.apache.ibatis.jdbc.SQL;
import com.webank.webase.chain.mgr.repository.bean.TbFront;

public class TbFrontSqlProvider {

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table tb_front
     *
     * @mbg.generated
     */
    public static final String ALL_COLUMN_FIELDS = "front_id,chain_id,node_id,front_ip,front_port,agency,description,create_time,modify_time";

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_front
     *
     * @mbg.generated
     */
    public String insertSelective(TbFront record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("tb_front");
        if (record.getChainId() != null) {
            sql.VALUES("chain_id", "#{chainId,jdbcType=INTEGER}");
        }
        if (record.getNodeId() != null) {
            sql.VALUES("node_id", "#{nodeId,jdbcType=VARCHAR}");
        }
        if (record.getFrontIp() != null) {
            sql.VALUES("front_ip", "#{frontIp,jdbcType=VARCHAR}");
        }
        if (record.getFrontPort() != null) {
            sql.VALUES("front_port", "#{frontPort,jdbcType=INTEGER}");
        }
        if (record.getAgency() != null) {
            sql.VALUES("agency", "#{agency,jdbcType=VARCHAR}");
        }
        if (record.getDescription() != null) {
            sql.VALUES("description", "#{description,jdbcType=VARCHAR}");
        }
        if (record.getCreateTime() != null) {
            sql.VALUES("create_time", "#{createTime,jdbcType=TIMESTAMP}");
        }
        if (record.getModifyTime() != null) {
            sql.VALUES("modify_time", "#{modifyTime,jdbcType=TIMESTAMP}");
        }
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_front
     *
     * @mbg.generated
     */
    public String updateByPrimaryKeySelective(TbFront record) {
        SQL sql = new SQL();
        sql.UPDATE("tb_front");
        if (record.getChainId() != null) {
            sql.SET("chain_id = #{chainId,jdbcType=INTEGER}");
        }
        if (record.getNodeId() != null) {
            sql.SET("node_id = #{nodeId,jdbcType=VARCHAR}");
        }
        if (record.getFrontIp() != null) {
            sql.SET("front_ip = #{frontIp,jdbcType=VARCHAR}");
        }
        if (record.getFrontPort() != null) {
            sql.SET("front_port = #{frontPort,jdbcType=INTEGER}");
        }
        if (record.getAgency() != null) {
            sql.SET("agency = #{agency,jdbcType=VARCHAR}");
        }
        if (record.getDescription() != null) {
            sql.SET("description = #{description,jdbcType=VARCHAR}");
        }
        if (record.getCreateTime() != null) {
            sql.SET("create_time = #{createTime,jdbcType=TIMESTAMP}");
        }
        if (record.getModifyTime() != null) {
            sql.SET("modify_time = #{modifyTime,jdbcType=TIMESTAMP}");
        }
        sql.WHERE("front_id = #{frontId,jdbcType=INTEGER}");
        return sql.toString();
    }
}
