package com.webank.webase.chain.mgr.front.entity;

import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RspEntityOfGroupPage extends TbGroup {
    private long nodeCountOfAgency;//查询时入参如果有agency，则返回agency在每每个群组的节点数
}
