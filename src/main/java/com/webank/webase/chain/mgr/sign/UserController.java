package com.webank.webase.chain.mgr.sign;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;
import com.webank.webase.chain.mgr.sign.req.ReqUpdateUserVo;
import com.webank.webase.chain.mgr.sign.rsp.RspUserInfo;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * get user list by app id
     */
    @ApiOperation(value = "get user list by app id", notes = "根据appId获取user列表")
    @GetMapping("/list/{appId}/{pageNumber}/{pageSize}")
    public Object getUserListByAppId(
            @NotBlank @PathVariable("appId") String appId,
            @NotNull @PathVariable("pageNumber") Integer pageNumber,
            @NotNull @PathVariable("pageSize") Integer pageSize) throws BaseException {
        log.info("getUserListByAppId start.");
        return userService.getUserListByAppId(appId, pageNumber, pageSize);
    }

    /**
     * get user list by app id
     */
    @ApiOperation(value = "register a new user in WeBASE-Sign", notes = "注册用户 id")
    @PostMapping("/newUser")
    public BaseResponse newUser(@Valid @RequestBody ReqNewUser reqNewUser, BindingResult result) throws BaseException {
        checkBindResult(result);
        log.info("newUser start.");
        RspUserInfo userInfo = userService.newUser(reqNewUser);
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        baseResponse.setData(userInfo);
        log.debug("newUser success result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }

    /**
     * update user description.
     *
     * @param param
     * @param result
     * @return
     * @throws BaseException
     */
    @ApiOperation(value = "update description of user", notes = "修改用户备注")
    @PatchMapping("/update")
    public BaseResponse updateUser(@Valid @RequestBody ReqUpdateUserVo param, BindingResult result) throws BaseException {
        checkBindResult(result);
        log.info("updateUser start.");
        userService.updateUserDescription(param.getSignUserId(), param.getDescription());
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("updateUser success result:{}", JsonTools.objToString(baseResponse));
        return baseResponse;
    }


    /**
     * @param pageNumber
     * @param pageSize
     * @param appIds
     * @return
     */
    @ApiOperation(value = "query user by page", notes = "分页查询用户列表")
    @GetMapping("/page")
    public BasePageResponse queryUserPage(@RequestParam("pageNumber") Integer pageNumber,
                                          @RequestParam("pageSize") Integer pageSize,
                                          @RequestParam(value = "chainIds", required = false) List<Integer> chainIds,
                                          @RequestParam(value = "appIds", required = false) List<String> appIds) {
        log.info("queryUserPage start. pageNumber：{}  pageSize：{} chainIds:{} appIds：{}", pageNumber, pageSize, JsonTools.objToString(chainIds), JsonTools.objToString(appIds));

        BasePageResponse pageResponse = userService.queryUserPage(pageNumber, pageSize, chainIds, appIds);
        log.info("queryUserPage finish. pageNumber：{}  pageSize：{} chainIds:{} appIds：{} result:{}", pageNumber, pageSize, JsonTools.objToString(chainIds), JsonTools.objToString(appIds), JsonTools.objToString(pageResponse));
        return pageResponse;
    }

    /**
     * get user count
     */
    @ApiOperation(value = "count user in chainmgr", notes = "count user info ")
    @GetMapping("/count")
    public BaseResponse countByChainLocal(@RequestParam(value = "chainId") Integer chainId,
        @RequestParam(value = "groupId") Integer groupId) throws BaseException {

        log.info("countByChainLocal start {}|{}", chainId, groupId);
        long count = userService.countByChainLocal(chainId, groupId);
        return new BaseResponse(ConstantCode.SUCCESS, count);
    }
}