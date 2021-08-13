alter table tb_contract modify column contract_path varchar(255);

CREATE TABLE IF NOT EXISTS tb_contract_path (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '合约路径编号',
  contract_path varchar(255) binary NOT NULL COMMENT '合约所在目录',
  chain_id int(11) NOT NULL COMMENT '所属区块链编号',
  group_id int(11) NOT NULL COMMENT '所属群组编号',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_group_path_name (chain_id,group_id,contract_path)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='合约路径表';
