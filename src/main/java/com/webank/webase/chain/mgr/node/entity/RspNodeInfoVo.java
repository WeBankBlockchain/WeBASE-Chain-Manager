package com.webank.webase.chain.mgr.node.entity;

import com.webank.webase.chain.mgr.repository.bean.TbNode;
import lombok.Data;

@Data
public class RspNodeInfoVo extends TbNode {
    private String frontPeerName;
}
