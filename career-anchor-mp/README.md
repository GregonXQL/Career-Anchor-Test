# Career Anchor Mini Program

M2 微信原生小程序，包含登录、首页、邀请码验证、二维码 `scene` 免输码入口、40 题作答、草稿恢复、3 题附加分、提交确认、JSON 结果和简明历史结果。

## 本地联调

1. 启动 `career-anchor-server` 的 `dev` profile 和 MySQL。
2. 在微信开发者工具中导入本目录。
3. 将 `utils/config.js` 的 `baseUrl` 改为后端地址。开发者工具可使用 `127.0.0.1`；真机必须使用已备案并配置为 request 合法域名的 HTTPS 地址。
4. 将真实 appid 写入本地 `project.private.config.json`（该文件已忽略），不要提交密钥。

二维码入口页面固定为 `pages/quiz/index`，scene 格式为 `i=AB3K9XQ2`。可在开发者工具的编译模式中添加 scene 参数验证扫码流程。

## JavaScript 语法检查

```bash
find . -name '*.js' -print0 | xargs -0 -n1 node --check
```
