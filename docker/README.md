

#### 编译&构建docker镜像

```
cd WeBASE-Chain-Manager/docker
chmod +x build.sh && bash build.sh
```



#### 容器启动示例

以docker-compose方式启动容器为例，新建一个docker-compose.yml文件，写入以下内容

```
vi docker-compose.yml
```

```
version : '3'

services:
  webase-chain-manager:
      image: webasepro/webase-chain-manager:latest
      privileged: true
      hostname: webase-chain-manager
      network_mode: "host"
      container_name: webase-chain-manager-5005
      restart: always
      environment:
          service_port: 5005
          db_ip: 192.168.58.60
          db_port: 3306
          db_name: webasechainmanager
          db_account: root
          db_password: 123456
          db_init: "yes"
      logging:
          driver: "json-file"
          options:
              max-size: "50m"
```

```
# 注意事项：
# 1. mysql账户请提前在数据库里建好，并赋予权限，确保容器能访问到mysql的地址及端口
# 2. 容器启动后执行"docker logs 容器名"查看日志，例如“docker logs webase-chain-manager-5005”
# 3. WebUI访问：http://{deployIP}:{deployPort}/WeBASE-Chain-Manager
#    swagger页面访问：http://{deployIP}:{deployPort}/WeBASE-Chain-Manager/swagger-ui.html

# 容器环境变量参数说明： (以下参数请勿省略)
environment:
    service_port: 5005   #服务端口，默认5005，容器暴露的端口也是5005
    db_ip: 192.168.58.60   #MYSQL数据库IP,需确保能被容器访问到
    db_port: 3306          #MYSQL数据库端口,需确保能被容器访问到
    db_name: webasechainmanager   #数据库名
    db_account: root       #数据库账户
    db_password: 123456    #数据库密码
    db_init: "yes"   #“yes”代表容器第一次启动时会连接MYSQL数据库建库及表初始化，如果未添加此环境变量或值非"yes"，则不进行数据库建库建表，你也可以参考官方文档自己建库建表(需在容器启动前完成)。
```





#### 扩展

##### [WeBase一键Docker部署 — WeBASE v1.5.4 文档 (webasedoc.readthedocs.io)](https://webasedoc.readthedocs.io/zh_CN/latest/docs/WeBASE-Install/docker_install.html#)

