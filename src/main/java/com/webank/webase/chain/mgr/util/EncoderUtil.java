package com.webank.webase.chain.mgr.util;

import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.fisco.bcos.web3j.abi.TypeEncoder;
import org.fisco.bcos.web3j.abi.Utils;
import org.fisco.bcos.web3j.abi.datatypes.*;
import org.fisco.bcos.web3j.crypto.Credentials;
import org.fisco.bcos.web3j.crypto.ExtendedRawTransaction;
import org.fisco.bcos.web3j.crypto.RawTransaction;
import org.fisco.bcos.web3j.crypto.Sign;
import org.fisco.bcos.web3j.crypto.gm.sm3.SM3Digest;
import org.fisco.bcos.web3j.rlp.RlpEncoder;
import org.fisco.bcos.web3j.rlp.RlpList;
import org.fisco.bcos.web3j.rlp.RlpString;
import org.fisco.bcos.web3j.rlp.RlpType;
import org.fisco.bcos.web3j.utils.Bytes;
import org.fisco.bcos.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.fisco.bcos.web3j.abi.datatypes.Type.MAX_BYTE_LENGTH;

/**
 * Create RLP encoded transaction, implementation as per p4 of the <a
 * href="http://gavwood.com/paper.pdf">yellow paper</a>.
 */
public class EncoderUtil {
    
    private static final int ECDSA_TYPE = 0;
    
    private int encryptType = ECDSA_TYPE;

    public EncoderUtil(){}
    
    public EncoderUtil(int encryptType){
        setEncryptType(encryptType);
    }
    
    public int getEncryptType() {
        return encryptType;
    }
    
    public void setEncryptType(int encryptType) {
        this.encryptType = encryptType;
    }

    /** TransactionEncoder */
    
    public byte[] signMessage(RawTransaction rawTransaction, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction);
        Sign.SignatureData signatureData =
                Sign.getSignInterface().signMessage(encodedTransaction, credentials.getEcKeyPair());

        return encode(rawTransaction, signatureData);
    }

    public byte[] signMessage(
            RawTransaction rawTransaction, byte chainId, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData =
                Sign.getSignInterface().signMessage(encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }
    
    public byte[] encode(RawTransaction rawTransaction) {
        return encode(rawTransaction, null);
    }

    public byte[] encode(RawTransaction rawTransaction, byte chainId) {
        Sign.SignatureData signatureData =
                new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    public byte[] encode(RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    private List<RlpType> asRlpValues(
            RawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();
        result.add(RlpString.create(rawTransaction.getRandomid()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));
        result.add(RlpString.create(rawTransaction.getBlockLimit()));
        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        if (signatureData != null) {
            if (this.encryptType == 1) {
                result.add(RlpString.create(signatureData.getPub()));
                // logger.debug("RLP-Pub:{},RLP-PubLen:{}",Hex.toHexString(signatureData.getPub()),signatureData.getPub().length);
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
                // logger.debug("RLP-R:{},RLP-RLen:{}",Hex.toHexString(signatureData.getR()),signatureData.getR().length);
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
                // logger.debug("RLP-S:{},RLP-SLen:{}",Hex.toHexString(signatureData.getS()),signatureData.getS().length);
            } else {
                result.add(RlpString.create(signatureData.getV()));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
            }
        }
        return result;
    }
    
    /** ExtendedTransactionEncoder */
    
    public byte[] signMessage(
            ExtendedRawTransaction rawTransaction, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction);
        Sign.SignatureData signatureData =
                Sign.getSignInterface().signMessage(encodedTransaction, credentials.getEcKeyPair());

        return encode(rawTransaction, signatureData);
    }

    public byte[] signMessage(
            ExtendedRawTransaction rawTransaction, byte chainId, Credentials credentials) {
        byte[] encodedTransaction = encode(rawTransaction, chainId);
        Sign.SignatureData signatureData =
                Sign.getSignInterface().signMessage(encodedTransaction, credentials.getEcKeyPair());

        Sign.SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        return encode(rawTransaction, eip155SignatureData);
    }

    public static Sign.SignatureData createEip155SignatureData(
            Sign.SignatureData signatureData, byte chainId) {
        byte v = (byte) (signatureData.getV() + (chainId << 1) + 8);

        return new Sign.SignatureData(v, signatureData.getR(), signatureData.getS());
    }

    public byte[] encode(ExtendedRawTransaction rawTransaction) {
        return encode(rawTransaction, null);
    }

    public byte[] encode(ExtendedRawTransaction rawTransaction, byte chainId) {
        Sign.SignatureData signatureData =
                new Sign.SignatureData(chainId, new byte[] {}, new byte[] {});
        return encode(rawTransaction, signatureData);
    }

    public byte[] encode(
            ExtendedRawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> values = asRlpValues(rawTransaction, signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    private List<RlpType> asRlpValues(
            ExtendedRawTransaction rawTransaction, Sign.SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();
        result.add(RlpString.create(rawTransaction.getRandomid()));
        result.add(RlpString.create(rawTransaction.getGasPrice()));
        result.add(RlpString.create(rawTransaction.getGasLimit()));
        result.add(RlpString.create(rawTransaction.getBlockLimit()));
        // an empty to address (contract creation) should not be encoded as a numeric 0 value
        String to = rawTransaction.getTo();
        if (to != null && to.length() > 0) {
            // addresses that start with zeros should be encoded with the zeros included, not
            // as numeric values
            result.add(RlpString.create(Numeric.hexStringToByteArray(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(rawTransaction.getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = Numeric.hexStringToByteArray(rawTransaction.getData());
        result.add(RlpString.create(data));

        // add extra data!!!

        result.add(RlpString.create(rawTransaction.getFiscoChainId()));
        result.add(RlpString.create(rawTransaction.getGroupId()));
        if (rawTransaction.getExtraData() == null) {
            result.add(RlpString.create(""));
        } else {
            result.add(
                    RlpString.create(Numeric.hexStringToByteArray(rawTransaction.getExtraData())));
        }
        if (signatureData != null) {
            if (this.encryptType == 1) {
                // Note: shouldn't trimLeadingZeroes here for the Pub must be with the length of 64
                // Bytes
                result.add(RlpString.create(signatureData.getPub()));
                // logger.debug("RLP-Pub:{},RLP-PubLen:{}",Hex.toHexString(signatureData.getPub()),signatureData.getPub().length);
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
                // logger.debug("RLP-R:{},RLP-RLen:{}",Hex.toHexString(signatureData.getR()),signatureData.getR().length);
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
                // logger.debug("RLP-S:{},RLP-SLen:{}",Hex.toHexString(signatureData.getS()),signatureData.getS().length);
            } else {
                result.add(RlpString.create(signatureData.getV()));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
            }
        }
        return result;
    }
    
    /** FunctionEncoder */
    
    public String encode(Function function) {
        List<Type> parameters = function.getInputParameters();

        String methodSignature = buildMethodSignature(function.getName(), parameters);
        String methodId = buildMethodId(methodSignature);

        StringBuilder result = new StringBuilder();
        result.append(methodId);

        return encodeParameters(parameters, result);
    }

    public static String encodeConstructor(List<Type> parameters) {
        return encodeParameters(parameters, new StringBuilder());
    }

    public static String encodeParameters(List<Type> parameters, StringBuilder result) {
        int dynamicDataOffset = Utils.getLength(parameters) * Type.MAX_BYTE_LENGTH;
        StringBuilder dynamicData = new StringBuilder();

        for (Type parameter : parameters) {
            String encodedValue = TypeEncoder.encode(parameter);

            if (parameter.dynamicType()) {
                String encodedDataOffset =
                        encodeNumeric(new Uint(BigInteger.valueOf(dynamicDataOffset)));
                result.append(encodedDataOffset);
                dynamicData.append(encodedValue);
                dynamicDataOffset += (encodedValue.length() >> 1);
            } else {
                result.append(encodedValue);
            }
        }
        result.append(dynamicData);

        return result.toString();
    }

    static String buildMethodSignature(String methodName, List<Type> parameters) {
        StringBuilder result = new StringBuilder();
        result.append(methodName);
        result.append("(");
        String params =
                parameters.stream().map(Type::getTypeAsString).collect(Collectors.joining(","));
        result.append(params);
        result.append(")");
        return result.toString();
    }

    public String buildMethodId(String methodSignature) {
        byte[] input = methodSignature.getBytes();
        byte[] hash = hash(input, 0, input.length);
        return Numeric.toHexString(hash).substring(0, 10);
    }
    
    static String encodeNumeric(NumericType numericType) {
        byte[] rawValue = toByteArray(numericType);
        byte paddingValue = getPaddingValue(numericType);
        byte[] paddedRawValue = new byte[MAX_BYTE_LENGTH];
        if (paddingValue != 0) {
            for (int i = 0; i < paddedRawValue.length; i++) {
                paddedRawValue[i] = paddingValue;
            }
        }

        System.arraycopy(
                rawValue, 0, paddedRawValue, MAX_BYTE_LENGTH - rawValue.length, rawValue.length);
        return Numeric.toHexStringNoPrefix(paddedRawValue);
    }
    
    private static byte[] toByteArray(NumericType numericType) {
        BigInteger value = numericType.getValue();
        if (numericType instanceof Ufixed || numericType instanceof Uint) {
            if (value.bitLength() == Type.MAX_BIT_LENGTH) {
                // As BigInteger is signed, if we have a 256 bit value, the resultant
                // byte array will contain a sign byte in it's MSB, which we should
                // ignore for this unsigned integer type.
                byte[] byteArray = new byte[MAX_BYTE_LENGTH];
                System.arraycopy(value.toByteArray(), 1, byteArray, 0, MAX_BYTE_LENGTH);
                return byteArray;
            }
        }
        return value.toByteArray();
    }
    
    private static byte getPaddingValue(NumericType numericType) {
        if (numericType.getValue().signum() == -1) {
            return (byte) 0xff;
        } else {
            return 0;
        }
    }
    
    private byte[] hash(byte[] input, int offset, int length) {
        if (this.encryptType == ECDSA_TYPE) {
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
}
