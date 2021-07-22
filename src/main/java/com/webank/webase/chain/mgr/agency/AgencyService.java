/**
 * Copyright 2014-2019 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.webank.webase.chain.mgr.agency;

import com.webank.webase.chain.mgr.agency.entity.RspAgencyVo;
import com.webank.webase.chain.mgr.agency.entity.RspAllOwnedDataOfAgencyVO;
import com.webank.webase.chain.mgr.util.JsonTools;
import com.webank.webase.chain.mgr.contract.ContractService;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.front.FrontManager;
import com.webank.webase.chain.mgr.front.FrontService;
import com.webank.webase.chain.mgr.frontgroupmap.FrontGroupMapService;
import com.webank.webase.chain.mgr.group.GroupService;
import com.webank.webase.chain.mgr.node.NodeService;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbFront;
import com.webank.webase.chain.mgr.repository.bean.TbGroup;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * service of chain.
 */
@Log4j2
@Service
public class AgencyService {

    /**
     * Is operating chain
     */
    public static AtomicBoolean isDeleting = new AtomicBoolean(false);


    @Autowired
    private FrontService frontService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private GroupService groupService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private FrontGroupMapService frontGroupMapService;
    @Autowired
    private FrontManager frontManager;


    /**
     * @param agencyId
     * @return
     */
    public RspAllOwnedDataOfAgencyVO getAllByAgencyId(int agencyId) {
        log.info("start exec method [getAllByAgencyId]. agencyId:{}", agencyId);

        //query front list
        List<TbFront> frontList = frontManager.listFrontByAgency(agencyId);
        if (CollectionUtils.isEmpty(frontList)) {
            log.info("finish exec method [getAllByAgencyId]. not found front record by agencyId:{}", agencyId);
            return null;
        }
        List<RspAllOwnedDataOfAgencyVO.OwnedFront> ownedFrontList = frontList.stream().map(front -> {
            RspAllOwnedDataOfAgencyVO.OwnedFront ownedFront = new RspAllOwnedDataOfAgencyVO.OwnedFront();
            BeanUtils.copyProperties(front, ownedFront);
            return ownedFront;
        }).collect(Collectors.toList());

        //chainIdList
        List<Integer> chainIdList = frontList.stream().map(front -> front.getChainId()).distinct().collect(Collectors.toList());

        //group list
        List<RspAllOwnedDataOfAgencyVO.OwnedGroup> ownedGroupList = null;
        for (int chainId : CollectionUtils.emptyIfNull(chainIdList)) {
            List<TbGroup> groupList = groupService.listGroupByChainAndAgencyId(chainId, agencyId);
            if (CollectionUtils.isNotEmpty(groupList)) {
                ownedGroupList = groupList.stream().map(group -> {
                    RspAllOwnedDataOfAgencyVO.OwnedGroup ownedGroup = new RspAllOwnedDataOfAgencyVO.OwnedGroup();
                    BeanUtils.copyProperties(group, ownedGroup);
                    return ownedGroup;
                }).collect(Collectors.toList());
            }

        }


        //contract list
        List<RspAllOwnedDataOfAgencyVO.OwnedContract> ownedContractList = new ArrayList<>();
        List<RspAllOwnedDataOfAgencyVO.ContractAddedByShelf> contractListAddedByShelf = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(ownedGroupList)) {
            for (Integer chainId : chainIdList) {
                List<Integer> groupIdList = ownedGroupList.stream()
                        .filter(group -> chainId.equals(group.getChainId()))
                        .map(g -> g.getGroupId())
                        .distinct()
                        .collect(Collectors.toList());
                List<TbContract> contractList = contractService.qureyContractList(new ContractParam(chainId, groupIdList));
                if (CollectionUtils.isEmpty(contractList)) continue;

                //all owned contract
                contractList.stream().distinct().forEach(contract -> {
                    RspAllOwnedDataOfAgencyVO.OwnedContract ownedContract = new RspAllOwnedDataOfAgencyVO.OwnedContract();
                    BeanUtils.copyProperties(contract, ownedContract);
                    ownedContractList.add(ownedContract);
                });

                //all contract save by current org
                contractList.stream().distinct()
                        .filter(c -> Integer.valueOf(agencyId).equals(c.getSaveByAgency()))
                        .forEach(contract -> {
                            RspAllOwnedDataOfAgencyVO.ContractAddedByShelf contractAddedByShelf = new RspAllOwnedDataOfAgencyVO.ContractAddedByShelf();
                            BeanUtils.copyProperties(contract, contractAddedByShelf);
                            contractListAddedByShelf.add(contractAddedByShelf);
                        });

            }
        }
        //result data
        RspAllOwnedDataOfAgencyVO result = new RspAllOwnedDataOfAgencyVO();
        result.setChainIdList(chainIdList);
        result.setFrontList(ownedFrontList);
        result.setGroupList(ownedGroupList);
        result.setContractList(ownedContractList);
        result.setContractListAddedByShelf(contractListAddedByShelf);

        log.info("success exec method [getAllByAgencyId]. agency:{} result:{}", agencyId, JsonTools.objToString(result));
        return result;
    }


    /**
     * @param chainId
     * @param groupId
     * @return
     */
    public List<RspAgencyVo> queryAgencyList(Integer chainId, Integer groupId, List<String> nodeTypes) {
        log.info("start exec method [queryAgencyList]. chainId:{} groupId:{} nodeTypes:{}", chainId, groupId, JsonTools.objToString(nodeTypes));

        //query by chain if group is null
        if (Objects.isNull(groupId))
            return listAgencyByChain(chainId);

        List<String> nodeIdList = null;
        if (CollectionUtils.isNotEmpty(nodeTypes)) {
            //query by group and type
            nodeIdList = nodeTypes.stream()
                    .map(type -> nodeService.getNodeIds(chainId, groupId, type))
                    .flatMap(List::stream)
                    .distinct()
                    .collect(Collectors.toList());
        } else {
            nodeIdList = nodeService.getSealerAndObserverList(chainId, groupId);
        }

        log.info("nodeIdList:{}", JsonTools.objToString(nodeIdList));

        List<RspAgencyVo> agencyList = null;
        if (CollectionUtils.isNotEmpty(nodeIdList))
            agencyList = listAgencyByChainAndNodeIds(chainId, nodeIdList);

        log.info("success exec method [queryAgencyList]. result:{}", JsonTools.objToString(agencyList));
        return agencyList;
    }

    /**
     * @param chainId
     * @return
     */
    public List<RspAgencyVo> listAgencyByChain(int chainId) {
        log.info("start exec method [listAgencyByChainAndNodeIds]. chainId:{}", chainId);

        //query front list
        List<TbFront> frontList = frontManager.listByChain(chainId);

        //agency list
        List<RspAgencyVo> agencyList = listAgencyFromFrontList(frontList);

        log.info("success exec method [listAgencyFromFrontList]. result:{}", JsonTools.objToString(agencyList));
        return agencyList;
    }


    /**
     * @param chainId
     * @return
     */
    public List<RspAgencyVo> listAgencyByChainAndNodeIds(int chainId, List<String> nodeIds) {
        log.info("start exec method [listAgencyByChainAndNodeIds]. chainId:{} nodeIds:{}", chainId, JsonTools.objToString(nodeIds));

        //query front list
        List<TbFront> frontList = frontManager.listByChainAndNodeIds(chainId, nodeIds);

        //agency list
        List<RspAgencyVo> agencyList = listAgencyFromFrontList(frontList);

        log.info("success exec method [listAgencyByChainAndNodeIds]. result:{}", JsonTools.objToString(frontList));
        return agencyList;
    }


    /**
     * @param frontList
     * @return
     */
    private List<RspAgencyVo> listAgencyFromFrontList(List<TbFront> frontList) {
        log.info("start exec method [listAgencyFromFrontList]. frontList:{}", JsonTools.objToString(frontList));

        if (CollectionUtils.isEmpty(frontList))
            return Collections.EMPTY_LIST;

        List<RspAgencyVo> agencyList = frontList.stream()
                .distinct()
                .map(front -> new RspAgencyVo(front.getExtAgencyId(), front.getAgency()))
                .collect(Collectors.toList());
        log.info("success exec method [listAgencyFromFrontList]. result:{}", JsonTools.objToString(agencyList));
        return agencyList;
    }
}
