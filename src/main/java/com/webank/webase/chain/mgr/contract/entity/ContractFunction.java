package com.webank.webase.chain.mgr.contract.entity;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.fisco.bcos.sdk.codec.datatypes.Type;
import org.fisco.bcos.sdk.codec.datatypes.TypeReference;

/**
 * abi's function information.
 */
@Data
@Builder
@Accessors(chain = true)
public class ContractFunction {
    String funcName;
    Boolean constant;
    List<String> inputList;
    List<String> outputList;
    List<Type> finalInputs;
    List<TypeReference<?>> finalOutputs;
}
