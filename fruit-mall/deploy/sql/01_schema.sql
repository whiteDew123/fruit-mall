-- =============================================================================
-- 基于推荐算法的水果商城 —— 建库建表脚本
-- 表结构与 docs/05-数据库设计.md 一一对应，两者必须同步修改
-- 执行方式：mysql -uroot -p < 01_schema.sql
-- 说明：使用 CREATE TABLE IF NOT EXISTS，重复执行不会删除已有数据；
--       需要重建时请手工 DROP TABLE，禁止把 DROP 语句留在本脚本中
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `fruit_mall`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `fruit_mall`;
SET NAMES utf8mb4;

-- =============================================================================
-- 一、系统权限（7 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`        VARCHAR(50)  NOT NULL                COMMENT '登录名',
    `password`        VARCHAR(100) NOT NULL                COMMENT 'BCrypt 密文',
    `nickname`        VARCHAR(50)  NOT NULL                COMMENT '显示名',
    `real_name`       VARCHAR(50)  DEFAULT NULL            COMMENT '真实姓名',
    `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
    `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
    `status`          TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10正常 20禁用',
    `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户';

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   VARCHAR(50)  NOT NULL                COMMENT '角色编码',
    `role_name`   VARCHAR(50)  NOT NULL                COMMENT '角色名称',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10正常 20禁用',
    `remark`      VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父级ID，0为顶级',
    `menu_name`   VARCHAR(50)  NOT NULL                COMMENT '名称',
    `menu_type`   TINYINT      NOT NULL DEFAULT 10     COMMENT '类型：10目录 20菜单 30按钮',
    `perm`        VARCHAR(100) DEFAULT NULL            COMMENT '权限标识，如 product:create',
    `path`        VARCHAR(200) DEFAULT NULL            COMMENT '前端路由路径',
    `component`   VARCHAR(200) DEFAULT NULL            COMMENT '前端组件路径',
    `icon`        VARCHAR(50)  DEFAULT NULL            COMMENT '图标',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `visible`     TINYINT      NOT NULL DEFAULT 1      COMMENT '是否显示：0隐藏 1显示',
    `status`      TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10正常 20禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm` (`perm`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '菜单权限';

-- 关联表采用物理删除（重新授权时先删后插），故不含 deleted 与 update_time
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT   DEFAULT NULL            COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `menu_id`     BIGINT   NOT NULL                COMMENT '菜单ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`   BIGINT   DEFAULT NULL            COMMENT '创建人ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色菜单关联';

-- 日志表只追加不修改，故不含 update_time / update_by / deleted
CREATE TABLE IF NOT EXISTS `sys_oper_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    `username`      VARCHAR(50)  DEFAULT NULL            COMMENT '操作人登录名',
    `module`        VARCHAR(50)  DEFAULT NULL            COMMENT '模块名',
    `action`        VARCHAR(50)  DEFAULT NULL            COMMENT '操作动作',
    `method`        VARCHAR(200) DEFAULT NULL            COMMENT '目标方法',
    `request_url`   VARCHAR(255) DEFAULT NULL            COMMENT '请求地址',
    `request_param` TEXT                                 COMMENT '请求参数',
    `result_status` TINYINT      NOT NULL DEFAULT 10     COMMENT '结果：10成功 20失败',
    `error_msg`     TEXT                                 COMMENT '异常信息',
    `ip`            VARCHAR(50)  DEFAULT NULL            COMMENT '来源IP',
    `cost_time`     BIGINT       DEFAULT NULL            COMMENT '耗时（毫秒）',
    `oper_time`     DATETIME     DEFAULT NULL            COMMENT '操作时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_time` (`oper_time`),
    KEY `idx_user` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(50)  DEFAULT NULL            COMMENT '登录名',
    `user_type`     TINYINT      NOT NULL DEFAULT 10     COMMENT '类型：10系统用户 20会员',
    `ip`            VARCHAR(50)  DEFAULT NULL            COMMENT '来源IP',
    `user_agent`    VARCHAR(255) DEFAULT NULL            COMMENT '浏览器标识',
    `result_status` TINYINT      NOT NULL DEFAULT 10     COMMENT '结果：10成功 20失败',
    `message`       VARCHAR(255) DEFAULT NULL            COMMENT '说明',
    `login_time`    DATETIME     DEFAULT NULL            COMMENT '登录时间',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_login_time` (`login_time`),
    KEY `idx_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '登录日志';

-- =============================================================================
-- 二、会员与行为（4 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_member` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`        VARCHAR(50)  NOT NULL                COMMENT '登录名',
    `password`        VARCHAR(100) NOT NULL                COMMENT 'BCrypt 密文',
    `nickname`        VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
    `phone`           VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `avatar`          VARCHAR(255) DEFAULT NULL            COMMENT '头像地址',
    `status`          TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10正常 20禁用',
    `last_login_time` DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员';

CREATE TABLE IF NOT EXISTS `fm_member_address` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id`       BIGINT       NOT NULL                COMMENT '会员ID',
    `receiver_name`   VARCHAR(50)  NOT NULL                COMMENT '收货人',
    `receiver_phone`  VARCHAR(20)  NOT NULL                COMMENT '收货电话',
    `province`        VARCHAR(50)  DEFAULT NULL            COMMENT '省',
    `city`            VARCHAR(50)  DEFAULT NULL            COMMENT '市',
    `district`        VARCHAR(50)  DEFAULT NULL            COMMENT '区县',
    `detail_address`  VARCHAR(255) NOT NULL                COMMENT '详细地址',
    `is_default`      TINYINT      NOT NULL DEFAULT 0      COMMENT '是否默认：0否 1是',
    `tag`             VARCHAR(20)  DEFAULT NULL            COMMENT '标签：家/公司等',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_member` (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '收货地址';

CREATE TABLE IF NOT EXISTS `fm_member_preference` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id`   BIGINT        NOT NULL                COMMENT '会员ID',
    `sweet_pref`  TINYINT       NOT NULL DEFAULT 0      COMMENT '甜度偏好：0未设置 1-5强度',
    `sour_pref`   TINYINT       NOT NULL DEFAULT 0      COMMENT '酸度偏好：0未设置 1-5强度',
    `crisp_pref`  TINYINT       NOT NULL DEFAULT 0      COMMENT '脆度偏好：0未设置 1-5强度',
    `price_min`   DECIMAL(10,2) DEFAULT NULL            COMMENT '可接受价格下限',
    `price_max`   DECIMAL(10,2) DEFAULT NULL            COMMENT '可接受价格上限',
    `avoid_list`  JSON          DEFAULT NULL            COMMENT '忌口/过敏项数组',
    `source`      TINYINT       NOT NULL DEFAULT 10     COMMENT '来源：10问卷 20行为推断',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member` (`member_id`),
    CONSTRAINT `ck_pref_price_min` CHECK (`price_min` IS NULL OR `price_min` >= 0),
    CONSTRAINT `ck_pref_price_range` CHECK (`price_min` IS NULL OR `price_max` IS NULL OR `price_max` >= `price_min`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员偏好';

-- 行为埋点只追加，故仅保留 create_time
CREATE TABLE IF NOT EXISTS `fm_user_behavior` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id`    BIGINT      DEFAULT NULL            COMMENT '会员ID，未登录为 NULL',
    `anonymous_id` VARCHAR(64) DEFAULT NULL            COMMENT '匿名标识',
    `session_id`   VARCHAR(64) DEFAULT NULL            COMMENT '会话ID',
    `behavior`     VARCHAR(20) NOT NULL                COMMENT '行为：VIEW/SEARCH/FAVORITE/CART/ORDER/REVIEW/DISLIKE',
    `target_type`  VARCHAR(20) DEFAULT NULL            COMMENT '目标类型：SPU/SKU/CATEGORY/KEYWORD',
    `target_id`    BIGINT      DEFAULT NULL            COMMENT '目标ID',
    `context_json` JSON        DEFAULT NULL            COMMENT '上下文：来源页面、关键词、位置',
    `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_time` (`member_id`, `create_time`),
    KEY `idx_target` (`target_id`, `behavior`, `create_time`),
    KEY `idx_session` (`session_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户行为日志';

-- =============================================================================
-- 三、商品（7 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_category` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`     BIGINT       NOT NULL DEFAULT 0      COMMENT '父级ID，0为顶级',
    `category_name` VARCHAR(50)  NOT NULL                COMMENT '分类名称',
    `category_code` VARCHAR(50)  DEFAULT NULL            COMMENT '分类编码',
    `level`         TINYINT      NOT NULL DEFAULT 1      COMMENT '层级',
    `sort`          INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `icon`          VARCHAR(255) DEFAULT NULL            COMMENT '图标地址',
    `status`        TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10启用 20停用',
    `remark`        VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品分类';

CREATE TABLE IF NOT EXISTS `fm_product_spu` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spu_code`          VARCHAR(50)  NOT NULL                COMMENT '商品编码',
    `category_id`       BIGINT       NOT NULL                COMMENT '分类ID',
    `spu_name`          VARCHAR(100) NOT NULL                COMMENT '商品名称',
    `subtitle`          VARCHAR(200) DEFAULT NULL            COMMENT '副标题/卖点',
    `origin_place`      VARCHAR(100) DEFAULT NULL            COMMENT '产地',
    `season_months`     VARCHAR(20)  DEFAULT NULL            COMMENT '应季月份，如 5,6,7',
    `storage_condition` VARCHAR(100) DEFAULT NULL            COMMENT '储存条件',
    `shelf_life_days`   INT          DEFAULT NULL            COMMENT '保质期天数',
    `unit`              VARCHAR(20)  DEFAULT NULL            COMMENT '计量单位',
    `main_image`        VARCHAR(255) DEFAULT NULL            COMMENT '主图地址',
    `detail`            MEDIUMTEXT                           COMMENT '图文详情',
    `sales_count`       INT          NOT NULL DEFAULT 0      COMMENT '累计销量（冗余，推荐热度用）',
    `status`            TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10草稿 20上架 30下架',
    `sort`              INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `remark`            VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`         BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`           TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_code` (`spu_code`),
    KEY `idx_category_status` (`category_id`, `status`),
    KEY `idx_status_sales` (`status`, `sales_count`),
    CONSTRAINT `ck_spu_life` CHECK (`shelf_life_days` IS NULL OR `shelf_life_days` >= 0),
    CONSTRAINT `ck_spu_sales` CHECK (`sales_count` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品SPU';

CREATE TABLE IF NOT EXISTS `fm_product_sku` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spu_id`         BIGINT        NOT NULL                COMMENT '商品SPU ID',
    `sku_code`       VARCHAR(50)   NOT NULL                COMMENT '规格编码',
    `spec_name`      VARCHAR(100)  NOT NULL                COMMENT '规格名，如 5斤装',
    `spec_json`      JSON          DEFAULT NULL            COMMENT '规格键值，如 {"净重":"5斤"}',
    `image`          VARCHAR(255)  DEFAULT NULL            COMMENT '规格图片',
    `original_price` DECIMAL(10,2) DEFAULT NULL            COMMENT '划线价',
    `price`          DECIMAL(10,2) NOT NULL                COMMENT '售价',
    `stock`          INT           NOT NULL DEFAULT 0      COMMENT '可售库存总量',
    `locked_stock`   INT           NOT NULL DEFAULT 0      COMMENT '预占库存',
    `warn_stock`     INT           NOT NULL DEFAULT 0      COMMENT '低库存预警阈值',
    `sales_count`    INT           NOT NULL DEFAULT 0      COMMENT '累计销量',
    `status`         TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10启用 20停用',
    `version`        INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`      BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`      BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`        TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sku_code` (`sku_code`),
    KEY `idx_spu` (`spu_id`),
    CONSTRAINT `ck_sku_price` CHECK (`price` > 0),
    CONSTRAINT `ck_sku_stock` CHECK (`stock` >= 0),
    CONSTRAINT `ck_sku_locked_stock` CHECK (`locked_stock` >= 0),
    CONSTRAINT `ck_sku_sales` CHECK (`sales_count` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品SKU';

CREATE TABLE IF NOT EXISTS `fm_product_attr_def` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `attr_code`     VARCHAR(50)   NOT NULL                COMMENT '属性编码',
    `attr_name`     VARCHAR(50)   NOT NULL                COMMENT '属性名称，如 甜度',
    `data_type`     TINYINT       NOT NULL DEFAULT 10     COMMENT '类型：10数值 20枚举 30文本 40布尔',
    `unit`          VARCHAR(20)   DEFAULT NULL            COMMENT '单位',
    `enum_options`  VARCHAR(500)  DEFAULT NULL            COMMENT '枚举取值数组（JSON）',
    `min_value`     DECIMAL(10,4) DEFAULT NULL            COMMENT '数值下限',
    `max_value`     DECIMAL(10,4) DEFAULT NULL            COMMENT '数值上限',
    `required`      TINYINT       NOT NULL DEFAULT 0      COMMENT '是否必填：0否 1是（上架校验）',
    `sort`          INT           NOT NULL DEFAULT 0      COMMENT '排序',
    `status`        TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10正常 20停用',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_attr_code` (`attr_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品特色属性定义';

CREATE TABLE IF NOT EXISTS `fm_product_attr_value` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spu_id`      BIGINT        NOT NULL                COMMENT '商品SPU ID',
    `attr_def_id` BIGINT        NOT NULL                COMMENT '属性定义ID',
    `attr_value`  VARCHAR(500)  DEFAULT NULL            COMMENT '属性原始值',
    `num_value`   DECIMAL(10,4) DEFAULT NULL            COMMENT '数值化取值，供特征向量使用',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu_attr` (`spu_id`, `attr_def_id`),
    KEY `idx_attr` (`attr_def_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品特色属性值';

CREATE TABLE IF NOT EXISTS `fm_product_image` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spu_id`      BIGINT       NOT NULL                COMMENT '商品SPU ID',
    `sku_id`      BIGINT       DEFAULT NULL            COMMENT '规格ID，规格图时填写',
    `image_url`   VARCHAR(255) NOT NULL                COMMENT '图片地址',
    `image_type`  TINYINT      NOT NULL DEFAULT 10     COMMENT '类型：10主图 20详情图 30规格图',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_spu` (`spu_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品图片';

CREATE TABLE IF NOT EXISTS `fm_product_feature` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `spu_id`          BIGINT       NOT NULL                COMMENT '商品SPU ID',
    `feature_version` VARCHAR(20)  DEFAULT NULL            COMMENT '特征版本',
    `vector_json`     JSON         DEFAULT NULL            COMMENT '定长特征向量',
    `price_norm`      DECIMAL(8,6) DEFAULT NULL            COMMENT '归一化价格',
    `season_months`   VARCHAR(20)  DEFAULT NULL            COMMENT '应季月份',
    `calc_time`       DATETIME     DEFAULT NULL            COMMENT '计算时间',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_spu` (`spu_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品特征向量';

-- =============================================================================
-- 四、库存（2 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_inventory_batch` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `batch_no`           VARCHAR(50)   NOT NULL                COMMENT '批次号',
    `sku_id`             BIGINT        NOT NULL                COMMENT '规格ID',
    `inbound_date`       DATE          DEFAULT NULL            COMMENT '入库日期',
    `expire_date`        DATE          DEFAULT NULL            COMMENT '到期日期',
    `quantity`           INT           NOT NULL DEFAULT 0      COMMENT '入库数量',
    `remaining_quantity` INT           NOT NULL DEFAULT 0      COMMENT '剩余数量',
    `locked_quantity`    INT           NOT NULL DEFAULT 0      COMMENT '该批次预占数量',
    `unit_cost`          DECIMAL(10,2) DEFAULT NULL            COMMENT '单位成本',
    `warehouse`          VARCHAR(50)   DEFAULT NULL            COMMENT '仓库',
    `status`             TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10正常 20临期 30冻结 40退货暂存 50报损',
    `remark`             VARCHAR(255)  DEFAULT NULL            COMMENT '备注',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`          BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`          BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`            TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_no` (`batch_no`),
    KEY `idx_sku_expire` (`sku_id`, `expire_date`),
    KEY `idx_expire` (`expire_date`),
    KEY `idx_status` (`status`),
    CONSTRAINT `ck_batch_quantity` CHECK (`quantity` >= 0),
    CONSTRAINT `ck_batch_remaining` CHECK (`remaining_quantity` >= 0),
    CONSTRAINT `ck_batch_locked` CHECK (`locked_quantity` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '库存批次';

CREATE TABLE IF NOT EXISTS `fm_inventory_transaction` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `batch_id`        BIGINT       DEFAULT NULL            COMMENT '批次ID，SKU 级操作时为空',
    `sku_id`          BIGINT       NOT NULL                COMMENT '规格ID',
    `type`            TINYINT      NOT NULL                COMMENT '类型：10入库 20预占 30释放 40实扣 50退货回补 60报损 70盘点调整',
    `quantity`        INT          NOT NULL                COMMENT '变动数量，入库为正、出库为负',
    `before_quantity` INT          DEFAULT NULL            COMMENT '变动前库存',
    `after_quantity`  INT          DEFAULT NULL            COMMENT '变动后库存',
    `biz_type`        TINYINT      NOT NULL                COMMENT '业务类型：10订单 20售后 30手工调整',
    `biz_no`          VARCHAR(50)  DEFAULT NULL            COMMENT '业务单号，如订单号/售后单号',
    `operator_id`     BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    `operator_name`   VARCHAR(50)  DEFAULT NULL            COMMENT '操作人名称',
    `remark`          VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_sku_time` (`sku_id`, `create_time`),
    KEY `idx_biz` (`biz_type`, `biz_no`),
    KEY `idx_batch` (`batch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '库存流水';

-- =============================================================================
-- 五、交易（6 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_cart_item` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id`   BIGINT   NOT NULL                COMMENT '会员ID',
    `spu_id`      BIGINT   NOT NULL                COMMENT '商品SPU ID（冗余，便于列表展示）',
    `sku_id`      BIGINT   NOT NULL                COMMENT '规格ID',
    `quantity`    INT      NOT NULL DEFAULT 1      COMMENT '数量',
    `selected`    TINYINT  NOT NULL DEFAULT 1      COMMENT '是否选中：0否 1是',
    `status`      TINYINT  NOT NULL DEFAULT 10     COMMENT '状态：10有效 20失效',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT   DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT   DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT  NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_sku` (`member_id`, `sku_id`),
    KEY `idx_member` (`member_id`),
    CONSTRAINT `ck_cart_quantity` CHECK (`quantity` > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '购物车项';

CREATE TABLE IF NOT EXISTS `fm_orders` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`          VARCHAR(32)   NOT NULL                COMMENT '订单号',
    `member_id`         BIGINT        NOT NULL                COMMENT '会员ID',
    `status`            TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10待支付 20已支付 30备货中 40配送中 50已完成 60已取消 70退款中 80已退款',
    `total_amount`      DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '商品金额合计',
    `freight_amount`    DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '运费',
    `pay_amount`        DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '应付金额',
    `receiver_name`     VARCHAR(50)   DEFAULT NULL            COMMENT '收货人（快照）',
    `receiver_phone`    VARCHAR(20)   DEFAULT NULL            COMMENT '收货电话（快照）',
    `receiver_province` VARCHAR(50)   DEFAULT NULL            COMMENT '省（快照）',
    `receiver_city`     VARCHAR(50)   DEFAULT NULL            COMMENT '市（快照）',
    `receiver_district` VARCHAR(50)   DEFAULT NULL            COMMENT '区县（快照）',
    `receiver_address`  VARCHAR(255)  DEFAULT NULL            COMMENT '详细地址（快照）',
    `member_remark`     VARCHAR(255)  DEFAULT NULL            COMMENT '会员备注',
    `admin_remark`      VARCHAR(255)  DEFAULT NULL            COMMENT '商家备注',
    `expire_time`       DATETIME      DEFAULT NULL            COMMENT '支付截止时间',
    `pay_time`          DATETIME      DEFAULT NULL            COMMENT '支付时间',
    `cancel_time`       DATETIME      DEFAULT NULL            COMMENT '取消时间',
    `cancel_reason`     VARCHAR(255)  DEFAULT NULL            COMMENT '取消原因',
    `finish_time`       DATETIME      DEFAULT NULL            COMMENT '完成时间',
    `version`           INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本',
    `create_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`         BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`         BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`           TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_member_status` (`member_id`, `status`),
    KEY `idx_status_expire` (`status`, `expire_time`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `ck_order_total` CHECK (`total_amount` >= 0),
    CONSTRAINT `ck_order_pay` CHECK (`pay_amount` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单主表';

CREATE TABLE IF NOT EXISTS `fm_order_item` (
    `id`                  BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`            BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`            VARCHAR(32)   NOT NULL                COMMENT '订单号（冗余）',
    `spu_id`              BIGINT        NOT NULL                COMMENT '商品SPU ID',
    `sku_id`              BIGINT        NOT NULL                COMMENT '规格ID',
    `spu_name`            VARCHAR(100)  NOT NULL                COMMENT '商品名称（快照）',
    `sku_name`            VARCHAR(100)  DEFAULT NULL            COMMENT '规格名称（快照）',
    `sku_snapshot`        JSON          DEFAULT NULL            COMMENT '规格与属性快照',
    `image`               VARCHAR(255)  DEFAULT NULL            COMMENT '商品图片（快照）',
    `price`               DECIMAL(10,2) NOT NULL                COMMENT '成交单价',
    `quantity`            INT           NOT NULL                COMMENT '数量',
    `amount`              DECIMAL(10,2) NOT NULL                COMMENT '小计金额',
    `after_sale_quantity` INT           NOT NULL DEFAULT 0      COMMENT '累计已售后数量',
    `reviewed`            TINYINT       NOT NULL DEFAULT 0      COMMENT '是否已评价：0否 1是',
    `create_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`           BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`           BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`             TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_order` (`order_id`),
    KEY `idx_sku` (`sku_id`),
    KEY `idx_spu` (`spu_id`),
    CONSTRAINT `ck_item_price` CHECK (`price` >= 0),
    CONSTRAINT `ck_item_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `ck_item_amount` CHECK (`amount` >= 0),
    CONSTRAINT `ck_item_after_sale` CHECK (`after_sale_quantity` >= 0 AND `after_sale_quantity` <= `quantity`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单项';

CREATE TABLE IF NOT EXISTS `fm_order_status_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT       NOT NULL                COMMENT '订单ID',
    `order_no`      VARCHAR(32)  NOT NULL                COMMENT '订单号',
    `from_status`   TINYINT      DEFAULT NULL            COMMENT '原状态',
    `to_status`     TINYINT      NOT NULL                COMMENT '新状态',
    `action`        VARCHAR(50)  NOT NULL                COMMENT '动作：PAY/CANCEL/TIMEOUT/PICK/DELIVER/SIGN/REFUND',
    `operator_type` TINYINT      NOT NULL DEFAULT 30     COMMENT '操作者：10会员 20商家 30系统',
    `operator_id`   BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    `operator_name` VARCHAR(50)  DEFAULT NULL            COMMENT '操作人名称',
    `remark`        VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_time` (`order_id`, `create_time`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单状态流水';

CREATE TABLE IF NOT EXISTS `fm_payment_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `pay_no`        VARCHAR(32)   NOT NULL                COMMENT '支付流水号（幂等键）',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`      VARCHAR(32)   NOT NULL                COMMENT '订单号',
    `member_id`     BIGINT        NOT NULL                COMMENT '会员ID',
    `channel`       VARCHAR(20)   NOT NULL DEFAULT 'MOCK' COMMENT '支付渠道：模拟支付',
    `amount`        DECIMAL(10,2) NOT NULL                COMMENT '支付金额',
    `status`        TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10已创建 20支付中 30支付成功 40支付失败 50已关闭 60退款中 70已退款',
    `pay_time`      DATETIME      DEFAULT NULL            COMMENT '发起支付时间',
    `callback_time` DATETIME      DEFAULT NULL            COMMENT '回调时间',
    `close_time`    DATETIME      DEFAULT NULL            COMMENT '关闭时间',
    `fail_reason`   VARCHAR(255)  DEFAULT NULL            COMMENT '失败原因',
    `version`       INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pay_no` (`pay_no`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_member` (`member_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `ck_payment_amount` CHECK (`amount` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '支付记录';

CREATE TABLE IF NOT EXISTS `fm_refund_record` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `refund_no`     VARCHAR(32)   NOT NULL                COMMENT '退款流水号',
    `after_sale_id` BIGINT        NOT NULL                COMMENT '售后单ID',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`      VARCHAR(32)   NOT NULL                COMMENT '订单号',
    `member_id`     BIGINT        NOT NULL                COMMENT '会员ID',
    `refund_amount` DECIMAL(10,2) NOT NULL                COMMENT '退款金额',
    `status`        TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10退款中 20退款成功 30退款失败',
    `channel`       VARCHAR(20)   NOT NULL DEFAULT 'MOCK' COMMENT '退款渠道',
    `refund_time`   DATETIME      DEFAULT NULL            COMMENT '退款完成时间',
    `fail_reason`   VARCHAR(255)  DEFAULT NULL            COMMENT '失败原因',
    `operator_id`   BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_after_sale` (`after_sale_id`),
    KEY `idx_order` (`order_id`),
    CONSTRAINT `ck_refund_amount` CHECK (`refund_amount` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '退款记录';

-- =============================================================================
-- 六、履约（2 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_fulfillment_order` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `fulfillment_no`   VARCHAR(32)  NOT NULL                COMMENT '履约单号',
    `order_id`         BIGINT       NOT NULL                COMMENT '订单ID',
    `order_no`         VARCHAR(32)  NOT NULL                COMMENT '订单号',
    `member_id`        BIGINT       NOT NULL                COMMENT '会员ID',
    `status`           TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10待分拣 20分拣完成 30待配送 40配送中 50已送达 60已签收 90异常',
    `receiver_name`    VARCHAR(50)  DEFAULT NULL            COMMENT '收货人（快照）',
    `receiver_phone`   VARCHAR(20)  DEFAULT NULL            COMMENT '收货电话（快照）',
    `receiver_address` VARCHAR(255) DEFAULT NULL            COMMENT '收货地址（快照）',
    `remark`           VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `pick_time`        DATETIME     DEFAULT NULL            COMMENT '分拣完成时间',
    `delivery_time`    DATETIME     DEFAULT NULL            COMMENT '发货时间',
    `arrive_time`      DATETIME     DEFAULT NULL            COMMENT '送达时间',
    `sign_time`        DATETIME     DEFAULT NULL            COMMENT '签收时间',
    `version`          INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`        BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`        BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`          TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_fulfillment_no` (`fulfillment_no`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '履约单';

CREATE TABLE IF NOT EXISTS `fm_fulfillment_trace` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `fulfillment_id` BIGINT        NOT NULL                COMMENT '履约单ID',
    `fulfillment_no` VARCHAR(32)   NOT NULL                COMMENT '履约单号',
    `node`           TINYINT       NOT NULL                COMMENT '节点：10分拣 20打包 30出库配送 40送达 50签收 90异常',
    `node_name`      VARCHAR(50)   DEFAULT NULL            COMMENT '节点名称',
    `operator_type`  TINYINT       NOT NULL DEFAULT 20     COMMENT '操作者：10会员 20商家 30系统',
    `operator_id`    BIGINT        DEFAULT NULL            COMMENT '操作人ID',
    `operator_name`  VARCHAR(50)   DEFAULT NULL            COMMENT '操作人名称',
    `remark`         VARCHAR(255)  DEFAULT NULL            COMMENT '备注/异常原因',
    `images`         VARCHAR(1000) DEFAULT NULL            COMMENT '图片地址（JSON 数组）',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '时间轴时间',
    PRIMARY KEY (`id`),
    KEY `idx_fulfillment_time` (`fulfillment_id`, `create_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '履约轨迹';

-- =============================================================================
-- 七、售后与评价（4 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_after_sale` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `after_sale_no`   VARCHAR(32)   NOT NULL                COMMENT '售后单号',
    `order_id`        BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`        VARCHAR(32)   NOT NULL                COMMENT '订单号',
    `member_id`       BIGINT        NOT NULL                COMMENT '会员ID',
    `type`            TINYINT       NOT NULL DEFAULT 10     COMMENT '类型：10仅退款 20退货退款',
    `status`          TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10待审核 20已同意 30已驳回 40退货中 50退款中 60已完成 70已取消',
    `quantity`        INT           NOT NULL DEFAULT 1      COMMENT '售后商品总数量',
    `refund_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '退款金额',
    `reason`          VARCHAR(255)  DEFAULT NULL            COMMENT '申请原因',
    `evidence_images` VARCHAR(1000) DEFAULT NULL            COMMENT '凭证图片（JSON 数组）',
    `apply_time`      DATETIME      DEFAULT NULL            COMMENT '申请时间',
    `audit_time`      DATETIME      DEFAULT NULL            COMMENT '审核时间',
    `auditor_id`      BIGINT        DEFAULT NULL            COMMENT '审核人ID',
    `audit_remark`    VARCHAR(255)  DEFAULT NULL            COMMENT '审核意见',
    `return_time`     DATETIME      DEFAULT NULL            COMMENT '会员寄回时间',
    `receive_time`    DATETIME      DEFAULT NULL            COMMENT '商家收货时间',
    `refund_time`     DATETIME      DEFAULT NULL            COMMENT '退款完成时间',
    `cancel_reason`   VARCHAR(255)  DEFAULT NULL            COMMENT '取消原因',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_after_sale_no` (`after_sale_no`),
    KEY `idx_order` (`order_id`),
    KEY `idx_member_status` (`member_id`, `status`),
    KEY `idx_status_time` (`status`, `create_time`),
    CONSTRAINT `ck_after_sale_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `ck_after_sale_amount` CHECK (`refund_amount` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '售后单';

CREATE TABLE IF NOT EXISTS `fm_after_sale_item` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `after_sale_id`   BIGINT        NOT NULL                COMMENT '售后单ID',
    `order_item_id`   BIGINT        NOT NULL                COMMENT '订单项ID',
    `sku_id`          BIGINT        NOT NULL                COMMENT '规格ID',
    `quantity`        INT           NOT NULL                COMMENT '售后数量',
    `refund_amount`   DECIMAL(10,2) NOT NULL DEFAULT 0.00   COMMENT '该明细退款金额',
    `return_batch_id` BIGINT        DEFAULT NULL            COMMENT '退货暂存批次ID',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`       BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`       BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`         TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    KEY `idx_after_sale` (`after_sale_id`),
    KEY `idx_order_item` (`order_item_id`),
    CONSTRAINT `ck_after_sale_item_quantity` CHECK (`quantity` > 0),
    CONSTRAINT `ck_after_sale_item_amount` CHECK (`refund_amount` >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '售后明细';

CREATE TABLE IF NOT EXISTS `fm_review` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`      VARCHAR(32)   NOT NULL                COMMENT '订单号',
    `order_item_id` BIGINT        NOT NULL                COMMENT '订单项ID',
    `spu_id`        BIGINT        NOT NULL                COMMENT '商品SPU ID',
    `sku_id`        BIGINT        DEFAULT NULL            COMMENT '规格ID',
    `member_id`     BIGINT        NOT NULL                COMMENT '会员ID',
    `star`          TINYINT       NOT NULL                COMMENT '星级：1-5',
    `content`       VARCHAR(1000) DEFAULT NULL            COMMENT '评价内容',
    `images`        VARCHAR(1000) DEFAULT NULL            COMMENT '评价图片（JSON 数组）',
    `is_anonymous`  TINYINT       NOT NULL DEFAULT 0      COMMENT '是否匿名：0否 1是',
    `status`        TINYINT       NOT NULL DEFAULT 10     COMMENT '状态：10显示 20隐藏',
    `reply_content` VARCHAR(500)  DEFAULT NULL            COMMENT '商家回复',
    `reply_time`    DATETIME      DEFAULT NULL            COMMENT '回复时间',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT        DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT        DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_item` (`order_item_id`),
    KEY `idx_spu_status` (`spu_id`, `status`),
    KEY `idx_member` (`member_id`),
    CONSTRAINT `ck_review_star` CHECK (`star` >= 1 AND `star` <= 5)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '订单评价';

CREATE TABLE IF NOT EXISTS `fm_review_tag` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tag_name`    VARCHAR(20) NOT NULL                COMMENT '标签名，如 新鲜',
    `tag_type`    TINYINT     NOT NULL DEFAULT 10     COMMENT '类型：10好评 20差评',
    `sort`        INT         NOT NULL DEFAULT 0      COMMENT '排序',
    `use_count`   INT         NOT NULL DEFAULT 0      COMMENT '被使用次数',
    `status`      TINYINT     NOT NULL DEFAULT 10     COMMENT '状态：10正常 20停用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`   BIGINT      DEFAULT NULL            COMMENT '创建人ID',
    `update_by`   BIGINT      DEFAULT NULL            COMMENT '更新人ID',
    `deleted`     TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '评价标签';

-- =============================================================================
-- 八、推荐（3 张）
-- =============================================================================

CREATE TABLE IF NOT EXISTS `fm_recommend_config` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `config_name`    VARCHAR(50)  NOT NULL                COMMENT '方案名称',
    `version`        VARCHAR(20)  NOT NULL                COMMENT '版本号，如 v2026-10-05',
    `dimension`      TINYINT      NOT NULL DEFAULT 10     COMMENT '维度：10全局 20品类 30人群',
    `dimension_value` VARCHAR(50) DEFAULT NULL            COMMENT '维度取值：品类ID或人群标识',
    `weights_json`   JSON         DEFAULT NULL            COMMENT '各因素权重',
    `rules_json`     JSON         DEFAULT NULL            COMMENT '规则：置顶/屏蔽/保量',
    `status`         TINYINT      NOT NULL DEFAULT 10     COMMENT '状态：10草稿 20生效 30已停用',
    `effective_time` DATETIME     DEFAULT NULL            COMMENT '生效时间',
    `operator_id`    BIGINT       DEFAULT NULL            COMMENT '操作人ID',
    `operator_name`  VARCHAR(50)  DEFAULT NULL            COMMENT '操作人名称',
    `remark`         VARCHAR(255) DEFAULT NULL            COMMENT '备注',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`      BIGINT       DEFAULT NULL            COMMENT '创建人ID',
    `update_by`      BIGINT       DEFAULT NULL            COMMENT '更新人ID',
    `deleted`        TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_version` (`version`),
    KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '推荐权重方案';

-- 推荐留痕只追加，故仅保留 create_time
CREATE TABLE IF NOT EXISTS `fm_recommend_trace` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `request_id`     VARCHAR(64)  DEFAULT NULL            COMMENT '请求标识，一次请求多条共用',
    `member_id`      BIGINT       DEFAULT NULL            COMMENT '会员ID，游客为 NULL',
    `anonymous_id`   VARCHAR(64)  DEFAULT NULL            COMMENT '匿名标识',
    `spu_id`         BIGINT       NOT NULL                COMMENT '被推荐商品SPU ID',
    `scene`          VARCHAR(20)  NOT NULL                COMMENT '场景：HOME/DETAIL/CART',
    `rank_no`        INT          NOT NULL DEFAULT 0      COMMENT '排名',
    `score`          DECIMAL(8,6) DEFAULT NULL            COMMENT '综合得分',
    `factors_json`   JSON         DEFAULT NULL            COMMENT '各因素原始值、权重、贡献值',
    `reason_text`    VARCHAR(500) DEFAULT NULL            COMMENT '推荐理由',
    `config_version` VARCHAR(20)  DEFAULT NULL            COMMENT '权重方案版本',
    `exposed`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否曝光：0否 1是',
    `clicked`        TINYINT      NOT NULL DEFAULT 0      COMMENT '是否点击：0否 1是',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    PRIMARY KEY (`id`),
    KEY `idx_member_scene_time` (`member_id`, `scene`, `create_time`),
    KEY `idx_spu_time` (`spu_id`, `create_time`),
    KEY `idx_request` (`request_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '推荐留痕';

CREATE TABLE IF NOT EXISTS `fm_recommend_feedback` (
    `id`            BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `member_id`     BIGINT      NOT NULL                COMMENT '会员ID',
    `spu_id`        BIGINT      NOT NULL                COMMENT '商品SPU ID',
    `feedback_type` TINYINT     NOT NULL DEFAULT 10     COMMENT '类型：10不感兴趣 20喜欢',
    `scene`         VARCHAR(20) DEFAULT NULL            COMMENT '来源场景：HOME/DETAIL/CART',
    `reason`        VARCHAR(100) DEFAULT NULL           COMMENT '反馈原因',
    `create_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by`     BIGINT      DEFAULT NULL            COMMENT '创建人ID',
    `update_by`     BIGINT      DEFAULT NULL            COMMENT '更新人ID',
    `deleted`       TINYINT     NOT NULL DEFAULT 0      COMMENT '逻辑删除：0未删除 1已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_spu` (`member_id`, `spu_id`),
    KEY `idx_member` (`member_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '推荐反馈';
