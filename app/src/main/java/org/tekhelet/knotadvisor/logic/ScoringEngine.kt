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

    /** מעל איזה יחס-לממוצע נחשב שהמשתמש "רוצה להבין בעצמו". */
    private const val CURIOUS_RATIO = 1.1

    data class Outcome(
        val ranked: List<ScoredMethod>,
        /** כמה תוצאות כדאי להציג בפועל - גדל כשהמשתמש רוצה להבין בעצמו. */
        val suggestedVisibleCount: Int,
        /**
         * וריאציות מומלצות לשיטות המובילות - "אתה מתאים לרמב"ם, אבל אמרת שחשוב
         * לך שיהיו קשרים, אז תעשה רמב"ם עם קשרים". ראו suggestVariants().
         */
        val variantSuggestions: List<VariantSuggestion> = emptyList()
    )

    data class VariantSuggestion(
        val baseMethod: KnotMethod,
        val variant: MethodVariant,
        val reason: String
    )

    /**
     * מוצא וריאציות שעונות על דרישה מפורשת של המשתמש טוב יותר משיטת הבסיס.
     *
     * זה הפער שהמשוב הצביע עליו: הרבה ממה שאנשים באמת קושרים הוא "שיטה מוכרת
     * ועוד משהו" - רמב"ם עם קשרים, רמב"ם בחוט ראב"ד, רב נגן עם קשרי גר"א.
     * בונה ההרכב האישי מאפשר את זה, אבל הוא מקום שצריך להגיע אליו ביוזמה;
     * כאן ההצעה מגיעה מעצמה, בתוך התוצאות.
     */
    fun suggestVariants(
        ranked: List<ScoredMethod>,
        constraints: Set<Constraint>,
        topN: Int = 3
    ): List<VariantSuggestion> {
        if (constraints.isEmpty()) return emptyList()
        return ranked.take(topN).flatMap { sm ->
            sm.method.variants.mapNotNull { v ->
                val applied = v.applyTo(sm.method.composition)
                val baseSatisfies = constraints.count { it.satisfiedBy(sm.method.composition) }
                val variantSatisfies = constraints.count { it.satisfiedBy(applied) }
                if (variantSatisfies > baseSatisfies) {
                    val gained = constraints.filter {
                        it.satisfiedBy(applied) && !it.satisfiedBy(sm.method.composition)
                    }
                    VariantSuggestion(
                        baseMethod = sm.method,
                        variant = v,
                        reason = "מתאים לך ${sm.method.name}, אבל אמרת ש" +
                            gained.joinToString(", ") { it.shortLabel } +
                            ". ${v.name} עונה על זה."
                    )
                } else null
            }
        }.sortedByDescending { it.variant.commonness }
    }

    fun score(
        methods: List<KnotMethod>,
        questions: List<Question>,
        answers: List<Answer>
    ): List<ScoredMethod> = evaluate(methods, questions, answers).ranked

    /**
     * דרישות מפורשות של המשתמש ("חשוב לי שיהיו קשרים כפולים"). בשונה מסליידר,
     * זו אמירה חדה - ולכן היא משפיעה חזק, אבל עדיין **לא פוסלת** שיטה שלא
     * עונה עליה. היא רק דוחפת אותה למטה, ומעלה את מי שכן עונה.
     */
    private fun applyConstraints(sm: ScoredMethod, constraints: Set<Constraint>): ScoredMethod {
        if (constraints.isEmpty()) return sm
        val met = constraints.count { it.satisfiedBy(sm.method.composition) }
        val delta = (met.toDouble() / constraints.size - 0.5) * 24.0
        return sm.copy(score = (sm.score + delta).coerceIn(0.0, 100.0))
    }

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
                delta += LOOK_PREFERENCE_WEIGHT * beautyWeight * 5.0
                parts += "היא נראית בדיוק כמו שאמרת שיפה בעיניך"
            } else {
                delta -= LOOK_PREFERENCE_WEIGHT * beautyWeight * 2.0
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
     * גדול מ-1 כי "מה יפה בעיניך" הוא מידע אישי וישיר, בעוד שציון היופי של
     * שיטה הוא הערכה כללית שלי. אבל לא גדול מדי: המשוב הצביע על כך שהמשקל
     * הקודם גרם ליופי להשתלט על התוצאה. הוא גם מוכפל במשקל שהמשתמש נתן ליופי,
     * כך שמי שהיופי לא מרכזי אצלו כמעט לא מושפע.
     */
    private const val LOOK_PREFERENCE_WEIGHT = 1.3

    fun evaluate(
        methods: List<KnotMethod>,
        questions: List<Question>,
        answers: List<Answer>,
        lookPreference: LookPreference? = null,
        knotPreference: KnotLookPreference? = null,
        constraints: Set<Constraint> = emptySet()
    ): Outcome {
        val questionById = questions.associateBy { it.id }
        val answerByQuestionId = answers.associateBy { it.questionId }

        val clarity = answerByQuestionId["q_personal_clarity"]?.sliderValue
        val normalized = buildNormalizedWeights(questions, answerByQuestionId, clarity)
        val weights = normalized.weights
        val affinityTags = buildAffinityTags(questionById, answerByQuestionId)

        val beautyWeight = weights[Axis.BEAUTY] ?: 1.0
        val ranked = methods
            .map { scoreOne(it, weights, affinityTags) }
            .map { applyLookPreference(it, lookPreference, knotPreference, beautyWeight) }
            .map { applyConstraints(it, constraints) }
            .sortedByDescending { it.score }

        return Outcome(
            variantSuggestions = suggestVariants(ranked, constraints),
            ranked = ranked,
            // גם כאן הסף יחסי ולא מוחלט, מאותו טעם - ראו buildNormalizedWeights
            suggestedVisibleCount =
                if (normalized.clarityRatio >= CURIOUS_RATIO) CURIOUS_RESULT_COUNT
                else DEFAULT_RESULT_COUNT
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
    /** משקלים מנורמלים, יחד עם היחס-לממוצע של שאלת הבהירות. */
    private data class Normalized(val weights: Map<Axis, Double>, val clarityRatio: Double)

    private fun buildNormalizedWeights(
        questions: List<Question>,
        answerByQuestionId: Map<String, Answer>,
        clarity: Int?
    ): Normalized {
        val raw = questions
            .filter { it.stage == QuestionStage.PRIMARY && it.type == QuestionType.SLIDER && it.axis != null }
            .mapNotNull { q ->
                val v = answerByQuestionId[q.id]?.sliderValue ?: return@mapNotNull null
                q.axis!! to v.toDouble()
            }
            .toMap()

        if (raw.isEmpty()) return Normalized(emptyMap(), 1.0)

        val mean = raw.values.average().coerceAtLeast(0.5)
        val normalized = raw.mapValues { (_, v) -> (v / mean).coerceIn(0.15, 2.5) }.toMutableMap()

        // "בהירות אישית" אינה ציר שמנקד שיטה, אלא היא מגבירה את משקל ההסבר העצמי.
        //
        // חשוב שגם כאן ההשוואה תהיה **יחסית לממוצע של אותו משתמש** ולא סף מוחלט.
        // קודם היה כאן `if (c >= 6)`, וזה סתר את כל הרעיון של הנירמול: מי שמשתמש
        // בחלק התחתון של הסולם (נניח 5 לדברים הכי חשובים לו) לא קיבל בכלל את
        // משקל ההסבר, גם אם הבהירות היתה העדיפות הראשונה שלו. בדיקת היחידה
        // "normalisation makes equivalent answers rank the same" תפסה בדיוק את זה.
        val clarityRatio = clarity?.let { (it / mean).coerceIn(0.15, 2.5) } ?: 1.0
        if (clarity != null) normalized[Axis.EXPLAINABILITY] = clarityRatio

        return Normalized(normalized, clarityRatio)
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
