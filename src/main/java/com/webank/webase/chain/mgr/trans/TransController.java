package com.webank.webase.chain.mgr.trans;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.controller.BaseController;
import com.webank.webase.chain.mgr.base.entity.BaseResponse;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.trans.entity.ReqSendByContractIdVO;
import java.time.Duration;
import java.time.Instant;
import javax.validation.Valid;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("trans")
public class TransController extends BaseController {

    @Autowired
    private TransService transService;

    /**
     * send transaction.
     */
    @PostMapping(value = "/sendByContractId")
    public BaseResponse sendTransaction(@RequestBody @Valid ReqSendByContractIdVO param,
                                        BindingResult result) throws BaseException {
        checkBindResult(result);
        Instant startTime = Instant.now();
        log.info("start sendTransaction startTime:{} param:{}", startTime.toEpochMilli(),
                JsonTools.toJSONString(param));
        BaseResponse baseResponse = new BaseResponse(ConstantCode.SUCCESS);
        Object transRsp = transService.send(param);
        baseResponse.setData(transRsp);
        log.info("end sendTransaction useTime:{}",
                Duration.between(startTime, Instant.now()).toMillis());

        return baseResponse;
    }
}
