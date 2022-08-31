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
 * 产品表
 * </p>
 *
 * @author lrj
 * @since 2022-08-223 16:57:21
 */
@Getter
@Setter
@TableName("tb_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 产品名称
     */
    @TableField("`name`")
    private String name;

    /**
     * 单价：元/并发数/时间单位
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 时间单位（1秒，2分，3时，4天，5周，6月，7年）
     */
    @TableField("unit")
    private Integer unit;

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
     * 删除时间
     */
    @TableField("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 是否删除（0否，1是）
     */
    @TableField("is_delete")
    private Integer isDelete;


}
