package com.webank.webase.chain.mgr.contract;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.enums.ContractStatus;
import com.webank.webase.chain.mgr.base.exception.BaseException;
import com.webank.webase.chain.mgr.contract.entity.ContractParam;
import com.webank.webase.chain.mgr.repository.bean.TbContract;
import com.webank.webase.chain.mgr.repository.bean.TbContractExample;
import com.webank.webase.chain.mgr.repository.mapper.TbContractMapper;
import com.webank.webase.chain.mgr.util.JsonTools;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public void verifyContractNotExistByName(String chainId, String groupId, String name, String path) {
        TbContract contract = tbContractMapper.getContract(chainId, groupId, name, path);
        if (Objects.nonNull(contract)) {
            log.warn("contract is exist. groupId:{} name:{} path:{}", groupId, name, path);
            throw new BaseException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    public void verifyContractNotExistByName(int contractId, String chainId, String groupId, String name, String path) {
        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andContractNameEqualTo(name);
        criteria.andContractPathEqualTo(path);

        Optional<TbContract> contractOpt = tbContractMapper.getOneByExample(example);
        if (contractOpt.isPresent() && !contractOpt.get().getContractId().equals(contractId)) {
            log.warn("found contract by groupId:{} name:{} path:{} the contractId is:{}, but input contractId:{}", groupId, name, path, contractOpt.get().getContractId(), contractId);
            throw new BaseException(ConstantCode.CONTRACT_EXISTS);
        }
    }

    /**
     * verify that the contract had not deployed.
     */
    public TbContract verifyContractNotDeploy(String chainId, int contractId, String groupId) {
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
    public TbContract verifyContractDeploy(String chainId, int contractId, String groupId) {
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
    public TbContract verifyContractIdExist(String chainId, int contractId, String groupId) {
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
    public List<TbContract> listToolingContractByChainAndGroup(String chainId, String groupId) {
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
//        criteria.andContractAddressIsNull();
//        criteria.andContractStatusNotEqualTo(ContractStatus.DEPLOYED.getValue());
        List<TbContract> contractList = this.tbContractMapper.selectByExampleWithBLOBs(example);
        log.debug("end listContractByPath. contractList size:{}", contractList.size());
        return contractList;
    }

    /**
     * list contract by path in chain group, path
     * @param contractPath
     * @return
     */
    public List<TbContract> listContractByPath(String chainId, String groupId, String contractPath) {
        log.debug("start listContractByPath. chainId:{},groupId:{}contractPath:{}", chainId, groupId, contractPath);
        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andContractPathEqualTo(contractPath);
        List<TbContract> contractList = this.tbContractMapper.selectByExampleWithBLOBs(example);
        log.debug("end listContractByPath. contractList size:{}", contractList.size());
        return contractList;
    }

    /**
     * @param chainId
     * @param groupId
     * @param agencyId
     * @return
     */
    public long getCountOfContract(String chainId, String groupId, Integer agencyId) {
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


    /**
     * delete contract by path.
     *
     * @param chainId
     * @param groupId
     * @param path
     */
    public void deleteByChainAndGroupAndPath(String chainId, String groupId, String path) {
        log.info("start deleteByChainAndGroupAndPath. chainId:{} groupId:{} path:{}", chainId, groupId, path);
        if (chainId.isEmpty() || groupId.isEmpty() || StringUtils.isBlank(path))
            throw new BaseException(ConstantCode.INVALID_PARAM_INFO.attach("require chainId>0 and groupId not empty and path not empty"));

        TbContractExample example = new TbContractExample();
        TbContractExample.Criteria criteria = example.createCriteria();
        criteria.andChainIdEqualTo(chainId);
        criteria.andGroupIdEqualTo(groupId);
        criteria.andContractPathEqualTo(path);

        List<TbContract> contractList = tbContractMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(contractList))
            return;

        long deployCount = contractList.stream().filter(c -> ContractStatus.DEPLOYED.getValue() == c.getContractStatus()).count();
        if (deployCount > 0)
            throw new BaseException(ConstantCode.CONTRACT_HAS_BEAN_DEPLOYED);

        tbContractMapper.deleteByExample(example);
    }
}
