package com.webank.webase.chain.mgr.method;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.webank.webase.chain.mgr.method.entity.Method;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.web3j.abi.EventValues;
import org.fisco.bcos.web3j.abi.FunctionReturnDecoder;
import org.fisco.bcos.web3j.abi.TypeReference;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.Event;
import org.fisco.bcos.web3j.abi.datatypes.Function;
import org.fisco.bcos.web3j.abi.datatypes.Type;
import org.fisco.bcos.web3j.crypto.gm.sm3.SM3Digest;
import org.fisco.bcos.web3j.protocol.ObjectMapperFactory;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition;
import org.fisco.bcos.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import org.fisco.bcos.web3j.protocol.core.methods.response.Log;
import org.fisco.bcos.web3j.protocol.exceptions.TransactionException;
import org.fisco.bcos.web3j.tuples.generated.Tuple2;
import org.fisco.bcos.web3j.tx.txdecode.BaseException;
import org.fisco.bcos.web3j.tx.txdecode.ContractAbiUtil;
import org.fisco.bcos.web3j.tx.txdecode.ContractTypeUtil;
import org.fisco.bcos.web3j.tx.txdecode.DynamicArrayReference;
import org.fisco.bcos.web3j.tx.txdecode.EventResultEntity;
import org.fisco.bcos.web3j.tx.txdecode.InputAndOutputResult;
import org.fisco.bcos.web3j.tx.txdecode.LogResult;
import org.fisco.bcos.web3j.tx.txdecode.ResultEntity;
import org.fisco.bcos.web3j.tx.txdecode.StaticArrayReference;
import org.fisco.bcos.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class TransactionDecoder {

    public static final int ECDSA_TYPE = 0;
    private static final Logger logger = LoggerFactory.getLogger(TransactionDecoder.class);

    private String abi = "";
    private int encryptType = ECDSA_TYPE;
    private Map<String, AbiDefinition> methodIdMap;

    public TransactionDecoder(String abi, int encryptType) {
        this.abi = abi;
        this.encryptType = encryptType;
        methodIdMap = new HashMap<String, AbiDefinition>();
        List<AbiDefinition> funcAbiDefinitionList = ContractAbiUtil.getFuncAbiDefinition(abi);
        for (AbiDefinition abiDefinition : funcAbiDefinitionList) {
            String methodSign = decodeMethodSign(abiDefinition);
            String methodId = buildMethodId(methodSign);
            methodIdMap.put(methodId, abiDefinition);
        }
    }

    private String addHexPrefixToString(String s) {
        if (!s.startsWith("0x")) {
            return "0x" + s;
        }
        return s;
    }

    public List<Method> methodInfo() {
        List<Method> list = new ArrayList<>();
        for (String methodId : methodIdMap.keySet()) {
            AbiDefinition abiDefinition = methodIdMap.get(methodId);
            Method method = new Method();
            method.setMethodId(methodId);
            method.setMethodName(abiDefinition.getName());
            method.setMethodType(abiDefinition.getType());
            list.add(method);
        }
        return list;
    }

    /**
     * @param input
     * @return
     * @throws JsonProcessingException
     * @throws TransactionException
     * @throws BaseException
     */
    public String decodeInputReturnJson(String input)
            throws JsonProcessingException, TransactionException, BaseException {

        // decode input
        InputAndOutputResult inputAndOutputResult = decodeInputReturnObject(input);
        if (inputAndOutputResult == null) {
            return input;
        }
        // format result to json
        String result =
                ObjectMapperFactory.getObjectMapper().writeValueAsString(inputAndOutputResult);

        return result;
    }

    /**
     * @param input
     * @return
     * @throws BaseException
     * @throws TransactionException
     */
    public InputAndOutputResult decodeInputReturnObject(String input)
            throws BaseException, TransactionException {

        String updatedInput = addHexPrefixToString(input);

        // select abi
        AbiDefinition abiDefinition = selectAbiDefinition(updatedInput);
        if (abiDefinition == null) {
            return null;
        }

        // decode input
        List<NamedType> inputTypes = abiDefinition.getInputs();
        List<TypeReference<?>> inputTypeReferences = ContractAbiUtil.paramFormat(inputTypes);
        Function function = new Function(abiDefinition.getName(), null, inputTypeReferences);
        List<Type> resultType = FunctionReturnDecoder.decode(updatedInput.substring(10),
                function.getOutputParameters());

        // set result to java bean
        List<ResultEntity> resultList = new ArrayList<ResultEntity>();
        for (int i = 0; i < inputTypes.size(); i++) {
            resultList.add(new ResultEntity(inputTypes.get(i).getName(),
                    inputTypes.get(i).getType(), resultType.get(i)));
        }
        String methodSign = decodeMethodSign(abiDefinition);

        return new InputAndOutputResult(methodSign, buildMethodId(methodSign), resultList);
    }

    /**
     * @param input
     * @param output
     * @return
     * @throws JsonProcessingException
     * @throws BaseException
     * @throws TransactionException
     */
    public String decodeOutputReturnJson(String input, String output)
            throws JsonProcessingException, BaseException, TransactionException {

        InputAndOutputResult inputAndOutputResult = decodeOutputReturnObject(input, output);
        if (inputAndOutputResult == null) {
            return output;
        }

        String result =
                ObjectMapperFactory.getObjectMapper().writeValueAsString(inputAndOutputResult);
        return result;
    }

    /**
     * @param input
     * @param output
     * @return
     * @throws TransactionException
     * @throws BaseException
     */
    public InputAndOutputResult decodeOutputReturnObject(String input, String output)
            throws TransactionException, BaseException {

        String updatedInput = addHexPrefixToString(input);
        String updatedOutput = addHexPrefixToString(output);

        // select abi
        AbiDefinition abiDefinition = selectAbiDefinition(updatedInput);
        if (abiDefinition == null) {
            return null;
        }

        // decode output
        List<NamedType> outputTypes = abiDefinition.getOutputs();
        List<TypeReference<?>> outputTypeReference = ContractAbiUtil.paramFormat(outputTypes);
        Function function = new Function(abiDefinition.getName(), null, outputTypeReference);
        List<Type> resultType =
                FunctionReturnDecoder.decode(updatedOutput, function.getOutputParameters());

        // set result to java bean
        List<ResultEntity> resultList = new ArrayList<>();
        for (int i = 0; i < outputTypes.size(); i++) {
            resultList.add(new ResultEntity(outputTypes.get(i).getName(),
                    outputTypes.get(i).getType(), resultType.get(i)));
        }
        String methodSign = decodeMethodSign(abiDefinition);

        return new InputAndOutputResult(methodSign, buildMethodId(methodSign), resultList);
    }

    /**
     * @param logs
     * @return
     * @throws BaseException
     * @throws IOException
     */
    public String decodeEventReturnJson(String logs) throws BaseException, IOException {
        // log json trans to list log
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        CollectionType listType =
                mapper.getTypeFactory().constructCollectionType(ArrayList.class, Log.class);
        @SuppressWarnings("unchecked")
        List<Log> logList = (List<Log>) mapper.readValue(logs, listType);
        if (CollectionUtils.isEmpty(logList)) {
            return null;
        }
        // decode event
        Map<String, List<List<EventResultEntity>>> resultEntityMap =
                decodeEventReturnObject(logList);
        String result = mapper.writeValueAsString(resultEntityMap);
        return result;
    }

    /**
     * @param logList
     * @return
     * @throws BaseException
     * @throws IOException
     */
    public String decodeEventReturnJson(List<Log> logList) throws BaseException, IOException {
        if (CollectionUtils.isEmpty(logList)) {
            return null;
        }
        // log json trans to list log
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        // decode event
        Map<String, List<List<EventResultEntity>>> resultEntityMap =
                decodeEventReturnObject(logList);
        String result = mapper.writeValueAsString(resultEntityMap);
        return result;
    }

    /**
     * @param logList
     * @return
     * @throws BaseException
     * @throws IOException
     */
    public Map<String, List<List<EventResultEntity>>> decodeEventReturnObject(List<Log> logList)
            throws BaseException, IOException {
        if (CollectionUtils.isEmpty(logList)) {
            return null;
        }
        // set result to java bean
        Map<String, List<List<EventResultEntity>>> resultEntityMap = new LinkedHashMap<>();

        for (Log log : logList) {
            Tuple2<AbiDefinition, List<EventResultEntity>> resultTuple2 =
                    decodeEventReturnObject(log);
            if (null == resultTuple2) {
                continue;
            }

            AbiDefinition abiDefinition = resultTuple2.getValue1();
            String eventName = decodeMethodSign(abiDefinition);
            if (resultEntityMap.containsKey(eventName)) {
                resultEntityMap.get(eventName).add(resultTuple2.getValue2());
            } else {
                List<List<EventResultEntity>> eventEntityList =
                        new ArrayList<List<EventResultEntity>>();
                eventEntityList.add(resultTuple2.getValue2());
                resultEntityMap.put(eventName, eventEntityList);
            }
        }

        return resultEntityMap;
    }

    /**
     * @param log
     * @return LogResult
     * @throws BaseException
     */
    public LogResult decodeEventLogReturnObject(Log log) throws BaseException {
        // decode log
        List<AbiDefinition> abiDefinitions = ContractAbiUtil.getEventAbiDefinitions(abi);

        LogResult result = new LogResult();

        for (AbiDefinition abiDefinition : abiDefinitions) {

            // String eventName = decodeMethodSign(abiDefinition);
            String eventSignature = buildEventSignature(decodeMethodSign(abiDefinition));

            List<String> topics = log.getTopics();
            if ((null == topics) || topics.isEmpty() || !topics.get(0).equals(eventSignature)) {
                continue;
            }

            EventValues eventValued = decodeEvent(log, abiDefinition);
            if (null != eventValued) {
                List<EventResultEntity> resultEntityList = new ArrayList<EventResultEntity>();
                List<NamedType> inputs = abiDefinition.getInputs();
                List<NamedType> indexedInputs =
                        inputs.stream().filter(NamedType::isIndexed).collect(Collectors.toList());
                List<NamedType> nonIndexedInputs =
                        inputs.stream().filter(p -> !p.isIndexed()).collect(Collectors.toList());

                for (int i = 0; i < indexedInputs.size(); i++) {
                    EventResultEntity eventEntity = new EventResultEntity(
                            indexedInputs.get(i).getName(), indexedInputs.get(i).getType(), true,
                            eventValued.getIndexedValues().get(i));

                    resultEntityList.add(eventEntity);
                }

                for (int i = 0; i < nonIndexedInputs.size(); i++) {
                    EventResultEntity eventEntity = new EventResultEntity(
                            nonIndexedInputs.get(i).getName(), nonIndexedInputs.get(i).getType(),
                            false, eventValued.getNonIndexedValues().get(i));

                    resultEntityList.add(eventEntity);
                }

                // result.setEventName(eventName);
                result.setLogParams(resultEntityList);
                result.setLog(log);

                logger.debug(" event log result: {}", result);

                return result;
            }
        }

        return null;
    }

    public Tuple2<AbiDefinition, List<EventResultEntity>> decodeEventReturnObject(Log log)
            throws BaseException, IOException {

        Tuple2<AbiDefinition, List<EventResultEntity>> result = null;

        // decode log
        List<AbiDefinition> abiDefinitions = ContractAbiUtil.getEventAbiDefinitions(abi);

        for (AbiDefinition abiDefinition : abiDefinitions) {

            String eventSignature = buildEventSignature(decodeMethodSign(abiDefinition));

            List<String> topics = log.getTopics();
            if ((null == topics) || topics.isEmpty() || !topics.get(0).equals(eventSignature)) {
                continue;
            }

            EventValues eventValued = decodeEvent(log, abiDefinition);
            if (null != eventValued) {
                List<EventResultEntity> resultEntityList = new ArrayList<EventResultEntity>();
                List<NamedType> inputs = abiDefinition.getInputs();
                List<NamedType> indexedInputs =
                        inputs.stream().filter(NamedType::isIndexed).collect(Collectors.toList());
                List<NamedType> nonIndexedInputs =
                        inputs.stream().filter(p -> !p.isIndexed()).collect(Collectors.toList());

                for (int i = 0; i < indexedInputs.size(); i++) {
                    EventResultEntity eventEntity = new EventResultEntity(
                            indexedInputs.get(i).getName(), indexedInputs.get(i).getType(), true,
                            eventValued.getIndexedValues().get(i));

                    resultEntityList.add(eventEntity);
                }

                for (int i = 0; i < nonIndexedInputs.size(); i++) {
                    EventResultEntity eventEntity = new EventResultEntity(
                            nonIndexedInputs.get(i).getName(), nonIndexedInputs.get(i).getType(),
                            false, eventValued.getNonIndexedValues().get(i));

                    resultEntityList.add(eventEntity);
                }

                result = new Tuple2<AbiDefinition, List<EventResultEntity>>(abiDefinition,
                        resultEntityList);
                break;
            }
        }

        return result;
    }

    private AbiDefinition selectAbiDefinition(String input) throws TransactionException {
        if (input == null || input.length() < 10) {
            return null;
        }
        String methodId = input.substring(0, 10);
        AbiDefinition abiDefinition = methodIdMap.get(methodId);
        // if (abiDefinition == null) {
        // throw new TransactionException("The method is not included in the contract abi.");
        // }
        return abiDefinition;
    }

    private String decodeMethodSign(AbiDefinition abiDefinition) {
        List<NamedType> inputTypes = abiDefinition.getInputs();
        StringBuilder methodSign = new StringBuilder();
        methodSign.append(abiDefinition.getName());
        methodSign.append("(");
        String params =
                inputTypes.stream().map(NamedType::getType).collect(Collectors.joining(","));
        methodSign.append(params);
        methodSign.append(")");
        return methodSign.toString();
    }

    private <T extends Type> String buildMethodSignature(String methodName,
            List<TypeReference<T>> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        String params =
                parameters.stream().map(p -> Utils.getTypeName(p)).collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }

    private String buildMethodId(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = hash(input, 0, input.length);
        return Numeric.toHexString(hash).substring(0, 10);
    }

    private String buildEventSignature(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = hash(input, 0, input.length);
        return Numeric.toHexString(hash);
    }

    private byte[] hash(byte[] input, int offset, int length) {
        if (encryptType == ECDSA_TYPE) {
            Keccak.DigestKeccak kecc = new Keccak.Digest256();
            kecc.update(input, offset, length);
            return kecc.digest();
        } else {
            byte[] md = new byte[32];
            SM3Digest sm3 = new SM3Digest();
            sm3.update(input, offset, length);
            sm3.doFinal(md, 0);
            String s = new String(Hex.encode(md));
            return md;
        }
    }

    private EventValues decodeEvent(Log log, AbiDefinition abiDefinition) throws BaseException {

        List<TypeReference<?>> finalOutputs = paramFormat(abiDefinition.getInputs());
        Event event = new Event(abiDefinition.getName(), finalOutputs);

        EventValues eventValues = staticExtractEventParameters(event, log);
        return eventValues;
    }

    private List<TypeReference<?>> paramFormat(List<NamedType> paramTypes) throws BaseException {
        List<TypeReference<?>> finalOutputs = new ArrayList<>();

        for (int i = 0; i < paramTypes.size(); i++) {

            AbiDefinition.NamedType.Type type =
                    new AbiDefinition.NamedType.Type(paramTypes.get(i).getType());
            // nested array , not support now.
            if (type.getDepth() > 1) {
                throw new BaseException(201202,
                        String.format("type:%s unsupported array decoding", type.getName()));
            }

            TypeReference<?> typeReference = null;
            if (type.dynamicArray()) {
                typeReference = DynamicArrayReference.create(type.getBaseName(),
                        paramTypes.get(i).isIndexed());
            } else if (type.staticArray()) {
                typeReference = StaticArrayReference.create(type.getBaseName(),
                        type.getDimensions(), paramTypes.get(i).isIndexed());
            } else {
                typeReference =
                        TypeReference.create(ContractTypeUtil.getType(paramTypes.get(i).getType()),
                                paramTypes.get(i).isIndexed());
            }

            finalOutputs.add(typeReference);
        }
        return finalOutputs;
    }

    private EventValues staticExtractEventParameters(Event event, Log log) {

        List<String> topics = log.getTopics();
        String encodedEventSignature = encode(event);
        if (!topics.get(0).equals(encodedEventSignature)) {
            return null;
        }

        List<Type> indexedValues = new ArrayList<>();
        List<Type> nonIndexedValues =
                FunctionReturnDecoder.decode(log.getData(), event.getNonIndexedParameters());

        List<TypeReference<Type>> indexedParameters = event.getIndexedParameters();
        for (int i = 0; i < indexedParameters.size(); i++) {
            Type value = FunctionReturnDecoder.decodeIndexedValue(topics.get(i + 1),
                    indexedParameters.get(i));
            indexedValues.add(value);
        }
        return new EventValues(indexedValues, nonIndexedValues);
    }

    private String encode(Event event) {

        String methodSignature = buildMethodSignature(event.getName(), event.getParameters());

        return buildEventSignature(methodSignature);
    }
}
