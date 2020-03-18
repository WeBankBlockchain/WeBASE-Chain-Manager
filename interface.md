# 接口说明

[TOC]

## 1 区块链管理模块

### 1.1 新增链信息

#### 1.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/chain/new**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 1.1.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注                       |
| ---- | ----------- | ------ | ------ | -------------------------- |
| 1    | chainId     | int    | 否     | 链编号                     |
| 2    | chainName   | string | 否     | 链名称                     |
| 3    | chainType   | int    | 否     | 链类型（0-非国密，1-国密） |
| 4    | description | string | 是     | 备注                       |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/chain/new
```

```
{
    "chainId": 100001,
    "chainName": "链一",
    "chainType": 0,
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
| 3.1  | chainId     | int           | 否   | 链编号                     |
| 3.2  | chainName   | string        | 否   | 链名称                     |
| 3.3  | chainType   | int           | 否   | 链类型（0-非国密，1-国密） |
| 3.4  | description | string        | 是   | 备注                       |
| 3.5  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.6  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "chainId": 100001,
        "chainName": "链一",
        "chainType": 0,
        "description": "test"
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

### 1.2 获取所有链列表 

#### 1.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/chain/all**
- 请求方式：GET
- 返回格式：JSON

#### 1.2.2 请求参数

***1）入参表***

无

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/chain/all
```

#### 1.2.3 返回参数 

***1）出参表***

| 序号  | 输出参数    | 类型          |      | 备注                       |
| ----- | ----------- | ------------- | ---- | -------------------------- |
| 1     | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2     | message     | String        | 否   | 描述                       |
| 3     | totalCount  | Int           | 否   | 总记录数                   |
| 4     | data        | List          | 否   | 组织列表                   |
| 4.1   |             | Object        |      | 节点信息对象               |
| 4.1.1 | chainId     | int           | 否   | 链编号                     |
| 4.1.2 | chainName   | string        | 否   | 链名称                     |
| 4.1.3 | chainType   | int           | 否   | 链类型（0-非国密，1-国密） |
| 4.1.4 | description | string        | 是   | 备注                       |
| 4.1.5 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.6 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": [
        {
        "chainId": 100001,
        "chainName": "链一",
        "chainType": 0,
        "description": "test"
        "createTime": "2019-02-14 17:47:00",
        "modifyTime": "2019-03-15 11:14:29"
    	}
    ],
    "totalCount": 1
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

### 1.3 删除链信息

#### 1.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/chain/{chainId}**
- 请求方式：DELETE
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 1.3.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | chainId  | int  | 否     | 链编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/chain/100001
```

#### 1.3.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | object | 是   | 返回信息实体（空）         |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "data": {},
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

## 2 前置管理模块

### 2.1 新增节点前置信息

#### 2.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/front/new**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 2.1.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注         |
| ---- | ----------- | ------ | ------ | ------------ |
| 1    | chainId     | int    | 否     | 链编号       |
| 2    | frontIp     | string | 否     | 前置ip       |
| 3    | frontPort   | int    | 否     | 前置服务端口 |
| 4    | agency      | string | 否     | 所属机构     |
| 5    | description | string | 是     | 备注         |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/new
```

```
{
    "chainId": 100001,
    "frontIp": "127.0.0.1",
    "frontPort": 5002,
    "agency": "abc",
    "description": "test"
}
```

#### 2.1.3 返回参数

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    |             | Object        |      | 节点信息对象               |
| 3.1  | frontId     | int           | 否   | 前置编号                   |
| 3.2  | chainId     | int           | 否   | 链编号                     |
| 3.3  | nodeId      | string        | 否   | 前置对应的节点编号         |
| 3.4  | frontIp     | string        | 否   | 前置ip                     |
| 3.5  | frontPort   | int           | 否   | 前置端口                   |
| 3.6  | agency      | string        | 否   | 所属机构                   |
| 3.7  | description | string        | 是   | 备注                       |
| 3.8  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.9  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "frontId": 200001,
        "chainId": 100001,
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

- 失败：

```
{
    "code": 102000,
    "message": "system exception",
    "data": {}
}
```

### 2.2 获取所有前置列表 

#### 2.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/front/find?chainId={chainId}?frontId={frontId}&groupId={groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 2.2.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注         |
| ---- | -------- | ---- | ------ | ------------ |
| 1    | chainId  | Int  | 是     | 链编号       |
| 2    | frontId  | Int  | 是     | 前置编号     |
| 3    | groupId  | Int  | 是     | 所属群组编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/find
```

#### 2.2.3 返回参数 

***1）出参表***

| 序号  | 输出参数    | 类型          |      | 备注                       |
| ----- | ----------- | ------------- | ---- | -------------------------- |
| 1     | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2     | message     | String        | 否   | 描述                       |
| 3     | totalCount  | Int           | 否   | 总记录数                   |
| 4     | data        | List          | 否   | 组织列表                   |
| 4.1   |             | Object        |      | 节点信息对象               |
| 4.1.1 | frontId     | int           | 否   | 前置编号                   |
| 4.1.2 | chainId     | int           | 否   | 链编号                     |
| 4.1.3 | nodeId      | string        | 否   | 前置对应的节点编号         |
| 4.1.4 | frontIp     | string        | 否   | 前置ip                     |
| 4.1.5 | frontPort   | int           | 否   | 前置端口                   |
| 4.1.6 | agency      | string        | 否   | 所属机构                   |
| 4.1.7 | description | string        | 是   | 备注                       |
| 4.1.8 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.9 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "frontId": 200001,
            "chainId": 100001,
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

- 失败：

```
{
   "code": 102000,
   "message": "system exception",
   "data": {}
}
```

### 2.3 删除前置信息

#### 2.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/front/{frontId}**
- 请求方式：DELETE
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 2.3.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注     |
| ---- | -------- | ---- | ------ | -------- |
| 1    | frontId  | int  | 否     | 前置编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/200001
```

#### 2.3.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | object | 是   | 返回信息实体（空）         |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "data": {},
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

### 2.4 前置节点监控信息

#### 2.4.1 传输协议规范

- 网络传输协议：使用HTTP协议

- 请求地址：

  ```
  /mointorInfo/{frontId}?beginDate={beginDate}&endDate={endDate}&contrastBeginDate={contrastBeginDate}&contrastEndDate={contrastEndDate}&gap={gap}&groupId={groupId}
  ```

- 请求方式：GET

- 请求头：Content-type: application/json

- 返回格式：JSON

#### 2.4.2 请求参数

***1）入参表***

| 序号 | 输入参数          | 类型          | 可为空 | 备注                                                         |
| ---- | ----------------- | ------------- | ------ | ------------------------------------------------------------ |
| 1    | frontId           | int           | 否     | 前置编号                                                     |
| 2    | beginDate         | LocalDateTime | 是     | 显示时间（开始） yyyy-MM-dd'T'HH:mm:ss.SSS 2019-03-13T00:00:00 |
| 3    | endDate           | LocalDateTime | 是     | 显示时间（结束）                                             |
| 4    | contrastBeginDate | LocalDateTime | 是     | 对比时间（开始）                                             |
| 5    | contrastEndDate   | LocalDateTime | 是     | 对比时间（结束）                                             |
| 6    | gap               | Int           | 是     | 数据粒度                                                     |
| 7    | groupId           | int           | 否     | 群组编号                                                     |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/mointorInfo/200001?beginDate=2019-03-13T00:00:00&endDate=2019-03-13T14:34:22&contrastBeginDate=2019-03-13T00:00:00&contrastEndDate=2019-03-13T14:34:22&gap=60&groupId=1
```

#### 2.4.3 返回参数 

***1）出参表***

| 序号      | 输出参数         | 类型            |      | 备注                                                         |
| --------- | ---------------- | --------------- | ---- | ------------------------------------------------------------ |
| 1         | code             | Int             | 否   | 返回码                                                       |
| 2         | message          | String          | 否   | 描述信息                                                     |
| 3         | data             | Array           | 否   | 返回信息列表                                                 |
| 3.1       |                  | Object          |      | 返回信息实体                                                 |
| 3.1.1     | metricType       | String          | 否   | 测量类型：blockHeight（块高）、pbftView（pbft视图）、pendingCount（待处理交易数量） |
| 3.1.2     | data             | Object          | 否   |                                                              |
| 3.1.2.1   | lineDataList     | Object          | 否   | 指定时间的数据                                               |
| 3.1.2.1.1 | timestampList    | List\<String\>  | 否   | 时间戳列表                                                   |
| 3.1.2.1.2 | valueList        | List\<Integer\> | 否   | 值列表                                                       |
| 3.1.2.2   | contrastDataList | Object          | 否   | 比对时间的数据                                               |
| 3.1.2.2.1 | timestampList    | List\<String\>  | 否   | 时间戳列表                                                   |
| 3.1.2.2.2 | valueList        | List\<Integer\> | 否   | 值列表                                                       |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "metricType": "blockHeight",
            "data": {
                "lineDataList": {
                    "timestampList": [
                        1552406401042,
                        1552406701001
                    ],
                    "valueList": [
                        747309,
                        747309
                    ]
                },
                "contrastDataList": {
                    "timestampList": [
                        1552320005000,
                        1552320301001
                    ],
                    "valueList": [
                        null,
                        747309
                    ]
                }
            }
        },
        {
            "metricType": "pbftView",
            "data": {
                "lineDataList": {
                    "timestampList": null,
                    "valueList": [
                        118457,
                        157604
                    ]
                },
                "contrastDataList": {
                    "timestampList": null,
                    "valueList": [
                        null,
                        33298
                    ]
                }
            }
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

### 2.5 前置节点服务器监控信息 

#### 2.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：

```
/front/ratio/{frontId}?gap={gap}&beginDate={beginDate}&endDate={endDate}&contrastBeginDate={contrastBeginDate}&contrastEndDate={contrastEndDate}
```

- 请求方式：GET
- 返回格式：JSON

#### 2.5.2 请求参数

***1）入参表***

| 序号 | 输入参数          | 类型          | 可为空 | 备注                                                         |
| ---- | ----------------- | ------------- | ------ | ------------------------------------------------------------ |
| 1    | frontId           | int           | 否     | 前置编号                                                     |
| 2    | beginDate         | LocalDateTime | 是     | 显示时间（开始） yyyy-MM-dd'T'HH:mm:ss.SSS 2019-03-13T00:00:00 |
| 3    | endDate           | LocalDateTime | 是     | 显示时间（结束）                                             |
| 4    | contrastBeginDate | LocalDateTime | 是     | 对比时间（开始）                                             |
| 5    | contrastEndDate   | LocalDateTime | 是     | 对比时间（结束）                                             |
| 6    | gap               | Int           | 是     | 数据粒度                                                     |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/ratio/200001?gap=1&beginDate=2019-03-15T00:00:00&endDate=2019-03-15T15:26:55&contrastBeginDate=2019-03-15T00:00:00&contrastEndDate=2019-03-15T15:26:55
```

#### 2.5.3 返回参数 

***1）出参表***

| 序号      | 输出参数         | 类型            |      | 备注                                                         |
| --------- | ---------------- | --------------- | ---- | ------------------------------------------------------------ |
| 1         | code             | Int             | 否   | 返回码                                                       |
| 2         | message          | String          | 否   | 描述信息                                                     |
| 3         | data             | Array           | 否   | 返回信息列表                                                 |
| 3.1       |                  | Object          |      | 返回信息实体                                                 |
| 3.1.1     | metricType       | String          | 否   | 测量类型: cpu（cpu利用率：%）、memory（内存利用率：%）、disk（硬盘利用率：%）、txbps（上行bandwith：KB/s）、rxbps（下行bandwith：KB/s） |
| 3.1.2     | data             | Object          | 否   |                                                              |
| 3.1.2.1   | lineDataList     | Object          | 否   | 指定时间的数据                                               |
| 3.1.2.1.1 | timestampList    | List\<String\>  | 否   | 时间戳列表                                                   |
| 3.1.2.1.2 | valueList        | List\<Integer\> | 否   | 值列表                                                       |
| 3.1.2.2   | contrastDataList | Object          | 否   | 比对时间的数据                                               |
| 3.1.2.2.1 | timestampList    | List\<String\>  | 否   | 时间戳列表                                                   |
| 3.1.2.2.2 | valueList        | List\<Integer\> | 否   | 值列表                                                       |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": [
        {
            "metricType": "txbps",
            "data": {
                "lineDataList": {
                    "timestampList": [
                        1552406401042,
                        1552406701001
                    ],
                    "valueList": [
                        12.24,
                        54.48
                    ]
                },
                "contrastDataList": {
                    "timestampList": [
                        1552320005000,
                        1552320301001
                    ],
                    "valueList": [
                        22.24,
                        24.48
                    ]
                }
            }
        },
        {
            "metricType": "cpu",
            "data": {
                "lineDataList": {
                    "timestampList": null,
                    "valueList": [
                        118457,
                        157604
                    ]
                },
                "contrastDataList": {
                    "timestampList": null,
                    "valueList": [
                        null,
                        33298
                    ]
                }
            }
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

### 2.5 前置节点服务器配置信息 

#### 2.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/front/config/{frontId}** 

- 请求方式：GET
- 返回格式：JSON

#### 2.5.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注     |
| ---- | -------- | ---- | ------ | -------- |
| 1    | frontId  | int  | 否     | 前置编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/config/200001
```

#### 2.5.3 返回参数 

***1）出参表***

| 序号  | 输出参数        | 类型   |      | 备注                         |
| ----- | --------------- | ------ | ---- | ---------------------------- |
| 1     | code            | int    | 否   | 返回码                       |
| 2     | message         | String | 否   | 描述信息                     |
| 3     | data            | Array  | 否   | 返回信息列表                 |
| 3.1   |                 | Object |      | 返回信息实体                 |
| 3.1.1 | ip              | String | 否   | ip地址                       |
| 3.1.2 | memoryTotalSize | String | 否   | 内存总量（单位：KB）         |
| 3.1.3 | memoryUsedSize  | String | 否   | 当前内存使用量（单位：KB）   |
| 3.1.4 | cpuSize         | String | 否   | CPU的大小（单位：MHz）       |
| 3.1.5 | cpuAmount       | String | 否   | CPU的核数（单位：个）        |
| 3.1.6 | diskTotalSize   | String | 否   | 文件系统总量（单位：KB）     |
| 3.1.7 | diskUsedSize    | String | 否   | 文件系统已使用量（单位：KB） |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "memoryTotalSize": "8010916",
    "cpuAmount": "4",
    "memoryUsedSize": "7674492",
    "cpuSize": "2599",
    "ip": "127.0.0.1",
    "diskUsedSize": "306380076",
    "diskTotalSize": "515928320"
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

### 2.6 检查前置节点进程是否存活 

#### 2.6.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/front/checkNodeProcess/{frontId}** 
- 请求方式：GET
- 返回格式：JSON

#### 2.6.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注     |
| ---- | -------- | ---- | ------ | -------- |
| 1    | frontId  | Int  | 否     | 前置编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/checkNodeProcess/200001
```

#### 2.6.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型    |      | 备注                    |
| ---- | -------- | ------- | ---- | ----------------------- |
| 1    | code     | Int     | 否   | 返回码                  |
| 2    | message  | String  | 否   | 描述信息                |
| 3    | data     | boolean | 否   | true-存活；false-未存活 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": true
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

### 2.7 获取前置节点所在群组物理大小信息 

#### 2.7.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/front/getGroupSizeInfos/{frontId}**

- 请求方式：GET
- 返回格式：JSON

#### 2.7.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注     |
| ---- | -------- | ---- | ------ | -------- |
| 1    | frontId  | Int  | 否     | 前置编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/front/getGroupSizeInfos/200001
```

#### 2.7.3 返回参数 

***1）出参表***

| 序号  | 输出参数  | 类型   |      | 备注             |
| ----- | --------- | ------ | ---- | ---------------- |
| 1     | code      | Int    | 否   | 返回码           |
| 2     | message   | String | 否   | 描述信息         |
| 3     | data      | Array  | 否   | 返回信息列表     |
| 3.1   |           | List   |      | 返回信息实体     |
| 3.1.1 | groupId   | Int    | 否   | 群组id           |
| 3.1.2 | groupName | String | 否   | 群组名           |
| 3.1.3 | path      | String | 否   | 文件路径         |
| 3.1.4 | size      | Long   | 否   | 大小（单位：KB） |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "groupId": 31231,
      "groupName": "group31231",
      "path": "/data/app/nodes/127.0.0.1/node0/data/group31231",
      "size": 27085
    },
    {
      "groupId": 2,
      "groupName": "group2",
      "path": "/data/app/nodes/127.0.0.1/node0/data/group2",
      "size": 23542
    },
    {
      "groupId": 1,
      "groupName": "group1",
      "path": "/data/app/nodes/127.0.0.1/node0/data/group1",
      "size": 25077
    },
    {
      "groupId": 111,
      "groupName": "group111",
      "path": "/data/app/nodes/127.0.0.1/node0/data/group111",
      "size": 21552
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

## 3 群组管理模块

### 3.1 生成新群组

​	向单个节点请求生成新群组配置信息，节点和前置一一对应，节点编号可以从前置列表获取。适用于新群组下的节点属于不同链管理服务，每个节点都要请求一遍。

#### 3.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/generate/{nodeId}**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 3.1.2 请求参数

***1）入参表***

| 序号 | 输入参数        | 类型         | 可为空 | 备注                                 |
| ---- | --------------- | ------------ | ------ | ------------------------------------ |
| 1    | chainId         | int          | 否     | 链编号                               |
| 2    | generateGroupId | int          | 否     | 生成的群组编号                       |
| 3    | timestamp       | BigInteger   | 否     | 创世块时间（单位：ms）               |
| 4    | nodeList        | List<String> | 否     | 节点编号列表（新群组的所有节点编号） |
| 5    | description     | string       | 是     | 备注                                 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/generate/78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2
```

```
{
    "chainId": 100001,
    "generateGroupId": 2,
    "timestamp": 1574853659000,
    "nodeList": [
       "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2"
    ],
    "description": "description"
}
```

#### 3.1.3 返回参数

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    | data        | Object        | 否   | 组织信息对象               |
| 3.1  | groupId     | int           | 否   | 群组编号                   |
| 3.2  | chainId     | int           | 否   | 链编号                     |
| 3.3  | groupName   | String        | 否   | 群组名称                   |
| 3.4  | nodeCount   | int           | 否   | 节点数量                   |
| 3.5  | description | String        | 是   | 描述                       |
| 3.6  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.7  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "groupId": 2,
        "chainId": 100001,
        "groupName": "group2",
        "nodeCount": 1,
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

### 3.2 批量生成新群组

​	向新群组下所有节点请求生成新群组配置信息，节点和前置一一对应，节点编号可以从前置列表获取。适用于新群组下的节点属于同一个链管理服务。

#### 3.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/generate**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 3.2.2 请求参数

***1）入参表***

| 序号 | 输入参数        | 类型         | 可为空 | 备注                                 |
| ---- | --------------- | ------------ | ------ | ------------------------------------ |
| 1    | chainId         | int          | 否     | 链编号                               |
| 2    | generateGroupId | int          | 否     | 生成的群组编号                       |
| 3    | timestamp       | BigInteger   | 否     | 创世块时间（单位：ms）               |
| 4    | nodeList        | List<String> | 否     | 节点编号列表（新群组的所有节点编号） |
| 5    | description     | string       | 是     | 备注                                 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/generate
```

```
{
    "chainId": 100001,
    "generateGroupId": 2,
    "timestamp": 1574853659000,
    "nodeList": [
       "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2"
    ],
    "description": "description"
}
```

#### 3.2.3 返回参数

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    | data        | Object        | 否   | 组织信息对象               |
| 3.1  | groupId     | int           | 否   | 群组编号                   |
| 3.2  | chainId     | int           | 否   | 链编号                     |
| 3.3  | groupName   | String        | 否   | 群组名称                   |
| 3.4  | nodeCount   | int           | 否   | 节点数量                   |
| 3.5  | description | String        | 是   | 描述                       |
| 3.6  | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.7  | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "groupId": 2,
        "chainId": 100001,
        "groupName": "group2",
        "nodeCount": 1,
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

### 3.3 启动群组

​	生成新群组后，新群组下每一个节点都要启动，节点和前置一一对应。适用于新群组下的节点属于不同链管理服务，每个节点都要请求一遍。

#### 3.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/start/{chainId}/{startGroupId}/{nodeId}**
- 请求方式：GET
- 返回格式：JSON

#### 3.3.2 请求参数

***1）入参表***

| 序号 | 输入参数     | 类型   | 可为空 | 备注         |
| ---- | ------------ | ------ | ------ | ------------ |
| 1    | chainId      | int    | 否     | 链编号       |
| 2    | startGroupId | int    | 否     | 启动的群组id |
| 3    | nodeId       | String | 否     | 启动的节点id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/start/100001/2/78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2
```

#### 3.3.3 返回参数 

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

### 3.4 批量启动群组

​	节点和前置一一对应，节点编号可以从前置列表获取。适用于新群组下的节点属于同一个链管理服务。

#### 3.4.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/batchStart**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 3.4.2 请求参数

***1）入参表***

| 序号 | 输入参数        | 类型         | 可为空 | 备注           |
| ---- | --------------- | ------------ | ------ | -------------- |
| 1    | chainId         | int          | 否     | 链编号         |
| 2    | generateGroupId | int          | 否     | 生成的群组编号 |
| 3    | nodeList        | List<String> | 否     | 节点编号列表   |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/generate
```

```
{
    "chainId": 100001,
    "generateGroupId": 2,
    "nodeList": [
       "78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2"
    ]
}
```

#### 3.4.3 返回参数

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

### 3.5 更新群组

​	生成新群组并启动新群组的每一个节点后，调用此接口更新群组相关信息。

#### 3.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/update**
- 请求方式：GET
- 返回格式：JSON

#### 3.5.2 请求参数

***1）入参表***

无

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/update
```

#### 3.5.3 返回参数 

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

### 3.6 获取群组概况

#### 3.6.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/group/general/{chainId}/{groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 3.6.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | chainId  | int  | 否     | 链编号 |
| 2    | groupId  | int  | 否     | 群组id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/100001/300001
```

#### 3.6.3 返回参数 

***1）出参表***

| 序号 | 输出参数  | 类型   |      | 备注                       |
| ---- | --------- | ------ | ---- | -------------------------- |
| 1    | code      | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message   | String | 否   | 描述                       |
| 3    | data      | object | 否   | 返回信息实体               |
| 3.1  | groupId   | int    | 否   | 群组id                     |
| 3.2  | nodeCount | int    | 否   | 节点数量                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "data": {
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

### 3.7 获取所有群组列表

#### 3.7.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/group/all/{chainId}**
- 请求方式：GET
- 返回格式：JSON

#### 3.7.2 请求参数

***1）入参表***
无

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/all/100001
```

#### 3.7.3 返回参数 

***1）出参表***

| 序号  | 输出参数    | 类型          |      | 备注                       |
| ----- | ----------- | ------------- | ---- | -------------------------- |
| 1     | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2     | message     | String        | 否   | 描述                       |
| 3     | totalCount  | Int           | 否   | 总记录数                   |
| 4     | data        | List          | 否   | 组织列表                   |
| 4.1   |             | Object        |      | 组织信息对象               |
| 4.1.1 | chainId     | int           | 否   | 链编号                     |
| 4.1.2 | groupId     | int           | 否   | 群组编号                   |
| 4.1.3 | groupName   | String        | 否   | 群组名称                   |
| 4.1.4 | nodeCount   | int           | 否   | 节点数量                   |
| 4.1.5 | description | String        | 是   | 描述                       |
| 4.1.6 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.7 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "chainId": 100001,
            "groupId": 2,
            "groupName": "group2",
            "nodeCount": 1,
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

### 3.8 获取群组下节点共识列表

​	获取节点的共识列表，包含节点id，节点共识状态。返回所有的共识/观察节点（无论运行或停止），以及正在运行的游离节点。

#### 3.8.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/getConsensusList/{chainId}/{groupId}**
- 请求方式：GET
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 3.8.2 请求参数

***1）入参表***

| 序号 | 输入参数   | 类型 | 可为空 | 备注         |
| ---- | ---------- | ---- | ------ | ------------ |
| 1    | chainId    | Int  | 否     | 链编号       |
| 2    | groupId    | Int  | 否     | 群组id       |
| 3    | pageSize   | Int  | 是     | 条数，默认10 |
| 4    | pageNumber | Int  | 是     | 页码，默认1  |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/getConsensusList/1001/1?pageSize=10&pageNumber=1
```

#### 3.8.3 返回参数

***1）出参表***

| 序号  | 输出参数   | 类型   |      | 备注                       |
| ----- | ---------- | ------ | ---- | -------------------------- |
| 1     | code       | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2     | message    | String | 否   | 描述                       |
| 3     | totalCount | Int    | 否   | 总记录数                   |
| 4     | data       | List   | 否   | 共识列表                   |
| 4.1   |            | Object |      | 共识信息对象               |
| 4.1.1 | nodeId     | String | 否   | 节点编号                   |
| 4.1.2 | nodeType   | String | 否   | 节点类型                   |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "nodeId": "626e1f1df03e217a7a25361444b857ec68003482aabfb24645a67111cbd96ceedc998975e158475605e38b899bc97be7283006a0171f4ec4796972ff6ad55b1a",
      "nodeType": "sealer"
    },
    {
      "nodeId": "cd3a0d965ca5e5de9edce69245db827a3a253e4868e074020c3f5fb83ca0ae884d5705940c1fc1de550874de0f02374e83eaeb5317b819e420a8ff2e07e4b84c",
      "nodeType": "sealer"
    }
  ],
  "totalCount": 2
}
```

- 失败：

```
{
  "code": 205002,
  "message": "not fount any front",
  "data": null
}
```

### 3.9 设置群组下节点共识状态

​	可用于节点三种共识状态的切换。分别是共识节点sealer，观察节点observer，游离节点remove。

#### 3.9.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址： **/group/setConsensusStatus**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 3.9.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型   | 可为空 | 备注                                     |
| ---- | -------- | ------ | ------ | ---------------------------------------- |
| 1    | chainId  | Int    | 否     | 链编号                                   |
| 2    | groupId  | Int    | 否     | 群组id                                   |
| 3    | address  | String | 否     | 私钥用户地址                             |
| 4    | nodeId   | String | 否     | 节点id                                   |
| 5    | nodeType | String | 否     | 要设置的节点类型：observer/sealer/remove |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/group/setConsensusStatus
```

```
{
  "address": "0x7e1e86e06874f9276982c45de7d974cf2b87e130",
  "chainId": 1001,
  "groupId": 1,
  "nodeId": "626e1f1df03e217a7a25361444b857ec68003482aabfb24645a67111cbd96ceedc998975e158475605e38b899bc97be7283006a0171f4ec4796972ff6ad55b1a",
  "nodeType": "remove"
}
```

#### 3.9.3 返回参数

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "msg": "success"
}
```

- 失败：

```
{
    "code": -51000,
    "message": "nodeId already exist"
}
```

## 4 节点管理模块

### 4.1 查询节点信息列表

#### 4.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/nodeList/{chainId}/{groupId}/{pageNumber}/{pageSize}?nodeName={nodeName}**
- 请求方式：GET
- 返回格式：JSON

#### 4.1.2 请求参数

***1）入参表***

| 序号 | 输入参数   | 类型   | 可为空 | 备注       |
| ---- | ---------- | ------ | ------ | ---------- |
| 1    | chainId    | int    | 否     | 链编号     |
| 2    | groupId    | int    | 否     | 群组id     |
| 3    | pageSize   | Int    | 否     | 每页记录数 |
| 4    | pageNumber | Int    | 否     | 当前页码   |
| 5    | nodeName   | String | 是     | 节点名称   |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/nodeList/100001/300001/1/10?nodeName=
```

#### 4.1.3 返回参数 

***1）出参表***

| 序号   | 输出参数    | 类型          |      | 备注                       |
| ------ | ----------- | ------------- | ---- | -------------------------- |
| 1      | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2      | message     | String        | 否   | 描述                       |
| 3      | totalCount  | Int           | 否   | 总记录数                   |
| 4      | data        | List          | 是   | 节点列表                   |
| 4.1    |             | Object        |      | 节点信息对象               |
| 4.1.1  | chainId     | int           | 否   | 链编号                     |
| 4.1.2  | nodeId      | String        | 否   | 节点编号                   |
| 4.1.3  | nodeName    | string        | 否   | 节点名称                   |
| 4.1.4  | groupId     | int           | 否   | 所属群组编号               |
| 4.1.5  | nodeActive  | int           | 否   | 共识状态（1正常，2不正常）       |
| 4.1.6  | nodeIp      | string        | 否   | 节点ip                     |
| 4.1.7  | P2pPort     | int           | 否   | 节点p2p端口                |
| 4.1.8  | description | String        | 否   | 备注                       |
| 4.1.9  | blockNumber | BigInteger    | 否   | 节点块高                   |
| 4.1.10 | pbftView    | BigInteger    | 否   | Pbft view                  |
| 4.1.11 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 4.1.12 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "totalCount": 1,
    "data": [
        {
            "chainId": 100001,
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

### 4.2 查询节点信息

​	节点和前置一一对应，节点编号可以从前置列表获取。

#### 4.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/nodeInfo/{chainId}/{groupId}/{nodeId}**
- 请求方式：GET
- 返回格式：JSON

#### 4.2.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型   | 可为空 | 备注     |
| ---- | -------- | ------ | ------ | -------- |
| 1    | chainId  | Int    | 否     | 链编号   |
| 2    | groupId  | Int    | 否     | 群组id   |
| 3    | nodeId   | String | 否     | 节点编号 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/nodeInfo/100001/1/78e467957af3d0f77e19b952a740ba8c53ac76913df7dbd48d7a0fe27f4c902b55e8543e1c4f65b4a61695c3b490a5e8584149809f66e9ffc8c05b427e9d3ca2
```

#### 4.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                       |
| ---- | ----------- | ------------- | ---- | -------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String        | 否   | 描述                       |
| 3    |             | Object        |      | 节点信息对象               |
| 3.1  | chainId     | Int           | 否   | 链编号                     |
| 3.2  | nodeId      | String        | 否   | 节点编号                   |
| 3.3  | nodeName    | String        | 否   | 节点名称                   |
| 3.4  | groupId     | Int           | 否   | 所属群组编号               |
| 3.5  | nodeActive  | Int           | 否   | 共识状态（1正常，2不正常）       |
| 3.6  | nodeIp      | String        | 否   | 节点ip                     |
| 3.7  | P2pPort     | Int           | 否   | 节点p2p端口                |
| 3.8  | description | String        | 否   | 备注                       |
| 3.9  | blockNumber | BigInteger    | 否   | 节点块高                   |
| 3.10 | pbftView    | BigInteger    | 否   | Pbft view                  |
| 3.11 | createTime  | LocalDateTime | 否   | 落库时间                   |
| 3.12 | modifyTime  | LocalDateTime | 否   | 修改时间                   |

***2）出参示例***

- 成功：

```
{
    "code": 0,
    "message": "success",
    "data": {
        "chainId": 100001,
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

### 4.3 获取区块高度

#### 4.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/getBlockNumber/{chainId}/{groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 4.3.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | chainId  | Int  | 否     | 链编号 |
| 2    | groupId  | Int  | 否     | 群组id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/getBlockNumber/1001/1
```

#### 4.3.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | Object |      | 块高                       |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": 74
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

### 4.4 根据区块高度获取区块信息

#### 4.4.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/getBlockByNumber/{chainId}/{groupId}/{blockNumber}**
- 请求方式：GET
- 返回格式：JSON

#### 4.4.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型       | 可为空 | 备注     |
| ---- | ----------- | ---------- | ------ | -------- |
| 1    | chainId     | Int        | 否     | 链编号   |
| 2    | groupId     | Int        | 否     | 群组id   |
| 3    | blockNumber | BigInteger | 否     | 区块高度 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/getBlockByNumber/1001/1/1
```

#### 4.4.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | Object |      | 区块信息                   |

***2）出参示例***

- 成功：

```
  "code": 0,
  "message": "success",
  "data": {
    "number": 1,
    "hash": "0x74ce7bf9daea04cfc9f69a2269f5f524dc62fcc19c7c649d56ded98c064321dd",
    "parentHash": "0xcd55822ef3c4bf20cd12a110e0d7d14e436385dd68ed133e4bf48183208943dc",
    "nonce": 0,
    "sha3Uncles": null,
    "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
    "transactionsRoot": "0x623f3f6b4a0bf166d70001876dd5ce6af6d215aa4282e390580e66f65f652eb5",
    "stateRoot": "0x286b6bb8045118b1f4429f4155e71847cf2c021dce78bf1ef780c5d131dfe0f2",
    "receiptsRoot": "0x5c15415b928ba2726259094659d3753d752e009fd5c36d4e86138e7260890553",
    "author": null,
    "sealer": "0x1",
    "mixHash": null,
    "extraData": [],
    "gasLimit": 0,
    "gasUsed": 0,
    "timestamp": 1577777367654,
    "transactions": [
      {
        "hash": "0x2bf33fff3b81d74548079a669333aef601d4d2acaf8d33a31687fac8d5d9c815",
        "nonce": 4.2909445613494797e+74,
        "blockHash": "0x74ce7bf9daea04cfc9f69a2269f5f524dc62fcc19c7c649d56ded98c064321dd",
        "blockNumber": 1,
        "transactionIndex": 0,
        "from": "0x42446154be80379b68debfdb06682d29d084fad4",
        "to": null,
        "value": 0,
        "gasPrice": 1,
        "gas": 100000000,
        "input": "0xxx",
        "creates": null,
        "publicKey": null,
        "raw": null,
        "r": null,
        "s": null,
        "v": 0,
        "blockNumberRaw": "1",
        "transactionIndexRaw": "0",
        "nonceRaw": "429094456134947991292268568258086729239801142894854477452577045806616816236",
        "gasRaw": "100000000",
        "valueRaw": "0",
        "gasPriceRaw": "1"
      }
    ],
    "uncles": null,
    "sealerList": [
      "626e1f1df03e217a7a25361444b857ec68003482aabfb24645a67111cbd96ceedc998975e158475605e38b899bc97be7283006a0171f4ec4796972ff6ad55b1a",
      "cd3a0d965ca5e5de9edce69245db827a3a253e4868e074020c3f5fb83ca0ae884d5705940c1fc1de550874de0f02374e83eaeb5317b819e420a8ff2e07e4b84c"
    ],
    "timestampRaw": "1577777367654",
    "nonceRaw": "0",
    "gasUsedRaw": "0",
    "gasLimitRaw": "0",
    "numberRaw": "1"
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

### 4.5 获取群组交易总数信息

#### 4.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/getTotalTransactionCount/{chainId}/{groupId}**
- 请求方式：GET
- 返回格式：JSON

#### 4.5.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型 | 可为空 | 备注   |
| ---- | -------- | ---- | ------ | ------ |
| 1    | chainId  | Int  | 否     | 链编号 |
| 2    | groupId  | Int  | 否     | 群组id |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/getTotalTransactionCount/1001/1
```

#### 4.5.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型   |      | 备注                       |
| ---- | ----------- | ------ | ---- | -------------------------- |
| 1    | code        | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message     | String | 否   | 描述                       |
| 3    | data        | Object |      |                            |
| 3.1  | txSum       | Int    | 否   | 交易总数                   |
| 3.2  | blockNumber | Int    | 否   | 当前块高                   |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "txSum": 74,
    "blockNumber": 74
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

### 4.6 根据交易hash获取交易信息

#### 4.6.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/getTransactionByHash/{chainId}/{groupId}/{transHash}**
- 请求方式：GET
- 返回格式：JSON

#### 4.6.2 请求参数

***1）入参表***

| 序号 | 输入参数  | 类型   | 可为空 | 备注     |
| ---- | --------- | ------ | ------ | -------- |
| 1    | chainId   | Int    | 否     | 链编号   |
| 2    | groupId   | Int    | 否     | 群组id   |
| 3    | transHash | String | 否     | 交易hash |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/getTransactionByHash/1001/1/0x2bf33fff3b81d74548079a669333aef601d4d2acaf8d33a31687fac8d5d9c815
```

#### 4.6.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | Object |      | 交易信息                   |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "hash": "0x2bf33fff3b81d74548079a669333aef601d4d2acaf8d33a31687fac8d5d9c815",
    "nonce": 4.2909445613494797e+74,
    "blockHash": "0x74ce7bf9daea04cfc9f69a2269f5f524dc62fcc19c7c649d56ded98c064321dd",
    "blockNumber": 1,
    "transactionIndex": 0,
    "from": "0x42446154be80379b68debfdb06682d29d084fad4",
    "to": "0x0000000000000000000000000000000000000000",
    "value": 0,
    "gasPrice": 1,
    "gas": 100000000,
    "input": "0xxxx",
    "creates": null,
    "publicKey": null,
    "raw": null,
    "r": null,
    "s": null,
    "v": 0,
    "blockNumberRaw": "1",
    "transactionIndexRaw": "0",
    "nonceRaw": "429094456134947991292268568258086729239801142894854477452577045806616816236",
    "gasRaw": "100000000",
    "valueRaw": "0",
    "gasPriceRaw": "1"
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

### 4.7 根据交易hash获取交易回执信息

#### 4.7.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/node/getTransactionReceipt/{chainId}/{groupId}/{transHash}**
- 请求方式：GET
- 返回格式：JSON

#### 4.7.2 请求参数

***1）入参表***

| 序号 | 输入参数  | 类型   | 可为空 | 备注     |
| ---- | --------- | ------ | ------ | -------- |
| 1    | chainId   | Int    | 否     | 链编号   |
| 2    | groupId   | Int    | 否     | 群组id   |
| 3    | transHash | String | 否     | 交易hash |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/node/getTransactionReceipt/1001/1/0x2bf33fff3b81d74548079a669333aef601d4d2acaf8d33a31687fac8d5d9c815
```

#### 4.7.3 返回参数 

***1）出参表***

| 序号 | 输出参数 | 类型   |      | 备注                       |
| ---- | -------- | ------ | ---- | -------------------------- |
| 1    | code     | Int    | 否   | 返回码，0：成功 其它：失败 |
| 2    | message  | String | 否   | 描述                       |
| 3    | data     | Object |      | 交易回执信息               |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "transactionHash": "0x2bf33fff3b81d74548079a669333aef601d4d2acaf8d33a31687fac8d5d9c815",
    "transactionIndex": 0,
    "blockHash": "0x74ce7bf9daea04cfc9f69a2269f5f524dc62fcc19c7c649d56ded98c064321dd",
    "blockNumber": 1,
    "gasUsed": 371053,
    "contractAddress": "0xff15a64b529be2538826acd6bd436ebdedbc0557",
    "root": "0x286b6bb8045118b1f4429f4155e71847cf2c021dce78bf1ef780c5d131dfe0f2",
    "status": "0x0",
    "message": null,
    "from": "0x42446154be80379b68debfdb06682d29d084fad4",
    "to": "0x0000000000000000000000000000000000000000",
    "input": "0xxxxx",
    "output": "0x",
    "logs": [],
    "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
    "blockNumberRaw": "1",
    "transactionIndexRaw": "0",
    "gasUsedRaw": "371053",
    "statusOK": true
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

## 5 合约管理模块  

### 5.1 编译合约

​	接口参数为合约文件压缩成zip并Base64编码后的字符串。合约文件需要放在同级目录压缩，涉及引用请使用"./XXX.sol"。可参考测试类ContractControllerTest的testCompileContract()方法。

#### 5.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/contract/compile**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 5.1.2 请求参数

***1）入参表***

| 序号 | 输入参数       | 类型   | 可为空 | 备注                                        |
| ---- | -------------- | ------ | ------ | ------------------------------------------- |
| 1    | contractSource | String | 是     | 合约源码（合约文件压缩成zip，并Base64编码） |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/compile
```

```
{
  "contractSource": "UEsDBBQACAgIACGIZVAAAAAAAAAAAAAAAAAOAAAASGVsbG9Xb3JsZC5zb2yFjjELwjAQhfdC/8ONzVIkuBV3JxcHNyGkZwkkF0muBZH+dxPaYpGqb7x373vvHlTnFERvTWv4Adddva9lUxbaEwelGY5orb/4YNtnWUBS5GCoA1IOm+mCAxLDGfmUbtXKF1sPcuvj1pNm4wk65Eqk8sgqRQJyHyjOCTEvyJqc9YrxgxQTaWlaJ9GZ91haBmRl2IG+4+RfnvwNHF9QSwcIuSZqBJwAAABvAQAAUEsBAhQAFAAICAgAIYhlULkmagScAAAAbwEAAA4AAAAAAAAAAAAAAAAAAAAAAEhlbGxvV29ybGQuc29sUEsFBgAAAAABAAEAPAAAANgAAAAAAA=="
}
```

#### 5.1.3 返回参数 

***1）出参表***

| 序号  | 输出参数     | 类型   |      | 备注                              |
| ----- | ------------ | ------ | ---- | --------------------------------- |
| 1     | code         | Int    | 否   | 返回码，0：成功 其它：失败        |
| 2     | message      | String | 否   | 描述                              |
| 3     | data         | List   |      | 列表                              |
| 3.1   |              | Object |      | 信息对象                          |
| 3.1.1 | contractName | String | 否   | 合约名称                          |
| 3.1.2 | contractAbi  | String | 是   | 编译合约生成的abi文件内容         |
| 3.1.3 | bytecodeBin  | String | 是   | 合约bytecode binary，用于部署合约 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "contractName": "HelloWorld",
      "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"string\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"string\"}],\"name\":\"set2\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"string\"}],\"name\":\"SetName2\",\"type\":\"event\"}]",
      "bytecodeBin": "608060405234801561001057600080fd5b5061049d806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e1461005c5780636d4ce63c146100c557806394ab626314610155575b600080fd5b34801561006857600080fd5b506100c3600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192905050506101be565b005b3480156100d157600080fd5b506100da610274565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561011a5780820151818401526020810190506100ff565b50505050905090810190601f1680156101475780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561016157600080fd5b506101bc600480360381019080803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509192919290505050610316565b005b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b83811015610220578082015181840152602081019050610205565b50505050905090810190601f16801561024d5780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906102709291906103cc565b5050565b606060008054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561030c5780601f106102e15761010080835404028352916020019161030c565b820191906000526020600020905b8154815290600101906020018083116102ef57829003601f168201915b5050505050905090565b7f5a10b7f1e7b0001e5072838ada067bb4410151e165607a3465bf0620c412e2a3816040518080602001828103825283818151815260200191508051906020019080838360005b8381101561037857808201518184015260208101905061035d565b50505050905090810190601f1680156103a55780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906103c89291906103cc565b5050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061040d57805160ff191683800117855561043b565b8280016001018555821561043b579182015b8281111561043a57825182559160200191906001019061041f565b5b509050610448919061044c565b5090565b61046e91905b8082111561046a576000816000905550600101610452565b5090565b905600a165627a7a72305820a29a15f3aad04ec24023c149e72a1c9690158e2b5835ce7b3054e0200947f1ea0029"
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

### 5.2 保存合约和更新

#### 5.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/contract/save**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 5.2.2 请求参数

***1）入参表***

| 序号 | 输入参数       | 类型   | 可为空 | 备注                                       |
| ---- | -------------- | ------ | ------ | ------------------------------------------ |
| 1    | chainId        | Int    | 否     | 链编号                                     |
| 2    | groupId        | Int    | 否     | 所属群组编号                               |
| 3    | contractName   | String | 否     | 合约名称                                   |
| 4    | contractSource | String | 是     | 合约源码，Base64编码                       |
| 5    | contractAbi    | String | 是     | 编译合约生成的abi文件内容                  |
| 6    | contractBin    | String | 是     | 合约运行时binary，用于合约解析             |
| 7    | bytecodeBin    | String | 是     | 合约bytecode binary，用于部署合约          |
| 8    | contractId     | String | 是     | 合约编号（为空时表示新增，不为空表示更新） |
| 9    | contractPath   | String | 否     | 合约所在目录                               |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/save
```

```
{
  "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
  "chainId": 1001,
  "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
  "contractBin": "xxxx",
  "contractName": "HelloWorld",
  "contractPath": "/",
  "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
  "groupId": 1
}
```

#### 5.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数        | 类型          |      | 备注                                    |
| ---- | --------------- | ------------- | ---- | --------------------------------------- |
| 1    | code            | Int           | 否   | 返回码，0：成功 其它：失败              |
| 2    | message         | String        | 否   | 描述                                    |
| 3    |                 | Object        |      | 返回信息实体                            |
| 3.1  | contractId      | Int           | 否   | 合约编号                                |
| 3.2  | contractPath    | String        | 否   | 合约所在目录                            |
| 3.3  | contractName    | String        | 否   | 合约名称                                |
| 3.4  | chainId         | Int           | 否   | 链编号                                  |
| 3.5  | groupId         | Int           | 否   | 所属群组编号                            |
| 3.6  | contractStatus  | Int           | 否   | 1未部署，2已部署                        |
| 3.7  | contractType    | Int           | 否   | 合约类型(0-普通合约，1-系统合约，默认0) |
| 3.8  | contractSource  | String        | 否   | 合约源码                                |
| 3.9  | contractAbi     | String        | 是   | 编译合约生成的abi文件内容               |
| 3.10 | contractBin     | String        | 是   | 合约运行时binary，用于合约解析          |
| 3.11 | bytecodeBin     | String        | 是   | 合约bytecode binary，用于部署合约       |
| 3.12 | contractAddress | String        | 是   | 合约地址                                |
| 3.13 | deployTime      | LocalDateTime | 是   | 部署时间                                |
| 3.14 | description     | String        | 是   | 备注                                    |
| 3.15 | createTime      | LocalDateTime | 否   | 创建时间                                |
| 3.16 | modifyTime      | LocalDateTime | 是   | 修改时间                                |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 2,
    "contractPath": "/",
    "contractName": "HelloWorld",
    "contractStatus": 1,
    "chainId": 1001,
    "groupId": 1,
    "contractType": 0,
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
    "contractBin": "xxxx",
    "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
    "contractAddress": null,
    "deployTime": null,
    "description": null,
    "createTime": "2020-02-26 17:51:46",
    "modifyTime": "2020-02-26 17:51:46"
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

### 5.3 查询合约列表 


#### 5.3.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/contractList**
* 请求方式：POST
* 返回格式：JSON

#### 5.3.2 请求参数


***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1 | chainId | Int | 否 | 链编号 |
| 1      | groupId       | Int           | 否     | 群组id                                          |
| 2      | contractName       | String           | 是    | 合约名                             |
| 3      | contractAddress    | String           | 是    | 合约地址                               |
| 4      | pageSize        | Int           | 是    | 每页记录数                                      |
| 5      | pageNumber      | Int           | 是    | 当前页码                                        |
| 6      | contractStatus      | Int           | 是    | 1未部署，2已部署                        |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/contractList
```

```
{
  "chainId": 1001,
  "groupId": 1
}
```

#### 5.3.3 返回参数 

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
| 5.1.4 | chainId | int | 否 | 链编号 |
| 5.1.5  | groupId       | Int           | 否     | 所属群组编号                                    |
| 5.1.6  | contractStatus      | int           | 否     | 1未部署，2已部署                        |
| 5.1.7  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 5.1.8  | contractSource  | String        | 否     | 合约源码                                        |
| 5.1.9  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 5.1.10 | contractBin     | String        | 是     | 合约运行时binary，用于合约解析               |
| 5.1.11 | bytecodeBin     | String        | 是     | 合约bytecode binary，用于部署合约                 |
| 5.1.12 | contractAddress | String        | 是     | 合约地址                                        |
| 5.1.13 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 5.1.14 | description     | String        | 是     | 备注                                            |
| 5.1.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 5.1.16 | modifyTime | LocalDateTime | 是 | 修改时间 |
***2）出参示例***

* 成功：
```
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "contractId": 2,
      "contractPath": "/",
      "contractName": "HelloWorld",
      "contractStatus": 1,
      "chainId": 1001,
      "groupId": 1,
      "contractType": 0,
      "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
      "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
      "contractBin": "xxxx",
      "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
      "contractAddress": null,
      "deployTime": null,
      "description": null,
      "createTime": "2020-02-26 17:51:46",
      "modifyTime": "2020-02-26 17:51:46"
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


### 5.4 查询合约信息


#### 5.4.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/{contractId}**
* 请求方式：GET
* 返回格式：JSON

#### 5.4.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | contractId      | int           | 否     | 合约编号                                        |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/2
```

#### 5.4.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Object         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4 | chainId | int | 否 | 链编号 |
| 3.5  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.6  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.7  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.8  | contractSource  | String        | 否     | 合约源码                                        |
| 3.9  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.10 | contractBin     | String        | 是     | 合约运行时binary，用于合约解析               |
| 3.11 | bytecodeBin     | String        | 是     | 合约bytecode binary，用于部署合约                 |
| 3.12 | contractAddress | String        | 是     | 合约地址                                        |
| 3.13 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |

***2）出参示例***

* 成功：
```
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 2,
    "contractPath": "/",
    "contractName": "HelloWorld",
    "contractStatus": 1,
    "chainId": 1001,
    "groupId": 1,
    "contractType": 0,
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
    "contractBin": "xxxx",
    "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
    "contractAddress": null,
    "deployTime": null,
    "description": null,
    "createTime": "2020-02-26 17:51:46",
    "modifyTime": "2020-02-26 17:51:46"
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

### 5.5 部署合约


#### 5.5.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/deploy**
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.4.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1 | chainId | Int | 否 | 链编号 |
| 2    | groupId           | Int            | 否     | 所属群组编号               |
| 3    | contractName      | String         | 否     | 合约名称               |
| 4    | contractSource    | String         | 是    | 合约源码                   |
| 5    | contractAbi       | String         | 否     | 编译合约生成的abi文件内容  |
| 6    | contractBin       | String         | 是    | 合约运行时binary，用于合约解析 |
| 7    | bytecodeBin       | String         | 否     | 合约bytecode binary，用于部署合约 |
| 8    | contractId      | String         | 否     | 合约名称               |
| 9    | contractPath      | String         | 否     | 合约所在目录               |
| 10   | user              | String         | 否     | 私钥用户地址             |
| 11    | constructorParams | List | 是     | 构造函数入参               |


***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/deploy
```

```
{
  "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
  "chainId": 1001,
  "constructorParams": [
  ],
  "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
  "contractBin": "xxxx",
  "contractId": 2,
  "contractName": "HelloWorld",
  "contractPath": "/",
  "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
  "groupId": 1,
  "user": "0x58df289113863a9bff8fd24c984a4ad51d36cd2d"
}
```

#### 5.5.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |        | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1    | code            | Int           | 否     | 返回码，0：成功 其它：失败                      |
| 2    | message         | String        | 否     | 描述                                            |
| 3    |                 | Object         |        | 返回信息实体                                    |
| 3.1  | contractId      | int           | 否     | 合约编号                                        |
| 3.2  | contractPath    | String        | 否     | 合约所在目录                              |
| 3.3  | contractName    | String        | 否     | 合约名称                                        |
| 3.4 | chainId | int | 否 | 链编号 |
| 3.5  | groupId         | Int           | 否     | 所属群组编号                                    |
| 3.6  | contractStatus  | int           | 否     | 1未部署，2已部署                        |
| 3.7  | contractType    | Int           | 否     | 合约类型(0-普通合约，1-系统合约)                |
| 3.8  | contractSource  | String        | 否     | 合约源码                                        |
| 3.9  | contractAbi     | String        | 是     | 编译合约生成的abi文件内容                       |
| 3.10 | contractBin     | String        | 是     | 合约binary                                      |
| 3.11 | bytecodeBin     | String        | 是     | 合约bin                                         |
| 3.12 | contractAddress | String        | 是     | 合约地址                                        |
| 3.13 | deployTime      | LocalDateTime | 是     | 部署时间                                        |
| 3.14 | description     | String        | 是     | 备注                                            |
| 3.15 | createTime      | LocalDateTime | 否     | 创建时间                                        |
| 3.16 | modifyTime      | LocalDateTime | 是     | 修改时间                                        |


***2）出参示例***
* 成功：
```
{
  "code": 0,
  "message": "success",
  "data": {
    "contractId": 2,
    "contractPath": "/",
    "contractName": "HelloWorld",
    "contractStatus": 2,
    "chainId": 1001,
    "groupId": 1,
    "contractType": null,
    "contractSource": "cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjsNCmNvbnRyYWN0IEhlbGxvV29ybGR7DQogICAgc3RyaW5nIG5hbWU7DQogICAgZXZlbnQgU2V0TmFtZShzdHJpbmcgbmFtZSk7DQogICAgZnVuY3Rpb24gZ2V0KCljb25zdGFudCByZXR1cm5zKHN0cmluZyl7DQogICAgICAgIHJldHVybiBuYW1lOw0KICAgIH0NCiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nIG4pew0KICAgICAgICBlbWl0IFNldE5hbWUobik7DQogICAgICAgIG5hbWU9bjsNCiAgICB9DQp9",
    "contractAbi": "[{\"constant\":false,\"inputs\":[{\"name\":\"n\",\"type\":\"String\"}],\"name\":\"set\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"name\":\"\",\"type\":\"String\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"name\",\"type\":\"String\"}],\"name\":\"SetName\",\"type\":\"event\"}]",
    "contractBin": "xxxx",
    "bytecodeBin": "608060405234801561001057600080fd5b50610373806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680634ed3885e146100515780636d4ce63c146100ba575b600080fd5b34801561005d57600080fd5b506100b8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929050505061014a565b005b3480156100c657600080fd5b506100cf610200565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010f5780820151818401526020810190506100f4565b50505050905090810190601f16801561013c5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b7f4df9dcd34ae35f40f2c756fd8ac83210ed0b76d065543ee73d868aec7c7fcf02816040518080602001828103825283818151815260200191508051906020019080838360005b838110156101ac578082015181840152602081019050610191565b50505050905090810190601f1680156101d95780820380516001836020036101000a031916815260200191505b509250505060405180910390a180600090805190602001906101fc9291906102a2565b5050565b606060008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156102985780601f1061026d57610100808354040283529160200191610298565b820191906000526020600020905b81548152906001019060200180831161027b57829003601f168201915b5050505050905090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106102e357805160ff1916838001178555610311565b82800160010185558215610311579182015b828111156103105782518255916020019190600101906102f5565b5b50905061031e9190610322565b5090565b61034491905b80821115610340576000816000905550600101610328565b5090565b905600a165627a7a72305820d3d37c423723b7082e475b2ff27e3db3abb1dad17430a8a7ec3f1e09a8b1e7b80029",
    "contractAddress": "0xd639179eaa10b59e44e9becd8cdbd340d33ae814",
    "deployTime": "2020-02-26 18:41:35",
    "description": null,
    "createTime": "2020-02-26 17:51:46",
    "modifyTime": "2020-02-26 18:41:35"
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


### 5.6 发送交易


#### 5.6.1 传输协议规范
* 网络传输协议：使用HTTP协议
* 请求地址：**/contract/transaction**
* 请求方式：POST
* 请求头：Content-type: application/json
* 返回格式：JSON

#### 5.6.2 请求参数


***1）入参表***

| 序号 | 输入参数    | 类型          | 可为空 | 备注                                       |
|------|-------------|---------------|--------|-------------------------------|
| 1 | chainId | int | 否 | 链编号 |
| 2    | groupId      | Int            | 否     | 所属群组编号               |
| 3    | user       | String  | 否     | 私钥用户地址           |
| 4    | contractName | String         | 否     | 合约名称                   |
| 5    | contractId      | Int      | 否     | 合约编号               |
| 6    | funcName     | String         | 否     | 合约方法名                 |
| 7    | contractAddress     | String         | 是     | 合约地址   |
| 8   | funcParam    | List | 是     | 合约方法入参               |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/contract/transaction
```

```
{
  "chainId": 1001,
  "contractAddress": "0xd639179eaa10b59e44e9becd8cdbd340d33ae814",
  "contractId": 2,
  "contractName": "HelloWorld",
  "funcName": "get",
  "funcParam": [],
  "groupId": 1,
  "useAes": true,
  "user": "0x58df289113863a9bff8fd24c984a4ad51d36cd2d"
}
```


#### 5.6.3 返回参数 

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
  "data": [
    "test"
  ]
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

## 6 用户管理模块 

### 6.1 新增私钥用户

#### 6.1.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/user/userInfo**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 6.1.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注                                             |
| ---- | ----------- | ------ | ------ | ------------------------------------------------ |
| 1    | userName    | String | 否     | 用户名称                                         |
| 2    | description | String | 是     | 备注                                             |
| 3    | groupId     | Int    | 否     | 所属群组                                         |
| 4    | chainId     | Int    | 否     | 所属链                                           |
| 5    | userType    | Int    | 是     | 用户类型（1-普通用户 2-系统用户，默认1），暂未用 |

***2）入参示例***

```
http://127.0.0.1:5005//WeBASE-Chain-Manager/user/userInfo
```

```
{
  "chainId": 100001,
  "description": "fdasf",
  "groupId": 1,
  "userName": "zhangsan",
  "userType": 1
}
```

#### 6.1.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                                     |
| ---- | ----------- | ------------- | ---- | ---------------------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败               |
| 2    | message     | String        | 否   | 描述                                     |
| 3    | data        | object        | 是   | 返回信息实体（成功时不为空）             |
| 3.1  | userId      | Int           | 否   | 用户编号                                 |
| 3.2  | userName    | String        | 否   | 用户名称                                 |
| 3.3  | chainId     | Int           | 否   | 所属链                                   |
| 3.4  | groupId     | Int           | 否   | 所属群组编号                             |
| 3.5  | description | String        | 是   | 备注                                     |
| 3.6  | userStatus  | Int           | 否   | 状态（1-正常 2-停用， 默认1）            |
| 3.7  | userType    | Int           | 否   | 用户类型（1-普通用户 2-系统用户，默认1） |
| 3.8  | publicKey   | String        | 否   | 公钥信息                                 |
| 3.9  | address     | String        | 是   | 用户地址（在链上位置的hash）             |
| 3.10 | hasPk       | Int           | 否   | 是否拥有私钥信息(1-拥有，2-不拥有)       |
| 3.11 | createTime  | LocalDateTime | 否   | 创建时间                                 |
| 3.12 | modifyTime  | LocalDateTime | 否   | 修改时间                                 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 700001,
    "userName": "zhangsan",
    "chainId": 100001,
    "groupId": 1,
    "publicKey": "0xe8a76f9a01557496d24f9dd167dffd4a3ea71b7b928f74a833d32c5a417b48ad5bda92bbe45e465b3a5ca81a3b0d8a47d7283e1f4742f86ea22c5bc0476fb64e",
    "userStatus": 1,
    "userType": 1,
    "address": "0xffe021fcf6e08be18104e3b82886159fb4f17386",
    "hasPk": 1,
    "description": "fdasf",
    "createTime": "2019-12-31 15:12:16",
    "modifyTime": "2019-12-31 15:12:16"
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

### 6.2 绑定公钥用户

#### 6.2.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/user/bind**
- 请求方式：POST
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 6.2.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注                                             |
| ---- | ----------- | ------ | ------ | ------------------------------------------------ |
| 1    | userName    | String | 否     | 用户名称                                         |
| 2    | description | String | 是     | 备注                                             |
| 3    | groupId     | Int    | 否     | 所属群组                                         |
| 4    | chainId     | Int    | 否     | 所属链                                           |
| 5    | userType    | Int    | 是     | 用户类型（1-普通用户 2-系统用户，默认1），暂未用 |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/user/bind
```

```
{
  "chainId": 100001,
  "description": "test",
  "groupId": 1,
  "publicKey": "0xa5e3298e8052fc419658b796755d65c6c86bdb9a051d9cbd7ab0ec67ea97bf008d18d58b812f6fd24e3c4841f96ef8d5d13b55a761e1086815b2b7a2c9f7b33a",
  "userName": "lisi",
  "userType": 1
}
```

#### 6.2.3 返回参数 

***1）出参表***

| 序号 | 输出参数    | 类型          |      | 备注                                     |
| ---- | ----------- | ------------- | ---- | ---------------------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败               |
| 2    | message     | String        | 否   | 描述                                     |
| 3    | data        | object        | 是   | 返回信息实体（成功时不为空）             |
| 3.1  | userId      | Int           | 否   | 用户编号                                 |
| 3.2  | userName    | String        | 否   | 用户名称                                 |
| 3.3  | chainId     | Int           | 否   | 所属链                                   |
| 3.4  | groupId     | Int           | 否   | 所属群组编号                             |
| 3.5  | description | String        | 是   | 备注                                     |
| 3.6  | userStatus  | Int           | 否   | 状态（1-正常 2-停用，默认1）             |
| 3.7  | userType    | Int           | 否   | 用户类型（1-普通用户 2-系统用户，默认1） |
| 3.8  | publicKey   | String        | 否   | 公钥信息                                 |
| 3.9  | address     | String        | 是   | 用户地址（在链上位置的hash）             |
| 3.10 | hasPk       | Int           | 否   | 是否拥有私钥信息(1-拥有，2-不拥有)       |
| 3.11 | createTime  | LocalDateTime | 否   | 创建时间                                 |
| 3.12 | modifyTime  | LocalDateTime | 否   | 修改时间                                 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 700002,
    "userName": "lisi",
    "chainId": 100001,
    "groupId": 1,
    "publicKey": "0xa5e3298e8052fc419658b796755d65c6c86bdb9a051d9cbd7ab0ec67ea97bf008d18d58b812f6fd24e3c4841f96ef8d5d13b55a761e1086815b2b7a2c9f7b33a",
    "userStatus": 1,
    "userType": 1,
    "address": "0x3107000a54392e13bccab685b1c3b74151720b7a",
    "hasPk": 2,
    "description": "test",
    "createTime": "2019-12-31 15:16:56",
    "modifyTime": "2019-12-31 15:16:56"
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

### 6.3 修改用户备注

#### 6.3.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/user/userInfo**
- 请求方式：PUT
- 请求头：Content-type: application/json
- 返回格式：JSON

#### 6.3.2 请求参数

***1）入参表***

| 序号 | 输入参数    | 类型   | 可为空 | 备注     |
| ---- | ----------- | ------ | ------ | -------- |
| 1    | userId      | int    | 否     | 用户编号 |
| 2    | description | String | 是     | 备注     |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/user/userInfo
```

```
{
    "userId": 700002,
    "description": "newDescription"
}
```

#### 6.3.3 返回参数 

***1）出参表***

***

| 序号 | 输出参数    | 类型          |      | 备注                                     |
| ---- | ----------- | ------------- | ---- | ---------------------------------------- |
| 1    | code        | Int           | 否   | 返回码，0：成功 其它：失败               |
| 2    | message     | String        | 否   | 描述                                     |
| 3    | data        | object        | 是   | 返回信息实体（成功时不为空）             |
| 3.1  | userId      | Int           | 否   | 用户编号                                 |
| 3.2  | userName    | String        | 否   | 用户名称                                 |
| 3.3  | chainId     | Int           | 否   | 所属链                                   |
| 3.4  | groupId     | Int           | 否   | 所属群组编号                             |
| 3.5  | description | String        | 是   | 备注                                     |
| 3.6  | userStatus  | Int           | 否   | 状态（1-正常 2-停用，默认1）             |
| 3.7  | userType    | Int           | 否   | 用户类型（1-普通用户 2-系统用户，默认1） |
| 3.8  | publicKey   | String        | 否   | 公钥信息                                 |
| 3.9  | address     | String        | 是   | 用户地址（在链上位置的hash）             |
| 3.10 | hasPk       | Int           | 否   | 是否拥有私钥信息(1-拥有，2-不拥有)       |
| 3.11 | createTime  | LocalDateTime | 否   | 创建时间                                 |
| 3.12 | modifyTime  | LocalDateTime | 否   | 修改时间                                 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "userId": 700002,
    "userName": "lisi",
    "chainId": 100001,
    "groupId": 1,
    "publicKey": "0xa5e3298e8052fc419658b796755d65c6c86bdb9a051d9cbd7ab0ec67ea97bf008d18d58b812f6fd24e3c4841f96ef8d5d13b55a761e1086815b2b7a2c9f7b33a",
    "userStatus": 1,
    "userType": 1,
    "address": "0x3107000a54392e13bccab685b1c3b74151720b7a",
    "hasPk": 2,
    "description": "newDescription",
    "createTime": "2019-12-31 15:16:56",
    "modifyTime": "2019-12-31 15:16:56"
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

### 6.4 查询私钥

#### 6.4.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/user/privateKey/{address}**
- 请求方式：GET
- 返回格式：json

#### 6.4.2 请求参数

***1）入参表***

| 序号 | 输入参数 | 类型   | 可为空 | 备注                         |
| ---- | -------- | ------ | ------ | ---------------------------- |
| 1    | address  | String | 否     | 用户地址（在链上位置的hash） |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/user/privateKey/0xffe021fcf6e08be18104e3b82886159fb4f17386
```

#### 6.4.3 返回参数 

***1）出参表***

| 序号 | 输出参数   | 类型   |      | 备注                         |
| ---- | ---------- | ------ | ---- | ---------------------------- |
| 1    | code       | Int    | 否   | 返回码，0：成功 其它：失败   |
| 2    | message    | String | 否   | 描述                         |
| 3    | data       | Object | 否   | 返回私钥信息实体             |
| 3.1  | privateKey | String | 否   | 私钥（加密的）               |
| 3.2  | address    | String | 否   | 用户地址（在链上位置的hash） |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": {
    "privateKey": "xzN5aJkq1f1v7kxQlPBKUEal9YwDgFC/0teltOPvW1W0aOeR0jZwpxWNmGSKP9G82tzYcTH8GBShwTeB+jh+QmbLMjuEpi2borChOV8nLUg=",
    "address": "0xffe021fcf6e08be18104e3b82886159fb4f17386"
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

### 6.5 查询用户列表

#### 6.5.1 传输协议规范

- 网络传输协议：使用HTTP协议
- 请求地址：**/user/userList/{chainId}/{groupId}/{pageNumber}/{pageSize}?userParam={userName}**
- 请求方式：GET
- 返回格式：JSON

#### 6.5.2 请求参数

***1）入参表***

| 序号 | 输入参数   | 类型   | 可为空 | 备注                         |
| ---- | ---------- | ------ | ------ | ---------------------------- |
| 1    | chainId    | Int    | 否     | 所属链编号                   |
| 2    | groupId    | Int    | 否     | 所属群组编号                 |
| 3    | pageSize   | Int    | 否     | 每页记录数                   |
| 4    | pageNumber | Int    | 否     | 当前页码                     |
| 5    | userParam  | String | 是     | 查询参数（用户名或用户地址） |

***2）入参示例***

```
http://127.0.0.1:5005/WeBASE-Chain-Manager/user/userList/100001/1/1/10?userParam=0x3107000a54392e13bccab685b1c3b74151720b7a
```

#### 6.5.3 返回参数 

***1）出参表***

| 序号   | 输出参数    | 类型          |      | 备注                                     |
| ------ | ----------- | ------------- | ---- | ---------------------------------------- |
| 1      | code        | Int           | 否   | 返回码，0：成功 其它：失败               |
| 2      | message     | String        | 否   | 描述                                     |
| 3      | totalCount  | Int           | 否   | 总记录数                                 |
| 4      | data        | List          | 是   | 用户列表                                 |
| 4.1    |             | Object        |      | 用户信息对象                             |
| 4.1.1  | userId      | Int           | 否   | 用户编号                                 |
| 4.1.2  | userName    | String        | 否   | 用户名称                                 |
| 4.1.3  | chainId     | Int           | 否   | 所属链编号                               |
| 4.1.4  | groupId     | Int           | 否   | 所属群组编号                             |
| 4.1.5  | description | String        | 是   | 备注                                     |
| 4.1.6  | userStatus  | Int           | 否   | 状态（1-正常 2-停用，默认1）             |
| 4.1.7  | userType    | Int           | 否   | 用户类型（1-普通用户 2-系统用户，默认1） |
| 4.1.8  | publicKey   | String        | 否   | 公钥信息                                 |
| 4.1.9  | address     | String        | 是   | 用户地址（在链上位置的hash）             |
| 4.1.10 | hasPk       | Int           | 否   | 是否拥有私钥信息(1-拥有，2-不拥有)       |
| 4.1.11 | createTime  | LocalDateTime | 否   | 创建时间                                 |
| 4.1.12 | modifyTime  | LocalDateTime | 否   | 修改时间                                 |

***2）出参示例***

- 成功：

```
{
  "code": 0,
  "message": "success",
  "data": [
    {
      "userId": 700002,
      "userName": "lisi",
      "chainId": 100001,
      "groupId": 1,
      "publicKey": "0xa5e3298e8052fc419658b796755d65c6c86bdb9a051d9cbd7ab0ec67ea97bf008d18d58b812f6fd24e3c4841f96ef8d5d13b55a761e1086815b2b7a2c9f7b33a",
      "userStatus": 1,
      "userType": 1,
      "address": "0x3107000a54392e13bccab685b1c3b74151720b7a",
      "hasPk": 2,
      "description": "test",
      "createTime": "2019-12-31 15:16:56",
      "modifyTime": "2019-12-31 15:16:56"
    }
  ],
  "totalCount": 1
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

## 附录 

### 1. 返回码信息列表

| Code   | message                                          | 描述               |
| ------ | ------------------------------------------------ | ------------------ |
| 0      | success                                          | 正常               |
| 105000 | system error                                     | 系统异常           |
| 205000 | invalid front id                                 | 无效的前置编号     |
| 205001 | database exception                               | 数据库异常         |
| 205002 | not fount any front                              | 找不到前置         |
| 205003 | front already exists                             | 前置已存在         |
| 205004 | group id cannot be empty                         | 群组编号不能为空   |
| 205005 | invalid group id                                 | 无效的群组编号     |
| 205006 | save front fail                                  | 保存前置失败       |
| 205007 | request front fail                               | 请求前置失败       |
| 205008 | abiInfo cannot be empty                          | abi信息不能为空    |
| 205009 | contract already exists                          | 合约已存在         |
| 205010 | invalid contract id                              | 无效的合约编号     |
| 205011 | invalid param info                               | 无效的参数         |
| 205012 | contract name cannot be repeated                 | 合约名称不能重复   |
| 205013 | contract has not deploy                          | 合约未部署         |
| 205014 | invalid contract address                         | 无效的合约地址     |
| 205015 | contract has been deployed                       | 合约已部署         |
| 205016 | contract deploy not success                      | 合约部署不成功     |
| 205017 | wrong host or port                               | 地址或端口错误     |
| 205018 | group id already exists                          | 群组编号已存在     |
| 205019 | node not exists                                  | 节点不存在         |
| 205020 | front's encrypt type not match                   | 前置加密类型不匹配 |
| 205021 | chain name already exists                        | 链名称已经存在     |
| 205022 | save chain fail                                  | 保存链失败         |
| 205023 | invalid chain id                                 | 无效的链编号       |
| 205024 | user already exists                              | 用户已存在         |
| 205025 | publicKey cannot be empty                        | 公钥不能为空       |
| 205026 | publicKey's length is 130,address's length is 42 | 公钥或地址长度不对 |
| 205027 | user id cannot be empty                          | 用户编号不能为空   |
| 205028 | invalid user                                     | 无效用户           |
| 205029 | chain id already exists                          | 链编号已存在       |
| 205030 | contract compile error                           | 合约编译错误       |
| 205031 | group generate fail                              | 群组创建失败       |
| 205032 | group start fail                                 | 群组启动失败       |
| 305000 | param exception                                  | 参数异常           |