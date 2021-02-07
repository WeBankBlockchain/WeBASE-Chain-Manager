package com.webank.webase.chain.mgr.node.entity;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RspAddSealerAsyncVO {
    private Boolean allSuccessFlag = true;
    private Set<String> sealerNodes = new HashSet<>();
    private Set<String> successNodes = new HashSet<>();
    private Set<String> errorMessages = new HashSet<>();
}
