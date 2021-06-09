package com.webank.webase.chain.mgr.group.entity;

import com.webank.webase.chain.mgr.agency.entity.RspAgencyVo;
import com.webank.webase.chain.mgr.node.entity.RspNodeInfoVo;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RspGroupDetailVo extends TbGroup {
    private List<RspAgencyVo> agencyList;
    private List<RspNodeInfoVo> nodeInfoList;
}
