package com.lrj.pay.vo;

import lombok.Data;

/**
 * @ClassName: PayRespVo
 * @Description: 支付返回参数
 * @Date: 2022/8/16 15:53
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class PayRespVo {
    private Long rechargeId;
    private String jumpUrl;
}
