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
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.type.JdbcType;
import com.webank.webase.chain.mgr.repository.bean.TbChain;

public interface TbChainMapper {

    @Select({ "select count(1) from tb_chain" })
    public int countAll();

    @Select({ "select count(1) from tb_chain where chain_id = #{chainId}" })
    public int countByChainId(@Param("chainId") int chainId);

    @Select({ "select count(1) from tb_chain where chain_name = #{chainName}" })
    public int countByName(@Param("chainName") String chainName);

    @Select({ "select ", TbChainSqlProvider.ALL_COLUMN_FIELDS, " from tb_chain order by chain_name" })
    public List<TbChain> selectAll();

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_chain
     *
     * @mbg.generated
     */
    @Delete({ "delete from tb_chain", "where chain_id = #{chainId,jdbcType=INTEGER}" })
    int deleteByPrimaryKey(Integer chainId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_chain
     *
     * @mbg.generated
     */
    @InsertProvider(type = TbChainSqlProvider.class, method = "insertSelective")
    int insertSelective(TbChain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_chain
     *
     * @mbg.generated
     */
    @Select({ "select", "chain_id, chain_name, chain_type, description, create_time, modify_time", "from tb_chain", "where chain_id = #{chainId,jdbcType=INTEGER}" })
    @Results({ @Result(column = "chain_id", property = "chainId", jdbcType = JdbcType.INTEGER, id = true), @Result(column = "chain_name", property = "chainName", jdbcType = JdbcType.VARCHAR), @Result(column = "chain_type", property = "chainType", jdbcType = JdbcType.TINYINT), @Result(column = "description", property = "description", jdbcType = JdbcType.VARCHAR), @Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP), @Result(column = "modify_time", property = "modifyTime", jdbcType = JdbcType.TIMESTAMP) })
    TbChain selectByPrimaryKey(Integer chainId);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_chain
     *
     * @mbg.generated
     */
    @UpdateProvider(type = TbChainSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(TbChain record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table tb_chain
     *
     * @mbg.generated
     */
    @Options(useGeneratedKeys = true, keyProperty = "chainId", keyColumn = "chain_id")
    @Insert({ "<script>", "insert into tb_chain (chain_id, ", "chain_name, chain_type, ", "description, create_time, ", "modify_time)", "values<foreach collection=\"list\" item=\"detail\" index=\"index\" separator=\",\">(#{detail.chainId,jdbcType=INTEGER}, ", "#{detail.chainName,jdbcType=VARCHAR}, #{detail.chainType,jdbcType=TINYINT}, ", "#{detail.description,jdbcType=VARCHAR}, #{detail.createTime,jdbcType=TIMESTAMP}, ", "#{detail.modifyTime,jdbcType=TIMESTAMP})</foreach></script>" })
    int batchInsert(java.util.List<TbChain> list);
}
