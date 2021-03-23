package com.webank.webase.chain.mgr.contract.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class RspContractVO {
    private Integer id;
    private Integer chainId;
    private String chainName;
    private Integer applicationId;
    private String applicationName;
    private String applicationNameZh;
    private String name;
    private Byte chainType;
    private Byte contractType;
    private String address;  //contract of fisco
    private String contractAbi; //contract of fisco
    private Byte status;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date modifyTime;
}
