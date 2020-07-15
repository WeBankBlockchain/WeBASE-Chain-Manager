-- ----------------------------
-- Table structure for tb_chain
-- ----------------------------
DROP TABLE IF EXISTS tb_chain;
CREATE TABLE tb_chain (
  chain_id int(11) NOT NULL COMMENT '区块链编号',
  chain_name varchar(120) DEFAULT NULL COMMENT '区块链名称',
  chain_type tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '加密类型：0，标密；1，国密；默认 0 ',
  description varchar(512) COMMENT '描述',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  modify_time datetime DEFAULT NULL COMMENT '修改时间',
  version varchar(64) NOT NULL DEFAULT '' COMMENT '创建链时选择的镜像版本',
  consensus_type varchar(16) NOT NULL DEFAULT 'pbft' COMMENT '共识算法 pbft,raft',
  storage_type varchar(16) NOT NULL COMMENT '存储类型（支持：RocksDB, LevelDB, MySQL）',
  chain_status tinyint(8) unsigned NOT NULL DEFAULT '0' COMMENT '链状态',
  webase_sign_addr varchar(255) NOT NULL DEFAULT '127.0.0.1:5004' COMMENT 'WeBASE-Sign 的访问地址',
  PRIMARY KEY (chain_id),
  UNIQUE KEY unique_name (chain_name)
) ENGINE=InnoDB CHARSET=utf8 COMMENT='区块链信息表';

-- ----------------------------
-- Table structure for tb_group
-- ----------------------------
DROP TABLE IF EXISTS tb_group;
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
    group_timestamp varchar(64) COMMENT '群组创世块时间戳',
    node_id_list text COMMENT '群组创世块成员节点的ID',
    epoch_sealer_num int DEFAULT '0' COMMENT '群组创世块共识节点数',
    PRIMARY KEY (group_id,chain_id)
) COMMENT='群组信息表' ENGINE=InnoDB CHARSET=utf8;


-- ----------------------------
-- Table structure for tb_front
-- ----------------------------
DROP TABLE IF EXISTS tb_front;
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
    front_status int(11) DEFAULT 1 COMMENT '前置服务状态：0，未创建；1，停止；2，启动；',
    version varchar(64) DEFAULT '' COMMENT '运行的镜像版本标签',
    container_name varchar(255) DEFAULT '' COMMENT 'Docker 启动的容器名称',
    jsonrpc_port int(6) DEFAULT '8545' COMMENT 'jsonrpc 端口',
    p2p_port int(6) DEFAULT '30303' COMMENT 'p2p 端口',
    channel_port int(6) DEFAULT '20200' COMMENT 'channel 端口',
    chain_name varchar(64) DEFAULT '' COMMENT '所属链名称，冗余字段',
    ext_company_id int(10) unsigned DEFAULT '0' COMMENT '节点所在主机的所属公司 ID(Web Server)',
    ext_agency_id int(10) unsigned DEFAULT '0' COMMENT '节点部署时的机构 ID(Web Server)',
    ext_host_id int(10) unsigned DEFAULT '0' COMMENT '所属主机',
    host_index int(6) DEFAULT '0' COMMENT '一台主机可能有多个节点。表示在主机中的编号，从 0 开始编号',
    ssh_user varchar(32) NOT NULL DEFAULT 'root' COMMENT 'SSH 登录账号',
    ssh_port int(11) NOT NULL DEFAULT 22 COMMENT 'SSH 登录 端口',
    docker_port int(11) NOT NULL DEFAULT 3000 COMMENT 'Docker demon 端口',
    root_on_host varchar(256) NOT NULL COMMENT 'front 所在主机存放节点数据的根目录，比如: /opt/fisco',
    node_root_on_host varchar(256) NOT NULL COMMENT '节点根目录，比如: /opt/fisco/[chain_name]/node0',
    PRIMARY KEY (`front_id`),
    UNIQUE KEY unique_chainid_nodeid (chain_id,node_id),
    UNIQUE KEY `unique_chainid_agencyid_frontip_frontport` (`chain_id`, `ext_agency_id`,`front_ip`,`front_port`)
) ENGINE=InnoDB AUTO_INCREMENT=200001 DEFAULT CHARSET=utf8 COMMENT='前置服务信息表';


-- ----------------------------
-- Table structure for tb_front_group_map
-- ----------------------------
DROP TABLE IF EXISTS tb_front_group_map;
CREATE TABLE IF NOT EXISTS tb_front_group_map (
    map_id int(11) NOT NULL AUTO_INCREMENT COMMENT '映射编号',
    chain_id int(11) NOT NULL COMMENT '区块链编号',
    front_id int(11) NOT NULL COMMENT '前置服务编号',
    group_id int(11) NOT NULL COMMENT '群组编号',
    create_time datetime DEFAULT NULL COMMENT '创建时间',
    modify_time datetime DEFAULT NULL COMMENT '修改时间',
    front_status tinyint(8) unsigned NOT NULL DEFAULT '0' COMMENT '节点状态: 0 游离；1 共识；2 观察',
    PRIMARY KEY (map_id),
    unique unique_chain_front_group (chain_id,front_id,group_id)
) ENGINE=InnoDB AUTO_INCREMENT=300001 DEFAULT CHARSET=utf8 COMMENT='前置群组映射表';


-- ----------------------------
-- Table structure for tb_node
-- ----------------------------
DROP TABLE IF EXISTS tb_node;
CREATE TABLE IF NOT EXISTS tb_node (
    node_id varchar(250) NOT NULL  COMMENT '节点编号',
    chain_id int(11) NOT NULL COMMENT '所属区块链编号',
    group_id int(11) NOT NULL COMMENT '所属群组编号',
    node_name varchar(255) NOT NULL COMMENT '节点名称',
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
DROP TABLE IF EXISTS tb_contract;
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



-- ----------------------------
-- Table structure for tb_config
-- ----------------------------
DROP TABLE IF EXISTS tb_config;
CREATE TABLE `tb_config` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增长 ID',
  `config_name` varchar(64) NOT NULL COMMENT '配置名称',
  `config_type` int(10) NOT NULL DEFAULT '0' COMMENT '配置类型',
  `config_value` varchar(512) NOT NULL DEFAULT '' COMMENT '配置值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime NOT NULL COMMENT '最近一次更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unq_type_value` (`config_type`,`config_value`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置信息表';
