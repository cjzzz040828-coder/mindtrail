# 心理健康 App `image2` 四屏稳定版

## 为什么改成四屏

你这个项目的参考图本身就是 **4 台手机横向排布**，所以 `image2` 在这个构图上更稳定。

相比 6 屏，4 屏更容易做到：

- 每个界面更大，细节不容易糊
- 中文标题更容易正常
- 卡片层级更清楚
- 不容易把不同角色界面混在一起
- 更接近你现在这张 [原型图.png](D:\桌面\心理健康\原型图.png) 的风格

## 正确使用方式

不要把整份文档丢给 `image2`。

你应该这样用：

1. 打开 `image2`
2. 上传参考图 [原型图.png](D:\桌面\心理健康\原型图.png)
3. 从下面复制一个完整 `prompt`
4. 直接粘贴生成
5. 一次只生成 4 个界面

## 四屏稳定规则

下面这些规则我已经写进每个 prompt 里了，所以你直接复制即可：

- `exactly 4 smartphone screens`
- `wide 16:9 presentation board`
- `four phones only, no extra screens`
- `use short Chinese labels only`
- `reduce tiny paragraph text`
- `focus on layout, cards, charts, buttons, hierarchy`

---

## 直接复制 Prompt 01：通用入口流程

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly. Extend it into a complete product system.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for a mental wellness support app for middle school students in mainland China. Show four phones only, evenly spaced horizontally, no extra screens, no overlapping, no tablets, no laptops. Use short Chinese labels only and reduce tiny paragraph text. Focus on layout, cards, charts, buttons, and realistic mobile product hierarchy.
Style/medium: realistic high-fidelity product UI mockup, not wireframe, not concept art
Lighting/mood: soft daylight studio lighting, calm, gentle, trustworthy
Color palette: off-white, soft green, calm blue accents, low visual pressure
Constraints: realistic Chinese mobile app interface, premium but simple, no watermark, no logos, no clutter, no hospital equipment, no cyberpunk, no cartoon style
Avoid: more than 4 phones, dense text, duplicated screens, chaotic layout, dashboard overload

Section title: "通用入口流程"

Screen 1: splash screen
- app name area
- soft green background
- abstract growth symbol
- short text: "心理健康助手"

Screen 2: role selection
- three role cards: student, guardian, teacher
- one main action button
- friendly onboarding

Screen 3: student login
- school field, student id field, invite code field
- simple form layout
- short title: "学生登录"

Screen 4: guardian binding
- invite code field
- relation confirmation card
- success button
- short title: "家长绑定"
```

## 直接复制 Prompt 02：授权与同意流程

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly. Extend it into a complete product system.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the authorization and consent flow of a mental wellness support app. Show four phones only, evenly spaced horizontally, no extra screens, no overlap. Use short Chinese labels only and reduce tiny paragraph text. Focus on card hierarchy, toggle states, status chips, and practical mobile layout.
Style/medium: realistic high-fidelity product UI mockup
Lighting/mood: calm, professional, privacy-aware
Color palette: off-white, soft green, warm orange accents
Constraints: realistic Chinese mobile app design, privacy-first, no dense legal text, no watermark, no logos
Avoid: more than 4 phones, long legal paragraphs, tiny unreadable text, duplicated layouts

Section title: "授权与同意"

Screen 1: consent center
- cards for guardian consent, student informed consent, service explanation
- one main confirm button
- title: "同意中心"

Screen 2: guardian consent detail
- one selected consent card
- agreement summary
- sign or confirm button
- title: "监护人同意"

Screen 3: special authorization
- camera training authorization
- digital avatar authorization
- toggles or check cards
- title: "专项授权"

Screen 4: authorization complete
- all required authorizations completed
- status chips and next step button
- title: "授权完成"
```

## 直接复制 Prompt 03：学生筛查流程

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the student assessment flow. Show four phones only, evenly spaced horizontally. Use short Chinese labels only and reduce tiny paragraph text. The screens should feel private, low-pressure, and supportive for middle school students.
Style/medium: realistic high-fidelity product UI mockup
Lighting/mood: soft, calm, reassuring
Color palette: off-white, soft green as main accent
Constraints: no diagnosis language, no scary warning visuals, no clutter, no watermark
Avoid: more than 4 phones, dense text, enterprise dashboard feeling

Section title: "学生筛查流程"

Screen 1: student home
- greeting card
- today mood summary
- quick entry cards
- bottom tabs
- title: "你好，林同学"

Screen 2: daily self-check
- mood chips or face icons
- sleep, stress, energy fields
- submit button
- title: "每日自评"

Screen 3: assessment intro
- short explanation
- estimated time
- privacy note
- start button
- title: "开始筛查"

Screen 4: assessment answering
- one question card
- four answer options
- progress bar at top
- title: "筛查答题"
```

## 直接复制 Prompt 04：学生结果与训练开始

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the student result and training kickoff flow. Show four phones only. Use short Chinese labels only and reduce tiny paragraph text. Focus on status chips, recommendation cards, task cards, and one clean training interaction.
Style/medium: realistic high-fidelity mobile product UI mockup
Lighting/mood: calm, supportive, encouraging
Color palette: off-white, soft green with a little blue
Constraints: practical product UI, no diagnosis wording, no clutter, no watermark
Avoid: more than 4 phones, too much text, duplicated screens

Section title: "结果与训练开始"

Screen 1: assessment result
- low medium high trend chip
- short explanation
- next step suggestion
- title: "筛查结果"

Screen 2: daily training plan
- three task cards
- completion marks
- title: "今日训练计划"

Screen 3: training detail
- one selected task
- duration, guidance, start button
- title: "训练详情"

Screen 4: breathing exercise
- large breathing circle
- timer
- inhale exhale guidance
- title: "呼吸训练"
```

## 直接复制 Prompt 05：学生成长与 AI

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the student growth and AI companionship flow. Show four phones only, evenly spaced. Use short Chinese labels only and reduce tiny paragraph text. Focus on journal cards, line charts, chat bubbles, and an AI-generated avatar module.
Style/medium: realistic high-fidelity mobile app UI mockup
Lighting/mood: calm, emotionally supportive, youth-friendly
Color palette: off-white, soft green, calm blue accents
Constraints: AI clearly marked, privacy-aware, no watermark, no logos, no visual clutter
Avoid: more than 4 phones, long body copy, scary crisis styling

Section title: "成长与 AI"

Screen 1: mood journal
- mood tag
- short text area
- save button
- title: "情绪日记"

Screen 2: growth trend
- weekly line chart
- streak days
- summary card
- title: "成长趋势"

Screen 3: AI coach chat
- assistant bubble
- student bubble
- one exercise suggestion card
- title: "AI 陪练"

Screen 4: digital avatar companion
- friendly avatar card
- AI generated badge
- supportive script bubble
- title: "数字人陪练"
```

## 直接复制 Prompt 06：学生支持与个人设置

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the student support and personal settings modules. Show four phones only. Use short Chinese labels only and reduce tiny paragraph text. Focus on action buttons, resource cards, list items, and settings sections.
Style/medium: realistic high-fidelity mobile product UI mockup
Lighting/mood: calm, safe, reassuring
Color palette: off-white, soft green, calm blue, a little muted red only for crisis actions
Constraints: no frightening emergency visuals, privacy-first, no watermark, no clutter
Avoid: more than 4 phones, excessive tiny text, duplicated screens

Section title: "支持与设置"

Screen 1: crisis help
- clear but calm support actions
- contact guardian
- contact school counselor
- emergency option
- title: "立即求助"

Screen 2: discover resources
- resource cards for sleep, breathing, emotional education
- clean card grid
- title: "发现"

Screen 3: notification center
- reminder list for check-in, training, authorization updates
- short list items
- title: "消息提醒"

Screen 4: profile and privacy settings
- profile card
- privacy entry
- consent management entry
- data export delete entry
- title: "我的"
```

## 直接复制 Prompt 07：家长端核心界面

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the guardian side of a mental wellness support app. Show four phones only, evenly spaced horizontally. Use short Chinese labels only and reduce tiny paragraph text. The parent side should emphasize summary-only design, trends, reminders, and supportive family action.
Style/medium: realistic high-fidelity product UI mockup
Lighting/mood: warm, calm, reassuring
Color palette: off-white, warm orange as main accent
Constraints: do not show full chat logs, raw diary, or raw video; no clutter; no watermark
Avoid: more than 4 phones, dense text, scary risk visuals

Section title: "家长端核心界面"

Screen 1: parent home summary
- weekly overview card
- mood stability
- training completion rate
- title: "家长端摘要"

Screen 2: weekly trend detail
- larger line chart
- sleep trend
- stress trend
- title: "周趋势详情"

Screen 3: reminder list
- late sleep
- incomplete training
- suggested family action
- title: "提醒事项"

Screen 4: training summary
- recent training completion
- recent self-check summary
- supportive suggestion card
- title: "训练摘要"
```

## 直接复制 Prompt 08：家长授权与老师预警

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens that bridges the guardian communication flow and the teacher alert flow. Show four phones only. Use short Chinese labels only and reduce tiny paragraph text. Keep role separation visually clear with orange for guardian and muted red for teacher.
Style/medium: realistic high-fidelity mobile app UI mockup
Lighting/mood: professional, calm, privacy-aware
Color palette: off-white, orange for guardian screens, muted red for teacher screens
Constraints: realistic Chinese app design, summary-first, no clutter, no watermark
Avoid: more than 4 phones, dense legal text, duplicated layouts

Section title: "家长沟通与老师预警"

Screen 1: guardian consent and communication
- consent status
- recent access log
- school communication suggestion
- title: "授权与沟通"

Screen 2: teacher alert dashboard
- high medium low risk counters
- top summary cards
- title: "老师端预警"

Screen 3: class risk list
- student rows
- class info
- risk chips
- filter tabs
- title: "班级风险列表"

Screen 4: alert detail review
- short risk reason
- action recommendation
- status buttons
- title: "复核详情"
```

## 直接复制 Prompt 09：老师端后续处置

```text
Use case: ui-mockup
Asset type: mobile app 4-screen presentation board
Input images: Image 1 is a style reference only. Follow the same clean Chinese mobile UI design language, realistic smartphone hardware frames, white background, soft pastel gradients, rounded cards, subtle shadows, calm startup presentation quality. Do not copy exactly.
Primary request: Create a wide 16:9 presentation board with exactly 4 smartphone screens for the teacher or school counselor follow-up flow. Show four phones only, evenly spaced. Use short Chinese labels only and reduce tiny paragraph text. Focus on limited student summary, follow-up actions, auditability, and professional privacy-respecting design.
Style/medium: realistic high-fidelity product UI mockup
Lighting/mood: restrained, professional, calm
Color palette: off-white, muted red as main accent, neutral gray support colors
Constraints: no full diary, no full chat log, no raw video, no alarmist styling, no watermark
Avoid: more than 4 phones, dashboard overload, dense tiny paragraphs

Section title: "老师端后续处置"

Screen 1: limited student summary
- trend card
- recent check-ins
- training completion
- privacy note
- title: "学生摘要"

Screen 2: action record
- follow-up timeline
- parent contact record
- offline review note
- title: "处置记录"

Screen 3: audit and filters
- class filter
- date filter
- risk filter
- audit trail list
- title: "审计与筛选"

Screen 4: review completed state
- reviewed status
- next follow-up time
- archive or continue action
- title: "复核完成"
```

---

## 最推荐的顺序

如果你要完整出图，建议按下面顺序逐个复制：

1. `Prompt 01`
2. `Prompt 02`
3. `Prompt 03`
4. `Prompt 04`
5. `Prompt 05`
6. `Prompt 06`
7. `Prompt 07`
8. `Prompt 08`
9. `Prompt 09`

## 如果它还是会乱

在任意 prompt 最后再加这一句：

```text
Make each of the four screens visually distinct. Use larger cards and fewer text blocks. Keep only short Chinese headings and button labels. Do not add any extra phone screens.
```

## 如果它开始把字做坏了

在任意 prompt 最后改加这一句：

```text
Use placeholder micro text for body copy, but keep the main Chinese page titles accurate and readable.
```
