package com.fruitmall.behavior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.behavior.domain.FmUserBehavior;
import org.apache.ibatis.annotations.Mapper;

/** 用户行为日志 Mapper。 */
@Mapper
public interface FmUserBehaviorMapper extends BaseMapper<FmUserBehavior> {
}
