package com.lrj.pay.exception;

import com.lrj.pay.enums.ApiResponseEnum;

/**
 * @ClassName: OrderException
 * @Description: 异常类
 * @Date: 2022/8/9 18:09
 * @Author luorenjie
 * @Version V1.0
 * @Since JDK 1.8
 */
public class PayException extends RuntimeException{
    public PayException(RuntimeException e) {
        super(e);
    }

    public PayException(ApiResponseEnum apiResponseEnum, Object errorData) {
        super(apiResponseEnum.getLabel());
    }

    public PayException(ApiResponseEnum apiResponseEnum) {
        super(apiResponseEnum.getLabel());
    }

    public PayException(Throwable cause) {
        super(cause);
    }

    public PayException(String errMsg) {
        super(errMsg);
    }
}
