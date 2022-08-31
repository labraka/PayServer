package com.lrj.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrj.pay.entity.Refund;
import com.lrj.pay.enums.RefundStatusEnum;
import com.lrj.pay.mapper.RefundMapper;
import com.lrj.pay.service.RefundService;
import com.lrj.pay.utils.DateUtil;
import com.lrj.pay.utils.SnowFlakeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 订单退款记录表 服务实现类
 * </p>
 *
 * @author lrj
 * @since 2022-08-21 10:53:08
 */
@Service
public class RefundServiceImpl extends ServiceImpl<RefundMapper, Refund> implements RefundService {

    @Autowired
    private RefundMapper refundMapper;

    private static final String PRFIX = "RL";

    @Override
    public Refund createRefundOnce(Long orderId) {
        Refund refund = refundMapper.selectOne(new LambdaQueryWrapper<Refund>().eq(Refund::getOrderId, orderId));
        if (!ObjectUtils.isEmpty(refund)) {
            return refund;
        }
        refund = new Refund();
        refund.setOrderId(orderId);
        SnowFlakeUtil idWorker = new SnowFlakeUtil(0, 0);
        long id = idWorker.nextId();
        String perDate = DateUtil.dateToStr(LocalDateTime.now(), "yyMMdd");
        String refundNo = perDate + id;
        refund.setRefundNo(PRFIX + refundNo);
        refundMapper.insert(refund);
        return refund;
    }

    @Override
    public void updateRefund(Long id, int status) {
        Refund refund = new Refund();
        refund.setStatus(1);
        refund.setSuccessTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        refundMapper.update(refund, new LambdaQueryWrapper<Refund>().eq(Refund::getId, id));
    }

    @Override
    public void updateRefund(Refund refund, Long id) {
        refundMapper.update(refund, new LambdaQueryWrapper<Refund>().eq(Refund::getId, id));
    }

    @Override
    public List<Refund> getRefundings() {
        LambdaQueryWrapper<Refund> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Refund::getStatus, RefundStatusEnum.REFUNDING.getType());
        List<Refund> refunds = refundMapper.selectList(oqw);
        return refunds;
    }

    @Override
    public Refund getRefundByOrderId(Long orderId) {
        LambdaQueryWrapper<Refund> oqw = new LambdaQueryWrapper<>();
        oqw.eq(Refund::getOrderId, orderId);
        Refund refund = refundMapper.selectOne(oqw);
        return refund;
    }
}
