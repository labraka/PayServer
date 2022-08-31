package com.lrj.pay.enums;

/**
 * @ClassName: PayStatusEnum
 * @Description: 订单状态枚举类
 * @Date: 2022/8/18 12:14
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum PayStatusEnum {
    PAYING(0, "待支付"),
    PAYED(1, "已支付"),
    PAY_FAIL(2, "支付失败"),
    CANCEL(3, "已取消"),
    REFUNDING(4, "退款中"),
    REFUNDED(5, "已退款"),
    REFUND_FAIL(6, "退款失败"),

    ;
    private Integer type;
    private String des;

    PayStatusEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    /**
     * 根据类型获取支付状态枚举
     *
     * @param type
     * @author: luorenjie
     * @date: 2022/8/31 14:25
     * @return: com.lrj.pay.enums.PayStatusEnum
     */
    public static PayStatusEnum getPayStatus(Integer type) {
        PayStatusEnum[] payStatusEnums = PayStatusEnum.values();
        for (PayStatusEnum payStatusEnum : payStatusEnums) {
            if (payStatusEnum.getType().equals(type)) {
                return payStatusEnum;
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
