package com.lrj.pay.response;

import com.alibaba.fastjson.JSON;
import com.lrj.pay.enums.ApiResponseEnum;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;

public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 5241526151768786394L;

    private final String version = "1.0.0";
    private boolean result;
    private String message = "";
    private int code;
    private T data;
    private Long serverTime;

    public ApiResponse() {
        this.setApiResponseEnum(ApiResponseEnum.SUCCESS);
    }

    private ApiResponse(T t) {
        this();
        if(t instanceof ApiResponseEnum){
            this.setMessage(((ApiResponseEnum) t).getLabel());
        }
        this.data = t;
    }

    private ApiResponse(ApiResponseEnum result) {
        this.setApiResponseEnum(result);
    }

    private ApiResponse(ApiResponseEnum result, T t) {
        this.setApiResponseEnum(result);
        this.data = t;
    }

    private ApiResponse(String message, T t) {
        this.result = true;
        this.message = message;
        this.data = t;
    }

    public static <T> ApiResponse<T> returnSuccess() {
        return new ApiResponse();
    }

    public static <T> ApiResponse<T> returnSuccess(T data) {
        return new ApiResponse(ApiResponseEnum.SUCCESS, data);
    }

    public static ApiResponse returnSuccess(String message) {
        ApiResponse ApiResponse = new ApiResponse();
        ApiResponse.setMessage(message);
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnSuccess(T data, ApiResponseEnum result) {
        return new ApiResponse(result, data);
    }

    public static <T> ApiResponse<T> returnSuccess(T data, String successMessage) {
        return new ApiResponse(successMessage, data);
    }

    public static <T> ApiResponse<T> returnSuccess(ApiResponseEnum result) {
        return new ApiResponse(result);
    }

    public static <T> ApiResponse<T> returnSuccess(ApiResponseEnum result, String appendSuccessMessage) {
        ApiResponse ApiResponse = returnSuccess();
        ApiResponse.setMessage(result.getLabel());
        if (appendSuccessMessage != null) {
            ApiResponse.message = ApiResponse.message + "（" + appendSuccessMessage + "）";
        }
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnFail(
            ApiResponseEnum result, String appendErrorMessage, T data) {
        ApiResponse ApiResponse = returnFail(result, appendErrorMessage);
        ApiResponse.setData(data);
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnFail(ApiResponseEnum result, String appendErrorMessage) {
        ApiResponse ApiResponse = returnFail(result);
        if (appendErrorMessage != null) {
            ApiResponse.message = ApiResponse.message + "(" + appendErrorMessage + ")";
        }
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnFail(ApiResponseEnum result) {
        ApiResponse ApiResponse = new ApiResponse();
        ApiResponse.result = false;
        ApiResponse.message = result.getLabel();
        ApiResponse.code = result.getId();
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnFail(String errorMessage) {
        ApiResponse ApiResponse = new ApiResponse();
        ApiResponse.result = false;
        ApiResponse.message = errorMessage;
        ApiResponse.code = ApiResponseEnum.FAIL.getId();
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnFail(T data, ApiResponseEnum result) {
        return returnSuccess(data, result);
    }

    public static <T> ApiResponse<T> returnFail(T data, String errorMessage) {
        return returnSuccess(data, errorMessage);
    }

    public static <T> ApiResponse<T> returnFail(int errorCode, String errorMessage) {
        ApiResponse ApiResponse = new ApiResponse();
        ApiResponse.code = errorCode;
        ApiResponse.message = errorMessage;
        ApiResponse.result = false;
        return ApiResponse;
    }

    public static <T> ApiResponse<T> returnSuccess(int errorCode, String errorMessage, T data) {
        ApiResponse ApiResponse = new ApiResponse();
        ApiResponse.code = errorCode;
        ApiResponse.message = errorMessage;
        ApiResponse.result = true;
        ApiResponse.setData(data);

        return ApiResponse;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = System.currentTimeMillis();
    }

    public String getVersion() {
        return version;
    }

    public boolean isResult() {
        return result;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setApiResponseEnum(ApiResponseEnum apiResponseEnum) {
        this.result = apiResponseEnum.isSuccess();
        this.code = apiResponseEnum.getId();
        this.message = apiResponseEnum.getLabel();
        this.serverTime = System.currentTimeMillis();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((StringUtils.isEmpty(message)) ? 0 : message.hashCode());
        result = prime * result + (this.result ? 1231 : 1237);
        result = prime * result + code;
        result = prime * result + version.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApiResponse<?> that = (ApiResponse<?>) o;

        if (code != that.code) {
            return false;
        }
        if (result != that.result) {
            return false;
        }
        if (!Objects.equals(data, that.data)) {
            return false;
        }
        return Objects.equals(message, that.message);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
