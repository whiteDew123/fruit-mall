package com.fruitmall.common.exception;

import com.fruitmall.common.result.Result;
import com.fruitmall.common.result.ResultCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理。
 * 分类处理参数校验、业务、资源不存在、系统异常，统一返回 { code, message, data }；
 * 堆栈只进日志，不返回给前端。
 * 注意：响应体中的 code 必须与 HTTP 状态码保持一致（见 AGENTS.md 接口与数据契约），
 * 因此这里用 ResponseEntity 显式设置状态码，而不是只把 code 写进响应体。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：响应码由异常自身携带，默认 409 */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result<Void>> handleBizException(BizException e) {
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
        return buildResponse(e.getCode(), e.getMessage());
    }

    /** DTO 校验失败（@RequestBody 与 @ModelAttribute 都走这里） */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Void>> handleBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null
                ? ResultCode.BAD_REQUEST.getMessage()
                : fieldError.getField() + " " + fieldError.getDefaultMessage();
        log.warn("参数校验失败：{}", message);
        return buildResponse(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 方法参数校验失败（@RequestParam / @PathVariable 上的约束注解） */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .orElse(ResultCode.BAD_REQUEST.getMessage());
        log.warn("参数校验失败：{}", message);
        return buildResponse(ResultCode.BAD_REQUEST.getCode(), message);
    }

    /** 请求体无法解析，例如 JSON 格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败：{}", e.getMessage());
        return buildResponse(ResultCode.BAD_REQUEST.getCode(), "请求体格式错误");
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<Void>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("请求方法不支持：{}", e.getMessage());
        return buildResponse(ResultCode.BAD_REQUEST.getCode(), "请求方法不支持：" + e.getMethod());
    }

    /** 资源不存在：未匹配到任何接口或静态资源 */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<Void>> handleNoResourceFoundException(NoResourceFoundException e) {
        log.warn("资源不存在：{}", e.getResourcePath());
        return buildResponse(ResultCode.NOT_FOUND.getCode(), ResultCode.NOT_FOUND.getMessage());
    }

    /** 兜底：未预期异常，堆栈只进日志不返回前端 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleException(Exception e) {
        log.error("系统异常", e);
        return buildResponse(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage());
    }

    /**
     * 按响应码构造 HTTP 状态，保证响应体中的 code 与 HTTP 状态码一致。
     * 出现规范外的响应码时退化为 500，避免抛出非法状态异常。
     */
    private ResponseEntity<Result<Void>> buildResponse(int code, String message) {
        HttpStatus status = HttpStatus.resolve(code);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status).body(Result.fail(code, message));
    }
}
