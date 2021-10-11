/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.chain.mgr.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsService;

/**
 * Constants and tool function related with Precompiled module
 */
public class PrecompiledUtils {


    public static final String ContractLogFileName = "deploylog.txt";

    // SystemConfig key
    public static final String TxCountLimit = "tx_count_limit";
    public static final String TxGasLimit = "tx_gas_limit";
    // node consensus type
    public static final String NODE_TYPE_SEALER = "sealer";
    public static final String NODE_TYPE_OBSERVER = "observer";
    public static final String NODE_TYPE_REMOVE = "remove";
    // permission manage type
    public static final String PERMISSION_TYPE_PERMISSION = "permission";
    public static final String PERMISSION_TYPE_DEPLOY_AND_CREATE = "deployAndCreate";
    public static final String PERMISSION_TYPE_USERTABLE = "userTable";
    public static final String PERMISSION_TYPE_NODE = "node";
    public static final String PERMISSION_TYPE_SYS_CONFIG = "sysConfig";
    public static final String PERMISSION_TYPE_CNS = "cns";
    // contract manage type
    public static final String CONTRACT_MANAGE_FREEZE = "freeze";
    public static final String CONTRACT_MANAGE_UNFREEZE = "unfreeze";
    public static final String CONTRACT_MANAGE_GETSTATUS = "getStatus";
    public static final String CONTRACT_MANAGE_GRANTMANAGER = "grantManager";
    public static final String CONTRACT_MANAGE_LISTMANAGER = "listManager";

    public static final int InvalidReturnNumber = -100;
    public static final int QueryLogCount = 20;
    public static final int LogMaxCount = 10000;
    public static final String PositiveIntegerRange = "from 1 to 2147483647";
    public static final String NonNegativeIntegerRange = "from 0 to 2147483647";
    public static final String DeployLogntegerRange = "from 1 to 100";
    public static final String NodeIdLength = "128";
    public static final String TxGasLimitRange = "from 100000 to 2147483647";
    public static final String EMPTY_CONTRACT_ADDRESS =
            "0x0000000000000000000000000000000000000000";
    public static final String EMPTY_OUTPUT = "0x";
    public static final int TxGasLimitMin = 10000;

    public static int PermissionCode = 0;
    public static int TableExist = 0;
    public static int PRECOMPILED_SUCCESS = 0; // permission denied
    public static int TABLE_NAME_AND_ADDRESS_ALREADY_EXIST = -51000; // table name and address already exist
    public static int TABLE_NAME_AND_ADDRESS_NOT_EXIST = -51001; // table name and address does not exist
    public static int CRUD_SQL_ERROR = -51503; // process sql error

    public static int SYS_TABLE_KEY_MAX_LENGTH = 58; // 64- "_user_".length
    public static int SYS_TABLE_KEY_FIELD_NAME_MAX_LENGTH = 64;
    public static int SYS_TABLE_VALUE_FIELD_MAX_LENGTH = 1024;
    public static int USER_TABLE_KEY_VALUE_MAX_LENGTH = 255;
    public static int USER_TABLE_FIELD_NAME_MAX_LENGTH = 64;
    public static int USER_TABLE_FIELD_VALUE_MAX_LENGTH = 16 * 1024 * 1024 - 1;


    public static boolean checkNodeId(String nodeId) {
        if (nodeId.length() != 128) {
            return false;
        } else {
            return true;
        }
    }


    public static Map string2Map(String str)
            throws JsonParseException, IOException {
        Map<String, Object> resMap;
        ObjectMapper mapper = new ObjectMapper();
        resMap = mapper.readValue(str, Map.class);
        return resMap;
    }
//
//
//    public static String handleReceiptOutput(String result) {
//        int code = JsonTools.stringToJsonNode(result).get("code").intValue();
//        String msg = "success";
//        if (code == PermissionDenied_RC3) {
//            msg = "permission denied";
//        } else if (code == TableNameAndAddressExist_RC3) {
//            msg = "table name and address already exist";
//        } else if (code == TableNameAndAddressNotExist_RC3) {
//            msg = "table name and address does not exist";
//        } else if (code == LastSealer_RC3) {
//            msg = "the last sealer cannot be removed";
//        } else if (code == TableExist_RC3) {
//            msg = "table already exist";
//        } else if (code == ContractNotExist) {
//            msg = "contract not exist";
//        } else if (code == InvalidKey_RC3) {
//            msg = "invalid configuration entry";
//        } else if (code == InvalidNodeId) {
//            msg = "invalid node ID";
//        } else if (code == P2pNetwork) {
//            msg = "the node is not reachable";
//        } else if (code == GroupPeers) {
//            msg = "the node is not a group peer";
//        } else if (code == SealerList) {
//            msg = "the node is already in the sealer list";
//        } else if (code == ObserverList) {
//            msg = "the node is already in the observer list";
//        } else if (code == ContractNameAndVersionExist) {
//            msg = "contract name and version already exist";
//        } else if (code == VersionExceeds) {
//            msg = "version string length exceeds the maximum limit";
//        } else if (code == TableNameLengthOverflow) {
//            msg = "tablename string length exceeds the maximum limit";
//        } else if (code == InvalidAddress) {
//            msg = "invalid address format";
//        } else if (code == InvalidContractFrozen) {
//            msg = "the contract has been frozen";
//        } else if (code == InvalidContractAvailable) {
//            msg = "the contract is available";
//        } else if (code == InvalidContractRepeatAuthorization) {
//            msg = "the contract has been granted authorization with same user";
//        } else if (code == InvalidContractAddress) {
//            msg = "the contract address is invalid";
//        } else if (code == InvalidTableNotExist) {
//            msg = "the address is not exist";
//        } else if (code == InvalidAuthorized) {
//            msg = "this operation has no permissions";
//        } else if (code == InvalidAccountFrozen) {
//            msg = "the account is frozen";
//        } else if (code == InvalidAccountAlreadyAvailable) {
//            msg = "the account is already available";
//        } else if (code == InvalidCurrentIsExpectedValue) {
//            msg = "the current value is expected";
//        } else if (code == InvalidAccountAddress) {
//            msg = "invalid account address";
//        } else if (code == InvalidAccountNotExist) {
//            msg = "account not exist";
//        } else if (code == InvalidOperatorNotExist) {
//            msg = "operator not exist";
//        } else if (code == InvalidOperatorAlreadyExist) {
//            msg = "operator already exist";
//        } else if (code == InvalidCommitteeMemberCannotBeOperator) {
//            msg = "committee member cannot be operator";
//        } else if (code == InvalidOperatorCannotBeCommitteeMember) {
//            msg = "operator cannot be committee member";
//        } else if (code == InvalidThreshold) {
//            msg = "invalid threshold, threshold should from 0 to 99";
//        } else if (code == InvalidRequestPermissionDeny) {
//            msg = " invalid request with permission deny";
//        } else if (code == InvalidCommitteeMemberNotExist) {
//            msg = "committee member not exist";
//        } else if (code == InvalidCommitteeMemberAlreadyExist) {
//            msg = "committee member already exist";
//        } else if (code == NotSupportPermissionCommand) {
//            msg = "committee permission control by ChainGovernancePrecompiled are recommended";
//        }
//
//        if (code == Success) {
//            // if success, return raw (permissionService needed)
//            return String.valueOf(Success);
//        } else {
//            // else throw exception by code and msg
//            throw new BaseException(code, msg);
//        }
//    }
}
