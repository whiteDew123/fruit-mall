package com.fruitmall.common.result;

/**
 * 统一响应码，取值与 HTTP 状态码保持一致。
 * 业务细分原因写在 message 中，前端可直接展示。
 */
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 参数校验失败 */
    BAD_REQUEST(400, "参数校验失败"),
    /** 未登录或登录已过期 */
    UNAUTHORIZED(401, "未登录或登录已过期"),
    /** 无访问权限 */
    FORBIDDEN(403, "无访问权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 状态冲突，如非法状态流转、库存不足 */
    CONFLICT(409, "操作冲突"),
    /** 系统异常 */
    ERROR(500, "系统异常，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
