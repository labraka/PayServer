package com.lrj.pay.config;

import com.lrj.pay.strategy.YamlPropertySourceFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @ClassName: WechatPayParams
 * @Description: 微信支付支付核心参数
 * @Date: 2022/8/10 11:21
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@PropertySource(factory = YamlPropertySourceFactory.class, value = {"classpath:wechatpay.yaml"})
@Configuration
@Component
@NoArgsConstructor
@Data
public class WechatPayParams {
    @Value("${wechat-pay.app-id}")
    private String appId;
    @Value("${wechat-pay.mch-id}")
    private String mchId;
//    @Value("${wechat-pay.open-id}")
//    private String openId;
    @Value("${wechat-pay.v3-key}")
    private String v3Key;
    @Value("${wechat-pay.notify-url}")
    private String notifyUrl;
    @Value("${wechat-pay.refund-notify-url}")
    private String refundNotifyUrl;
    @Value("${wechat-pay.mch-serial-no}")
    private String  mchSerialNo;
    @Value("${wechat-pay.private-key-path}")
    private String  privateKeyPath;
//    @Value("${wechat-pay.platform-cert-path}")
//    private String  platformCertPath;
    @Value("${wechat-pay.booking-time}")
    private String  bookingTime;
}
