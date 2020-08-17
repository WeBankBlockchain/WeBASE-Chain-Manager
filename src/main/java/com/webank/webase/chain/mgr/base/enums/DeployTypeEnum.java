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
public enum DeployTypeEnum {
    MANUALLY((byte)0,"Add manually"),
    API((byte)1,"Deploy by api"),
    ;

    private byte type;
    private String description;

    /**
     *
     * @param type
     * @return
     */
    public static DeployTypeEnum getById(byte type) {
        for (DeployTypeEnum value : DeployTypeEnum.values()) {
            if (value.type == type) {
                return value;
            }
        }
        return DeployTypeEnum.MANUALLY;
    }
}
