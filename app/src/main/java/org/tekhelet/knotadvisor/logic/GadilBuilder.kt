package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/**
 * יחידה אחת בגדיל. הצורות כאן מתורגמות ישירות לציור ב-TzitzitVisual, לפי המפרט
 * שאוריאל נתן:
 *  - קשר כפול מצויר כ-X כפול.
 *  - חוליה תימנית = חצי ליפוף ימני, שני ליפופים באלכסון (משמאל למעלה לימין
 *    למטה), וחצי ליפוף שמאלי. סה"כ מבנה אחד באורך שלושה ליפופים.
 *  - חוליה תימנית משולבת (רק ראשונה/אחרונה) = חלק לבן כליפוף "שבור", וחלק
 *    התכלת כחצי ימני + אלכסון + חצי שמאלי.
 *  - חוליה תימנית הפוכה = קו אלכסוני על גבי הליפופים. יכולה להיות באורך 1, 2
 *    או 3 ליפופים, כי בשיטה החסידית קשר כפול חותך אותה באמצע.
 *  - ברמב"ם יש בין כל שתי חוליות רווח באורך חוליה, שדרכו רואים את החוטים
 *    המאונכים שסביבם מלפפים.
 */
sealed interface GadilSegment {
    /** ליפוף פשוט. */
    data class Wind(val tekhelet: Boolean, val chulyaIndex: Int) : GadilSegment

    /** חוליה תימנית שלמה - נתפסת כיחידה אחת באורך שלושה ליפופים. */
    data class YemeniteChulya(
        val tekhelet: Boolean,
        val chulyaIndex: Int,
        /** ראשונה/אחרונה יכולה להיות משולבת: חלק לבן "שבור" + חלק תכלת. */
        val mixedWithWhite: Boolean = false
    ) : GadilSegment

    /** חוליה תימנית הפוכה - קו אלכסוני על הליפופים. אורך 1-3 ליפופים. */
    data class InvertedYemenite(
        val tekhelet: Boolean,
        val chulyaIndex: Int,
        val windCount: Int
    ) : GadilSegment

    data class Knot(val label: String) : GadilSegment

    /** רווח באורך חוליה, שדרכו רואים את החוטים המאונכים (שיטת הרמב"ם). */
    data object ChulyaGap : GadilSegment

    /** רווח קטן בלבד, להיכר חוליות. */
    data object SmallGap : GadilSegment
}

object GadilBuilder {

    fun build(c: KnotComposition): List<GadilSegment> {
        val out = mutableListOf<GadilSegment>()
        val chulyot = chulyotCount(c)
        val windsPer = windsPerChulya(c, chulyot)
        val knotAfter = knotPositions(c, chulyot)

        if (c.knotScheme != KnotScheme.CHULYA_IS_KNOT) {
            out += GadilSegment.Knot("קשר כפול ראשון")
        }

        for (i in 0 until chulyot) {
            val n = windsPer.getOrElse(i) { 3 }
            val isFirst = i == 0
            val isLast = i == chulyot - 1
            out += renderChulya(c, i, n, isFirst, isLast)

            if (!isLast) {
                if (i in knotAfter) out += GadilSegment.Knot("קשר כפול")
                else if (c.chulyaForm == ChulyaForm.YEMENITE_SELF_HOLDING ||
                    c.knotScheme == KnotScheme.CHULYA_IS_KNOT
                ) out += GadilSegment.ChulyaGap
                else out += GadilSegment.SmallGap
            }
        }

        if (c.knotScheme != KnotScheme.CHULYA_IS_KNOT &&
            c.knotScheme != KnotScheme.DOUBLE_AT_START
        ) {
            out += GadilSegment.Knot("קשר כפול אחרון")
        }
        return out
    }

    /** בונה חוליה אחת לפי צורת החוליה שנבחרה. */
    private fun renderChulya(
        c: KnotComposition,
        index: Int,
        winds: Int,
        isFirst: Boolean,
        isLast: Boolean
    ): List<GadilSegment> {
        val tekhelet = chulyaIsTekhelet(c, index)
        // הכריכה הראשונה והאחרונה תמיד בלבן - דין גמרא, ראו CompositionCoherence
        val edgeWhite = isFirst || isLast

        return when (c.chulyaForm) {
            ChulyaForm.YEMENITE_SELF_HOLDING -> listOf(
                GadilSegment.YemeniteChulya(
                    tekhelet = tekhelet,
                    chulyaIndex = index,
                    mixedWithWhite = edgeWhite && tekhelet
                )
            )
            ChulyaForm.YEMENITE_INVERTED -> listOf(
                GadilSegment.InvertedYemenite(
                    tekhelet = tekhelet && !(edgeWhite && winds <= 1),
                    chulyaIndex = index,
                    windCount = winds.coerceIn(1, 3)
                )
            )
            else -> (0 until winds).map { w ->
                val veryFirst = isFirst && w == 0
                val veryLast = isLast && w == winds - 1
                GadilSegment.Wind(
                    tekhelet = if (veryFirst || veryLast) false else windIsTekhelet(c, index, w),
                    chulyaIndex = index
                )
            }
        }
    }

    private fun chulyaIsTekhelet(c: KnotComposition, index: Int): Boolean =
        when (c.windingColor) {
            WindingColor.MOSTLY_TEKHELET_SINGLE_WIND -> true
            WindingColor.MOSTLY_TEKHELET_FULL_CHULYA -> index != 0
            WindingColor.ALTERNATING_CHULYOT -> index % 2 == 1
            WindingColor.ALTERNATING_WINDS -> true
            null -> true
        }

    private fun windIsTekhelet(c: KnotComposition, chulyaIndex: Int, windIndex: Int): Boolean =
        when (c.windingColor) {
            WindingColor.MOSTLY_TEKHELET_SINGLE_WIND -> true
            WindingColor.MOSTLY_TEKHELET_FULL_CHULYA -> chulyaIndex != 0
            WindingColor.ALTERNATING_CHULYOT -> chulyaIndex % 2 == 1
            WindingColor.ALTERNATING_WINDS -> windIndex % 2 == 1
            null -> true
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
            return (0 until chulyot).map {
                val v = c.windsPerChulya.getOrElse(it) { fallback }
                if (v > 0) v else fallback
            }
        }
        // ארבע "חוליות" זה מנהג הלבן - קבוצות כריכות, לא חוליות של שלוש
        if (c.chulyotCount == ChulyotCount.FOUR) return listOf(7, 8, 11, 13)
        return List(chulyot) { 3 }
    }

    /** אחרי אילו חוליות (אינדקס) יש קשר כפול. */
    private fun knotPositions(c: KnotComposition, chulyot: Int): Set<Int> = when (c.knotScheme) {
        KnotScheme.CHULYA_IS_KNOT, KnotScheme.DOUBLE_AT_START,
        KnotScheme.DOUBLE_AT_START_AND_END -> emptySet()
        KnotScheme.DOUBLE_EVERY_CHULYA -> (0 until chulyot).toSet()
        KnotScheme.FIVE_GROUPS_TOSAFOT -> cumulative(listOf(2, 2, 2, 1))
        KnotScheme.FIVE_GROUPS_CHINUCH -> cumulative(listOf(3, 3, 3, 4))
        KnotScheme.FIVE_GROUPS_GRA -> cumulative(listOf(4, 4, 4, 1))
        KnotScheme.FIVE_WINDS_7_8_11_13 -> cumulative(listOf(3, 3, 3, 3))
        null -> emptySet()
    }

    private fun cumulative(groups: List<Int>): Set<Int> {
        val out = mutableSetOf<Int>()
        var acc = 0
        groups.forEach { acc += it; out += acc - 1 }
        return out
    }

    fun summary(c: KnotComposition): Summary {
        val segs = build(c)
        var total = 0
        var tekhelet = 0
        segs.forEach { s ->
            when (s) {
                is GadilSegment.Wind -> { total++; if (s.tekhelet) tekhelet++ }
                is GadilSegment.YemeniteChulya -> {
                    total += 3
                    if (s.tekhelet) tekhelet += if (s.mixedWithWhite) 2 else 3
                }
                is GadilSegment.InvertedYemenite -> {
                    total += s.windCount
                    if (s.tekhelet) tekhelet += s.windCount
                }
                else -> Unit
            }
        }
        return Summary(
            totalWinds = total,
            tekheletWinds = tekhelet,
            whiteWinds = total - tekhelet,
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
