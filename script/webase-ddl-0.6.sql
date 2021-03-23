-- add 2021.03.23 --
ALTER TABLE tb_user ADD COLUMN address varchar(64) DEFAULT NULL COMMENT '用户公钥地址' AFTER user_name;
