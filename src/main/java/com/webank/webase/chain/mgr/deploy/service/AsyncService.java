/**
 * Copyright 2014-2020  the original author or authors.
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

package com.webank.webase.chain.mgr.deploy.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AsyncService {

    @Autowired private TbChainMapper tbChainMapper;
    @Autowired private TbFrontMapper tbFrontMapper;

    @Autowired private ConstantProperties constant;
    @Autowired private ChainService chainService;
    @Autowired private FrontService frontService;
    @Autowired private PathService pathService;
    @Autowired private DeployShellService deployShellService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * @param chainName
     */
    @Async("deployAsyncScheduler")
    public void deployChain(String chainName) {
        // scp file to remote host
        Set<Integer> finishHost = new HashSet<>();
        List<TbFront> frontList = this.tbFrontMapper.selectByChainName(chainName);
        for (TbFront front : CollectionUtils.emptyIfNull(frontList)) {

            if (finishHost.contains(front.getExtHostId())){
                continue;
            }
            // scp config files from local to remote
            // local: NODES_ROOT/[chainName]/[ip] TO remote: /opt/fisco/[chainName]
            String src = String.format("%s/*", pathService.getHost(chainName, front.getFrontIp()).toString());
            String dst = PathService.getChainRootOnHost(front.getRootOnHost(), chainName);
            deployShellService.scp(ScpTypeEnum.UP, front.getSshUser(), front.getFrontIp(), front.getSshPort(), src, dst);
            log.info("Send files from:[{}] to:[{}@{}#{}:{}] success.",
                    src, front.getSshUser(), front.getFrontId(), front.getSshPort(), dst);

            finishHost.add(front.getExtHostId());
        }

        //
        this.startChain(chainName,ChainStatusEnum.RUNNING, ChainStatusEnum.DEPLOY_FAILED);
    }

    /**
     *  启动整条链，启动之前应该设置链的状态。
     *
     * @param chainName
     */
    private void startChain(String chainName, ChainStatusEnum successStatus, ChainStatusEnum failedStatus) {
        TbChain chain = this.tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            log.error("No chain:[{}] to deploy.", chainName);
            return;
        }
        // front of chain
        List<TbFront> frontList = this.tbFrontMapper.selectByChainId(chain.getChainId());

        try {
            final CountDownLatch startLatch = new CountDownLatch(CollectionUtils.size(frontList));
            AtomicInteger totalFrontCount = new AtomicInteger(CollectionUtils.size(frontList));
            AtomicInteger startSuccessCount = new AtomicInteger(0);

            for (TbFront front : CollectionUtils.emptyIfNull(frontList)) {
                log.info("Start front:[{}:{}:{}].",front.getFrontIp(),front.getHostIndex(),front.getNodeId());
                try {
                    boolean startResult = this.frontService.restart(chain.getChainId(), front.getNodeId());
                    if (startResult){
                        log.info("Start front:[{}:{}:{}] success.",front.getFrontIp(),front.getHostIndex(),front.getNodeId());
                        startSuccessCount.incrementAndGet();
                    }
                    Thread.sleep(constant.getDockerRestartPeriodTime());
                } catch (Exception e) {
                    log.error("Start front:[{}] error",front.getFrontIp(),front.getHostIndex(), e);
                }finally {
                    startLatch.countDown();
                }
            }

            startLatch.await(constant.getStartNodeTimeout(), TimeUnit.MILLISECONDS);

            boolean startSuccess = startSuccessCount.get() == totalFrontCount.get();
            // check if all host init success
            if (startSuccess){
                log.info( "Start chain:[{}] success,total:[{}]", chainName, startSuccessCount.get());
            }else{
                log.error( "Start chain:[{}] failed, total:[{}], success:[{}]", chainName, totalFrontCount.get(), startSuccessCount.get());
            }

            final int chainId = chain.getChainId();
            final ChainStatusEnum chainStatusEnum = startSuccess ? successStatus : failedStatus;
            threadPoolTaskScheduler.schedule(()->{
                chainService.updateStatus(chainId,chainStatusEnum);
            }, Instant.now().plusMillis( constant.getDockerRestartPeriodTime()));
        } catch (Exception e) {
            log.error("Start chain:[{}] error", chainName, e);
            chainService.updateStatus(chain.getChainId(), failedStatus);
        }
    }

}


