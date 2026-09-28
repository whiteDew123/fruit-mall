# AGENTS.md — 基于推荐算法的水果商城

本文件是本仓库对 AI 编码 Agent 的**强制约定**。每次会话开始先读它，与它冲突的做法一律不做。
完整版（人类阅读）在 `docs/01-开发规范.md`。本文件只写四样：**事实、红线、目录地图、验收命令**。

---

## 0. 项目是什么

单商家自营水果商城（毕业设计）。三端：

- **消费者端 H5**（`web-shop`）：浏览检索、个性化推荐、购物车、下单、模拟支付、履约跟踪、售后评价
- **商家后台**（`web-admin`）：商品与特色属性建档、批次库存、订单与履约处理、售后审核、经营分析、推荐权重配置
- **后端服务**（`server`）：REST API + 多因素加权推荐模块

业务闭环：商品建档 → 浏览选购 → 购物车 → 订单确认 → 模拟支付 → 履约交付 → 售后评价。

**框架自建**：不使用若依等第三方后台框架，认证、权限、日志、分页、异常处理全部在本仓库实现。

---

## 1. 事实清单（**不要猜，按这里写**）

### 技术栈与版本

| 项 | 取值 |
|---|---|
| JDK | 17（不要使用 PATH 上的 JDK 8） |
| 后端框架 | Spring Boot 3.x（新项目默认 3.5.x） |
| 认证授权 | 自研 JWT + RBAC（`AuthInterceptor` + `@RequiresPermission`） |
| 持久层 | MyBatis-Plus（Boot 3 用 `mybatis-plus-spring-boot3-starter`） |
| 数据库 | MySQL 8 |
| 缓存 | Redis（热点商品缓存、推荐结果缓存；登录态为无状态 JWT，不落 Redis） |
| 后端构建 | Maven |
| 消费者端 | Vue 3 + Vite + Vant |
| 商家后台 | Vue 3 + Vite + Element Plus + ECharts |
| 接口文档 | Knife4j（OpenAPI 3，Jakarta 版） |
| 推荐算法 | Java，写在 `com.fruitmall.recommend` 包内，不做独立服务 |

### 命名与路径

- 根包 `com.fruitmall`；后端为**单 Maven 工程**，位于 `fruit-mall/server/`，按业务分包（package by feature）。
- 业务表前缀 `fm_`，表名小写下划线单数（`fm_product_spu`）。
- 实体 `FmProductSpu`；Mapper `FmProductSpuMapper`；Service `IFmProductSpuService` + `FmProductSpuServiceImpl`；Controller `FmProductSpuController`；入参出参 `XxxDTO` / `XxxVO` / `XxxQuery`。
- 接口路径：消费者端 `/api/shop/{模块}/{资源}`，商家后台 `/api/admin/{模块}/{资源}`，登录注册在 `/api/auth/**`。消费者与后台接口**不要混用前缀**。
- 权限标识 `{模块}:{操作}`，如 `product:create`；接口方法上标注 `@RequiresPermission("product:create")`。

### 接口与数据契约

- 统一响应 `{ code, message, data }`，`code` 与 HTTP 状态码一致（200 成功 / 400 参数 / 401 未登录 / 403 无权限 / 404 不存在 / 409 状态冲突 / 500 系统异常）。
- 分页请求 `pageNum`（从 1 开始）、`pageSize`（默认 10、上限 100）；响应 `data` 为 `{ pageNum, pageSize, total, list }`。
- 后台写操作必须标注 `@OperLog(module = "...", action = "...")`，由切面自动落库。
- 金额：Java 用 `BigDecimal`，MySQL 用 `DECIMAL(10,2)`，**禁止 double/float**。时间格式 `yyyy-MM-dd HH:mm:ss`。
- 业务表必备字段：`id`、`create_time`、`update_time`、`create_by`、`update_by`、`deleted`（逻辑删除）。审计字段由 `MetaObjectHandler` 自动填充，业务代码不手工赋值。
- 状态枚举只能定义在 `com.fruitmall.common.enums`，**禁止在代码里散落状态字符串**。

### 状态机（唯一入口，不可绕过）

订单、支付、履约、售后四个状态机。**所有状态变更必须经过对应的 `*StateMachine` 类**，禁止在 Controller 或 Mapper 中直接 `update status`。取值与迁移规则见 `docs/05-数据库设计.md`（待产出）。

---

## 2. 红线（做了就是错）

1. **不擅自加依赖。** 允许的依赖只有：`spring-boot-starter-web`、`mybatis-plus-spring-boot3-starter`、`mysql-connector-j`、`spring-boot-starter-data-redis`、`spring-boot-starter-validation`、`spring-boot-starter-aop`、`spring-security-crypto`、`jjwt`、`lombok`、`knife4j-openapi3-jakarta-spring-boot-starter`。除此之外要加先问。
2. **不引入重量级组件。** 禁止 MongoDB、Elasticsearch、RabbitMQ、Kafka、Spring Cloud、工作流引擎、多租户组件。
3. **不擅自改表结构。** 变更必须先更新 `docs/05-数据库设计.md` 与 `deploy/sql/`，再改代码；禁止自动建表。
4. **不擅自重构。** 不重命名既有类与方法，不顺手"优化"无关代码，不删除看不懂用途的代码。
5. **不在 Controller 写业务逻辑**，Controller 不直接调用 Mapper。
6. **状态变更只能经状态机**（见上）。
7. **不做超范围功能。** 优惠券、秒杀、拼团、多商家、微信小程序、真实支付、退货物流一律不做。
8. **注释与文档统一中文**，不写英文注释。
9. **不删除或跳过已有测试**，不允许为让编译通过而注释掉代码。
10. **不提交敏感信息。** 数据库密码、JWT 密钥走本地配置，禁止硬编码进仓库。

---

## 3. 目录地图

```
gratuation-project/           # 仓库根：文档与代码分开放
├── AGENTS.md                 # 本文件
├── README.md                 # 环境要求、启动步骤、演示账号
├── docs/                     # 开发文档（规范、选型、计划、数据库、接口、推荐方案）
├── 文档/                      # 学校材料（任务书、开题、论文），不参与构建
└── fruit-mall/               # ★代码目录，所有开发都在这里进行
    ├── server/               # 后端：Maven 单工程，包根 com.fruitmall
    │   └── src/main/java/com/fruitmall/
    │       ├── common/       # result / exception / annotation / aspect / interceptor / config / util / enums / constant
    │       ├── auth/         # 登录注册、JWT 签发校验、UserContext
    │       ├── product/  inventory/  cart/  order/  payment/
    │       ├── fulfillment/  aftersale/  review/  behavior/
    │       ├── recommend/    # feature / recall / rank / reason / eval / config
    │       └── stat/
    ├── web-shop/             # 消费者端 H5（Vue 3 + Vant）
    ├── web-admin/            # 商家后台（Vue 3 + Element Plus）
    └── deploy/               # deploy/sql/（建表与初始化脚本）、Nginx 配置
```

每个业务包内部固定为：`controller/`（消费者与共用）、`admin/`（后台接口）、`service/` + `service/impl/`、`mapper/`、`domain/`、`dto/`、`vo/`、`query/`。

---

## 4. 验收命令（改完必须自己跑，并贴出结果）

```bash
# 后端编译（必须通过）
cd fruit-mall/server && mvn -q -DskipTests clean package

# 后端启动（需 MySQL 与 Redis 已运行）
cd fruit-mall/server && mvn spring-boot:run

# 消费者端构建
cd fruit-mall/web-shop && npm install && npm run build

# 商家后台构建
cd fruit-mall/web-admin && npm install && npm run build
```

没有实际跑过这些命令，**不要声称任务完成**。

### 交付答复格式

每次改动完成后，回复中必须包含三行：

1. 改了哪些文件（路径列表）
2. 跑了哪些验收命令
3. 命令的实际结果

---

## 5. 工作方式

- 小步提交：一次只做一个模块的一件事，保持每一步都能编译通过。
- 先看再改：动手前先读同目录已有代码，模仿现有写法，不要发明新风格。
- 规范没覆盖的问题：按 `docs/01-开发规范.md` 的同类做法处理，并在回复中说明你的选择。
- 拿不准是否触碰红线时，先问，不要先做。

---

## 6. 已确认决策（2026-09-27，已并入上文约束）

- **认证**：自研 JWT + RBAC，不引入 Sa-Token 或其他认证框架。
- **命名**：根包名 `com.fruitmall`，业务表前缀 `fm_`，系统权限与日志表前缀 `sys_`。
- **Redis**：仅用于热点商品缓存与推荐结果缓存，登录态为无状态 JWT 不落 Redis；本机已安装 Redis，按需启动，不可用时降级为直接查库。
- **数据库**：库名 `fruit_mall`，字符集 `utf8mb4`，本机 MySQL 8.0.46 使用 `root` 账号；密码走本地配置（`application-local.yml`，已被 `.gitignore` 忽略），**禁止写入仓库**。
- **演示账号**：用 `PasswordUtil` 生成 BCrypt 密文，写入 `deploy/sql/02_init_data.sql` 并在 `README.md` 登记。
- **密码哈希**：BCrypt（`spring-security-crypto`，2026-09-28 经确认加入依赖白名单），成本因子 10；只在 `PasswordUtil` 中调用，业务代码不直接使用加密 API。
