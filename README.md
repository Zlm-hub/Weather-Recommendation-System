# 天气生活推荐系统

基于Spring+Boot的多源数据智能天气生活推荐系统

## 项目简介

本项目是一个毕业设计作品，旨在为用户提供基于实时气象数据的个性化生活建议。系统通过对接第三方天气 API（和风天气/心知天气），获取实时天气、预报数据，结合内置的智能推荐规则引擎，自动生成穿衣、出行、健康、活动四大类生活建议。同时集成了 AI 对话助手，支持自然语言天气问答。

## 技术架构

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.15 | 基础框架 |
| Spring Security | - | 安全认证框架 |
| MyBatis-Plus | 3.5.3.1 | ORM 持久层框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | 缓存中间件 |
| JWT (jjwt) | 0.9.1 | Token 认证 |
| Hutool | 5.8.20 | Java 工具库 |
| FastJSON | 2.0.32 | JSON 解析 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.3.8 | 渐进式 JavaScript 框架 |
| Vue Router | 4.2.5 | 路由管理 |
| Element Plus | 2.4.3 | UI 组件库 |
| ECharts | 5.6.0 | 数据可视化图表 |
| Axios | 1.6.2 | HTTP 请求库 |
| Vite | 5.0.0 | 构建工具 |

### 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                          │
│   ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────────┐   │
│   │Element  │  │ECharts  │  │VueRouter│  │   Axios     │   │
│   │  Plus   │  │ 图表组件 │  │ 路由管理 │  │ HTTP 请求   │   │
│   └─────────┘  └─────────┘  └─────────┘  └─────────────┘   │
└─────────────────────────┬───────────────────────────────────┘
                          │ RESTful API
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                     后端 (Spring Boot)                       │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Controller 控制层                   │  │
│  │  UserController / WeatherController / RecommendController │
│  └────────────────────────┬─────────────────────────────┘  │
│                           ▼                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Service 业务层                      │  │
│  │  WeatherApiService / RecommendationEngine / AiService │  │
│  └────────────────────────┬─────────────────────────────┘  │
│                           ▼                                 │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    DAO 持久层                          │  │
│  │              MyBatis-Plus / MySQL Mapper              │  │
│  └────────────────────────┬─────────────────────────────┘  │
└───────────────────────────┼─────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       数据存储层                             │
│        ┌──────────┐              ┌──────────┐              │
│        │  MySQL   │              │  Redis   │              │
│        │  数据库   │              │  缓存    │              │
│        └──────────┘              └──────────┘              │
└─────────────────────────────────────────────────────────────┘
                            ▲
                            │ HTTP API
        ┌───────────────────┴───────────────────┐
        │              外部服务                   │
        │   和风天气 API / 心知天气 API           │
        └───────────────────────────────────────┘
```

## 功能特性

### 前台用户端

- **气象数据看板**：实时天气、24 小时预报、5 天趋势图表
- **个性化智能指南**：穿衣、出行、健康、活动四大类推荐
- **AI 对话助手**：自然语言天气问答、生活咨询
- **个人中心**：个人信息管理、偏好设置、历史反馈

### 后台管理端

- **多源数据监控**：API 状态监控、数据采集日志
- **推荐规则管理**：规则新增、编辑、启停、优先级调整
- **用户权限管控**：用户列表、账号启停



## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/weather-recommend-system.git
cd weather-recommend-system
```

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE weather_recommend DEFAULT CHARACTER SET utf8mb4;

-- 导入表结构（执行 docs/sql/schema.sql）
```

### 3. 后端配置

修改 `src/main/resources/application.yml`：

## 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/weather_recommend?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    password: your_redis_password

weather:
  api:
    xinzhi:
      key: your_api_key  # 心知天气 API Key

jwt:
  secret: your_jwt_secret_key
```

### 4. 启动后端

```bash
mvn spring-boot:run
```

后端服务运行在 `http://localhost:8080/api`

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端服务运行在 `http://localhost:5173`

## 项目结构

```
weather-recommend-system/
├── src/main/java/com/weather/
│   ├── config/              # 配置类 (Security, JWT, CORS)
│   ├── controller/          # 控制器层
│   ├── service/             # 业务逻辑层
│   ├── mapper/              # MyBatis Mapper
│   ├── entity/              # 实体类
│   ├── vo/                  # 视图对象
│   ├── dto/                 # 数据传输对象
│   ├── util/                # 工具类
│   └── exception/           # 异常处理
├── src/main/resources/
│   ├── application.yml      # 主配置文件
│   └── mybatis/             # MyBatis XML 映射文件
├── frontend/                # Vue 3 前端项目
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── api/             # API 接口
│   │   └── assets/          # 静态资源
│   └── package.json
├── docs/                    # 文档目录
│   ├── database-schema.md   # 数据库表结构
│   └── diagrams/            # 论文图表
└── pom.xml                  # Maven 配置
```

## 核心模块说明

### 推荐引擎服务 (RecommendationEngineService)

基于规则的推荐引擎，通过遍历规则库匹配当前气象条件，生成个性化生活建议：

1. 获取启用的推荐规则列表
2. 遍历规则，匹配温度、天气类型、空气质量等条件
3. 按优先级排序，输出推荐结果

### 天气 API 服务 (WeatherApiService)

对接第三方天气 API，实现多源数据获取与异构数据清洗：

- 支持和风天气、心知天气两个数据源
- 自动解析 JSON 响应，适配不同数据格式
- 数据缓存到 Redis，减少 API 调用

### JWT 安全认证 (JwtAuthenticationFilter)

基于 JWT 的无状态认证机制：

- 用户登录成功后生成 Token
- 后续请求携带 Token 进行身份验证
- Token 存储在 Redis 中，支持主动失效

## API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /api/auth/login | POST | 用户登录 |
| /api/auth/register | POST | 用户注册 |
| /api/weather/now | GET | 获取实时天气 |
| /api/weather/forecast | GET | 获取天气预报 |
| /api/recommend/generate | GET | 生成推荐建议 |
| /api/ai/chat | POST | AI 对话 |
| /api/user/profile | GET | 获取用户信息 |
| /api/user/update | PUT | 更新用户信息 |

## 截图预览

> 前台首页 - 气象数据看板
> 推荐建议页面
> AI 对话助手
> 后台管理界面

## 作者

毕业设计项目 - 天气生活推荐系统

## 许可证

MIT License
