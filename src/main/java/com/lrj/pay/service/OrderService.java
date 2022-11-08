package com.lrj.pay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrj.pay.dto.PayReqDto;
import com.lrj.pay.entity.Order;
import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.utils.Pages;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户订单表 服务类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
public interface OrderService extends IService<Order> {

    ApiResponse buy(PayReqDto payReqDto, Long userId);

    ApiResponse fee(PayReqDto feeReqDto, Long userId);

    String aliCallUrl(HttpServletRequest request);

    String wechatCallUrl(HttpServletRequest request, HttpServletResponse response);

    void checkPayStatus();

    ApiResponse refund(PayReqDto payReqDto);

    String wechatRefundCallUrl(HttpServletRequest request, HttpServletResponse response);

    void checkRefundStatus();

    void close(Order order);

    ApiResponse getUserOrders(Pages page, Long userId);

    ApiResponse unitePay(PayReqDto payReqDto, Long userId, HttpServletRequest request);
}
