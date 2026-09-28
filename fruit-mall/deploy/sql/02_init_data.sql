-- =============================================================================
-- 基于推荐算法的水果商城 —— 初始化数据
-- 内容：角色、菜单权限、角色授权
-- 执行方式：mysql -uroot -p fruit_mall < 02_init_data.sql
-- 说明：全部使用 ON DUPLICATE KEY UPDATE / INSERT IGNORE，可重复执行不报错、不产生重复行
-- =============================================================================

USE `fruit_mall`;
SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 一、角色（三个后台角色；消费者是 fm_member，不占系统角色）
-- -----------------------------------------------------------------------------
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `sort`, `status`, `remark`)
VALUES (1, 'SUPER_ADMIN', '系统管理员', 1, 10, '账号、角色、权限、日志、字典、参数'),
       (2, 'OPERATOR', '商家运营人员', 2, 10, '商品建档、库存、订单处理、经营分析、推荐权重调整'),
       (3, 'FULFILLMENT', '履约与售后人员', 3, 10, '分拣、配送、异常登记、售后审核与退款确认')
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`),
                        `sort`      = VALUES(`sort`),
                        `status`    = VALUES(`status`),
                        `remark`    = VALUES(`remark`);

-- -----------------------------------------------------------------------------
-- 二、菜单权限
--     menu_type：10 目录 / 20 菜单 / 30 按钮；perm 为空表示目录
--     权限标识统一为 {模块}:{操作}，与接口上的 @RequiresPermission 一致
-- -----------------------------------------------------------------------------
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `perm`, `path`, `component`, `icon`, `sort`)
VALUES
    -- 商品管理
    (100, 0,   '商品管理',   10, NULL,                   '/product',          NULL,                  'goods',    1),
    (110, 100, '分类管理',   20, 'category:list',        '/product/category', 'product/category',    NULL,       1),
    (111, 110, '分类新增',   30, 'category:create',      NULL,                NULL,                  NULL,       1),
    (112, 110, '分类编辑',   30, 'category:update',      NULL,                NULL,                  NULL,       2),
    (113, 110, '分类删除',   30, 'category:delete',      NULL,                NULL,                  NULL,       3),
    (120, 100, '商品列表',   20, 'product:list',         '/product/list',     'product/list',        NULL,       2),
    (121, 120, '商品新增',   30, 'product:create',       NULL,                NULL,                  NULL,       1),
    (122, 120, '商品编辑',   30, 'product:update',       NULL,                NULL,                  NULL,       2),
    (123, 120, '上下架',     30, 'product:status',       NULL,                NULL,                  NULL,       3),
    (124, 120, '商品删除',   30, 'product:delete',       NULL,                NULL,                  NULL,       4),
    (125, 120, '特色属性',   30, 'product:attr:update',  NULL,                NULL,                  NULL,       5),

    -- 库存管理
    (200, 0,   '库存管理',   10, NULL,                   '/inventory',        NULL,                  'box',      2),
    (210, 200, '批次列表',   20, 'inventory:list',       '/inventory/batch',  'inventory/batch',     NULL,       1),
    (211, 210, '批次入库',   30, 'inventory:inbound',    NULL,                NULL,                  NULL,       1),
    (212, 210, '库存调整',   30, 'inventory:adjust',     NULL,                NULL,                  NULL,       2),
    (213, 210, '库存报损',   30, 'inventory:loss',       NULL,                NULL,                  NULL,       3),
    (220, 200, '库存流水',   20, 'inventory:txn:list',   '/inventory/txn',    'inventory/txn',       NULL,       2),

    -- 订单管理
    (300, 0,   '订单管理',   10, NULL,                   '/order',            NULL,                  'list',     3),
    (310, 300, '订单列表',   20, 'order:list',           '/order/list',       'order/list',          NULL,       1),
    (311, 310, '订单详情',   30, 'order:detail',         NULL,                NULL,                  NULL,       1),
    (312, 310, '取消订单',   30, 'order:cancel',         NULL,                NULL,                  NULL,       2),
    (313, 310, '订单备注',   30, 'order:remark',         NULL,                NULL,                  NULL,       3),

    -- 履约管理
    (400, 0,   '履约管理',   10, NULL,                   '/fulfillment',      NULL,                  'truck',    4),
    (410, 400, '履约列表',   20, 'fulfillment:list',     '/fulfillment/list', 'fulfillment/list',    NULL,       1),
    (411, 410, '分拣确认',   30, 'fulfillment:pick',     NULL,                NULL,                  NULL,       1),
    (412, 410, '配送推进',   30, 'fulfillment:deliver',  NULL,                NULL,                  NULL,       2),
    (413, 410, '送达签收',   30, 'fulfillment:sign',     NULL,                NULL,                  NULL,       3),
    (414, 410, '异常登记',   30, 'fulfillment:exception',NULL,                NULL,                  NULL,       4),

    -- 售后管理
    (500, 0,   '售后管理',   10, NULL,                   '/aftersale',        NULL,                  'refund',   5),
    (510, 500, '售后列表',   20, 'aftersale:list',       '/aftersale/list',   'aftersale/list',      NULL,       1),
    (511, 510, '售后审核',   30, 'aftersale:audit',      NULL,                NULL,                  NULL,       1),
    (512, 510, '退货收货',   30, 'aftersale:receive',    NULL,                NULL,                  NULL,       2),
    (513, 510, '退款确认',   30, 'aftersale:refund',     NULL,                NULL,                  NULL,       3),
    (520, 500, '评价管理',   20, 'review:list',          '/aftersale/review', 'aftersale/review',    NULL,       2),
    (521, 520, '评价回复',   30, 'review:reply',         NULL,                NULL,                  NULL,       1),
    (522, 520, '评价隐藏',   30, 'review:status',        NULL,                NULL,                  NULL,       2),

    -- 会员管理
    (600, 0,   '会员管理',   10, NULL,                   '/member',           NULL,                  'user',     6),
    (610, 600, '会员列表',   20, 'member:list',          '/member/list',      'member/list',         NULL,       1),
    (611, 610, '会员详情',   30, 'member:detail',        NULL,                NULL,                  NULL,       1),

    -- 经营分析
    (700, 0,   '经营分析',   10, NULL,                   '/stat',             NULL,                  'chart',    7),
    (710, 700, '销售趋势',   20, 'stat:sales',           '/stat/sales',       'stat/sales',          NULL,       1),
    (720, 700, '品类占比',   20, 'stat:category',        '/stat/category',    'stat/category',       NULL,       2),
    (730, 700, '商品销量TOP',20, 'stat:product',         '/stat/product',     'stat/product',        NULL,       3),

    -- 推荐运营
    (800, 0,   '推荐运营',   10, NULL,                   '/recommend',        NULL,                  'star',     8),
    (810, 800, '权重方案',   20, 'recommend:config:list','/recommend/config', 'recommend/config',    NULL,       1),
    (811, 810, '方案编辑',   30, 'recommend:config:update', NULL,             NULL,                  NULL,       1),
    (812, 810, '方案发布',   30, 'recommend:config:publish', NULL,            NULL,                  NULL,       2),
    (820, 800, '置顶屏蔽保量',20,'recommend:rule:update','/recommend/rule',   'recommend/rule',      NULL,       2),
    (830, 800, '推荐效果',   20, 'recommend:effect',     '/recommend/effect', 'recommend/effect',    NULL,       3),

    -- 系统管理
    (900, 0,   '系统管理',   10, NULL,                   '/system',           NULL,                  'setting',  9),
    (910, 900, '用户管理',   20, 'system:user:list',     '/system/user',      'system/user',         NULL,       1),
    (911, 910, '用户新增',   30, 'system:user:create',   NULL,                NULL,                  NULL,       1),
    (912, 910, '用户编辑',   30, 'system:user:update',   NULL,                NULL,                  NULL,       2),
    (913, 910, '重置密码',   30, 'system:user:password', NULL,                NULL,                  NULL,       3),
    (920, 900, '角色管理',   20, 'system:role:list',     '/system/role',      'system/role',         NULL,       2),
    (921, 920, '角色新增',   30, 'system:role:create',   NULL,                NULL,                  NULL,       1),
    (922, 920, '角色编辑',   30, 'system:role:update',   NULL,                NULL,                  NULL,       2),
    (923, 920, '角色授权',   30, 'system:role:grant',    NULL,                NULL,                  NULL,       3),
    (930, 900, '菜单权限',   20, 'system:menu:list',     '/system/menu',      'system/menu',         NULL,       3),
    (931, 930, '菜单编辑',   30, 'system:menu:update',   NULL,                NULL,                  NULL,       1),
    (940, 900, '操作日志',   20, 'system:operlog:list',  '/system/operlog',   'system/operlog',      NULL,       4),
    (950, 900, '登录日志',   20, 'system:loginlog:list', '/system/loginlog',  'system/loginlog',     NULL,       5)
ON DUPLICATE KEY UPDATE `parent_id`   = VALUES(`parent_id`),
                        `menu_name`   = VALUES(`menu_name`),
                        `menu_type`   = VALUES(`menu_type`),
                        `path`        = VALUES(`path`),
                        `component`   = VALUES(`component`),
                        `icon`        = VALUES(`icon`),
                        `sort`        = VALUES(`sort`);

-- -----------------------------------------------------------------------------
-- 三、角色授权
--     系统管理员：全部权限
--     商家运营人员：商品、库存、订单、会员、经营分析、推荐运营
--     履约与售后人员：履约、售后、评价、订单查看
-- -----------------------------------------------------------------------------
-- 系统管理员：全部菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu` WHERE `deleted` = 0;

-- 商家运营人员：商品、库存、订单、会员、经营分析、推荐运营
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, `id`
FROM `sys_menu`
WHERE `deleted` = 0
  AND (
    `id` BETWEEN 100 AND 199
    OR `id` BETWEEN 200 AND 299
    OR `id` BETWEEN 300 AND 399
    OR `id` BETWEEN 600 AND 699
    OR `id` BETWEEN 700 AND 799
    OR `id` BETWEEN 800 AND 899
  );

-- 履约与售后人员：履约、售后评价、订单查看
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, `id`
FROM `sys_menu`
WHERE `deleted` = 0
  AND (`id` BETWEEN 400 AND 599 OR `id` IN (300, 310, 311));

-- -----------------------------------------------------------------------------
-- 四、演示账号
--     密码一律存哈希密文，禁止明文、禁止 MD5。
--     算法：BCrypt（spring-security-crypto），成本因子 10，密文自带随机盐，
--     因此三个账号即使密码相同，密文也各不相同。选型原因见 PasswordUtil 类注释。
--
--     三个账号的明文密码统一为 Fruit@2026（仅演示环境使用，已登记到 README）。
--     重新生成密文：
--       cd fruit-mall/server && mvn -q -DskipTests package
--       java -cp target/fruit-mall-server-0.1.0-SNAPSHOT.jar \
--            "-Dloader.main=com.fruitmall.common.util.PasswordUtil" \
--            org.springframework.boot.loader.launch.PropertiesLauncher 新密码
--     说明：BCrypt 依赖在可执行 jar 内，用 -cp target/classes 会报 NoClassDefFoundError。
--
--     注意：sys_user 的 username 有唯一索引且配合逻辑删除使用，
--     重复执行本段只更新密码与昵称，不会重复插入账号。
-- -----------------------------------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `status`)
VALUES (1, 'admin',
        '$2a$10$wowp2jTbLLKRKgaT5g1UOervW1G4I5SILN4LsJTCl6cMRvIbWHqZq',
        '系统管理员', '演示账号', '13800000001', 10),
       (2, 'operator',
        '$2a$10$SYaidfHW3XhORrzmAYxjaeDyXS5mMWf1CV8anaUhVCl4JmJgfaL4.',
        '商家运营', '演示账号', '13800000002', 10),
       (3, 'fulfillment',
        '$2a$10$PBlPRtLSEK5Sy0yRMT/BNOmRyKNzSUVNtVDkrBlVTiEiiNIMnXxnS',
        '履约售后', '演示账号', '13800000003', 10)
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`),
                        `nickname` = VALUES(`nickname`),
                        `real_name` = VALUES(`real_name`),
                        `phone`    = VALUES(`phone`),
                        `status`   = VALUES(`status`);

-- 账号与角色关联：admin 超管、operator 运营、fulfillment 履约售后
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
VALUES (1, 1), (2, 2), (3, 3);
