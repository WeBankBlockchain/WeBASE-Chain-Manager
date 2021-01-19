ALTER TABLE tb_front MODIFY COLUMN ssh_user varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'SSH 登录账号';
ALTER TABLE tb_front MODIFY COLUMN ssh_port int(11) NULL COMMENT 'SSH 登录 端口';
ALTER TABLE tb_front MODIFY COLUMN docker_port int(11) NULL COMMENT 'Docker demon 端口';
ALTER TABLE tb_front MODIFY COLUMN root_on_host varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'front 所在主机存放节点数据的根目录，比如: /opt/fisco';
ALTER TABLE tb_front MODIFY COLUMN jsonrpc_port int(6) NULL COMMENT 'jsonrpc 端口';
ALTER TABLE tb_front MODIFY COLUMN p2p_port int(6) NULL COMMENT 'p2p 端口';
ALTER TABLE tb_front MODIFY COLUMN channel_port int(6) NULL COMMENT 'channel 端口';

ALTER TABLE tb_chain MODIFY COLUMN consensus_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '共识算法 pbft,raft';
ALTER TABLE tb_chain MODIFY COLUMN storage_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '存储类型（支持：RocksDB, LevelDB, MySQL）';
