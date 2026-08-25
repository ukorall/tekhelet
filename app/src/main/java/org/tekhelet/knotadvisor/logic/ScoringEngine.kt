package org.tekhelet.knotadvisor.logic

import org.tekhelet.knotadvisor.model.*
import kotlin.math.abs

/**
 * מנוע הניקוד. ממיר תשובות שאלון לרשימת שיטות מדורגת.
 *
 *  1. **נירמול** של התשובות - ראו normalize() להסבר, זה החלק הכי חשוב כאן.
 *  2. סכום משוקלל של כל ציר מול "כמה כל שיטה מקיימת" אותו.
 *  3. בונוס הטיה (קהילה/פוסק מועדף/נטייה קבלית וכו').
 *  4. שאלת "בהירות אישית" משפיעה בשתי דרכים - ראו clarityEffect().
 *
 * שים לב: אין כאן סינון קשיח שמוציא שיטות מהרשימה. גם בחירת מספר חוטים אינה
 * פוסלת שום שיטת קשירה - כל הרכב אפשרי, חלקם פשוט פחות מסתדרים עם עצמם,
 * וזה מטופל ב-CompositionCoherence ולא כאן.
 */
object ScoringEngine {

    private const val VISUAL_TIE_BREAK_THRESHOLD = 6.0
    private const val VISUAL_OVERRIDE_BONUS = 12.0

    /** כמה תוצאות להציג כברירת מחדל, וכמה כשהמשתמש רוצה להבין בעצמו. */
    private const val DEFAULT_RESULT_COUNT = 5
    private const val CURIOUS_RESULT_COUNT = 11

    data class Outcome(
        val ranked: List<ScoredMethod>,
        /** כמה תוצאות כדאי להציג בפועל - גדל כשהמשתמש רוצה להבין בעצמו. */
        val suggestedVisibleCount: Int
    )

    fun score(
        methods: List<KnotMethod>,
        questions: List<Question>,
        answers: List<Answer>
    ): List<ScoredMethod> = evaluate(methods, questions, answers).ranked

    /**
     * מחיל את ההעדפה החזותית המפורשת. ההתאמה נמדדת מול ההרכב בפועל (איך השיטה
     * באמת נראית), ולא מול ציון "יופי" שנקבע מראש.
     */
    private fun applyLookPreference(
        sm: ScoredMethod,
        look: LookPreference?,
        knots: KnotLookPreference?,
        beautyWeight: Double
    ): ScoredMethod {
        if (look == null && knots == null) return sm
        var delta = 0.0
        val parts = mutableListOf<String>()

        look?.let {
            val matches = when (it) {
                LookPreference.ALTERNATING_WINDS ->
                    sm.method.composition.windingColor == WindingColor.ALTERNATING_WINDS
                LookPreference.ALTERNATING_CHULYOT ->
                    sm.method.composition.windingColor == WindingColor.ALTERNATING_CHULYOT
                LookPreference.ALL_TEKHELET ->
                    sm.method.composition.windingColor == WindingColor.MOSTLY_TEKHELET_SINGLE_WIND ||
                        sm.method.composition.windingColor == WindingColor.MOSTLY_TEKHELET_FULL_CHULYA
            }
            if (matches) {
                delta += LOOK_PREFERENCE_WEIGHT * beautyWeight * 6.0
                parts += "היא נראית בדיוק כמו שאמרת שיפה בעיניך"
            } else {
                delta -= LOOK_PREFERENCE_WEIGHT * beautyWeight * 3.0
            }
        }

        knots?.let {
            val visibleKnots = when (sm.method.composition.knotScheme) {
                KnotScheme.DOUBLE_EVERY_CHULYA, KnotScheme.FIVE_GROUPS_CHINUCH,
                KnotScheme.FIVE_GROUPS_GRA, KnotScheme.FIVE_GROUPS_TOSAFOT,
                KnotScheme.FIVE_WINDS_7_8_11_13 -> true
                else -> false
            }
            when (it) {
                KnotLookPreference.NICER ->
                    if (visibleKnots) { delta += beautyWeight * 3.0; parts += "יש בה קשרים כפולים גלויים" }
                KnotLookPreference.LESS_NICE ->
                    if (!visibleKnots) { delta += beautyWeight * 3.0; parts += "היא כמעט בלי קשרים גלויים" }
                KnotLookPreference.NEUTRAL -> Unit
            }
        }

        val explanation = if (parts.isEmpty()) sm.explanation
        else sm.explanation + " " + parts.joinToString(", ") + "."
        return sm.copy(score = (sm.score + delta).coerceIn(0.0, 100.0), explanation = explanation)
    }

    /**
     * כמה משקל נותנים להעדפה החזותית המפורשת, ביחס לציון היופי הכללי של שיטה.
     * גדול מ-1 בכוונה: "מה יפה בעיניך" הוא מידע אישי וישיר, בעוד שציון היופי
     * של שיטה הוא הערכה כללית שלי - ולכן ההעדפה שלך גוברת עליה.
     */
    private const val LOOK_PREFERENCE_WEIGHT = 2.0

    fun evaluate(
        methods: List<KnotMethod>,
        questions: List<Question>,
        answers: List<Answer>,
        lookPreference: LookPreference? = null,
        knotPreference: KnotLookPreference? = null
    ): Outcome {
        val questionById = questions.associateBy { it.id }
        val answerByQuestionId = answers.associateBy { it.questionId }

        val clarity = answerByQuestionId["q_personal_clarity"]?.sliderValue
        val weights = buildNormalizedWeights(questions, answerByQuestionId, clarity)
        val affinityTags = buildAffinityTags(questionById, answerByQuestionId)

        val beautyWeight = weights[Axis.BEAUTY] ?: 1.0
        val ranked = methods
            .map { scoreOne(it, weights, affinityTags) }
            .map { applyLookPreference(it, lookPreference, knotPreference, beautyWeight) }
            .sortedByDescending { it.score }

        return Outcome(
            ranked = ranked,
            suggestedVisibleCount = if ((clarity ?: 5) >= 7) CURIOUS_RESULT_COUNT else DEFAULT_RESULT_COUNT
        )
    }

    /**
     * **נירמול התשובות.** אנשים שונים משתמשים בסולם 1-10 אחרת לגמרי: אחד מסמן 9
     * לכל דבר שחשוב לו ו-7 לדברים פחות חשובים, ואחר מסמן 5 ו-2 בדיוק לאותה
     * העדפה יחסית. בלי נירמול, הראשון פשוט "צועק" יותר והציונים שלו מטים את כל
     * החישוב, למרות ששניהם התכוונו לאותו דבר.
     *
     * לכן המשקל של כל ציר נמדד **ביחס לממוצע של אותו משתמש**: מי שנתן לציר ציון
     * שווה לממוצע שלו מקבל משקל 1.0, מי שנתן יותר מקבל מעל 1.0, ומי שנתן פחות -
     * מתחת. כך מה שנשמר הוא סדר העדיפויות היחסי, שזה מה שבאמת התכוונו אליו.
     */
    private fun buildNormalizedWeights(
        questions: List<Question>,
        answerByQuestionId: Map<String, Answer>,
        clarity: Int?
    ): Map<Axis, Double> {
        val raw = questions
            .filter { it.stage == QuestionStage.PRIMARY && it.type == QuestionType.SLIDER && it.axis != null }
            .mapNotNull { q ->
                val v = answerByQuestionId[q.id]?.sliderValue ?: return@mapNotNull null
                q.axis!! to v.toDouble()
            }
            .toMap()

        if (raw.isEmpty()) return emptyMap()

        val mean = raw.values.average().coerceAtLeast(0.5)
        val normalized = raw.mapValues { (_, v) -> (v / mean).coerceIn(0.15, 2.5) }.toMutableMap()

        // "בהירות אישית" אינה ציר שמנקד שיטה, אלא היא מגבירה את משקל ההסבר העצמי.
        clarity?.let { c ->
            if (c >= 6) normalized[Axis.EXPLAINABILITY] = (c / mean).coerceIn(0.15, 2.5)
        }
        return normalized
    }

    private fun buildAffinityTags(
        questionById: Map<String, Question>,
        answerByQuestionId: Map<String, Answer>
    ): List<String> {
        val question = questionById["q_affinity_bias"] ?: return emptyList()
        val answer = answerByQuestionId["q_affinity_bias"] ?: return emptyList()
        return answer.selectedOptionIds.mapNotNull { id ->
            question.options.find { it.id == id }?.matchesAffinityTag
        }
    }

    private fun scoreOne(
        method: KnotMethod,
        weights: Map<Axis, Double>,
        affinityTags: List<String>
    ): ScoredMethod {
        val axisBreakdown = mutableMapOf<Axis, Double>()
        var numerator = 0.0
        var denominator = 0.0

        for ((axis, weight) in weights) {
            val axisValue = (method.axisScores[axis] ?: 5).toDouble()
            val contribution = weight * axisValue
            numerator += contribution
            denominator += weight * 10.0
            axisBreakdown[axis] = contribution
        }

        if (affinityTags.isNotEmpty()) {
            val matched = affinityTags.count { method.affinityTags.contains(it) }
            numerator += (matched.toDouble() / affinityTags.size) * 10.0
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
            .map { it.key.label }

        val parts = mutableListOf<String>()
        if (topAxes.isNotEmpty()) {
            parts += "מתאימה לך בעיקר לפי ${topAxes.joinToString(" ו")}."
        }
        val matched = affinityTags.filter { method.affinityTags.contains(it) }
        if (matched.isNotEmpty()) {
            parts += "היא גם תואמת את ההטיה שציינת."
        }
        method.editorialNote?.let { parts += it }
        return parts.joinToString(" ")
    }

    fun needsWindCountTieBreaker(topCandidates: List<ScoredMethod>, topN: Int = 3): Boolean =
        topCandidates.take(topN).any { it.method.variants.size > 1 }

    fun needsVisualTieBreaker(topCandidates: List<ScoredMethod>): Boolean {
        if (topCandidates.size < 2) return false
        return abs(topCandidates[0].score - topCandidates[1].score) < VISUAL_TIE_BREAK_THRESHOLD
    }

    fun applyVisualOverride(
        scored: List<ScoredMethod>,
        chosenMethodId: String
    ): List<ScoredMethod> = scored
        .map { sm ->
            if (sm.method.id == chosenMethodId) {
                sm.copy(
                    score = sm.score + VISUAL_OVERRIDE_BONUS,
                    explanation = sm.explanation + " בנוסף, זו השיטה שבחרת כיפה ביותר בעיניך."
                )
            } else sm
        }
        .sortedByDescending { it.score }
}
