package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/**
 * בונה את הגדיל מתוך ההרכב.
 *
 * ## המודל: זרם כריכות, לא רשימת חוליות
 *
 * הגרסה הקודמת בנתה חוליה-אחרי-חוליה ותחבה קשרים ביניהן. זה היה **שגוי
 * מהיסוד**, וזה מה שגרם לשיטה החסידית להיראות לא נכון: שם הקשרים נופלים
 * *בתוך* חוליות ולא ביניהן.
 *
 * המודל הנכון, שאוריאל תיאר ואומת מול המפרט שלו:
 *  1. יש **זרם רציף של כריכות** (בחסידית: 39).
 *  2. **חוליה** היא כל שלוש כריכות רצופות בזרם.
 *  3. **קשרים** ממוקמים אחרי מספר כריכה מסוים - לא אחרי חוליה.
 *  4. מה שרואים בפועל הוא "מקטעים": רצף כריכות באותו צבע, שלא חוצה גבול
 *     חוליה ולא חוצה קשר. לכן חוליה יכולה להתפצל למקטע של 1 ומקטע של 2.
 *
 * אימות: בחסידית הקשרים אחרי כריכות 7, 15, 26, 39 (חלוקת 7-8-11-13), הכריכה
 * הראשונה והאחרונה לבנות. המקטעים שיוצאים מזה הם בדיוק
 * `לבן1, תכלת2, תכלת3, תכלת1 | תכלת2, תכלת3, תכלת3 | ...` - כפי שאוריאל תיאר.
 *
 * זה גם מה שהופך את הציור ל"מורכב מרכיבים": כל מקטע הוא רכיב, ואפשר להרכיב
 * כל שיטה מהם בלי לצייר תמונה סגורה לכל שיטה בנפרד.
 */
object GadilBuilder {

    /** ברירת מחדל: חוליה היא שלוש כריכות (הגמרא - "כדי שיכרוך וישנה וישלש"). */
    const val WINDS_PER_CHULYA = 3

    /**
     * מקטע אחד שרואים בגדיל: רצף כריכות באותו צבע, בתוך אותה חוליה, בלי קשר
     * באמצע. `length` הוא בין 1 ל-WINDS_PER_CHULYA.
     */
    data class Piece(
        val tekhelet: Boolean,
        val length: Int,
        /** האם המקטע הוא חוליה שלמה, או רק חלק ממנה (בגלל קשר או שינוי צבע). */
        val isWholeChulya: Boolean,
        val chulyaIndex: Int
    )

    sealed interface Element {
        data class Winds(val piece: Piece) : Element
        data object Knot : Element
        /** רווח באורך חוליה, שדרכו רואים את החוטים המאונכים (שיטת הרמב"ם). */
        data object ChulyaGap : Element
        /** רווח קטן להיכר חוליות. */
        data object SmallGap : Element
    }

    data class Plan(
        val elements: List<Element>,
        val totalWinds: Int,
        val tekheletWinds: Int,
        val chulyot: Int,
        val doubleKnots: Int,
        val chulyaForm: ChulyaForm
    ) {
        val whiteWinds: Int get() = totalWinds - tekheletWinds
    }

    fun plan(c: KnotComposition): Plan {
        val totalWinds = totalWinds(c)
        val knotAfter = knotPositions(c, totalWinds)
        val colours = windColours(c, totalWinds)
        val form = c.chulyaForm ?: ChulyaForm.STANDARD_DOUBLE_KNOT

        val elements = mutableListOf<Element>()

        // קשר פותח, אלא אם החוליה עצמה היא הקשר
        if (c.knotScheme != KnotScheme.CHULYA_IS_KNOT) elements += Element.Knot

        var runStart = 1
        for (i in 1..totalWinds) {
            val endOfChulya = i % WINDS_PER_CHULYA == 0
            val knotHere = i in knotAfter
            val colourChanges = i == totalWinds || colours[i] != colours[i + 1]
            val lastWind = i == totalWinds

            if (endOfChulya || knotHere || colourChanges || lastWind) {
                val length = i - runStart + 1
                val chulyaIndex = (runStart - 1) / WINDS_PER_CHULYA
                elements += Element.Winds(
                    Piece(
                        tekhelet = colours[runStart],
                        length = length,
                        isWholeChulya = length == WINDS_PER_CHULYA &&
                            (runStart - 1) % WINDS_PER_CHULYA == 0,
                        chulyaIndex = chulyaIndex
                    )
                )
                if (knotHere && !lastWind) {
                    elements += Element.Knot
                } else if (endOfChulya && !lastWind) {
                    elements += if (needsWideGap(c, form)) Element.ChulyaGap else Element.SmallGap
                }
                runStart = i + 1
            }
        }

        // קשר סוגר
        if (c.knotScheme != KnotScheme.CHULYA_IS_KNOT &&
            c.knotScheme != KnotScheme.DOUBLE_AT_START
        ) elements += Element.Knot

        return Plan(
            elements = elements,
            totalWinds = totalWinds,
            tekheletWinds = (1..totalWinds).count { colours[it] },
            chulyot = (totalWinds + WINDS_PER_CHULYA - 1) / WINDS_PER_CHULYA,
            doubleKnots = elements.count { it is Element.Knot },
            chulyaForm = form
        )
    }

    /**
     * ברמב"ם ובמנהג תימן אין קשר בין החוליות, ולכן ההפרדה היא רווח רחב שדרכו
     * רואים את החוטים - וזה מה שיוצר את "היכר החוליות".
     */
    private fun needsWideGap(c: KnotComposition, form: ChulyaForm): Boolean =
        form == ChulyaForm.YEMENITE_SELF_HOLDING ||
            c.knotScheme == KnotScheme.CHULYA_IS_KNOT

    private fun totalWinds(c: KnotComposition): Int {
        c.windGroups.takeIf { it.isNotEmpty() }?.let { return it.sum() }
        val chulyot = when (c.chulyotCount) {
            ChulyotCount.SEVEN -> 7
            ChulyotCount.THIRTEEN -> 13
            ChulyotCount.SEVEN_OR_THIRTEEN -> 13
            null -> 13
        }
        return chulyot * WINDS_PER_CHULYA
    }

    /** אחרי אילו מספרי כריכה יש קשר כפול (1-based). */
    private fun knotPositions(c: KnotComposition, totalWinds: Int): Set<Int> {
        // windGroups מגדיר ישירות כמה כריכות בין קשר לקשר
        c.windGroups.takeIf { it.isNotEmpty() }?.let { groups ->
            if (c.knotScheme == KnotScheme.FIVE_WINDS_7_8_11_13 ||
                c.knotScheme == KnotScheme.DOUBLE_EVERY_CHULYA
            ) {
                var acc = 0
                return groups.map { acc += it; acc }.toSet()
            }
        }
        val chulyot = totalWinds / WINDS_PER_CHULYA
        fun afterChulyot(groups: List<Int>): Set<Int> {
            var acc = 0
            return groups.map { acc += it; acc * WINDS_PER_CHULYA }.filter { it <= totalWinds }.toSet()
        }
        return when (c.knotScheme) {
            KnotScheme.CHULYA_IS_KNOT, KnotScheme.DOUBLE_AT_START,
            KnotScheme.DOUBLE_AT_START_AND_END, null -> emptySet()
            KnotScheme.DOUBLE_EVERY_CHULYA -> (1..chulyot).map { it * WINDS_PER_CHULYA }.toSet()
            KnotScheme.FIVE_GROUPS_TOSAFOT -> afterChulyot(listOf(2, 2, 2, 1))
            KnotScheme.FIVE_GROUPS_CHINUCH -> afterChulyot(listOf(3, 3, 3, 4))
            KnotScheme.FIVE_GROUPS_GRA -> afterChulyot(listOf(4, 4, 4, 1))
            KnotScheme.FIVE_WINDS_7_8_11_13 -> {
                var acc = 0
                listOf(7, 8, 11, 13).map { acc += it; acc }.filter { it <= totalWinds }.toSet()
            }
        }
    }

    /**
     * צבע כל כריכה, 1-based.
     *
     * הכריכה הראשונה והאחרונה **תמיד** לבנות - דין גמרא מפורש (מנחות ל"ט ע"א),
     * וזה נאכף כאן ולא נשאר לנתונים, כדי שאי אפשר יהיה להגדיר שיטה שמפרה אותו.
     */
    private fun windColours(c: KnotComposition, total: Int): Map<Int, Boolean> {
        val chulyaOf = { i: Int -> (i - 1) / WINDS_PER_CHULYA }
        val map = (1..total).associateWith { i ->
            when (c.windingColor) {
                WindingColor.MOSTLY_TEKHELET_SINGLE_WIND -> true
                WindingColor.MOSTLY_TEKHELET_FULL_CHULYA ->
                    chulyaOf(i) != 0 && chulyaOf(i) != chulyaOf(total)
                WindingColor.ALTERNATING_CHULYOT -> chulyaOf(i) % 2 == 1
                WindingColor.ALTERNATING_CHULYOT_TEKHELET_FIRST -> chulyaOf(i) % 2 == 0
                WindingColor.ALTERNATING_WINDS -> (i % 2) == 0
                null -> true
            }
        }.toMutableMap()
        map[1] = false
        map[total] = false
        return map
    }

    fun summary(c: KnotComposition): Plan = plan(c)
}
