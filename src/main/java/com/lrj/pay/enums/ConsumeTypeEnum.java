package com.lrj.pay.enums;

/**
 * @ClassName: ConsumeTypeEnum
 * @Description: 消费类型枚举
 * @Date: 2022/8/11 14:09
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum ConsumeTypeEnum {
    BUY(1,"购买"),
    FEE(2,"续费");
    private Integer type;
    private String des;

    ConsumeTypeEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    public Integer getType() {
        return type;
    }

    public String getDes() {
        return des;
    }
}
