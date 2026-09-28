package com.fruitmall.common.exception;

import com.fruitmall.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常。
 * 默认按 409（状态冲突）返回，例如非法状态流转、库存不足、商品已下架；
 * 参数类问题传入 400，归属校验失败传入 403。
 */
@Getter
public class BizException extends RuntimeException {

    /** 响应码 */
    private final int code;

    public BizException(String message) {
        this(ResultCode.CONFLICT.getCode(), message);
    }

    public BizException(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMessage());
    }

    public BizException(ResultCode resultCode, String message) {
        this(resultCode.getCode(), message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
