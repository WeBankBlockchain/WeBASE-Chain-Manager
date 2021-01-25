ALTER TABLE tb_front ADD COLUMN front_peer_name varchar(64) DEFAULT null COMMENT 'k8s节点名称' AFTER chain_id;
ALTER TABLE tb_group ADD CONSTRAINT group_name_un UNIQUE KEY (group_name);
ALTER TABLE tb_front MODIFY COLUMN ssh_user varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'SSH 登录账号';
ALTER TABLE tb_front MODIFY COLUMN ssh_port int(11) NULL COMMENT 'SSH 登录 端口';
ALTER TABLE tb_front MODIFY COLUMN docker_port int(11) NULL COMMENT 'Docker demon 端口';
ALTER TABLE tb_front MODIFY COLUMN root_on_host varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'front 所在主机存放节点数据的根目录，比如: /opt/fisco';
ALTER TABLE tb_front MODIFY COLUMN jsonrpc_port int(6) NULL COMMENT 'jsonrpc 端口';
ALTER TABLE tb_front MODIFY COLUMN p2p_port int(6) NULL COMMENT 'p2p 端口';
ALTER TABLE tb_front MODIFY COLUMN channel_port int(6) NULL COMMENT 'channel 端口';

ALTER TABLE tb_chain MODIFY COLUMN consensus_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '共识算法 pbft,raft';
ALTER TABLE tb_chain MODIFY COLUMN storage_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '存储类型（支持：RocksDB, LevelDB, MySQL）';



CREATE TABLE IF NOT EXISTS tb_user (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '用户自增编号',
  sign_user_id varchar(64) NOT NULL COMMENT '用于唯一id',
  user_name varchar(64) binary NOT NULL COMMENT '用户名',
  chain_id int(10) unsigned NOT NULL COMMENT '链 ID',
  group_id int(11) DEFAULT NULL COMMENT '所属群组编号',
  user_status int(1) NOT NULL DEFAULT '1' COMMENT '状态（1-正常 2-停用）',
  gmt_create datetime DEFAULT NULL COMMENT '创建时间',
  gmt_modified datetime DEFAULT NULL COMMENT '修改时间',
  description varchar(250) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY unique_sign_user_id (sign_user_id),
  UNIQUE KEY unique_chain_group_user (chain_id,group_id,user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='密钥用户信息表';