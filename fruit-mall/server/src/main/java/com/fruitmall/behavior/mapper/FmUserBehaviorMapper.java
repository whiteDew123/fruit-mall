package com.fruitmall.behavior.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fruitmall.behavior.domain.FmUserBehavior;
import com.fruitmall.behavior.vo.BehaviorCountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/** 用户行为日志 Mapper。 */
@Mapper
public interface FmUserBehaviorMapper extends BaseMapper<FmUserBehavior> {

    /**
     * 按商品与行为类型统计行为次数，供推荐模块计算热度。
     * 行为权重不写死在 SQL 里，由 Java 侧读取 BehaviorTypeEnum 的权重计算，
     * 保证权重的唯一定义处仍在枚举类中。
     */
    @Select("""
            SELECT target_id AS target_id, behavior AS behavior, COUNT(*) AS cnt
              FROM fm_user_behavior
             WHERE target_type = 'SPU'
               AND target_id IS NOT NULL
               AND create_time >= #{since}
             GROUP BY target_id, behavior
            """)
    List<BehaviorCountVO> countByTargetAndBehavior(@Param("since") LocalDateTime since);
}
