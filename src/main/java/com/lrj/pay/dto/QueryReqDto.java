package com.lrj.pay.dto;

import lombok.Data;

/**
 * @ClassName: QueryReqDto
 * @Description: 订单查询请求参数
 * @Date: 2022/9/6 12:12
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class QueryReqDto {
    /**
     * 支付方式（1支付宝；2微信）
     */
    private Integer payType;
    /**
     * 购买类型（1购买；2续费）
     */
    private Integer consumeType;
    /**
     * 订阅时长
     */
    private Integer timeNum;
    /**
     * 并发数
     */
    private Integer num;
    /**
     * 产品id
     */
    private Long productId;
}
