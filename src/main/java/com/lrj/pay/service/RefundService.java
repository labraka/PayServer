package com.lrj.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrj.pay.entity.Refund;

import java.util.List;

/**
 * <p>
 * 订单退款记录表 服务类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:53:08
 */
public interface RefundService extends IService<Refund> {
    /**
     * 一次性退款构建退款单
     *
     * @param orderId
     * @author: luorenjie
     * @date: 2022/8/31 14:21
     * @return: com.lrj.pay.entity.Refund
     */
    Refund createRefundOnce(Long orderId);

    void updateRefund(Long id, int status);

    void updateRefund(Refund refund, Long id);

    List<Refund> getRefundings();

    Refund getRefundByOrderId(Long orderId);
}
