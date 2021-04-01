package com.webank.webase.chain.mgr.base.enums;

public enum TaskTypeEnum {
    OBSERVER_TO_SEALER((byte)1),//观察者节点转共识节点
    ;

    private byte value;

    private TaskTypeEnum(byte groupType) {
        this.value = groupType;
    }

    public byte getValue() {
        return this.value;
    }

    public static boolean isInclude(byte key) {
        boolean include = false;
        for (TaskTypeEnum e : TaskTypeEnum.values()) {
            if (e.getValue() == key) {
                include = true;
                break;
            }
        }
        return include;
    }
}
