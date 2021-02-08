package com.webank.webase.chain.mgr.agency.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 机构拥有的资源，返回参。
 */
@Data
public class RspAllOwnedDataOfAgencyVO {
    private List<Integer> chainIdList;
    private List<OwnedGroup> groupList;
    private List<OwnedFront> frontList;
    private List<OwnedContract> contractList;
    private List<ContractAddedByShelf> contractListAddedByShelf;

    @Data
    @NoArgsConstructor
    public static class OwnedGroup {
        private Integer chainId;
        private Integer groupId;
        private String groupName;
    }

    @Data
    @NoArgsConstructor
    public static class OwnedFront {
        private Integer chainId;
        private Integer frontId;
        private String nodeId;
    }


    /**
     * 当前机构能看到的合约（同一个群组的合约都能看到）
     */
    @Data
    @NoArgsConstructor
    public static class OwnedContract {
        private Integer chainId;
        private Integer groupId;
        private Integer contractId;
        private String contractPath;
        private String contractName;
    }


    /**
     * 由当前机构添加的合约（首次保存）
     */
    @Data
    @NoArgsConstructor
    public static class ContractAddedByShelf {
        private Integer chainId;
        private Integer groupId;
        private Integer contractId;
        private String contractPath;
        private String contractName;
    }
}
