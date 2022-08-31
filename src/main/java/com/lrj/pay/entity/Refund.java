package com.lrj.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 订单退款记录表
 * </p>
 *
 * @author lrj
 * @since 2022-08-231 10:53:08
 */
@Getter
@Setter
@TableName("tb_refund")
public class Refund implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单id
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 退款订单号
     */
    @TableField("refund_no")
    private String refundNo;

    /**
     * 退款流水号
     */
    @TableField("refund_trade_no")
    private String refundTradeNo;

    /**
     * 状态（0退款中，1退款成功，2退款失败）
     */
    @TableField("`status`")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 退款成功时间
     */
    @TableField("success_time")
    private LocalDateTime successTime;


}
