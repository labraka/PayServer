package com.lrj.pay.config;

/**
 * @ClassName: WechatUrlConfig
 * @Description: 微信发送请求的url
 * @Date: 2022/8/15 15:56
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public class WechatUrlConfig {
    /**
     * 统一下单
     */
    public static final String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/native";

    /**
     * 查询订单
     */
    public static final String ORDER_QUERY_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s";

    /**
     * 关闭订单
     */
    public static final String ORDER_CLOSE_URL = "https://api.mch.weixin.qq.com/v3/pay/transactions/out-trade-no/%s/close";

    /**
     * 申请退款
     */
    public static final String ORDER_REFUND_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds";

    /**
     * 查询退款
     */
    public static final String REFUND_QUERY_URL = "https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/%s";
}
