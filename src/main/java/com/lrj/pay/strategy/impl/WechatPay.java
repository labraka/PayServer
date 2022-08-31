package com.lrj.pay.strategy.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lrj.pay.client.WechatPayInitClient;
import com.lrj.pay.config.WechatPayParams;
import com.lrj.pay.config.WechatUrlConfig;
import com.lrj.pay.enums.DateTimeTypeEnum;
import com.lrj.pay.strategy.PaymentContext;
import com.lrj.pay.strategy.PaymentStrategy;
import com.lrj.pay.utils.DateUtil;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.wechat.pay.contrib.apache.httpclient.constant.WechatPayHttpHeaders.*;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * @ClassName: WechatPay
 * @Description: 微信支付实现类
 * @Date: 2022/8/9 18:29
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Slf4j
@Component
public class WechatPay implements PaymentStrategy {
    @Autowired
    private CloseableHttpClient wechatPayClient;
    @Autowired
    private WechatPayInitClient wechatPayInitClient;
    @Autowired
    private Verifier verifier;

    private static String DESCRIPTION = "瑞云远程桌面连接数购买";
    private static final long RESPONSE_EXPIRED_MINUTES = 5;

    @Override
    public Object pay(PaymentContext paymentContext) throws IOException {
        //1.根据微信的配置生成一个支付客户端
//        CloseableHttpClient httpClient = wechatPayInitClient.initHttpClient();

        //2.创建一个支付请求 //设置请求参数
        HttpPost httpPost = new HttpPost(WechatUrlConfig.UNIFIED_ORDER_URL);
        httpPost.addHeader(ACCEPT, APPLICATION_JSON.toString());
        httpPost.addHeader(CONTENT_TYPE, APPLICATION_JSON.toString());


        //3.组装当前业务方法的请求参数
        String paramsStr = createPayRequestParams(paymentContext);
        StringEntity entityStr = new StringEntity(paramsStr, "UTF-8");
        entityStr.setContentType(APPLICATION_JSON.toString());
        httpPost.setEntity(entityStr);

        //4.执行请求，调用微信接口
        CloseableHttpResponse response = wechatPayClient.execute(httpPost);
        String bodyAsString = EntityUtils.toString(response.getEntity());
        log.info("微信支付返回参数：{}", bodyAsString);
        return response;
    }

    @Override
    public boolean signVerified(PaymentContext paymentContext) throws Exception {
        HttpServletRequest request = paymentContext.getRequest();

        //处理通知参数
        Map<String, Object> bodyMap = JSONObject.parseObject(paymentContext.getReqBody(), Map.class);
        log.info("验签请求体：{}", bodyMap);
        String requestId = (String) bodyMap.get("id");

        //处理请求参数
        validateParameters(request, requestId);

        //构造验签名串
        String message = buildMessage(request, paymentContext.getReqBody());
        String serial = request.getHeader(WECHAT_PAY_SERIAL);
        String signature = request.getHeader(WECHAT_PAY_SIGNATURE);

        //验签
        boolean signVerified = verifier.verify(serial, message.getBytes(StandardCharsets.UTF_8), signature);
        return signVerified;
    }

    @Override
    public String checkPayStatus(PaymentContext paymentContext) throws Exception {
        //构建请求url
        String url = String.format(WechatUrlConfig.ORDER_QUERY_URL, paymentContext.getOrderNo());
        url = url.concat("?mchid=").concat(wechatPayInitClient.getWechatPayParams().getMchId());
        URIBuilder uriBuilder = new URIBuilder(url);

        //构建请求并设置参数
        HttpGet httpGet = new HttpGet(uriBuilder.build());
        httpGet.addHeader(ACCEPT, APPLICATION_JSON.toString());

        //执行查询请求
        CloseableHttpResponse response = wechatPayClient.execute(httpGet);

        //响应体
        String body = EntityUtils.toString(response.getEntity());
        return body;
    }

    @Override
    public Object refund(PaymentContext paymentContext) throws IOException {
        //构建请求
        HttpPost httpPost = new HttpPost(WechatUrlConfig.ORDER_REFUND_URL);

        //构建参数
        Map paramsMap = createRefundRequestParams(paymentContext);
        String reqJson = JSONObject.toJSONString(paramsMap);
        StringEntity entity = new StringEntity(reqJson, "utf-8");
        entity.setContentType(APPLICATION_JSON.toString());//设置请求报文格式
        httpPost.setEntity(entity);//将请求报文放入请求对象
        httpPost.setHeader(ACCEPT, APPLICATION_JSON.toString());//设置响应报文格式

        //并执行请求
        CloseableHttpResponse response = wechatPayClient.execute(httpPost);
        return response;
    }

    @Override
    public String checkRefundStatus(PaymentContext paymentContext) throws Exception {
        //构建请求
        String url = String.format(WechatUrlConfig.REFUND_QUERY_URL, paymentContext.getRefundNo());
        //创建远程Get 请求对象
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(ACCEPT, APPLICATION_JSON.toString());

        //执行请求
        CloseableHttpResponse response = wechatPayClient.execute(httpGet);

        //响应体
        String body = EntityUtils.toString(response.getEntity());
        return body;
    }

    @Override
    public Object close(PaymentContext paymentContext) throws IOException {
        //构建请求
        String url = String.format(WechatUrlConfig.ORDER_CLOSE_URL, paymentContext.getOrderNo());

        //创建远程Post 请求对象
        HttpPost httpPost = new HttpPost(url);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wechatPayInitClient.getWechatPayParams().getMchId());
        String jsonParams = JSON.toJSONString(paramsMap);

        //将请求参数设置到请求对象中
        StringEntity entity = new StringEntity(jsonParams,"utf-8");
        entity.setContentType(APPLICATION_JSON.toString());
        httpPost.setEntity(entity);
        httpPost.setHeader(ACCEPT, APPLICATION_JSON.toString());

        //执行请求
        CloseableHttpResponse response = wechatPayClient.execute(httpPost);
        return response;
    }

    /**
     * 组装微信支付请求参数
     *
     * @param paymentContext
     * @author: luorenjie
     * @date: 2022/8/17 10:44
     * @return: com.fasterxml.jackson.databind.node.ObjectNode
     */
    private String createPayRequestParams(PaymentContext paymentContext) {
        WechatPayParams wechatPayParams = wechatPayInitClient.getWechatPayParams();
        LocalDateTime endTime = DateUtil.addTime(LocalDateTime.now(), DateTimeTypeEnum.MILLIS.getType(), Long.valueOf(wechatPayParams.getBookingTime()));
        String timeOut = DateUtil.transRFC3339(endTime);

        Map paramsMap = new HashMap();
        paramsMap.put("appid", wechatPayParams.getAppId());
        paramsMap.put("mchid", wechatPayParams.getMchId());
        paramsMap.put("description", DESCRIPTION);
        paramsMap.put("notify_url", wechatPayParams.getNotifyUrl());
        paramsMap.put("out_trade_no", paymentContext.getOrderNo());
        paramsMap.put("time_expire", timeOut);

        Map amountMap = new HashMap();
        amountMap.put("total", paymentContext.getPayReqDto().getAmount().multiply(new BigDecimal(100)).intValue());
        amountMap.put("currency", "CNY");
        paramsMap.put("amount", amountMap);

        String json = JSON.toJSONString(paramsMap);
        return json;
    }

    /**
     * 构建微信退款请求参数
     *
     * @param paymentContext
     * @author: luorenjie
     * @date: 2022/8/19 16:38
     * @return: java.util.Map
     */
    private Map createRefundRequestParams(PaymentContext paymentContext) {
        Map paramsMap = new HashMap();
        paramsMap.put("out_trade_no", paymentContext.getOrderNo());//订单编号
        paramsMap.put("out_refund_no", paymentContext.getRefundNo());//退款单编号
        paramsMap.put("reason", paymentContext.getRefundReason());//退款原因
        paramsMap.put("notify_url", wechatPayInitClient.getWechatPayParams().getRefundNotifyUrl());//退款通知地址

        Map amountMap = new HashMap();
        amountMap.put("refund", paymentContext.getRefundAmount().multiply(new BigDecimal(100)).intValue());//退款金额
        amountMap.put("total", paymentContext.getRefundAmount().multiply(new BigDecimal(100)).intValue());//原订单金额
        amountMap.put("currency", "CNY");//退款币种
        paramsMap.put("amount", amountMap);
        return paramsMap;
    }

    /**
     * 读取请求体
     *
     * @param request
     * @author: luorenjie
     * @date: 2022/8/17 10:55
     * @return: java.lang.String
     */
    public String readData(HttpServletRequest request) {
        BufferedReader br = null;
        try {
            StringBuilder result = new StringBuilder();
            br = request.getReader();
            for (String line; (line = br.readLine()) != null; ) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(line);
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 校验请求头参数
     *
     * @param request
     * @param requestId
     * @author: luorenjie
     * @date: 2022/8/17 10:56
     * @return: void
     */
    private void validateParameters(HttpServletRequest request, String requestId) {
        // NOTE: ensure HEADER_WECHAT_PAY_TIMESTAMP at last
        String[] headers = {WECHAT_PAY_SERIAL, WECHAT_PAY_SIGNATURE, WECHAT_PAY_NONCE, WECHAT_PAY_TIMESTAMP};

        String header = null;
        for (String headerName : headers) {
            header = request.getHeader(headerName);
            if (header == null) {
                String message = String.format("empty [%s], request-id=[%s]", headerName, requestId);
                throw new IllegalArgumentException(message);
            }
        }

        //判断请求是否过期
        String timestampStr = header;
        try {
            Instant responseTime = Instant.ofEpochSecond(Long.parseLong(timestampStr));
            // 拒绝过期请求
            if (Duration.between(responseTime, Instant.now()).abs().toMinutes() >= RESPONSE_EXPIRED_MINUTES) {
                String message = String.format("timestamp=[%s] expires, request-id=[%s]", timestampStr, requestId);
                throw new IllegalArgumentException(message);
            }
        } catch (DateTimeException | NumberFormatException e) {
            String message = String.format("invalid timestamp=[%s], request-id=[%s]", timestampStr, requestId);
            throw new IllegalArgumentException(message);
        }

    }

    /**
     * 构建消息体
     *
     * @param request
     * @param body
     * @author: luorenjie
     * @date: 2022/8/17 10:56
     * @return: java.lang.String
     */
    public String buildMessage(HttpServletRequest request, String body) throws IOException {
        String timestamp = request.getHeader(WECHAT_PAY_TIMESTAMP);
        String nonce = request.getHeader(WECHAT_PAY_NONCE);
        return timestamp + "\n"
                + nonce + "\n"
                + body + "\n";
    }


    /**
     * 对称解密
     * @author: luorenjie
     * @date: 2022/8/23 17:27
     * @param bodyMap
     * @return: java.lang.String
     */
    public  String decFromResource(Map<String, Object> bodyMap) throws GeneralSecurityException {

        log.info("密文解密");

        //通知数据
        Map<String, String> resourceMap = (Map) bodyMap.get("resource");
        //数据密文
        String ciphertext = resourceMap.get("ciphertext");
        //随机串
        String nonce = resourceMap.get("nonce");
        //附加数据
        String associatedData = resourceMap.get("associated_data");

        log.info("密文 ===> {}", ciphertext);
        AesUtil aesUtil = new AesUtil(wechatPayInitClient.getWechatPayParams().getV3Key().getBytes(StandardCharsets.UTF_8));
        String plainText = aesUtil.decryptToString(associatedData.getBytes(StandardCharsets.UTF_8),
                nonce.getBytes(StandardCharsets.UTF_8),
                ciphertext);

        log.info("明文 ===> {}", plainText);

        return plainText;
    }
}
