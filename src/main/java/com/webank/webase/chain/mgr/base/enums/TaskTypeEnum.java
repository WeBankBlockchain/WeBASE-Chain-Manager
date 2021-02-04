package com.webank.webase.chain.mgr.base.enums;

public enum TaskTypeEnum {
    OBSERVER_TO_SEALER(1),//观察者节点转共识节点
    ;

    private int value;

    private TaskTypeEnum(Integer groupType) {
        this.value = groupType;
    }

    public int getValue() {
        return this.value;
    }

    public static boolean isInclude(int key) {
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
