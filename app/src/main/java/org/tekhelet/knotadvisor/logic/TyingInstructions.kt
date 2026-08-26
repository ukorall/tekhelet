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
        val closing = closingSteps()

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

    private fun closingSteps(): List<String> = buildList {
        add("הגדיל צריך להיות כשליש מאורך הציצית, והענף שני שליש.")
        add(
            "הרב ינון מלאכי ממליץ ללחלח את הציציות במים לפני הכריכה, או לטבול אותן " +
                "רגע במים חמים אחריה, כדי שלא יתפרדו."
        )
    }

    /**
     * שורה אחת לכל יחידה שאדם באמת עושה.
     *
     * הנקודה החשובה כאן: כשהכריכות מתחלפות בכל כריכה (הראב"ד), הציור צריך כל
     * כריכה בנפרד - אבל **ההוראות לא**. אף אחד לא רוצה לקרוא "כריכה בלבן,
     * כריכה בתכלת" שבע פעמים; הוא רוצה "חוליה של 7 כריכות, לבן ותכלת
     * לסירוגין, מתחיל בלבן" - וכך גם המקורות מתארים את זה. לכן מקטעים
     * שנמצאים באותה חוליה מאוחדים כאן לשורה אחת.
     */
    private fun bodyLines(c: KnotComposition): List<String> {
        val plan = GadilBuilder.plan(c)
        val form = plan.chulyaForm
        val out = mutableListOf<String>()

        var i = 0
        while (i < plan.elements.size) {
            when (val el = plan.elements[i]) {
                is GadilBuilder.Element.Winds -> {
                    // אוספים את כל המקטעים של אותה חוליה
                    var j = i
                    var winds = 0
                    val chulya = el.piece.chulyaIndex
                    while (j < plan.elements.size) {
                        val e = plan.elements[j]
                        if (e !is GadilBuilder.Element.Winds || e.piece.chulyaIndex != chulya) break
                        winds += e.piece.length
                        j++
                    }
                    val parts = j - i
                    val first = el.piece
                    val colour = if (first.tekhelet) "בתכלת" else "בלבן"

                    out += when {
                        parts > 1 -> {
                            val startsWith = if (first.tekhelet) "בתכלת" else "בלבן"
                            "חוליה של $winds כריכות, לבן ותכלת לסירוגין, מתחילה $startsWith."
                        }
                        form == ChulyaForm.YEMENITE_SELF_HOLDING && first.isWholeChulya ->
                            "חוליה תימנית $colour: חצי כריכה ימנית, שתי כריכות באלכסון היורד " +
                                "משמאל לימין, וחצי כריכה שמאלית. המבנה מחזיק את עצמו."
                        form == ChulyaForm.YEMENITE_SELF_HOLDING ->
                            "$winds כריכות $colour, כחלק מהמבנה התימני."
                        form == ChulyaForm.YEMENITE_INVERTED ->
                            "$winds כריכות $colour, בחוליה תימנית הפוכה."
                        winds == 1 -> "כריכה אחת $colour."
                        else -> "$winds כריכות $colour."
                    }
                    i = j
                    continue
                }
                GadilBuilder.Element.Knot -> out += "קושרים קשר כפול."
                GadilBuilder.Element.ChulyaGap ->
                    out += "משאירים רווח באורך חוליה, שדרכו רואים את החוטים שסביבם כורכים - " +
                        "זה מה שיוצר את היכר החוליות."
                GadilBuilder.Element.SmallGap ->
                    out += "משאירים רווח קטן וברור לפני החוליה הבאה."
            }
            i++
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

                    // מקפלים רק אם זה חוסך לפחות שורה אחת בפועל. בלי זה נוצרות
                    // שורות "חוזרים עוד פעם אחת" שלא חוסכות כלום ורק מפריעות.
                    val saving = len * (reps - 1) - 1
                    if (reps >= 2 && saving >= 1) {
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
