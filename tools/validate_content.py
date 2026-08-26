#!/usr/bin/env python3
"""
בודק שקבצי התוכן ב-assets תואמים למודל ב-Kotlin.

למה זה קיים: ערך enum ב-JSON שלא קיים יותר בקוד גורם ל-kotlinx.serialization
לזרוק בזמן ריצה - והאפליקציה קורסת בכל מסך שנוגע בשיטות, בלי שהקומפילציה
תתלונן בכלל. בדיוק זה קרה: שתי שיטות נשארו עם שמות KnotScheme ישנים אחרי
שינוי שמות ב-enum, הבנייה עברה, והאפליקציה קרסה בכל מקום.

הסקריפט רץ ב-CI לפני הבנייה, ומפיל את ה-build אם יש אי-התאמה.
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
MODEL_DIR = ROOT / "app/src/main/java/org/tekhelet/knotadvisor/model"
ASSETS = ROOT / "app/src/main/assets"


def kotlin_enums() -> dict[str, set[str]]:
    """
    שולף את כל ערכי ה-enum שמוגדרים בקוד.

    צריך להתמודד עם שתי צורות כתיבה: enum על שורה אחת בלי פרמטרים
    (`enum class Era { GEONIM, RISHONIM }`), ו-enum רב-שורתי שבו לכל ערך יש
    פרמטרים בסוגריים. לכן מוצאים את גוף ה-enum בעזרת ספירת סוגריים, חותכים
    בנקודה-פסיק אם יש (שם נגמרת רשימת הערכים ומתחילים איברים אחרים), ואז
    לוקחים כל מזהה שנמצא בעומק סוגריים אפס.
    """
    enums: dict[str, set[str]] = {}
    for f in MODEL_DIR.glob("*.kt"):
        txt = f.read_text(encoding="utf-8")
        # מסירים הערות קודם: סוגריים בתוך טקסט הערה שיברו את ספירת העומק
        txt = re.sub(r"/\*.*?\*/", "", txt, flags=re.S)
        txt = re.sub(r"//[^\n]*", "", txt)
        for m in re.finditer(r"enum class (\w+)", txt):
            name = m.group(1)
            brace = txt.find("{", m.end())
            if brace < 0:
                continue
            depth, i = 0, brace
            while i < len(txt):
                if txt[i] == "{":
                    depth += 1
                elif txt[i] == "}":
                    depth -= 1
                    if depth == 0:
                        break
                i += 1
            body = txt[brace + 1 : i]

            values, depth, token = set(), 0, ""
            for ch in body:
                if ch in "([":
                    depth += 1
                elif ch in ")]":
                    depth -= 1
                elif depth == 0:
                    if ch == ";":
                        break
                    if ch == ",":
                        cand = token.strip()
                        if re.fullmatch(r"[A-Z][A-Z0-9_]*", cand):
                            values.add(cand)
                        token = ""
                        continue
                    token += ch
                    continue
                if depth > 0 and ch in "([":
                    cand = token.strip()
                    if re.fullmatch(r"[A-Z][A-Z0-9_]*", cand):
                        values.add(cand)
                    token = ""
            cand = token.strip()
            if re.fullmatch(r"[A-Z][A-Z0-9_]*", cand):
                values.add(cand)
            enums[name] = values
    return enums


COMPOSITION_FIELDS = {
    "threadCount": "ThreadCount",
    "windingColor": "WindingColor",
    "chulyotCount": "ChulyotCount",
    "chulyaForm": "ChulyaForm",
    "knotScheme": "KnotScheme",
}


def main() -> int:
    enums = kotlin_enums()
    problems: list[str] = []

    methods = json.loads((ASSETS / "methods.json").read_text(encoding="utf-8"))["methods"]

    seen_ids = set()
    for m in methods:
        mid = m.get("id", "<ללא id>")
        if mid in seen_ids:
            problems.append(f"{mid}: id כפול")
        seen_ids.add(mid)

        if m.get("era") not in enums.get("Era", set()):
            problems.append(f"{mid}: era לא חוקי - {m.get('era')!r}")

        for ax in m.get("axisScores", {}):
            if ax not in enums.get("Axis", set()):
                problems.append(f"{mid}: ציר לא קיים - {ax!r}")

        comp = m.get("composition", {})
        for field, enum_name in COMPOSITION_FIELDS.items():
            v = comp.get(field)
            if v is not None and v not in enums.get(enum_name, set()):
                problems.append(
                    f"{mid}: {field}={v!r} אינו ערך חוקי של {enum_name}. "
                    f"הערכים הקיימים: {sorted(enums.get(enum_name, set()))}"
                )

        # וריאציות משתמשות באותם enums, ולכן נבדקות באותה מידה
        seen_variants = set()
        for var in m.get("variants", []):
            vid = var.get("id", "<ללא id>")
            if vid in seen_variants:
                problems.append(f"{mid}: וריאציה עם id כפול - {vid}")
            seen_variants.add(vid)
            if not var.get("name") or not var.get("rationale"):
                problems.append(f"{mid}/{vid}: לוריאציה חסר name או rationale")
            for field, enum_name in COMPOSITION_FIELDS.items():
                v = var.get(field)
                if v is not None and v not in enums.get(enum_name, set()):
                    problems.append(
                        f"{mid}/{vid}: {field}={v!r} אינו ערך חוקי של {enum_name}"
                    )

        # "מה אפשר לעשות עם זה" - טקסט חופשי, אבל בלי כותרת או גוף זה מוצג ריק
        for i, opt in enumerate(m.get("practicalOptions", [])):
            if not opt.get("title") or not opt.get("body"):
                problems.append(
                    f"{mid}: practicalOptions[{i}] חסר title או body"
                )

    questions = json.loads((ASSETS / "questions.json").read_text(encoding="utf-8"))["questions"]
    for q in questions:
        qid = q.get("id", "<ללא id>")
        if q.get("type") not in enums.get("QuestionType", set()):
            problems.append(f"{qid}: type לא חוקי - {q.get('type')!r}")
        if q.get("stage") not in enums.get("QuestionStage", set()):
            problems.append(f"{qid}: stage לא חוקי - {q.get('stage')!r}")
        if q.get("axis") is not None and q["axis"] not in enums.get("Axis", set()):
            problems.append(f"{qid}: axis לא חוקי - {q['axis']!r}")

    # תיקיות תמונות צריכות להתאים ל-id אמיתי של שיטה
    images = ROOT / "content/images"
    if images.is_dir():
        for d in images.iterdir():
            if d.is_dir() and d.name not in seen_ids:
                problems.append(
                    f"content/images/{d.name}/ אינה תואמת לאף id של שיטה - "
                    f"תמונות שם לא יוצגו לעולם"
                )

    if problems:
        print("אימות התוכן נכשל:\n", file=sys.stderr)
        for p in problems:
            print(f"  • {p}", file=sys.stderr)
        return 1

    print(f"התוכן תקין: {len(methods)} שיטות, {len(questions)} שאלות.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
