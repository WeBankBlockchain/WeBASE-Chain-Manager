/**
 * Copyright 2014-2019 the original author or authors.
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
package com.webank.webase.chain.mgr.contract.entity;

import com.webank.webase.chain.mgr.base.entity.BaseQueryParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ContractParam extends BaseQueryParam {
    private Integer chainId;
    private Integer groupId;
    private Integer contractId;
    private String contractName;
    private String contractPath;
    private String contractVersion;
    private String contractAddress;
    private Integer contractStatus;
    private Integer contractType;
    private String partOfBytecodeBin;
    private List<Integer> contractIdList;
    private List<Integer> groupIdList;

    /**
     * init by contractId.
     */
    public ContractParam(int chainId, int contractId, int groupId) {
        super();
        super.setChainId(chainId);
        this.contractId = contractId;
        this.groupId = groupId;
    }

    public ContractParam(Integer chainId, List<Integer> groupIds) {
        this.chainId = chainId;
        this.groupIdList = groupIds;
    }

    public ContractParam(String contractName, String contractAddress) {
        this.contractName = contractName;
        this.contractAddress = contractAddress;
    }
}
