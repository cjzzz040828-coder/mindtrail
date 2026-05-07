from PIL import Image, ImageOps, ImageDraw, ImageFont
import os


BASE = os.path.join(os.getcwd(), "pictures")
FILES = [
    "通用入口流程.png",
    "授权与同意.png",
    "学生筛查流程.png",
    "结果与训练开始.png",
    "支持与设置.png",
    "家长端核心界面.png",
    "老师端后续处理.png",
    "原型图.png",
]

FONT_PATH = r"C:\Windows\Fonts\msyh.ttc"
FONT = ImageFont.truetype(FONT_PATH, 28)
THUMB_W, THUMB_H = 760, 428
PAD = 30
COLS = 2
ROWS = 2


def build_sheet(batch_files, out_name):
    sheet_w = COLS * THUMB_W + (COLS + 1) * PAD
    sheet_h = ROWS * (THUMB_H + 60) + (ROWS + 1) * PAD
    sheet = Image.new("RGB", (sheet_w, sheet_h), (245, 248, 250))
    draw = ImageDraw.Draw(sheet)

    for i, name in enumerate(batch_files):
        path = os.path.join(BASE, name)
        im = Image.open(path).convert("RGB")
        fit = ImageOps.contain(im, (THUMB_W, THUMB_H))
        x = PAD + (i % COLS) * (THUMB_W + PAD)
        y = PAD + (i // COLS) * (THUMB_H + 60 + PAD)

        frame = Image.new("RGB", (THUMB_W, THUMB_H), "white")
        fx = (THUMB_W - fit.width) // 2
        fy = (THUMB_H - fit.height) // 2
        frame.paste(fit, (fx, fy))
        sheet.paste(frame, (x, y))

        draw.rounded_rectangle((x, y, x + THUMB_W, y + THUMB_H), radius=18, outline=(210, 217, 223), width=2)
        draw.text((x, y + THUMB_H + 12), os.path.splitext(name)[0], font=FONT, fill=(45, 58, 69))

    out_path = os.path.join(BASE, out_name)
    sheet.save(out_path, quality=95)
    return out_path


batch1 = FILES[:4]
batch2 = FILES[4:]

print(build_sheet(batch1, "contact_sheet_1.png"))
print(build_sheet(batch2, "contact_sheet_2.png"))
