### v1.1.1 (2020-06-17)

**Fix**
- 移除Fastjson，替换为Jackson 2.11.0; web3sdk升级为2.4.1
- 升级依赖包：spring: 5.1.15; log4j: 2.13.3; slf4j: 1.7.30; 

**兼容性**

- 支持FISCO-BCOS release-2.3.0-bsn 版本
- WeBASE-Front bsn 版本
- WeBASE-Sign bsn 版本

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)

### v1.1.0 (2020-04-03)

​	WeBASE-Chain-Manager（微众区块链中间件平台-链管理子系统）。

**Add**

- 合约管理，包括编译、部署、交易、状态管理（冻结、解冻）等。
- 动态操作群组，包括启动、停止、删除、恢复、状态查询。
- 节点信息查询，查询块高和区块信息等。
- 检查节点进程，获取节点所在群组物理大小
- 获取群组网络统计数据和交易统计日志数据
- 交易签名调用WeBASE-Sign

**兼容性**

- 支持FISCO-BCOS release-2.3.0-bsn 版本
- WeBASE-Front bsn 版本
- WeBASE-Sign bsn 版本

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)



### v1.0.0 (2019-12-30)

​	WeBASE-Chain-Manager（微众区块链中间件平台-链管理子系统）。

**Add**

- 链管理
- 群组管理
- 节点前置管理

**兼容性**

- 支持FISCO-BCOS release-2.2.0 版本
- WeBASE-Front v1.2.2 版本

详细了解,请阅读[**技术文档**](https://webasedoc.readthedocs.io/zh_CN/latest/)
