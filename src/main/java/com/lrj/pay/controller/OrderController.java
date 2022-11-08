package com.lrj.pay.controller;


import com.lrj.pay.dto.PayReqDto;
import com.lrj.pay.request.BaseReqBody;
import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.service.OrderService;
import com.lrj.pay.utils.Pages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 用户订单表 前端控制器
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
@RestController
@RequestMapping("/consume")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 购买
     *
     * @param payReqDto
     * @param request
     * @author: luorenjie
     * @date: 2022/8/31 14:24
     * @return: com.lrj.pay.response.ApiResponse
     */
    @PostMapping("/buy")
    public ApiResponse buy(@RequestBody PayReqDto payReqDto, HttpServletRequest request) {
//        String userId = request.getHeader("userId");
        String userId = "1";
        return orderService.buy(payReqDto, Long.valueOf(userId));
    }

    /**
     * 续费
     *
     * @param payReqDto
     * @param request
     * @author: luorenjie
     * @date: 2022/8/31 14:24
     * @return: com.lrj.pay.response.ApiResponse
     */
    @PostMapping("/fee")
    public ApiResponse fee(@RequestBody PayReqDto payReqDto, HttpServletRequest request) {
//        String userId = request.getHeader("userId");
        String userId = "1";
        return orderService.fee(payReqDto, Long.valueOf(userId));
    }

    /**
     * 支付宝支付回调
     *
     * @param request
     * @author: luorenjie
     * @date: 2022/8/11 19:58
     * @return: java.lang.String
     */
    @PostMapping("/aliCallUrl")
    public String aliCallUrl(HttpServletRequest request) {
        return orderService.aliCallUrl(request);
    }

    /**
     * 微信支付回调
     *
     * @param request
     * @author: luorenjie
     * @date: 2022/8/11 20:02
     * @return: java.lang.String
     */
    @PostMapping("/wechatCallUrl")
    public String wechatCallUrl(HttpServletRequest request, HttpServletResponse response) {
        return orderService.wechatCallUrl(request, response);
    }

    /**
     * 检测超时未支付订单并处理
     *
     * @author: luorenjie
     * @date: 2022/8/18 21:07
     * @return: void
     */
    @GetMapping("/checkPayStatus")
    public void checkPayStatus() {
        orderService.checkPayStatus();
    }

    /**
     * 申请退款接口
     *
     * @param payReqDto
     * @author: luorenjie
     * @date: 2022/8/31 14:24
     * @return: com.lrj.pay.response.ApiResponse
     */
    @PostMapping("/refund")
    public ApiResponse refund(@RequestBody PayReqDto payReqDto) {
        return orderService.refund(payReqDto);
    }

    /**
     * 微信申请退款回调
     *
     * @param request
     * @author: luorenjie
     * @date: 2022/8/19 17:24
     * @return: java.lang.String
     */
    @PostMapping("/wechatRefundCallUrl")
    public String wechatRefundCallUrl(HttpServletRequest request, HttpServletResponse response) {
        return orderService.wechatRefundCallUrl(request, response);
    }

    /**
     * 检测退款中的订单并处理
     *
     * @author: luorenjie
     * @date: 2022/8/18 21:07
     * @return: void
     */
    @GetMapping("/checkRefundStatus")
    public void checkRefundStatus() {
        orderService.checkRefundStatus();
    }


    /**
     * 用户查询订单明细
     *
     * @param baseReqBody
     * @param request
     * @author: luorenjie
     * @date: 2022/8/31 14:25
     * @return: com.lrj.pay.response.ApiResponse
     */
    @PostMapping("/query")
    public ApiResponse getUserOrders(@RequestBody(required = false) BaseReqBody<PayReqDto> baseReqBody, HttpServletRequest request) {
//        String userId = request.getHeader("userId");
        String userId = "1";
        if (ObjectUtils.isEmpty(baseReqBody)) {
            baseReqBody = BaseReqBody.init();
        }
        Pages page = baseReqBody.getPage();
        PayReqDto payReqDto = baseReqBody.getReq();
        return orderService.getUserOrders(page, Long.valueOf(userId));
    }

    /**
     * 测试定时任务所执行的方法
     *
     * @author: luorenjie
     * @date: 2022/8/23 10:53
     * @return: void
     */
    @GetMapping("/testTask")
    public void testTask() {
//        orderService.checkPayStatus();
        orderService.checkRefundStatus();
    }

    /**
     * 聚合支付
     * @author: luorenjie
     * @date: 2022/9/15 16:43
     * @param payReqDto
     * @param request
     * @return: com.ray.link.api.ApiResponse
     */
    @PostMapping("/unitePay")
    public ApiResponse unitePay(@RequestBody PayReqDto payReqDto, HttpServletRequest request){
        String userId = request.getHeader("userId");
//        String userId = "1";
        return orderService.unitePay(payReqDto, Long.valueOf(userId), request);
    }
}
