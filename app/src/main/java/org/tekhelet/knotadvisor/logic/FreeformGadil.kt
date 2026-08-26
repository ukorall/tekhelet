package org.tekhelet.knotadvisor.logic

import kotlin.random.Random

/**
 * גדיל חופשי - רצף מקטעים שנבחרים אחד-אחד, בלי שום מודל הלכתי מאחוריו.
 *
 * ## למה זה נפרד מ-GadilBuilder
 *
 * [GadilBuilder] בונה גדיל מתוך **הרכב הלכתי**: מכריעים בחמישה דיונים, והגדיל
 * נגזר מהם. זו הנקודה החזקה שלו וגם המגבלה שלו - אי אפשר לבקש ממנו חוליה
 * אדומה, כי אין דבר כזה.
 *
 * כאן ההיגיון הפוך: בוחרים ישירות מה יש בכל מקטע, כולל דברים שאין להם שום
 * משמעות - חוליה בגוונים, קשר שטוח, פפיון. זה מכוון. הכלי הזה נמצא ב"טביעת
 * אצבע", והוא צעצוע ולא פוסק. **אסור** להזין ממנו המלצות, מחירים או הוראות
 * קשירה, ולכן הוא לא מייצר `KnotComposition` בכלל.
 */
object FreeformGadil {

    enum class SegmentGroup(val label: String) {
        CHULYOT("חוליות"),
        WINDS("ליפופים"),
        KNOTS("קשרים"),
        GAPS("רווחים"),
        ODD("מקטעים מוזרים")
    }

    /**
     * `units` הוא הגובה שהמקטע תופס בציור, ביחידות של כריכה אחת - אותן יחידות
     * שבהן GadilBuilder מודד, כדי ששני הציורים ייראו באותו קנה מידה.
     */
    enum class SegmentKind(
        val label: String,
        val group: SegmentGroup,
        val units: Float
    ) {
        CHULYA_TEKHELET("חוליה בתכלת", SegmentGroup.CHULYOT, 3f),
        CHULYA_WHITE("חוליה בלבן", SegmentGroup.CHULYOT, 3f),
        CHULYA_ALTERNATING("חוליה לסירוגין", SegmentGroup.CHULYOT, 3f),
        YEMENITE_TEKHELET("חוליה תימנית בתכלת", SegmentGroup.CHULYOT, 3f),
        YEMENITE_WHITE("חוליה תימנית בלבן", SegmentGroup.CHULYOT, 3f),
        YEMENITE_MIXED_FIRST("חוליה תימנית משולבת, לבן ראשון", SegmentGroup.CHULYOT, 3f),
        YEMENITE_MIXED_LAST("חוליה תימנית משולבת, לבן אחרון", SegmentGroup.CHULYOT, 3f),
        YEMENITE_INVERTED("חוליה תימנית הפוכה", SegmentGroup.CHULYOT, 3f),

        WIND_TEKHELET("ליפוף תכלת בודד", SegmentGroup.WINDS, 1f),
        WIND_WHITE("ליפוף לבן בודד", SegmentGroup.WINDS, 1f),
        WINDS_TEKHELET_2("שני ליפופי תכלת", SegmentGroup.WINDS, 2f),
        WINDS_WHITE_2("שני ליפופי לבן", SegmentGroup.WINDS, 2f),

        KNOT_DOUBLE("קשר כפול", SegmentGroup.KNOTS, 1.8f),

        GAP_CHULYA("רווח באורך חוליה", SegmentGroup.GAPS, 2.4f),
        GAP_SMALL("רווח קטן", SegmentGroup.GAPS, 0.8f),

        // מכאן והלאה: אין לזה שום משמעות. זו כל הנקודה.
        ODD_SPLIT_3("שלישייה חצי-לבן חצי-תכלת", SegmentGroup.ODD, 3f),
        ODD_RED_CHULYA("חוליה אדומה", SegmentGroup.ODD, 3f),
        ODD_RED_WIND("ליפוף אדום בודד", SegmentGroup.ODD, 1f),
        ODD_YEMENITE_RED("חוליה תימנית אדומה", SegmentGroup.ODD, 3f),
        ODD_RAINBOW_3("שלישייה בגוונים", SegmentGroup.ODD, 3f),
        ODD_FLAT_KNOT("קשר שטוח", SegmentGroup.ODD, 1.8f),
        ODD_BOW("פפיון", SegmentGroup.ODD, 2f),
        ODD_SPIRAL("לולאה מסולסלת", SegmentGroup.ODD, 3f);

        val isOdd: Boolean get() = group == SegmentGroup.ODD
    }

    private val sane = SegmentKind.entries.filterNot { it.isOdd }
    private val odd = SegmentKind.entries.filter { it.isOdd }

    /**
     * מייצר גדיל מהתשובות.
     *
     * הזרע נגזר מהתשובות עצמן, ולכן אותן תשובות נותנות תמיד את אותו גדיל -
     * אחרת זו לא טביעת אצבע אלא סתם כפתור אקראי. רוב המקטעים שפויים, ואחד
     * מכל חמישה בערך מוזר, כדי שתמיד ייצא משהו שמושך את העין.
     */
    fun generate(seed: Int, count: Int): List<SegmentKind> {
        val rnd = Random(seed)
        return List(count) {
            if (rnd.nextInt(5) == 0) odd.random(rnd) else sane.random(rnd)
        }
    }

    /** זרע יציב מתוך התשובות. */
    fun seedFrom(answers: Collection<String>): Int =
        answers.joinToString("|").fold(7_919) { acc, c -> acc * 31 + c.code }
}
