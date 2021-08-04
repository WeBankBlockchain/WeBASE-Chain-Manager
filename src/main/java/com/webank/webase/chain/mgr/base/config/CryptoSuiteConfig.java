/*
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
package com.webank.webase.chain.mgr.base.config;


import com.webank.webase.chain.mgr.base.enums.EncryptTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * init encoder.
 */
@Data
@Slf4j
@Configuration
@Deprecated
public class CryptoSuiteConfig {

    @Bean
    public Map<Integer, CryptoSuite> cryptoSuiteMapMap() {
        log.info("*****init encoderMap.");
        Map<Integer, CryptoSuite> encoderMap =
                new ConcurrentHashMap<Integer, CryptoSuite>(EncryptTypeEnum.values().length);
        for (EncryptTypeEnum encryptType : EncryptTypeEnum.values()) {
//            EncoderUtil encoderUtil = new EncoderUtil(encryptType.getType());
            CryptoSuite cryptoSuite = new CryptoSuite(encryptType.getType());
            encoderMap.put(encryptType.getType(), cryptoSuite);
        }
        return encoderMap;
    }

}
