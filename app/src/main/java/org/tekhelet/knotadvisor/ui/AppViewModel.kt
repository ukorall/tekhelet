package org.tekhelet.knotadvisor.ui

import android.app.Application
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.tekhelet.knotadvisor.BuildConfig
import org.tekhelet.knotadvisor.data.ContentRepository
import org.tekhelet.knotadvisor.data.FeedbackStore
import org.tekhelet.knotadvisor.data.HistoryStore
import org.tekhelet.knotadvisor.data.SessionStore
import org.tekhelet.knotadvisor.logic.ScoringEngine
import org.tekhelet.knotadvisor.model.*
import java.util.UUID

/**
 * מצב האפליקציה. כולל את השאלון, המסע, ההרכב האישי שנבנה ידנית, ושם המתייעץ -
 * כי כל האפליקציה עובדת במצב יועץ, גם כשמתייעצים עם עצמך.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContentRepository(application)
    private val historyStore = HistoryStore(application)
    private val sessionStore = SessionStore(application)
    private val feedbackStore = FeedbackStore(application)

    val allMethods: List<KnotMethod> by lazy { repository.loadMethods() }
    val allQuestions: List<Question> by lazy { repository.loadQuestions() }
    val primaryQuestions: List<Question> by lazy { allQuestions.filter { it.stage == QuestionStage.PRIMARY } }

    var answers by mutableStateOf<Map<String, Answer>>(emptyMap()); private set
    var results by mutableStateOf<List<ScoredMethod>>(emptyList()); private set
    var visibleResultCount by mutableStateOf(5); private set
    var showWindCountTieBreaker by mutableStateOf(false); private set
    var showVisualTieBreaker by mutableStateOf(false); private set

    var consultingFor by mutableStateOf(""); private set
    var journey by mutableStateOf(JourneyState()); private set

    /** ההרכב שהמשתמש בונה ידנית בבונה ההרכב האישי. */
    var customComposition by mutableStateOf(KnotComposition()); private set

    val history = historyStore.consultations

    init {
        viewModelScope.launch {
            journey = sessionStore.journey.first()
            consultingFor = sessionStore.consultingFor.first()
        }
    }

    // --- שאלון ---
    fun setAnswer(answer: Answer) { answers = answers + (answer.questionId to answer) }
    fun answerFor(questionId: String): Answer? = answers[questionId]

    fun computeResults() {
        val outcome = ScoringEngine.evaluate(allMethods, allQuestions, answers.values.toList())
        results = outcome.ranked
        visibleResultCount = outcome.suggestedVisibleCount
        showWindCountTieBreaker = ScoringEngine.needsWindCountTieBreaker(outcome.ranked)
        showVisualTieBreaker = ScoringEngine.needsVisualTieBreaker(outcome.ranked)
    }

    fun applyVisualChoice(chosenMethodId: String) {
        results = ScoringEngine.applyVisualOverride(results, chosenMethodId)
        showVisualTieBreaker = false
    }

    fun dismissWindCountTieBreaker() { showWindCountTieBreaker = false }

    fun resetQuestionnaire() {
        answers = emptyMap(); results = emptyList()
        showWindCountTieBreaker = false; showVisualTieBreaker = false
    }

    // --- מתייעץ ---
    fun updateConsultingFor(name: String) {
        consultingFor = name
        viewModelScope.launch { sessionStore.saveConsultingFor(name) }
    }

    fun saveFinalChoice(methodId: String) {
        viewModelScope.launch {
            historyStore.save(
                SavedConsultation(
                    id = UUID.randomUUID().toString(),
                    consultingFor = consultingFor.ifBlank { "אני" },
                    createdAtEpochMillis = System.currentTimeMillis(),
                    answers = answers.values.toList(),
                    topResultMethodIds = results.take(3).map { it.method.id },
                    finalChoiceMethodId = methodId
                )
            )
        }
    }

    fun deleteConsultation(id: String) {
        viewModelScope.launch { historyStore.delete(id) }
    }

    // --- מסע ---
    fun startJourney() = updateJourney(JourneyState(active = true, currentStation = JourneyStation.WHETHER))

    fun goToStation(station: JourneyStation) =
        updateJourney(journey.copy(active = true, currentStation = station))

    fun completeCurrentStation() {
        val done = journey.completed + journey.currentStation
        val next = journey.currentStation.next()
        updateJourney(journey.copy(completed = done, currentStation = next ?: journey.currentStation))
    }

    fun pauseJourney() = updateJourney(journey.copy(active = false))
    fun resetJourney() = updateJourney(JourneyState())

    private fun updateJourney(state: JourneyState) {
        journey = state
        viewModelScope.launch { sessionStore.saveJourney(state) }
    }

    // --- בונה ההרכב האישי ---
    fun updateComposition(transform: (KnotComposition) -> KnotComposition) {
        customComposition = transform(customComposition)
    }

    fun loadCompositionFrom(method: KnotMethod) { customComposition = method.composition }

    /** השיטה המוכרת הקרובה ביותר להרכב שנבנה, וכמה היא קרובה (0-5). */
    fun closestMethod(c: KnotComposition = customComposition): Pair<KnotMethod, Int>? =
        allMethods
            .map { it to compositionOverlap(it.composition, c) }
            .maxByOrNull { it.second }
            ?.takeIf { it.second > 0 }

    private fun compositionOverlap(a: KnotComposition, b: KnotComposition): Int {
        var n = 0
        if (a.threadCount != null && a.threadCount == b.threadCount) n++
        if (a.windingColor != null && a.windingColor == b.windingColor) n++
        if (a.chulyotCount != null && a.chulyotCount == b.chulyotCount) n++
        if (a.chulyaForm != null && a.chulyaForm == b.chulyaForm) n++
        if (a.knotScheme != null && a.knotScheme == b.knotScheme) n++
        return n
    }

    // --- פידבק ---
    fun submitFeedback(
        kind: FeedbackKind,
        text: String,
        rating: Int?,
        screen: String,
        methodId: String?,
        onSaved: (java.io.File) -> Unit
    ) {
        val fb = Feedback(
            id = UUID.randomUUID().toString(),
            createdAtEpochMillis = System.currentTimeMillis(),
            fromName = consultingFor,
            kind = kind,
            text = text,
            rating = rating,
            appVersion = BuildConfig.VERSION_NAME,
            screen = screen,
            methodId = methodId,
            androidRelease = Build.VERSION.RELEASE ?: "",
            deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"
        )
        onSaved(feedbackStore.save(fb))
    }

    fun feedbackShareIntent(file: java.io.File) = feedbackStore.shareIntent(file)
}
