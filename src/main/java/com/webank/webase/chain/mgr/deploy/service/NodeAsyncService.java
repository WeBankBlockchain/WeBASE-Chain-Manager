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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import com.webank.webase.chain.mgr.base.enums.ChainStatusEnum;
import com.webank.webase.chain.mgr.base.enums.FrontStatusEnum;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.enums.ScpTypeEnum;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.ChainService;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.deploy.service.docker.DockerOptions;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import com.webank.webase.chain.mgr.repository.mapper.TbFrontMapper;
import com.webank.webase.chain.mgr.util.NetUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NodeAsyncService {

    @Autowired private TbFrontMapper frontMapper;
    @Autowired private TbChainMapper tbChainMapper;

    @Autowired private FrontService frontService;
    @Autowired private ChainService chainService;
    @Autowired private ConstantProperties constant;
    @Autowired private PathService pathService;
    @Autowired private DeployShellService deployShellService;
    @Autowired private DockerOptions dockerOptions;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * @param deploy
     * @param optionType
     */
    @Async("deployAsyncScheduler")
    public void asyncDeployChain(ReqDeploy deploy, OptionType optionType) {
        String chainName = deploy.getChainName();
        TbChain chain = this.tbChainMapper.getByChainName(chainName);
        if (chain == null) {
            log.error("No chain:[{}] to deploy.", chainName);
            return;
        }
        try {
            // check chain status
            if (ChainStatusEnum.successOrDeploying(chain.getChainStatus())) {
                log.error("Chain:[{}] is running or deploying", chainName);
                return;
            }
            boolean success = this.chainService.updateStatus(chain.getChainId(), ChainStatusEnum.DEPLOYING, null);
            if (!success) {
                log.error("Update chain:[{}] status to DEPLOYING failed when deploying.", chainName);
                return;
            }

            // init host
            // 1. install docker and docker-compose,
            // 3. docker pull image
            boolean deploySuccess = this.initHostList(chain, deploy.getDeployHostList(), true);
            if (deploySuccess) {
                // start chain
                this.asyncStartChain(chain.getChainId(), optionType, ChainStatusEnum.RUNNING, ChainStatusEnum.DEPLOY_FAILED,
                        FrontStatusEnum.INITIALIZED, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);

                return;
            } else {
                log.error("Init host list failed:[{}]", chainName);
                chainService.updateStatus(chain.getChainId(), ChainStatusEnum.DEPLOY_FAILED, "Deploy failed by unknown error.");
            }
        } catch (Exception e) {
            log.error("Init host list and start chain:[{}] error", chainName, e);
            chainService.updateStatus(chain.getChainId(), ChainStatusEnum.DEPLOY_FAILED, String.format("Deploy failed with exception: %s.", e.getMessage()));
        }
    }

    /**
     * @param chainId
     */
    @Async("deployAsyncScheduler")
    public void asyncStartChain(int chainId, OptionType optionType, ChainStatusEnum success, ChainStatusEnum failed,
                                FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess, FrontStatusEnum frontFailed) {
        final boolean startSuccess = this.restartChain(chainId, optionType, frontBefore, frontSuccess, frontFailed);
        threadPoolTaskScheduler.schedule(() -> {
            chainService.updateStatus(chainId, startSuccess ? success : failed, null);
        }, Instant.now().plusMillis(1L));
    }


    /**
     * @param chainId
     * @param groupIdSet
     * @param optionType
     */
    @Async("deployAsyncScheduler")
    public void asyncRestartRelatedFront(int chainId, Set<Integer> groupIdSet, OptionType optionType,
                                         FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess, FrontStatusEnum frontFailed) {

        // update chain to updating
        this.chainService.updateStatus(chainId, ChainStatusEnum.RESTARTING, "");

        this.restartFrontOfGroupSet(chainId, groupIdSet, optionType, frontBefore, frontSuccess, frontFailed);

        // update chain to running
        threadPoolTaskScheduler.schedule(() -> {
            this.chainService.updateStatus(chainId, ChainStatusEnum.RUNNING, "");
        }, Instant.now().plusMillis(constant.getDockerRestartPeriodTime()));
    }

    /**
     * @param chain
     * @param host
     * @param group
     * @param optionType
     * @param newFrontList
     */
//    @Async("deployAsyncScheduler")
//    public void asyncAddNode(TbChain chain, TbHost host, TbGroup group, OptionType optionType, List<TbFront> newFrontList) {
//        try {
//            int groupId = group.getGroupId();
//            boolean initSuccess = this.hostService.initHostList(chain, Arrays.asList(host), false);
//            log.info("Init host:[{}], result:[{}]", host.getIp(), initSuccess);
//            if (initSuccess) {
//                // start front and  related front
//                this.asyncRestartRelatedFront(chain.getId(), Collections.singleton(groupId), optionType,
//                        FrontStatusEnum.STARTING, FrontStatusEnum.RUNNING, FrontStatusEnum.STOPPED);
//            } else {
//                newFrontList.forEach((tbFront -> {
//                    this.frontService.updateStatus(tbFront.getFrontId(), FrontStatusEnum.ADD_FAILED);
//                }));
//            }
//        } catch (Exception e) {
//            log.error("Init host:[{}] list and start chain:[{}] error", host.getIp(), chain.getChainName(), e);
//        }
//
//    }

    /**
     * @param chainId
     * @param groupIdSet
     * @param optionType
     */
    private boolean restartFrontOfGroupSet(int chainId, Set<Integer> groupIdSet, OptionType optionType,
                                           FrontStatusEnum frontBefore, FrontStatusEnum frontSuccess, FrontStatusEnum frontFailed) {
        List<TbFront> frontList = this.frontService.selectFrontListByGroupIdSet(chainId, groupIdSet);
        if (CollectionUtils.isEmpty(frontList)) {
            log.info("No front of group id set:[{}]", JsonTools.toJSONString(groupIdSet));
            return false;
        }

        log.info("Restart front of group:[{}]", JsonTools.toJSONString(groupIdSet));

        // group front by host
        Map<String, List<TbFront>> hostFrontListMap = frontList.stream().collect(Collectors.groupingBy(TbFront::getFrontIp));

        // restart front by host
        return this.restartFrontByHost(chainId, optionType, hostFrontListMap, frontBefore, frontSuccess, frontFailed);
    }

    /**
     * @param chainId
     * @param optionType
     * @return
     * @throws InterruptedException
     */
    private boolean restartChain(int chainId, OptionType optionType,
                                 FrontStatusEnum before, FrontStatusEnum success, FrontStatusEnum failed) {
        // host of chain
        List<TbFront> frontList = this.frontMapper.selectByChainId(chainId);

        // group front by host
        Map<String, List<TbFront>> hostFrontListMap = frontList.stream().collect(Collectors.groupingBy(TbFront::getFrontIp));

        // restart by host one by one
        return restartFrontByHost(chainId, optionType, hostFrontListMap, before, success, failed);
    }

    /**
     * @param chainId
     * @param optionType
     * @param hostFrontListMap
     * @return
     * @throws InterruptedException
     */
    private boolean restartFrontByHost(int chainId, OptionType optionType, Map<String, List<TbFront>> hostFrontListMap,
                                       FrontStatusEnum before, FrontStatusEnum success, FrontStatusEnum failed) {
        final CountDownLatch startLatch = new CountDownLatch(CollectionUtils.size(hostFrontListMap));

        final AtomicInteger totalFrontCount = new AtomicInteger(0);
        final AtomicInteger startSuccessCount = new AtomicInteger(0);

        // set maxWaitTime
        final AtomicLong maxWaitTime = new AtomicLong();

        hostFrontListMap.values().stream().forEach(frontList -> {
            // add to total
            totalFrontCount.addAndGet(CollectionUtils.size(frontList));

            // set max wait time
            long estimateTimeOfHost = CollectionUtils.size(frontList) * constant.getDockerRestartPeriodTime();
            if (estimateTimeOfHost > maxWaitTime.get()) {
                maxWaitTime.set(estimateTimeOfHost);
            }
        });
        maxWaitTime.addAndGet(constant.getDockerRestartPeriodTime());

        for (String hostIp : CollectionUtils.emptyIfNull(hostFrontListMap.keySet())) {
            threadPoolTaskScheduler.submit(() -> {
                List<TbFront> frontListToRestart = hostFrontListMap.get(hostIp);
                //
                try {
                    for (TbFront front : CollectionUtils.emptyIfNull(frontListToRestart)) {
                        log.info("Start front:[{}:{}:{}].", front.getFrontIp(), front.getHostIndex(), front.getNodeId());
                        boolean startResult = this.frontService.restart(chainId, front.getNodeId(), optionType, before, success, failed);
                        if (startResult) {
                            log.info("Start front:[{}:{}:{}] success.", front.getFrontIp(), front.getHostIndex(), front.getNodeId());
                            startSuccessCount.incrementAndGet();
                        }
                        Thread.sleep(constant.getDockerRestartPeriodTime());
                    }
                } catch (Exception e) {
                    log.error("Start front on host:[{}] error", hostIp, e);
                } finally {
                    startLatch.countDown();
                }

            });
        }
        boolean startSuccess = false;
        try {
            log.info("Wait:[{}] to restart all fronts.", maxWaitTime.get());
            startLatch.await(maxWaitTime.get(), TimeUnit.MILLISECONDS);
            startSuccess = startSuccessCount.get() == totalFrontCount.get();
        } catch (InterruptedException e) {
            log.error("Start front of chain:[{}] error", chainId, e);
        }

        // check if all host init success
        if (startSuccess) {
            log.info("Front of chain:[{}] start success result, total:[{}], success:[{}]",
            chainId, totalFrontCount.get(), startSuccessCount.get());
        } else {
            log.error("Front of chain:[{}] start failed: total:[{}], success:[{}]",
            chainId, totalFrontCount.get(), startSuccessCount.get());
        }
        return startSuccess;
    }

    /**
     * Init hosts:
     * 1. Send node config to remote hosts;
     * 2. docker pull image;
     *
     * @param tbChain
     * @param hostList
     * @return
     */
    public boolean initHostList(TbChain tbChain, List<ReqDeploy.DeployHost> hostList, boolean scpNodeConfig) throws InterruptedException {
        log.info("Start init chain:[{}:{}] hosts:[{}].", tbChain.getChainId(), tbChain.getChainName(), CollectionUtils.size(hostList));

        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(hostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);
        Map<String, Future> taskMap = new HashedMap<>();

        for (final ReqDeploy.DeployHost host : hostList) {
            Future<?> task = threadPoolTaskScheduler.submit(() -> {
                try {
                    if (scpNodeConfig) {
                        // scp config files from local to remote
                        // local: NODES_ROOT/[chainName]/[ip] TO remote: /opt/fisco/[chainName]
                        String src = String.format("%s/*", pathService.getHost(tbChain.getChainName(), host.getIp()).toString());
                        String dst = PathService.getChainRootOnHost(host.getRootDirOnHost(), tbChain.getChainName());
                        try {
                            deployShellService.scp(ScpTypeEnum.UP, host.getSshUser(), host.getIp(), host.getSshPort(), src, dst);
                            log.info("Send files from:[{}] to:[{}@{}#{}:{}] success.",
                                    src, host.getSshUser(), host.getIp(), host.getSshPort(), dst);
                        } catch (Exception e) {
                            log.error("Send file to host :[{}] failed", host.getIp(), e);
                            this.chainService.updateStatus(tbChain.getChainId(), ChainStatusEnum.DEPLOY_FAILED,
                                    String.format("Scp failed:[%s]", host.getIp()));
                            return;
                        }

                    }

                    // docker pull image
                    try {
                        dockerOptions.pullImage(host.getIp(), host.getDockerDemonPort(), host.getSshUser(), host.getSshPort(), tbChain.getVersion());
                    } catch (Exception e) {
                        log.error("Docker pull image on host :[{}] failed", host.getIp(), e);
                        this.chainService.updateStatus(tbChain.getChainId(), ChainStatusEnum.DEPLOY_FAILED,
                                String.format("Docker pull failed:[%s:%s]", host.getIp(), tbChain.getVersion()));
                        return;
                    }

                    // check port
                    Pair<Boolean, Integer> portReachable = NetUtils.checkPorts(host.getIp(), 2000,
                            constant.getDefaultChannelPort(), constant.getDefaultP2pPort(), constant.getDefaultFrontPort(), constant.getDefaultJsonrpcPort());
                    if (portReachable.getKey()) {
                        log.error("Port:[{}] is in use on host :[{}] failed", portReachable.getValue(), host.getIp());
                        this.chainService.updateStatus(tbChain.getChainId(), ChainStatusEnum.DEPLOY_FAILED,
                                String.format("Port:[%s:%s] in use", host.getIp(), portReachable.getValue()));
                        return;
                    }

                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Init host:[{}] with unknown error", host.getIp(), e);
                } finally {
                    initHostLatch.countDown();
                }
            });
            taskMap.put(host.getIp(), task);
        }

        initHostLatch.await(constant.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);
        taskMap.entrySet().forEach((entry) -> {
            String ip = entry.getKey();
            Future<?> task = entry.getValue();
            if (!task.isDone()) {
                log.error("Init host:[{}] timeout, cancel the task.", ip);
                this.chainService.updateStatus(tbChain.getChainId(), ChainStatusEnum.DEPLOY_FAILED,
                        String.format("Init host timeout:[%s]", ip));
                task.cancel(false);
            }
        });

        boolean hostInitSuccess = initSuccessCount.get() == CollectionUtils.size(hostList);
        // check if all host init success
        if (hostInitSuccess) {
            log.info("Host of chain:[{}] init success: total:[{}], success:[{}]",
                    tbChain.getChainName(), CollectionUtils.size(hostList), initSuccessCount.get());
        } else {
            log.error("Host of chain:[{}] init failed: total:[{}], success:[{}]",
                    tbChain.getChainName(), CollectionUtils.size(hostList), initSuccessCount.get());
        }
        return hostInitSuccess;
    }
}


