/**
 * Copyright 2014-2020 the original author or authors.
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
package com.webank.webase.chain.mgr.group.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class of table tb_group.
 */
@Data
@NoArgsConstructor
public class TbGroup {

    private Integer groupId;
    private Integer chainId;
    private String groupName;
    private Integer groupStatus;
    private Integer nodeCount;
    private String description;
    private Integer groupType;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    public TbGroup(Integer groupId, Integer chainId, String groupName, Integer nodeCount,
            String description, Integer groupType) {
        this.groupId = groupId;
        this.chainId = chainId;
        this.groupName = groupName;
        this.nodeCount = nodeCount;
        this.description = description;
        this.groupType = groupType;
    }
}
