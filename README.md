# 数字化营销与客户管理平台

全栈 CRM 系统，涵盖客户管理、线索追踪、交易管理、产品目录和系统配置。

## 技术栈

| 层级   | 技术                                               |
| ------ | -------------------------------------------------- |
| 前端   | Vue 3 + Vite + Element Plus + ECharts + Axios      |
| 后端   | Spring Boot 3.3 + Spring Security + MyBatis + JWT  |
| 数据库 | MySQL 8                                             |
| 缓存   | Redis                                               |
| 构建   | Maven（后端）/ npm（前端）                           |

## 项目结构

```
dlyk/
├── dlyk-front/          # Vue 3 前端
│   ├── src/
│   │   ├── view/        # 页面组件
│   │   ├── router/      # Vue Router 路由配置
│   │   ├── http/        # Axios 请求封装
│   │   └── util/        # 工具函数
│   ├── package.json
│   └── vite.config.js
├── dlyk-server/         # Spring Boot 后端
│   ├── src/main/java/com/bjpowernode/
│   │   ├── web/         # REST 控制器
│   │   ├── service/     # 业务逻辑
│   │   ├── mapper/      # MyBatis 映射接口
│   │   ├── model/       # 实体类
│   │   ├── config/      # 安全与过滤器配置
│   │   └── util/        # JWT 与 JSON 工具
│   ├── src/main/resources/
│   │   ├── mapper/      # MyBatis XML 映射文件
│   │   └── application.yml
│   └── pom.xml
└── README.md
```

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

然后将项目中提供的 `dlyk.sql` 文件导入该数据库。

项目默认数据库连接信息：

| 配置项   | 值             |
| -------- | -------------- |
| 主机     | 127.0.0.1:3306 |
| 数据库   | dlyk           |
| 用户名   | root           |
| 密码     | root           |

可在 [application.yml](dlyk-server/src/main/resources/application.yml) 中修改。

### 2. 启动后端

```bash
cd dlyk-server

# Windows
mvnw spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

后端启动在 **8089 端口**。

### 3. 启动前端

```bash
cd dlyk-front

# 首次运行需安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发服务器启动在 **5173 端口**，浏览器打开 `http://localhost:5173`。

### 4. 登录

默认管理员账号：

| 字段 | 值     |
| ---- | ------ |
| 账号 | admin  |
| 密码 | 123456 |

## 生产部署

### 后端

```bash
cd dlyk-server
./mvnw package -DskipTests
```

JAR 文件生成在 `target/dlyk-server-0.0.1-SNAPSHOT.jar`，运行：

```bash
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

## 功能模块

- **仪表盘** —— ECharts 图表展示销售统计数据
- **用户管理** —— 用户增删改查，基于角色的权限控制
- **市场活动** —— 管理营销活动及参与情况
- **线索管理** —— 录入和跟进销售线索
- **客户管理** —— 线索转客户、查看详情、导出 Excel
- **交易管理** —— 跟踪交易阶段流转（创建→确认清单→交付定金→产品检验→付款成交→丢失关闭）
- **产品管理** —— 管理车型目录与定价
- **字典管理** —— 维护下拉选项（线索来源、交易阶段等）
- **系统管理** —— 管理系统级配置信息
- **个人中心** —— 修改个人资料和密码

## 配置说明

[application.yml](dlyk-server/src/main/resources/application.yml) 中的关键配置项：

| 配置属性                        | 默认值                 | 说明         |
| ------------------------------- | ---------------------- | ------------ |
| `server.port`                   | 8089                   | 后端端口     |
| `spring.datasource.url`         | `jdbc:mysql://...`     | MySQL 连接   |
| `spring.datasource.username`    | root                   | 数据库用户名 |
| `spring.datasource.password`    | root                   | 数据库密码   |
| `spring.data.redis.host`        | 127.0.0.1              | Redis 主机   |
| `spring.data.redis.port`        | 6379                   | Redis 端口   |

## 许可证

本项目仅用于学习目的。