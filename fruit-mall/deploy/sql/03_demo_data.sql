-- =============================================================================
-- 基于推荐算法的水果商城 —— 演示数据
-- 用途：为推荐算法与前台展示准备一批可解释的样例数据（多品类商品 + 差异化用户行为）
-- 执行方式：mysql -uroot -p fruit_mall -e "source .../03_demo_data.sql"
-- 说明：全部使用显式主键 + ON DUPLICATE KEY UPDATE，可重复执行不产生重复数据；
--       行为埋点带 {"source":"demo"} 标记，重跑前会先清理本脚本写入的演示行为
-- =============================================================================

USE `fruit_mall`;
SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 一、数值型特色属性（推荐特征向量的维度来源，缺一个维度向量就少一维）
-- -----------------------------------------------------------------------------
INSERT INTO `fm_product_attr_def` (`id`, `attr_code`, `attr_name`, `data_type`, `unit`,
                                   `min_value`, `max_value`, `required`, `sort`, `status`)
VALUES (1, 'SWEETNESS', '甜度', 10, '级', 1, 5, 1, 1, 10),
       (3, 'SOURNESS', '酸度', 10, '级', 1, 5, 0, 2, 10),
       (4, 'CRISPNESS', '脆度', 10, '级', 1, 5, 0, 3, 10),
       (5, 'SUGAR_BRIX', '糖度', 10, 'Brix', 8, 25, 0, 4, 10)
ON DUPLICATE KEY UPDATE `attr_name` = VALUES(`attr_name`),
                        `unit`      = VALUES(`unit`),
                        `min_value` = VALUES(`min_value`),
                        `max_value` = VALUES(`max_value`),
                        `required`  = VALUES(`required`),
                        `sort`      = VALUES(`sort`),
                        `status`    = VALUES(`status`);

-- -----------------------------------------------------------------------------
-- 二、分类（一级分组 + 二级品类，供多样性打散与品类占比统计使用）
-- -----------------------------------------------------------------------------
INSERT INTO `fm_category` (`id`, `parent_id`, `category_name`, `category_code`, `level`, `sort`, `status`)
VALUES (1, 0, '热带水果', 'TROPICAL', 1, 1, 10),
       (2, 1, '芒果', 'MANGO', 2, 1, 10),
       (3, 1, '荔枝', 'LYCHEE', 2, 2, 10),
       (4, 0, '温带水果', 'TEMPERATE', 1, 2, 10),
       (5, 4, '苹果', 'APPLE', 2, 1, 10),
       (6, 4, '柑橘', 'CITRUS', 2, 2, 10),
       (7, 0, '进口水果', 'IMPORTED', 1, 3, 10),
       (8, 7, '车厘子', 'CHERRY', 2, 1, 10),
       (9, 0, '瓜果', 'MELON', 1, 4, 10)
ON DUPLICATE KEY UPDATE `parent_id`     = VALUES(`parent_id`),
                        `category_name` = VALUES(`category_name`),
                        `level`         = VALUES(`level`),
                        `sort`          = VALUES(`sort`),
                        `status`        = VALUES(`status`);

-- -----------------------------------------------------------------------------
-- 三、商品（SPU）
--     应季月份特意覆盖当前月份（10 月）与其它月份，便于演示时令召回
-- -----------------------------------------------------------------------------
INSERT INTO `fm_product_spu` (`id`, `spu_code`, `category_id`, `spu_name`, `subtitle`,
                              `origin_place`, `season_months`, `storage_condition`, `shelf_life_days`,
                              `unit`, `main_image`, `detail`, `sales_count`, `status`, `sort`)
VALUES (1, 'SPU-MANGO-001', 2, '海南小台农芒果', '树上熟 甜度高 果核薄',
        '海南三亚', '5,6,7', '阴凉通风处', 7, '斤', '/api/files/demo/mango.jpg',
        '<p>海南小台农芒果，皮薄核小，甜度高。</p>', 32, 20, 1),
       (2, 'SPU-LYCHEE-001', 3, '海南妃子笑荔枝', '现摘直发 清甜多汁',
        '海南海口', '6,7', '冷藏保存', 5, '斤', '/api/files/demo/lychee.jpg',
        '<p>妃子笑荔枝，果肉饱满，清甜带香。</p>', 18, 20, 2),
       (3, 'SPU-APPLE-001', 5, '新疆阿克苏苹果', '冰糖心 脆甜爽口',
        '新疆阿克苏', '9,10,11', '阴凉通风处', 30, '箱', '/api/files/demo/apple.jpg',
        '<p>阿克苏冰糖心苹果，口感脆甜。</p>', 45, 20, 3),
       (4, 'SPU-ORANGE-001', 6, '赣南脐橙', '酸甜适口 汁水足',
        '江西赣州', '11,12,1', '阴凉通风处', 20, '箱', '/api/files/demo/orange.jpg',
        '<p>赣南脐橙，酸甜平衡，汁水丰富。</p>', 26, 20, 4),
       (5, 'SPU-CHERRY-001', 8, '智利车厘子', '空运直达 果肉紧实',
        '智利', '12,1,2', '冷藏保存', 10, '盒', '/api/files/demo/cherry.jpg',
        '<p>智利车厘子，果大味甜，脆度好。</p>', 12, 20, 5),
       (6, 'SPU-MELON-001', 9, '新疆哈密瓜', '香甜多汁 瓜香浓郁',
        '新疆哈密', '8,9,10', '阴凉通风处', 12, '个', '/api/files/demo/melon.jpg',
        '<p>新疆哈密瓜，糖度高，香气足。</p>', 21, 20, 6)
ON DUPLICATE KEY UPDATE `category_id`       = VALUES(`category_id`),
                        `spu_name`          = VALUES(`spu_name`),
                        `subtitle`          = VALUES(`subtitle`),
                        `origin_place`      = VALUES(`origin_place`),
                        `season_months`     = VALUES(`season_months`),
                        `storage_condition` = VALUES(`storage_condition`),
                        `shelf_life_days`   = VALUES(`shelf_life_days`),
                        `unit`              = VALUES(`unit`),
                        `main_image`        = VALUES(`main_image`),
                        `detail`            = VALUES(`detail`),
                        `sales_count`       = VALUES(`sales_count`),
                        `status`            = VALUES(`status`),
                        `sort`              = VALUES(`sort`);

-- -----------------------------------------------------------------------------
-- 四、规格（SKU）：价格与库存挂在规格上
-- -----------------------------------------------------------------------------
INSERT INTO `fm_product_sku` (`id`, `spu_id`, `sku_code`, `spec_name`, `spec_json`, `price`,
                              `original_price`, `stock`, `locked_stock`, `warn_stock`, `sales_count`, `status`, `version`)
VALUES (1, 1, 'MANGO-5J', '5斤装', '{"净重":"5斤"}', 59.90, 79.00, 100, 0, 10, 20, 10, 0),
       (2, 1, 'MANGO-10J', '10斤装', '{"净重":"10斤"}', 109.00, 139.00, 50, 0, 10, 12, 10, 0),
       (3, 2, 'LYCHEE-3J', '3斤装', '{"净重":"3斤"}', 89.00, 108.00, 60, 0, 10, 18, 10, 0),
       (4, 3, 'APPLE-10J', '10斤箱装', '{"净重":"10斤"}', 29.90, 39.90, 200, 0, 20, 45, 10, 0),
       (5, 4, 'ORANGE-10J', '10斤箱装', '{"净重":"10斤"}', 39.90, 49.90, 150, 0, 20, 26, 10, 0),
       (6, 5, 'CHERRY-2J', '2斤礼盒', '{"净重":"2斤"}', 129.00, 159.00, 40, 0, 5, 12, 10, 0),
       (7, 6, 'MELON-1', '单果约4斤', '{"净重":"4斤"}', 49.90, 59.90, 80, 0, 10, 21, 10, 0)
ON DUPLICATE KEY UPDATE `spec_name`      = VALUES(`spec_name`),
                        `spec_json`      = VALUES(`spec_json`),
                        `price`          = VALUES(`price`),
                        `original_price` = VALUES(`original_price`),
                        `stock`          = VALUES(`stock`),
                        `warn_stock`     = VALUES(`warn_stock`),
                        `sales_count`    = VALUES(`sales_count`),
                        `status`         = VALUES(`status`);

-- -----------------------------------------------------------------------------
-- 五、特色属性取值：推荐特征向量的直接来源
--     甜度/酸度/脆度用 1-5 级，糖度用 Brix 实测值，量纲不同但会统一做 min-max 归一化
-- -----------------------------------------------------------------------------
INSERT INTO `fm_product_attr_value` (`id`, `spu_id`, `attr_def_id`, `attr_value`, `num_value`)
VALUES (1, 1, 1, '5', 5), (2, 1, 3, '2', 2), (3, 1, 4, '1', 1), (4, 1, 5, '18', 18),
       (5, 2, 1, '5', 5), (6, 2, 3, '2', 2), (7, 2, 4, '2', 2), (8, 2, 5, '19', 19),
       (9, 3, 1, '4', 4), (10, 3, 3, '2', 2), (11, 3, 4, '5', 5), (12, 3, 5, '15', 15),
       (13, 4, 1, '4', 4), (14, 4, 3, '4', 4), (15, 4, 4, '2', 2), (16, 4, 5, '14', 14),
       (17, 5, 1, '5', 5), (18, 5, 3, '3', 3), (19, 5, 4, '4', 4), (20, 5, 5, '20', 20),
       (21, 6, 1, '5', 5), (22, 6, 3, '1', 1), (23, 6, 4, '3', 3), (24, 6, 5, '17', 17)
ON DUPLICATE KEY UPDATE `attr_value` = VALUES(`attr_value`),
                        `num_value`  = VALUES(`num_value`);

-- -----------------------------------------------------------------------------
-- 六、演示用行为埋点
--     两位会员口味刻意不同，用于验证"千人千面"：
--       会员 1（fruitfan）：偏好高甜、热带水果（芒果、荔枝、哈密瓜）
--       会员 2（fruitfan2）：偏好脆爽、酸甜（苹果、脐橙）
--     行为时间分散在最近两周内，用于验证时间衰减
-- -----------------------------------------------------------------------------
DELETE FROM `fm_user_behavior`
 WHERE `context_json` IS NOT NULL
   AND JSON_UNQUOTE(JSON_EXTRACT(`context_json`, '$.source')) = 'demo';

INSERT INTO `fm_user_behavior` (`member_id`, `session_id`, `behavior`, `target_type`, `target_id`,
                                `context_json`, `create_time`)
VALUES
    -- 会员 1：高甜热带口味
    (1, 'demo-1', 'VIEW', 'SPU', 1, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'demo-1', 'CART', 'SPU', 1, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'demo-1', 'ORDER', 'SPU', 1, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (1, 'demo-2', 'VIEW', 'SPU', 2, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 'demo-2', 'CART', 'SPU', 2, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (1, 'demo-3', 'VIEW', 'SPU', 6, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 'demo-3', 'CART', 'SPU', 6, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (1, 'demo-4', 'VIEW', 'SPU', 2, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (1, 'demo-5', 'VIEW', 'SPU', 1, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 8 DAY)),
    -- 会员 2：脆爽酸甜口味
    (2, 'demo-6', 'VIEW', 'SPU', 3, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'demo-6', 'CART', 'SPU', 3, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'demo-6', 'ORDER', 'SPU', 3, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (2, 'demo-7', 'VIEW', 'SPU', 4, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2, 'demo-7', 'CART', 'SPU', 4, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (2, 'demo-8', 'VIEW', 'SPU', 4, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 4 DAY)),
    (2, 'demo-9', 'VIEW', 'SPU', 3, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 6 DAY)),
    -- 全站热度：车厘子与哈密瓜被多人浏览，用于验证热销召回
    (3, 'demo-10', 'VIEW', 'SPU', 5, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (4, 'demo-11', 'VIEW', 'SPU', 5, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (5, 'demo-12', 'CART', 'SPU', 5, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (6, 'demo-13', 'VIEW', 'SPU', 6, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (7, 'demo-14', 'VIEW', 'SPU', 6, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 3 DAY)),
    (8, 'demo-15', 'VIEW', 'SPU', 5, '{"source":"demo"}', DATE_SUB(NOW(), INTERVAL 4 DAY));
