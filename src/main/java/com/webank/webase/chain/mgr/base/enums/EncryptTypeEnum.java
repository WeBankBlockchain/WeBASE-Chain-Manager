/**
 *
 */


package com.webank.webase.chain.mgr.base.enums;

import com.webank.webase.chain.mgr.base.code.ConstantCode;
import com.webank.webase.chain.mgr.base.exception.BaseException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum EncryptTypeEnum {
    ECDSA_TYPE(0,"ECDS"),
    SM2_TYPE(1,"SM2, guomi"),
    ;

    private int type;
    private String description;

    /**
     *
     * @param type
     * @return
     */
    public static EncryptTypeEnum getById(int type) {
        for (EncryptTypeEnum value : EncryptTypeEnum.values()) {
            if (value.type == type) {
                return value;
            }
        }
        throw new BaseException(ConstantCode.ENCRYPT_TYPE_NOT_MATCH);
    }

    public static boolean isInclude(int type) {
        boolean include = false;
        for (EncryptTypeEnum e : EncryptTypeEnum.values()) {
            if (e.getType() == type) {
                include = true;
                break;
            }
        }
        return include;
    }
}
