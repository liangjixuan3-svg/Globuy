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

### 环境要求

- JDK 24（与当前 `pom.xml` 一致）
- MySQL 8 及以上
- Node.js 24
- npm 或 yarn

### 1. 初始化数据库

创建空数据库 `secondhand`，将 `sql/schema.sql` 导入该空数据库。公开脚本不含原始账号或业务数据，用户需自行注册；管理员账号需自行初始化。
复制 `src/main/resources/application.example.yaml` 为以下本地配置文件，并设置 `DB_HOST`、`DB_USERNAME`、`DB_PASSWORD`、`DEEPSEEK_API_KEY` 环境变量：

```text
src/main/resources/application.yaml
```

### 2. 启动后端

使用 IDEA 或其他 Java IDE 打开项目，运行：

```text
src/main/java/com/example/springboot/SpringbootApplication.java
```

### 3. 启动前端

```bash
cd vue
npm install
npm run dev
```

如果你本地使用 `yarn`，也可以执行：

```bash
yarn
yarn dev
```

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
