package com.webank.webase.chain.mgr.base.enums;

public enum TaskStatusEnum {
    WAITING(0),//未开始
    READY(1),//准备开始
    HANDLING(2),//处理中
    SUCCESS(3),//成功
    FAIL(4),//失败
    ;

    private int value;

    private TaskStatusEnum(Integer groupType) {
        this.value = groupType;
    }

    public int getValue() {
        return this.value;
    }

    public static boolean isInclude(int key) {
        boolean include = false;
        for (TaskStatusEnum e : TaskStatusEnum.values()) {
            if (e.getValue() == key) {
                include = true;
                break;
            }
        }
        return include;
    }
}
