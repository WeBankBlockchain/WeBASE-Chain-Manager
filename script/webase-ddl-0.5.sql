ALTER TABLE tb_front ADD COLUMN front_peer_name varchar(64) DEFAULT null COMMENT 'k8s节点名称' AFTER chain_id;
ALTER TABLE tb_group ADD CONSTRAINT group_name_un UNIQUE KEY (group_name);
ALTER TABLE tb_front MODIFY COLUMN ssh_user varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'SSH 登录账号';
ALTER TABLE tb_front MODIFY COLUMN ssh_port int(11) NULL COMMENT 'SSH 登录 端口';
ALTER TABLE tb_front MODIFY COLUMN docker_port int(11) NULL COMMENT 'Docker demon 端口';
ALTER TABLE tb_front MODIFY COLUMN root_on_host varchar(256) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'front 所在主机存放节点数据的根目录，比如: /opt/fisco';
ALTER TABLE tb_front MODIFY COLUMN jsonrpc_port int(6) NULL COMMENT 'jsonrpc 端口';
ALTER TABLE tb_front MODIFY COLUMN p2p_port int(6) NULL COMMENT 'p2p 端口';
ALTER TABLE tb_front MODIFY COLUMN channel_port int(6) NULL COMMENT 'channel 端口';
ALTER TABLE tb_front DROP KEY unique_chainid_agencyid_frontip_frontport;
ALTER TABLE tb_front ADD CONSTRAINT unique_frontip_frontport_frontPeerName UNIQUE KEY (front_ip,front_port,front_peer_name);

ALTER TABLE tb_chain MODIFY COLUMN consensus_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '共识算法 pbft,raft';
ALTER TABLE tb_chain MODIFY COLUMN storage_type varchar(16) CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '存储类型（支持：RocksDB, LevelDB, MySQL）';




--add 2021.02.05--
ALTER TABLE tb_node ADD COLUMN node_type varchar(64) DEFAULT 'sealer' COMMENT '节点类型（sealer、observer、remove）' AFTER node_name;


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



--add 2021.02.05--
CREATE TABLE IF NOT EXISTS tb_task (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '任务自增编号',
  task_type tinyint(8) NOT NULL DEFAULT '1' COMMENT '任务类型（1：将节点类型变更为sealer）',
  task_status tinyint(8) NOT NULL DEFAULT '0' COMMENT '状态（0-未开始，1-准备开始，2-处理中，3-成功，4-失败）',
  chain_id int(11)  NOT NULL NULL COMMENT '链 ID',
  group_id int(11)  NOT NULL COMMENT '所属群组编号',
  node_id varchar(250) NOT NULL  COMMENT '节点编号',
  current_handler_host varchar(250) COMMENT '当前处理的机器',
  gmt_create datetime DEFAULT NULL COMMENT '创建时间',
  gmt_modified datetime DEFAULT NULL COMMENT '修改时间',
  description varchar(250) DEFAULT NULL COMMENT '描述',
  remark text COMMENT '备注',
  PRIMARY KEY (id),
    UNIQUE KEY unique_chain_group_node (chain_id,group_id,node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='任务信息表';


--add 2021.02.08--
ALTER TABLE tb_contract ADD COLUMN agency_id int(10) DEFAULT  NULL COMMENT '合约发起机构（合约首次保存的机构Id）' AFTER group_id;
