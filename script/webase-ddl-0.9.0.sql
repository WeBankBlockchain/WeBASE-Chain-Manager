-- add in 0.9 for add new nodes
-- tb_front
ALTER TABLE tb_front ADD COLUMN front_type int(11) DEFAULT 1 COMMENT '节点类型：1，链部署；2，添加前置；3，动态扩容';
