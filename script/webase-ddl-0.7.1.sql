-- add in 0.9 for add new nodes
-- tb_front
ALTER TABLE tb_front ADD COLUMN front_type int(11) DEFAULT 0 COMMENT '节点类型：0，链部署；1，添加前置；2，动态扩容';

-- todo 考虑用tar压缩链目录下的整个证书文件夹cert, gmcert，然后解压
--
-- tb_cert_file
CREATE TABLE IF NOT EXISTS tb_cert (
  id int(11) NOT NULL AUTO_INCREMENT COMMENT '证书自增编号',
  chain_id int(11) NOT NULL COMMENT '链 ID',
  chain_name varchar(120) DEFAULT '' COMMENT '链名称',
  encrypt_type int(10) unsigned DEFAULT '0' COMMENT '加密类型，0-非国密，1-国密',
  file_name varchar(250) NOT NULL  COMMENT '证书文件名，包含后缀',
  compress_type varchar(64) NOT NULL COMMENT '压缩类型，tar, zip',
  content longtext NOT NULL COMMENT '证书压缩文件内容(base64)',
  gmt_create datetime DEFAULT NULL COMMENT '创建时间',
  gmt_modified datetime DEFAULT NULL COMMENT '修改时间',
  description varchar(250) DEFAULT NULL COMMENT '描述',
  remark text COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY unique_chain_file (chain_id,file_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='证书压缩文件信息表';
