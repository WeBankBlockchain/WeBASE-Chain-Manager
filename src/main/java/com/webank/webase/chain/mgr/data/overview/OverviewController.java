package com.webank.webase.chain.mgr.data.overview;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.entity.BasePageResponse;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.data.transaction.TransactionService;
import com.webank.webase.chain.mgr.data.transaction.entity.TbTransaction;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for processing system overview information.
 */
@Log4j2
@RestController
@RequestMapping("overview")
public class OverviewController {

    @Autowired
    private TransactionService transactionService;

    /**
     * query trans all
     */
    @GetMapping(value = "/transCountAll/{chainId}/{groupId}/{startTime}/{endTime}")
    public BaseResponse queryTransCountAll(@PathVariable("chainId") Integer chainId,
                                           @PathVariable("groupId") Integer groupId,
                                           @PathVariable("startTime") Long startTime,
                                           @PathVariable("endTime") Long endTime) {
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        log.info("start transCountAll.");
        TranxCount tranxCount = new TranxCount();
        tranxCount.setChainId(chainId);
        tranxCount.setGroupId(groupId);
        tranxCount.setStartTime(startTime);
        tranxCount.setEndTime(endTime);
        tranxCount.setTranxCount(transactionService.getTranxCountAll(chainId, groupId, startTime, endTime));
        baseResponse.setData(tranxCount);
        return baseResponse;
    }

    /**
     * query trans all
     */
    @GetMapping(value = "/transListAll/{chainId}/{groupId}/{startTime}/{endTime}")
    public BasePageResponse queryTransListAll(@PathVariable("chainId") Integer chainId,
                                              @PathVariable("groupId") Integer groupId,
                                              @PathVariable("startTime") Long startTime,
                                              @PathVariable("endTime") Long endTime) {
        BasePageResponse pageResponse = new BasePageResponse(ConstantCode.SUCCESS);
        log.info("start queryTransAll.");
        int count = transactionService.getTranxCountAll(chainId, groupId, startTime, endTime);
        if (count > 0) {
            List<TbTransaction> transList = transactionService.getTranxListAll(chainId, groupId, startTime, endTime);
            pageResponse.setData(transList);
            pageResponse.setTotalCount(count);
        }
        return pageResponse;
    }
}