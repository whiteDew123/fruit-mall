package com.fruitmall.fulfillment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.fulfillment.domain.FmFulfillmentOrder;
import com.fruitmall.fulfillment.query.FulfillmentQuery;
import com.fruitmall.fulfillment.vo.FulfillmentListVO;
import com.fruitmall.fulfillment.vo.FulfillmentVO;
import com.fruitmall.order.domain.FmOrders;

/** 履约服务。 */
public interface IFmFulfillmentOrderService extends IService<FmFulfillmentOrder> {

    /**
     * 支付成功后创建履约单（幂等：同一订单已存在履约单时直接返回）。
     * 同时写入待分拣的起始轨迹节点。
     */
    void createForOrder(FmOrders order);

    /** 会员查看自己订单的履约进度与时间轴 */
    FulfillmentVO getByOrderIdForMember(Long orderId);

    /** 后台履约单分页 */
    PageResult<FulfillmentListVO> pageAll(FulfillmentQuery query);

    /** 后台履约单详情 */
    FulfillmentVO detailForAdmin(Long id);

    /** 分拣完成：履约 待分拣 → 分拣完成，订单 已支付 → 备货中 */
    void pick(Long id, String remark, String images);

    /** 打包完成待配送：履约 分拣完成 → 待配送 */
    void ready(Long id, String remark, String images);

    /** 出库配送：履约 待配送 → 配送中，订单 备货中 → 配送中 */
    void deliver(Long id, String remark, String images);

    /** 送达：履约 配送中 → 已送达 */
    void arrive(Long id, String remark, String images);

    /** 签收：履约 已送达 → 已签收，订单 配送中 → 已完成 */
    void sign(Long id, String remark, String images);

    /** 异常登记：履约转异常并记录原因，订单状态暂不变更 */
    void exception(Long id, String remark, String images);
}
