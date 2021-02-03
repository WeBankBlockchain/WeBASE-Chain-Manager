package com.webank.webase.chain.mgr.trans.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ReqSendByContractIdVO {
    @NotNull
    private Integer contractId;
    @NotBlank
    private String signUserId;
    @NotBlank
    private String funcName;
    private List<Object> funcParam;
    private String funcParamJson;
}
