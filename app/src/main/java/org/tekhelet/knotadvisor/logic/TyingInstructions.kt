package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.ChulyaForm
import org.tekhelet.knotadvisor.model.KnotComposition
import org.tekhelet.knotadvisor.model.ThreadCount

/**
 * הוראות קשירה מילוליות, נגזרות מאותו GadilBuilder שמזין את הציור - כך שהטקסט
 * והתמונה לא יכולים להיפרד.
 *
 * ההוראות **מתקפלות**: רצף שחוזר על עצמו בדיוק נכתב פעם אחת, ואחריו
 * "חזור על שלבים X-Y עוד N פעמים". בלי זה, שיטה של 13 חוליות מייצרת עשרים
 * שורות כמעט זהות, ואף אחד לא קורא את זה.
 */
object TyingInstructions {

    data class Step(val number: Int, val text: String, val note: String? = null)

    fun generate(c: KnotComposition): List<Step> {
        val opening = openingSteps(c)
        val body = collapse(bodyLines(c))
        val closing = closingSteps(c)

        val all = opening + body + closing
        val notes = mutableMapOf<Int, String>()
        all.indexOfFirst { it.contains("כריכה אחת בלבן") || it.contains("מתחילים בלבן") }
            .takeIf { it >= 0 }?.let {
                notes[it] = "הכריכה הראשונה חייבת להיות בלבן - זו גמרא מפורשת, " +
                    "ולא משנה באיזו שיטה בחרת."
            }
        return all.mapIndexed { i, t -> Step(i + 1, t, notes[i]) }
    }

    private fun openingSteps(c: KnotComposition): List<String> = buildList {
        add(threadsLine(c))
        add(
            "משחילים את החוטים בנקב הבגד. כדאי להכניס קודם את חוטי הלבן ואחר כך את " +
                "התכלת, כדי לקיים \"ונתנו על ציצית הכנף פתיל תכלת\" - שעל הלבן יינתן פתיל התכלת."
        )
    }

    private fun closingSteps(c: KnotComposition): List<String> = buildList {
        add("הגדיל צריך להיות כשליש מאורך הציצית, והענף שני שליש.")
        add(
            "הרב ינון מלאכי ממליץ ללחלח את הציציות במים לפני הכריכה, או לטבול אותן " +
                "רגע במים חמים אחריה, כדי שלא יתפרדו."
        )
    }

    /** שורה אחת לכל אלמנט בגדיל. */
    private fun bodyLines(c: KnotComposition): List<String> {
        val plan = GadilBuilder.plan(c)
        val form = plan.chulyaForm
        val out = mutableListOf<String>()

        plan.elements.forEach { el ->
            when (el) {
                is GadilBuilder.Element.Winds -> {
                    val p = el.piece
                    val colour = if (p.tekhelet) "בתכלת" else "בלבן"
                    val n = p.length
                    out += when (form) {
                        ChulyaForm.YEMENITE_SELF_HOLDING ->
                            if (p.isWholeChulya)
                                "חוליה תימנית $colour: חצי כריכה ימנית, שתי כריכות באלכסון " +
                                    "היורד משמאל לימין, וחצי כריכה שמאלית. המבנה מחזיק את עצמו."
                            else
                                "$n כריכות $colour, כחלק מהמבנה התימני."
                        ChulyaForm.YEMENITE_INVERTED ->
                            "$n כריכות $colour, בחוליה תימנית הפוכה."
                        else ->
                            if (n == 1) "כריכה אחת $colour." else "$n כריכות $colour."
                    }
                }
                GadilBuilder.Element.Knot -> out += "קושרים קשר כפול."
                GadilBuilder.Element.ChulyaGap ->
                    out += "משאירים רווח באורך חוליה, שדרכו רואים את החוטים שסביבם כורכים - " +
                        "זה מה שיוצר את היכר החוליות."
                GadilBuilder.Element.SmallGap ->
                    out += "משאירים רווח קטן וברור לפני החוליה הבאה."
            }
        }
        return out
    }

    /**
     * מקפל רצפים חוזרים.
     *
     * מחפש את התבנית הארוכה ביותר שחוזרת על עצמה ברצף לפחות פעמיים, כותב אותה
     * פעם אחת, ומוסיף שורת "חזור על שלבים X-Y עוד N פעמים". רץ שוב ושוב עד
     * שאין יותר מה לקפל.
     */
    internal fun collapse(lines: List<String>): List<String> {
        var current = lines
        var changed = true
        while (changed) {
            changed = false
            outer@ for (start in current.indices) {
                val maxLen = (current.size - start) / 2
                for (len in maxLen downTo 1) {
                    val block = current.subList(start, start + len)
                    var reps = 1
                    while (start + len * (reps + 1) <= current.size &&
                        current.subList(start + len * reps, start + len * (reps + 1)) == block
                    ) reps++

                    // כדאי לקפל רק אם זה באמת חוסך שורות
                    if (reps >= 2 && len * reps >= 3) {
                        val firstStep = start + 1
                        val lastStep = start + len
                        val label = if (len == 1)
                            "חוזרים על השלב הקודם עוד ${reps - 1} פעמים."
                        else
                            "חוזרים על שלבים $firstStep-$lastStep עוד ${reps - 1} פעמים."
                        current = current.subList(0, start + len) +
                            listOf(label) +
                            current.subList(start + len * reps, current.size)
                        changed = true
                        break@outer
                    }
                }
            }
        }
        return current
    }

    private fun threadsLine(c: KnotComposition): String = when (c.threadCount) {
        ThreadCount.RAMBAM_1_OF_8 ->
            "לוקחים שלושה חוטי לבן ועוד חוט שחציו תכלת וחציו לבן - אחד מתוך שמונה בתכלת."
        ThreadCount.RAAVAD_2_OF_8 ->
            "לוקחים שלושה חוטי לבן וחוט אחד שלם של תכלת - שניים מתוך שמונה בתכלת."
        ThreadCount.TOSAFOT_4_OF_8 ->
            "לוקחים שני חוטי לבן ושני חוטי תכלת - ארבעה מתוך שמונה בתכלת."
        null -> "לוקחים ארבעה חוטים, לפי מה שהכרעת בשאלת מספר החוטים."
    }
}
