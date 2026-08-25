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

        var chulya = 0
        var runWinds = 0
        var runTekhelet = 0

        fun flushWinds() {
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

        GadilBuilder.build(c).forEach { seg ->
            when (seg) {
                is GadilSegment.Wind -> {
                    runWinds++
                    if (seg.tekhelet) runTekhelet++
                }

                is GadilSegment.YemeniteChulya -> {
                    flushWinds()
                    chulya++
                    steps += if (seg.mixedWithWhite)
                        "חוליה $chulya (תימנית, משולבת): כריכה אחת בלבן, ואז המבנה התימני בתכלת - " +
                            "חצי כריכה ימנית, שתי כריכות באלכסון, וחצי כריכה שמאלית."
                    else
                        "חוליה $chulya (תימנית${if (seg.tekhelet) " בתכלת" else " בלבן"}): " +
                            "חצי כריכה ימנית, שתי כריכות באלכסון היורד משמאל לימין, וחצי כריכה שמאלית. " +
                            "המבנה הזה מחזיק את עצמו, ולכן אין צורך בקשר אחריו."
                }

                is GadilSegment.InvertedYemenite -> {
                    flushWinds()
                    chulya++
                    steps += "חוליה $chulya (תימנית הפוכה, ${seg.windCount} כריכות" +
                        "${if (seg.tekhelet) " בתכלת" else " בלבן"}): כמו החוליה התימנית אבל בכיוון " +
                        "ההפוך, כך שהיא אינה מחזיקה את עצמה."
                }

                is GadilSegment.Knot -> {
                    flushWinds()
                    steps += "קושרים קשר כפול."
                }

                GadilSegment.ChulyaGap -> {
                    flushWinds()
                    steps += "משאירים רווח באורך חוליה שלמה, שדרכו רואים את החוטים שסביבם כורכים - " +
                        "זה מה שיוצר את \"היכר החוליות\"."
                }

                GadilSegment.SmallGap -> {
                    flushWinds()
                    steps += "משאירים רווח קטן וברור לפני החוליה הבאה, כדי שיהיה היכר בין החוליות."
                }
            }
        }
        flushWinds()

        val firstChulyaStep = steps.indexOfFirst { it.startsWith("חוליה 1") }
        if (firstChulyaStep >= 0) {
            notes[firstChulyaStep] = "הכריכה הראשונה חייבת להיות בלבן - זו גמרא מפורשת, " +
                "ולא משנה באיזו שיטה בחרת."
        }
        notes[steps.lastIndex] = "גם הכריכה האחרונה בלבן, מאותו טעם: \"מעלין בקודש ולא מורידין\"."

        steps += "הגדיל צריך להיות כשליש מאורך הציצית, והענף שני שליש."
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
