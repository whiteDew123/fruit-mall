package com.fruitmall.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fruitmall.common.result.PageResult;
import com.fruitmall.review.domain.FmReview;
import com.fruitmall.review.dto.ReviewCreateDTO;
import com.fruitmall.review.query.ReviewQuery;
import com.fruitmall.review.vo.ReviewVO;

/** 评价服务。 */
public interface IFmReviewService extends IService<FmReview> {

    /**
     * 会员提交评价。
     * 校验：订单必须已完成、一个订单项只能评价一次、订单项必须属于当前会员。
     */
    Long create(ReviewCreateDTO dto);

    /** 商品评价列表（消费者端，只返回显示中的评价） */
    PageResult<ReviewVO> pageBySpu(ReviewQuery query);

    /** 评价列表（后台，可按状态与星级筛选） */
    PageResult<ReviewVO> pageAll(ReviewQuery query);

    /** 商家回复评价 */
    void reply(Long id, String replyContent);

    /** 显示 / 隐藏评价 */
    void changeStatus(Long id, Integer status);
}
