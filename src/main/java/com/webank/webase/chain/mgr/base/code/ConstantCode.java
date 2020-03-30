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
package com.webank.webase.chain.mgr.base.code;

/**
 * A-BB-CCC A:error level. <br/>
 * 1:system exception <br/>
 * 2:business exception <br/>
 * B:project number <br/>
 * WeBASE-Chain-Manager:05 <br/>
 * C: error code <br/>
 */
public class ConstantCode {

    /* return success */
    public static final RetCode SUCCESS = RetCode.mark(0, "success");

    /* system exception */
    public static final RetCode SYSTEM_EXCEPTION = RetCode.mark(105000, "system exception");
    public static final RetCode SYSTEM_EXCEPTION_GET_PRIVATE_KEY_FAIL = RetCode.mark(105001, "system exception: please check front");

    /* Business exception */
    public static final RetCode INVALID_FRONT_ID = RetCode.mark(205000, "invalid front id");

    public static final RetCode DB_EXCEPTION = RetCode.mark(205001, "database exception");

    public static final RetCode FRONT_LIST_NOT_FOUNT = RetCode.mark(205002, "not fount any front");

    public static final RetCode FRONT_EXISTS = RetCode.mark(205003, "front already exists");

    public static final RetCode GROUP_ID_NULL = RetCode.mark(205004, "group id cannot be empty");

    public static final RetCode INVALID_GROUP_ID = RetCode.mark(205005, "invalid group id");

    public static final RetCode SAVE_FRONT_FAIL = RetCode.mark(205006, "save front fail");

    public static final RetCode REQUEST_FRONT_FAIL = RetCode.mark(205007, "request front fail, please check front");

    public static final RetCode CONTRACT_ABI_EMPTY =
            RetCode.mark(205008, "abiInfo cannot be empty");

    public static final RetCode CONTRACT_EXISTS = RetCode.mark(205009, "contract already exists");

    public static final RetCode INVALID_CONTRACT_ID = RetCode.mark(205010, "invalid contract id");

    public static final RetCode INVALID_PARAM_INFO = RetCode.mark(205011, "invalid param info");

    public static final RetCode CONTRACT_NAME_REPEAT =
            RetCode.mark(205012, "contract name cannot be repeated");

    public static final RetCode CONTRACT_NOT_DEPLOY =
            RetCode.mark(205013, "contract has not deploy");

    public static final RetCode CONTRACT_ADDRESS_INVALID =
            RetCode.mark(205014, "invalid contract address");

    public static final RetCode CONTRACT_HAS_BEAN_DEPLOYED =
            RetCode.mark(205015, "contract has been deployed");

    public static final RetCode CONTRACT_DEPLOY_FAIL =
            RetCode.mark(205016, "contract deploy not success");

    public static final RetCode SERVER_CONNECT_FAIL = RetCode.mark(205017, "wrong host or port");

    public static final RetCode GROUP_ID_EXISTS = RetCode.mark(205018, "group id already exists");

    public static final RetCode NODE_NOT_EXISTS = RetCode.mark(205019, "node front not exists");

    public static final RetCode ENCRYPT_TYPE_NOT_MATCH =
            RetCode.mark(205020, "front's encrypt type not match");
    
    public static final RetCode CHAIN_NAME_EXISTS = RetCode.mark(205021, "chain name already exists");
    
    public static final RetCode SAVE_CHAIN_FAIL = RetCode.mark(205022, "save chain fail");
    
    public static final RetCode INVALID_CHAIN_ID = RetCode.mark(205023, "invalid chain id");
    
    public static final RetCode USER_EXISTS = RetCode.mark(205024, "user already exists");
    
    public static final RetCode PUBLICKEY_NULL = RetCode.mark(205025, "publicKey cannot be empty");
    
    public static final RetCode PUBLICKEY_LENGTH_ERROR = RetCode
            .mark(205026, "publicKey's length is 130,address's length is 42");
    
    public static final RetCode USER_ID_NULL = RetCode.mark(205027, "user id cannot be empty");

    public static final RetCode INVALID_USER = RetCode.mark(205028, "invalid user");
    
    public static final RetCode CHAIN_ID_EXISTS = RetCode.mark(205029, "chain id already exists");
    
    public static final RetCode CONTRACT_COMPILE_ERROR = RetCode.mark(205030, "contract compile error");
    
    public static final RetCode GROUP_GENERATE_FAIL = RetCode.mark(205031, "group generate fail");
    
    public static final RetCode GROUP_OPERATE_FAIL = RetCode.mark(205032, "group operate fail");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(305000, "param exception");

}
