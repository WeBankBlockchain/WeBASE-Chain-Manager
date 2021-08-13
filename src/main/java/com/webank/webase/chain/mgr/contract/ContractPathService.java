/**
 * Copyright 2014-2021 the original author or authors.
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

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.group.GroupManager;
import com.webank.webase.chain.mgr.repository.bean.TbContractPath;
import com.webank.webase.chain.mgr.repository.mapper.TbContractPathMapper;
import java.util.Date;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * contract's path in IDE
 */
@Log4j2
@Service
public class ContractPathService {
    @Autowired
    private ContractPathManager contractPathManager;
    @Autowired
    private TbContractPathMapper tbContractPathMapper;
    @Autowired
    private GroupManager groupManager;

    /**
     * save not exist path
     * 
     * @param groupId
     * @param pathName
     * @return
     */
    public int save(Integer chainId, Integer groupId, String pathName, boolean ignoreRepeat) {
        log.info("save path chainId:{},groupId;{},pathName:{},ignoreRepeat:{}",
            chainId, groupId, pathName, ignoreRepeat);
        this.groupManager.requireGroupExist(chainId, groupId);
        boolean exist =
                contractPathManager.checkPathExist(chainId, groupId, pathName);
        if (exist) {
            if (ignoreRepeat) {
                return 0;
            } else {
                log.error("save path, path already exists :{}", pathName);
                throw new BaseException(ConstantCode.CONTRACT_PATH_IS_EXISTS);
            }
        }
        TbContractPath contractPath = new TbContractPath();
        contractPath.setContractPath(pathName);
        contractPath.setChainId(chainId);
        contractPath.setGroupId(groupId);
        Date now = new Date();
        contractPath.setCreateTime(now);
        contractPath.setModifyTime(now);
        return tbContractPathMapper.insert(contractPath);
    }

    /**
     * update contract path: update all related contract
     */
//    public int updatePath(Integer pathId, String pathName) {
//        TbContractPath contractPath = new TbContractPath();
//        contractPath.setContractPath(pathName);
//        contractPath.setId(pathId);
//        return tbContractPathMapper.update(contractPath);
//    }

    public int remove(Integer pathId) {
        log.info("remove path id:{}", pathId);
        return tbContractPathMapper.deleteByPrimaryKey(pathId);
    }

    public int removeByPathName(Integer chainId, Integer groupId, String contractPath) {
        log.info("removeByPathName chainId:{},groupId;{},contractPath:{}", chainId, groupId, contractPath);
        return tbContractPathMapper.removeByPathName(chainId, groupId, contractPath);
    }

    public int removeByGroupId(Integer chainId, Integer groupId) {
        log.info("removeByGroupId chainId:{},groupId:{}", chainId, groupId);
        return tbContractPathMapper.removeByGroupId(chainId, groupId);
    }

    public int removeByChainId(Integer chainId) {
        log.info("removeByChainId chainId:{}", chainId);
        return tbContractPathMapper.removeByChainId(chainId);
    }

}
