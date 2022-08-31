package com.lrj.pay.task;

import com.lrj.pay.service.OrderService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName: OrderRefundStatusJob
 * @Description: 扫描退款中的订单，并关闭订单
 * @Date: 2022/8/22 11:42
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Slf4j
@Component
public class OrderRefundStatusJob {
    @Autowired
    private OrderService orderService;

    //    @Scheduled(cron = "0 * * * * ?")
    @XxlJob("OrderRefundStatusJob")
    public ReturnT<String> execute(String param) {
        //查询失败的订单，并关闭订单
        log.info(">>>>>>>>>>定时任务开始扫描退款中的订单，并关闭订单");
        orderService.checkRefundStatus();
        return ReturnT.SUCCESS;
    }
}
