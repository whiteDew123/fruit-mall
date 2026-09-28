package com.fruitmall.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.system.domain.SysOperLog;

/**
 * 操作日志服务。
 */
public interface ISysOperLogService extends IService<SysOperLog> {

    /**
     * 异步写入操作日志，写失败只记日志，不影响业务主流程
     */
    void saveLogAsync(SysOperLog operLog);
}
