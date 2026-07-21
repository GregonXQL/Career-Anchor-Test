# Career Anchor Server

施恩职业锚测评小程序后端。当前完成开发文档第 13 章的 M5：在完整用户与管理体验上补齐提交频控、生产密钥约束、部署限流模板和上线安全检查。

## 环境

- Java 21
- Maven 3.9+
- MySQL 8（也可用仓库中的 Compose 配置）

## 本地启动

MySQL 可任选一种方式启动。Docker：

```bash
docker compose up -d
```

或已安装 Homebrew MySQL 时：

```bash
brew services start mysql@8.0
```

随后启动后端：

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH=/opt/homebrew/opt/openjdk@21/bin:$PATH
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

`dev` profile 会将任意非空的微信 `code` 稳定映射为本地 openid，仅用于本地联调。默认和 `prod` profile 都会调用真实 `jscode2session`。

`dev` profile 在 `admin_accounts` 为空时会创建本地管理员，默认密码为 `admin123`。可在启动前通过 `ADMIN_INITIAL_PASSWORD` 修改；已有管理员不会被覆盖。管理员先以普通用户登录，再从小程序底部“管理员入口”使用该密码提权。

本地快速验证：

```bash
curl -sS -X POST http://localhost:8080/api/auth/wx-login \
  -H 'Content-Type: application/json' \
  -d '{"code":"local-user"}'

curl -sS http://localhost:8080/api/questions \
  -H 'Authorization: Bearer <上一步返回的 token>'
```

M4 可直接从小程序管理控制台生成邀请码。如需绕过界面做接口测试，也可手动插入：

```sql
INSERT INTO invite_codes (code, max_uses, status, channel, remark)
VALUES ('M2TEST88', 1, 'ACTIVE', 'MANUAL', 'local M2 test');
```

之后依次调用 `POST /invite/verify`、`POST /tests/submit`、`GET /results` 和 `GET /results/{id}`。提交接口使用开发文档约定的 40 条 `{ "q": 1, "v": 4 }` 答案和恰好 3 个 `boosted` 题号；邀请码只在提交事务成功时消耗。

二维码邀请复用同一实体和同一套校验/消耗逻辑。V3 迁移为 `invite_codes` 增加 `channel` 字段；小程序码的 `scene` 使用 `i=M2TEST88`。

M4 已实现微信 `access_token` 内存缓存和 `getwxacodeunlimit`。`dev` profile 返回可预览但不可扫码的本地占位 PNG；扫码流程可在开发者工具编译模式中使用 `scene=i=M3TEST88` 验证。真实小程序码需在非 `dev` 环境配置 `WECHAT_APP_ID`、`WECHAT_APP_SECRET`，并可通过 `WECHAT_ACODE_ENV_VERSION` 和 `WECHAT_ACODE_CHECK_PATH` 控制开发版、体验版或正式版页面校验。

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

`prod` profile 还会强制要求 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`WECHAT_APP_ID` 和 `WECHAT_APP_SECRET`。Nginx HTTPS、10r/s 单 IP 限流及安全响应头模板位于 `deploy/nginx-career-anchor.conf.example`；启用前需替换域名与证书路径。完整平台配置和发布回归项见 `../docs/M5上线检查清单.md`。

## 测试

```bash
./mvnw test
```

当前 25 个单测覆盖 JWT、计分边界与同分优先级、邀请码状态与原子消耗、提交频率限制、报告换算、结果归属校验、解析文案，以及管理员登录锁定、管理统计/状态筛选、8 位邀请码生成、微信小程序码 token 缓存和生产环境密钥约束。构建和测试均固定使用 Java 21。
