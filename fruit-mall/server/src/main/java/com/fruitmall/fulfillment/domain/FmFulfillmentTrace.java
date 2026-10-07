package com.fruitmall.fulfillment.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 履约轨迹：只追加不修改，构成前台展示的时间轴。 */
@Data
@TableName("fm_fulfillment_trace")
public class FmFulfillmentTrace {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 履约单ID */
    private Long fulfillmentId;

    /** 履约单号 */
    private String fulfillmentNo;

    /** 节点，见 FulfillmentNodeEnum */
    private Integer node;

    /** 节点名称 */
    private String nodeName;

    /** 操作者类型，见 OperatorTypeEnum */
    private Integer operatorType;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人名称 */
    private String operatorName;

    /** 备注或异常原因 */
    private String remark;

    /** 图片地址（JSON 数组） */
    private String images;

    /** 时间轴时间 */
    private LocalDateTime createTime;
}
