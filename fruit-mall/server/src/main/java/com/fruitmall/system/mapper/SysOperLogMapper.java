package com.fruitmall.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.system.domain.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper。
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {
}
