package com.webank.webase.chain.mgr.sign;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.sign.req.ReqNewUser;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@RestController
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired private UserService userService;

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
    public Object newUser(@Valid @RequestBody ReqNewUser reqNewUser, BindingResult result) throws BaseException {
        checkBindResult(result);
        log.info("newUser start.");
        return userService.newUser(reqNewUser);
    }

}