package com.lrj.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户订单表
 * </p>
 *
 * @author lrj
 * @since 2022-08-221 10:20:03
 */
@Getter
@Setter
@TableName("tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 订单编号
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 交易流水号
     */
    @TableField("trade_no")
    private String tradeNo;

    /**
     * 产品id
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 原价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 实付金额
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 购买类型（1新购，2续费）
     */
    @TableField("consume_type")
    private Integer consumeType;

    /**
     * 购买时长
     */
    @TableField("consume_num")
    private Double consumeNum;

    /**
     * 时间单位（1秒，2分，3时，4天，5周，6月，7年）
     */
    @TableField("time_type")
    private Integer timeType;

    /**
     * 支付类型（1支付宝，2微信）
     */
    @TableField("pay_type")
    private Integer payType;

    /**
     * 支付状态（0待支付，1已支付，2支付失败，3已取消，4退款中，5已退款，6退款失败）
     */
    @TableField("`status`")
    private Integer status;

    /**
     * 跳转链接
     */
    @TableField("`jump_url`")
    private String jumpUrl;

    /**
     * 并发数
     */
    @TableField("concurrent_num")
    private Integer concurrentNum;

    /**
     * 创建时间
     */
    @TableField("cerate_time")
    private LocalDateTime cerateTime;

    /**
     * 开始时间
     */
    @TableField("begin_time")
    private LocalDateTime beginTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;


}
