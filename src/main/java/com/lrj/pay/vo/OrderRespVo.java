package com.lrj.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName: OrderRespVo
 * @Description: 订单记录返回体
 * @Date: 2022/8/29 15:54
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class OrderRespVo {
    private Long id;
    private Long userId;
    private String orderNo;
    private String productName;
    private BigDecimal amount;
    private Integer consumeType;
    private Double consumeNum;
    private Integer timeType;
    private Integer payType;
    private Integer status;
    private Integer concurrentNum;
    private LocalDateTime tradeTime;
}
