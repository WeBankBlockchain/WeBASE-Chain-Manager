package com.webank.webase.chain.mgr.base.enums;

public enum OsNameEnum {
    LINUX("linux"),
    WINDOW("window"),
    MAC("mac");


    private String value;

    OsNameEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }


}
