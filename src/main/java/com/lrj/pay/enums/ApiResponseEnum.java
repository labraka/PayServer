package com.lrj.pay.enums;

public enum ApiResponseEnum {

    SUCCESS(200, "success", "成功", true),
    FAIL(100, "fail", "失败", false),
    FORBIDDEN(403, "forbidden", "没有权限", false),
    RESOURCE_NOT_FOUND(404, "resource_not_found", "资源不存在", false),
    INTERNAL_ERROR(500, "internal_error", "服务器处理失败", false),

    PARAMETER_INVALID(600, "parameter_invalid", "非法参数", false),
    PARAMETER_CANT_BE_EMPTY(601, "parameter_cant_be_empty", "缺少必要参数", false),
    NEED_USER_LOGIN(602, "need_user_login", "需要用户登录", false),
    ILLEGAL_PROTOCOL(603, "illegal_protocol", "登录过期，请重新登录", false),
    VALIDATE_CODE_ERROR(604, "validate_code_error", "手机验证码错误", false),
    VALIDATE_SEND_CODE_ERROR(607, "validate_send_code_error", "验证码发送失败", false),
    SIGNATURE_EXPIRED(610, "signature_expired", "签名过期", false),
    SIGNATURE_INVALID(611, "signature_invalid", "非法签名", false),
    USER_NOT_FOUND(612, "user_not_found", "用户不存在", false),
    USER_LOW_LEVEL(613, "user_low_level", "用户等级过低", false),


    //-------------------------------order,start at 1000--------------------------------------
    MONEY_ERROR(1000, "money_error", "金额错误", false),
    PRODUCT_NONE(1001, "product_none", "产品不存在", false),
    PAY_FAIL(1002, "pay_fail", "支付失败", false),
    PAY_TYPE_ERROR(1003, "pay_type_error", "支付方式错误", false),
    REFUND_FAIL(1004, "refund_fail", "退款失败", false),
    CLOSE_FAIL(1005, "close_fail", "关闭订单失败", false),

    ;

    protected int id;

    protected String code;

    protected String label;

    protected boolean success;

    ApiResponseEnum(int id, String code, String label, boolean success) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.success = success;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
