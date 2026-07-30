# 数字化营销与客户管理平台

基于 Spring Boot 3 + Vue 3 的全栈 CRM 系统，涵盖客户全生命周期管理，集成 AI 智能评分引擎，支持精细化权限控制。

## 技术栈

| 层级   | 技术                                                         |
| ------ | ------------------------------------------------------------ |
| 前端   | Vue 3 + Vite + Element Plus + ECharts + Axios               |
| 后端   | Spring Boot 3.3 + Spring Security + MyBatis + JWT            |
| AI     | LangChain4j 0.35 + DeepSeek V4 Flash                         |
| 数据库 | MySQL 8                                                       |
| 缓存   | Redis                                                         |
| 构建   | Maven（后端）/ npm（前端）                                     |

## 项目结构

```
dlyk/
├── dlyk-front/                    # Vue 3 前端
│   ├── src/
│   │   ├── view/                  # 页面组件（29个）
│   │   ├── router/                # Vue Router 路由配置
│   │   ├── http/                  # Axios 请求封装
│   │   └── util/                  # 工具函数
│   ├── package.json
│   └── vite.config.js
│
├── dlyk-server/                   # Spring Boot 后端
│   ├── src/main/java/com/bjpowernode/
│   │   ├── web/                   # REST 控制器（16个）
│   │   ├── service/               # 业务逻辑层
│   │   ├── mapper/                # MyBatis 映射接口
│   │   ├── model/                 # 实体类
│   │   ├── scoring/               # AI 评分引擎（策略模式）
│   │   │   ├── rules/             # 评分规则实现
│   │   │   ├── LeadScoringEngine.java
│   │   │   └── ScoringRule.java
│   │   ├── config/                # 安全、过滤器、异常处理
│   │   ├── task/                  # 定时任务（缓存刷新、批量评分）
│   │   ├── aspect/                # AOP 切面（数据权限）
│   │   └── util/                  # JWT、Redis 工具
│   ├── src/main/resources/
│   │   ├── mapper/                # MyBatis XML 映射文件
│   │   └── application.yml        # 应用配置
│   └── pom.xml
│
├── database/
│   └── dlyk.sql                   # 数据库初始化脚本
│
└── README.md
```

## 核心功能模块

### 1. 市场活动管理
- 活动创建、编辑、删除（支持批量删除）
- 活动状态跟踪与数据统计
- 负责人权限控制（普通用户仅可见自己负责的活动）

### 2. 线索管理与 AI 评分引擎
- 线索录入、编辑、删除（支持 Excel 批量导入）
- 跟踪记录管理（多种方式：电话、微信、邮件等）
- **AI 智能评分**（集成 DeepSeek V4 Flash 大模型）
  - 基础属性评分（年收入、职业、年龄、贷款需求）
  - 行为活跃度评分（跟踪记录数量、最近活跃度、线索状态）
  - 时间衰减评分（创建时间越久分数越低）
  - 加分项评分（来源、产品价值、意向状态）
  - AI 语义分析评分（参考分，不计入总分）
- 评分等级：A（≥80分）、B（≥60分）、C（≥40分）、D（<40分）
- 支持手动触发评分和定时批量重评分

### 3. 客户管理
- 线索转客户（自动关联）
- 客户详情查看与编辑
- 客户数据导出（Excel）

### 4. 交易管理
- 交易阶段流转看板（创建→确认清单→交付定金→产品检验→付款成交→丢失关闭）
- 交易记录与统计
- ECharts 数据可视化

### 5. 产品管理
- 车型目录与定价管理
- 产品上下架控制

### 6. 权限管理系统
- **基于角色的细粒度权限控制**
- 权限模块分配（管理员可为用户分配模块访问权限）
- 权限管理模块仅限管理员使用（代码级强制）
- 数据权限控制（AOP 切面，按负责人过滤数据）

### 7. 字典管理
- 系统级字典类型与字典值维护
- 动态刷新前端下拉选项（内存缓存 + 定时任务）

### 8. 用户管理
- 用户增删改查
- 密码加密存储（Spring Security）
- 个人资料与密码修改

### 9. 数据看板
- ECharts 多维度统计图表
- 销售漏斗分析
- 线索来源分布

## 环境要求

- **JDK 17** 或更高版本
- **MySQL 8.0**（运行在 3306 端口）
- **Redis**（运行在 6379 端口）
- **Node.js 18+** 和 **npm**
- **Maven 3.8+**（也可使用项目自带的 `mvnw` 包装器）

## 快速启动

### 1. 数据库准备

创建 MySQL 数据库 `dlyk` 并导入 SQL 脚本：

```sql
CREATE DATABASE dlyk DEFAULT CHARACTER SET utf8mb4;
```

然后将 `database/dlyk.sql` 文件导入该数据库。

### 2. 配置环境变量（可选）

项目已为所有敏感配置提供了默认值，本地开发可直接跳过此步骤。如需自定义，设置以下环境变量：

| 环境变量          | 默认值         | 说明                    |
| ----------------- | -------------- | ----------------------- |
| `DB_USERNAME`     | root           | 数据库用户名            |
| `DB_PASSWORD`     | root           | 数据库密码              |
| `REDIS_HOST`      | 127.0.0.1      | Redis 主机地址          |
| `REDIS_PORT`      | 6379           | Redis 端口              |
| `JWT_SECRET`      | (内置默认值)   | JWT 签名密钥            |
| `DEEPSEEK_API_KEY`| (空)           | DeepSeek API Key（AI评分）|

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="你的密码"
$env:DEEPSEEK_API_KEY="你的DeepSeek API Key"
```

**Linux / macOS:**
```bash
export DB_PASSWORD="你的密码"
export DEEPSEEK_API_KEY="你的DeepSeek API Key"
```

> **注意：** 如不配置 `DEEPSEEK_API_KEY`，AI 评分功能将使用默认分数（50分），其他评分规则正常工作。

### 3. 启动后端

```bash
cd dlyk-server

# Windows
直接运行main方法(DlykServerApplication.java文件)

# Linux / macOS
./mvnw spring-boot:run
```

后端启动在 **8089 端口**。

### 4. 启动前端

```bash
cd dlyk-front

# 首次运行需安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器启动在 **5173 端口**，浏览器打开 `http://localhost:5173`。

### 5. 登录

默认管理员账号：

| 字段 | 值     |
| ---- | ------ |
| 账号 | admin  |
| 密码 | 123456 |

## AI 评分引擎架构

评分引擎采用**策略模式**设计，支持灵活扩展：

```
ScoringRule (接口)
    ├── AttributeScoringRule    # 基础属性评分 (权重 40%)
    ├── BehaviorScoringRule     # 行为活跃度评分 (权重 30%)
    ├── TimeDecayScoringRule    # 时间衰减评分 (权重 20%)
    ├── BonusScoringRule        # 加分项评分 (权重 10%)
    └── AiScoringRule           # AI 语义分析评分 (权重 30%，参考分)
```

**评分流程：**
1. 构建评分上下文（线索信息、跟踪记录、产品价格等）
2. 依次执行 5 个评分规则
3. 计算加权总分（排除 AI 分）
4. 确定评分等级（A/B/C/D）
5. 更新数据库并返回评分报告

**AI 评分配置：**

编辑 `application.yml`：
```yaml
ai:
  scoring:
    enabled: true
    deepseek:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      model-name: deepseek-v4-flash
      temperature: 0.7
      timeout: 30000
```

## 生产部署

### 后端

```bash
cd dlyk-server
./mvnw package -DskipTests
```

JAR 文件生成在 `target/dlyk-server-0.0.1-SNAPSHOT.jar`，运行时通过环境变量注入生产配置：

```bash
export DB_PASSWORD="生产环境密码"
export JWT_SECRET="生产环境密钥"
export DEEPSEEK_API_KEY="DeepSeek API Key"
java -jar target/dlyk-server-0.0.1-SNAPSHOT.jar
```

### 前端

```bash
cd dlyk-front
npm run build
```

静态文件生成在 `dist/` 目录，使用 **Nginx** 或任意静态文件服务器部署。Nginx 配置示例：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /path/to/dlyk-front/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8089;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 配置说明

[application.yml](dlyk-server/src/main/resources/application.yml) 中的关键配置项均支持环境变量覆盖，格式为 `${环境变量名:默认值}`。

| 配置属性                        | 环境变量          | 默认值             | 说明              |
| ------------------------------- | ----------------- | ------------------ | ----------------- |
| `server.port`                   | —                 | 8089               | 后端端口          |
| `spring.datasource.url`         | —                 | `jdbc:mysql://...` | MySQL 连接        |
| `spring.datasource.username`    | `DB_USERNAME`     | root               | 数据库用户名      |
| `spring.datasource.password`    | `DB_PASSWORD`     | root               | 数据库密码        |
| `spring.data.redis.host`        | `REDIS_HOST`      | 127.0.0.1          | Redis 主机        |
| `spring.data.redis.port`        | `REDIS_PORT`      | 6379               | Redis 端口        |
| `jwt.secret`                    | `JWT_SECRET`      | (内置默认值)       | JWT 签名密钥      |
| `ai.scoring.enabled`            | —                 | true               | 启用 AI 评分      |
| `ai.scoring.deepseek.api-key`   | `DEEPSEEK_API_KEY`| (空)               | DeepSeek API Key  |
| `ai.scoring.deepseek.model-name`| —                 | deepseek-v4-flash  | AI 模型名称       |

## 开发特性

- **输入校验**：基于 JSR-303 Bean Validation，统一返回 400 状态码
- **全局异常处理**：统一错误响应格式
- **数据权限控制**：AOP 切面自动过滤数据（按负责人）
- **缓存机制**：字典数据内存缓存 + 定时刷新
- **事务管理**：关键业务操作使用 `@Transactional`
- **安全加固**：JWT 密钥空值防御、登录参数校验

## 许可证

本项目仅用于学习目的。

---

**最后更新：** 2026年7月30日
