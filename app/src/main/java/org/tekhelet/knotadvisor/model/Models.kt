package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * Every numeric axis a knot method is scored on (0-10, higher = "more of this quality").
 * Primary questions map 1:1 onto these axes; the mapping direction is always
 * "higher user importance -> prefer methods with a higher value on this axis".
 */
@Serializable
enum class Axis(val label: String) {
    AFFORDABILITY("עלות נמוכה"),
    BEAUTY("יופי"),
    RISHONIM_BASIS("ביסוס בראשונים"),
    GENERAL_SOURCE_BASIS("ביסוס מקורות כללי"),
    COMMONNESS("נפוצות"),
    UNIQUENESS("ייחודיות"),
    CONSENSUS_COVERAGE("כיסוי כמה שיטות"),
    NAMED_ATTRIBUTION("מקור שמי ברור"),

    /**
     * כמה השיטה "מסבירה את עצמה" - עד כמה יש לה רציונל מפורש שאפשר לעקוב אחריו,
     * בשונה משיטה שקיימת כמסורת אבל לא ברור איך הגיעו אליה.
     * מזין את שאלה 7 (בהירות אישית): מי שחשוב לו להבין בעצמו למה זו קשירה הגיונית
     * יקבל תיעדוף לשיטות מוסברות, וגם יראה יותר אפשרויות (ראו ScoringEngine).
     */
    EXPLAINABILITY("מידת ההסבר העצמי")
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
    // ההרכב ההלכתי של השיטה - חמשת הדיונים הנפרדים. ראו Composition.kt להסבר מלא.
    // זו השכבה העובדתית (ממה השיטה מורכבת), בשונה מ-axisScores שהיא שכבת ההעדפות.
    val composition: KnotComposition = KnotComposition(),
    val axisScores: Map<Axis, Int>,   // 0-10 לכל ציר, ראו Axis
    /** וריאציות מקובלות על השיטה - ראו Variant.kt. */
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
