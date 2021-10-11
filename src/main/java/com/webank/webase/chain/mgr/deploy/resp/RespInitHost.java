package com.webank.webase.chain.mgr.deploy.resp;

import lombok.Data;

@Data
public class RespInitHost {
    private Integer hostId;
    private boolean success;
    private String errorMessage;

    public RespInitHost(boolean isSuccess) {
        success = isSuccess;
    }
}