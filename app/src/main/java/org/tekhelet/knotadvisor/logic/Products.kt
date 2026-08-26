package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/**
 * המוצרים שאפשר לקנות בפועל - סטים של פתילי תכלת.
 *
 * ## מקור המחירים
 *
 * המחירים כאן נלקחו מחנות **ארגון פתיל תכלת** (chanut.tekhelet.com), מהקטלוג
 * עצמו ולא מהערכה. הם נכונים לאוגוסט 2026 ויכולים להשתנות - הם נועדו לתת סדר
 * גודל ולעזור להשוות בין המדרגות, לא לשמש מחירון.
 *
 * שמות המוצרים כאן הם **בדיוק** השמות שבחנות, כדי שאפשר יהיה למצוא אותם.
 * במיוחד: מה שאני קורא לו "ארוך" נמכר תחת השם "אורך סטנדרתי".
 *
 * ## כלל האצבע
 *
 * כלל האצבע שאוריאל נתן, והוא הבסיס לכל החישוב כאן:
 * **סופרים רק את הכריכות בתכלת.** קרוב ל-40 → צריך ארוך. קרוב ל-20 → קצר
 * (חינוך/גר"א). קרוב ל-0 → 7 (רלוונטי רק לראב"ד, ובפועל כמעט אף פעם לא).
 */
object Products {

    enum class Thickness(val label: String) { THIN("דק"), THICK("עבה") }

    /**
     * "מדרגת אורך" - זה מה שבאמת קובע את המחיר, והרוב הגדול של השיטות נופל
     * על המדרגה האמצעית או העליונה.
     */
    enum class LengthTier(val label: String) {
        SEVEN("7"),
        CHINUCH_GRA("חינוך/גר\"א"),
        STANDARD("אורך סטנדרתי"),
        SCHACHTER("רב שכטר")
    }

    data class Product(
        /** השם המדויק כפי שהוא מופיע בחנות. */
        val name: String,
        val threadCount: ThreadCount,
        val tier: LengthTier,
        val priceThin: Int,
        val priceThick: Int,
        val note: String? = null
    )

    /** מחירי החנות של ארגון פתיל תכלת, אוגוסט 2026. */
    val catalogue = listOf(
        Product(
            "רמב\"ם 7", ThreadCount.RAMBAM_1_OF_8, LengthTier.CHINUCH_GRA, 115, 145,
            "השם מטעה: מבחינה פרקטית זה מקביל לחינוך/גר\"א של הראב\"ד, ולא ל-\"7\" שלו. " +
                "ברמב\"ם יש רק חצי חוט בתכלת, ולכן אותו אורך מספיק לפחות כריכות."
        ),
        Product(
            "רמב\"ם 13", ThreadCount.RAMBAM_1_OF_8, LengthTier.STANDARD, 200, 230,
            "זה הרמב\"ם ה\"מלא\" - שלוש עשרה חוליות."
        ),
        Product(
            "ראב\"ד 7", ThreadCount.RAAVAD_2_OF_8, LengthTier.SEVEN, 115, 145,
            "לדעתי מוצר שלא אמור להתקיים - הוא בקושי מספיק לאיזושהי שיטה קיימת."
        ),
        Product("ראב\"ד חינוך/גר\"א", ThreadCount.RAAVAD_2_OF_8, LengthTier.CHINUCH_GRA, 175, 205),
        Product(
            "ראב\"ד אורך סטנדרתי", ThreadCount.RAAVAD_2_OF_8, LengthTier.STANDARD, 200, 230,
            "זה מה שאני קורא לו \"ארוך\". בחנות הוא פשוט ה\"סתם\"."
        ),
        Product("תוספות חינוך/גר\"א", ThreadCount.TOSAFOT_4_OF_8, LengthTier.CHINUCH_GRA, 285, 335),
        Product(
            "תוספות אורך סטנדרתי", ThreadCount.TOSAFOT_4_OF_8, LengthTier.STANDARD, 315, 355,
            "בתוספות שני חוטים שלמים בתכלת, ולכן הקפיצה במחיר."
        ),
        Product(
            "תוספות רב שכטר", ThreadCount.TOSAFOT_4_OF_8, LengthTier.SCHACHTER, 335, 385,
            "ארוך במיוחד."
        )
    )

    /**
     * קו מוצרים נפרד: הצמר נופץ לשמה. זה **לא** מדרגת אורך אלא מדרגת הידור,
     * ולכן הוא נשמר בנפרד ולא מעורבב בטבלת האורכים. אין בו פיצול לדק/עבה.
     */
    data class LishmahProduct(val name: String, val threadCount: ThreadCount, val price: Int)

    val lishmah = listOf(
        LishmahProduct("ניפוץ לשמה - ראב\"ד חינוך/גר\"א", ThreadCount.RAAVAD_2_OF_8, 415),
        LishmahProduct("ניפוץ לשמה - רמב\"ם", ThreadCount.RAMBAM_1_OF_8, 440),
        LishmahProduct("ניפוץ לשמה - ראב\"ד", ThreadCount.RAAVAD_2_OF_8, 440),
        LishmahProduct("ניפוץ לשמה - תוספות", ThreadCount.TOSAFOT_4_OF_8, 570)
    )

    data class Recommendation(
        val tekheletWinds: Int,
        val tier: LengthTier,
        val reasoning: String,
        val matches: List<Product>,
        val lishmahOptions: List<LishmahProduct>
    )

    /** ממליץ על מדרגת אורך ומוצרים, לפי מספר הכריכות בתכלת בלבד. */
    fun recommend(composition: KnotComposition): Recommendation {
        val summary = GadilBuilder.plan(composition)
        val t = summary.tekheletWinds

        val tier = when {
            t >= 30 -> LengthTier.STANDARD
            t >= 12 -> LengthTier.CHINUCH_GRA
            else -> LengthTier.SEVEN
        }
        val reasoning = when (tier) {
            LengthTier.STANDARD ->
                "בהרכב הזה יוצאות $t כריכות בתכלת - קרוב ל-40, ולכן צריך את הארוך " +
                    "(בחנות: \"אורך סטנדרתי\")."
            LengthTier.CHINUCH_GRA ->
                "בהרכב הזה יוצאות $t כריכות בתכלת - קרוב ל-20, ולכן מספיק חינוך/גר\"א " +
                    "(או רמב\"ם 7, שמקביל לו מבחינה פרקטית)."
            LengthTier.SEVEN ->
                "בהרכב הזה יוצאות רק $t כריכות בתכלת, ולכן תיאורטית מספיק \"7\". " +
                    "בפועל זה כמעט אף פעם לא באמת רלוונטי."
            LengthTier.SCHACHTER -> "הרכב עתיר כריכות במיוחד."
        }

        val threadCount = composition.threadCount
        // מציגים גם את המדרגה שמעל: עודף חוט הוא חסרון קטן, חוסר הוא כישלון.
        val matches = catalogue.filter {
            (threadCount == null || it.threadCount == threadCount) &&
                (it.tier == tier || it.tier == LengthTier.STANDARD)
        }.sortedBy { it.priceThin }

        val lishmahOptions = lishmah.filter { threadCount == null || it.threadCount == threadCount }

        return Recommendation(t, tier, reasoning, matches, lishmahOptions)
    }
}
