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
package com.webank.webase.chain.mgr.user.entity;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TbUser {
    private Integer userId;
    private Integer chainId;
    private Integer groupId;
    private String signUserId;
    private String appId;
    private String publicKey;
    private String address;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime modifyTime;

    /**
     * init TbUser.
     */
    public TbUser(Integer chainId, Integer groupId, String signUserId, String appId, String address,
            String publicKey, String description) {
        super();
        this.chainId = chainId;
        this.groupId = groupId;
        this.signUserId = signUserId;
        this.appId = appId;
        this.publicKey = publicKey;
        this.address = address;
        this.description = description;
    }
}
