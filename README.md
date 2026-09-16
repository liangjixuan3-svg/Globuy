# Globuy

> 一个基于 SpringBoot 和 Vue 3 的留学生二手交易平台。

## 在线体验（无需本地部署）

[点击进入在线体验](https://liangjixuan3-svg.github.io/Globuy/)

打开在线体验链接后默认进入普通用户首页。页面顶部可切换到登录页：

- 普通用户：账号 `demo`，密码 `demo`，角色选择“普通用户”
- 后台管理员：账号 `admin`，密码 `demo`，角色选择“管理员”

演示版复用原有界面及作者确认可公开的原始虚构业务数据，包括商品名称、价格、描述、图片、用户、地址、订单和聊天。账号密码字段不导出，演示登录统一使用密码 `demo`。
原始图片中有两张本地文件缺失，演示显示“原图片文件缺失”；其余引用的 211 张图片使用原文件。
可体验商品浏览、搜索、分类筛选、收藏、模拟下单与订单流转，以及后台数据统计和管理。
支付不会收款，聊天仅在本地模拟，不建立真实 WebSocket 连接；AI、邮箱认证、注册、修改密码及文件上传不连接真实服务。
汇率为固定演示值，不是实时金融数据。修改仅保存在页面内存中，刷新即恢复；请勿输入私人信息。

## 产品文档

面向产品岗位的项目说明与需求设计，见 [产品文档目录](产品文档/README.md)。

- [Globuy 产品需求文档 PRD](产品文档/PRD.md)：用户场景、需求优先级、业务规则、验收标准、指标与迭代规划。

## 隐私与发布说明

- 原始 `sql/secondhand.sql`、`files/` 上传目录及本地 `application.yaml` 全部排除，不进入公开 Git 历史。
- `sql/schema.sql` 仅保留数据库结构，不包含原始数据或导出环境元信息。
- 使用 `application.example.yaml` 提供配置示例；真实数据库密码和 AI 密钥通过环境变量配置。
- `.gitignore` 使用根目录允许清单；上传前运行 `node scripts/security-audit.mjs` 审计 Git 暂存内容。
- GitHub Actions 再次审计已提交文件、运行测试并发布演示构建；不需要配置任何真实业务密钥。
- 构建不发布 source map。授权说明见 [NOTICE.md](NOTICE.md)；公开展示不等于授予任意使用许可。

Globuy 是一个面向留学生群体的前后端分离二手交易平台毕业设计项目，围绕商品发布、商品浏览、订单交易、实时聊天、多币种展示、校园邮箱认证以及后台管理等核心场景进行设计与实现。

## 功能特性

### 前台功能

- 用户注册、登录与 JWT 身份认证
- 首页推荐、商品搜索、分类筛选与地区筛选
- 商品发布，支持主图、多图和富文本详情描述
- 商品详情查看、收藏商品、发起下单
- 订单生成、支付、发货、收货、评价
- 收藏夹管理
- 基于 WebSocket 的实时单聊
- 个人信息管理与收货地址管理
- 校园邮箱认证
- 多币种价格展示与换算

### 后台功能

- 后台首页数据统计与图表展示
- 用户管理
- 商品管理
- 订单管理
- 公告管理
- 轮播图管理
- 商品分类管理

## 技术栈

### 后端

- SpringBoot
- MyBatis-Plus
- MySQL
- JWT
- WebSocket
- Hutool

### 前端

- Vue 3
- Vue Router
- Axios
- Element Plus
- ECharts
- WangEditor

## 项目结构

```text
Globuy
├── src/main/java/com/example/springboot
│   ├── common          公共类与 WebSocket 服务
│   ├── config          拦截器、跨域、WebSocket 配置
│   ├── controller      接口控制层
│   ├── entity          实体类
│   ├── exception       全局异常处理
│   ├── mapper          MyBatis-Plus 数据访问层
│   ├── service         业务接口层
│   ├── service/impl    业务实现层
│   └── utils           工具类，如 TokenUtils
├── src/main/resources  后端配置文件
├── sql                 数据库脚本
├── vue                 前端项目
└── files               上传文件存储目录

```

## 快速开始

以下步骤用于运行**本地真实后端版本**，不是 GitHub Pages 静态演示。在线演示的 `demo / demo`、`admin / demo` 不会自动创建到本地数据库中。

### 环境要求

- JDK 24（与当前 `pom.xml` 一致）
- MySQL 8，服务已启动，能够通过 `mysql` 客户端连接
- Maven（项目未附带 Maven Wrapper，需能运行 `mvn`）
- Node.js 24
- npm（下文统一使用 npm）

先检查命令是否可用；`java` 与 Maven 使用的 Java 都应为 24：

```bash
java -version
mvn -version
mysql --version
node -v
npm -v
```

### 1. 获取项目

```bash
git clone https://github.com/liangjixuan3-svg/Globuy.git
cd Globuy
```

除前端步骤外，下文命令均在项目根目录执行。已有本地项目时直接进入其根目录，不必重复克隆。

### 2. 创建数据库并导入表结构

在项目根目录打开终端，连接 MySQL。`-p` 会交互询问数据库密码，不要把真实密码直接拼在命令里：

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p
```

进入 `mysql>` 后依次执行：

```sql
CREATE DATABASE IF NOT EXISTS secondhand
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE secondhand;
SHOW TABLES;
```

确认这是供本项目使用的空数据库，再导入：

```sql
SOURCE sql/schema.sql;
SHOW TABLES;
DESCRIBE sys_user;
DESCRIBE goods;
```

`SOURCE` 按启动 MySQL 客户端时的工作目录解析文件路径；如果提示无法打开文件，改用 `sql/schema.sql` 的绝对路径。Navicat / DataGrip 用户也可创建同名空库，在该库执行 [schema.sql](sql/schema.sql)。

成功后应有 10 张表：`address`、`banner`、`chat`、`collect`、`goods`、`notice`、`orders`、`sys_admin`、`sys_user`、`type`。

注意：脚本只建表，不创建数据库、不插入业务数据，也不删除已有表。若 `SHOW TABLES` 已有业务表，请先确认是否属于本项目并备份，不要重复导入或删除数据来处理报错。

### 3. 初始化本地管理员

表结构导入后，在同一个 MySQL 会话执行以下示例。先把占位文本替换为**自行设置的本地测试密码**，不要使用真实服务密码，也不要将替换后的 SQL 提交到 GitHub：

```sql
USE secondhand;
SELECT id, username, nickname FROM sys_admin WHERE username = 'local_admin';
-- 仅在不存在该账号时执行下面的 INSERT。
INSERT INTO sys_admin (username, password, nickname)
VALUES ('local_admin', '请替换为自行设置的本地测试密码', '本地管理员');
SELECT id, username, nickname FROM sys_admin;
EXIT;
```

登录时使用 `local_admin`、你设置的密码，角色选择“管理员”。当前原项目直接比较数据库中的密码字符串，所以上述初始化与现有逻辑一致；这不是生产级密码存储方案，本地测试之外需先完善安全机制。查询示例不输出密码列。

普通用户启动前后端后通过注册页面创建，不必手工插入 `sys_user`。注册并登录后可维护个人资料、收货地址和发布商品。

### 4. 配置后端连接信息

将 `src/main/resources/application.example.yaml` **复制为**同目录下的 `application.yaml`。如果本地已有该文件，保留原文件并对照检查，不要直接覆盖。

示例默认连接 `127.0.0.1:3306/secondhand`，后端端口为 `9090`。数据库名或端口不同时，修改本地 `application.yaml` 的 JDBC URL；该文件已被 Git 忽略。

macOS / Linux 在**用于启动后端的同一个终端**设置环境变量：

```bash
export DB_HOST=127.0.0.1
export DB_USERNAME=root
read -s -p '请输入本地数据库密码：' DB_PASSWORD
export DB_PASSWORD
```

上述 `read -s -p` 使用 Bash 语法；macOS 默认 zsh 可用下面的交互输入替代该行：

```zsh
read -s 'DB_PASSWORD?请输入本地数据库密码：'
export DB_PASSWORD
```

Windows PowerShell 使用：

```powershell
$env:DB_HOST = '127.0.0.1'
$env:DB_USERNAME = 'root'
$globuyDbCredential = Get-Credential -UserName 'root' -Message '请输入本地 MySQL 密码'
$env:DB_PASSWORD = $globuyDbCredential.GetNetworkCredential().Password
```

`DB_PASSWORD` 是 **MySQL 连接密码**，与 `local_admin` 的网站登录密码是两个不同概念。正式环境应使用权限受限的专用数据库账号，不应直接使用 root。

AI 功能可选：不设置 `DEEPSEEK_API_KEY` 也可启动基础功能，原项目在模型不可用时有回退逻辑；若需要真实调用，在本地环境设置自己的密钥。**不要把数据库密码或模型密钥写入 README、前端配置、截图或提交文件。**

### 5. 启动后端

在配置好环境变量的终端、项目根目录执行：

```bash
mvn org.springframework.boot:spring-boot-maven-plugin:3.5.0:run
```

当前 `pom.xml` 未显式声明 Spring Boot Maven 插件，所以上面使用完整插件坐标执行，版本与项目的 Spring Boot 父项目一致。

也可用 IDEA 打开根目录 `pom.xml`，选择 JDK 24、等待 Maven 依赖导入，再运行 `SpringbootApplication`。IDEA 的运行配置中需单独设置上述环境变量，并将工作目录设为项目根目录；终端变量不一定会传递给已打开的 IDEA。

日志出现 `Started SpringbootApplication` 且端口为 `9090` 后再启动前端。后端是 API 服务，直接打开根地址可能显示 404，不代表启动失败。上传文件会保存到后端工作目录的 `files/`，无需导入原有私有上传目录；该目录不要提交。

### 6. 启动前端并访问

另开一个终端，进入项目根目录后执行：

```bash
cd vue
npm ci
npm run dev
```

浏览器打开终端中 Vite 输出的 `Local` 地址，通常为 `http://localhost:5173/`；若端口被占用，以实际输出为准。前端默认连接 `http://127.0.0.1:9090`，配置在 [config.default.js](vue/config/config.default.js)。若调整后端端口，需同步修改前端配置并重启前端；部分原页面还有本地地址引用，本说明仅覆盖默认本机部署。

本地真实后端模式下不要设置 `VITE_DEMO_MODE=true`。若此前打开过静态演示或其他账号，建议使用新的浏览器无痕窗口登录本地站点，避免旧登录状态干扰。

### 7. 补充基础数据并验证流程

空库没有商品、分类、公告或轮播，首页空白区域不代表导入失败。建议按以下顺序操作：

1. 用 `local_admin` 登录后台，先在分类管理新增分类；需要首页热门展示的分类设置推荐状态。按需新增公告与轮播。
2. 注册一个卖家普通用户，填写资料并发布商品，使用本地上传的图片，不需要复制演示数据或原 SQL 转储。
3. 注册另一个买家普通用户，在个人中心新增收货地址。
4. 买家搜索商品并下单，取消支付后检查“我买到的”仍有待支付订单和 5 分钟倒计时；取消订单应释放商品。
5. 重新下单后体验支付确认、卖家发货、买家收货和评价。当前支付只是状态确认，**不要进行真实转账**。

可通过 MySQL 做只读检查，确认页面操作确实写入本地库：

```sql
USE secondhand;
SELECT id, username, nickname FROM sys_user;
SELECT id, name, type_id, user_id, price, country, status FROM goods ORDER BY id DESC;
SELECT id, no, item_id, from_id, to_id, status, time FROM orders ORDER BY id DESC;
SELECT COUNT(*) AS address_count FROM address;
```

不要直接手工更新订单状态来代替页面验收：订单与商品占用需要联动，单独改一张表可能造成不一致。

### 常见问题

| 现象 | 检查方法 |
| --- | --- |
| `Unknown database 'secondhand'` | 确认建库成功，JDBC 数据库名与实际库名一致 |
| `Table ... doesn't exist` | 确认在 `secondhand` 导入了完整脚本，并用 `SHOW TABLES` 检查 10 张表 |
| `Table ... already exists` | 不要重复导入；先核对已有表结构，保留并备份已有数据 |
| `Access denied for user` | 先用相同主机、端口和用户名运行 `mysql ... -p` 验证，核对后端环境变量与权限 |
| 无法连接 MySQL | 确认 MySQL 服务运行、端口为 3306；非默认端口需修改 JDBC URL |
| Java 编译版本不匹配 | 检查 `mvn -version` 的 Java 版本及 IDEA 项目 SDK 是否都是 24 |
| 前端网络请求失败 | 确认后端 9090 启动，前端连接地址正确，并查看浏览器 Network 与后端日志 |
| 本地无法用 `demo / demo` 登录 | 在线演示账号只属于静态演示；本地应注册或使用自行初始化的管理员 |
| 无商品或发布时分类为空 | 空库只含表结构，先在后台新增分类，再用普通用户发布商品 |
| 上传后的图片无法访问 | 检查后端工作目录、`files/` 写入权限，以及图片 URL 是否指向正确的本地后端 |

## 核心模块说明

### 商品模块

- 发布商品
- 浏览商品
- 搜索与筛选商品
- 查看商品详情
- 收藏商品

### 订单模块

- 从商品详情页发起下单
- 进入收银台支付
- 订单状态流转
- 发货、收货与评价

### 聊天模块

- 建立用户会话
- 基于 WebSocket 实现实时消息推送
- 聊天记录持久化
- 未读消息与在线状态处理

### 后台管理模块

- 平台基础信息维护
- 用户、商品、订单统一管理
- 首页统计图表展示

## 数据库概览

系统核心数据表包括：

- `sys_user` 用户表
- `sys_admin` 管理员表
- `goods` 商品表
- `orders` 订单表
- `address` 收货地址表
- `collect` 收藏表
- `chat` 聊天记录表
- `type` 商品分类表
- `notice` 公告表
- `banner` 轮播图表

这些数据表共同支撑了从商品发布、商品浏览到订单交易、聊天沟通和后台管理的完整业务流程。

## 开发说明

### 演示构建与验证

```bash
cd vue
npm ci
npm test
VITE_DEMO_MODE=true npm run build
VITE_DEMO_MODE=true npm run preview
```

预览路径为 `/Globuy/`。不设置 `VITE_DEMO_MODE` 时仍使用原有 Java 后端和 history 路由。
演示接口独立位于 `vue/src/demo/api.js`，没有修改 Java 业务实现。
GitHub Pages 发布流程位于 `.github/workflows/pages.yml`，主分支推送后自动部署。

- 后端整体采用分层架构：`Controller -> Service -> Mapper -> Database`
- 使用 JWT 进行登录认证和接口拦截
- 使用 WebSocket 实现实时聊天
- 使用 MyBatis-Plus 简化增删改查和分页开发
- 商品详情富文本使用 WangEditor 实现

## 后续可扩展方向

- 完善支付能力
- 增加消息通知机制
- 优化订单并发控制
- 增加自动化测试
- 增加容器化部署支持

## 使用说明

本项目主要用于本科毕业设计、学习交流和功能演示。
