package com.webank.webase.chain.mgr.transaction;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.webank.webase.chain.mgr.base.exception.BaseException;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
@RestController
public class TransactionController {

    @Autowired private TransactionService transactionService;

    /**
     * get user list by app id
     */
    @ApiOperation(value = "get user list by app id", notes = "根据appId获取user列表")
    @GetMapping("/user/list/{chainId}/{appId}/{pageNumber}/{pageSize}")
    public Object getUserListByAppId(
            @NotNull @PathVariable("chainId") Integer chainId,
            @NotBlank @PathVariable("appId") String appId,
            @NotNull @PathVariable("pageNumber") Integer pageNumber,
            @NotNull @PathVariable("pageSize") Integer pageSize) throws BaseException {
        log.info("getUserListByAppId start.");
        return transactionService.getUserListByAppId(chainId,appId, pageNumber, pageSize);
    }


}