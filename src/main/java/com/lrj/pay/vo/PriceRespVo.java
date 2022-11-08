package com.lrj.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @ClassName: PriceRespVo
 * @Description:
 * @Date: 2022/9/7 17:16
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class PriceRespVo {
    private Long productId;
    private BigDecimal total;
    private LocalDateTime endTime;
}
