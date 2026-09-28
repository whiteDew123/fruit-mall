package com.fruitmall.common.result;

import lombok.Getter;

/**
 * 统一响应体：{ code, message, data }
 */
@Getter
public class Result<T> {

    /** 响应码，与 HTTP 状态码一致 */
    private final int code;

    /** 提示信息，前端可直接展示 */
    private final String message;

    /** 业务数据，无数据时为 null */
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功，无数据 */
    public static <T> Result<T> ok() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /** 成功，带数据 */
    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /** 成功，自定义提示与数据 */
    public static <T> Result<T> ok(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /** 失败，使用预定义响应码 */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /** 失败，自定义响应码与提示 */
    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
}
