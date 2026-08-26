package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * וריאציה על שיטה - "רמב"ם עם קשרים", "רב נגן עם קשרי גר"א", "רמב"ם בחוט ראב"ד".
 *
 * למה זה קיים כישות בפני עצמה, ולא רק דרך בונה ההרכב האישי:
 *
 * הרבה מהשיטות שאנשים **באמת קושרים** אינן השיטה ה"נקייה" של הראשון, אלא
 * צירוף שנוצר בפועל - בדרך כלל הוספה של משהו מבוסס לשיטה קיימת. אדם שמתאים לו
 * רמב"ם אבל רוצה קשרים לא צריך "לבנות הרכב מאפס"; הוא צריך לשמוע "תעשה רמב"ם
 * עם קשרים, וזו שיטה נפוצה לגמרי".
 *
 * לכן וריאציה היא **override חלקי** על ההרכב של שיטת הבסיס: היא יורשת ממנה כל
 * מה שלא שונה במפורש, מקבלת ניקוד בשאלון בזכות עצמה, ומוצגת בעמוד שיטת הבסיס
 * כ"מה עוד אפשר לעשות עם זה".
 */
@Serializable
data class MethodVariant(
    val id: String,
    val name: String,
    /** למה אנשים עושים את זה, בשורה. */
    val rationale: String,
    // --- override חלקי: כל מה ש-null יורש משיטת הבסיס ---
    val threadCount: ThreadCount? = null,
    val windingColor: WindingColor? = null,
    val chulyotCount: ChulyotCount? = null,
    val chulyaForm: ChulyaForm? = null,
    val knotScheme: KnotScheme? = null,
    val windGroups: List<Int> = emptyList(),
    /** כמה נפוצה הוריאציה בפועל, 0-10. משפיע על הניקוד. */
    val commonness: Int = 5,
    val note: String? = null
) {
    /** מחיל את הוריאציה על הרכב הבסיס. */
    fun applyTo(base: KnotComposition): KnotComposition = base.copy(
        threadCount = threadCount ?: base.threadCount,
        windingColor = windingColor ?: base.windingColor,
        chulyotCount = chulyotCount ?: base.chulyotCount,
        chulyaForm = chulyaForm ?: base.chulyaForm,
        knotScheme = knotScheme ?: base.knotScheme,
        windGroups = windGroups.ifEmpty { base.windGroups },
        note = note ?: base.note
    )
}
