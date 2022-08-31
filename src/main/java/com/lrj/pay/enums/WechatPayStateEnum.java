package com.lrj.pay.enums;

/**
 * @ClassName: WechatPayStateEnum
 * @Description: 微信支付状态值
 * @Date: 2022/8/16 10:12
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public enum WechatPayStateEnum {
    SUCCESS(0, "SUCCESS", "支付成功"),
    REFUND(1, "REFUND", "转入退款"),
    NOTPAY(2, "NOTPAY", "未支付"),
    CLOSED(3, "CLOSED", "已关闭"),
    REVOKED(4, "REVOKED", "已撤销（付款码支付）"),
    USERPAYING(5, "USERPAYING", "用户支付中（付款码支付）"),
    PAYERROR(6, "PAYERROR", "支付失败(其他原因，如银行返回失败)"),
    ACCEPT(7, "ACCEPT", "已接收，等待扣款"),
    ABSENCE(8, "ABSENCE", "订单不存在"),
    OK(9, "OK", "OK"),
    PROCESSING(10, "PROCESSING", "退款处理中"),
    ABNORMAL(11, "ABNORMAL", "退款异常");
    private Integer code;

    private String name;

    private String description;

    WechatPayStateEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
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

    /**
     * 根据名字返回枚举
     *
     * @param name
     * @author: luorenjie
     * @date: 2022/8/31 14:27
     * @return: com.lrj.pay.enums.WechatPayStateEnum
     */
    public static WechatPayStateEnum getByName(String name) {
        for (WechatPayStateEnum wechatPayStateEnum : WechatPayStateEnum.values()) {
            if (wechatPayStateEnum.getName().equals(name)) {
                return wechatPayStateEnum;
            }
        }
        return null;
    }
}
