package com.lrj.pay.enums;

/**
 * @ClassName: AliPayStatusEnum
 * @Description: 支付宝支付状态值
 * @Date: 2022/8/18 20:34
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum AliPayStatusEnum {
    WAIT_BUYER_PAY(0, "WAIT_BUYER_PAY", "交易创建，等待买家付款"),
    TRADE_SUCCESS(1, "TRADE_SUCCESS", "交易支付成功"),
    TRADE_CLOSED(2, "TRADE_CLOSED", "未付款交易超时关闭，或支付完成后全额退款"),
    TRADE_FINISHED(3, "TRADE_FINISHED", "交易结束，不可退款"),
    REFUND_SUCCESS(4, "REFUND_SUCCESS", "退款处理成功"),
    ;
    private Integer code;
    private String name;
    private String description;

    AliPayStatusEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * 根据名字返回枚举
     *
     * @param name
     * @author: luorenjie
     * @date: 2022/8/31 14:23
     * @return: com.lrj.pay.enums.AliPayStatusEnum
     */
    public static AliPayStatusEnum getByName(String name) {
        for (AliPayStatusEnum aliPayStatusEnum : AliPayStatusEnum.values()) {
            if (aliPayStatusEnum.getName().equals(name)) {
                return aliPayStatusEnum;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
