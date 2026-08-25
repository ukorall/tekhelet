package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * פידבק מבודקי בטא.
 *
 * מה נמצא כאן חוץ מהטקסט החופשי, ולמה: כשמגיעים עשרות פידבקים, השאלה הראשונה
 * היא תמיד "על מה בדיוק הוא מדבר ובאיזו גרסה". בלי זה, חצי מהפידבקים לא ניתנים
 * לפעולה. לכן נאסף אוטומטית: הגרסה המדויקת (כדי לדעת אם הבאג כבר תוקן), המסך
 * שממנו נשלח (כדי למקם את הבעיה), והשיטה שהוצגה באותו רגע אם רלוונטי.
 * בנוסף נאספים במפורש: סוג הפידבק (כדי למיין), ודירוג (כדי לזהות מגמה גם בלי
 * לקרוא הכל).
 */
@Serializable
data class Feedback(
    val id: String,
    val createdAtEpochMillis: Long,
    /** מי שלח - אופציונלי, כדי שאפשר יהיה לחזור אליו. */
    val fromName: String = "",
    val kind: FeedbackKind,
    val text: String,
    /** 1-5, אופציונלי. שימושי כדי לראות מגמה בלי לקרוא כל פידבק. */
    val rating: Int? = null,
    // --- נאסף אוטומטית ---
    val appVersion: String = "",
    val screen: String = "",
    val methodId: String? = null,
    val androidRelease: String = "",
    val deviceModel: String = ""
)

@Serializable
enum class FeedbackKind(val label: String) {
    CONTENT_ERROR("טעות בתוכן או במקור"),
    BUG("משהו לא עובד"),
    UNCLEAR("ניסוח לא ברור"),
    MISSING("חסר משהו"),
    IDEA("רעיון או הצעה"),
    GENERAL("סתם מחשבה")
}
