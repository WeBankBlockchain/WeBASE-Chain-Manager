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
package com.webank.webase.chain.mgr.chain;


import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.enums.DockerImageTypeEnum;
import com.webank.webase.chain.mgr.base.enums.EnumService;
import com.webank.webase.chain.mgr.base.enums.OptionType;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.chain.entity.ChainInfo;
import com.webank.webase.chain.mgr.chain.entity.ReqDeleteChainVo;
import com.webank.webase.chain.mgr.deploy.req.ReqAddNode;
import com.webank.webase.chain.mgr.deploy.req.ReqDeploy;
import com.webank.webase.chain.mgr.deploy.resp.RespInitHost;
import com.webank.webase.chain.mgr.deploy.service.DeployService;
import com.webank.webase.chain.mgr.deploy.service.NodeAsyncService;
import com.webank.webase.chain.mgr.repository.bean.TbChain;
import com.webank.webase.chain.mgr.repository.mapper.TbChainMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * chain controller
 */
@Log4j2
@RestController
@RequestMapping("chain")
@Api(value = "chain")
public class ChainController extends BaseController {
    @Autowired
    private TbChainMapper tbChainMapper;
    @Autowired
    private ChainService chainService;

    @Autowired
    private DeployService deployService;
    @Autowired
    private NodeAsyncService nodeAsyncService;
    @Autowired
    private EnumService enumService;

    /**
     * add new chain
     */
    @ApiOperation(value = "添加链")
    @PostMapping("/new")
    public BaseResponse newChain(@RequestBody @Valid ChainInfo chainInfo, BindingResult result) {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start newChain startTime:{} chainInfo:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(chainInfo));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        TbChain tbChain = chainService.newChain(chainInfo);
        baseResponse.setData(tbChain);
        log.info("end newChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * query chain info list.
     */
    @ApiOperation(value = "查询所有链")
    @GetMapping("/all")
    public BasePageResponse queryChainList() {
        BasePageResponse pagesponse = new BasePageResponse(ConstantCode.SUCCESS);
        Instant startTime = Instant.now();
        log.info("start queryChainList startTime:{}", startTime.toEpochMilli());

        // query chain info
        int count = tbChainMapper.countAll();
        pagesponse.setTotalCount(count);
        if (count > 0) {
            List<TbChain> list = tbChainMapper.selectAll();
            pagesponse.setData(list);
        }

        log.info("end queryChainList useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(pagesponse));
        return pagesponse;
    }

    /**
     * delete by chainId
     */
    @ApiOperation(value = "删除链")
    @DeleteMapping("{chainId}")
    public BaseResponse removeChain(@PathVariable("chainId") Integer chainId) {
        Instant startTime = Instant.now();
        log.info("start removeChain startTime:{} chainId:{}", startTime.toEpochMilli(), chainId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // remove
        chainService.removeChain(chainId);

        log.info("end removeChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }


    /**
     * @param param
     * @param result
     * @return
     * @throws BaseException
     */
    @ApiOperation(value = "删除链(post请求)")
    @PostMapping("removeChain")
    public BaseResponse removeChain(@RequestBody @Valid ReqDeleteChainVo param, BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start removeChain startTime:{} param:{}", startTime.toEpochMilli(), JsonTools.objToString(param));

        // remove
        chainService.removeChain(param.getChainId());

        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("end removeChain useTime:{} result:{}",
                Duration.between(startTime, Instant.now()).toMillis(),
                JsonTools.toJSONString(baseResponse));
        return baseResponse;

    }


    /**
     * 1. check local docker image & generate chain config files & init chain db data(include chain, front, node, group db data etc.)
     * 2. init host and start node (init hosts' image & scp config files to host)
     * @param reqDeploy
     * @param result
     * @return
     * @throws BaseException
     */
    @ApiOperation(value = "部署链")
    @PostMapping(value = "deploy")
    public BaseResponse deploy(
            @RequestBody @Valid ReqDeploy reqDeploy,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] deploy chain:[{}] ", startTime, JsonTools.toJSONString(reqDeploy));

        try {
            // check chain name
            if (StringUtils.isBlank(reqDeploy.getChainName())) {
                reqDeploy.setChainName(String.valueOf(reqDeploy.getChainId()));
            }

            // verify dockerImageType
            DockerImageTypeEnum imageTypeEnum = enumService.verifyDockerImageTypeEnumId(reqDeploy.getDockerImageType());

            // generate node config and return shell execution log
            this.deployService.deployChain(reqDeploy, imageTypeEnum);

            // init host and start node (init image/scp config files)
            this.nodeAsyncService.asyncDeployChain(reqDeploy, OptionType.DEPLOY_CHAIN, imageTypeEnum);

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @ApiOperation(value = "新增节点", hidden = true)
    @PostMapping(value = "addNode")
    public BaseResponse addNode(
            @RequestBody @Valid ReqAddNode reqAddNode,
            BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] add node:[{}] ", startTime, JsonTools.toJSONString(reqAddNode));

        try {
            // check chain name
            if (StringUtils.isBlank(reqAddNode.getChainName())) {
                reqAddNode.setChainName(String.valueOf(reqAddNode.getChainId()));
            }

            // verify dockerImageType
            DockerImageTypeEnum imageTypeEnum = enumService.verifyDockerImageTypeEnumId(reqAddNode.getDockerImageType());

            // generate node config and return shell execution log
            this.deployService.addNodes(reqAddNode, imageTypeEnum);

            return new BaseResponse(ConstantCode.SUCCESS);
        } catch (BaseException e) {
            return new BaseResponse(e.getRetCode());
        }
    }

    @ApiOperation(value = "查询单链信息")
    @GetMapping("/get/{chainId}")
    public BaseResponse getChain(@PathVariable("chainId") int chainId)
            throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] get chain:[{}] ", startTime, chainId);

        TbChain chain = this.tbChainMapper.selectByPrimaryKey(chainId);
        if (chain != null) {
            int progress = chainService.progress(chain);
            chain.setProgress(progress);
            return BaseResponse.success(chain);
        }
        return new BaseResponse(ConstantCode.CHAIN_ID_NOT_EXISTS);
    }

    @ApiOperation(value = "查询镜像获取方式")
    @GetMapping("/image/type")
    public BaseResponse getImageType() throws BaseException {

        Instant startTime = Instant.now();
        log.info("Start:[{}] get image type ", startTime);

        return BaseResponse.success(DockerImageTypeEnum.getTypeMap());
    }

    @ApiOperation(value = "节点机器初始化（拉镜像/确认端口未被占用）")
    @PostMapping("/initHostList")
    public BaseResponse initHostList(@RequestBody @Valid ReqDeploy reqDeploy,
                                     BindingResult result) throws BaseException {
        checkBindResult(result);

        Instant startTime = Instant.now();
        log.info("Start:[{}] initHostList, param:[{}] ", startTime, JsonTools.toJSONString(reqDeploy));

        try {
            //verify dockerImageType
            DockerImageTypeEnum imageTypeEnum = enumService.verifyDockerImageTypeEnumId(reqDeploy.getDockerImageType());

            // init host and start node
            List<RespInitHost> list = nodeAsyncService.initHostList(reqDeploy.getDeployHostList(), reqDeploy.getVersion(), imageTypeEnum);

            BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
            baseResponse.setData(list);
            return baseResponse;
        } catch (BaseException e) {
            log.error("fail initHostList with BaseException", e);
            return new BaseResponse(e.getRetCode());
        } catch (Exception e) {
            log.error("fail initHostList with Exception", e);
            return new BaseResponse(ConstantCode.HOST_INIT_NOT_SUCCESS);
        }
    }

    /**
     * delete by chainId
     */
    @ApiOperation(value = "删除新增的节点")
    @DeleteMapping("/node/delete/{chainId}/{nodeId}")
    public BaseResponse deleteNode(@PathVariable("chainId") Integer chainId,
        @PathVariable("nodeId") String nodeId) {
        Instant startTime = Instant.now();
        log.info("start deleteNode startTime:{} chainId:{},nodeId:{}",
            startTime.toEpochMilli(), chainId, nodeId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // delete node
        deployService.deleteNode(chainId, nodeId);

        log.info("end deleteNode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }

    /**
     * delete by chainId
     */
    @ApiOperation(value = "停止新增的节点")
    @DeleteMapping("/node/stop/{chainId}/{nodeId}")
    public BaseResponse stopNewNode(@PathVariable("chainId") Integer chainId,
        @PathVariable("nodeId") String nodeId) {
        Instant startTime = Instant.now();
        log.info("start stopNewNode startTime:{} chainId:{},nodeId:{}",
            startTime.toEpochMilli(), chainId, nodeId);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);

        // delete node
        deployService.stopNode(chainId, nodeId);

        log.info("end stopNewNode useTime:{} result:{}",
            Duration.between(startTime, Instant.now()).toMillis(),
            JsonTools.toJSONString(baseResponse));
        return baseResponse;
    }
}
