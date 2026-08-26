package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * דרישה מפורשת של המשתמש על הקשירה עצמה - "חשוב לי שיהיו קשרים כפולים בין
 * החוליות", "חשוב לי שיהיה קשר בהתחלה ובסוף".
 *
 * למה זה נפרד מהסליידרים: הסליידרים שואלים על **ערכים** (כמה חשוב לך שיהיה
 * זול, יפה, מבוסס). זה שואל על **הקשירה עצמה**, ובשפה של מי שכבר יודע מה הוא
 * רוצה. שתי השאלות לגיטימיות, והן משרתות שני סוגי משתמשים: מי שרוצה שיובילו
 * אותו, ומי שכבר יודע ובא לוודא.
 *
 * חשוב: דרישה כאן **לא פוסלת** שיטה שלא עונה עליה. היא דוחפת אותה למטה,
 * ובעיקר - היא מפעילה הצעת וריאציה ("מתאים לך רמב"ם, ואמרת שחשוב לך קשרים,
 * אז תעשה רמב"ם עם קשרים"). ראו ScoringEngine.suggestVariants.
 */
@Serializable
enum class Constraint(
    val label: String,
    val shortLabel: String,
    val explanation: String
) {
    WANT_FIVE_KNOTS(
        "חשוב לי שיהיו חמישה קשרים כפולים",
        "חשובים לך חמישה קשרים",
        "עניין חמשת הקשרים נזכר בזוהר, בתרגום יונתן ובמדרש תנחומא. הרב רפמן מחמיר " +
            "בזה מאוד וכותב שגם מי שנוהג כרמב\"ם וגם בני תימן צריכים לעשותם בזמן שיש " +
            "תכלת. מנגד, הרמב\"ם עצמו לא הזכיר אותם כלל."
    ),
    WANT_KNOT_BETWEEN_CHULYOT(
        "חשוב לי קשר כפול בין כל שתי חוליות",
        "חשוב לך קשר בין החוליות",
        "כך מפרידים בין החוליות במנהג אשכנז וספרד, וכך יש \"היכר חוליות\" ברור. " +
            "הרב שמואל אריאל מעדיף את זה על פני חוליות תימניות למי שאינו תימני."
    ),
    WANT_START_END_KNOT(
        "חשוב לי לפחות קשר כפול בהתחלה ובסוף",
        "חשוב לך קשר בהתחלה ובסוף",
        "הקשר הראשון פותר את החשש שחוליות הרמב\"ם אינן נחשבות \"קשר\" כשלעצמן, " +
            "ולכן ראוי להחמיר בו גם בשיטות שלא דורשות קשרים."
    ),
    WANT_YEMENITE_CHULYA(
        "אני רוצה חוליות תימניות",
        "אתה רוצה חוליות תימניות",
        "החוליה התימנית מחזיקה את עצמה בלי קשר. שים לב ששני המקורות שקראתי " +
            "מסתייגים מזה למי שאינו תימני - כל אחד כמנהג אבותיו."
    ),
    WANT_STANDARD_CHULYA(
        "אני מעדיף חוליות כמנהג אבותיי (לא תימניות)",
        "אתה מעדיף חוליה רגילה",
        "זו העמדה של הרב שמואל אריאל ושל הרב רפמן: אין סיבה שאשכנזי או ספרדי " +
            "יאמץ חוליות תימניות דווקא בתכלת."
    ),
    WANT_WHOLE_THREAD(
        "אני רוצה חוט תכלת שלם, לא חצי",
        "אתה רוצה חוט שלם",
        "שיטת הראב\"ד והערוך, וכן הכרעת הגר\"א. מי שעושה כך יוצא ידי חובה גם " +
            "לשיטת הרמב\"ם."
    );

    fun satisfiedBy(c: KnotComposition): Boolean = when (this) {
        WANT_FIVE_KNOTS -> c.knotScheme in setOf(
            KnotScheme.FIVE_GROUPS_CHINUCH, KnotScheme.FIVE_GROUPS_GRA,
            KnotScheme.FIVE_GROUPS_TOSAFOT, KnotScheme.FIVE_WINDS_7_8_11_13
        )
        WANT_KNOT_BETWEEN_CHULYOT -> c.knotScheme == KnotScheme.DOUBLE_EVERY_CHULYA
        WANT_START_END_KNOT -> c.knotScheme != KnotScheme.CHULYA_IS_KNOT
        WANT_YEMENITE_CHULYA -> c.chulyaForm in setOf(
            ChulyaForm.YEMENITE_SELF_HOLDING, ChulyaForm.YEMENITE_INVERTED
        )
        WANT_STANDARD_CHULYA -> c.chulyaForm == ChulyaForm.STANDARD_DOUBLE_KNOT
        WANT_WHOLE_THREAD -> c.threadCount != ThreadCount.RAMBAM_1_OF_8
    }
}
