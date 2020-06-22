-- ----------------------------
-- Table structure for tb_chain
-- ----------------------------
DROP TABLE IF EXISTS tb_chain;
CREATE TABLE tb_chain (
  chain_id int(11) NOT NULL COMMENT '区块链编号',
  chain_name varchar(120) DEFAULT NULL COMMENT '区块链名称',
  chain_type tinyint(4) DEFAULT '0' COMMENT '类型（ 0-非国密 1-国密）',
  description varchar(1024) COMMENT '描述',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (chain_id),
  UNIQUE KEY unique_name (chain_name)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='区块链信息表';

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_group (
    group_id int(11) NOT NULL COMMENT '群组ID',
    chain_id int(11) NOT NULL COMMENT '所属区块链编号',
    group_name varchar(64) NOT NULL COMMENT '群组名称',
    group_status tinyint(4) DEFAULT '1' COMMENT '状态（1-正常 2-异常）',
    node_count int DEFAULT '0' COMMENT '群组下节点数',
    description varchar(1024) COMMENT '描述',
    group_type tinyint(4) DEFAULT '1' COMMENT '群组类型(1-同步的，2-手动创建的)',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (group_id,chain_id)
) COMMENT='群组信息表' ENGINE=InnoDB CHARSET=utf8;


-- ----------------------------
-- Table structure for tb_front
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_front (
    front_id int(11) NOT NULL AUTO_INCREMENT COMMENT '前置服务编号',
    chain_id int(11) NOT NULL COMMENT '所属区块链编号',
    node_id varchar(250) NOT NULL COMMENT '节点编号',
    front_ip varchar(16) NOT NULL COMMENT '前置服务ip',
    front_port int(11) DEFAULT NULL COMMENT '前置服务端口',
    agency varchar(32) NOT NULL COMMENT '所属机构名称',
    description varchar(1024) COMMENT '描述',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (front_id),
    UNIQUE KEY unique_chain_node (chain_id,node_id)
) ENGINE=InnoDB AUTO_INCREMENT=200001 DEFAULT CHARSET=utf8 COMMENT='前置服务信息表';


-- ----------------------------
-- Table structure for tb_front_group_map
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_front_group_map (
    map_id int(11) NOT NULL AUTO_INCREMENT COMMENT '映射编号',
    chain_id int(11) NOT NULL COMMENT '区块链编号',
    front_id int(11) NOT NULL COMMENT '前置服务编号',
    group_id int(11) NOT NULL COMMENT '群组编号',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (map_id),
    unique unique_chain_front_group (chain_id,front_id,group_id)
) ENGINE=InnoDB AUTO_INCREMENT=300001 DEFAULT CHARSET=utf8 COMMENT='前置群组映射表';


-- ----------------------------
-- Table structure for tb_node
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_node (
    node_id varchar(250) NOT NULL  COMMENT '节点编号',
    chain_id int(11) NOT NULL COMMENT '所属区块链编号',
    group_id int(11) NOT NULL COMMENT '所属群组编号',
    node_name varchar(120) NOT NULL COMMENT '节点名称',
    node_ip varchar(16) DEFAULT NULL COMMENT '节点ip',
    p2p_port int(11) DEFAULT NULL COMMENT '节点p2p端口',
    block_number bigint(20) DEFAULT '0' COMMENT '节点块高',
    pbft_view bigint(20) DEFAULT '0' COMMENT 'pbft_view',
    node_active tinyint(4) DEFAULT '2' COMMENT '节点存活标识(1存活，2不存活)',
    description varchar(1024) COMMENT '描述',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (node_id,chain_id,group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点表';


-- ----------------------------
-- Table structure for tb_contract
-- ----------------------------
CREATE TABLE IF NOT EXISTS tb_contract (
    contract_id int(11) NOT NULL AUTO_INCREMENT COMMENT '合约编号',
    contract_path varchar(24) binary NOT NULL COMMENT '合约所在目录',
    contract_name varchar(120) binary NOT NULL COMMENT '合约名称',
    chain_id int(11) NOT NULL COMMENT '所属区块链编号',
    group_id int(11) NOT NULL COMMENT '所属群组编号',
    contract_source text COMMENT '合约源码',
    contract_abi text COMMENT '编译合约生成的abi文件内容',
    contract_bin text COMMENT '合约运行时binary，用于合约解析',
    bytecode_bin text COMMENT '合约bytecode binary，用于部署合约',
    contract_address varchar(64) DEFAULT NULL COMMENT '合约地址',
    deploy_time datetime DEFAULT NULL COMMENT '部署时间',
    contract_status tinyint(4) DEFAULT '1' COMMENT '部署状态（1：未部署，2：部署成功，3：部署失败）',
    contract_type tinyint(4) DEFAULT '0' COMMENT '合约类型(0-普通合约，1-系统合约)',
    description varchar(1024) COMMENT '描述',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    PRIMARY KEY (contract_id),
    UNIQUE KEY uk_group_path_name (chain_id,group_id,contract_path,contract_name)
) ENGINE=InnoDB AUTO_INCREMENT=400001 DEFAULT CHARSET=utf8 COMMENT='合约表';
