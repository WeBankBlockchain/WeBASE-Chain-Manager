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
package com.webank.webase.chain.mgr.base.properties;

import com.webank.webase.chain.mgr.util.JsonTools;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.io.File.separator;

/**
 * constants.
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = ConstantProperties.CONSTANT_PREFIX)
public class ConstantProperties {
    public static final BigInteger GAS_PRICE = new BigInteger("100000000");
    public static final BigInteger GAS_LIMIT = new BigInteger("100000000");
    public static final BigInteger INITIAL_WEI_VALUE = new BigInteger("0");
    public static final BigInteger LIMIT_VALUE = new BigInteger("1000");

    // constant
    public static final String CONSTANT_PREFIX = "constant";
    public static final String PREFIX_RESULT_CODE = "0x";
    public static final String SEPARATOR = "SSS";
    public static final String ADMIN_USER_FORMAT = "admin_%s";
    public static final int HTTP_SUCCESS_RESPONSE_CODE = 0;
    public static final String ORDER_BY_CREATE_TIME_FORMAT = "create_time %s";

    //solidity compile
    public static final String DEFAULT_SOLC_VERSION = "0.4.25";
    public static final String SOLC_FILE_PATH = "./solc";
    public static final String SOLIDITY_BASE_PATH = Paths.get("./solidity", "%s").toString();
    public static final String SOLIDITY_FILE_NAME_FORMAT = Paths.get("%1s.sol").toString();
    public static final String BINARY_FILE_SUFFIX = ".bin";
    public static final String RUNTIME_BINARY_FILE_SUFFIX = ".bin-runtime";
    public static final String ABI_FILE_SUFFIX = ".abi";
    public static final String WINDOW_EXEC_FILE_SUFFIX = ".exe";

    private String groupInvalidGrayscaleValue; // y:year, M:month, d:day of month, h:hour, m:minute,
    // n:forever valid
    // front http request
    private String frontUrl;
    private Integer httpTimeOut = 5000;
    private Integer contractDeployTimeOut = 30000;
    private Integer maxRequestFail = 3;
    private Long sleepWhenHttpMaxFail = 60000L; // default 1min

    // about node type
    private BigInteger maxBlockDifferenceOfNewSealer = new BigInteger("5");


    //************************8 add by deploy
    public static final String DEFAULT_GROUP_ID = "group0";

    //exception code of group already exist
    public static int GROUP_ALREADY_EXIST_RETURN_CODE = 201122;

    // timeout config
    private long execShellTimeout = 2 * 60 * 1000L;
    private long dockerRestartPeriodTime = 30 * 1000L;
    private int solidityCompileTimeOut = 10 * 1000; //10 second


    //constant
    private long resetGroupListCycle = 300000L;

    private String webaseSignAddress = "127.0.0.1:5004";
    // data pull
    public static final int MAX_FORK_CERTAINTY_BLOCK_NUMBER = 6;
    public static final int DEPOT_TIME_OUT = 60;
    private long startBlockNumber = 0;
    private int crawlBatchUnit = 100;
    private boolean ifPullData = true; // default true
    // retain max data of block & trans
    /**
     * block into
     */
    private BigInteger blockRetainMax = new BigInteger("5000");

    private BigInteger transRetainMax = new BigInteger("10000");

    private Map<Integer, String> transactionMap = new HashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        log.info("Init constant properties, webaseSignAddress: [{}]", webaseSignAddress);
    }

}