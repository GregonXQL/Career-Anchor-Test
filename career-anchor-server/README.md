# Career Anchor Server

施恩职业锚测评小程序后端。当前完成开发文档第 13 章的 M1：Flyway 数据库初始化、微信登录、USER JWT、题目接口和计分服务。

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

计分单测覆盖满分卷、最低分卷、同分优先级和非法输入。
