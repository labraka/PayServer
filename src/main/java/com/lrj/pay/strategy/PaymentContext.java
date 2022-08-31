package com.lrj.pay.strategy;

import com.alipay.api.AlipayApiException;
import com.lrj.pay.dto.PayReqDto;
import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @ClassName: PaymentContext
 * @Description: 支付策略上下文
 * @Date: 2022/8/9 18:18
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class PaymentContext {
    private PayReqDto payReqDto;
    private String orderNo;
    private HttpServletRequest request;

    private String refundNo;
    private BigDecimal refundAmount;
    private String refundReason;

    private String reqBody;

    private PaymentStrategy paymentStrategy;

    public PaymentContext(PayReqDto payReqDto, String orderNo, PaymentStrategy paymentStrategy) {
        this.payReqDto = payReqDto;
        this.orderNo = orderNo;
        this.paymentStrategy = paymentStrategy;
    }
    public PaymentContext(HttpServletRequest request, String reqBody, PaymentStrategy paymentStrategy) {
        this.request = request;
        this.reqBody = reqBody;
        this.paymentStrategy = paymentStrategy;
    }

    public PaymentContext(String orderNo, PaymentStrategy paymentStrategy) {
        this.orderNo = orderNo;
        this.paymentStrategy = paymentStrategy;
    }

    public PaymentContext(String orderNo, String refundNo, PaymentStrategy paymentStrategy) {
        this.orderNo = orderNo;
        this.refundNo = refundNo;
        this.paymentStrategy = paymentStrategy;
    }

    public PaymentContext(String orderNo, String refundNo, BigDecimal refundAmount, String refundReason, PaymentStrategy paymentStrategy) {
        this.orderNo = orderNo;
        this.refundNo = refundNo;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.paymentStrategy = paymentStrategy;
    }

    public Object payNow() throws AlipayApiException, IOException {
        return paymentStrategy.pay(this);
    }

    public boolean signVerified() throws Exception {
        return paymentStrategy.signVerified(this);
    }

    public String checkPayStatus() throws Exception {
        return paymentStrategy.checkPayStatus(this);
    }
    public Object refundNow() throws AlipayApiException, IOException {
        return paymentStrategy.refund(this);
    }

    public String checkRefundStatus() throws Exception {
        return paymentStrategy.checkRefundStatus(this);
    }

    public Object closeNow() throws Exception {
        return paymentStrategy.close(this);
    }
}
