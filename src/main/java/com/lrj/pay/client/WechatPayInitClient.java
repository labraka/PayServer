package com.lrj.pay.client;

import com.lrj.pay.config.WechatPayParams;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.Verifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/**
 * @ClassName: WechatPayInitClient
 * @Description: 初始化微信客户端和验签器
 * @Date: 2022/8/15 16:12
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
@NoArgsConstructor
@Component
@Slf4j
public class WechatPayInitClient {

    @Autowired
    private WechatPayParams wechatPayParams;

    /**
     * 初始化微信支付wechatPayClient
     *
     * @author: luorenjie
     * @date: 2022/8/16 10:26
     * @return: org.apache.http.impl.client.CloseableHttpClient
     */
    @Bean
    public CloseableHttpClient initHttpClient(Verifier verifier) throws FileNotFoundException {
        log.info("初始化微信支付wechatPayClient");
        //获取商户私钥
        PrivateKey privateKey = PemUtil.loadPrivateKey(new FileInputStream(wechatPayParams.getPrivateKeyPath()));

        CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
                .withMerchant(wechatPayParams.getMchId(), wechatPayParams.getMchSerialNo(), privateKey)
                .withValidator(new WechatPay2Validator(verifier)).build();

//        CloseableHttpClient httpClient = WechatPayHttpClientBuilder.create()
//                .withMerchant(wechatPayParams.getMchId(), wechatPayParams.getMchSerialNo(), privateKey)
//                .withWechatPay(listCertificates)
//                .build();
        return httpClient;
    }

    /**
     * 初始化验签器
     *
     * @author: luorenjie
     * @date: 2022/8/17 15:09
     * @return: com.wechat.pay.contrib.apache.httpclient.auth.Verifier
     */
    @Bean
    public Verifier getVerifier() throws Exception {

        log.info("初始化微信签名验证器");

        //获取商户私钥
        PrivateKey privateKey = PemUtil.loadPrivateKey(new FileInputStream(wechatPayParams.getPrivateKeyPath()));

        // 获取证书管理器实例
        CertificatesManager certificatesManager = CertificatesManager.getInstance();

        // 向证书管理器增加需要自动更新平台证书的商户信息
        certificatesManager.putMerchant(wechatPayParams.getMchId(), new WechatPay2Credentials(wechatPayParams.getMchId(),
                        new PrivateKeySigner(wechatPayParams.getMchSerialNo(), privateKey)),
                wechatPayParams.getV3Key().getBytes(StandardCharsets.UTF_8));
        // 从证书管理器中获取verifier
        Verifier verifier = certificatesManager.getVerifier(wechatPayParams.getMchId());

        return verifier;
    }
}
