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


    // transaction
    public static final RetCode REQUEST_TRANSACTION_EXCEPTION = RetCode.mark(203201, "request transaction server exception");


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

    /* param exception */
    public static final RetCode PARAM_EXCEPTION = RetCode.mark(305000, "param exception");



    public static final RetCode NO_DOCKER_TAG_UPDATE_URL_ERROR = RetCode.mark(400001, "No docker image tag update url.");
    public static final RetCode UPDATE_DOCKER_TAG_ERROR = RetCode.mark(400002, "Update docker tag from registry error.");
    public static final RetCode UNKNOWN_CONFIG_TYPE_ERROR = RetCode.mark(400003, "Unknown config type.");
    public static final RetCode SAVE_IP_CONFIG_FILE_ERROR = RetCode.mark(400004, "Save IP config error.");
    public static final RetCode TAG_ID_PARAM_ERROR = RetCode.mark(400005, "Tag id param error.");
    public static final RetCode IP_CONF_PARAM_NULL_ERROR = RetCode.mark(400006, "ipconf null.");
    public static final RetCode CHAIN_NAME_EXISTS_ERROR = RetCode.mark(400007, "Chain name exists.");
    public static final RetCode INSERT_CHAIN_ERROR = RetCode.mark(400008, "Insert new chain failed.");
    public static final RetCode NO_CONFIG_FILE_ERROR = RetCode.mark(400009, "No ipconf file.");
    public static final RetCode EXEC_BUILD_CHAIN_ERROR = RetCode.mark(400010, "Exec build chain script failed.");
    public static final RetCode IP_CONFIG_LINE_ERROR = RetCode.mark(400011, "ipconf line error.");
    public static final RetCode IP_NUM_ERROR = RetCode.mark(400012, "IP and num config error.");
    public static final RetCode AGENCY_NAME_CONFIG_ERROR = RetCode.mark(400013, "Agency name config error.");
    public static final RetCode GROUPS_CONFIG_ERROR = RetCode.mark(400014, "Groups config error.");
    public static final RetCode HOST_CONNECT_ERROR = RetCode.mark(400015, "Connect to host error.");
    public static final RetCode INSERT_AGENCY_ERROR = RetCode.mark(400016, "Insert new agency failed.");
    public static final RetCode INSERT_GROUP_ERROR = RetCode.mark(400017, "Insert new group failed.");
    public static final RetCode INSERT_HOST_ERROR = RetCode.mark(400018, "Insert new host failed.");
    public static final RetCode INSERT_FRONT_ERROR = RetCode.mark(400019, "Insert new front failed.");
    public static final RetCode INSERT_NODE_ERROR = RetCode.mark(400020, "Insert new node failed.");
    public static final RetCode INSERT_FRONT_GROUP_ERROR = RetCode.mark(400021, "Insert new front node group failed.");
    public static final RetCode PARSE_HOST_INDEX_ERROR = RetCode.mark(400022, "Parse host index from node directory failed.");
    public static final RetCode HOST_ONLY_BELONGS_ONE_AGENCY_ERROR = RetCode.mark(400023, "A host only belongs to one agency.");
    public static final RetCode DEPLOY_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(400024, "Unexpected exception occurred when deploy.");
    public static final RetCode UNSUPPORTED_PASSWORD_SSH_ERROR = RetCode.mark(400025, "SSH password login not supported yet.");
    public static final RetCode CHAIN_WITH_NO_AGENCY_ERROR = RetCode.mark(400026, "Chain has no agency.");
    public static final RetCode CHAIN_NAME_NOT_EXISTS_ERROR = RetCode.mark(400027, "Chain name not exists.");
    public static final RetCode IP_FORMAT_ERROR = RetCode.mark(400028, "IP format error.");
    public static final RetCode AGENCY_NAME_EMPTY_ERROR = RetCode.mark(400029, "Agency name is null when host ip is new.");
    public static final RetCode AGENCY_NAME_EXISTS_ERROR = RetCode.mark(400030, "Agency name exists when host ip is new.");
    public static final RetCode ADD_NODE_WITH_UNKNOWN_EXCEPTION_ERROR = RetCode.mark(400031, "Unexpected exception occurred when add new node.");
    public static final RetCode CHAIN_CERT_NOT_EXISTS_ERROR = RetCode.mark(400032, "Chain cert directory not exists.");
    public static final RetCode EXEC_GEN_AGENCY_ERROR = RetCode.mark(400033, "Exec generate agency script failed.");
    public static final RetCode HOST_WITH_NO_AGENCY_ERROR = RetCode.mark(400034, "Host's agency is null.");
    public static final RetCode NODES_NUM_ERROR = RetCode.mark(400035, "Num should be positive integer and less then 4.");
    public static final RetCode EXEC_GEN_SDK_ERROR = RetCode.mark(400036, "Exec generate node script to generate sdk dir failed.");
    public static final RetCode EXEC_GEN_NODE_ERROR = RetCode.mark(400037, "Exec generate node script to generate node dir failed.");
    public static final RetCode COPY_SDK_FILES_ERROR = RetCode.mark(400038, "Copy sdk config files error.");
    public static final RetCode SEND_SDK_FILES_ERROR = RetCode.mark(400039, "Send sdk config files error.");
    public static final RetCode SEND_NODE_FILES_ERROR = RetCode.mark(400040, "Send node config files error.");
    public static final RetCode COPY_GROUP_FILES_ERROR = RetCode.mark(400041, "Copy original group config files error.");
    public static final RetCode DELETE_OLD_AGENCY_DIR_ERROR = RetCode.mark(400042, "Delete old agency config files error.");
    public static final RetCode DELETE_OLD_SDK_DIR_ERROR = RetCode.mark(400043, "Delete old sdk of host config files error.");
    public static final RetCode DELETE_OLD_NODE_DIR_ERROR = RetCode.mark(400044, "Delete old node config files error.");
    public static final RetCode NODE_ID_NOT_EXISTS_ERROR = RetCode.mark(400045, "Nodeid not exists.");
    public static final RetCode STOP_NODE_ERROR = RetCode.mark(400046, "Stop node failed.");
    public static final RetCode START_NODE_ERROR = RetCode.mark(400047, "Start node failed.");
    public static final RetCode UPGRADE_WITH_SAME_TAG_ERROR = RetCode.mark(400048, "New image tag and current are the same.");
    public static final RetCode UPDATE_CHAIN_WITH_NEW_VERSION_ERROR = RetCode.mark(400049, "Update chain version error.");
    public static final RetCode NODE_IN_GROUP_ERROR = RetCode.mark(400050, "Node still in group, remove before deleting.");
    public static final RetCode READ_NODE_CONFIG_ERROR = RetCode.mark(400051, "Read node config error.");
    public static final RetCode DELETE_NODE_DIR_ERROR = RetCode.mark(400052, "Delete node config files error.");
    public static final RetCode NODE_RUNNING_ERROR = RetCode.mark(400053, "Node is running.");
    public static final RetCode UPDATE_RELATED_NODE_ERROR = RetCode.mark(400054, "Update related nodes error.");
    public static final RetCode DELETE_CHAIN_ERROR = RetCode.mark(400055, "Delete chain error.");
    public static final RetCode NODE_NEED_REMOVE_FROM_GROUP_ERROR = RetCode.mark(400056, "Node is sealer or observer, remove from group first.");
    public static final RetCode LIST_HOST_NODE_DIR_ERROR = RetCode.mark(400057, "List node dirs of host error.");
    public static final RetCode GENERATE_FRONT_YML_ERROR = RetCode.mark(400058, "Generate front application.yml file failed.");
    public static final RetCode EXEC_HOST_INIT_SCRIPT_ERROR = RetCode.mark(400059, "Exec host init script failed.");
    public static final RetCode TRANSFER_FILES_ERROR = RetCode.mark(400060, "Transfer files error.");
    public static final RetCode DOCKER_OPERATION_ERROR = RetCode.mark(400061, "Docker option error.");
    public static final RetCode TWO_NODES_AT_LEAST = RetCode.mark(400062, "Two nodes at least.");
    public static final RetCode TWO_SEALER_IN_GROUP_AT_LEAST = RetCode.mark(400063, "Group need two sealers at least.");
    public static final RetCode WEBASE_SIGN_CONFIG_ERROR = RetCode.mark(400064, "Please check webaseSignAddress in application.yml file.");
    public static final RetCode UNKNOWN_DOCKER_IMAGE_TYPE = RetCode.mark(400065, "Docker image type param error.");
    public static final RetCode IMAGE_NOT_EXISTS_ON_HOST = RetCode.mark(400066, "Image not exists on host.");
    public static final RetCode NODES_NUM_EXCEED_MAX_ERROR = RetCode.mark(400067, "Max 4 nodes on a same host.");
    public static final RetCode SAME_HOST_ERROR = RetCode.mark(400068, "Cannot install node and WeBASE-Node-Manager on same host.");
    public static final RetCode CANNOT_USE_DEFAULT_GROUP_ID = RetCode.mark(400069, "Cannot use default group Id.");
    public static final RetCode CANNOT_OPERATE_DEFAULT_GROUP_ID = RetCode.mark(400069, "Cannot use default group Id.");
    public static final RetCode NODE_ID_AND_ORG_LIST_EMPTY = RetCode.mark(400070, "Both node id list and org id list are empty.");
    public static final RetCode DOWNLOAD_FILE_ERROR = RetCode.mark(400071, "Download from url error.");
    public static final RetCode FILE_NOT_EXISTS = RetCode.mark(400072, "Image tar file not exits.");
}
