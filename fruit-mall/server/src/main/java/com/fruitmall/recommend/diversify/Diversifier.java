package com.fruitmall.recommend.diversify;

import com.fruitmall.recommend.rank.RankedItem;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多样性打散：按分数降序遍历，同一品类最多先取若干个，其余进入补位队列。
 * 这样 Top-N 不会集中在单一品类，能明显提升"覆盖率"与"长尾曝光占比"两项实验指标。
 */
@Component
public class Diversifier {

    /** 同一品类在结果中优先保留的个数 */
    private static final int MAX_PER_CATEGORY = 2;

    public List<RankedItem> diversify(List<RankedItem> rankedItems, int limit) {
        List<RankedItem> selected = new ArrayList<>(limit);
        Deque<RankedItem> deferred = new ArrayDeque<>();
        Map<Long, Integer> categoryCount = new HashMap<>();

        for (RankedItem item : rankedItems) {
            if (selected.size() >= limit) {
                break;
            }
            Long categoryId = item.getFeature().getCategoryId() == null
                    ? -1L : item.getFeature().getCategoryId();
            int count = categoryCount.getOrDefault(categoryId, 0);
            if (count >= MAX_PER_CATEGORY) {
                deferred.add(item);
                continue;
            }
            selected.add(item);
            categoryCount.merge(categoryId, 1, Integer::sum);
        }

        // 候选不足时，用被品类限制挤出的高分商品按序补位，保证结果条数
        while (selected.size() < limit && !deferred.isEmpty()) {
            selected.add(deferred.poll());
        }
        return selected;
    }
}
