package com.webank.webase.chain.mgr.deploy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.HostStatusEnum;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.properties.ConstantProperties;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.deploy.req.ReqHostCheckAndInit;
import com.webank.webase.chain.mgr.repository.bean.TbHost;
import com.webank.webase.chain.mgr.repository.mapper.TbHostMapper;
import com.webank.webase.chain.mgr.util.SshTools;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@Component
public class HostService {

    @Autowired private TbHostMapper tbHostMapper;

    @Autowired private ConstantProperties constantProperties;
    @Autowired private DeployShellService deployShellService;

    @Qualifier(value = "deployAsyncScheduler")
    @Autowired private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    /**
     * 1. Check host connectable.
     * 2. Save host to db.
     *
     * @param reqHostCheckAndInit
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public List<TbHost> checkAndSave(ReqHostCheckAndInit reqHostCheckAndInit) {
        List<TbHost> insertedHostList = new ArrayList<>();
        reqHostCheckAndInit.getHostList().forEach((host) -> {
            boolean connect = SshTools.connect(host.getIp(), host.getSshUser(), host.getSshPort(), constantProperties.getPrivateKey());
            if (!connect){
                throw new BaseException(ConstantCode.HOST_CONNECT_ERROR, String.format("SSH connect to %s error.", host.getIp()));
            }

            TbHost inserted = ((HostService) AopContext.currentProxy()).insert(reqHostCheckAndInit.getExtAgencyId(),
                    reqHostCheckAndInit.getExtAgencyName(), host, HostStatusEnum.ADDED);
            insertedHostList.add(inserted);
        });
        return insertedHostList;
    }

    /**
     *
     * @param extAgencyId
     * @param extAgencyName
     * @param host
     * @param hostStatusEnum
     * @return
     * @throws BaseException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public TbHost insert(int extAgencyId, String extAgencyName, ReqHostCheckAndInit.Host host,
                         HostStatusEnum hostStatusEnum) throws BaseException {

        TbHost tbHost = TbHost.init(extAgencyId,extAgencyName,host,hostStatusEnum);

        if ( tbHostMapper.insertSelective(tbHost) != 1 || tbHost.getId() <= 0) {
            throw new BaseException(ConstantCode.INSERT_HOST_ERROR);
        }
        return tbHost;
    }

    /**
     *
     * @param hostId
     * @param newStatus
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateStatus(int hostId, HostStatusEnum newStatus){
        log.info("Change host status  to:[{}]",hostId, newStatus);
        TbHost newHost = new TbHost();
        newHost.setId(hostId);
        newHost.setHostStatus(newStatus.getId());
        newHost.setModifyTime(new Date());
        return tbHostMapper.updateByPrimaryKeySelective(newHost) == 1;
    }

    /**
     *
     * @param hostId
     * @return
     */
    @Transactional
    public TbHost changeHostStatusToInitiating(int hostId ){
        // check chain status
        TbHost host = null;
        synchronized (HostService.class) {
            host = tbHostMapper.selectByPrimaryKey(hostId);
            if (host == null) {
                log.error("Host:[{}] does not exist.", hostId);
                return null;
            }
            // check host status
            if (HostStatusEnum.successOrInitiating(host.getHostStatus())) {
                log.error("Host:[{}:{}] is already init success or is initiating.", host.getIp(), host.getHostStatus());
                return null;
            }

            // update chain status
            log.info("Start to init host:[{}:{}] from status:[{}]", host.getId(), host.getIp(), host.getHostStatus());

            if (!((HostService) AopContext.currentProxy()).updateStatus(host.getId(), HostStatusEnum.INITIATING)) {
                log.error("Start to init host:[{}:{}], but update status to initiating failed.", host.getIp(), host.getHostStatus());
                return null;
            }
        }
        return host;
    }

    /**
     * Init hosts:
     * 1. Install docker and docker-compose;
     *
     * @param tbHostList
     * @return
     */
    @Transactional
    public boolean initHostList(List<TbHost> tbHostList) throws InterruptedException {
        log.info("Start init hosts:[{}].", CollectionUtils.size(tbHostList));

        final CountDownLatch initHostLatch = new CountDownLatch(CollectionUtils.size(tbHostList));
        // check success count
        AtomicInteger initSuccessCount = new AtomicInteger(0);
        for (final TbHost tbHost : tbHostList) {
            log.info("Init host:[{}] by exec shell script:[{}]", tbHost.getIp(), constantProperties.getNodeOperateShell());

            // set host status
            TbHost hostWithStatus = ((HostService) AopContext.currentProxy()).changeHostStatusToInitiating(tbHost.getId());
            if (hostWithStatus == null){
                log.error("Change host:[{}] status to initiating error.", JsonTools.toJSONString(tbHost));
                continue;
            }

            threadPoolTaskScheduler.submit(() -> {
                try {
                    // exec host init shell script
                    deployShellService.execHostOperate(tbHost.getIp(), tbHost.getSshPort(), tbHost.getSshUser(), null);

                    // update host status
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_SUCCESS) ;
                    initSuccessCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("Init host:[{}] error", tbHost.getIp(), e);
                    this.updateStatus(tbHost.getId(), HostStatusEnum.INIT_FAILED);
                } finally {
                    initHostLatch.countDown();
                }
            });
        }

        initHostLatch.await(constantProperties.getExecHostInitTimeout(), TimeUnit.MILLISECONDS);

        boolean initSuccess = initSuccessCount.get() == CollectionUtils.size(tbHostList);
        // check if all host init success
        if (initSuccess){
            log.info("Host init success, total:[{}], success:[{}]", initSuccessCount.get(),initSuccessCount.get());
        }else{
            log.error("Host init ERROR, total:[{}], success:[{}]", initSuccessCount.get(),initSuccessCount.get());
        }
        return initSuccess;
    }

    /**
     *
     * @param extAgencyIdList
     * @return
     */
    public List<TbHost> selectHosByExtAgencyIdList(List<Integer> extAgencyIdList){
        List<TbHost> tbHostList = extAgencyIdList.stream()
                .map((extAgencyId) -> tbHostMapper.selectByExtAgencyId(extAgencyId))
                .filter((host) -> host != null)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        return tbHostList;
    }

}