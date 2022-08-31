package com.lrj.pay.enums;

/**
 * @ClassName: RefundStatusEnum
 * @Description: 退款状态枚举类
 * @Date: 2022/8/23 12:04
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum RefundStatusEnum {
    REFUNDING(0, "退款中"),
    REFUNDED(1, "已退款"),
    REFUND_FAIL(2, "退款失败"),
    ;

    private Integer type;
    private String des;

    RefundStatusEnum(Integer type, String des) {
        this.type = type;
        this.des = des;
    }

    /**
     * 根据类型获取支付状态枚举
     *
     * @param type
     * @author: luorenjie
     * @date: 2022/8/31 14:26
     * @return: com.lrj.pay.enums.RefundStatusEnum
     */
    public static RefundStatusEnum getRefundStatus(Integer type) {
        RefundStatusEnum[] refundStatusEnums = RefundStatusEnum.values();
        for (RefundStatusEnum refundStatusEnum : refundStatusEnums) {
            if (refundStatusEnum.getType().equals(type)) {
                return refundStatusEnum;
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
