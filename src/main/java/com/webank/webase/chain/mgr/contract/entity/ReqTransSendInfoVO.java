package com.webank.webase.chain.mgr.contract.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.util.ArrayList;
import java.util.List;

/**
 * param of send transaction.
 */
@Data
public class ReqTransSendInfoVO {
    @Positive(message = "contractId error")
    private Integer contractId;
    @NotBlank
    private String signUserId;
    @NotBlank
    private String funcName;
    private List<Object> funcParam = new ArrayList<>();
}
