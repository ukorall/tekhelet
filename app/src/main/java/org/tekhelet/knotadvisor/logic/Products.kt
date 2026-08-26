package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/**
 * המוצרים שאפשר לקנות בפועל - סטים של חוטי תכלת ולבן.
 *
 * המספרים כאן הם **קנה מידה, לא מחירון מדויק**. אוריאל הדגיש את זה במפורש,
 * והמטרה היא רק לתת סדר גודל ולעזור להבין לאיזו "מדרגה" של אורך צריך להגיע.
 *
 * כלל האצבע שאוריאל נתן, והוא הבסיס לכל החישוב כאן:
 * **סופרים רק את הכריכות בתכלת.** קרוב ל-40 → צריך ארוך. קרוב ל-20 → קצר
 * (או רמב"ם 7). קרוב ל-0 → 7 (רלוונטי רק לראב"ד ואולי לתוספות, ובפועל כמעט
 * אף פעם לא).
 */
object Products {

    enum class Thickness(val label: String) { THIN("דק"), THICK("עבה") }

    /**
     * "מדרגת אורך" - זה מה שבאמת קובע את המחיר, והרוב הגדול של השיטות נופל
     * על המדרגה האמצעית.
     */
    enum class LengthTier(val label: String) {
        SEVEN("7"),
        SHORT("חינוך/גר\"א (בעבר נקרא \"קצר\")"),
        LONG("ארוך (נמכר גם כ\"סתם\")"),
        EXTRA_LONG("ארוך במיוחד")
    }

    data class Product(
        val name: String,
        val threadCount: ThreadCount,
        val tier: LengthTier,
        val priceThin: Int,
        val priceThick: Int,
        val note: String? = null
    )

    val catalogue = listOf(
        Product("רמב\"ם 7", ThreadCount.RAMBAM_1_OF_8, LengthTier.SHORT, 127, 157,
            "מבחינה פרקטית מקביל דווקא לחינוך/גר\"א, ולא ל\"7\" של הראב\"ד."),
        Product("רמב\"ם 13", ThreadCount.RAMBAM_1_OF_8, LengthTier.LONG, 210, 240,
            "נמכר לפעמים כ\"רמב\"ם\" סתם. בדרך כלל עולה בערך כמו ראב\"ד חינוך/גר\"א."),
        Product("ראב\"ד 7", ThreadCount.RAAVAD_2_OF_8, LengthTier.SEVEN, 127, 157,
            "לדעתי מוצר שלא אמור להתקיים - הוא בקושי מספיק לאיזושהי שיטה קיימת."),
        Product("ראב\"ד חינוך/גר\"א", ThreadCount.RAAVAD_2_OF_8, LengthTier.SHORT, 185, 215,
            "בעבר נקרא \"ראב\"ד קצר\"."),
        Product("ראב\"ד", ThreadCount.RAAVAD_2_OF_8, LengthTier.LONG, 210, 240,
            "המוכרים משאירים את זה כ\"סתם ראב\"ד\"; אני קורא לזה \"ארוך\"."),
        Product("תוספות 7", ThreadCount.TOSAFOT_4_OF_8, LengthTier.SEVEN, 225, 280),
        Product("תוספות חינוך/גר\"א", ThreadCount.TOSAFOT_4_OF_8, LengthTier.SHORT, 295, 355),
        Product("תוספות", ThreadCount.TOSAFOT_4_OF_8, LengthTier.LONG, 330, 370,
            "כמו בראב\"ד - נמכר כ\"סתם\", וזה למעשה הארוך."),
        Product("תוספות הרב שכטר", ThreadCount.TOSAFOT_4_OF_8, LengthTier.EXTRA_LONG, 345, 405,
            "ארוך במיוחד.")
    )

    data class Recommendation(
        val tekheletWinds: Int,
        val tier: LengthTier,
        val reasoning: String,
        val matches: List<Product>
    )

    /** ממליץ על מדרגת אורך ומוצרים, לפי מספר הכריכות בתכלת בלבד. */
    fun recommend(composition: KnotComposition): Recommendation {
        val summary = GadilBuilder.plan(composition)
        val t = summary.tekheletWinds

        val tier = when {
            t >= 30 -> LengthTier.LONG
            t >= 12 -> LengthTier.SHORT
            else -> LengthTier.SEVEN
        }
        val reasoning = when (tier) {
            LengthTier.LONG ->
                "בהרכב הזה יוצאות $t כריכות בתכלת - קרוב ל-40, ולכן צריך את הארוך."
            LengthTier.SHORT ->
                "בהרכב הזה יוצאות $t כריכות בתכלת - קרוב ל-20, ולכן מספיק חינוך/גר\"א " +
                    "(או רמב\"ם 7, שמקביל לו מבחינה פרקטית)."
            LengthTier.SEVEN ->
                "בהרכב הזה יוצאות רק $t כריכות בתכלת, ולכן תיאורטית מספיק \"7\". " +
                    "בפועל זה כמעט אף פעם לא באמת רלוונטי."
            LengthTier.EXTRA_LONG -> "הרכב עתיר כריכות במיוחד."
        }

        val threadCount = composition.threadCount
        val matches = catalogue.filter {
            (threadCount == null || it.threadCount == threadCount) &&
                (it.tier == tier || it.tier == LengthTier.LONG)
        }.sortedBy { it.priceThin }

        return Recommendation(t, tier, reasoning, matches)
    }
}
