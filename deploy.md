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


## 3. 服务启停

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

## 4. 验证服务部署结果

WeBASE-Chain-Manager 链管理平台示例页面：

```
http://{deployIP}:{deployPort}/WeBASE-Chain-Manager
示例：http://localhost:5005/WeBASE-Chain-Manager
```


## 5. 查看日志

在 log 目录查看：
```shell
全量日志：tail -f log/WeBASE-Chain-Manager.log
错误日志：tail -f log/WeBASE-Chain-Manager-error.log
```


## 6. 配置国密库

* 进入 WeBASE-Chain-Manager 根目录
* 解压 `tassl.tar.gz` 文件后，解压后放置于 `~/.fisco/tassl`

```shell
# 解压文件
tar -zvxf tassl.tar.gz

# 创建目录
mkdir -p ~/.fisco/

# 移动文件
mv tassl ~/.fisco/
```

## 7. 常见问题
### 镜像问题
在部署 FISCO-BCOS 链时，需要部署节点的主机上有 docker 镜像，建议：
* **手动传输镜像** 到部署节点主机
* 部署时选择 **手动上传镜像到节点主机**

操作步骤：
* 将 WeBASE-Chain-Manager 目录下的 `fisco-webase-v2.5.0.tar` 文件传输到节点主机
    * 可以使用 `scp` 命令
* 登陆节点主机，使用 `docker load -i  fisco-webase-v2.5.0.tar` 加载镜像即可