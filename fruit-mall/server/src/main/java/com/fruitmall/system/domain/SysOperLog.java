package com.fruitmall.system.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志。日志表只追加不修改，因此不继承 BaseEntity（没有 update_time、deleted）。
 */
@Data
@TableName("sys_oper_log")
public class SysOperLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long userId;

    /** 操作人登录名 */
    private String username;

    /** 模块名 */
    private String module;

    /** 操作动作 */
    private String action;

    /** 目标方法 */
    private String method;

    /** 请求地址 */
    private String requestUrl;

    /** 请求参数，密码类字段已掩码 */
    private String requestParam;

    /** 结果，见 OperLogResultEnum */
    private Integer resultStatus;

    /** 异常信息 */
    private String errorMsg;

    /** 来源IP */
    private String ip;

    /** 耗时（毫秒） */
    private Long costTime;

    /** 操作时间 */
    private LocalDateTime operTime;

    /** 创建时间 */
    private LocalDateTime createTime;
}
