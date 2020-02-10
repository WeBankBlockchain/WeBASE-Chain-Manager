/**
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.frontinterface;

import com.webank.webase.chain.mgr.frontinterface.entity.NodeMonitorInfo;
import com.webank.webase.chain.mgr.frontinterface.entity.PerformanceRatio;
import org.fisco.bcos.web3j.protocol.core.methods.response.*;

import java.time.LocalDateTime;
import java.util.List;

public interface FrontInterface {

    //获取节点版本
    NodeVersion getNodeVersion();

    //获取节点的账本（group）列表
    GroupList getGroupList();

    //获取网络连接的节点信息
    Peers getPeers();

    //获取前置中的账本块高和view监控数据
    public NodeMonitorInfo getNodeMonitorInfo(LocalDateTime beginDate, LocalDateTime endDate,
                                              LocalDateTime contrastBeginDate, LocalDateTime contrastEndDate, int gap, int groupId);
    //获取前置中的节点cpu、内存和io监控数据
    public PerformanceRatio getPerformanceRatio(LocalDateTime beginDate, LocalDateTime endDate,
                                                LocalDateTime contrastBeginDate, LocalDateTime contrastEndDate, int gap);
    //生成一个账本（group）
    GenerateGroup generateGroup(int groupId, int timestamp, List<String> nodeList);

    //启动一个账本（group）
    StartGroup startGroup(int groupId);

    //刷新前置
    void refreshFront();
}
