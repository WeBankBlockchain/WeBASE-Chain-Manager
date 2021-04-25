package com.webank.webase.chain.mgr.base.enums;

public enum GroupOperateTypeEnum {
    START("start"),//启动
    STOP("stop"),//停止
    REMOVE("remove"),//移除
    RECOVER("recover")//成功;
    ;


    private String value;

    private GroupOperateTypeEnum(String status) {
        this.value = status;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean isInclude(String key) {
        boolean include = false;
        for (GroupOperateTypeEnum e : GroupOperateTypeEnum.values()) {
            if (e.getValue().equals(key)) {
                include = true;
                break;
            }
        }
        return include;
    }
}