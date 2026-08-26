"""משחזר מיפוי תווים ל-PDF עברי בלי ToUnicode, בלי להניח כלום על מבנה הגופן.

**מצב: עובד חלקית - בערך 70% מהאותיות.** מספיק כדי לחפש מילה, לא מספיק כדי
לצטט. מה שנשאר לתקן: הגליפים המודגשים מתפרשים כאותיות אחרות (ו' כד'), כי
ההשוואה היא מול משקל אחד בלבד. ניסיון להוסיף משקלים נוספים דווקא החמיר,
כנראה כי הנרמול לריבוע מבטל חלק מהמידע שמבחין בין משקלים. הכיוון הנכון הוא
כנראה להשוות מול הגופן המקורי (MFFrankRuhl) ולא מול פרנק-רוהל של גוגל.

עד אז, הדרך האמינה לקרוא את הקבצים האלה היא לרנדר עמוד לתמונה ולקרוא אותה.

לכל קוד תו: חותכים את הצורה שלו מתוך רינדור של העמוד (זו האמת - בדיוק מה
שהעין רואה), ומשווים לאותיות שמרונדרות מגופן עברי ידוע. ההתאמה הטובה ביותר
היא האות. אין כאן הנחה שאינדקס הגליף שווה לקוד התו - ההנחה הזו שגויה כאן.
"""
import sys, pymupdf
from PIL import Image, ImageFont, ImageDraw

REF = 'app/src/main/res/font/frank_ruhl_libre_variable.ttf'
ALPHABET = list('אבגדהוזחטיכךלמםנןסעפףצץקרשת') + list('0123456789') + list('.,;:()[]-?!')
N = 40

def norm(img):
    """חיתוך לדיו, ואז הטמעה בריבוע **בלי למתוח** - יחס הגובה-רוחב הוא מידע
    מבחין: הוא מה שמפריד בין ו' לר' ובין נקודה למקף."""
    bbox = img.point(lambda v: 255 if v < 190 else 0).getbbox()
    if not bbox: return None
    g = img.crop(bbox).convert('L')
    s = max(g.width, g.height)
    scale = (N - 4) / s
    g = g.resize((max(1, int(g.width*scale)), max(1, int(g.height*scale))), Image.LANCZOS)
    canvas = Image.new('L', (N, N), 255)
    canvas.paste(g, ((N-g.width)//2, (N-g.height)//2))
    return canvas

def ref_images(size=90):
    """מרנדרים כל אות בכמה משקלים. הכותרות במסמך מודגשות והגוף לא, וגליף מודגש
    לא מתאים היטב לאות רגילה - זה מה שגרם ל-ו' להתפרש כ-ד' בכותרת."""
    f = ImageFont.truetype(REF, size)
    out = {}
    for ch in ALPHABET:
        im = Image.new('L', (size*3, size*3), 255)
        ImageDraw.Draw(im).text((size, size), ch, font=f, fill=0)
        n = norm(im)
        if n: out[ch + "\u0000400"] = (n, list(n.getdata()))
    return out

def code_images(doc, dpi=400, pages=6):
    first = {}
    for pno in range(min(pages, len(doc))):
        pg = doc[pno]
        pix = pg.get_pixmap(dpi=dpi)
        page = Image.frombytes("RGB", (pix.width, pix.height), pix.samples).convert('L')
        sc = dpi / 72.0
        for blk in pg.get_text("rawdict")["blocks"]:
            for ln in blk.get("lines", []):
                for sp in ln["spans"]:
                    if sp["size"] < 9: continue
                    for ch in sp["chars"]:
                        c = ch["c"]
                        if c in ' \n' or c in first: continue
                        x0, y0, x1, y1 = [v*sc for v in ch["bbox"]]
                        if x1-x0 < 3 or y1-y0 < 3: continue
                        n = norm(page.crop((int(x0), int(y0), int(x1), int(y1))))
                        if n: first[c] = n
    return first

def build(pdf):
    doc = pymupdf.open(pdf)
    refs = ref_images()
    glyphs = code_images(doc)
    mapping = {}
    for c, gimg in glyphs.items():
        gp = list(gimg.getdata())
        scored = sorted(
            ((sum(abs(a-b) for a, b in zip(gp, rp))/(N*N*255.0), key.split("\u0000")[0])
             for key, (_, rp) in refs.items())
        )
        best = scored[0][1]
        alt = next((ch for _, ch in scored if ch != best), best)
        altscore = next((s for s, ch in scored if ch != best), 1.0)
        mapping[c] = (best, round(scored[0][0], 4), alt, round(altscore, 4))
    return mapping, doc

if __name__ == '__main__':
    m, doc = build(sys.argv[1])
    for c in sorted(m, key=ord):
        ch, s, ch2, s2 = m[c]
        flag = '  <-- close' if s2 - s < 0.02 else ''
        print(f"  {ord(c):02X} -> {ch}  ({s})   2nd: {ch2} ({s2}){flag}")
