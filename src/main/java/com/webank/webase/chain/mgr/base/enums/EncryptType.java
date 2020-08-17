/**
 *
 */


package com.webank.webase.chain.mgr.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public enum EncryptType {
    ECDSA_TYPE(0,"ECDS"),
    SM2_TYPE(1,"SM2, guomi"),
    ;

    private int type;
    private String description;
}
