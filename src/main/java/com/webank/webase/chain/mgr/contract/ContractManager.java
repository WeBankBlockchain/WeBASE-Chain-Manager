package com.webank.webase.chain.mgr.contract;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.base.tools.JsonTools;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbContractExample;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class ContractManager {
    @Autowired
    private TbContractMapper tbContractMapper;

    /**
     * @param contractId
     * @return
     */
    public TbContract verifyContractId(int contractId) {
        log.info("start exec method [verifyContractId]. contractId:{}", contractId);
        TbContract tbContract = tbContractMapper.selectByPrimaryKey(contractId);
        if (Objects.isNull(tbContract)) {
            log.warn("fail exec method [verifyContractId]. not found record by contractId:{}", contractId);
            throw new BaseException(ConstantCode.INVALID_CONTRACT_ID);
        }
        log.info("success exec method [verifyContractId]. result:{}", JsonTools.objToString(tbContract));
        return tbContract;
    }


    /**
     * verify that the contract does not exist.
     */
    public void verifyContractNotExistByName(int chainId, int groupId, String name, String path) {
        TbContract contract = tbContractMapper.getContract(chainId, groupId, name, path);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new BaseException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    public TbContract verifyContractNotDeploy(int chainId, int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(chainId, contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() == contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new BaseException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);
        }
        return contract;
    }

    /**
     * verify that the contract had bean deployed.
     */
    public TbContract verifyContractDeploy(int chainId, int contractId, int groupId) {
        TbContract contract = verifyContractIdExist(chainId, contractId, groupId);
        if (ContractStatus.DEPLOYED.getValue() != contract.getContractStatus()) {
            log.info("contract had bean deployed contractId:{}", contractId);
            throw new BaseException(ConstantCode.CONTRACT_NOT_DEPLOY);
        }
        return contract;
    }

    /**
     * verify that the contractId is exist.
     */
    public TbContract verifyContractIdExist(int chainId, int contractId, int groupId) {
        ContractParam param = new ContractParam(chainId, contractId, groupId);
        TbContract contract = queryContract(param);
        if (Objects.isNull(contract)) {
            log.info("contractId is invalid. contractId:{}", contractId);
            throw new BaseException(ConstantCode.INVALID_CONTRACT_ID);
        }
        return contract;
    }


    /**
     * query contract info.
     */
    public TbContract queryContract(ContractParam queryParam) {
        log.debug("start queryContract. queryParam:{}", JsonTools.toJSONString(queryParam));
        TbContract tbContract = this.tbContractMapper.getByParam(queryParam);
        log.debug("end queryContract. queryParam:{} tbContract:{}", JsonTools.toJSONString(queryParam),
                JsonTools.toJSONString(tbContract));
        return tbContract;
    }


    /**
     * 查询被引用的工具合约列表  TODO 这只是临时方案，因为查出的可能是工具合约,也可能不是工具合约
     *
     * @param chainId
     * @param groupId
     * @return
     */
    public List<TbContract> listToolingContractByChainAndGroup(int chainId, int groupId) {
        log.debug("start listContractByChainAndGroup. chainId:{} groupId:{}", chainId, groupId);
        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andContractAddressIsNull();
        criteria.andContractStatusNotEqualTo(ContractStatus.DEPLOYED.getValue());
        List<TbContract> contractList = this.tbContractMapper.selectByExampleWithBLOBs(example);
        log.debug("end listContractByChainAndGroup.");
        return contractList;
    }

    /**
     * @param contractPath
     * @return
     */
    public List<TbContract> listContractByPath(String contractPath) {
        log.debug("start listContractByPath. contractPath:{}", contractPath);
        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andContractPathEqualTo(contractPath);
        criteria.andContractAddressIsNull();
        criteria.andContractStatusNotEqualTo(ContractStatus.DEPLOYED.getValue());
        List<TbContract> contractList = this.tbContractMapper.selectByExampleWithBLOBs(example);
        log.debug("end listContractByPath.");
        return contractList;
    }

    /**
     * @param chainId
     * @param groupId
     * @param agencyId
     * @return
     */
    public long getCountOfContract(int chainId, Integer groupId, Integer agencyId) {
        log.info("start getCountOfContract. chainId:{} groupId:{} agencyId:{}", chainId, groupId, agencyId);
        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        if (Objects.nonNull(groupId))
            criteria.andGroupIdEqualTo(groupId);
        if (Objects.nonNull(agencyId))
            criteria.andSaveByAgencyEqualTo(agencyId);

        long count = tbContractMapper.countByExample(example);
        log.debug("end getCountOfContract. result:{}", count);
        return count;
    }
}
