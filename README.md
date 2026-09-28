# 基于推荐算法的水果商城（毕业设计）

单商家自营水果商城，三端一体：消费者端 H5、商家后台、后端服务。推荐算法以纯 Java 实现，
采用"多通道召回 + 多因素加权排序"的两阶段方案，不做独立推荐服务。

> 开发约定见根目录 `AGENTS.md`（强制）与 `docs/01-开发规范.md`（完整版）。

---

## 一、环境要求

| 项 | 要求 | 本机实测 |
|---|---|---|
| JDK | 17 | `E:\developmentTools\java17`（`JAVA_HOME` 已指向此目录） |
| Maven | 3.9.x | 3.9.9 |
| MySQL | 8.x | 8.0.46，服务名 `MySQL80`，端口 3306 |
| Redis | 用于热点商品与推荐结果缓存 | 本机已安装，按需启动 |
| Node.js | 18+ | v24.19.0（npm 11.4.2） |
| Git | 用于版本管理与每日提交 | 2.55.0 |

注意：命令行直接执行 `java -version` 可能仍指向 PATH 上的 JDK 8，Maven 与 IDEA 走 `JAVA_HOME`
不受影响。首次构建前请在 Maven `settings.xml` 配置国内镜像，避免依赖下载超时。

---

## 二、目录结构

```
gratuation-project/
├── AGENTS.md                 # AI Agent 强制约定：事实、红线、目录地图、验收命令
├── README.md                 # 本文件
├── docs/                     # 开发文档（怎么建）
│   ├── 01-开发规范.md
│   ├── 02-技术说明.md
│   ├── 03-开发计划.md
│   ├── 04-推荐算法实现方案.md
│   └── 05-数据库设计.md
├── 文档/                      # 学校材料（任务书、开题、论文），不参与构建
└── fruit-mall/               # ★ 代码目录
    ├── server/               # 后端：Maven 单工程，包根 com.fruitmall
    ├── web-shop/             # 消费者端 H5：Vue 3 + Vite + Vant
    ├── web-admin/            # 商家后台：Vue 3 + Vite + Element Plus
    └── deploy/
        ├── sql/              # 建表与初始化脚本（01_schema / 02_init_data / 03_mock_data）
        └── nginx/            # Nginx 配置（后期补充）
```

---

## 三、数据库初始化

PowerShell 不支持 `<` 输入重定向，用 `source` 方式执行；脚本内不含 `DROP TABLE`，
重复执行不会清空已有数据。

```powershell
# 建库与建表
mysql -uroot -p -e "source E:/gratuation-project/fruit-mall/deploy/sql/01_schema.sql"

# 初始化角色与权限菜单（演示账号待认证模块完成后补充）
mysql -uroot -p fruit_mall -e "source E:/gratuation-project/fruit-mall/deploy/sql/02_init_data.sql"

# 校验结果：应列出 35 张表
mysql -uroot -p -e "use fruit_mall; show tables;"
```

如遇 `mysql` 不在 PATH，使用完整路径：`& 'C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe'`。
在 cmd 中也可用重定向写法：`mysql -uroot -p < fruit-mall\deploy\sql\01_schema.sql`。

需要重建库时手工执行 `DROP DATABASE fruit_mall;` 后再跑脚本——**DROP 语句刻意不放在脚本里**，
避免误执行导致数据丢失。

数据库名 `fruit_mall`，字符集 `utf8mb4`，排序规则 `utf8mb4_general_ci`。
表结构以 `docs/05-数据库设计.md` 为准，**禁止手工改库不入脚本**。

---

## 四、启动步骤

> 后端骨架已完成（2026-09-28），两个前端工程尚未开始；本节命令随进度逐条补齐，最终作为答辩演示的操作依据。

首次运行前先复制本地配置模板并填入数据库密码：

```powershell
Copy-Item fruit-mall\server\src\main\resources\application-local.yml.example `
          fruit-mall\server\src\main\resources\application-local.yml
```

```bash
# 后端编译（必须通过）
cd fruit-mall/server && mvn -q -DskipTests clean package

# 后端启动（需 MySQL 与 Redis 已运行）
cd fruit-mall/server && mvn spring-boot:run

# 消费者端
cd fruit-mall/web-shop && npm install && npm run dev

# 商家后台
cd fruit-mall/web-admin && npm install && npm run dev
```

端口约定：后端 8080，消费者端 5173，商家后台 5174。

启动后接口文档地址：

| 地址 | 说明 |
|---|---|
| `http://localhost:8080/doc.html` | Knife4j 接口文档页 |
| `http://localhost:8080/v3/api-docs` | OpenAPI 3 规范（JSON） |

后端骨架已完成的能力：统一响应 `{ code, message, data }`（响应码与 HTTP 状态码一致）、全局异常处理、
MyBatis-Plus 分页插件（每页上限 100）与审计字段自动填充、Knife4j 按 `/api/shop`、`/api/admin`、`/api/auth` 分组。

---

## 五、演示账号

待认证模块落地后，用 `PasswordUtil` 生成 BCrypt 密文写入 `02_init_data.sql`，
届时在此登记账号与角色：系统管理员、商家运营人员、履约与售后人员、注册会员各一个。

---

## 六、文档索引

| 文档 | 用途 |
|---|---|
| `docs/01-开发规范.md` | 分层职责、命名、接口、数据库、状态机与事务规范 |
| `docs/02-技术说明.md` | 技术栈清单、每项的用途与选型理由（论文第 2 章素材） |
| `docs/03-开发计划.md` | 冲刺计划、验收标准、砍范围预案 |
| `docs/04-推荐算法实现方案.md` | 推荐模块的流程、类设计与代码骨架 |
| `docs/05-数据库设计.md` | 表字典、状态枚举取值、状态机迁移规则、索引与一致性约束 |
