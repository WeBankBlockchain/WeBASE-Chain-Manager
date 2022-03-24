/**
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
package com.webank.webase.chain.mgr.data.txndaily;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.data.table.TableService;
import com.webank.webase.chain.mgr.data.txndaily.entity.LatestTransCount;
import com.webank.webase.chain.mgr.data.txndaily.entity.TbTxnDaily;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Log4j2
@Service
public class TxnDailyService {

    @Autowired
    private TxnDailyMapper txnDailyMapper;
    @Autowired
    private TableService tableService;

    @Async("asyncExecutor")
    public void statProcess(CountDownLatch latch, String chainId, String groupId) {
        log.debug("start statProcess. chainId:{} groupId:{}", chainId, groupId);
        try {
            // check table
            tableService.newSubTable(chainId, groupId);
            List<LatestTransCount> latestTransCountList =
                    txnDailyMapper.queryLatestTransCount(chainId, groupId);
            if (CollectionUtils.isEmpty(latestTransCountList)) {
                return;
            }
            latestTransCountList.stream()
                    .forEach(ltc -> saveLatestTransCount(ltc, chainId, groupId));
        } catch (Exception ex) {
            log.error("fail statProcess chainId:{} groupId:{} ", chainId, groupId, ex);
        } finally {
            if (Objects.nonNull(latch)) {
                latch.countDown();
            }
        }
        log.debug("end statProcess. chainId:{} groupId:{}", chainId, groupId);
    }

    /**
     * save latest transaction count.
     */
    private void saveLatestTransCount(LatestTransCount latestTransCount, String chainId, String groupId) {
        TbTxnDaily tbTxnDaily = new TbTxnDaily();
        tbTxnDaily.setChainId(chainId);
        tbTxnDaily.setGroupId(groupId);
        BeanUtils.copyProperties(latestTransCount, tbTxnDaily);
        txnDailyMapper.addTransDaily(tbTxnDaily);
    }

    /**
     * query Trading within seven days.
     */
    public List<TbTxnDaily> listSeventDayOfTrans(String chainId, String groupId) throws BaseException {
        try {
            List<TbTxnDaily> transList = txnDailyMapper.listSeventDayOfTransDaily(chainId, groupId);
            return transList;
        } catch (RuntimeException ex) {
            log.error("fail listSeventDayOfTrans groupId:{}", groupId, ex);
            throw new BaseException(ConstantCode.DB_EXCEPTION);
        }
    }
    
    /**
     * delete by chainId.
     */
    public void deleteByChainId(String chainId) {
        if (chainId.isEmpty()) {
            return;
        }
        txnDailyMapper.deleteByChainId(chainId);
    }

    /**
     * delete by groupId.
     */
    public void deleteByGroupId(String chainId, String groupId) {
        if (StringUtils.isBlank(chainId)|| StringUtils.isBlank(groupId)) {
            return;
        }
        txnDailyMapper.deleteByGroupId(chainId, groupId);
    }
}
