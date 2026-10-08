package com.fruitmall.recommend.service;

import com.fruitmall.recommend.vo.RecommendItemVO;

import java.util.List;

/** 推荐服务。 */
public interface IRecommendService {

    /**
     * 首页个性化推荐。
     * 已登录会员按画像做内容匹配，游客与新用户自动退化为"时令 + 热销"的冷启动策略。
     *
     * @param limit       返回条数
     * @param anonymousId 游客匿名标识，可为空
     */
    List<RecommendItemVO> recommendHome(int limit, String anonymousId);
}
