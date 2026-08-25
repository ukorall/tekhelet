package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*
import kotlin.math.ceil

/**
 * חישוב אורך החוט הנדרש, ומכאן - מה כדאי לקנות.
 *
 * הדינים שהחישוב נשען עליהם:
 *  - שליש גדיל ושני שליש ענף (מנחות ל"ט ע"א). הגדיל הוא הקשרים והכריכות.
 *  - אורך כולל של י"ב אגודלים לפחות מהקשר הראשון ועד קצה החוטים. לפי כמה
 *    שיטות אם קצר מזה הציצית פסולה, כי דין גרדומין נאמר רק כשנעשתה בכשרות
 *    מלכתחילה.
 *  - הגדיל צריך להיות ד' אגודלים, והענף פי שניים ממנו.
 *
 * המספרים כאן הם קירוב שימושי לקנייה, לא פסק. שיעור "אגודל" עצמו שנוי במחלוקת,
 * ולכן החישוב מציג טווח ולא מספר יחיד.
 */
object ThreadLength {

    /** שיעור אגודל בס"מ - טווח מקובל בין השיעורים השונים. */
    private const val THUMB_CM_MIN = 2.0
    private const val THUMB_CM_MAX = 2.4

    /** כמה ס"מ חוט "נבלע" בכל כריכה - קירוב גס, תלוי בעובי החוט. */
    private const val CM_PER_WIND = 1.6
    private const val CM_PER_DOUBLE_KNOT = 3.0

    data class Estimate(
        val totalWinds: Int,
        val doubleKnots: Int,
        /** אורך מינימלי של הציצית כולה (גדיל + ענף), בס"מ. */
        val minTotalCm: ClosedFloatingPointRange<Double>,
        /** כמה חוט תכלת נצרך בפועל לכריכה, בס"מ (מוכפל, כי החוט מקופל). */
        val tekheletWorkingCm: Double,
        val notes: List<String>
    )

    fun estimate(composition: KnotComposition): Estimate {
        val winds = totalWinds(composition)
        val knots = doubleKnots(composition)

        val minTotal = (12 * THUMB_CM_MIN)..(12 * THUMB_CM_MAX)
        val working = winds * CM_PER_WIND + knots * CM_PER_DOUBLE_KNOT

        val notes = buildList {
            add("החישוב מניח ${winds} כריכות ו-${knots} קשרים כפולים לפי ההרכב שבחרת.")
            add("צריך שהציצית כולה, מהקשר הראשון ועד קצה החוטים, תהיה לפחות י\"ב אגודלים - " +
                "בערך ${"%.0f".format(minTotal.start)}-${"%.0f".format(minTotal.endInclusive)} ס\"מ. " +
                "לפי כמה שיטות אם קצר מזה הציצית פסולה, כי דין גרדומין נאמר רק כשנעשתה בכשרות מלכתחילה.")
            add("מזה, הגדיל (הקשרים והכריכות) צריך להיות שליש והענף שני שליש.")
            if (composition.threadCount == ThreadCount.RAMBAM_1_OF_8) {
                add("שים לב: בשיטת הרמב\"ם רק חצי חוט צבוע בתכלת, ולכן צריך פחות חוט תכלת - " +
                    "אבל אסור לחבר חוט תכלת לחוט לבן כדי לייצר \"חוט\" אחד, כי יש בזה חשש בל תוסיף.")
            }
            if (winds >= 39) {
                add("זה הרכב עתיר כריכות. אם חוט התכלת שקנית קצר, שקול 7 חוליות במקום 13, " +
                    "או שלוש כריכות בכל חוליה במקום יותר.")
            }
        }

        return Estimate(winds, knots, minTotal, working, notes)
    }

    private fun totalWinds(c: KnotComposition): Int {
        if (c.windsPerChulya.isNotEmpty()) {
            val known = c.windsPerChulya.filter { it > 0 }
            if (known.isNotEmpty()) {
                // חוליה שלא הוגדר בה מספר - נניח את הממוצע של השאר
                val avg = known.average()
                return c.windsPerChulya.sumOf { if (it > 0) it else ceil(avg).toInt() }
            }
        }
        val chulyot = when (c.chulyotCount) {
            ChulyotCount.SEVEN -> 7
            ChulyotCount.THIRTEEN -> 13
            ChulyotCount.SEVEN_OR_THIRTEEN -> 13
            ChulyotCount.FOUR -> 4
            null -> 13
        }
        // ברירת מחדל: שלוש כריכות בחוליה, כדין הגמרא
        return if (c.chulyotCount == ChulyotCount.FOUR) 39 else chulyot * 3
    }

    private fun doubleKnots(c: KnotComposition): Int = when (c.knotScheme) {
        KnotScheme.NONE -> 2
        KnotScheme.KNOT_EVERY_CHULYA -> when (c.chulyotCount) {
            ChulyotCount.SEVEN -> 8
            ChulyotCount.THIRTEEN -> 14
            else -> 8
        }
        null -> 2
        else -> 5
    }
}
