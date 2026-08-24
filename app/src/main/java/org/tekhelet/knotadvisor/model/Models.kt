package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * Every numeric axis a knot method is scored on (0-10, higher = "more of this quality").
 * Primary questions map 1:1 onto these axes; the mapping direction is always
 * "higher user importance -> prefer methods with a higher value on this axis".
 */
@Serializable
enum class Axis {
    AFFORDABILITY,        // זול
    BEAUTY,                // יופי (ברירת מחדל עריכתית - ניתנת לעקיפה ע"י בחירה חזותית של המשתמש)
    RISHONIM_BASIS,        // ביסוס בראשונים
    GENERAL_SOURCE_BASIS,  // ביסוס מקורות כללי (כולל גאונים, אחרונים, מחקר)
    COMMONNESS,             // נפוצות / "רגילות"
    UNIQUENESS,             // ייחודיות מעניינת
    CONSENSUS_COVERAGE,    // יוצא ידי חובה לפי כמה שיטות
    NAMED_ATTRIBUTION       // "יש לזה גב" - מקור שמי ברור, לא תערובת מאוחרת
}

@Serializable
enum class Era { GEONIM, RISHONIM, ACHRONIM, CONTEMPORARY }

@Serializable
data class SourceRef(
    val title: String,           // למשל: "ראב"ד, הלכות ציצית פ"א"
    val author: String,          // למשל: "הראב"ד"
    val excerpt: String? = null, // ציטוט קצר / תמצית (ימולא בהמשך)
    val link: String? = null,    // קישור למקור דיגיטלי אם קיים (למשל ספריא)
    val note: String? = null     // הערת עריכה, למשל "לוודא ציטוט מדויק"
)

@Serializable
data class MethodVariant(
    val id: String,              // למשל "raavad-13"
    val name: String,            // למשל "ראב"ד - 13 כריכות"
    val windCount: Int? = null,  // מספר כריכות בפתיל התחתון, אם רלוונטי להבחנה
    val note: String? = null
)

@Serializable
data class KnotMethod(
    val id: String,
    val name: String,
    val shortSummary: String,
    val fullDescription: String,
    val editorialNote: String? = null, // "שיקולים בשלוף" - הערכה אישית חופשית, כפי שמופיע בשיחות
    val era: Era,
    // תגיות זיקה: קהילה/חסידות, אך גם פוסק מועדף, נטייה קבלית, דמיון לקשירה בלבן וכו' -
    // כל מה שנבדק מול שאלת ה"הטיה" (q_affinity_bias). למשל ["chabad"], ["general"], ["posek-rambam"]
    val affinityTags: List<String> = emptyList(),
    // קישור טיוטתי לאזור "כמה" העתידי: איזו מ-3 שיטות מספר החוטים (1/8, 2/8, 4/8) השיטה
    // הזו הולכת בעקבותיה, אם ידוע. null = לא ידוע/לא רלוונטי. ראו DESIGN.md סעיף 2.
    val threadCountSchool: String? = null,
    val axisScores: Map<Axis, Int>,   // 0-10 לכל ציר, ראו Axis
    val variants: List<MethodVariant> = emptyList(),
    val sources: List<SourceRef> = emptyList(),
    val disqualifyingTags: List<String> = emptyList() // תגיות לסינון קשיח, למשל "invented-mix", "tosafot-based"
)

@Serializable
enum class QuestionType { SLIDER, SINGLE_CHOICE, MULTI_CHOICE, BOOLEAN }

@Serializable
enum class QuestionStage { PRIMARY, SECONDARY }

@Serializable
data class QuestionOption(
    val id: String,
    val label: String,
    // תגית זיקה/עידן שהאופציה הזו "מתאימה" אליה, לצורך בונוס/סינון (ראו affinityTags למעלה)
    val matchesAffinityTag: String? = null,
    val matchesEra: Era? = null,
    // אם true, בחירה באופציה זו מהווה סינון קשיח (לא רק העדפה)
    val hardFilter: Boolean = false
)

@Serializable
data class Question(
    val id: String,
    val text: String,
    val helpText: String? = null,
    val type: QuestionType,
    val stage: QuestionStage,
    // לשאלות SLIDER: הציר שהתשובה שוקלת (1-10, 10 = "חשוב לי מאוד")
    val axis: Axis? = null,
    // לשאלות בחירה
    val options: List<QuestionOption> = emptyList(),
    // מתי לשאול שאלת משנה (stage=SECONDARY) - טקסט תיאורי לתיעוד, הלוגיקה בפועל ב-ScoringEngine
    val triggerCondition: String? = null
)

/** תשובת משתמש בודדת לשאלה. */
@Serializable
data class Answer(
    val questionId: String,
    val sliderValue: Int? = null,        // 1-10 עבור SLIDER
    val selectedOptionIds: List<String> = emptyList(), // עבור SINGLE_CHOICE/MULTI_CHOICE
    val booleanValue: Boolean? = null    // עבור BOOLEAN
)

@Serializable
data class ScoredMethod(
    val method: KnotMethod,
    val score: Double,            // 0-100, לתצוגה כ"אחוז התאמה"
    val axisBreakdown: Map<Axis, Double>, // תרומת כל ציר לציון הסופי, לצורך הסבר "למה"
    val explanation: String       // טקסט הסבר קצר שנוצר אוטומטית, בסגנון הניתוחים בשיחות
)

/** ייעוץ שמור - כדי לתמוך בשימוש כ"יועץ" עבור כמה חברים, כמו בשיחות המצורפות. */
@Serializable
data class SavedConsultation(
    val id: String,
    val consultingFor: String,   // שם החבר/ה שעבורו נערך הייעוץ (או "אני")
    val createdAtEpochMillis: Long,
    val answers: List<Answer>,
    val topResultMethodIds: List<String>,
    val finalChoiceMethodId: String? = null // אם נבחרה שיטה סופית, כמו "אני אלך על ראב"ד"
)
