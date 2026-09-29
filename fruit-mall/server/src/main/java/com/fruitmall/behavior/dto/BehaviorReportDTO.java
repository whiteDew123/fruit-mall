package com.fruitmall.behavior.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 行为埋点上报入参。 */
@Data
@Schema(description = "行为埋点上报入参")
public class BehaviorReportDTO {

    @Schema(description = "行为类型：VIEW / SEARCH / FAVORITE / CART / ORDER / REVIEW / DISLIKE",
            example = "VIEW")
    @NotBlank(message = "行为类型不能为空")
    private String behavior;

    @Schema(description = "目标类型：SPU / SKU / CATEGORY / KEYWORD", example = "SPU")
    private String targetType;

    @Schema(description = "目标ID，关键词类型可传 0", example = "1")
    private Long targetId;

    @Schema(description = "会话ID，未登录用户用于串联行为")
    @Size(max = 64, message = "会话ID长度不能超过 64")
    private String sessionId;

    @Schema(description = "匿名标识，未登录用户使用")
    @Size(max = 64, message = "匿名标识长度不能超过 64")
    private String anonymousId;

    @Schema(description = "上下文 JSON：来源页面、关键词、位置",
            example = "{\"scene\":\"HOME\",\"keyword\":\"芒果\"}")
    private String contextJson;
}
