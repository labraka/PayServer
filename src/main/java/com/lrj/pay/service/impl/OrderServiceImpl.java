package com.lrj.pay.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrj.pay.dto.PayReqDto;
import com.lrj.pay.dto.QueryReqDto;
import com.lrj.pay.entity.Customer;
import com.lrj.pay.entity.Order;
import com.lrj.pay.entity.Product;
import com.lrj.pay.entity.Refund;
import com.lrj.pay.enums.*;
import com.lrj.pay.exception.PayException;
import com.lrj.pay.feign.UserFeignClient;
import com.lrj.pay.mapper.OrderMapper;
import com.lrj.pay.response.ApiResponse;
import com.lrj.pay.response.BaseRespBody;
import com.lrj.pay.service.OrderService;
import com.lrj.pay.service.ProductService;
import com.lrj.pay.service.RefundService;
import com.lrj.pay.strategy.PaymentContext;
import com.lrj.pay.strategy.impl.AliPay;
import com.lrj.pay.strategy.impl.WechatPay;
import com.lrj.pay.utils.DateUtil;
import com.lrj.pay.utils.Pages;
import com.lrj.pay.utils.SnowFlakeUtil;
import com.lrj.pay.vo.OrderRespVo;
import com.lrj.pay.vo.PayRespVo;
import com.lrj.pay.vo.PriceRespVo;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * ??????????????? ???????????????
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:20:03
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    @Autowired
    private ProductService productService;
    @Autowired
    private RefundService refundService;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AliPay aliPay;
    @Autowired
    private WechatPay wechatPay;
    @Autowired
    private UserFeignClient userFeignClient;

    ExecutorService checkPayStatusPool = new ThreadPoolExecutor(20, 200, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(2000), new NamedThreadFactory("Scan payment status"), new ThreadPoolExecutor.DiscardPolicy());
    ExecutorService checkRefundStatusPool = new ThreadPoolExecutor(20, 200, 1000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(2000), new NamedThreadFactory("Scan refund status"), new ThreadPoolExecutor.DiscardPolicy());

    private static final String REFUND_REASON = "?????????????????????";
    private static final String PRFIX = "RL";
    private static final int MONTH_DAYS = 30;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse buy(PayReqDto payReqDto, Long userId) {
        log.info(">>>>>>>>>>????????????");
        //????????????????????????
        Customer customer = userFeignClient.getCustomerById(userId);
        if (ObjectUtils.isEmpty(customer)) {
            log.error(ApiResponseEnum.USER_NOT_FOUND.getLabel());
            return ApiResponse.returnFail(ApiResponseEnum.USER_NOT_FOUND);
        }

        Product product = productService.getProduct(payReqDto.getProductId());
        if (ObjectUtils.isEmpty(product)) {
            log.error("??????????????????????????????{}", payReqDto.getProductId());
            return ApiResponse.returnFail(ApiResponseEnum.PRODUCT_NONE);
        }
        /*
         * ????????????????????????,???????????????????????????????????????or????????????????????????????????????
         */
        BigDecimal totalAmount = BigDecimal.ZERO;
        double timeNums;
        if (customer.getLevel() == 1
                || (ObjectUtils.isEmpty(customer.getVipEndTime())
                && customer.getVipEndTime().compareTo(LocalDateTime.now()) == -1)) {

            timeNums = payReqDto.getTimeNum();
        } else {
            LocalDateTime now = LocalDateTime.now();
            long days = DateUtil.differTimeNums(now, customer.getVipEndTime(), 4);
            if (days < 0) {
                log.error("?????????????????????{}", days);
                return ApiResponse.returnFail(ApiResponseEnum.PARAMETER_INVALID);
            }
            if (days == 0) {
                days = days + 1;
            }
            timeNums = Math.round(days * 100 / MONTH_DAYS) / 100.0;
        }

        totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(payReqDto.getNum()))
                .multiply(BigDecimal.valueOf(timeNums));

        //????????????
        if (payReqDto.getAmount().compareTo(BigDecimal.ZERO) != 1
                || totalAmount.compareTo(payReqDto.getAmount()) != 0) {
            log.error("???????????????????????????{}", payReqDto.getAmount());
            return ApiResponse.returnFail(ApiResponseEnum.MONEY_ERROR);
        }
        PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(payReqDto.getPayType());
        if (ObjectUtils.isEmpty(payTypeEnum)) {
            log.error("?????????????????????{}", payReqDto.getPayType());
            return ApiResponse.returnFail(ApiResponseEnum.PAY_TYPE_ERROR);
        }
        //????????????
        Order order = createOrder(payReqDto,
                customer,
                ConsumeTypeEnum.BUY.getType(),
                timeNums,
                product);

        //???????????????
        PayRespVo payRespVo = toPay(payReqDto, order, payTypeEnum);
        return ApiResponse.returnSuccess(payRespVo);
    }

    @Override
    public ApiResponse fee(PayReqDto payReqDto, Long userId) {
        //??????????????????
        Customer customer = userFeignClient.getCustomerById(userId);
        if (ObjectUtils.isEmpty(customer)) {
            log.error(ApiResponseEnum.USER_NOT_FOUND.getLabel());
            return ApiResponse.returnFail(ApiResponseEnum.USER_NOT_FOUND);
        }
        if (customer.getLevel() == 1
                || (ObjectUtils.isEmpty(customer.getVipEndTime())
                && customer.getVipEndTime().compareTo(LocalDateTime.now()) == -1)) {
            log.error("???????????????????????????????????????????????? ??????id???{}", userId);
            return ApiResponse.returnFail(ApiResponseEnum.USER_LOW_LEVEL);
        }

        //????????????????????????
        Product product = productService.getProduct(payReqDto.getProductId());
        if (ObjectUtils.isEmpty(product)) {
            log.error("?????????????????????????????????{}", payReqDto.getProductId());
            return ApiResponse.returnFail(ApiResponseEnum.PRODUCT_NONE);
        }

        //????????????
        BigDecimal totalAmount = product.getPrice()
                .multiply(BigDecimal.valueOf(payReqDto.getNum()))
                .multiply(new BigDecimal(payReqDto.getTimeNum()));

        //????????????
        if (payReqDto.getAmount().compareTo(BigDecimal.ZERO) != 1
                || totalAmount.compareTo(payReqDto.getAmount()) != 0) {
            log.error("???????????????????????????{}", payReqDto.getAmount());
            return ApiResponse.returnFail(ApiResponseEnum.MONEY_ERROR);
        }
        PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(payReqDto.getPayType());
        if (ObjectUtils.isEmpty(payTypeEnum)) {
            log.error("?????????????????????{}", payReqDto.getPayType());
            return ApiResponse.returnFail(ApiResponseEnum.PAY_TYPE_ERROR);
        }

        //????????????
        Order order = createOrder(payReqDto,
                customer,
                ConsumeTypeEnum.FEE.getType(),
                payReqDto.getTimeNum(),
                product);

        //???????????????
        PayRespVo payRespVo = toPay(payReqDto, order, payTypeEnum);
        return ApiResponse.returnSuccess(payRespVo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String aliCallUrl(HttpServletRequest request) {
        log.info(">>>>>>>>>>???????????????????????????");
        try {
            //??????SDK????????????
//            PaymentStrategy aliPay = aliPay;
            PaymentContext paymentContext = new PaymentContext(request, null, aliPay);
            boolean signVerified = paymentContext.signVerified();
            if (!signVerified) {
                // ?????????????????????????????????
                return "fail";
            }
            // ???????????????
            String orderNo = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
            // ??????????????????
            String tradeNo = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
            // ????????????
            String orderAmount = new String(request.getParameter("total_amount").getBytes("ISO-8859-1"), "UTF-8");

            String tradeStatus = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");

            //???????????????????????????????????????
            synchronized (orderNo.intern()) {
                updateOrder(tradeStatus, orderNo, tradeNo, LocalDateTime.now());
            }
            return "trade_no:" + tradeNo + "<br/>out_trade_no:" + orderNo + "<br/>total_amount:" + orderAmount;

        } catch (Exception e) {
            log.error("????????????????????????{}", e);
            throw new PayException(ApiResponseEnum.FAIL, e);
//            return "fail";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String wechatCallUrl(HttpServletRequest request, HttpServletResponse response) {
        log.info(">>>>>>>>>>????????????????????????");
        try {
            //??????????????????
            String body = wechatPay.readData(request);
            Map<String, Object> bodyMap = JSONObject.parseObject(body, Map.class);
            log.info("??????????????????{}", bodyMap);

            PaymentContext paymentContext = new PaymentContext(request, body, wechatPay);
            boolean signVerified = paymentContext.signVerified();
            Map<String, String> map = new HashMap<>();
            if (!signVerified) {
                log.error("??????????????????");
                //????????????
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "??????????????????");
                return JSONObject.toJSONString(map);
            }

            //????????????
            String plainText = wechatPay.decFromResource(bodyMap);
            //??????????????????map
            HashMap plainTextMap = JSONObject.parseObject(plainText, HashMap.class);
            //???????????????
            String orderNo = (String) plainTextMap.get("out_trade_no");
            String tradeState = (String) plainTextMap.get("trade_state");
            //???????????????
            String tradeNo = (String) plainTextMap.get("transaction_id");
            String successTime = (String) plainTextMap.get("success_time");
            synchronized (orderNo.intern()) {
                updateOrder(tradeState, orderNo, tradeNo, DateUtil.retransRFC3339(successTime));
            }

            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "??????");
            return JSONObject.toJSONString(map);

        } catch (Exception e) {
            log.error("?????????????????????{}", e);
            throw new PayException(ApiResponseEnum.FAIL, e);
        }
    }

    @Override
    public void checkPayStatus() {
        log.info(">>>>>>>>>>?????????????????????????????????");
        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Order::getStatus, PayStatusEnum.PAYING.getType());
        List<Order> orders = orderMapper.selectList(oqw);
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }

        //????????????4min?????????
        LocalDateTime end = DateUtil.addTime(LocalDateTime.now(), DateTimeTypeEnum.MINUTES.getType(), 4);
        List<Order> timeOutList = orders.stream().filter(x -> end.compareTo(x.getCerateTime()) != -1).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(timeOutList)) {
            return;
        }
        List<Long> oids = timeOutList.stream().map(x -> x.getId()).collect(Collectors.toList());
        log.info("????????????????????????????????????{}", oids);
        for (Order order : timeOutList) {
            PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(order.getPayType());
            checkPayStatusPool.execute(() -> {
                checkAndUpdate(payTypeEnum, order);
            });

        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse refund(PayReqDto payReqDto) {
        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Order::getOrderNo, payReqDto.getOrderNo());
        Order order = orderMapper.selectOne(oqw);
        if (ObjectUtils.isEmpty(order)) {
            return ApiResponse.returnFail(ApiResponseEnum.FAIL);
        }
        //??????????????????
        Refund refund = refundService.createRefundOnce(order.getId());

        //??????????????????
        Order order1 = new Order();
        order1.setStatus(PayStatusEnum.REFUNDING.getType());
        orderMapper.update(order1,
                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));

        //????????????
        PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(order.getPayType());
        switch (payTypeEnum) {
            case ALI:
                PaymentContext paymentContext = new PaymentContext(order.getOrderNo(),
                        refund.getRefundNo(),
                        order.getAmount(),
                        REFUND_REASON,
                        aliPay);
                try {
                    AlipayTradeRefundResponse response = (AlipayTradeRefundResponse) paymentContext.refundNow();
                    String msg = "??????????????????????????????????????? ===> " + response.getCode() + ", ???????????? ===> " + response.getMsg();
                    if (response.isSuccess()) {
                        msg = "?????????????????????????????????????????? ===> " + response.getBody();
                        //??????????????????????????????????????????????????????
                        refundService.updateRefund(refund.getId(), 1);

                        //??????????????????
                        order1.setStatus(PayStatusEnum.REFUNDED.getType());
                        orderMapper.update(order1,
                                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));
                    }
                    log.info(msg);
                } catch (Exception e) {
                    log.error("??????????????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.REFUND_FAIL, e);
                }
                break;
            case WECHAT:
                paymentContext = new PaymentContext(order.getOrderNo(),
                        refund.getRefundNo(),
                        order.getAmount(),
                        REFUND_REASON,
                        wechatPay);
                try {
                    CloseableHttpResponse response = (CloseableHttpResponse) paymentContext.refundNow();
                    int statusCode = response.getStatusLine().getStatusCode();
                    JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                    String msg = "???????????????????????????????????? ===> " + statusCode + ", ????????? ===> " + jsonObject;
                    if (statusCode == 200 || statusCode == 204) {
                        msg = "??????????????????????????????????????? ===> " + jsonObject;
                    }
                    log.info(msg);
                    String refundTradeNo = (String) jsonObject.get("refund_id");

                    if ((statusCode == 400
                            && jsonObject.get("code").equals("INVALID_REQUEST")
                            && jsonObject.get("message").equals("?????????????????????"))
                            || jsonObject.get("status").equals(WechatPayStateEnum.SUCCESS.getName())) {
                        Refund refund1 = new Refund();
                        refund1.setStatus(RefundStatusEnum.REFUNDED.getType());
                        refund1.setSuccessTime(LocalDateTime.now());
                        refund1.setUpdateTime(LocalDateTime.now());
                        refundService.updateRefund(refund1, refund.getId());

                        //??????????????????
                        order1.setStatus(PayStatusEnum.REFUNDED.getType());
                        orderMapper.update(order1,
                                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));
                    }

                    //???????????????????????????????????????
                    if (!ObjectUtils.isEmpty(jsonObject.get("status"))
                            && jsonObject.get("status").equals(WechatPayStateEnum.PROCESSING.getName())) {
                        if (!ObjectUtils.isEmpty(refundTradeNo)) {
                            Refund refund1 = new Refund();
                            refund1.setRefundTradeNo(refundTradeNo);
                            refundService.updateRefund(refund1, refund.getId());
                        }
                    }
                } catch (Exception e) {
                    log.error("???????????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.REFUND_FAIL, e);
                }
                break;
        }
        return ApiResponse.returnSuccess(ApiResponseEnum.SUCCESS);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String wechatRefundCallUrl(HttpServletRequest request, HttpServletResponse response) {
        log.info(">>>>>>>>>>????????????????????????");
        try {
            //??????????????????
            String body = wechatPay.readData(request);
            Map<String, Object> bodyMap = JSONObject.parseObject(body, Map.class);
            log.info("??????????????????{}", bodyMap);

            PaymentContext paymentContext = new PaymentContext(request, body, wechatPay);
            boolean signVerified = paymentContext.signVerified();
            Map<String, String> map = new HashMap<>();
            if (!signVerified) {
                log.error("??????????????????");
                //????????????
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "??????????????????");
                return JSONObject.toJSONString(map);
            }

            //????????????
            String plainText = wechatPay.decFromResource(bodyMap);

            //??????????????????map
            HashMap plainTextMap = JSONObject.parseObject(plainText, HashMap.class);

            //???????????????
            String orderNo = (String) plainTextMap.get("out_trade_no");
            synchronized (orderNo.intern()) {
                updateRefund(orderNo, plainTextMap);
            }

            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "??????");
            return JSONObject.toJSONString(map);

        } catch (Exception e) {
            log.error("?????????????????????{}", e);
            throw new PayException(ApiResponseEnum.FAIL, e);
        }
    }

    @Override
    public void checkRefundStatus() {
        log.info(">>>>>>>>>>???????????????????????????????????????");
        List<Refund> refunds = refundService.getRefundings();
        if (CollectionUtils.isEmpty(refunds)) {
            return;
        }
        List<Long> fids = refunds.stream().map(x -> x.getId()).collect(Collectors.toList());
        List<Long> oids = refunds.stream().map(x -> x.getOrderId()).collect(Collectors.toList());
        log.info("?????????????????????????????????{}", fids);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>().in(Order::getId, oids));
        Map<Long, Order> oMap = orders.stream().collect(Collectors.toMap(x -> x.getId(), x -> x));
        for (Refund refund : refunds) {
            Order order = oMap.get(refund.getOrderId());
            PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(order.getPayType());
            checkRefundStatusPool.execute(() -> {
                checkAndUpdate(payTypeEnum, refund, order.getOrderNo());
            });
        }

    }

    @Override
    public void close(Order order) {
        PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(order.getPayType());
        switch (payTypeEnum) {
            case ALI:
                PaymentContext paymentContext = new PaymentContext(order.getOrderNo(), aliPay);
                try {
                    AlipayTradeCloseResponse response = (AlipayTradeCloseResponse) paymentContext.closeNow();
                    String msg = "????????????????????????????????????????????? ===> " + response.getCode() + ", ???????????? ===> " + response.getMsg();
                    if (response.isSuccess()) {
                        msg = "???????????????????????????????????????????????? ===> " + response.getBody();
                    }
                    log.info(msg);
                } catch (Exception e) {
                    log.error("??????????????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.CLOSE_FAIL, e);
                }
                break;
            case WECHAT:
                paymentContext = new PaymentContext(order.getOrderNo(), wechatPay);
                try {
                    CloseableHttpResponse response = (CloseableHttpResponse) paymentContext.closeNow();
                    int statusCode = response.getStatusLine().getStatusCode();//???????????????
                    JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                    String msg = "?????????????????????????????????????????? ===> " + statusCode + ", ????????? ===> " + jsonObject;
                    if (statusCode == 200 || statusCode == 204) {
                        msg = "????????????????????????????????????????????? ===> " + jsonObject;
                    }
                    log.info(msg);
                    if (statusCode == 400 && jsonObject.get("code").equals("ORDER_CLOSED")
                            && jsonObject.get("message").equals("???????????????")) {
                        return;
                    }
                } catch (Exception e) {
                    log.error("???????????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.CLOSE_FAIL, e);
                }
                break;
        }
    }

    @Override
    public ApiResponse getUserOrders(Pages page, Long userId) {
        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Order::getUserId, userId);
        IPage<Order> iPage = new Page<>(page.getPageNo(), page.getSize());
        IPage<Order> orderIPage = orderMapper.selectPage(iPage, oqw);
        List<Order> orders = orderIPage.getRecords();
        page.setTotalRecords((int) orderIPage.getTotal());
        List<OrderRespVo> orderRespVos = new ArrayList<>();
        BaseRespBody baseRespBody = BaseRespBody.builder().page(page).resp(orderRespVos).build();
        if (CollectionUtils.isEmpty(orders)) {
            return ApiResponse.returnSuccess(baseRespBody);
        }

        BeanCopier copier = BeanCopier.create(Order.class, OrderRespVo.class, false);
        for (Order order : orders) {
            OrderRespVo orderRespVo = new OrderRespVo();
            copier.copy(order, orderRespVo, null);
            orderRespVo.setTradeTime(order.getUpdateTime());
            orderRespVos.add(orderRespVo);
        }
        return ApiResponse.returnSuccess(baseRespBody);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse unitePay(PayReqDto payReqDto, Long userId, HttpServletRequest request) {
        if (ObjectUtils.isEmpty(payReqDto.getPayMode())
                || ConsumeTypeEnum.getConsumeType(payReqDto.getPayMode()) == null){
            log.error(ApiResponseEnum.PARAMETER_INVALID.getLabel());
            return ApiResponse.returnFail(ApiResponseEnum.PARAMETER_INVALID);
        }

        //????????????????????????
        Customer customer = userFeignClient.getCustomerById(userId);
        if (ObjectUtils.isEmpty(customer)) {
            log.error(ApiResponseEnum.USER_NOT_FOUND.getLabel());
            return ApiResponse.returnFail(ApiResponseEnum.USER_NOT_FOUND);
        }

        //?????????????????????
        BeanCopier beanCopier = BeanCopier.create(PayReqDto.class, QueryReqDto.class, false);
        QueryReqDto queryReqDto = new QueryReqDto();
        queryReqDto.setConsumeType(payReqDto.getPayMode());
        beanCopier.copy(payReqDto, queryReqDto, null);
        ApiResponse apiResponse = productService.price(queryReqDto, customer);
        if (apiResponse.getCode() != 200){
            return apiResponse;
        }
        Map<String, Object> map = (Map<String, Object>) apiResponse.getData();
        String jsonStr = JSONObject.toJSONString(map.get("priceRespVo"));
        PriceRespVo priceRespVo = JSONObject.parseObject(jsonStr, PriceRespVo.class);
        String jsonStr1 = JSONObject.toJSONString(map.get("product"));
        Product product = JSONObject.parseObject(jsonStr1, Product.class);
        String jsonStr2 = JSONObject.toJSONString(map.get("customer"));
        double timeNums = (double) map.get("timeNums");

       /*
        //????????????????????????
        Customer customer = userFeignClient.getCustomerById(userId);
        if (ObjectUtils.isEmpty(customer)) {
            log.error(ApiResponseEnum.USER_NOT_FOUND.getLabel());
            return ApiResponse.returnFail(ApiResponseEnum.USER_NOT_FOUND);
        }
        if (payReqDto.getPayMode() == ConsumeTypeEnum.FEE.getType()){
            if (customer.getAccountLevel() == 1
                    || (ObjectUtils.isEmpty(customer.getVipEndTime())
                    && customer.getVipEndTime().compareTo(LocalDateTime.now()) == -1)) {
                log.error("???????????????????????????????????????????????? ??????id???{}", userId);
                return ApiResponse.returnFail(ApiResponseEnum.USER_LOW_LEVEL);
            }
        }

        RayLinkProduct rayLinkProduct = productService.getProduct(payReqDto.getProductId());
        if (ObjectUtils.isEmpty(rayLinkProduct)) {
            log.error("??????????????????????????????{}", payReqDto.getProductId());
            return ApiResponse.returnFail(ApiResponseEnum.PRODUCT_NONE);
        }
        double timeNums;
        if (payReqDto.getPayMode() == ConsumeTypeEnum.BUY.getType()){
            *//*
         * ????????????????????????,???????????????????????????????????????or????????????????????????????????????
         *//*
            BigDecimal totalAmount = BigDecimal.ZERO;
            if (customer.getAccountLevel() == 1
                    || (ObjectUtils.isEmpty(customer.getVipEndTime())
                    && customer.getVipEndTime().compareTo(LocalDateTime.now()) == -1)) {

                timeNums = payReqDto.getTimeNum();
            } else {
                LocalDateTime now = LocalDateTime.now();
                long days = DateUtil.differTimeNums(now, customer.getVipEndTime(), 4);
                if (days < 0) {
                    log.error("?????????????????????{}", days);
                    return ApiResponse.returnFail(ApiResponseEnum.PARAMETER_INVALID);
                }
                if (days == 0) {
                    days = days + 1;
                }
                timeNums = Math.round(days * 100 / MONTH_DAYS) / 100.0;
            }

            totalAmount = rayLinkProduct.getPrice()
                    .multiply(BigDecimal.valueOf(payReqDto.getNum()))
                    .multiply(BigDecimal.valueOf(timeNums));

            //????????????
            if (payReqDto.getAmount().compareTo(BigDecimal.ZERO) != 1
                    || totalAmount.compareTo(payReqDto.getAmount()) != 0) {
                log.error("???????????????????????????{}", payReqDto.getAmount());
                return ApiResponse.returnFail(ApiResponseEnum.MONEY_ERROR);
            }
        }else {
            //????????????
            BigDecimal totalAmount = rayLinkProduct.getPrice()
                    .multiply(BigDecimal.valueOf(payReqDto.getNum()))
                    .multiply(new BigDecimal(payReqDto.getTimeNum()));

            //????????????
            if (payReqDto.getAmount().compareTo(BigDecimal.ZERO) != 1
                    || totalAmount.compareTo(payReqDto.getAmount()) != 0) {
                log.error("???????????????????????????{}", payReqDto.getAmount());
                return ApiResponse.returnFail(ApiResponseEnum.MONEY_ERROR);
            }
            timeNums = payReqDto.getTimeNum();
        }

*/
        //????????????
        if (payReqDto.getAmount().compareTo(BigDecimal.ZERO) != 1
                || priceRespVo.getTotal().compareTo(payReqDto.getAmount()) != 0) {
            log.error("??????" + ConsumeTypeEnum.getConsumeType(payReqDto.getPayMode()).getDes()
                    + "???????????????{}?????????????????????{}", payReqDto.getAmount(), priceRespVo.getTotal());
            return ApiResponse.returnFail(ApiResponseEnum.MONEY_ERROR);
        }

        PayTypeEnum payTypeEnum = PayTypeEnum.getPayType(payReqDto.getPayType());
        if (ObjectUtils.isEmpty(payTypeEnum)) {
            log.error("?????????????????????{}", payReqDto.getPayType());
            return ApiResponse.returnFail(ApiResponseEnum.PAY_TYPE_ERROR);
        }
        //????????????
        Order order = createOrder(payReqDto,
                customer,
                payReqDto.getPayMode(),
                timeNums,
                product);

        //???????????????
        PayRespVo payRespVo = toUnitPay(payReqDto, order, payTypeEnum, request);
        return ApiResponse.returnSuccess(payRespVo);
    }

    /**
     * ??????????????????????????????/??????
     *
     * @param payReqDto
     * @param order
     * @param payTypeEnum
     * @param request
     * @author: luorenjie
     * @date: 2022/9/14 18:49
     * @return: com.ray.link.vo.PayRespVo
     */
    private PayRespVo toUnitPay(PayReqDto payReqDto, Order order, PayTypeEnum payTypeEnum, HttpServletRequest request) {
        PayRespVo payRespVo = new PayRespVo();
        String form = "";
        try {
            switch (payTypeEnum){
                case ALI:
                    PaymentContext paymentContext = new PaymentContext(payReqDto, order.getOrderNo(), aliPay);
                    AlipayTradeWapPayResponse response = (AlipayTradeWapPayResponse) paymentContext.payUnite();
                    String msg = "??????????????????????????????????????? ===> " + response.getCode() + ", ???????????? ===> " + response.getMsg();
                    if (response.isSuccess()) {
                        msg = "?????????????????????????????????????????? ===> " + response.getBody();
                        form = response.getBody();
                    }
                    log.info(msg);
                    break;
                case WECHAT:
                    paymentContext = new PaymentContext(request, payReqDto, order.getOrderNo(), wechatPay);
                    CloseableHttpResponse response1 = (CloseableHttpResponse)paymentContext.payUnite();
                    int statusCode = response1.getStatusLine().getStatusCode();
                    JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response1.getEntity()));
                    msg = "???????????????????????????????????? ===> " + statusCode + ", ????????? ===> " + jsonObject;
                    String prepayId = "";
                    if (statusCode == 200 || statusCode == 204) {
                        msg = "??????????????????????????????????????? ===> " + jsonObject;
                        prepayId = jsonObject.getString("prepay_id");
                    }
                    log.info(msg);
                    Map<String, String> map = wechatPay.buildPayMap(prepayId);
                    form = com.alibaba.fastjson2.JSON.toJSONString(map);
                    log.info("??????????????????:{}", map);
                    break;
            }
        }catch (Exception e){
            log.error("?????????????????????{}", e);
            throw new PayException(ApiResponseEnum.PAY_FAIL, e);
        }
        payRespVo.setRechargeId(order.getId());
        payRespVo.setJumpUrl(form);

        Order order1 = new Order();
        order1.setJumpUrl(form);
        orderMapper.update(order1,
                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));
        return payRespVo;
    }

    /**
     * ??????-???????????????
     *
     * @param payTypeEnum
     * @param order
     * @author: luorenjie
     * @date: 2022/8/20 14:42
     * @return: void
     */
    private void checkAndUpdate(PayTypeEnum payTypeEnum, Order order) {
        try {
            Order order1 = new Order();
            String tradeNo = null;
            String tradeStatus = null;
            switch (payTypeEnum) {
                case ALI:
                    PaymentContext paymentContext = new PaymentContext(order.getOrderNo(), aliPay);
                    String respBody = paymentContext.checkPayStatus();
                    Map<String, Object> bodyMap = JSONObject.parseObject(respBody, HashMap.class);
                    log.info("?????????????????????????????????{}", bodyMap);
                    Map<String, String> resMap = (HashMap) bodyMap.get("alipay_trade_query_response");
                    tradeStatus = resMap.get("trade_status");
                    tradeNo = resMap.get("trade_no");

                    if (tradeStatus.equals(AliPayStatusEnum.TRADE_SUCCESS.getName())
                            || tradeStatus.equals(AliPayStatusEnum.TRADE_FINISHED.getName())) {
                        order1.setStatus(PayStatusEnum.PAYED.getType());
                    } else {
                        order1.setStatus(PayStatusEnum.CANCEL.getType());
                    }
                    break;
                case WECHAT:
                    paymentContext = new PaymentContext(order.getOrderNo(), wechatPay);
                    respBody = paymentContext.checkPayStatus();
                    bodyMap = JSONObject.parseObject(respBody, HashMap.class);
                    log.info("??????????????????????????????{}", bodyMap);
                    tradeStatus = (String) bodyMap.get("trade_state");
                    tradeNo = (String) bodyMap.get("transaction_id");

                    if (tradeStatus.equals(WechatPayStateEnum.SUCCESS.getName())) {
                        order1.setStatus(PayStatusEnum.PAYED.getType());
                    } else {
                        order1.setStatus(PayStatusEnum.CANCEL.getType());
                    }
                    break;
            }
            order1.setUpdateTime(LocalDateTime.now());
            order1.setTradeNo(tradeNo);
            orderMapper.update(order1,
                    new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));

            //????????????
            close(order);
        } catch (Exception e) {
            log.error("?????????????????????????????????{}", e);
        }
    }

    /**
     * ??????-???????????????
     *
     * @param payTypeEnum
     * @param refund
     * @param orderNo
     * @author: luorenjie
     * @date: 2022/8/20 14:43
     * @return: void
     */
    private void checkAndUpdate(PayTypeEnum payTypeEnum, Refund refund, String orderNo) {
        try {
            String refundTradeNo = null;
            String refundStatus = null;
            String successTime = null;
            Refund refund1 = new Refund();
            Order order = new Order();
            switch (payTypeEnum) {
                case ALI:
                    PaymentContext paymentContext = new PaymentContext(orderNo, refund.getRefundNo(), aliPay);
                    String respBody = paymentContext.checkRefundStatus();
                    Map<String, Object> bodyMap = JSONObject.parseObject(respBody, HashMap.class);
                    log.info("???????????????????????????????????????{}", bodyMap);
                    Map<String, String> resMap = (HashMap) bodyMap.get("alipay_trade_fastpay_refund_query_response");
                    refundStatus = (String) bodyMap.get("code");
                    refundTradeNo = (String) bodyMap.get("msg");

                    if ((resMap.get("code").equals("10000") && resMap.get("msg").equals("Success"))
                            || refundStatus.equals(AliPayStatusEnum.REFUND_SUCCESS.getName())) {
                        refund1.setStatus(RefundStatusEnum.REFUNDED.getType());
                        refund1.setSuccessTime(LocalDateTime.now());
                        refund1.setRefundTradeNo(refundTradeNo);
                        order.setStatus(PayStatusEnum.REFUNDED.getType());
                    } else {
                        refund1.setStatus(RefundStatusEnum.REFUND_FAIL.getType());
                        order.setStatus(PayStatusEnum.REFUND_FAIL.getType());
                    }
                    refund1.setUpdateTime(LocalDateTime.now());

                    break;
                case WECHAT:
                    paymentContext = new PaymentContext(orderNo, refund.getRefundNo(), wechatPay);
                    respBody = paymentContext.checkRefundStatus();
                    bodyMap = JSONObject.parseObject(respBody, HashMap.class);
                    log.info("????????????????????????????????????{}", bodyMap);
                    refundStatus = (String) bodyMap.get("status");
                    refundTradeNo = (String) bodyMap.get("refund_id");

                    if (refundStatus.equals(WechatPayStateEnum.SUCCESS.getName())) {
                        refund1.setStatus(RefundStatusEnum.REFUNDED.getType());
                        successTime = (String) bodyMap.get("success_time");
                        LocalDateTime st = DateUtil.retransRFC3339(successTime);
                        refund1.setSuccessTime(st);
                        refund1.setRefundTradeNo(refundTradeNo);
                        order.setStatus(PayStatusEnum.REFUNDED.getType());
                    } else if (refundStatus.equals(WechatPayStateEnum.PROCESSING.getName())) {
                        refund1.setStatus(RefundStatusEnum.REFUNDING.getType());
                        order.setStatus(PayStatusEnum.REFUNDING.getType());
                    } else {
                        refund1.setStatus(RefundStatusEnum.REFUND_FAIL.getType());
                        order.setStatus(PayStatusEnum.REFUND_FAIL.getType());
                    }
                    refund1.setUpdateTime(LocalDateTime.now());
                    break;
            }
            refundService.updateRefund(refund1, refund.getId());
        } catch (Exception e) {
            log.error("?????????????????????????????????{}", e);
        }
    }

    /**
     * ???????????????/??????????????????
     *
     * @param payReqDto
     * @param order
     * @param payTypeEnum
     * @author: luorenjie
     * @date: 2022/8/31 14:30
     * @return: com.lrj.pay.vo.PayRespVo
     */
    private PayRespVo toPay(PayReqDto payReqDto, Order order, PayTypeEnum payTypeEnum) {
        PayRespVo payRespVo = new PayRespVo();
        payRespVo.setRechargeId(order.getId());
        String jumpUrl = null;
        PaymentContext paymentContext = null;
        switch (payTypeEnum) {
            case ALI:
                paymentContext = new PaymentContext(payReqDto, order.getOrderNo(), aliPay);
                try {
                    AlipayTradePagePayResponse response = (AlipayTradePagePayResponse) paymentContext.payNow();
                    String msg = "??????????????????????????????????????? ===> " + response.getCode() + ", ???????????? ===> " + response.getMsg();
                    if (response.isSuccess()) {
                        msg = "?????????????????????????????????????????? ===> " + response.getBody();
                        jumpUrl = response.getBody();
                    }
                    log.info(msg);
                } catch (Exception e) {
                    log.error("????????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.PAY_FAIL, e);
                }
                break;
            case WECHAT:
                paymentContext = new PaymentContext(payReqDto, order.getOrderNo(), wechatPay);
                try {
                    CloseableHttpResponse response = (CloseableHttpResponse) paymentContext.payNow();
                    int statusCode = response.getStatusLine().getStatusCode();
                    JSONObject jsonObject = JSONObject.parseObject(EntityUtils.toString(response.getEntity()));
                    String msg = "???????????????????????????????????? ===> " + statusCode + ", ????????? ===> " + jsonObject;
                    if (statusCode == 200 || statusCode == 204) {
                        msg = "??????????????????????????????????????? ===> " + jsonObject;
                        jumpUrl = jsonObject.getString("code_url");
                    }
                    log.info(msg);
                } catch (Exception e) {
                    log.error("?????????????????????{}", e);
                    throw new PayException(ApiResponseEnum.PAY_FAIL, e);
                }
                break;
        }
        payRespVo.setJumpUrl(jumpUrl);

        Order order1 = new Order();
        order1.setJumpUrl(jumpUrl);
        orderMapper.update(order1,
                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));
        return payRespVo;
    }

    /**
     * ????????????
     *
     * @param payReqDto
     * @param userId
     * @param consumeType
     * @param timeNums
     * @param product
     * @author: luorenjie
     * @date: 2022/8/31 14:29
     * @return: com.lrj.pay.entity.Order
     */
    private Order createOrder(PayReqDto payReqDto, Customer customer, Integer consumeType, double timeNums, Product product) {
        Order order = new Order();
        order.setUserId(customer.getId());
        order.setAmount(payReqDto.getAmount());
        order.setConcurrentNum(payReqDto.getNum());
        order.setConsumeType(consumeType);
        order.setConsumeNum(timeNums);
        order.setOrderNo(createOrderNo());
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setPrice(product.getPrice());
        order.setTimeType(product.getUnit());
        order.setPayType(payReqDto.getPayType());
        //?????????????????????????????????
        if (payReqDto.getPayMode().equals(ConsumeTypeEnum.BUY.getType())){
            if (customer.getLevel() == 2 || customer.getVipEndTime().isAfter(LocalDateTime.now())){
                order.setEndTime(customer.getVipEndTime());
                LocalDateTime now = LocalDateTime.now();
                long months = DateUtil.differTimeNums(now, customer.getVipEndTime(),  DateTimeTypeEnum.MONTH.getType());
                if (months <= 0) {
                    long days = DateUtil.differTimeNums(now, customer.getVipEndTime(), DateTimeTypeEnum.DAY.getType());
                    days += 1;
                    order.setConsumeNum((double) days);
                    order.setTimeType(DateTimeTypeEnum.DAY.getType());
                }
            }
        }
        orderMapper.insert(order);
        return order;
    }

    /**
     * ???????????????
     * @author: luorenjie
     * @date: 2022/9/27 17:22
     * @return: java.lang.String
     */
    private String createOrderNo(){
        SnowFlakeUtil idWorker = new SnowFlakeUtil(1, 1);
        long id = idWorker.nextId();
        String perDate = DateUtil.dateToStr(LocalDateTime.now(), "yyMMdd");
        String orderNo = perDate + id;
        return PRFIX + orderNo;
    }

    /**
     * ????????????
     *
     * @param orderNo
     * @param tradeNo
     * @author: luorenjie
     * @date: 2022/8/17 17:29
     * @return: void
     */
    private void updateOrder(String tradeState, String orderNo, String tradeNo, LocalDateTime successTime) {
        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(oqw);
        if (ObjectUtils.isEmpty(order)) {
            log.error("?????????{}?????????", orderNo);
            return;
        }
        if (order.getStatus() == 1) {
            log.info("????????????");
            return;
        }
        Customer customer = userFeignClient.getCustomerById(order.getUserId());

        LocalDateTime now = LocalDateTime.now();
        //??????????????????
        LocalDateTime endTime;
        if (customer.getLevel() == 1
                || (ObjectUtils.isEmpty(customer.getVipEndTime())
                && customer.getVipEndTime().compareTo(LocalDateTime.now()) == -1)) {
            endTime = DateUtil.addTime(LocalDateTime.now(),
                    order.getTimeType(),
                    order.getConsumeNum().intValue());
        } else {
            endTime = customer.getVipEndTime();
        }

        //??????????????????
        Order order1 = new Order();
        boolean tradeSuccess = false;
        if (order.getPayType().equals(PayTypeEnum.ALI.getType())) {
            if (tradeState.equals("TRADE_SUCCESS") || tradeState.equals("TRADE_FINISHED")) {
                order1.setStatus(PayStatusEnum.PAYED.getType());
                order1.setBeginTime(LocalDateTime.now());
                order1.setEndTime(endTime);
                tradeSuccess = true;
            }
        } else {
            if (tradeState.equals(WechatPayStateEnum.SUCCESS.getName())) {
                order1.setStatus(PayStatusEnum.PAYED.getType());
                order1.setBeginTime(LocalDateTime.now());
                order1.setEndTime(endTime);
                tradeSuccess = true;
            }
        }
        order1.setUpdateTime(successTime);
        order1.setTradeNo(tradeNo);
        orderMapper.update(order1,
                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));
        //??????????????????vip??????
        if (!tradeSuccess) {
            return;
        }

        log.info("???????????????????????????id???{}?????????", customer.getId());
        Customer customer1 = new Customer();
        //??????
        if (order.getConsumeType() == 1) {
            customer1.setLevel(2);
        } else {
            //???????????????????????????????????????
            endTime = DateUtil.addTime(customer.getVipEndTime(),
                    order.getTimeType(), order.getConsumeNum().intValue());
        }
        customer1.setVipEndTime(endTime);
        //????????????????????????????????????
        if (ObjectUtils.isEmpty(customer.getVipFirstTime())) {
            customer1.setVipFirstTime(now);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("userId", customer.getId());
        map.put("customer", customer1);
        userFeignClient.updateUser(map);
    }

    /**
     * ?????????????????????
     *
     * @param orderNo
     * @param plainTextMap
     * @author: luorenjie
     * @date: 2022/8/23 15:17
     * @return: void
     */
    private void updateRefund(String orderNo, HashMap plainTextMap) {
        LambdaQueryWrapper<Order> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Order::getOrderNo, orderNo);
        Order order = orderMapper.selectOne(oqw);
        if (ObjectUtils.isEmpty(order)) {
            log.error("?????????{}?????????", orderNo);
            return;
        }
        if (order.getStatus() == 5) {
            log.info("????????????");
            return;
        }


        //???????????????
        String refundTradeNo = (String) plainTextMap.get("refund_id");
        String refundStatus = (String) plainTextMap.get("refund_status");

        Order order1 = new Order();
        Refund refund1 = new Refund();
        if (refundStatus.equals(WechatPayStateEnum.SUCCESS.getName())) {
            order1.setStatus(PayStatusEnum.REFUNDED.getType());

            refund1.setStatus(RefundStatusEnum.REFUNDED.getType());
            String refundSuccessTime = (String) plainTextMap.get("success_time");
            refund1.setSuccessTime(DateUtil.retransRFC3339(refundSuccessTime));
        } else {
            order1.setStatus(PayStatusEnum.REFUND_FAIL.getType());
        }

        //????????????
        orderMapper.update(order1,
                new LambdaQueryWrapper<Order>().eq(Order::getId, order.getId()));


        //??????????????????
        Refund refund = refundService.getRefundByOrderId(order.getId());
        refund1.setRefundTradeNo(refundTradeNo);
        refund1.setUpdateTime(LocalDateTime.now());
        refundService.updateRefund(refund1, refund.getId());

    }
}
