/**
 * Copyright 2014-2020 the original author or authors.
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

package com.webank.webase.chain.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum FrontTypeEnum {
    CHAIN_DEPLOY( 0, "部署链的节点"),
    API_NEW( 1, "接口添加的节点"),
    DEPLOY_ADD(2, "动态扩容的节点");
  
    private int id;
    private String description;

    /**
     *
     * @param id
     * @return
     */
    public static FrontTypeEnum getById(int id) {
        for (FrontTypeEnum value : FrontTypeEnum.values()) {
            if (value.id == id) {
                return value;
            }
        }
        return null;
    }

    /**
     * isDeployAdded
     * @param id
     * @return
     */
    public static boolean isDeployAdded(int id){
        return id == DEPLOY_ADD.getId();
    }
}

