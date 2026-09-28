package com.fruitmall.system.query;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 系统用户列表查询条件。
 * 分页参数从 1 开始，pageSize 默认 10、上限 100。
 */
@Data
@Schema(description = "系统用户查询条件")
public class SysUserQuery {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，上限 100", example = "10")
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

    @Schema(description = "登录名，模糊匹配")
    private String username;

    @Schema(description = "状态：10 正常 / 20 禁用")
    private Integer status;
}
