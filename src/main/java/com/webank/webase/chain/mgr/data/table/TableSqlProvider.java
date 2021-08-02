package com.webank.webase.chain.mgr.data.table;

import java.util.Map;
import org.apache.ibatis.jdbc.SQL;

public class TableSqlProvider {

    public String queryTables(Map<String, Object> parameter) {
        String tableName = (String) parameter.get("tableName");

        SQL sql = new SQL();
        sql.SELECT("table_name");
        sql.FROM("information_schema.tables");
        sql.WHERE("table_schema = #{dbName}");
        if (tableName != null) {
            sql.WHERE("table_name = #{tableName}");
        }
        return sql.toString();
    }
}
