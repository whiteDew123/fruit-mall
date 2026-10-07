package com.fruitmall.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fruitmall.review.domain.FmReview;
import com.fruitmall.review.query.ReviewQuery;
import com.fruitmall.review.vo.ReviewVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 评价 Mapper。
 * 列表查询连带会员昵称，匿名评价统一显示为「匿名用户」，避免泄露会员身份。
 */
@Mapper
public interface FmReviewMapper extends BaseMapper<FmReview> {

    @Select("""
            <script>
            SELECT r.id, r.order_no, r.spu_id, r.sku_id, r.star, r.content, r.images,
                   r.is_anonymous, r.status, r.reply_content, r.reply_time, r.create_time,
                   CASE WHEN r.is_anonymous = 1 THEN '匿名用户'
                        ELSE IFNULL(m.nickname, '已注销用户') END AS member_nickname,
                   s.spu_name
              FROM fm_review r
              LEFT JOIN fm_member m ON m.id = r.member_id AND m.deleted = 0
              LEFT JOIN fm_product_spu s ON s.id = r.spu_id
             WHERE r.deleted = 0
               <if test="q.spuId != null">AND r.spu_id = #{q.spuId}</if>
               <if test="q.star != null">AND r.star = #{q.star}</if>
               <if test="q.status != null">AND r.status = #{q.status}</if>
               <if test="q.onlyVisible">AND r.status = 10</if>
             ORDER BY r.id DESC
            </script>
            """)
    IPage<ReviewVO> selectReviewPage(IPage<ReviewVO> page, @Param("q") ReviewQuery query);
}
