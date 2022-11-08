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

    /**
     * 根据消费类型获取消费类型枚举
     * @author: luorenjie
     * @date: 2022/9/15 16:39
     * @param type
     * @return: com.ray.link.enums.PayTypeEnum
     */
    public static ConsumeTypeEnum getConsumeType(Integer type){
        ConsumeTypeEnum[] consumeTypeEnums = ConsumeTypeEnum.values();
        for (ConsumeTypeEnum payTypeEnum : consumeTypeEnums) {
            if (payTypeEnum.getType().equals(type)){
                return payTypeEnum;
            }
        }
        return null;
    }
}
