package com.lrj.pay.request;

import com.lrj.pay.utils.Pages;
import lombok.Data;

/**
 * @ClassName: BaseReqBody
 * @Description: 带分页的公共请求体
 * @Date: 2022/8/29 14:32
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
@Data
public class BaseReqBody<R> {
    private Pages page;
    private R req;

    /**
     * 初始化请求参数
     *
     * @author: luorenjie
     * @date: 2022/8/31 14:28
     * @return: com.lrj.pay.request.BaseReqBody
     */
    public static BaseReqBody init() {
        BaseReqBody reqBody = new BaseReqBody();
        Pages pages = new Pages();
        pages.setPageNo(1);
        reqBody.setPage(pages);
        return reqBody;
    }
}
