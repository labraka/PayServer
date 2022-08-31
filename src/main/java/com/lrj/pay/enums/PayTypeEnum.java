package com.lrj.pay.enums;

/**
 * @ClassName: PayTypeEnum
 * @Description: 支付类型枚举
 * @Date: 2022/8/9 18:32
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum PayTypeEnum {
    ALI(1, "支付宝"),
    WECHAT(2, "微信");
    private Integer type;
    private String des;

    private PayTypeEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    /**
     * 根据支付类型获取支付类型枚举
     *
     * @param type
     * @author: luorenjie
     * @date: 2022/8/31 14:26
     * @return: com.lrj.pay.enums.PayTypeEnum
     */
    public static PayTypeEnum getPayType(Integer type) {
        PayTypeEnum[] payTypeEnums = PayTypeEnum.values();
        for (PayTypeEnum payTypeEnum : payTypeEnums) {
            if (payTypeEnum.getType().equals(type)) {
                return payTypeEnum;
            }
        }
        return null;
    }

    public Integer getType() {
        return type;
    }

    public String getDes() {
        return des;
    }
}
