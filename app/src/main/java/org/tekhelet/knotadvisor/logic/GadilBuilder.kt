package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/** יחידה אחת בגדיל - כריכה, קשר, או רווח בין חוליות. */
sealed interface GadilSegment {
    data class Wind(val tekhelet: Boolean, val chulyaIndex: Int) : GadilSegment
    data class Knot(val label: String) : GadilSegment
    data object Gap : GadilSegment
}

/**
 * בונה את רצף הגדיל מתוך ההרכב.
 *
 * זה הלב של שתי היכולות שביקשת: הוא מזין גם את הציור (TzitzitVisual) וגם את
 * הוראות הקשירה המילוליות (TyingInstructions) - כך ששניהם תמיד מספרים בדיוק
 * אותו סיפור, ואי אפשר שהם ייפרדו זה מזה.
 *
 * הכלל היחיד שנאכף כאן בלי תנאי: הכריכה הראשונה והאחרונה בלבן. זו גמרא מפורשת,
 * והרבה אנשים טועים בזה - ראו CompositionCoherence.alwaysRemember().
 */
object GadilBuilder {

    fun build(c: KnotComposition): List<GadilSegment> {
        val out = mutableListOf<GadilSegment>()
        val chulyot = chulyotCount(c)
        val windsPer = windsPerChulya(c, chulyot)
        val knotAfter = knotPositions(c, chulyot)

        out += GadilSegment.Knot("קשר כפול ראשון")

        for (i in 0 until chulyot) {
            val n = windsPer.getOrElse(i) { 3 }
            for (w in 0 until n) {
                val isVeryFirst = (i == 0 && w == 0)
                val isVeryLast = (i == chulyot - 1 && w == n - 1)
                val tekhelet = when {
                    isVeryFirst || isVeryLast -> false      // תמיד לבן - דין הגמרא
                    else -> windIsTekhelet(c, i, w)
                }
                out += GadilSegment.Wind(tekhelet, i)
            }
            if (i in knotAfter && i != chulyot - 1) {
                out += GadilSegment.Knot("קשר כפול")
            } else if (i != chulyot - 1) {
                out += GadilSegment.Gap
            }
        }

        out += GadilSegment.Knot("קשר כפול אחרון")
        return out
    }

    private fun chulyotCount(c: KnotComposition): Int = when (c.chulyotCount) {
        ChulyotCount.SEVEN -> 7
        ChulyotCount.THIRTEEN -> 13
        ChulyotCount.SEVEN_OR_THIRTEEN -> 13
        ChulyotCount.FOUR -> 4
        null -> 13
    }

    private fun windsPerChulya(c: KnotComposition, chulyot: Int): List<Int> {
        if (c.windsPerChulya.isNotEmpty()) {
            val known = c.windsPerChulya.filter { it > 0 }
            val fallback = if (known.isEmpty()) 3 else known.average().toInt().coerceAtLeast(3)
            return (0 until chulyot).map { c.windsPerChulya.getOrElse(it) { fallback }.let { v -> if (v > 0) v else fallback } }
        }
        // ארבע "חוליות" זה בעצם מנהג הלבן - קבוצות כריכות, לא חוליות של שלוש
        if (c.chulyotCount == ChulyotCount.FOUR) return listOf(7, 8, 11, 13)
        return List(chulyot) { 3 }
    }

    private fun windIsTekhelet(c: KnotComposition, chulyaIndex: Int, windIndex: Int): Boolean =
        when (c.windingColor) {
            WindingColor.MOSTLY_TEKHELET_SINGLE_WIND -> true
            WindingColor.MOSTLY_TEKHELET_FULL_CHULYA -> chulyaIndex != 0
            WindingColor.ALTERNATING_CHULYOT -> chulyaIndex % 2 == 1
            WindingColor.ALTERNATING_WINDS -> windIndex % 2 == 1
            null -> true
        }

    /** אחרי אילו חוליות (אינדקס) יש קשר כפול. */
    private fun knotPositions(c: KnotComposition, chulyot: Int): Set<Int> = when (c.knotScheme) {
        KnotScheme.NONE -> emptySet()
        KnotScheme.KNOT_EVERY_CHULYA -> (0 until chulyot).toSet()
        KnotScheme.PAIRS_2_2_2_1 -> cumulative(listOf(2, 2, 2, 1))
        KnotScheme.GROUPS_3_3_3_4 -> cumulative(listOf(3, 3, 3, 4))
        KnotScheme.GROUPS_4_4_4_1 -> cumulative(listOf(4, 4, 4, 1))
        KnotScheme.WINDS_7_8_11_13 -> cumulative(listOf(1, 1, 1, 1))
        null -> emptySet()
    }

    private fun cumulative(groups: List<Int>): Set<Int> {
        val out = mutableSetOf<Int>()
        var acc = 0
        groups.forEach { acc += it; out += acc - 1 }
        return out
    }

    // --- סטטיסטיקה לתצוגה ---
    fun summary(c: KnotComposition): Summary {
        val segs = build(c)
        return Summary(
            totalWinds = segs.count { it is GadilSegment.Wind },
            tekheletWinds = segs.count { it is GadilSegment.Wind && it.tekhelet },
            whiteWinds = segs.count { it is GadilSegment.Wind && !it.tekhelet },
            doubleKnots = segs.count { it is GadilSegment.Knot },
            chulyot = chulyotCount(c)
        )
    }

    data class Summary(
        val totalWinds: Int,
        val tekheletWinds: Int,
        val whiteWinds: Int,
        val doubleKnots: Int,
        val chulyot: Int
    )
}
