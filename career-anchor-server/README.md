# Career Anchor Server

施恩职业锚测评小程序后端。当前完成开发文档第 13 章的 M2：在 M1 的数据库、微信登录、JWT、题目和计分能力上，补齐邀请码校验与原子消耗、答卷提交、结果查询和频率限制。

## 环境

- Java 21
- Maven 3.9+
- MySQL 8（也可用仓库中的 Compose 配置）

## 本地启动

```bash
docker compose up -d

export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH=/opt/homebrew/opt/openjdk@21/bin:$PATH
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 会将任意非空的微信 `code` 稳定映射为本地 openid，仅用于本地联调。默认和 `prod` profile 都会调用真实 `jscode2session`。

本地快速验证：

```bash
curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"local-user"}'

curl -sS http://localhost:8080/api/questions \
  -H 'Authorization: Bearer <上一步返回的 token>'
```

M2 本地联调前可插入一条一次性邀请码（管理端生成能力属于 M4）：

```sql
INSERT INTO invite_codes (code, max_uses, status, channel, remark)
VALUES ('M2TEST88', 1, 'ACTIVE', 'MANUAL', 'local M2 test');
```

之后依次调用 `POST /invite/verify`、`POST /tests/submit`、`GET /results` 和 `GET /results/{id}`。提交接口使用开发文档约定的 40 条 `{ "q": 1, "v": 4 }` 答案和恰好 3 个 `boosted` 题号；邀请码只在提交事务成功时消耗。

二维码邀请复用同一实体和同一套校验/消耗逻辑。V3 迁移为 `invite_codes` 增加 `channel` 字段；小程序码的 `scene` 使用 `i=M2TEST88`。微信接口取 token 和生成小程序码的管理端能力仍按计划在 M4 实现。

也可导入 [postman/M1.postman_collection.json](postman/M1.postman_collection.json)，依次运行两个请求；集合会自动保存 JWT，并校验题目数量与量表配置。Swagger UI 位于 `http://localhost:8080/api/swagger-ui.html`。

## 真实微信登录

不启用 `dev` profile，并设置：

```bash
export WECHAT_APP_ID='<appid>'
export WECHAT_APP_SECRET='<secret>'
export JWT_SECRET='<至少 32 字节的随机密钥>'
./mvnw spring-boot:run
```

微信密钥、JWT 密钥和数据库口令都不应写入仓库。`prod` profile 会强制要求 `JWT_SECRET`，并关闭 Swagger。

## 测试

```bash
./mvnw test
```

当前 12 个单测覆盖 JWT、计分边界与同分优先级、邀请码状态与原子消耗、提交频率限制、报告换算和结果归属校验。构建和测试均固定使用 Java 21。
