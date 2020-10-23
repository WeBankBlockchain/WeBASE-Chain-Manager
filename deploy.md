# 部署说明

部署流程：

1. 检查依赖服务
2. 生成公私钥对
3. 部署服务
4. 启动，停止服务
5. 验证服务部署结果
6. 查看日志
7. 配置交易服地址
8. 常见问题

## 1. 前提条件

| 序号 | 软件           |
| ---- | ------------------- |
| 1    | Java 8 或以上版本     |
| 2    | 已部署 WeBASE-Transaction（交易）服务|
| 3    | 已部署 WeBASE-Sign（签名）服务|
    
## 2. 生成公私钥对

执行命令生成 SSH 公私钥对

```shell
ssh-keygen -t rsa -m PEM
```

在运维管理平台的 `服务配置` 中添加一个 SSH 公钥配置项，内容是 `~/.ssh/id_rsa.pub` 文件的公钥

## 3. 部署服务

解压安装包：
```shell
unzip webase-chain-manager.zip
```
进入目录：

```shell
cd webase-chain-manager
```
初始化数据库

```shell
# 登陆数据库
mysql -u {DB_USER} -p 

# 创建 DB
CREATE DATABASE `webasechainmanager` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;

# 使用 DB
use `webasechainmanager`

# 执行数据库创建脚本
source script/webase-ddl.sql;
source script/webase-dml.sql;
```

复制配置文件

```
cp -rfv conf_template conf
```

修改配置文件 `conf/application.yml`：

1. 修改数据库配置
2. 修改 WeBASE-Sign 地址
3. **运维平台在链部署成功后**，修改 `WeBASE-Transaction` 连接到新部署链后，再修改 `transactionMap` 配置

```shell
# database connection configuration
spring:
  datasource:
    # webasechainmanager 数据库的访问地址，账号和密码
    # 修改 localhost 为数据库的 IP 地址
    # 修改 3306 位数据库端口
    # 修改 webasechainmanager 数据库，默认为：webasechainmanager
    # 修改数据库用户名和密码
    url: jdbc:mysql://localhost:3306/webasechainmanager?serverTimezone=GMT%2B8&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
    username: "defaultAccount"
    password: "defaultPassword"

#constants
constant:
  # WeBASE-Sign 服务的 IP 和 端口
  webaseSignAddress: "127.0.0.1:5004"

  # 链 chainId 和交易服地址的对应关系，可配置多行
  # 1 表示链 id，127.0.0.1:5003 表示交易服地址
  # 需要在部署链成功后，再启动交易服
  transactionMap: # WeBASE-Transaction 配置，跟 ChainId 相关
     1: "127.0.0.1:5003"
```

## 4. 服务启停

在dist目录下执行：
```shell
启动：bash start.sh
停止：bash stop.sh
检查：bash status.sh
```
**备注**：服务进程起来后，需通过日志确认是否正常启动，出现以下内容表示正常；如果服务出现异常，确认修改配置后，重启。如果提示服务进程在运行，则先执行stop.sh，再执行start.sh。

```
...
	Application() - main run success...
```

## 5. 验证服务部署结果

WeBASE-Chain-Manager 链管理平台示例页面：

```
http://{deployIP}:{deployPort}/WeBASE-Chain-Manager
示例：http://localhost:5005/WeBASE-Chain-Manager
```


## 6. 查看日志

在 log 目录查看：
```shell
全量日志：tail -f log/WeBASE-Chain-Manager.log
错误日志：tail -f log/WeBASE-Chain-Manager-error.log
```


## 7. 配置交易服（WeBASE-Transaction）地址


**注意：后续版本做成自动配置后，就不再需要手动维护**
**注意：后续版本做成自动配置后，就不再需要手动维护**


* 运维平台部署区块链服务成功后

* 获取新部署链的 `chain_id`
    1. 登陆 `webserver` 数据库，**注意：webserver** 库
    2. 执行 SQL `select * from tb_chain order by gmt_create desc`
    3. 找到最新创建的链，`remote_id` 字段就为 `chain_id`

* 修改 WeBASE-Transaction 配置，参考 WeBASE-Transaction 文档

* 重启 WeBASE-Transaction 服务，使配置生效

* 配置 WeBASE-Chain-Manager 配置， 修改 WeBASE-Chain-Manager 的 `conf/application.yml` 文件
    1. 在 `transactionMap` 配置下添加一行
    2. 注意新行对齐，冒号后的空格
        
```shell
constant:
  # 链 chainId 和交易服地址的对应关系，可配置多行
  # 1 表示链 id，127.0.0.1:5003 表示交易服地址
  # 需要在部署链成功后，再启动交易服
  # 格式 {chainId}: "{Transaction-IP}:{Transaction-PORT}"
  
  transactionMap: # WeBASE-Transaction 配置，跟 ChainId 相关
     1: "127.0.0.1:5003"
     
```

* 重启 WeBASE-Chain-Manager 服务


## 8. 常见问题
### 镜像问题
在部署 FISCO-BCOS 链时，需要部署节点的主机上有 docker 镜像，建议：
* **手动传输镜像** 到部署节点主机
* 部署时选择 **手动上传镜像到节点主机**

操作步骤：
* 将 WeBASE-Chain-Manager 目录下的 `fisco-webase-v2.5.0.tar` 文件传输到节点主机
    * 可以使用 `scp` 命令
* 登陆节点主机，使用 `docker load -i  fisco-webase-v2.5.0.tar` 加载镜像即可