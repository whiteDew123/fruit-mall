package com.fruitmall.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.system.domain.SysOperLog;
import com.fruitmall.system.mapper.SysOperLogMapper;
import com.fruitmall.system.service.ISysOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现。
 */
@Slf4j
@Service
public class SysOperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog>
        implements ISysOperLogService {

    @Async("operLogExecutor")
    @Override
    public void saveLogAsync(SysOperLog operLog) {
        try {
            this.save(operLog);
        } catch (Exception e) {
            // 日志写入失败不能影响业务，这里只记录错误
            log.error("操作日志写入失败：module={}, action={}", operLog.getModule(), operLog.getAction(), e);
        }
    }
}
