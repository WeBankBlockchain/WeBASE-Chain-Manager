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

import com.alibaba.fastjson.JSON;
import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.NodeMgrException;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.chain.entity.ChainParam;
import com.webank.webase.chain.mgr.chain.entity.TbChain;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.frontgroupmap.entity.FrontGroupMapCache;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.scheduler.ResetGroupListTask;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * service of chain.
 */
@Log4j2
@Service
public class ChainService {

    @Autowired
    private ChainMapper chainMapper;
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
    private ResetGroupListTask resetGroupListTask;

    /**
     * add new chain
     */
    public TbChain newChain(ChainInfo chainInfo) {
        log.debug("start newChain chainInfo:{}", chainInfo);
        
        ChainParam param = new ChainParam();
        param.setChainName(chainInfo.getChainName());
        int count = getChainCount(param);
        if (count > 0) {
            throw new NodeMgrException(ConstantCode.CHAIN_EXISTS);
        }
        
        // copy attribute
        TbChain tbChain = new TbChain();
        BeanUtils.copyProperties(chainInfo, tbChain);
        
        // save chain info
        chainMapper.add(tbChain);
        if (tbChain.getChainId() == null || tbChain.getChainId() == 0) {
            log.warn("fail newChain, after save, tbChain:{}", JSON.toJSONString(tbChain));
            throw new NodeMgrException(ConstantCode.SAVE_CHAIN_FAIL);
        }
        return tbChain;
    }

    /**
     * get chain count
     */
    public int getChainCount(ChainParam param) {
        Integer count = chainMapper.getCount(param);
        return count == null ? 0 : count;
    }

    /**
     * get chain list
     */
    public List<TbChain> getChainList(ChainParam param) {
        return chainMapper.getList(param);
    }
    
    /**
     * get chain info
     */
    public TbChain getChainById(Integer chainId) {
        return chainMapper.getChainById(chainId);
    }
    
    /**
     * remove chain
     */
    @Transactional
    public void removeChain(Integer chainId) {
        // check chainId
        ChainParam param = new ChainParam();
        param.setChainId(chainId);
        int count = getChainCount(param);
        if (count == 0) {
            throw new NodeMgrException(ConstantCode.INVALID_CHAIN_ID);
        }

        // remove chain
        chainMapper.remove(chainId);
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
        frontGroupMapCache.clearMapList();
    }
}
