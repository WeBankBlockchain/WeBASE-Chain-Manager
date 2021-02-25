package com.webank.webase.chain.mgr.base.enums;

public enum GroupStatusEnum {
    INEXISTENT("inexistent"),
    STOPPING("stopping"),
    RUNNING("running"),
    STOPPED("stopped"),
    DELETED("deleted");


    private String value;

    private GroupStatusEnum(String status) {
        this.value = status;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean isInclude(String key) {
        boolean include = false;
        for (GroupStatusEnum e : GroupStatusEnum.values()) {
            if (e.getValue().equals(key)) {
                include = true;
                break;
            }
        }
        return include;
    }
}