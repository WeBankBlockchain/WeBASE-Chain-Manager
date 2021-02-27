package com.webank.webase.chain.mgr.node.entity;

import com.webank.webase.chain.mgr.repository.bean.TbNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RspNodeInfoVo extends TbNode {
    private String frontPeerName;
    private Integer agency;
    private String agencyName;
}
