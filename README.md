# 心理健康 App MVP

这是一个基于现有原型图继续落地的试点型 MVP 工程骨架，目标是逐步做出一套可演示、可扩展的：

- 学生端 App
- 家长端
- 老师端
- 后端安全与业务中台

## 当前状态

当前仓库已经包含：

- 需求与边界文档：[PLAN.md](D:\桌面\心理健康\PLAN.md)
- 项目统一上下文：[PROJECT_CONTEXT.md](D:\桌面\心理健康\PROJECT_CONTEXT.md)
- 方案图对齐说明：[docs/SCHEME_ALIGNMENT_FROM_IMAGE.md](D:\桌面\心理健康\docs\SCHEME_ALIGNMENT_FROM_IMAGE.md)
- 多组原型图：[`pictures/`](D:\桌面\心理健康\pictures)
- 第一版工程骨架：
  - [backend](D:\桌面\心理健康\backend)
  - [frontend_vue](D:\桌面\心理健康\frontend_vue)
  - [app_flutter](D:\桌面\心理健康\app_flutter)
  - [app_react_native](D:\桌面\心理健康\app_react_native)
  - [docs/MVP_BACKLOG.md](D:\桌面\心理健康\docs\MVP_BACKLOG.md)
- 开发路线图：
  - [docs/DEVELOPMENT_PLAN.md](D:\桌面\心理健康\docs\DEVELOPMENT_PLAN.md)
- 已串通的学生端最小闭环：
  - 登录
  - 同意与授权
  - 筛查模板与答题
  - 筛查结果
  - 今日训练计划
  - 学生首页
  - AI 陪练前端 Mock

## 目录结构

```text
.
├─ backend/         Spring Boot Mock API 与后续业务后端
├─ frontend_vue/    Vue 3 + Vite H5/Web MVP
├─ app_flutter/     Flutter App 入口骨架
├─ app_react_native/ React Native + Expo 原生 App 新路线
├─ docs/            开发清单与补充文档
├─ pictures/        原型图与联系图
├─ PLAN.md
└─ PROJECT_CONTEXT.md
```

## 技术决策

- 后端：`Spring Boot 3.3 + Java 17 + Maven`
- 数据库：默认 `H2` 本地文件库，联调/部署推荐 `MySQL 8`
- 当前前端优先路线：`Vue 3 + Vite + TypeScript`
- 当前 App 新主路线：`React Native + Expo + TypeScript`
- App 备选路线：`Flutter` 骨架已创建
- 当前本机可直接运行后端
- 当前本机可构建 Vue 前端
- 当前本机已创建并通过类型检查的 React Native 工程
- 当前本机尚未安装 Flutter SDK，因此 Flutter 代码暂未本地编译验证

说明：

- 方案文档原先倾向 `Java 21`，但当前机器可直接稳定运行的是 `Java 17`。
- Spring Boot 3.3 对 Java 17 支持良好，所以第一版骨架先按 `Java 17` 落地，后续升级到 `Java 21` 成本很低。
- Vue 版本用于更快做 H5/Web MVP，也方便后续用 `uni-app` 或 `Capacitor` 继续打包成 App。
- React Native 版本已改为“林间聊愈室”方向，不再默认展示旧的学生/家长/老师平台流程。
- React Native 版本已根据原型视频提取页面结构，当前包含草地聊愈主场景、碎念动态流、日记情绪曲线、困扰橱窗、星空记忆、趣聊房间和 React Navigation 原生底部 Tab。
- React Native 版本已加入本地 PNG 序列帧动物动画管线，当前 `assets/sprites/` 内有花花狸、森森鹿、咕咕熊的 8 帧待机动作，并补充了草地装饰、漂浮情绪泡、动态流反应、星云和趣聊房间雪点等细节。
- React Native 版本已补本地功能闭环：碎念可发布并持久化、趣聊房间可发送并生成伙伴回复、困扰橱窗可写入日记，当前数据仍保存在手机本地，尚未接正式账号、后端和真实 AI 服务。
- 已按原型视频修正「聊愈」入口：草地首页点击大圆形「聊愈」会打开独立沉浸式趣聊房间，返回后回到首页，不再误跳到「记忆」页。
- 后端已经接入 `Spring Data JPA + Flyway`，默认用 H2 免安装数据库；如果要接正式 MySQL，可用 `local-mysql` profile。

## 后端启动

在项目根目录执行：

```powershell
cd backend
mvn spring-boot:run
```

启动后可访问：

- `GET http://localhost:8080/api/v1/health`
- `GET http://localhost:8080/api/v1/bootstrap`
- `POST http://localhost:8080/api/v1/auth/student/login`
- `POST http://localhost:8080/api/v1/auth/guardian/bind`
- `GET http://localhost:8080/api/v1/consents/status?studentId=S2024001`
- `POST http://localhost:8080/api/v1/consents/submit`
- `GET http://localhost:8080/api/v1/screenings/template`
- `POST http://localhost:8080/api/v1/screenings/submit`
- `GET http://localhost:8080/api/v1/trainings/today`
- `POST http://localhost:8080/api/v1/trainings/tasks/status`
- `GET http://localhost:8080/api/v1/guardian/summary?studentId=S2024001`
- `POST http://localhost:8080/api/v1/ai-coach/messages`
- `POST http://localhost:8080/api/v1/wellbeing/observations/analyze`
- `GET http://localhost:8080/api/v1/alerts/teacher/demo`
- `POST http://localhost:8080/api/v1/alerts/{alertId}/status`

默认启动会在 `backend/data/` 下创建本地 H2 数据库文件，适合开发演示。

如果要连接 MySQL 8，先创建数据库：

```sql
create database mindcare character set utf8mb4 collate utf8mb4_unicode_ci;
```

然后按需修改 [application.yml](D:\桌面\心理健康\backend\src\main\resources\application.yml) 里的账号密码，并使用：

```powershell
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local-mysql
```

## 一键启动

如果你是在 Windows 本机开发，当前推荐直接使用根目录脚本：

```powershell
.\start-local.cmd
```

脚本会自动完成：

- 进入 `app_react_native`
- 以离线模式启动 React Native / Expo Web 版本“林间聊愈室”
- 使用 `http://localhost:8081` 打开本机预览页面
- 窗口保持打开时 App 预览服务持续运行

补充说明：

- 脚本会关闭自动拉起浏览器，避免部分 Windows 环境下的启动失败。
- 首次打开如果还是空白页，等 Metro 完成 bundling 后手动刷新一次即可。

停止服务可执行：

```powershell
.\stop-local.cmd
```

## Vue 前端启动

当前推荐先用 Vue 版本推进 MVP：

```powershell
cd frontend_vue
npm install --cache .npm-cache
npm run dev
```

默认前端地址：

```text
http://localhost:5173
```

当前开发模式默认通过 Vite 代理转发 `/api` 到本机后端 `http://127.0.0.1:8080`，因此：

- 同一台电脑访问时，不需要额外配置前端 API 地址
- 局域网里用手机或其他电脑访问前端时，也不需要把前端改成请求它自己的 `localhost`

如果你的后端不是本机 `8080`，再创建 `.env.local`：

```text
VITE_API_BASE_URL=http://你的后端地址:8080
```

构建检查：

```powershell
cd frontend_vue
npm run build
```

## Flutter 启动

Flutter 目录保留为备选 App 路线，当前目录已提供标准 Flutter 入口文件：

- [pubspec.yaml](D:\桌面\心理健康\app_flutter\pubspec.yaml)
- [main.dart](D:\桌面\心理健康\app_flutter\lib\main.dart)

本机安装 Flutter SDK 后可尝试：

```powershell
cd app_flutter
flutter pub get
flutter run
```

如果你是在 Android 模拟器里跑，建议显式指定后端地址：

```powershell
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

如果你是在 Windows 桌面端或 Chrome 本机调试，通常可以直接使用默认的：

```text
http://localhost:8080
```

## 下一步推荐

建议按下面顺序继续做：

1. 补后端接口测试和基础鉴权
2. 完善账号、授权、筛查、训练、预警的真实业务规则
3. 用 Vue 继续细化学生端、家长端、老师端页面
4. 再接家长端和老师端
5. 继续完善 AI 网关与数字人网关

## 近期重点文件

- [backend/src/main/java/com/mindcare/app/MentalHealthApplication.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\MentalHealthApplication.java)
- [backend/src/main/java/com/mindcare/app/bootstrap/BootstrapController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\bootstrap\BootstrapController.java)
- [backend/src/main/java/com/mindcare/app/auth/AuthController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\auth\AuthController.java)
- [backend/src/main/java/com/mindcare/app/consent/ConsentController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\consent\ConsentController.java)
- [backend/src/main/java/com/mindcare/app/screening/ScreeningController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\screening\ScreeningController.java)
- [backend/src/main/java/com/mindcare/app/training/TrainingController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\training\TrainingController.java)
- [backend/src/main/java/com/mindcare/app/alert/AlertController.java](D:\桌面\心理健康\backend\src\main\java\com\mindcare\app\alert\AlertController.java)
- [frontend_vue/src/App.vue](D:\桌面\心理健康\frontend_vue\src\App.vue)
- [frontend_vue/src/api/client.ts](D:\桌面\心理健康\frontend_vue\src\api\client.ts)
- [app_flutter/lib/main.dart](D:\桌面\心理健康\app_flutter\lib\main.dart)
- [app_flutter/lib/src/pages/student_flow_pages.dart](D:\桌面\心理健康\app_flutter\lib\src\pages\student_flow_pages.dart)
