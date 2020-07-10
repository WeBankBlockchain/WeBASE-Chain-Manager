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
package com.webank.webase.chain.mgr.chain;

import java.util.Date;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;

import lombok.extern.log4j.Log4j2;

/**
 * service of chain.
 */
@Log4j2
@Service
public class ChainService {

    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private GroupService groupService;
    @Autowired
    private FrontService frontService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private FrontGroupMapCache frontGroupMapCache;
    @Autowired
    @Lazy
    private ResetGroupListTask resetGroupListTask;

    /**
     * add new chain
     */
    public TbChain newChain(ChainInfo chainInfo) {
        log.debug("start newChain chainInfo:{}", chainInfo);

        // check id
        TbChain tbChainInfo = tbChainMapper.selectByPrimaryKey(chainInfo.getChainId());
        if (tbChainInfo != null) {
            throw new BaseException(ConstantCode.CHAIN_ID_EXISTS);
        }

        // check name
        int nameCount = tbChainMapper.countByName(chainInfo.getChainName());
        if (nameCount > 0) {
            throw new BaseException(ConstantCode.CHAIN_NAME_EXISTS);
        }

        // copy attribute
        TbChain tbChain = new TbChain();
        BeanUtils.copyProperties(chainInfo, tbChain);
        Date now = new Date();
        tbChain.setCreateTime(now);
        tbChain.setModifyTime(now);

        // save chain info
        int result = tbChainMapper.insertSelective(tbChain);
        if (result == 0) {
            log.warn("fail newChain, after save, tbChain:{}", JsonTools.toJSONString(tbChain));
            throw new BaseException(ConstantCode.SAVE_CHAIN_FAIL);
        }
        return tbChainMapper.selectByPrimaryKey(tbChain.getChainId());
    }

    /**
     * remove chain
     */
    @Transactional
    public void removeChain(Integer chainId) {
        // check chainId
        int count = tbChainMapper.countByChainId(chainId);
        if (count == 0) {
            throw new BaseException(ConstantCode.INVALID_CHAIN_ID);
        }

        // remove chain
        tbChainMapper.deleteByPrimaryKey(chainId);
        // remove group
        groupService.removeByChainId(chainId);
        // remove front
        frontService.removeByChainId(chainId);
        // remove map
        frontGroupMapService.removeByChainId(chainId);
        // remove node
        nodeService.deleteByChainId(chainId);
        // remove contract
        contractService.deleteContractByChainId(chainId);
        // reset group list
        resetGroupListTask.asyncResetGroupList();
        // clear cache
        frontGroupMapCache.clearMapList(chainId);
    }
}
