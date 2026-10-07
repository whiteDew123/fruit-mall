package com.fruitmall.review.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fruitmall.auth.context.UserContext;
import com.fruitmall.behavior.dto.BehaviorReportDTO;
import com.fruitmall.behavior.service.IBehaviorService;
import com.fruitmall.common.enums.BehaviorTargetTypeEnum;
import com.fruitmall.common.enums.BehaviorTypeEnum;
import com.fruitmall.common.enums.OrderStatusEnum;
import com.fruitmall.common.enums.ReviewStatusEnum;
import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.order.domain.FmOrderItem;
import com.fruitmall.order.domain.FmOrders;
import com.fruitmall.order.mapper.FmOrderItemMapper;
import com.fruitmall.order.mapper.FmOrdersMapper;
import com.fruitmall.review.domain.FmReview;
import com.fruitmall.review.dto.ReviewCreateDTO;
import com.fruitmall.review.mapper.FmReviewMapper;
import com.fruitmall.review.query.ReviewQuery;
import com.fruitmall.review.service.IFmReviewService;
import com.fruitmall.review.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 评价服务实现。
 * 对应一致性规则 R-10：仅已完成订单可评价、一个订单项一次评价（唯一索引 uk_order_item 兜底）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FmReviewServiceImpl extends ServiceImpl<FmReviewMapper, FmReview> implements IFmReviewService {

    private final FmOrderItemMapper orderItemMapper;
    private final FmOrdersMapper ordersMapper;
    private final IBehaviorService behaviorService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long create(ReviewCreateDTO dto) {
        Long memberId = UserContext.getRequiredMemberId();

        FmOrderItem orderItem = orderItemMapper.selectById(dto.getOrderItemId());
        if (orderItem == null) {
            throw new BizException(ResultCode.NOT_FOUND, "订单项不存在");
        }
        FmOrders order = ordersMapper.selectById(orderItem.getOrderId());
        if (order == null || !memberId.equals(order.getMemberId())) {
            throw new BizException(ResultCode.NOT_FOUND, "订单项不存在");
        }
        if (!OrderStatusEnum.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BizException(ResultCode.CONFLICT, "订单完成后才能评价");
        }
        if (Integer.valueOf(1).equals(orderItem.getReviewed())) {
            throw new BizException(ResultCode.CONFLICT, "该商品已经评价过了");
        }

        FmReview review = new FmReview();
        review.setOrderId(order.getId());
        review.setOrderNo(order.getOrderNo());
        review.setOrderItemId(orderItem.getId());
        review.setSpuId(orderItem.getSpuId());
        review.setSkuId(orderItem.getSkuId());
        review.setMemberId(memberId);
        review.setStar(dto.getStar());
        review.setContent(dto.getContent());
        review.setImages(dto.getImages());
        review.setIsAnonymous(dto.getIsAnonymous() == null ? 0 : dto.getIsAnonymous());
        review.setStatus(ReviewStatusEnum.VISIBLE.getCode());
        try {
            this.save(review);
        } catch (DuplicateKeyException e) {
            // 并发下同一订单项重复提交，由唯一索引兜住
            throw new BizException(ResultCode.CONFLICT, "该商品已经评价过了");
        }

        FmOrderItem itemUpdate = new FmOrderItem();
        itemUpdate.setId(orderItem.getId());
        itemUpdate.setReviewed(1);
        orderItemMapper.updateById(itemUpdate);

        // 评价行为埋点放到事务提交后
        Long spuId = orderItem.getSpuId();
        reportReviewBehaviorAfterCommit(spuId);
        log.info("评价提交成功：orderNo={}, spuId={}, star={}", order.getOrderNo(), spuId, dto.getStar());
        return review.getId();
    }

    @Override
    public PageResult<ReviewVO> pageBySpu(ReviewQuery query) {
        // 消费者端只查显示中的评价，不接受前端传状态
        query.setOnlyVisible(true);
        query.setStatus(null);
        return page(query);
    }

    @Override
    public PageResult<ReviewVO> pageAll(ReviewQuery query) {
        return page(query);
    }

    @Override
    public void reply(Long id, String replyContent) {
        if (!StringUtils.hasText(replyContent)) {
            throw new BizException(ResultCode.BAD_REQUEST, "回复内容不能为空");
        }
        FmReview review = requireReview(id);
        FmReview update = new FmReview();
        update.setId(review.getId());
        update.setReplyContent(replyContent);
        update.setReplyTime(LocalDateTime.now());
        this.updateById(update);
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        FmReview review = requireReview(id);
        FmReview update = new FmReview();
        update.setId(review.getId());
        update.setStatus(status);
        this.updateById(update);
    }

    private PageResult<ReviewVO> page(ReviewQuery query) {
        IPage<ReviewVO> page = baseMapper.selectReviewPage(
                new Page<>(query.getPageNum(), query.getPageSize()), query);
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    private FmReview requireReview(Long id) {
        FmReview review = this.getById(id);
        if (review == null) {
            throw new BizException(ResultCode.NOT_FOUND, "评价不存在");
        }
        return review;
    }

    private void reportReviewBehaviorAfterCommit(Long spuId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            reportReviewBehavior(spuId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                reportReviewBehavior(spuId);
            }
        });
    }

    private void reportReviewBehavior(Long spuId) {
        try {
            BehaviorReportDTO dto = new BehaviorReportDTO();
            dto.setBehavior(BehaviorTypeEnum.REVIEW.getCode());
            dto.setTargetType(BehaviorTargetTypeEnum.SPU.getCode());
            dto.setTargetId(spuId);
            behaviorService.report(dto);
        } catch (Exception e) {
            // 埋点失败不影响评价，只记日志
            log.warn("评价行为埋点失败：spuId={}", spuId, e);
        }
    }
}
