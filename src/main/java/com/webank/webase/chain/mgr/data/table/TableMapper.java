package com.webank.webase.chain.mgr.data.table;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

public interface TableMapper {

    @SelectProvider(type = TableSqlProvider.class, method = "queryTables")
    List<String> queryTables(@Param("dbName") String dbName, @Param("tableName") String tableName);

    @Update("drop table if exists ${tableName}")
    int dropTable(@Param("tableName") String tableName);

    @Delete("delete from ${tableName} limit 1000")
    int deleteByTableName(@Param("tableName") String tableName);
    
    @Update(createTbTaskPool)
    int createTbTaskPool(@Param("tableName") String tableName);

    @Update(createTbBlock)
    int createTbBlock(@Param("tableName") String tableName);

    @Update(createTbTransaction)
    int createTbTransaction(@Param("tableName") String tableName);

    @Update(createTbTxnDaily)
    int createTbTxnDaily();

    
    /**
     * create dynamic task pool table
     */
    String createTbTaskPool = "CREATE TABLE IF NOT EXISTS ${tableName} (\n" +
            "  id bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',\n" +
            "  block_number bigint(25) NOT NULL COMMENT '块高',\n" +
            "  sync_status tinyint(4) NOT NULL COMMENT '同步状态：INIT(0), DOING(1), DONE(2), ERROR(3), TIMEOUT(4)',\n" +
            "  certainty tinyint(4) NOT NULL COMMENT '确定性',\n" +
            "  handle_item tinyint(4) NOT NULL COMMENT '处理项',\n" +
            "  create_time datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  modify_time datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  PRIMARY KEY (id),\n" +
            "  UNIQUE KEY uk_number (block_number),\n" +
            "  KEY idx_status (sync_status)\n" +
            "  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='区块拉取任务信息表';";

    /**
     * create dynamic block table
     */
    String createTbBlock = "CREATE TABLE IF NOT EXISTS ${tableName} (\n" +
        "  id bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',\n" +
        "  block_number bigint(25) NOT NULL COMMENT '块高',\n" +
        "  block_hash varchar(128) NOT NULL COMMENT '块hash',\n" +
        "  block_timestamp datetime NOT NULL COMMENT '出块时间',\n" +
        "  sealer_index int(4) NOT NULL COMMENT '打包节点索引',\n" +
        "  sealer varchar(250) DEFAULT NULL COMMENT '打包节点',\n" +
        "  trans_count int(11) DEFAULT '0' COMMENT '块包含的交易数',\n" +
        "  block_detail mediumtext COMMENT '区块详情',\n" +
        "  create_time datetime DEFAULT NULL COMMENT '创建时间',\n" +
        "  modify_time datetime DEFAULT NULL COMMENT '修改时间',\n" +
        "  PRIMARY KEY (id),\n" +
        "  UNIQUE KEY uk_number (block_number)\n" +
        "  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='区块信息表';";

    /**
     * create dynamic transaction table
     */
    String createTbTransaction = "CREATE TABLE IF NOT EXISTS ${tableName} (\n" +
            "  id bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',\n" +
            "  chain_id int(11) NOT NULL COMMENT '所属区块链编号',\n" +
            "  group_id int(11) NOT NULL COMMENT '群组ID',\n" +
            "  chain_name varchar(120) DEFAULT NULL COMMENT '区块链名称',\n" +
            "  app_name varchar(128) DEFAULT NULL COMMENT '应用名称',\n" +
            "  trans_hash varchar(128) NOT NULL COMMENT '交易hash',\n" +
            "  block_number bigint(25) NOT NULL COMMENT '所属区块',\n" +
            "  block_timestamp datetime NOT NULL COMMENT '所属块出块时间',\n" +
            "  trans_detail mediumtext COMMENT '交易详情',\n" +
            "  audit_flag tinyint(4) DEFAULT '1' COMMENT '是否已统计（1-未审计，2-已审计）',\n" +
            "  create_time datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  modify_time datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  PRIMARY KEY (id),\n" +
            "  UNIQUE KEY uk_hash (trans_hash),\n" +
            "  KEY idx_flag (audit_flag),\n" +
            "  KEY idx_number (block_number)\n" +
            "  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='交易信息表';";

    /**
     * create dynamic receipt table
     */
    String createTbReceipt = "CREATE TABLE IF NOT EXISTS ${tableName} (\n" +
            "  id bigint NOT NULL AUTO_INCREMENT COMMENT '自增编号',\n" +
            "  trans_hash varchar(128) NOT NULL COMMENT '交易hash',\n" +
            "  block_number bigint(25) NOT NULL COMMENT '所属区块',\n" +
            "  block_timestamp datetime NOT NULL COMMENT '所属块出块时间',\n" +
            "  receipt_detail mediumtext COMMENT '交易回执详情',\n" +
            "  create_time datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  modify_time datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  PRIMARY KEY (id),\n" +
            "  UNIQUE KEY uk_hash (trans_hash),\n" +
            "  KEY idx_number (block_number)\n" +
            "  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='交易回执信息表';";

    /**
     * create common tx daily statistic table
     */
    String createTbTxnDaily = "CREATE TABLE IF NOT EXISTS tb_txn_daily (\n" +
            "  id int(11) NOT NULL AUTO_INCREMENT COMMENT '自增编号',\n" +
            "  chain_id int(11) NOT NULL COMMENT '所属区块链编号',\n" +
            "  group_id int(11) NOT NULL COMMENT '所属群组编号',\n" +
            "  stat_date date NOT NULL COMMENT '统计日期',\n" +
            "  txn int(11) COMMENT '交易量',\n" +
            "  block_number int(11) DEFAULT '0' COMMENT '当前统计到的块高',\n" +
            "  create_time datetime DEFAULT NULL COMMENT '创建时间',\n" +
            "  modify_time datetime DEFAULT NULL COMMENT '修改时间',\n" +
            "  PRIMARY KEY (id),\n" +
            "  UNIQUE KEY uk_data (chain_id,group_id,stat_date)\n" +
            "  ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='每日交易量记录表';";

}
