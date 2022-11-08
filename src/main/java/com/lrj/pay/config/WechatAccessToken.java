package com.lrj.pay.config;

import lombok.Data;

/**
 * @ClassName: WechatAccessToken
 * @Description: 用于映射微信登录token的对象
 * @Date: 2022/9/15 18:45
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class WechatAccessToken {
    //接口调用凭证
    private String access_token;
    //access_token接口调用凭证超时时间，单位（秒）
    private Integer expires_in;
    //用户刷新access_token
    private String refresh_token;
    //授权用户唯一标识
    private String openid;
    //用户授权的作用域，使用逗号（,）分隔
    private String scope;
    //当且仅当该网站应用已获得该用户的userinfo授权时，才会出现该字段
    private String unionid;
    private Integer errcode;
    private String errmsg;
}