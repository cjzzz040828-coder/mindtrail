# MindTrail 心理健康 App

面向中国大陆中学未成年人的心理健康筛查辅助 + AI 康复训练陪伴 + 家校摘要预警 MVP 试点工程。

不做医学诊断，不保存原始视频，高风险/危机内容触发学校心理老师人工复核。

## 项目结构

```
.
├── backend/              Spring Boot 3.3 + JPA + Flyway 后端
├── frontend_vue/         Vue 3 + Vite + TypeScript Web MVP
├── app_react_native/     React Native + Expo「林间聊愈室」
├── app_flutter/          Flutter App（备选路线）
├── docs/                 开发计划与积压清单
├── scripts/              一键启动/停止脚本
├── PLAN.md               技术方案
└── PROJECT_CONTEXT.md    项目决策与边界
```

## 技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 后端 | Java 17 + Spring Boot 3.3 + Spring Data JPA + Flyway | H2 本地开发，MySQL 8 生产 |
| Web 前端 | Vue 3 + Vite + TypeScript + lucide-vue-next | 单文件原型，无路由库 |
| 移动端 | React Native 0.81 + Expo 54 + TypeScript | 独立「聊愈」方向，AsyncStorage 本地持久化 |
| 移动端备选 | Flutter 3.3+ | 与后端 API 对接的学生端骨架 |

## 后端

### 启动

```bash
cd backend
mvn spring-boot:run
```

默认使用 H2 内嵌数据库，数据存放在 `backend/data/`。

连接 MySQL 8：

```bash
# 先创建数据库
mysql -e "CREATE DATABASE mindcare CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
# 启动
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local-mysql
```

### 业务模块

#### 认证（Auth）

学生登录通过学校编码 + 学号 + 姓名自动创建或更新记录，返回会话令牌。家长绑定为存根接口。

| 端点 | 说明 |
|---|---|
| `POST /api/v1/auth/student/login` | 学生登录/注册，自动建校、班、生 |
| `POST /api/v1/auth/guardian/bind` | 家长绑定邀请码（stub） |

#### 授权管理（Consent）

四级授权：监护人同意、学生知情同意、摄像头训练授权、个人形象用于数字人。前三项为必选，头像授权可选。支持版本管理和撤回状态。

| 端点 | 说明 |
|---|---|
| `GET /api/v1/consents/status?studentId=` | 查询授权状态 |
| `POST /api/v1/consents/submit` | 提交授权记录 |

#### 心理筛查（Screening）

动态问卷模板，3 道关于睡眠、压力、社交意愿的题目。根据压力得分计算风险等级：`>= 8` 高风险，`>= 5` 中风险，其余低风险。高风险自动创建预警单。

| 端点 | 说明 |
|---|---|
| `GET /api/v1/screenings/template` | 获取筛查问卷模板 |
| `POST /api/v1/screenings/submit` | 提交筛查结果，返回风险等级和训练建议 |

#### 训练计划（Training）

每日自动创建训练计划，含三项默认任务：呼吸放松（3 分钟）、情绪日记（1 次）、正念专注（5 分钟）。支持任务状态更新和完成率统计。

| 端点 | 说明 |
|---|---|
| `GET /api/v1/trainings/today?studentId=` | 获取今日训练计划 |
| `POST /api/v1/trainings/tasks/status` | 更新任务完成状态 |

#### AI 陪练（AI Coach）

基于关键词匹配的风险检测。危机词（自杀、自伤等）触发高风险预警并自动创建人工复核单；痛苦词（崩溃、绝望等）触发中风险提示。回复模板按风险等级区分。

| 端点 | 说明 |
|---|---|
| `POST /api/v1/ai-coach/messages` | 发送消息，返回 AI 回复和风险判定 |

#### 状态观察（Wellbeing）

接收行为指标码（注意力分散、低能量、紧张等），自动生成表情指标、身心状态建议、行为建议、音乐建议、饮食建议和数字人台词。支持历史记录查询。

| 端点 | 说明 |
|---|---|
| `POST /api/v1/wellbeing/observations/analyze` | 分析行为指标，生成状态报告 |
| `GET /api/v1/wellbeing/observations/history?studentId=&limit=` | 查询观察历史 |

#### 预警复核（Alert）

高风险筛查或 AI 危机检测自动创建预警单，学校心理老师查看摘要、风险原因、建议动作，标记处理状态。附带训练快照、跟进提示和操作时间线。

| 端点 | 说明 |
|---|---|
| `GET /api/v1/alerts/teacher/demo?highlightAlertId=` | 获取预警列表和高亮详情 |
| `POST /api/v1/alerts/{alertId}/status` | 更新预警处理状态 |

#### 家长摘要（Guardian）

聚合筛查风险等级、训练完成率、最新观察记录，生成情绪标签、趋势柱状图和上下文提醒。不展示完整聊天、隐私日记和原始视频。

| 端点 | 说明 |
|---|---|
| `GET /api/v1/guardian/summary?studentId=` | 获取家长周报摘要 |

#### 审计日志（Audit）

内部服务，记录所有预警创建、状态更新、训练打卡、观察分析、AI 对话事件。无独立接口。

### 数据库表

11 张表，Flyway 自动迁移：

```
schools ──> school_classes ──> students
                                  ├── consent_records
                                  ├── screening_submissions
                                  ├── training_plans ──> training_tasks
                                  ├── training_observation_sessions ──> training_observation_features
                                  ├── alert_cases
                                  └── ai_coach_sessions ──> ai_coach_events
audit_logs（独立，通过 targetType + targetId 关联）
```

## Web 前端（Vue）

```bash
cd frontend_vue
npm install
npm run dev    # 访问 http://localhost:5173
```

Vite 开发模式自动代理 `/api` 到后端 `http://127.0.0.1:8080`。桌面端以手机壳模拟框展示，430px 以下自动全屏。

三角色通过标签切换，学生端含 9 个子视图：

| 视图 | 功能 |
|---|---|
| 登录 | 学校编码 + 学号 + 姓名 |
| 授权 | 四项开关式授权 |
| 筛查引导 | 问卷说明与免责声明 |
| 筛查答题 | 逐题作答 + 进度条 |
| 筛查结果 | 风险等级、训练建议、安全提示 |
| 训练计划 | 今日任务清单，可勾选完成 |
| 首页 | 三只森林伙伴角色卡片、快捷入口、今日数据 |
| AI 陪练 | 聊天界面，危机检测时刷新家长和老师数据 |
| 数字人/思维工具 | 三个引导场景（舒压呼吸、专注回正、能量唤醒），可选摄像头人脸检测辅助观察 |

家长端：情绪稳定性、训练完成率、提醒列表、趋势图、最新观察摘要。

老师端：预警列表（颜色标记风险等级）、高亮详情（建议动作、训练快照、跟进提示、操作时间线）、状态更新和备注。

## React Native（林间聊愈室）

独立的聊愈方向产品，与后端 API 无关，完全离线本地运行。

```bash
# Windows 一键启动
.\start-local.cmd

# 手动启动
cd app_react_native
npm install
npx expo start --web
```

四个底部 Tab：

| Tab | 功能 |
|---|---|
| 聊愈 | 草地主场景，三只动画伙伴（花花狸/森森鹿/咕咕熊），脉冲式聊愈按钮进入聊天室 |
| 碎念 | 碎念动态流，可发布和查看帖子，带点赞和评论 |
| 日记 | 每周情绪柱状图、困扰识别（魔法门隐喻）、阶段目标追踪 |
| 记忆 | 星空夜景、语音朗读问候、保存日记条目 |

三只伙伴性格各异，分别对应吐槽、温柔陪伴、能量补充。聊天室根据关键词和伙伴性格生成本地模拟回复。所有数据通过 AsyncStorage 本地持久化。

含 PNG 序列帧动画（8 帧待机动作）、浮动情绪气泡、雪花、星光闪烁等视觉效果。

## Flutter（备选）

```bash
cd app_flutter
flutter pub get
flutter run
```

实现学生端最小闭环：登录 → 授权 → 筛查 → 结果 → 训练计划 → 首页。家长和老师端为占位页面。与后端 API 对接，状态管理使用原生 setState。

## 一键启动

```powershell
# 启动 React Native Web 预览
.\start-mobile.cmd

# 停止
.\stop-local.cmd
```

## 下一步

1. 后端鉴权从 Demo 令牌升级为 JWT
2. 完善学校批量导入、邀请码绑定正式流程
3. Vue 前端拆分组件，引入 vue-router 和 Pinia
4. AI 陪练接入真实大模型安全网关
5. 数字人能力接入供应商适配层
6. 审计日志完善和数据导出机制
