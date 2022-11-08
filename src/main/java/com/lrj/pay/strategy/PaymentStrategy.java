package com.lrj.pay.strategy;

import com.alipay.api.AlipayApiException;

import java.io.IOException;

/**
 * @ClassName: PaymentStrategy
 * @Description: 支付策略类
 * @Date: 2022/8/9 18:13
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public interface PaymentStrategy {
    /**
     * 支付
     * @author: luorenjie
     * @date: 2022/8/18 21:21
     * @param paymentContext
     * @return: java.lang.Object
     */
    Object payForPc(PaymentContext paymentContext) throws AlipayApiException, IOException;

    /**
     * 验签
     * @author: luorenjie
     * @date: 2022/8/18 21:21
     * @param paymentContext
     * @return: boolean
     */
    boolean signVerified(PaymentContext paymentContext) throws Exception;

    /**
     * 订单支付状态查询
     * @author: luorenjie
     * @date: 2022/8/18 21:22
     * @param paymentContext
     * @return: java.lang.String
     */
    String checkPayStatus(PaymentContext paymentContext) throws Exception;

    /**
     * 申请退款
     * @author: luorenjie
     * @date: 2022/8/18 21:22
     * @param paymentContext
     * @return: java.lang.String
     */
    Object refund(PaymentContext paymentContext) throws AlipayApiException, IOException;

    /**
     * 订单退款状态查询
     * @author: luorenjie
     * @date: 2022/8/18 21:24
     * @param paymentContext
     * @return: java.lang.String
     */
    String checkRefundStatus(PaymentContext paymentContext) throws Exception;

    /**
     * 关闭订单
     * @author: luorenjie
     * @date: 2022/8/29 10:07
     * @param paymentContext
     * @return: java.lang.Object
     */
    Object close(PaymentContext paymentContext) throws AlipayApiException, IOException;

    /**
     * 手机跳转支付
     * @author: luorenjie
     * @date: 2022/9/15 14:18
     * @param paymentContext
     * @return: java.lang.Object
     */
    Object payForMp(PaymentContext paymentContext) throws Exception;
}
