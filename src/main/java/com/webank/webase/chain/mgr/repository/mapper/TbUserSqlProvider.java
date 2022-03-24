package com.webank.webase.chain.mgr.repository.mapper;

import com.webank.webase.chain.mgr.repository.bean.TbUser;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample.Criteria;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample.Criterion;
import com.webank.webase.chain.mgr.repository.bean.TbUserExample;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.jdbc.SQL;

public class TbUserSqlProvider {

    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public static final String ALL_COLUMN_FIELDS = "id,sign_user_id,user_name,address,chain_id,group_id,user_status,gmt_create,gmt_modified,description";

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String countByExample(TbUserExample example) {
        SQL sql = new SQL();
        sql.SELECT("count(*)").FROM("tb_user");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String deleteByExample(TbUserExample example) {
        SQL sql = new SQL();
        sql.DELETE_FROM("tb_user");
        applyWhere(sql, example, false);
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String insertSelective(TbUser record) {
        SQL sql = new SQL();
        sql.INSERT_INTO("tb_user");
        if (record.getSignUserId() != null) {
            sql.VALUES("sign_user_id", "#{signUserId,jdbcType=VARCHAR}");
        }
        if (record.getUserName() != null) {
            sql.VALUES("user_name", "#{userName,jdbcType=VARCHAR}");
        }
        if (record.getAddress() != null) {
            sql.VALUES("address", "#{address,jdbcType=VARCHAR}");
        }
        if (record.getChainId() != null) {
            sql.VALUES("chain_id", "#{chainId,jdbcType=INTEGER}");
        }
        if (record.getGroupId() != null) {
            sql.VALUES("group_id", "#{groupId,jdbcType=INTEGER}");
        }
        if (record.getUserStatus() != null) {
            sql.VALUES("user_status", "#{userStatus,jdbcType=INTEGER}");
        }
        if (record.getGmtCreate() != null) {
            sql.VALUES("gmt_create", "#{gmtCreate,jdbcType=TIMESTAMP}");
        }
        if (record.getGmtModified() != null) {
            sql.VALUES("gmt_modified", "#{gmtModified,jdbcType=TIMESTAMP}");
        }
        if (record.getDescription() != null) {
            sql.VALUES("description", "#{description,jdbcType=VARCHAR}");
        }
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String selectByExample(TbUserExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("sign_user_id");
        sql.SELECT("user_name");
        sql.SELECT("address");
        sql.SELECT("chain_id");
        sql.SELECT("group_id");
        sql.SELECT("user_status");
        sql.SELECT("gmt_create");
        sql.SELECT("gmt_modified");
        sql.SELECT("description");
        sql.FROM("tb_user");
        applyWhere(sql, example, false);
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        // add pagination for mysql with limit clause 
        StringBuilder sqlBuilder = new StringBuilder(sql.toString());
        if (example != null && (example.getStart() > -1 || example.getCount() > -1)) {
            sqlBuilder.append(" limit ");
            if (example.getStart() > -1 && example.getCount() > -1) {
                sqlBuilder.append(example.getStart()).append(",").append(example.getCount());
            } else if (example.getStart() > -1) {
                sqlBuilder.append(example.getStart());
            } else if (example.getCount() > -1) {
                sqlBuilder.append(example.getCount());
            }
        }
        return sqlBuilder.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String updateByExampleSelective(Map<String, Object> parameter) {
        TbUser record = (TbUser) parameter.get("record");
        TbUserExample example = (TbUserExample) parameter.get("example");
        SQL sql = new SQL();
        sql.UPDATE("tb_user");
        if (record.getId() != null) {
            sql.SET("id = #{record.id,jdbcType=INTEGER}");
        }
        if (record.getSignUserId() != null) {
            sql.SET("sign_user_id = #{record.signUserId,jdbcType=VARCHAR}");
        }
        if (record.getUserName() != null) {
            sql.SET("user_name = #{record.userName,jdbcType=VARCHAR}");
        }
        if (record.getAddress() != null) {
            sql.SET("address = #{record.address,jdbcType=VARCHAR}");
        }
        if (record.getChainId() != null) {
            sql.SET("chain_id = #{record.chainId,jdbcType=INTEGER}");
        }
        if (record.getGroupId() != null) {
            sql.SET("group_id = #{record.groupId,jdbcType=INTEGER}");
        }
        if (record.getUserStatus() != null) {
            sql.SET("user_status = #{record.userStatus,jdbcType=INTEGER}");
        }
        if (record.getGmtCreate() != null) {
            sql.SET("gmt_create = #{record.gmtCreate,jdbcType=TIMESTAMP}");
        }
        if (record.getGmtModified() != null) {
            sql.SET("gmt_modified = #{record.gmtModified,jdbcType=TIMESTAMP}");
        }
        if (record.getDescription() != null) {
            sql.SET("description = #{record.description,jdbcType=VARCHAR}");
        }
        applyWhere(sql, example, true);
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String updateByExample(Map<String, Object> parameter) {
        SQL sql = new SQL();
        sql.UPDATE("tb_user");
        sql.SET("id = #{record.id,jdbcType=INTEGER}");
        sql.SET("sign_user_id = #{record.signUserId,jdbcType=VARCHAR}");
        sql.SET("user_name = #{record.userName,jdbcType=VARCHAR}");
        sql.SET("address = #{record.address,jdbcType=VARCHAR}");
        sql.SET("chain_id = #{record.chainId,jdbcType=INTEGER}");
        sql.SET("group_id = #{record.groupId,jdbcType=INTEGER}");
        sql.SET("user_status = #{record.userStatus,jdbcType=INTEGER}");
        sql.SET("gmt_create = #{record.gmtCreate,jdbcType=TIMESTAMP}");
        sql.SET("gmt_modified = #{record.gmtModified,jdbcType=TIMESTAMP}");
        sql.SET("description = #{record.description,jdbcType=VARCHAR}");
        TbUserExample example = (TbUserExample) parameter.get("example");
        applyWhere(sql, example, true);
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String updateByPrimaryKeySelective(TbUser record) {
        SQL sql = new SQL();
        sql.UPDATE("tb_user");
        if (record.getSignUserId() != null) {
            sql.SET("sign_user_id = #{signUserId,jdbcType=VARCHAR}");
        }
        if (record.getUserName() != null) {
            sql.SET("user_name = #{userName,jdbcType=VARCHAR}");
        }
        if (record.getAddress() != null) {
            sql.SET("address = #{address,jdbcType=VARCHAR}");
        }
        if (record.getChainId() != null) {
            sql.SET("chain_id = #{chainId,jdbcType=INTEGER}");
        }
        if (record.getGroupId() != null) {
            sql.SET("group_id = #{groupId,jdbcType=INTEGER}");
        }
        if (record.getUserStatus() != null) {
            sql.SET("user_status = #{userStatus,jdbcType=INTEGER}");
        }
        if (record.getGmtCreate() != null) {
            sql.SET("gmt_create = #{gmtCreate,jdbcType=TIMESTAMP}");
        }
        if (record.getGmtModified() != null) {
            sql.SET("gmt_modified = #{gmtModified,jdbcType=TIMESTAMP}");
        }
        if (record.getDescription() != null) {
            sql.SET("description = #{description,jdbcType=VARCHAR}");
        }
        sql.WHERE("id = #{id,jdbcType=INTEGER}");
        return sql.toString();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    protected void applyWhere(SQL sql, TbUserExample example, boolean includeExamplePhrase) {
        if (example == null) {
            return;
        }
        String parmPhrase1;
        String parmPhrase1_th;
        String parmPhrase2;
        String parmPhrase2_th;
        String parmPhrase3;
        String parmPhrase3_th;
        if (includeExamplePhrase) {
            parmPhrase1 = "%s #{example.oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{example.oredCriteria[%d].allCriteria[%d].value} and #{example.oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{example.oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{example.oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{example.oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{example.oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        } else {
            parmPhrase1 = "%s #{oredCriteria[%d].allCriteria[%d].value}";
            parmPhrase1_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s}";
            parmPhrase2 = "%s #{oredCriteria[%d].allCriteria[%d].value} and #{oredCriteria[%d].criteria[%d].secondValue}";
            parmPhrase2_th = "%s #{oredCriteria[%d].allCriteria[%d].value,typeHandler=%s} and #{oredCriteria[%d].criteria[%d].secondValue,typeHandler=%s}";
            parmPhrase3 = "#{oredCriteria[%d].allCriteria[%d].value[%d]}";
            parmPhrase3_th = "#{oredCriteria[%d].allCriteria[%d].value[%d],typeHandler=%s}";
        }
        StringBuilder sb = new StringBuilder();
        List<Criteria> oredCriteria = example.getOredCriteria();
        boolean firstCriteria = true;
        for (int i = 0; i < oredCriteria.size(); i++) {
            Criteria criteria = oredCriteria.get(i);
            if (criteria.isValid()) {
                if (firstCriteria) {
                    firstCriteria = false;
                } else {
                    sb.append(" or ");
                }
                sb.append('(');
                List<Criterion> criterions = criteria.getAllCriteria();
                boolean firstCriterion = true;
                for (int j = 0; j < criterions.size(); j++) {
                    Criterion criterion = criterions.get(j);
                    if (firstCriterion) {
                        firstCriterion = false;
                    } else {
                        sb.append(" and ");
                    }
                    if (criterion.isNoValue()) {
                        sb.append(criterion.getCondition());
                    } else if (criterion.isSingleValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(String.format(parmPhrase1, criterion.getCondition(), i, j));
                        } else {
                            sb.append(String.format(parmPhrase1_th, criterion.getCondition(), i, j,
                                criterion.getTypeHandler()));
                        }
                    } else if (criterion.isBetweenValue()) {
                        if (criterion.getTypeHandler() == null) {
                            sb.append(
                                String.format(parmPhrase2, criterion.getCondition(), i, j, i, j));
                        } else {
                            sb.append(String.format(parmPhrase2_th, criterion.getCondition(), i, j,
                                criterion.getTypeHandler(), i, j, criterion.getTypeHandler()));
                        }
                    } else if (criterion.isListValue()) {
                        sb.append(criterion.getCondition());
                        sb.append(" (");
                        List<?> listItems = (List<?>) criterion.getValue();
                        boolean comma = false;
                        for (int k = 0; k < listItems.size(); k++) {
                            if (comma) {
                                sb.append(", ");
                            } else {
                                comma = true;
                            }
                            if (criterion.getTypeHandler() == null) {
                                sb.append(String.format(parmPhrase3, i, j, k));
                            } else {
                                sb.append(String.format(parmPhrase3_th, i, j, k,
                                    criterion.getTypeHandler()));
                            }
                        }
                        sb.append(')');
                    }
                }
                sb.append(')');
            }
        }
        if (sb.length() > 0) {
            sql.WHERE(sb.toString());
        }
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table
     * tb_user
     *
     * @mbg.generated
     */
    public String getOneByExample(TbUserExample example) {
        SQL sql = new SQL();
        if (example != null && example.isDistinct()) {
            sql.SELECT_DISTINCT("id");
        } else {
            sql.SELECT("id");
        }
        sql.SELECT("sign_user_id");
        sql.SELECT("user_name");
        sql.SELECT("address");
        sql.SELECT("chain_id");
        sql.SELECT("group_id");
        sql.SELECT("user_status");
        sql.SELECT("gmt_create");
        sql.SELECT("gmt_modified");
        sql.SELECT("description");
        sql.FROM("tb_user");
        applyWhere(sql, example, false);
        if (example != null && example.getOrderByClause() != null) {
            sql.ORDER_BY(example.getOrderByClause());
        }
        StringBuilder sqlBuilder = new StringBuilder(sql.toString());
        sqlBuilder.append(" limit 1 ");
        return sqlBuilder.toString();
    }


}
