# 接口说明

[TOC]


## 1 前置管理模块

### 1.1 新增节点前置信息


#### 1.1.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： **/front/new**
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 1.1.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注         |
| ---- | ----------- | ------ | ------ | ------------ |
| 1    | frontIp     | string | 否     | 前置ip       |
| 2    | frontPort   | int    | 否     | 前置服务端口 |
| 3    | agency      | int    | 否     | 所属机构     |
| 4    | description | string | 是     | 备注         |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/new
```

```
{
    "frontIp": "127.0.0.1",
    "frontPort": "5002",
    "agency": "abc",
    "description": "test"
}
```


#### 1.1.3 返回参数

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    |             | Object        |      | 节点信息对象               |
| 3.1  | frontId     | int           | 否   | 前置编号                   |
| 3.2  | nodeId      | string        | 否   | 前置对应的节点编号         |
| 3.3  | frontIp     | string        | 否   | 前置ip                     |
| 3.4  | frontPort   | int           | 否   | 前置端口                   |
| 3.5  | agency      | string        | 否   | 所属机构                   |
| 3.6  | description | string        | 是   | 备注                       |
| 3.7  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.8  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "frontId": 500001,
        "nodeId": "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2",
        "frontIp": "127.0.0.1",
        "frontPort": 5002,
        "agency": "aa",
        "description": "test"
        "createTime": "2019-02-14 17:47:00",
        "modifyTime": "2019-03-15 11:14:29"
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 1.2 获取所有前置列表 


#### 1.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/front/find?frontId={frontId}&groupId={groupId}**
* 请求方式：GET
* 返回格式：JSON

#### 1.2.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                 |
|------|-------------|---------------|--------|-------------------------------|
| 1     | frontId       | Int           | 是     | 前置编号                  |
| 2     | groupId       | Int           | 是     | 所属群组编号                |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/find
```


#### 1.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1     | code          | Int           | 否     | 返回码，0：成功 其它：失败 |
| 2     | message       | String        | 否     | 描述                       |
| 3     | totalCount    | Int           | 否     | 总记录数                   |
| 4     | data          | List          | 否     | 组织列表                   |
| 4.1   |               | Object        |        | 节点信息对象               |
| 4.1.1 | frontId       | int           | 否     | 前置编号                   |
| 4.1.2 | nodeId | string | 否 | 前置对应的节点编号 |
| 4.1.3 | frontIp       | string        | 否     | 前置ip                     |
| 4.1.4 | frontPort     | int           | 否     | 前置端口                   |
| 4.1.5 | agency        | string        | 否     | 所属机构                   |
| 4.1.6 | description | string | 是 | 备注 |
| 4.1.7 | createTime | LocalDateTime | 否 | 落库时间 |
| 4.1.8 | modifyTime | LocalDateTime | 否 | 修改时间 |



***2）出参示例***

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "frontId": 500001,
            "nodeId": "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2",
            "frontIp": "127.0.0.1",
            "frontPort": 5002,
            "agency": "aa",
            "description": "test"
            "createTime": "2019-06-04 20:49:42",
            "modifyTime": "2019-06-04 20:49:42"
        }
    ],
    "totalCount": 1
}
```

* 失败：
```
{
   "code": 102000,
   "message": "system exception",
   "data": {}
}
```


### 1.3 删除前置信息

#### 1.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/front/{frontId}**
* 请求方式：DELETE
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 1.3.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | frontId    | int    | 否     | 前置编号                   |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/500001
```


#### 1.3.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code       | Int    | 否     | 返回码，0：成功 其它：失败 |
| 2    | message    | String | 否     | 描述                       |
| 3    | data       | object | 是     | 返回信息实体（空）         |


***2）出参示例***
* 成功：
```
{
    "code": 0,
    "data": {},
    "message": "success"
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 2 群组管理模块

### 2.1 生成新群组

​	节点和前置一一对应，节点编号可以从前置列表获取。

#### 2.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/generate**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 2.1.2 请求参数

***1）入参表***

| 序号 | 输入参数        | 类型         | 可为空 | 备注                   |
| ---- | --------------- | ------------ | ------ | ---------------------- |
| 1    | generateGroupId | int          | 否     | 生成的群组编号         |
| 2    | timestamp       | BigInteger   | 否     | 创世块时间（单位：ms） |
| 3    | nodeList        | List<String> | 否     | 节点编号列表           |
| 4    | description     | string       | 是     | 备注                   |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/generate
```

```
{
    "generateGroupId": 2,
    "timestamp": 1574853659000,
    "nodeList": [
       "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2"
    ],
    "description": "description"
}
```

#### 2.1.3 返回参数

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    | data        | Object        | 否   | 组织信息对象               |
| 3.1  | groupId     | int           | 否   | 群组编号                   |
| 3.2  | groupName   | String        | 否   | 群组名称                   |
| 3.3  | nodeCount   | int           | 否   | 节点数量                   |
| 3.4  | description | String        | 是   | 描述                       |
| 3.5  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.6  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "groupId": 2,
        "groupName": "group2",
        "nodeCount": 3,
        "description": "test",
        "createTime": "2019-02-14 17:33:50",
        "modifyTime": "2019-03-15 09:36:17"
    }
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.2 启动群组

​	生成新群组后，新群组下每一个节点都要启动，节点和前置一一对应。

#### 2.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/start/{startGroupId}/{nodeId}**
- 请求方式：GET
- 返回格式：JSON

#### 2.2.2 请求参数

***1）入参表***

| 序号 | 输入参数     | 类型   | 可为空 | 备注         |
| ---- | ------------ | ------ | ------ | ------------ |
| 1    | startGroupId | int    | 否     | 启动的群组id |
| 2    | nodeId       | String | 否     | 启动的节点id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/start/2/78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2
```

#### 2.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | object | 否   | 返回信息实体               |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {}
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.3 批量启动群组

​	节点和前置一一对应，节点编号可以从前置列表获取。

#### 2.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/batchStart**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 2.3.2 请求参数

***1）入参表***

| 序号 | 输入参数        | 类型         | 可为空 | 备注           |
| ---- | --------------- | ------------ | ------ | -------------- |
| 1    | generateGroupId | int          | 否     | 生成的群组编号 |
| 2    | nodeList        | List<String> | 否     | 节点编号列表   |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/generate
```

```
{
    "generateGroupId": 2,
    "nodeList": [
       "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2"
    ]
}
```

#### 2.3.3 返回参数

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | Object | 否   | 组织信息对象               |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {}
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.4 更新群组

​	生成新群组并启动新群组的每一个节点后，调用此接口更新群组相关信息。

#### 2.4.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/update**
- 请求方式：GET
- 返回格式：JSON

#### 2.4.2 请求参数

***1）入参表***

无

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/update
```

#### 2.4.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | object | 否   | 返回信息实体               |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {}
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.5 获取群组概况

#### 2.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/group/general/{groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 2.5.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | groupId  | int  | 否     | 群组id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/300001
```

#### 2.5.3 返回参数 

***1）出参表***

| 序号 | 输出参数      | 类型   |      | 备注                       |
| ---- | ------------- | ------ | ---- | -------------------------- |
| 1    | code          | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message       | String | 否   | 描述                       |
| 3    | data          | object | 否   | 返回信息实体               |
| 3.1  | groupId       | int    | 否   | 群组id                     |
| 3.2  | nodeCount     | int    | 否   | 节点数量                   |
| 3.3  | contractCount | int    | 否   | 已部署智能合约数量         |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "data": {
        "contractCount": 0,
        "groupId": "300001",
        "nodeCount": 2
    },
    "message": "success"
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.6 获取所有群组列表

#### 2.6.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/group/all**
- 请求方式：GET
- 返回格式：JSON

#### 2.6.2 请求参数

***1）入参表***
无

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/all
```

#### 2.6.3 返回参数 

***1）出参表***

| 序号  | 输出参数    | 类型          |      | 备注                       |
| ----- | ----------- | ------------- | ---- | -------------------------- |
| 1     | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2     | message     | String        | 否   | 描述                       |
| 3     | totalCount  | Int           | 否   | 总记录数                   |
| 4     | data        | List          | 否   | 组织列表                   |
| 4.1   |             | Object        |      | 组织信息对象               |
| 4.1.1 | groupId     | int           | 否   | 群组编号                   |
| 4.1.2 | groupName   | String        | 否   | 群组名称                   |
| 4.1.3 | nodeCount   | int           | 否   | 节点数量                   |
| 4.1.4 | description | String        | 是   | 描述                       |
| 4.1.5 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.6 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "groupId": 300001,
            "groupName": "group1",
            "nodeCount": 3,
            "description": "test",
            "createTime": "2019-02-14 17:33:50",
            "modifyTime": "2019-03-15 09:36:17"
        }
    ]
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 3 节点管理模块

### 3.1 查询节点列表

#### 3.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/nodeList/{groupId}/{pageNumber}/{pageSize}?nodeName={nodeName}**
- 请求方式：GET
- 返回格式：JSON

#### 3.1.2 请求参数

***1）入参表***

| 序号 | 输入参数   | 类型   | 可为空 | 备注       |
| ---- | ---------- | ------ | ------ | ---------- |
| 1    | groupId    | int    | 否     | 群组id     |
| 2    | pageSize   | Int    | 否     | 每页记录数 |
| 3    | pageNumber | Int    | 否     | 当前页码   |
| 4    | nodeName   | String | 是     | 节点名称   |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/nodeList/300001/1/10?nodeName=
```

#### 3.1.3 返回参数 

***1）出参表***

| 序号   | 输出参数    | 类型          |      | 备注                       |
| ------ | ----------- | ------------- | ---- | -------------------------- |
| 1      | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2      | message     | String        | 否   | 描述                       |
| 3      | totalCount  | Int           | 否   | 总记录数                   |
| 4      | data        | List          | 是   | 节点列表                   |
| 4.1    |             | Object        |      | 节点信息对象               |
| 4.1.1  | nodeId      | String        | 否   | 节点编号                   |
| 4.1.2  | nodeName    | string        | 否   | 节点名称                   |
| 4.1.3  | groupId     | int           | 否   | 所属群组编号               |
| 4.1.4  | nodeActive  | int           | 否   | 状态                       |
| 4.1.5  | nodeIp      | string        | 否   | 节点ip                     |
| 4.1.6  | P2pPort     | int           | 否   | 节点p2p端口                |
| 4.1.7  | description | String        | 否   | 备注                       |
| 4.1.8  | blockNumber | BigInteger    | 否   | 节点块高                   |
| 4.1.9  | pbftView    | BigInteger    | 否   | Pbft view                  |
| 4.1.10 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.11 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "nodeId": "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2,
            "nodeName": "1_78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b",
            "groupId": 1,
            "nodeIp": "127.0.0.1",
            "p2pPort": 10303,
            "description": null,
            "blockNumber": 133,
            "pbftView": 5852,
            "nodeActive": 1,
            "createTime": "2019-02-14 17:47:00",
            "modifyTime": "2019-03-15 11:14:29"
        }
    ]
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 3.2 查询节点信息

#### 3.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/nodeInfo/{groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 9.2.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | groupId  | int  | 否     | 群组id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/nodeInfo/1
```

#### 3.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    |             | Object        |      | 节点信息对象               |
| 3.1  | nodeId      | String        | 否   | 节点编号                   |
| 3.2  | nodeName    | string        | 否   | 节点名称                   |
| 3.3  | groupId     | int           | 否   | 所属群组编号               |
| 3.4  | nodeActive  | int           | 否   | 状态                       |
| 3.5  | nodeIp      | string        | 否   | 节点ip                     |
| 3.6  | P2pPort     | int           | 否   | 节点p2p端口                |
| 3.7  | description | String        | 否   | 备注                       |
| 3.8  | blockNumber | BigInteger    | 否   | 节点块高                   |
| 3.9  | pbftView    | BigInteger    | 否   | Pbft view                  |
| 3.10 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.11 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "nodeId": "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2",
        "nodeName": "1_78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b",
        "groupId": 1,
        "nodeIp": "127.0.0.1",
        "p2pPort": 10303,
        "description": null,
        "blockNumber": 133,
        "pbftView": 5852,
        "nodeActive": 1,
        "createTime": "2019-02-14 17:47:00",
        "modifyTime": "2019-03-15 11:14:29"
    }
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

## 4 合约管理模块  

### 4.1 保存合约和更新

#### 4.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/contract/save**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 4.1.2 请求参数

***1）入参表***

| 序号 | 输入参数       | 类型   | 可为空 | 备注                                       |
| ---- | -------------- | ------ | ------ | ------------------------------------------ |
| 1    | groupId        | Int    | 否     | 所属群组编号                               |
| 2    | contractName   | String | 否     | 合约名称                                   |
| 3    | contractSource | String | 是     | 合约源码                                   |
| 4    | contractAbi    | String | 是     | 编译合约生成的abi文件内容                  |
| 5    | contractBin    | String | 是     | 合约binary                                 |
| 6    | bytecodeBin    | String | 是     | 合约bytecode binary                        |
| 7    | contractId     | String | 是     | 合约编号（为空时表示新增，不为空表示更新） |
| 8    | contractPath   | String | 否     | 合约所在目录                               |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/save
```

```
{
    "groupId": 1,
    "contractBin": "60806040526004361061004c576000357c010000002269b80029",
    "bytecodeBin": "60806040523480156100105761146031c79ef057dd274c87bff322ea2269b80029",
    "contractAbi": "[]",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLICAJbmFtZSA9IG47CiAgICB9Cn0=",
    "contractName": "HeHe",
    "contractId": 200008,
    "contractPath": "Hi"
}
```

#### 4.1.3 返回参数 

***1）出参表***

| 序号 | 输出参数        | 类型          |      | 备注                             |
| ---- | --------------- | ------------- | ---- | -------------------------------- |
| 1    | code            | Int           | 否   | 返回码，0：成功 其它：失败       |
| 2    | message         | String        | 否   | 描述                             |
| 3    |                 | Object        |      | 返回信息实体                     |
| 3.1  | contractId      | int           | 否   | 合约编号                         |
| 3.2  | contractPath    | String        | 否   | 合约所在目录                     |
| 3.3  | contractName    | String        | 否   | 合约名称                         |
| 3.4  | groupId         | Int           | 否   | 所属群组编号                     |
| 3.5  | contractStatus  | int           | 否   | 1未部署，2已部署                 |
| 3.6  | contractType    | Int           | 否   | 合约类型(0-普通合约，1-系统合约) |
| 3.7  | contractSource  | String        | 否   | 合约源码                         |
| 3.8  | contractAbi     | String        | 是   | 编译合约生成的abi文件内容        |
| 3.9  | contractBin     | String        | 是   | 合约binary                       |
| 3.10 | bytecodeBin     | String        | 是   | 合约bin                          |
| 3.11 | contractAddress | String        | 是   | 合约地址                         |
| 3.12 | deployTime      | LocalDateTime | 是   | 部署时间                         |
| 3.13 | description     | String        | 是   | 备注                             |
| 3.14 | createTime      | LocalDateTime | 否   | 创建时间                         |
| 3.15 | modifyTime      | LocalDateTime | 是   | 修改时间                         |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200008,
        "contractPath": "Hi",
        "contractName": "HeHe",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": null,
        "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuM0=",
        "contractAbi": "[]",
        "contractBin": "60806040526004361061004c576000357c010274c87bff322ea2269b80029",
        "bytecodeBin": "608060405234801561001057629",
        "contractAddress": "0xa2ea2280b3a08a3ae2e1785dff09561e13915fb2",
        "deployTime": "2019-06-11 18:58:33",
        "description": null,
        "createTime": null,
        "modifyTime": null
    }
}
```

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 4.2 查询合约列表 


#### 4.2.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/contractList/{groupId}/{pageNumber}/{pageSize}**
* 请求方式：POST
* 返回格式：JSON

#### 4.2.2 请求参数


***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1      | groupId       | int           | 否     | 群组id                                          |
| 2      | contractName       | String           | 否     | 合约名                             |
| 3      | contractAddress    | String           | 否     | 合约地址                               |
| 4      | pageSize        | int           | 否     | 每页记录数                                      |
| 5      | pageNumber      | int           | 否     | 当前页码                                        |
| 6      | contractStatus      | int           | 否     | 1未部署，2已部署                        |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/contractList
```

#### 4.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1      | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2      | message         | String        | 否     | 描述                                            |
| 3      | totalCount      | Int           | 否     | 总记录数                                        |
| 4      | data            | List          | 是     | 列表                                            |
| 5.1    |                 | Object         |        | 返回信息实体                                    |
| 5.1.1  | contractId      | int           | 否     | 合约编号                                        |
| 5.1.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 5.1.3  | contractName    | String        | 否     | 合约名称                                        |
| 5.1.4  | groupId       | Int           | 否     | 所属群组编号                                    |
| 5.1.5  | contractStatus      | int           | 否     | 1未部署，2已部署                        |
| 5.1.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5.1.7  | contractSource  | String        | 否     | 合约源码                                        |
| 5.1.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 5.1.9  | contractBin     | String        | 是     | 合约binary                                      |
| 5.1.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 5.1.11 | contractAddress | String        | 是     | 合约地址                                        |
| 5.1.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 5.1.13 | description     | String        | 是     | 备注                                            |
| 5.1.14 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 5.1.15 | modifyTime | LocalDateTime | 是 | 修改时间 |
***2）出参示例***

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "contractId": 200002,
            "contractPath": "hellos",
            "contractName": "hellos",
            "contractStatus": 2,
            "groupId": 1,
            "contractType": 0,
            "contractSource": "cHJhZ21hIHNvbGlkaXgICAJbmFtZSA9IG47CiAgICB9Cn0=",
            "contractAbi": "[\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]",
            "contractBin": "60806040526004361061004c576000398de7e4ddf5fdc9ccbcfd44565fed695cd960b0029",
            "bytecodeBin": "608060405234801561001057600080004d4c",
            "deployTime": "2019-06-11 18:11:33",
            "description": null,
            "createTime": "2019-06-05 16:40:40",
            "modifyTime": "2019-06-11 18:11:33"
        }
    ],
    "totalCount": 1
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 4.3 查询合约信息


#### 4.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/{contractId}**
* 请求方式：GET
* 返回格式：JSON

#### 4.2.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | groupId       | int           | 否     | 群组id                                          |
| 2    | contractId      | int           | 否     | 合约编号                                        |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/200001
```

#### 4.3.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Object         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.5  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.7  | contractSource  | String        | 否     | 合约源码                                        |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | description     | String        | 是     | 备注                                            |
| 3.14 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.15 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

***2）出参示例***

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200002,
        "contractPath": "hellos",
        "contractName": "hellos",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": 0,
        "contractSource": "cHJhZ21hIHNvbGlkaXgICAJbmFtZSA9IG47CiAgICB9Cn0=",
        "contractAbi": "[\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"constructor\"}]",
        "contractBin": "60806040526004361061004c576000398de7e4ddf5fdc9ccbcfd44565fed695cd960b0029",
        "bytecodeBin": "608060405234801561001057600080004d4c",
        "deployTime": "2019-06-11 18:11:33",
        "description": null,
        "createTime": "2019-06-05 16:40:40",
        "modifyTime": "2019-06-11 18:11:33"
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 4.4 部署合约


#### 4.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/deploy**
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 4.4.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | groupId           | Int            | 否     | 所属群组编号               |
| 2    | contractName      | String         | 否     | 合约名称               |
| 3    | contractSource    | String         | 否     | 合约源码                   |
| 4    | contractAbi       | String         | 否     | 编译合约生成的abi文件内容  |
| 5    | contractBin       | String         | 否     | 合约binary                 |
| 6    | bytecodeBin       | String         | 否     | 合约bin                    |
| 7    | contractId      | String         | 否     | 合约名称               |
| 8    | contractPath      | String         | 否     | 合约所在目录               |
| 9    | user              | String         | 否     | 私钥用户               |
| 10    | constructorParams | List | 是     | 构造函数入参               |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/deploy
```

```
{
    "groupId": 1,
    "contractBin": "60806040526004361061004c576000357c010000002269b80029",
    "bytecodeBin": "60806040523480156100105761146031c79ef057dd274c87bff322ea2269b80029",
    "contractAbi": "[]",
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLICAJbmFtZSA9IG47CiAgICB9Cn0=",
    "user": 700006,
    "contractName": "HeHe",
    "contractId": 200008,
    "contractPath": "Hi",
    "constructorParams": ["a"]
}
```

#### 4.4.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Object         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.5  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.6  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.7  | contractSource  | String        | 否     | 合约源码                                        |
| 3.8  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.9  | contractBin     | String        | 是     | 合约binary                                      |
| 3.10 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.11 | contractAddress | String        | 是     | 合约地址                                        |
| 3.12 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.13 | description     | String        | 是     | 备注                                            |
| 3.14 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.15 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |


***2）出参示例***
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200008,
        "contractPath": "Hi",
        "contractName": "HeHe",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": null,
        "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuM0=",
        "contractAbi": "[]",
        "contractBin": "60806040526004361061004c576000357c010274c87bff322ea2269b80029",
        "bytecodeBin": "608060405234801561001057629",
        "contractAddress": "0xa2ea2280b3a08a3ae2e1785dff09561e13915fb2",
        "deployTime": "2019-06-11 18:58:33",
        "description": null,
        "createTime": null,
        "modifyTime": null
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 4.5 发送交易


#### 4.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/transaction**
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 4.5.2 请求参数


***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | groupId      | Int            | 否     | 所属群组编号               |
| 2    | user       | String  | 否     | 用户地址             |
| 3    | contractName | String         | 否     | 合约名称                   |
| 4    | contractId      | Int      | 否     | 合约编号               |
| 5    | funcName     | String         | 否     | 合约方法名                 |
| 6    | contractAddress     | String         | 是     | 合约地址   |
| 7   | funcParam    | List | 是     | 合约方法入参               |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/transaction
```

```
{
    "groupId":1,
    "user":"0x6accbdb86107b70decceee618ce40e20e15c8ad4",
    "contractName":"HelloWorld",
    "funcName":"set",
    "funcParam":["gwes"],
    "contractId":200002,
    "contractAddress":"0x7bd586b045e3684dbcd5506cb175c5d771f38d13"
}
```


#### 4.5.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code         | Int            | 否     | 返回码，0：成功 其它：失败 |
| 2    | message      | String         | 否     | 描述                       |
| 3    | data         | object         | 是     | 返回信息实体（空）         |


***2）出参示例***
* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {}
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```


### 4.6 根据包含bytecodeBin的字符串查询合约  


#### 4.6.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址： **/contract/findByPartOfBytecodeBin**
* 请求方式：GET
* 返回格式：JSON

#### 4.6.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | groupId         | int           | 否     | 所属群组编号                                        |
| 2    | partOfBytecodeBin      | String           | 否     | 包含合约bytecodeBin的的字符串         |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/findByPartOfBytecodeBin
```

```
{
    "groupId": "300001",
    "partOfBytecodeBin": "abc123455dev"
}
```


#### 4.6.3 返回参数 

***1）出参表***

| 序号 | 输出参数        | 类型          |      | 备注                             |
| ---- | --------------- | ------------- | ---- | -------------------------------- |
| 1    | code            | Int           | 否   | 返回码，0：成功 其它：失败       |
| 2    | message         | String        | 否   | 描述                             |
| 3    |                 | Object        |      | 返回信息实体                     |
| 3.1  | contractId      | int           | 否   | 合约编号                         |
| 3.2  | contractPath    | String        | 否   | 合约所在目录                     |
| 3.3  | contractName    | String        | 否   | 合约名称                         |
| 3.4  | groupId         | Int           | 否   | 所属群组编号                     |
| 3.5  | contractStatus  | int           | 否   | 1未部署，2已部署                 |
| 3.6  | contractType    | Int           | 否   | 合约类型(0-普通合约，1-系统合约) |
| 3.7  | contractSource  | String        | 否   | 合约源码                         |
| 3.8  | contractAbi     | String        | 是   | 编译合约生成的abi文件内容        |
| 3.9  | contractBin     | String        | 是   | 合约binary                       |
| 3.10 | bytecodeBin     | String        | 是   | 合约bin                          |
| 3.11 | contractAddress | String        | 是   | 合约地址                         |
| 3.12 | deployTime      | LocalDateTime | 是   | 部署时间                         |
| 3.13 | description     | String        | 是   | 备注                             |
| 3.14 | createTime      | LocalDateTime | 否   | 创建时间                         |
| 3.15 | modifyTime      | LocalDateTime | 是   | 修改时间                         |

***2）出参示例***

* 成功：
```
{
    "code": 0,
    "message": "success",
    "data": {
        "contractId": 200008,
        "contractPath": "Hi",
        "contractName": "HeHe",
        "contractStatus": 2,
        "groupId": 1,
        "contractType": null,
        "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuM0=",
        "contractAbi": "[]",
        "contractBin": "60806040526004361061004c576000357c010274c87bff322ea2269b80029",
        "bytecodeBin": "608060405234801561001057629",
        "contractAddress": "0xa2ea2280b3a08a3ae2e1785dff09561e13915fb2",
        "deployTime": "2019-06-11 18:58:33",
        "description": null,
        "createTime": null,
        "modifyTime": null
    }
}
```

* 失败：
```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```
