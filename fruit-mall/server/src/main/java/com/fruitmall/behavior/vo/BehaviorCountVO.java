package com.fruitmall.behavior.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 行为统计行：某商品某类行为的次数。 */
@Data
@Schema(description = "行为统计")
public class BehaviorCountVO {

    @Schema(description = "目标商品SPU ID")
    private Long targetId;

    @Schema(description = "行为类型编码")
    private String behavior;

    @Schema(description = "次数")
    private Integer cnt;
}
