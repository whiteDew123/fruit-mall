package com.fruitmall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.dto.OrderSubmitDTO;
import com.fruitmall.order.query.OrderQuery;
import com.fruitmall.order.vo.OrderDetailVO;
import com.fruitmall.order.vo.OrderListVO;
import com.fruitmall.order.vo.OrderPreviewVO;
import com.fruitmall.order.vo.OrderSubmitVO;

import java.util.List;

/** 订单服务。 */
public interface IFmOrdersService extends IService<FmOrders> {

    /** 确认订单页数据：默认地址 + 已勾选商品 + 金额，金额按 SKU 实时价重算 */
    OrderPreviewVO preview();

    /**
     * 提交订单。事务内完成：库存条件预占、订单与订单项写入、库存流水写入、购物车清理。
     * 任一商品库存不足则整单回滚，不出现超卖。
     */
    OrderSubmitVO submit(OrderSubmitDTO dto);

    /** 我的订单分页 */
    PageResult<OrderListVO> pageMine(OrderQuery query);

    /** 我的订单详情 */
    OrderDetailVO detailMine(Long orderId);

    /** 会员取消订单：仅待支付可取消，取消后释放预占库存 */
    void cancelByMember(Long orderId, String reason);

    /** 后台订单分页 */
    PageResult<OrderListVO> pageAll(OrderQuery query);

    /** 后台订单详情 */
    OrderDetailVO detailForAdmin(Long orderId);

    /** 后台修改订单备注 */
    void updateAdminRemark(Long orderId, String remark);

    /** 后台取消订单：仅待支付可取消 */
    void cancelByAdmin(Long orderId, String reason);

    /** 查询已超时未支付的订单，供定时任务扫描 */
    List<FmOrders> listExpiredOrders(int limit);

    /** 关闭单笔超时订单（定时任务调用），非待支付状态直接跳过 */
    void closeExpiredOrder(Long orderId);
}
