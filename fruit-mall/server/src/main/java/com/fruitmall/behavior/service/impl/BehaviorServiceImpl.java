package com.fruitmall.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.behavior.domain.FmUserBehavior;
import com.fruitmall.behavior.dto.BehaviorReportDTO;
import com.fruitmall.behavior.mapper.FmUserBehaviorMapper;
import com.fruitmall.behavior.service.IBehaviorService;
import com.fruitmall.common.enums.BehaviorTypeEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/** 用户行为埋点服务实现。 */
@Slf4j
@Service
public class BehaviorServiceImpl extends ServiceImpl<FmUserBehaviorMapper, FmUserBehavior>
        implements IBehaviorService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void report(BehaviorReportDTO dto) {
        BehaviorTypeEnum behaviorType = BehaviorTypeEnum.of(dto.getBehavior());
        if (behaviorType == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "不支持的行为类型：" + dto.getBehavior());
        }
        FmUserBehavior behavior = new FmUserBehavior();
        behavior.setMemberId(UserContext.getUserId());
        behavior.setAnonymousId(dto.getAnonymousId());
        behavior.setSessionId(dto.getSessionId());
        behavior.setBehavior(behaviorType.getCode());
        behavior.setTargetType(StringUtils.hasText(dto.getTargetType()) ? dto.getTargetType() : "SPU");
        behavior.setTargetId(dto.getTargetId());
        behavior.setContextJson(normalizeJson(dto.getContextJson()));
        behavior.setCreateTime(LocalDateTime.now());
        this.save(behavior);
    }

    private String normalizeJson(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readTree(json).toString();
        } catch (Exception e) {
            // 上下文格式错误不阻断埋点，原样截断保存，便于排查
            log.warn("埋点上下文不是合法 JSON：{}", json);
            return json.length() <= 500 ? json : json.substring(0, 500);
        }
    }
}
