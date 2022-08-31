package com.lrj.pay.response;

import com.lrj.pay.utils.Pages;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName: BaseRespBody
 * @Description: 带分页的公共响应体
 * @Date: 2022/8/29 15:07
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
@Builder
public class BaseRespBody<R> {
    private Pages page;
    private R resp;
}
