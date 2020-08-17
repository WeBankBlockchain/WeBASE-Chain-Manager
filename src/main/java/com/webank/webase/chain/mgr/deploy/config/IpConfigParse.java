/**
 * Copyright 2014-2020  the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.webank.webase.chain.mgr.deploy.config;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class IpConfigParse {

    private String ip;
    private String sshUser;
    private int sshPort;
    private int dockerPort;
    private int num;
    private String agencyName;
    private Set<Integer> groupIdSet;

    public static IpConfigParse build(ReqDeploy.DeployHost deployHost, int groupId){
        IpConfigParse ipConfigParse = new IpConfigParse();
        ipConfigParse.setIp(deployHost.getIp());
        ipConfigParse.setSshUser(deployHost.getSshUser());
        ipConfigParse.setSshPort(deployHost.getSshPort());
        ipConfigParse.setDockerPort(deployHost.getDockerDemonPort());
        ipConfigParse.setNum(deployHost.getNum());
        ipConfigParse.setAgencyName(deployHost.getExtOrgName());

        HashSet<Integer> groupIdSet = Sets.newHashSet();
        groupIdSet.add(groupId);
        ipConfigParse.setGroupIdSet(groupIdSet);
        return ipConfigParse;
    }
}

