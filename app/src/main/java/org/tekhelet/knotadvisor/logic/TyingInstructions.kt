package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*

/**
 * מייצר הוראות קשירה מילוליות מתוך ההרכב - בדיוק כמו "סיכום מעשי" של הרב אריאל
 * או "הנחיות לענ"ד" של הרב רפמן, רק מותאם לשיטה שנבחרה בפועל.
 *
 * נשען על אותו GadilBuilder שמזין את הציור, כדי שהטקסט והתמונה לא ייפרדו לעולם.
 */
object TyingInstructions {

    data class Step(val number: Int, val text: String, val note: String? = null)

    fun generate(c: KnotComposition): List<Step> {
        val steps = mutableListOf<String>()
        val notes = mutableMapOf<Int, String>()

        steps += threadsLine(c)
        steps += "משחילים את החוטים בנקב הבגד. כדאי להכניס קודם את חוטי הלבן ואחר כך את התכלת, " +
            "כדי לקיים \"ונתנו על ציצית הכנף פתיל תכלת\" - שעל הלבן יינתן פתיל התכלת."
        steps += "קושרים קשר כפול."

        val segs = GadilBuilder.build(c)
        var chulya = 0
        var runWinds = 0
        var runTekhelet = 0

        fun flush() {
            if (runWinds == 0) return
            chulya++
            val color = when {
                runTekhelet == runWinds -> "בתכלת"
                runTekhelet == 0 -> "בלבן"
                else -> "בתכלת ובלבן לסירוגין"
            }
            steps += "חוליה $chulya: כורכים $runWinds כריכות $color."
            runWinds = 0; runTekhelet = 0
        }

        segs.drop(1).forEach { seg ->
            when (seg) {
                is GadilSegment.Wind -> { runWinds++; if (seg.tekhelet) runTekhelet++ }
                is GadilSegment.Knot -> { flush(); steps += "קושרים קשר כפול." }
                is GadilSegment.Gap -> {
                    flush()
                    steps += "משאירים רווח ברור לפני החוליה הבאה, כדי שיהיה \"היכר חוליות\"."
                }
            }
        }
        flush()

        // ההערה שחייבת להופיע תמיד
        val firstWindStep = steps.indexOfFirst { it.startsWith("חוליה 1") }
        if (firstWindStep >= 0) {
            notes[firstWindStep] = "הכריכה הראשונה חייבת להיות בלבן - זו גמרא מפורשת, ולא משנה " +
                "באיזו שיטה בחרת."
        }
        notes[steps.lastIndex] = "גם הכריכה האחרונה בלבן, מאותו טעם: \"מעלין בקודש ולא מורידין\"."

        steps += "מסיימים בקשר כפול. הגדיל צריך להיות כשליש מאורך הציצית, והענף שני שליש."
        steps += "כדאי ללחלח את הציציות במים לפני הכריכה, או לטבול אותן רגע במים חמים אחריה, " +
            "כדי שלא יתפרדו."

        return steps.mapIndexed { i, t -> Step(i + 1, t, notes[i]) }
    }

    private fun threadsLine(c: KnotComposition): String = when (c.threadCount) {
        ThreadCount.RAMBAM_1_OF_8 ->
            "לוקחים שלושה חוטי לבן ועוד חוט שחציו תכלת וחציו לבן - כלומר אחד מתוך שמונה בתכלת."
        ThreadCount.RAAVAD_2_OF_8 ->
            "לוקחים שלושה חוטי לבן וחוט אחד שלם של תכלת - כלומר שניים מתוך שמונה בתכלת."
        ThreadCount.TOSAFOT_4_OF_8 ->
            "לוקחים שני חוטי לבן ושני חוטי תכלת - כלומר ארבעה מתוך שמונה בתכלת."
        null -> "לוקחים ארבעה חוטים, כשחלקם צבועים בתכלת לפי מה שהכרעת בשאלת מספר החוטים."
    }
}
