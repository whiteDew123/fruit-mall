package com.fruitmall.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。后台写操作必须标注，由 OperLogAspect 自动落库 sys_oper_log，
 * 记录操作人、模块、动作、请求参数、结果、耗时与来源 IP。
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperLog {

    /** 模块名，如 系统管理 */
    String module();

    /** 操作动作，如 新增用户 */
    String action();
}
