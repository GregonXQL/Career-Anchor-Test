# Career Anchor Mini Program

M4 微信原生小程序，包含完整用户测评体验和小程序内管理控制台。管理员可二次鉴权，查看统计与全部结果，批量生成/停用邀请码，生成、保存和转发邀请小程序码；扫码 `scene` 复用普通邀请码校验与消耗链路，免输码直达测评。

## 本地联调

1. 启动 `career-anchor-server` 的 `dev` profile 和 MySQL。
2. 在微信开发者工具中导入本目录。
3. 将 `utils/config.js` 的 `baseUrl` 改为后端地址。开发者工具可使用 `127.0.0.1`；真机必须使用已备案并配置为 request 合法域名的 HTTPS 地址。
4. 将真实 appid 写入本地 `project.private.config.json`（该文件已忽略），不要提交密钥。

二维码入口页面固定为 `pages/quiz/index`，scene 格式为 `i=AB3K9XQ2`。可在开发者工具的编译模式中添加 scene 参数验证扫码流程。

`dev` 后端首次启动会在管理员表为空时创建默认密码 `admin123`；该密码仅供本地联调，可通过后端环境变量 `ADMIN_INITIAL_PASSWORD` 覆盖。生产环境不会自动创建管理员。

## 测试

```bash
npm test
find . -name '*.js' -print0 | xargs -0 -n1 node --check
```

单测覆盖报告排序、雷达图固定维度、Top3 详解结构、历史摘要、解析文案缓存、扫码 scene 严格解析，以及管理结果/邀请码展示和筛选参数。
