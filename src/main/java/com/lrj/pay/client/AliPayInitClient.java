package com.lrj.pay.client;

import com.alipay.api.*;
import com.lrj.pay.config.AlipayParams;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @ClassName: AliPayInitClient
 * @Description: 支付宝初始化客户端和参数
 * @Date: 2022/8/10 11:21
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
@NoArgsConstructor
@Component
@Slf4j
public class AliPayInitClient {
    @Autowired
    private AlipayParams alipayParams;

    @Bean
    public AlipayClient initAlipayClient() throws AlipayApiException {
        log.info("初始化支付宝支付alipayClient");
        AlipayConfig alipayConfig = new AlipayConfig();
        //设置网关地址
        alipayConfig.setServerUrl(alipayParams.getServerUrl());
        //设置应用Id
        alipayConfig.setAppId(alipayParams.getAppId());
        //设置应用私钥
        alipayConfig.setPrivateKey(alipayParams.getPrivateKey());
        //设置支付宝公钥
        alipayConfig.setAlipayPublicKey(alipayParams.getAlipayPublicKey());
        //设置请求格式，固定值json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        //设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        //设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        //构造client
        AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);
        return alipayClient;
    }
}
