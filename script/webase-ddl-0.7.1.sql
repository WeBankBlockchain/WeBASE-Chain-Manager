-- add in 0.9 for add new nodes
-- tb_front
ALTER TABLE tb_front ADD COLUMN front_type int(11) DEFAULT 0 COMMENT '节点类型：0，链部署；1，添加前置；2，动态扩容';
