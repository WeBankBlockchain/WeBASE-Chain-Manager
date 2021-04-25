/*
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
package com.webank.webase.chain.mgr.base.enums;

/**
 * Enumeration of contract status.
 */
public enum ContractStatus {

    NOTDEPLOYED((byte)1), DEPLOYED((byte)2), DEPLOYMENTFAILED((byte)3), COMPILED((byte)4), COMPILE_FAILED((byte)5);

    private byte value;

    private ContractStatus(byte dataStatus) {
        this.value = dataStatus;
    }

    public byte getValue() {
        return this.value;
    }

    public static ContractStatus getByValue(byte value) {
        for (ContractStatus enumObj : ContractStatus.values()) {
            if (enumObj.value == value) {
                return enumObj;
            }
        }
        return null;
    }
}
