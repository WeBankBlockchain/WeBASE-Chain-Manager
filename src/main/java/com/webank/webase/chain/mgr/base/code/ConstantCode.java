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


    // transaction
    public static final RetCode REQUEST_TRANSACTION_EXCEPTION = RetCode.mark(205101, "request transaction server exception");


    // sign
    public static final RetCode SIGN_USERID_ERROR = RetCode.mark(204201, "signUserId check failed");
    public static final RetCode DATA_SIGN_ERROR = RetCode.mark(204202, "data request sign error");
    public static final RetCode REQUEST_SIGN_EXCEPTION = RetCode.mark(204203, "request sign server exception");

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

    public static final RetCode REQUEST_NODE_EXCEPTION = RetCode.mark(205033, "request node exception");
    // JSON PARSE
    public static final RetCode FAIL_PARSE_JSON = RetCode.mark(205034, "Fail to parse json");
    public static final RetCode CHAIN_ID_NOT_EXISTS = RetCode.mark(205035, "chain id not exists");
    public static final RetCode BUILD_TIME_NOT_MATCH = RetCode.mark(205036, "node's build time not match");

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(205100, "param exception");


    //    public static final RetCode NO_DOCKER_TAG_UPDATE_URL_ERROR = RetCode.mark(205201, "No docker image tag update url.");
//    public static final RetCode UPDATE_DOCKER_TAG_ERROR = RetCode.mark(205202, "Update docker tag from registry error.");
//    public static final RetCode UNKNOWN_CONFIG_TYPE_ERROR = RetCode.mark(205203, "Unknown config type.");
    public static final RetCode SAVE_IP_CONFIG_FILE_ERROR = RetCode.mark(205204, "Save IP config error.");
    //    public static final RetCode TAG_ID_PARAM_ERROR = RetCode.mark(205205, "Tag id param error.");
//    public static final RetCode IP_CONF_PARAM_NULL_ERROR = RetCode.mark(205206, "ipconf null.");
    public static final RetCode CHAIN_NAME_EXISTS_ERROR = RetCode.mark(205207, "Chain name exists.");
    public static final RetCode INSERT_CHAIN_ERROR = RetCode.mark(205208, "Insert new chain failed.");
    //    public static final RetCode NO_CONFIG_FILE_ERROR = RetCode.mark(205209, "No ipconf file.");
    public static final RetCode EXEC_BUILD_CHAIN_ERROR = RetCode.mark(205210, "Exec build chain script failed.");
    //    public static final RetCode IP_CONFIG_LINE_ERROR = RetCode.mark(205211, "ipconf line error.");
//    public static final RetCode IP_NUM_ERROR = RetCode.mark(205212, "IP and num config error.");
    public static final RetCode AGENCY_NAME_CONFIG_ERROR = RetCode.mark(205213, "Agency name config error.");
//    public static final RetCode GROUPS_CONFIG_ERROR = RetCode.mark(205214, "Groups config error.");
    public static final RetCode HOST_CONNECT_ERROR = RetCode.mark(205215, "Connect to host error.");
    //    public static final RetCode INSERT_AGENCY_ERROR = RetCode.mark(205216, "Insert new agency failed.");
//    public static final RetCode INSERT_GROUP_ERROR = RetCode.mark(205217, "Insert new group failed.");
//    public static final RetCode INSERT_HOST_ERROR = RetCode.mark(205218, "Insert new host failed.");
    public static final RetCode INSERT_FRONT_ERROR = RetCode.mark(205219, "Insert new front failed.");
    public static final RetCode INSERT_NODE_ERROR = RetCode.mark(205220, "Insert new node failed.");
    //    public static final RetCode INSERT_FRONT_GROUP_ERROR = RetCode.mark(205221, "Insert new front node group failed.");
    public static final RetCode PARSE_HOST_INDEX_ERROR = RetCode.mark(205222, "Parse host index from node directory failed.");
    //    public static final RetCode HOST_ONLY_BELONGS_ONE_AGENCY_ERROR = RetCode.mark(205223, "A host only belongs to one agency.");
//    public static final RetCode DEPLOY_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(205224, "Unexpected exception occurred when deploy.");
    public static final RetCode UNSUPPORTED_PASSWORD_SSH_ERROR = RetCode.mark(205225, "SSH password login not supported yet.");
    //    public static final RetCode CHAIN_WITH_NO_AGENCY_ERROR = RetCode.mark(205226, "Chain has no agency.");
    public static final RetCode CHAIN_NAME_NOT_EXISTS_ERROR = RetCode.mark(205227, "Chain name not exists.");
    public static final RetCode IP_FORMAT_ERROR = RetCode.mark(205228, "IP format error.");
    //    public static final RetCode AGENCY_NAME_EMPTY_ERROR = RetCode.mark(205229, "Agency name is null when host ip is new.");
//    public static final RetCode AGENCY_NAME_EXISTS_ERROR = RetCode.mark(205230, "Agency name exists when host ip is new.");
    public static final RetCode ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(205231, "Unexpected exception occurred when add new node.");
    public static final RetCode CHAIN_CERT_NOT_EXISTS_ERROR = RetCode.mark(205232, "Chain cert directory not exists.");
        public static final RetCode EXEC_GEN_AGENCY_ERROR = RetCode.mark(205233, "Exec generate agency script failed.");
//    public static final RetCode HOST_WITH_NO_AGENCY_ERROR = RetCode.mark(205234, "Host's agency is null.");
//    public static final RetCode NODES_NUM_ERROR = RetCode.mark(205235, "Num should be positive integer and less then 4.");
    public static final RetCode EXEC_GEN_SDK_ERROR = RetCode.mark(205236, "Exec generate node script to generate sdk dir failed.");
    public static final RetCode EXEC_GEN_NODE_ERROR = RetCode.mark(205237, "Exec generate node script to generate node dir failed.");
    public static final RetCode COPY_SDK_FILES_ERROR = RetCode.mark(205238, "Copy sdk config files error.");
    //    public static final RetCode SEND_SDK_FILES_ERROR = RetCode.mark(205239, "Send sdk config files error.");
//    public static final RetCode SEND_NODE_FILES_ERROR = RetCode.mark(205240, "Send node config files error.");
    public static final RetCode COPY_GROUP_FILES_ERROR = RetCode.mark(205241, "Copy original group config files error.");
        public static final RetCode AGENCY_DIR_EXIST_ERROR = RetCode.mark(205242, "Agency config files already exist error.");
    public static final RetCode DELETE_OLD_SDK_DIR_ERROR = RetCode.mark(205243, "Delete old sdk of host config files error.");
    public static final RetCode DELETE_OLD_NODE_DIR_ERROR = RetCode.mark(205244, "Delete old node config files error.");
    public static final RetCode NODE_ID_NOT_EXISTS_ERROR = RetCode.mark(205245, "Nodeid not exists.");
    //    public static final RetCode STOP_NODE_ERROR = RetCode.mark(205246, "Stop node failed.");
    public static final RetCode START_NODE_ERROR = RetCode.mark(205247, "Start node failed.");
    //    public static final RetCode UPGRADE_WITH_SAME_TAG_ERROR = RetCode.mark(205248, "New image tag and current are the same.");
//    public static final RetCode UPDATE_CHAIN_WITH_NEW_VERSION_ERROR = RetCode.mark(205249, "Update chain version error.");
//    public static final RetCode NODE_IN_GROUP_ERROR = RetCode.mark(205250, "Node still in group, remove before deleting.");
    public static final RetCode READ_NODE_CONFIG_ERROR = RetCode.mark(205251, "Read node config error.");
    public static final RetCode DELETE_NODE_DIR_ERROR = RetCode.mark(205252, "Delete node config files error.");
    public static final RetCode NODE_RUNNING_ERROR = RetCode.mark(205253, "Node is running, cannot be stopped.");
//    public static final RetCode UPDATE_RELATED_NODE_ERROR = RetCode.mark(205254, "Update related nodes error.");
    public static final RetCode DELETE_CHAIN_ERROR = RetCode.mark(205255, "Delete chain error.");
        public static final RetCode NODE_NEED_REMOVE_FROM_GROUP_ERROR = RetCode.mark(205256, "Node is sealer or observer, remove from group first.");
    public static final RetCode LIST_HOST_NODE_DIR_ERROR = RetCode.mark(205257, "List node dirs of host error.");
    public static final RetCode GENERATE_FRONT_YML_ERROR = RetCode.mark(205258, "Generate front application.yml file failed.");
    public static final RetCode EXEC_HOST_INIT_SCRIPT_ERROR = RetCode.mark(205259, "Exec host init script failed.");
    public static final RetCode TRANSFER_FILES_ERROR = RetCode.mark(205260, "Transfer files error.");
    public static final RetCode DOCKER_OPERATION_ERROR = RetCode.mark(205261, "Docker option error.");
    public static final RetCode TWO_NODES_AT_LEAST = RetCode.mark(205262, "Two nodes at least.");
    //    public static final RetCode TWO_SEALER_IN_GROUP_AT_LEAST = RetCode.mark(205263, "Group need two sealers at least.");
//    public static final RetCode WEBASE_SIGN_CONFIG_ERROR = RetCode.mark(205264, "Please check webaseSignAddress in application.yml file.");
    public static final RetCode UNKNOWN_DOCKER_IMAGE_TYPE = RetCode.mark(205265, "Docker image type param error.");
    public static final RetCode IMAGE_NOT_EXISTS_ON_HOST = RetCode.mark(205266, "Image not exists on host.");
    //    public static final RetCode NODES_NUM_EXCEED_MAX_ERROR = RetCode.mark(205267, "Max 4 nodes on a same host.");
//    public static final RetCode SAME_HOST_ERROR = RetCode.mark(205268, "Cannot install node and WeBASE-Node-Manager on same host.");
    public static final RetCode CANNOT_USE_DEFAULT_GROUP_ID = RetCode.mark(205269, "Cannot use default group Id.");
    //    public static final RetCode CANNOT_OPERATE_DEFAULT_GROUP_ID = RetCode.mark(205269, "Cannot use default group Id.");
    public static final RetCode NODE_ID_AND_ORG_LIST_EMPTY = RetCode.mark(205270, "Both node id list and org id list are empty.");
    public static final RetCode DOWNLOAD_FILE_ERROR = RetCode.mark(205271, "Download from url error.");
    public static final RetCode FILE_NOT_EXISTS = RetCode.mark(205272, "Image tar file not exits.");
    public static final RetCode CHECK_PORT_NOT_SUCCESS = RetCode.mark(205273, "port check not success.");
    public static final RetCode HOST_INIT_NOT_SUCCESS = RetCode.mark(205274, "host init not success");
    public static final RetCode IN_FUNCPARAM_ERROR = RetCode.mark(205259, "contract funcParam is error");
    public static final RetCode ABI_PARSE_ERROR = RetCode.mark(205260, "abi parse error");
    public static final RetCode INVALID_ENCRYPT_TYPE = RetCode.mark(205261, "invalid encrypt type");
    public static final RetCode FUNCTION_NOT_EXISTS = RetCode.mark(205262, "function is not exists");
    public static final RetCode FOUND_TOO_MANY_DATA_BY_APP_ID = RetCode.mark(205263, "found too many group by appId");
    public static final RetCode INVALID_APP_ID = RetCode.mark(205264, "invalid appId");
    public static final RetCode REST_REQUEST_FAIL = RetCode.mark(205265, "rest server request not success");
    public static final RetCode GROUP_NAME_EMPTY = RetCode.mark(205266, "group name empty");
    public static final RetCode DUPLICATE_GROUP_NAME = RetCode.mark(205267, "duplicate group name");
    public static final RetCode NOT_FOUND_GROUP_BY_ID_AND_CHAIN = RetCode.mark(205268, "not found group by chain and groupId");
    public static final RetCode GET_LIST_MANAGER_FAIL = RetCode.mark(205269, "get list of manager on chain fail");
    public static final RetCode CRUD_TABLE_KEY_LENGTH_ERROR = RetCode.mark(205270, "table key length error");
    public static final RetCode CRUD_PARSE_CONDITION_ENTRY_FIELD_JSON_ERROR = RetCode.mark(205271, "crud's param parse json error");
    public static final RetCode PRECOMPILED_COMMON_TRANSFER_JSON_FAIL = RetCode.mark(205272, "precompiled common transfer to json fail");
    public static final RetCode TX_RECEIPT_OUTPUT_PARSE_JSON_FAIL = RetCode.mark(205273, "transaction receipt fail and parse output fail");
    public static final RetCode TX_RECEIPT_OUTPUT_NULL = RetCode.mark(205274, "transaction receipt fail and output is null");
    public static final RetCode TX_RECEIPT_CODE_ERROR = RetCode.mark(205275, "transaction receipt status return error");
    public static final RetCode NODE_PARAM_EMPTY = RetCode.mark(205276, "nodeId input is empty");
    public static final RetCode INVALID_NODE_TYPE = RetCode.mark(205277, "invalid node type: sealer, observer, remove ");
    public static final RetCode INVALID_NODE_ID = RetCode.mark(205278, "invalid node id");
    public static final RetCode SET_CONSENSUS_STATUS_FAIL = RetCode.mark(205279, "set consensus status not success");
    public static final RetCode NOT_FOUND_OBSERVER_NODE = RetCode.mark(205280, "not found observer nodes");
    public static final RetCode NODE_IN_TASK = RetCode.mark(205281, "node is already in task");
    public static final RetCode ADD_SEALER_ASYNC_FAIL = RetCode.mark(205282, "add sealer async not success");
    public static final RetCode NOT_FOUND_GENESIS_NODE_LIST_OF_GROUP = RetCode.mark(205283, "not found genesis node list of this group");
    public static final RetCode SOLC_NOT_SUPPORT_OS = RetCode.mark(205284, "The current system does not support contract compilation");
    public static final RetCode FAIL_TO_CREATE_TEMP_FILE = RetCode.mark(205285, "fail to create temp file");
    public static final RetCode NODE_ID_EMPTY = RetCode.mark(205286, "node id is empty");
    public static final RetCode NOT_FOUND_GROUP_BY_AGENCY_AND_CHAIN = RetCode.mark(205287, "not found group by agency and chain");
    public static final RetCode NOT_FOUND_VALID_NODE = RetCode.mark(205288, "not found valid node");
    public static final RetCode NODE_ID_NOT_MATCH = RetCode.mark(205289, "node id not match");
    // v0.9
    public static final RetCode MANUALLY_ADDED_CHAIN_NOT_SUPPORT_ADD_NODE = RetCode.mark(205290, "manually added chain cannot add new nodes");
    public static final RetCode ADD_NEW_NODES_MUST_USING_EXISTED_GROUP_ID = RetCode.mark(205291, "only support add new node in existed group");
    public static final RetCode ONLY_SUPPORT_DELETE_ADDED_NODE_ERROR = RetCode.mark(205292, "only support delete new added error node");
    public static final RetCode ONLY_SUPPORT_STOP_CHAIN_DEPLOY_NODE_ERROR = RetCode.mark(205293, "only support stop node deployed by chain");

}
