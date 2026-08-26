package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.KnotComposition
import org.tekhelet.knotadvisor.model.ThreadCount

/**
 * "יש לי מספיק חוט?" - מחשבון לשאריות.
 *
 * התרחיש שזה נועד לו: מישהו רוצה להכין ציצית משאריות ולדעת אם יש לו מספיק,
 * בלי לקנות סט חדש.
 *
 * **המקדמים כאן הם אומדן גס וזמני.** אוריאל אמר שהוא ייתן בהמשך רשימת אורכים
 * בסנטימטרים ודרך חישוב מדויקת של כמה "מבזבז" קשר כפול, כמה מבזבז ליפוף,
 * וחלוקה ל-2. עד אז - זה נותן סדר גודל, וכל המספרים מרוכזים כאן בקבועים
 * כדי שיהיה קל להחליף אותם ברגע שיגיעו הנתונים האמיתיים.
 */
object ThreadLength {

    // --- מקדמים לכיול. להחליף כשיגיעו המספרים האמיתיים. ---
    /** כמה ס"מ חוט "נבלע" בליפוף אחד סביב הגדיל. */
    const val CM_PER_WIND = 1.6
    /** כמה ס"מ נבלעים בקשר כפול. */
    const val CM_PER_DOUBLE_KNOT = 3.0
    /** שיעור אגודל, בס"מ - טווח בין השיעורים המקובלים. */
    const val THUMB_CM_MIN = 2.0
    const val THUMB_CM_MAX = 2.4

    data class Estimate(
        val tekheletWinds: Int,
        val totalWinds: Int,
        val doubleKnots: Int,
        /** כמה ס"מ של חוט תכלת נצרכים לכריכה עצמה. */
        val tekheletNeededCm: Double,
        /** האורך המינימלי של הציצית כולה (גדיל + ענף), בס"מ. */
        val minTotalCm: ClosedFloatingPointRange<Double>,
        val notes: List<String>
    )

    fun estimate(composition: KnotComposition): Estimate {
        val s = GadilBuilder.plan(composition)
        val minTotal = (12 * THUMB_CM_MIN)..(12 * THUMB_CM_MAX)
        // החוט מקופל, ולכן צריך בערך פי שניים מהאורך ה"נצרך"
        val tekheletCm = (s.tekheletWinds * CM_PER_WIND + s.doubleKnots * CM_PER_DOUBLE_KNOT) * 2

        val notes = buildList {
            add("בהרכב הזה יוצאות ${s.totalWinds} כריכות בסך הכול, מהן ${s.tekheletWinds} בתכלת, " +
                "ו-${s.doubleKnots} קשרים כפולים.")
            add("הציצית כולה, מהקשר הראשון ועד קצה החוטים, צריכה להיות לפחות י\"ב אגודלים - " +
                "בערך ${"%.0f".format(minTotal.start)}-${"%.0f".format(minTotal.endInclusive)} ס\"מ. " +
                "לפי כמה שיטות אם קצרה מזה הציצית פסולה, כי דין גרדומין נאמר רק כשנעשתה " +
                "בכשרות מלכתחילה.")
            add("מזה, הגדיל (הקשרים והכריכות) הוא שליש והענף שני שליש.")
            if (composition.threadCount == ThreadCount.RAMBAM_1_OF_8) {
                add("בשיטת הרמב\"ם רק חצי חוט צבוע בתכלת. שים לב שאסור לחבר חוט תכלת לחוט לבן " +
                    "כדי לייצר חוט כזה בעצמך - יש בזה חשש בל תוסיף.")
            }
        }
        return Estimate(s.tekheletWinds, s.totalWinds, s.doubleKnots, tekheletCm, minTotal, notes)
    }

    data class Verdict(val enough: Boolean, val headroomCm: Double, val message: String)

    /** בודק אם אורך חוט תכלת נתון (בס"מ) מספיק להרכב. */
    fun check(composition: KnotComposition, availableCm: Double): Verdict {
        val e = estimate(composition)
        val needed = e.tekheletNeededCm + e.minTotalCm.endInclusive * 0.67
        val headroom = availableCm - needed
        return Verdict(
            enough = headroom >= 0,
            headroomCm = headroom,
            message = if (headroom >= 0)
                "נראה שיש לך מספיק - בערך ${"%.0f".format(headroom)} ס\"מ מעל הנדרש. " +
                    "זה אומדן גס, אז אם זה גבולי כדאי למדוד בזהירות."
            else
                "כנראה שחסר לך בערך ${"%.0f".format(-headroom)} ס\"מ. " +
                    "אפשר לשקול פחות חוליות (7 במקום 13), או שלוש כריכות בכל חוליה במקום יותר."
        )
    }
}
