# DLYK (动力云客) -- Customer Relationship Management System

A full-stack CRM system for BYD electric vehicle dealerships, covering customer management, lead tracking, transaction management, product catalog, and system configuration.

## Tech Stack

| Layer    | Technology                                          |
| -------- | --------------------------------------------------- |
| Frontend | Vue 3 + Vite + Element Plus + ECharts + Axios       |
| Backend  | Spring Boot 3.3 + Spring Security + MyBatis + JWT   |
| Database | MySQL 8                                              |
| Cache    | Redis                                                |
| Build    | Maven (backend) / npm (frontend)                    |

## Project Structure

```
dlyk/
├── dlyk-front/          # Vue 3 frontend
│   ├── src/
│   │   ├── view/        # Page components
│   │   ├── router/      # Vue Router config
│   │   ├── http/        # Axios request wrapper
│   │   └── util/        # Utility functions
│   ├── package.json
│   └── vite.config.js
├── dlyk-server/         # Spring Boot backend
│   ├── src/main/java/com/bjpowernode/
│   │   ├── web/         # REST controllers
│   │   ├── service/     # Business logic
│   │   ├── mapper/      # MyBatis mapper interfaces
│   │   ├── model/       # Entity classes
│   │   ├── config/      # Security & filter config
│   │   └── util/        # JWT & JSON utilities
│   ├── src/main/resources/
│   │   ├── mapper/      # MyBatis XML mappings
│   │   └── application.yml
│   └── pom.xml
└── README.md
```

## Prerequisites

- **JDK 17** or later
- **MySQL 8.0** (running on port 3306)
- **Redis** (running on port 6379)
- **Node.js 18+** and **npm**
- **Maven 3.8+** (or use the included `mvnw` wrapper)

## Quick Start

### 1. Database Setup

Create a MySQL database named `dlyk` and import the SQL script:

```sql
CREATE DATABASE dlyk DEFAULT CHARACTER SET utf8mb4;
```

Then import the provided `dlyk.sql` file into the database.

Default database credentials in the project are:

| Config   | Value          |
| -------- | -------------- |
| Host     | 127.0.0.1:3306 |
| Database | dlyk           |
| Username | root           |
| Password | root           |

You can change these in [application.yml](dlyk-server/src/main/resources/application.yml).

### 2. Backend

```bash
cd dlyk-server

# Windows
mvnw spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

The backend starts on **port 8089**.

### 3. Frontend

```bash
cd dlyk-front

# Install dependencies (first time only)
npm install

# Start dev server
npm run dev
```

The frontend dev server starts on **port 5173**. Open `http://localhost:5173` in your browser.

### 4. Login

Default admin account:

| Field    | Value     |
| -------- | --------- |
| Account  | admin     |
| Password | 123456    |

## Production Deployment

### Backend

```bash
cd dlyk-server
./mvnw package -DskipTests
```

The JAR file is generated at `target/dlyk-server-0.0.1-SNAPSHOT.jar`. Run it with:

```bash
java -jar target/dlyk-server-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd dlyk-front
npm run build
```

The static files are generated in the `dist/` directory. Serve them with **Nginx** or any static file server. Example Nginx config:

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

## Features

- **Dashboard** -- sales statistics with ECharts charts
- **User Management** -- user CRUD with role-based permissions
- **Market Activity** -- manage marketing campaigns and track participation
- **Lead Management** -- capture and follow up on sales leads
- **Customer Management** -- convert leads to customers, view details, export to Excel
- **Transaction Management** -- track deals through stages (created -> confirmed -> deposit -> inspection -> closed)
- **Product Management** -- manage vehicle catalog with pricing
- **Dictionary Management** -- maintain dropdown options (lead sources, stages, etc.)
- **System Configuration** -- manage system-level settings
- **Profile & Password** -- update personal info and change password

## Configuration

Key settings in [application.yml](dlyk-server/src/main/resources/application.yml):

| Property                        | Default                | Description              |
| ------------------------------- | ---------------------- | ------------------------ |
| `server.port`                   | 8089                   | Backend port             |
| `spring.datasource.url`         | `jdbc:mysql://...`     | MySQL connection         |
| `spring.datasource.username`    | root                   | Database user            |
| `spring.datasource.password`    | root                   | Database password        |
| `spring.data.redis.host`        | 127.0.0.1              | Redis host               |
| `spring.data.redis.port`        | 6379                   | Redis port               |

## License

This project is for educational purposes.