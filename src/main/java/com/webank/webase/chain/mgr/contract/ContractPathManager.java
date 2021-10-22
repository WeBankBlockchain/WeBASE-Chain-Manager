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

package com.webank.webase.chain.mgr.contract;

import com.webank.webase.chain.mgr.repository.bean.TbContractPath;
import com.webank.webase.chain.mgr.repository.mapper.TbContractPathMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContractPathManager {
    @Autowired
    private TbContractPathMapper tbContractPathMapper;


    public boolean checkPathExist(Integer chainId, Integer groupId, String pathName) {
        TbContractPath contractPath =
            tbContractPathMapper.findOne(chainId, groupId, pathName);
        if (contractPath != null) {
            return true;
        } else {
            return false;
        }
    }
}
