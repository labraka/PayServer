package com.lrj.pay.strategy.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.lrj.pay.client.AliPayInitClient;
import com.lrj.pay.enums.DateTimeTypeEnum;
import com.lrj.pay.strategy.PaymentContext;
import com.lrj.pay.strategy.PaymentStrategy;
import com.lrj.pay.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @ClassName: AliPay
 * @Description: 支付宝支付实现类
 * @Date: 2022/8/9 18:29
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Slf4j
@Component
public class AliPay implements PaymentStrategy {
    @Autowired
    private AliPayInitClient aliPayInitClient;
    @Autowired
    private AlipayClient alipayClient;

    private static final String SUBJECT = "subject";
    private static final String BODY = "subject";

    @Override
    public Object payForPc(PaymentContext paymentContext) throws AlipayApiException {
        //1.根据支付宝的配置生成一个支付客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //2.创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setNotifyUrl(aliPayInitClient.getAlipayParams().getNotifyUrl());

        //3.组装当前业务方法的请求参数
        String bizContent = createPcPayRequestParams(paymentContext);
        alipayRequest.setBizContent(bizContent);

        //4.执行请求，调用支付宝接口
        AlipayTradePagePayResponse response = alipayClient.pageExecute(alipayRequest);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean signVerified(PaymentContext paymentContext) throws AlipayApiException {
        //获取支付宝POST过来反馈信息
        Map<String , String> params = new HashMap<> ();
        Map requestParams = paymentContext.getRequest().getParameterMap();
        for(Iterator iter = requestParams.keySet().iterator();iter.hasNext();){
            String name = (String)iter.next();
            String[] values = (String [])requestParams.get(name);
            String valueStr = "";
            for(int i = 0;i < values.length;i ++ ){
                valueStr =  (i==values.length-1)?valueStr + values [i]:valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("编码异常：{}", e);
            }
            params.put (name,valueStr);
        }
        log.info("支付宝支付回调返回值：{}", params);
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)

        //调用SDK验证签名
        boolean signVerified = AlipaySignature.rsaCheckV1 (params,
                aliPayInitClient.getAlipayParams().getAlipayPublicKey(),
                AlipayConstants.CHARSET_UTF8,
                AlipayConstants.SIGN_TYPE_RSA2);
        return signVerified;
    }

    @Override
    public String checkPayStatus(PaymentContext paymentContext) throws Exception {

        //1、获取alipay客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //2、请求参数
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        request.setBizContent(bizContent.toString());

        //3、执行查询请求
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        //4、响应内容
        String body = response.getBody();
        return body;
    }

    @Override
    public Object refund(PaymentContext paymentContext) throws AlipayApiException {
        //1、获取alipay客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //2、请求参数
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        String bizContent = createRefundRequestParams(paymentContext);
        request.setBizContent(bizContent);

        //3、执行请求，调用支付宝接口
        AlipayTradeRefundResponse response = alipayClient.execute(request);
        return response;
    }

    @Override
    public String checkRefundStatus(PaymentContext paymentContext) throws Exception {
        //=================== 1、获取alipay客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //=================== 2、请求参数
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        JSONObject bizContent = new JSONObject();
        //bizContent.put("trade_no", "2022050122001421280506656470");
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        bizContent.put("out_request_no", paymentContext.getRefundNo());

        //// 返回参数选项，按需传入
        //JSONArray queryOptions = new JSONArray();
        //queryOptions.add("refund_detail_item_list");
        //bizContent.put("query_options", queryOptions);
        request.setBizContent(bizContent.toString());

        //=================== 3、响应内容
        AlipayTradeFastpayRefundQueryResponse response = alipayClient.execute(request);
        return response.getBody();
    }

    @Override
    public Object close(PaymentContext paymentContext) throws AlipayApiException {
        //=================== 1、获取alipay客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //=================== 2、请求参数
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        request.setBizContent(bizContent.toString());

        //=================== 3、响应内容
        AlipayTradeCloseResponse response = alipayClient.execute(request);
        return response;
    }

    @Override
    public Object payForMp(PaymentContext paymentContext) throws AlipayApiException {
        //1.根据支付宝的配置生成一个支付客户端
//        AlipayClient alipayClient = aliPayInitClient.initAlipayClient();

        //2.创建一个支付请求 //设置请求参数
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setNotifyUrl(aliPayInitClient.getAlipayParams().getNotifyUrl());

        //3.组装当前业务方法的请求参数
        String bizContent = createMpPayRequestParams(paymentContext);
        alipayRequest.setBizContent(bizContent);

        //4.执行请求，调用支付宝接口
        AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
        return response;
    }

    /**
     * 组装支付宝支付请求参数
     * @author: luorenjie
     * @date: 2022/8/9 18:30
     * @param paymentContext
     * @return: java.lang.String
     */
    private String createPcPayRequestParams(PaymentContext paymentContext){
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        bizContent.put("total_amount", paymentContext.getPayReqDto().getAmount());
        bizContent.put("subject", SUBJECT);
        bizContent.put("body", BODY);
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        LocalDateTime bookingTime = DateUtil.addTime(LocalDateTime.now(), DateTimeTypeEnum.MILLIS.getType(), Long.valueOf(aliPayInitClient.getAlipayParams().getBookingTime()));
        String timeOut = DateUtil.dateToStr(bookingTime, "yyyy-MM-dd HH:mm:ss");
        bizContent.put("time_expire", timeOut);
        bizContent.put("qr_pay_mode",paymentContext.getPayReqDto().getPayQrMode());
        return bizContent.toString();
    }

    /**
     * 组装支付宝手机网站支付参数
     * @author: luorenjie
     * @date: 2022/9/15 14:27
     * @param paymentContext
     * @return: java.lang.String
     */
    private String createMpPayRequestParams(PaymentContext paymentContext){
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        bizContent.put("total_amount", paymentContext.getPayReqDto().getAmount());
        bizContent.put("subject", SUBJECT);
        bizContent.put("body", BODY);
        bizContent.put("product_code", "QUICK_WAP_WAY");
        LocalDateTime bookingTime = DateUtil.addTime(LocalDateTime.now(), DateTimeTypeEnum.MILLIS.getType(), Long.valueOf(aliPayInitClient.getAlipayParams().getBookingTime()));
        String timeOut = DateUtil.dateToStr(bookingTime, "yyyy-MM-dd HH:mm:ss");
        bizContent.put("time_expire", timeOut);
        return bizContent.toString();
    }

    /**
     * 组装支付宝退款请求参数
     * @author: luorenjie
     * @date: 2022/8/19 17:16
     * @param paymentContext
     * @return: java.lang.String
     */
    private String createRefundRequestParams(PaymentContext paymentContext){
        JSONObject bizContent = new JSONObject();
        bizContent.put("refund_reason", paymentContext.getRefundReason());
        bizContent.put("out_trade_no", paymentContext.getOrderNo());
        bizContent.put("refund_amount", paymentContext.getRefundAmount());
//        bizContent.put("out_request_no", "HZ01RF001");
        return bizContent.toString();
    }

}
