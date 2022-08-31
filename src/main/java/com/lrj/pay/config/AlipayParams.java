package com.lrj.pay.config;

import com.lrj.pay.strategy.YamlPropertySourceFactory;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @ClassName: AlipayParams
 * @Description: 支付宝支付核心参数
 * @Date: 2022/8/10 11:21
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@PropertySource(factory = YamlPropertySourceFactory.class, value = {"classpath:alipay.yaml"})
@Configuration
@Component
@NoArgsConstructor
@Data
public class AlipayParams {
    @Value("${ali-pay.app-id}")
    private String appId;
    @Value("${ali-pay.gateway-url}")
    private String serverUrl;
    @Value("${ali-pay.notify-url}")
    private String notifyUrl;
    @Value("${ali-pay.private-key}")
    private String privateKey;
    @Value("${ali-pay.public-key}")
    private String alipayPublicKey;
    @Value("${ali-pay.booking-time}")
    private String bookingTime;
}