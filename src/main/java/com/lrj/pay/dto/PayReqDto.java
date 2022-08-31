package com.lrj.pay.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: PayReqDto
 * @Description: 支付请求主要传参
 * @Date: 2022/8/10 15:28
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */

@Data
public class PayReqDto {
    /**
     * 产品id
     */
    private Long productId;
    /**
     * 总金额
     */
    private BigDecimal amount;
    /**
     * 订阅时长
     */
    private Integer timeNum;
    /**
     * 支付方式（1支付宝；2微信）
     */
    private Integer payType;
    /**
     *  0：简约前置模式；1：前置模式；2：跳转模式；3：迷你前置模式；4：可定义宽度的嵌入式二维码
     *  详见说明：https://pingplusplus.kf5.com/hc/kb/article/1137360/
     */
    private Integer payQrMode;
    /**
     * 并发数
     */
    private Integer num;
    /**
     * 订单编号
     */
    private String orderNo;
    /**
     * 时间类型（1日，2周，3月，4年）
     */
//    private Integer timeType;
}
