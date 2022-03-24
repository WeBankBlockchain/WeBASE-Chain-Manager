package com.webank.webase.chain.mgr.group.entity;


import lombok.Data;

@Data
public class ReqSetSysConfigInfo {

  private String groupId;
  private String configKey;
  private String configValue;
  private String fromAddress;
  private String signUserId;

}
