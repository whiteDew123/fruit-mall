package com.fruitmall.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务表公共字段。
 * 审计字段由 MyMetaObjectHandler 自动填充，业务代码不手工赋值；
 * deleted 由 @TableLogic 接管逻辑删除，业务 SQL 不写 deleted = 0。
 */
@Data
public class BaseEntity {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人ID */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人ID */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除：0 未删除 / 1 已删除 */
    @TableLogic
    private Integer deleted;
}
