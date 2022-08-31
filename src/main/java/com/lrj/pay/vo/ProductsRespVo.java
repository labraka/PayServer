package com.lrj.pay.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName: ProductsRespVo
 * @Description: 查询产品返回参数
 * @Date: 2022/8/29 12:12
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class ProductsRespVo {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer unit;
}
