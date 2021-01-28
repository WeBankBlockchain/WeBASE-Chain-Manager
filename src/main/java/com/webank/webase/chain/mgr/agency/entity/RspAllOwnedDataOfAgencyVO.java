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

    @Data
    @NoArgsConstructor
    public static class OwnedContract {
        private Integer chainId;
        private Integer groupId;
        private Integer contractId;
        private String contractPath;
        private String contractName;
    }

}
