package com.fruitmall.behavior.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 用户行为日志：推荐系统的数据源，只追加不修改。 */
@Data
@TableName("fm_user_behavior")
public class FmUserBehavior {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 会员ID，未登录为 null */
    private Long memberId;

    /** 匿名标识 */
    private String anonymousId;

    /** 会话ID */
    private String sessionId;

    /** 行为类型，见 BehaviorTypeEnum */
    private String behavior;

    /** 目标类型，见 BehaviorTargetTypeEnum */
    private String targetType;

    /** 目标ID */
    private Long targetId;

    /** 上下文 JSON：来源页面、关键词、位置 */
    private String contextJson;

    /** 发生时间 */
    private LocalDateTime createTime;
}
