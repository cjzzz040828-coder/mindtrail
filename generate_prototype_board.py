from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os


OUT = os.path.join(os.getcwd(), "prototype_board_v1.png")
FONT = r"C:\Windows\Fonts\msyh.ttc"
W, H = 1800, 1220


img = Image.new("RGBA", (W, H), "#F3F8F6")
draw = ImageDraw.Draw(img)

for y in range(H):
    t = y / (H - 1)
    r = int(243 * (1 - t) + 232 * t)
    g = int(248 * (1 - t) + 242 * t)
    b = int(246 * (1 - t) + 255 * t)
    draw.line((0, y, W, y), fill=(r, g, b, 255))

shape = Image.new("RGBA", (W, H), (0, 0, 0, 0))
sd = ImageDraw.Draw(shape)
sd.ellipse((40, 80, 360, 400), fill=(82, 173, 145, 46))
sd.ellipse((1340, 40, 1750, 450), fill=(72, 132, 218, 34))
sd.ellipse((1280, 860, 1710, 1220), fill=(255, 175, 92, 28))
sd.rounded_rectangle((120, 980, 560, 1135), radius=36, fill=(255, 255, 255, 120))
shape = shape.filter(ImageFilter.GaussianBlur(10))
img.alpha_composite(shape)

font_title = ImageFont.truetype(FONT, 56)
font_sub = ImageFont.truetype(FONT, 24)
font_phone = ImageFont.truetype(FONT, 28)
font_h1 = ImageFont.truetype(FONT, 26)
font_h2 = ImageFont.truetype(FONT, 21)
font_text = ImageFont.truetype(FONT, 18)
font_small = ImageFont.truetype(FONT, 15)
font_tiny = ImageFont.truetype(FONT, 13)


def shadow_box(base, box, radius=28, fill=(255, 255, 255, 255), shadow=(24, 58, 76, 26), blur=16, offset=(0, 8)):
    layer = Image.new("RGBA", base.size, (0, 0, 0, 0))
    ld = ImageDraw.Draw(layer)
    x1, y1, x2, y2 = box
    ox, oy = offset
    ld.rounded_rectangle((x1 + ox, y1 + oy, x2 + ox, y2 + oy), radius=radius, fill=shadow)
    layer = layer.filter(ImageFilter.GaussianBlur(blur))
    base.alpha_composite(layer)
    bd = ImageDraw.Draw(base)
    bd.rounded_rectangle(box, radius=radius, fill=fill)


def text_size(text, font):
    box = draw.textbbox((0, 0), text, font=font)
    return box[2] - box[0], box[3] - box[1]


def chip(d, xy, text, bg, fg=(255, 255, 255, 255), font=font_small):
    x, y = xy
    tw, th = text_size(text, font)
    d.rounded_rectangle((x, y, x + tw + 24, y + th + 14), radius=18, fill=bg)
    d.text((x + 12, y + 6), text, font=font, fill=fg)


def card(d, box, title, subtitle=None, fill=(248, 251, 252, 255), title_fill=(38, 52, 61, 255)):
    d.rounded_rectangle(box, radius=26, fill=fill)
    x1, y1, x2, y2 = box
    d.text((x1 + 18, y1 + 14), title, font=font_h2, fill=title_fill)
    if subtitle:
        d.text((x1 + 18, y1 + 46), subtitle, font=font_small, fill=(109, 125, 132, 255))


def line_chart(d, x, y, points, color, width=4):
    scaled = []
    for i, p in enumerate(points):
        scaled.append((x + i * 34, y + 72 - p * 12))
    d.line(scaled, fill=color, width=width, joint="curve")
    for px, py in scaled:
        d.ellipse((px - 4, py - 4, px + 4, py + 4), fill=color)


draw.text((84, 72), "心理健康 App 原型图预览", font=font_title, fill=(33, 46, 56, 255))
draw.text((86, 142), "基于当前 MVP 方案整理的四端界面方向：学生端、AI 陪练、家长摘要、老师预警复核", font=font_sub, fill=(87, 107, 117, 255))
chip(draw, (84, 188), "MVP 试点", (51, 134, 103, 255))
chip(draw, (196, 188), "中学未成年人", (61, 116, 201, 255))
chip(draw, (372, 188), "摘要预警 + 人工复核", (234, 148, 63, 255))

phone_w, phone_h = 340, 760
screen_margin = 12
screen_top = 18
phone_y = 270
phone_xs = [90, 500, 910, 1320]
labels = ["学生首页", "AI 陪练", "家长端", "老师端"]
label_colors = [(40, 118, 96, 255), (68, 94, 180, 255), (224, 146, 61, 255), (201, 88, 88, 255)]

for idx, px in enumerate(phone_xs):
    shadow_box(img, (px, phone_y, px + phone_w, phone_y + phone_h), radius=42, fill=(29, 41, 50, 255), shadow=(18, 44, 59, 40))
    pd = ImageDraw.Draw(img)
    screen = (px + screen_margin, phone_y + screen_top, px + phone_w - screen_margin, phone_y + phone_h - screen_margin)
    pd.rounded_rectangle(screen, radius=34, fill=(250, 252, 253, 255))
    pd.rounded_rectangle((px + 125, phone_y + 8, px + 215, phone_y + 22), radius=8, fill=(52, 64, 73, 255))
    chip(pd, (px + 8, phone_y - 54), labels[idx], label_colors[idx], font=font_phone)

# 学生端
px = phone_xs[0] + screen_margin
py = phone_y + screen_top
pd = ImageDraw.Draw(img)
pd.rounded_rectangle((px + 14, py + 18, px + 302, py + 158), radius=30, fill=(46, 140, 113, 255))
pd.text((px + 30, py + 34), "你好，林同学", font=font_h1, fill="white")
pd.text((px + 30, py + 70), "今天也一起完成 10 分钟练习", font=font_text, fill=(231, 244, 239, 255))
chip(pd, (px + 30, py + 108), "当前情绪：平稳", (255, 255, 255, 58), font=font_small)
chip(pd, (px + 180, py + 108), "授权状态：已完成", (255, 255, 255, 58), font=font_small)
card(pd, (px + 14, py + 178, px + 302, py + 286), "今日筛查", "3 个题目 · 预计 2 分钟")
pd.text((px + 34, py + 230), "睡眠：一般   压力：中等   风险趋势：低", font=font_text, fill=(69, 83, 90, 255))
chip(pd, (px + 34, py + 250), "开始自评", (74, 122, 214, 255), font=font_small)
card(pd, (px + 14, py + 304, px + 302, py + 470), "今日训练计划", "AI 根据筛查结果生成")
for i, text in enumerate(["呼吸放松 3 分钟", "大笑训练 2 组", "情绪记录 1 次"]):
    y = py + 356 + i * 34
    pd.ellipse((px + 32, y + 4, px + 44, y + 16), fill=(46, 140, 113, 255))
    pd.text((px + 56, y), text, font=font_text, fill=(58, 72, 80, 255))
card(pd, (px + 14, py + 488, px + 302, py + 664), "情绪趋势", "近 7 天摘要，仅自己可见")
line_chart(pd, px + 34, py + 558, [2, 3, 3, 4, 3, 4, 5], (74, 122, 214, 255))
pd.text((px + 34, py + 632), "连续 3 天完成训练，建议继续保持。", font=font_small, fill=(96, 112, 121, 255))
for i, label in enumerate(["首页", "训练", "记录", "我的"]):
    x = px + 28 + i * 70
    pd.text((x, py + 692), label, font=font_small, fill=(91, 106, 114, 255))

# AI 陪练
px = phone_xs[1] + screen_margin
py = phone_y + screen_top
pd.rounded_rectangle((px + 14, py + 18, px + 302, py + 94), radius=28, fill=(67, 94, 184, 255))
pd.text((px + 28, py + 34), "AI 陪练", font=font_h1, fill="white")
pd.text((px + 28, py + 64), "安全陪伴，不作医学诊断", font=font_small, fill=(232, 237, 253, 255))
pd.ellipse((px + 26, py + 124, px + 74, py + 172), fill=(67, 94, 184, 255))
pd.rounded_rectangle((px + 86, py + 120, px + 286, py + 186), radius=24, fill=(233, 238, 255, 255))
pd.text((px + 102, py + 136), "我们先做一个 3 次呼吸练习，", font=font_text, fill=(55, 69, 78, 255))
pd.text((px + 102, py + 158), "好不好？", font=font_text, fill=(55, 69, 78, 255))
pd.rounded_rectangle((px + 80, py + 210, px + 290, py + 268), radius=24, fill=(226, 248, 240, 255))
pd.text((px + 102, py + 228), "我今天有点烦，睡不太好。", font=font_text, fill=(48, 69, 74, 255))
card(pd, (px + 14, py + 298, px + 302, py + 444), "个性化练习建议", "系统仅保存必要摘要与风险命中")
for i, text in enumerate(["1 分钟身体扫描", "记录一件今天完成的小事", "如果持续难受，可联系老师/家长"]):
    pd.text((px + 30, py + 344 + i * 28), text, font=font_text if i < 2 else font_small, fill=(60, 74, 82, 255))
card(pd, (px + 14, py + 466, px + 302, py + 604), "数字人陪练", "可选开启，界面会明确标注 AI 生成")
pd.rounded_rectangle((px + 30, py + 518, px + 124, py + 580), radius=20, fill=(245, 247, 251, 255))
pd.ellipse((px + 54, py + 528, px + 100, py + 574), fill=(120, 198, 170, 255))
pd.text((px + 140, py + 532), "虚拟陪练头像", font=font_text, fill=(63, 77, 84, 255))
chip(pd, (px + 140, py + 560), "需要单独授权", (234, 148, 63, 255), font=font_small)
pd.rounded_rectangle((px + 18, py + 640, px + 298, py + 694), radius=24, fill=(255, 255, 255, 255), outline=(221, 228, 231, 255), width=2)
pd.text((px + 34, py + 655), "输入你的感受...", font=font_text, fill=(149, 159, 165, 255))
pd.rounded_rectangle((px + 248, py + 648, px + 290, py + 686), radius=18, fill=(67, 94, 184, 255))

# 家长端
px = phone_xs[2] + screen_margin
py = phone_y + screen_top
pd.rounded_rectangle((px + 14, py + 18, px + 302, py + 122), radius=30, fill=(233, 154, 71, 255))
pd.text((px + 28, py + 36), "家长端摘要", font=font_h1, fill="white")
pd.text((px + 28, py + 70), "仅展示必要趋势与提醒", font=font_text, fill=(255, 241, 225, 255))
chip(pd, (px + 28, py + 92), "不显示完整聊天", (255, 255, 255, 58), font=font_small)
card(pd, (px + 14, py + 146, px + 302, py + 302), "本周状态摘要", "由系统自动汇总给监护人")
line_chart(pd, px + 30, py + 212, [3, 3, 4, 4, 3, 4, 4], (233, 154, 71, 255))
pd.text((px + 30, py + 278), "训练完成率 78%，情绪整体平稳。", font=font_small, fill=(94, 110, 118, 255))
card(pd, (px + 14, py + 320, px + 302, py + 476), "提醒事项", "当连续异常时才推送")
for i, text in enumerate(["已连续 2 天晚睡", "建议今晚一起完成呼吸练习", "当前无高风险预警"]):
    y = py + 368 + i * 30
    pd.text((px + 34, y), text, font=font_text if i < 2 else font_small, fill=(63, 77, 84, 255))
card(pd, (px + 14, py + 494, px + 302, py + 664), "授权与隐私", "监护同意、撤回、查看记录")
pd.text((px + 30, py + 542), "监护授权版本：v1.0", font=font_text, fill=(63, 77, 84, 255))
pd.text((px + 30, py + 574), "最近一次查看：今天 19:20", font=font_text, fill=(63, 77, 84, 255))
chip(pd, (px + 30, py + 606), "申请导出摘要", (233, 154, 71, 255), font=font_small)
for i, label in enumerate(["摘要", "趋势", "提醒", "设置"]):
    x = px + 28 + i * 70
    pd.text((x, py + 692), label, font=font_small, fill=(91, 106, 114, 255))

# 老师端
px = phone_xs[3] + screen_margin
py = phone_y + screen_top
pd.rounded_rectangle((px + 14, py + 18, px + 302, py + 122), radius=30, fill=(202, 91, 92, 255))
pd.text((px + 28, py + 36), "老师端预警", font=font_h1, fill="white")
pd.text((px + 28, py + 70), "只看摘要、原因与建议动作", font=font_text, fill=(255, 233, 233, 255))
chip(pd, (px + 28, py + 92), "人工复核必经", (255, 255, 255, 58), font=font_small)
card(pd, (px + 14, py + 146, px + 302, py + 392), "待处理预警", "班级 / 个人摘要")
rows = [
    ("高风险", "初二(3)班  林同学", "命中危机表达，建议立即联系"),
    ("中风险", "初二(1)班  王同学", "连续 5 天未完成训练"),
    ("低风险", "初二(2)班  周同学", "睡眠波动，建议继续观察"),
]
colors = {"高风险": (202, 91, 92, 255), "中风险": (233, 154, 71, 255), "低风险": (61, 134, 103, 255)}
for i, (level, name, note) in enumerate(rows):
    top = py + 186 + i * 62
    pd.rounded_rectangle((px + 26, top, px + 290, top + 52), radius=18, fill=(246, 248, 250, 255))
    chip(pd, (px + 36, top + 10), level, colors[level], font=font_tiny)
    pd.text((px + 100, top + 8), name, font=font_small, fill=(57, 70, 78, 255))
    pd.text((px + 100, top + 28), note, font=font_tiny, fill=(108, 120, 127, 255))
card(pd, (px + 14, py + 414, px + 302, py + 664), "复核详情", "不会默认展示完整私密内容")
pd.text((px + 30, py + 458), "风险原因：", font=font_text, fill=(60, 74, 82, 255))
pd.text((px + 30, py + 486), "自评文本出现明显自伤倾向表述，", font=font_small, fill=(84, 96, 104, 255))
pd.text((px + 30, py + 508), "且近 3 日情绪快速下滑。", font=font_small, fill=(84, 96, 104, 255))
pd.text((px + 30, py + 548), "建议动作：联系监护人、校心理老师、", font=font_small, fill=(84, 96, 104, 255))
pd.text((px + 30, py + 570), "安排线下复核并留痕。", font=font_small, fill=(84, 96, 104, 255))
chip(pd, (px + 30, py + 610), "标记处理中", (202, 91, 92, 255), font=font_small)
chip(pd, (px + 146, py + 610), "查看审计", (91, 110, 120, 255), font=font_small)

note_layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
shadow_box(note_layer, (84, 1060, 1716, 1148), radius=30, fill=(255, 255, 255, 220), shadow=(18, 44, 59, 24), blur=12, offset=(0, 6))
img.alpha_composite(note_layer)
d = ImageDraw.Draw(img)
d.text((108, 1086), "设计说明：学生端强调自评 + 训练；家长端只看摘要；老师端只处理必要预警；AI 与数字人均通过后端网关接入。", font=font_text, fill=(58, 72, 80, 255))

img = img.convert("RGB")
img.save(OUT, quality=95)
print(OUT)
