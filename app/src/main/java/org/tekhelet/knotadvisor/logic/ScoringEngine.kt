package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*
import kotlin.math.abs

/**
 * מנוע הניקוד. ממיר תשובות שאלון (בעיקר שלב ה-PRIMARY) לרשימת שיטות מדורגת,
 * תוך חיקוי תהליך החשיבה שמופיע בשיחות המקור:
 *  1. סינון קשיח של שיטות שנפסלות מראש (למשל: לא בני-זמננו, אם המשתמש ביקש כך).
 *  2. ניקוד משוקלל לפי חשיבות שהמשתמש נתן לכל ציר, מול "כמה כל שיטה מקיימת" אותו ציר.
 *  3. בונוס הטיה (קהילה/פוסק מועדף/נטייה קבלית/דמיון לקשירה בלבן וכו'), אם רלוונטי.
 *  4. זיהוי מתי נדרשות שאלות משנה (SECONDARY) - הכרעת שוויון או בחירת variant.
 */
object ScoringEngine {

    private const val VISUAL_TIE_BREAK_THRESHOLD = 6.0 // הפרש נק' (מתוך 100) שמתחתיו שווים "צמודים"
    private const val VISUAL_OVERRIDE_BONUS = 12.0

    fun score(
        methods: List<KnotMethod>,
        questions: List<Question>,
        answers: List<Answer>
    ): List<ScoredMethod> {
        val questionById = questions.associateBy { it.id }
        val answerByQuestionId = answers.associateBy { it.questionId }

        val eligible = methods.filter { method -> passesHardFilters(method, questionById, answerByQuestionId) }

        val weightedTerms = buildWeightedTerms(questions, answerByQuestionId)
        val affinityTags = buildAffinityTags(questionById, answerByQuestionId)

        return eligible
            .map { method -> scoreOne(method, weightedTerms, affinityTags) }
            .sortedByDescending { it.score }
    }

    /** תגית עזר פנימית לתרומת ציר בודד לניקוד. */
    private data class WeightedTerm(val axis: Axis, val importance01: Double)

    private fun buildWeightedTerms(
        questions: List<Question>,
        answerByQuestionId: Map<String, Answer>
    ): List<WeightedTerm> = questions
        .filter { it.stage == QuestionStage.PRIMARY && it.type == QuestionType.SLIDER && it.axis != null }
        .mapNotNull { q ->
            val value = answerByQuestionId[q.id]?.sliderValue ?: return@mapNotNull null
            WeightedTerm(q.axis!!, value / 10.0)
        }

    /**
     * שאלת ה"הטיה" (q_affinity_bias) היא MULTI_CHOICE - המשתמש יכול לבחור כמה סיבות
     * הטיה בבת אחת (מנהג לפי הגר"א, זיקה לחסידות, קבלה, דמיון לקשירה בלבן, פוסק מועדף וכו').
     */
    private fun buildAffinityTags(
        questionById: Map<String, Question>,
        answerByQuestionId: Map<String, Answer>
    ): List<String> {
        val question = questionById["q_affinity_bias"] ?: return emptyList()
        val answer = answerByQuestionId["q_affinity_bias"] ?: return emptyList()
        return answer.selectedOptionIds.mapNotNull { selectedId ->
            question.options.find { it.id == selectedId }?.matchesAffinityTag
        }
    }

    private fun passesHardFilters(
        method: KnotMethod,
        questionById: Map<String, Question>,
        answerByQuestionId: Map<String, Answer>
    ): Boolean {
        val contemporaryQuestion = questionById["q_contemporary_ok"]
        val contemporaryAnswer = answerByQuestionId["q_contemporary_ok"]
        if (contemporaryQuestion != null && contemporaryAnswer != null) {
            val selectedId = contemporaryAnswer.selectedOptionIds.firstOrNull()
            val option = contemporaryQuestion.options.find { it.id == selectedId }
            if (option?.hardFilter == true && method.era == Era.CONTEMPORARY) {
                return false
            }
        }
        return true
    }

    private fun scoreOne(
        method: KnotMethod,
        weightedTerms: List<WeightedTerm>,
        affinityTags: List<String>
    ): ScoredMethod {
        val axisBreakdown = mutableMapOf<Axis, Double>()
        var numerator = 0.0
        var denominator = 0.0

        for (term in weightedTerms) {
            val axisValue = (method.axisScores[term.axis] ?: 5).toDouble() // 5 = ברירת מחדל ניטרלית
            val contribution = term.importance01 * axisValue
            numerator += contribution
            denominator += term.importance01 * 10.0
            axisBreakdown[term.axis] = (axisBreakdown[term.axis] ?: 0.0) + contribution
        }

        // בונוס הטיה: יחס התגיות שהמשתמש בחר שהשיטה הזו אכן מקיימת (0..1 * משקל 10),
        // ולא רק "הכל או כלום" - כך בחירה בכמה סיבות הטיה בבת אחת עדיין מבחינה בין שיטות.
        if (affinityTags.isNotEmpty()) {
            val matchedCount = affinityTags.count { method.affinityTags.contains(it) }
            numerator += (matchedCount.toDouble() / affinityTags.size) * 10.0
            denominator += 10.0
        }

        val score = if (denominator > 0) (numerator / denominator) * 100.0 else 50.0

        return ScoredMethod(
            method = method,
            score = score,
            axisBreakdown = axisBreakdown,
            explanation = buildExplanation(method, axisBreakdown, affinityTags)
        )
    }

    private fun buildExplanation(
        method: KnotMethod,
        axisBreakdown: Map<Axis, Double>,
        affinityTags: List<String>
    ): String {
        val topAxes = axisBreakdown.entries
            .sortedByDescending { it.value }
            .take(2)
            .map { axisLabel(it.key) }

        val parts = mutableListOf<String>()
        if (topAxes.isNotEmpty()) {
            parts.add("השיטה הזו מתאימה לך בעיקר לפי: ${topAxes.joinToString(" ו")}.")
        }
        val matchedTags = affinityTags.filter { method.affinityTags.contains(it) }
        if (matchedTags.isNotEmpty()) {
            parts.add("היא גם תואמת את ההטיה שציינת (${matchedTags.joinToString(", ")}).")
        }
        method.editorialNote?.let { parts.add(it) }
        return parts.joinToString(" ")
    }

    private fun axisLabel(axis: Axis): String = when (axis) {
        Axis.AFFORDABILITY -> "עלות נמוכה"
        Axis.BEAUTY -> "יופי"
        Axis.RISHONIM_BASIS -> "ביסוס בראשונים"
        Axis.GENERAL_SOURCE_BASIS -> "ביסוס מקורות כללי"
        Axis.COMMONNESS -> "נפיצות"
        Axis.UNIQUENESS -> "ייחודיות"
        Axis.CONSENSUS_COVERAGE -> "כיסוי כמה שיטות"
        Axis.NAMED_ATTRIBUTION -> "מקור שמי ברור"
    }

    /** האם יש להציג את שאלת המשנה על מספר הכריכות (7/13) - כשלמועמדת המובילה יש כמה variants. */
    fun needsWindCountTieBreaker(topCandidates: List<ScoredMethod>, topN: Int = 3): Boolean =
        topCandidates.take(topN).any { it.method.variants.size > 1 }

    /** האם יש להציג שאלת הכרעה חזותית - כששני המועמדים המובילים צמודים בניקוד. */
    fun needsVisualTieBreaker(topCandidates: List<ScoredMethod>): Boolean {
        if (topCandidates.size < 2) return false
        val diff = abs(topCandidates[0].score - topCandidates[1].score)
        return diff < VISUAL_TIE_BREAK_THRESHOLD
    }

    /**
     * מפעיל את ההכרעה החזותית: המשתמש בחר איזו שיטה (מבין המובילות) הכי יפה בעיניו,
     * וזה "דורס" את ציון היופי המחושב - בדיוק כפי שקרה בפועל באחת השיחות שהובילו לאפליקציה.
     */
    fun applyVisualOverride(
        scored: List<ScoredMethod>,
        chosenMethodId: String
    ): List<ScoredMethod> = scored
        .map { sm ->
            if (sm.method.id == chosenMethodId) {
                sm.copy(
                    score = sm.score + VISUAL_OVERRIDE_BONUS,
                    explanation = sm.explanation + " בנוסף, זו השיטה שבחרת כיפה ביותר בעינך מבין המועמדות."
                )
            } else sm
        }
        .sortedByDescending { it.score }
}
