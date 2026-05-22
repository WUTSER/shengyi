# 差旅报销单后端服务

技术栈：Java 17、Spring Boot 3.2、Maven、MySQL、Spring JDBC。

基础数据和表结构脚本位于 `data` 目录：

```text
data/schema.sql
data/data.sql
```

应用启动时会自动执行建表和初始化脚本。初始化 SQL 使用 `ON DUPLICATE KEY UPDATE`，重复启动不会重复插入基础数据。

## 数据库

默认连接本机 MySQL：

```text
Host: localhost
Port: 3306
Database: shengyi
Username: root
Password: 123456
```

可通过环境变量覆盖：

```text
MYSQL_HOST
MYSQL_PORT
MYSQL_DATABASE
MYSQL_USERNAME
MYSQL_PASSWORD
```

示例：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的密码"
mvn spring-boot:run
```

## 启动

```bash
mvn spring-boot:run
```

默认地址：

```text
http://localhost:8081
```

## 编译

```bash
mvn clean package
```

如果已有服务正在运行并占用了 `target` 下的 jar，先停止对应 Java 进程再执行 `mvn clean package`。

## 主要接口

基础数据：

```text
GET /api/v1/master-data/reim-companies
GET /api/v1/master-data/reim-departments
GET /api/v1/master-data/employees
GET /api/v1/master-data/business-types
GET /api/v1/master-data/cities
```

报销单：

```text
GET    /api/v1/travel-reimbursements
POST   /api/v1/travel-reimbursements
GET    /api/v1/travel-reimbursements/{reimbursementId}
PUT    /api/v1/travel-reimbursements/{reimbursementId}
DELETE /api/v1/travel-reimbursements/{reimbursementId}
POST   /api/v1/travel-reimbursements/{reimbursementId}/submit
POST   /api/v1/travel-reimbursements/{reimbursementId}/withdraw
POST   /api/v1/travel-reimbursements/{reimbursementId}/approve
POST   /api/v1/travel-reimbursements/{reimbursementId}/disburse
POST   /api/v1/travel-reimbursements/{reimbursementId}/void
```

补录行程和补助：

```text
POST   /api/v1/travel-reimbursements/{reimbursementId}/trips
PUT    /api/v1/travel-reimbursements/{reimbursementId}/trips/{tripId}
DELETE /api/v1/travel-reimbursements/{reimbursementId}/trips/{tripId}
POST   /api/v1/travel-reimbursements/{reimbursementId}/trips/{tripId}/copy
GET    /api/v1/travel-reimbursements/{reimbursementId}/allowances
GET    /api/v1/travel-reimbursements/{reimbursementId}/allowances/{allowanceId}/calendar
PUT    /api/v1/travel-reimbursements/{reimbursementId}/allowances/{allowanceId}/calendar
GET    /api/v1/travel-reimbursements/{reimbursementId}/expense-totals
```
