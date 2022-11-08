package com.lrj.pay.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("tb_customer")
public class Customer implements Serializable{
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    @TableField("username")
    private String username ;
    /**
     * 手机号
     */
    @TableField("phone")
    private String phone ;
    /**
     * 账户类型（0普通，1vip）
     */
    @TableField("level")
    private Integer level ;

    /**
     * 主控并发数
     */
    @TableField("master_control_num")
    private Integer masterControlNum ;

    /**
     * 注册时间
     */
    @TableField("create_time")
    private LocalDateTime createTime ;

    /**
     *首次购买时间
     */
    @TableField("vip_first_time")
    private LocalDateTime vipFirstTime ;

    /**
     * 到期时间
     */
    @TableField("vip_end_time")
    private LocalDateTime vipEndTime ;

}