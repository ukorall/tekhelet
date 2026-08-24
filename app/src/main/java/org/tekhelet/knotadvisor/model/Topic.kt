package org.tekhelet.knotadvisor.model

/**
 * ארבעת אזורי הייעוץ שהאפליקציה שואפת לתמוך בהם (ראו DESIGN.md סעיף "מטרות עתידיות"):
 * שלוש שאלות עוקבות בתהליך ההחלטה (האם / כמה / איך) בתוספת אזור הלכות ושאלות נפוצות.
 * כרגע רק HOW ממומש במלואו; שאר האזורים מוצגים כ-placeholder עד שיתווסף תוכן.
 */
enum class Topic(
    val title: String,
    val subtitle: String,
    val isImplemented: Boolean
) {
    HOW(
        title = "איך? - שיטת קשירה",
        subtitle = "אחרי שהוחלט להטיל תכלת: לפי איזו שיטה לקשור",
        isImplemented = true
    ),
    WHETHER(
        title = "האם? - להתחיל להטיל תכלת",
        subtitle = "שאלון מיפוי וניפוי לשאלת הבסיס עצמה, עם הפניה למאמרים",
        isImplemented = false
    ),
    HOW_MANY(
        title = "כמה? - כמות החוטים",
        subtitle = "כמה מתוך 8 חוטי הציצית להטיל בתכלת - שאלון קליל ואינטראקטיבי",
        isImplemented = false
    ),
    HALACHA_FAQ(
        title = "הלכות ושאלות נפוצות",
        subtitle = "סוגיות שכדאי לפתוח, והשוואה להלכות ציצית בלי תכלת",
        isImplemented = false
    )
}
