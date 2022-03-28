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

/**
 * enum of node status in consensus
 */
public enum NodeStatus {
    /**
     * normal status
     */
    NORMAL(1),
    /**
     * abnormal/invalid status
     */
    INVALID(2),
    /**
     * syncing status
     */
    SYNCING(3);

    private int value;

    NodeStatus(Integer dataStatus) {
        this.value = dataStatus;
    }

    public int getValue() {
        return this.value;
    }
}