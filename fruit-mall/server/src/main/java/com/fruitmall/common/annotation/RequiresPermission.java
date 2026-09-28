package com.fruitmall.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能级权限校验注解。
 * 标注在 Controller 方法（或类）上，由 AuthInterceptor 统一校验当前登录用户是否具备该权限标识；
 * 权限标识格式为 {模块}:{操作}，如 product:create。
 * 权限不足时返回 403，不允许只靠前端隐藏按钮。
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {

    /** 权限标识，如 product:create */
    String value();
}
