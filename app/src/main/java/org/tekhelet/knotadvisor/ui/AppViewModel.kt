package org.tekhelet.knotadvisor.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.tekhelet.knotadvisor.data.ContentRepository
import org.tekhelet.knotadvisor.data.HistoryStore
import org.tekhelet.knotadvisor.logic.ScoringEngine
import org.tekhelet.knotadvisor.model.*
import java.util.UUID

/**
 * מחזיק את מצב האפליקציה כולו: תשובות השאלון בתהליך, תוצאות מחושבות, ושלב שאלות המשנה.
 * ViewModel יחיד פשוט מספיק לגודל האפליקציה הנוכחי; אם המסכים יתרבו כדאי לפצל.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ContentRepository(application)
    private val historyStore = HistoryStore(application)

    val allMethods: List<KnotMethod> by lazy { repository.loadMethods() }
    val allQuestions: List<Question> by lazy { repository.loadQuestions() }
    val primaryQuestions: List<Question> by lazy { allQuestions.filter { it.stage == QuestionStage.PRIMARY } }

    var answers by mutableStateOf<Map<String, Answer>>(emptyMap())
        private set

    var results by mutableStateOf<List<ScoredMethod>>(emptyList())
        private set

    var showWindCountTieBreaker by mutableStateOf(false)
        private set

    var showVisualTieBreaker by mutableStateOf(false)
        private set

    var consultingFor by mutableStateOf("")

    val history = historyStore.consultations

    fun setAnswer(answer: Answer) {
        answers = answers + (answer.questionId to answer)
    }

    fun answerFor(questionId: String): Answer? = answers[questionId]

    /** מריץ את מנוע הניקוד על סמך כל התשובות שנאספו עד כה, וקובע אילו שאלות משנה נדרשות. */
    fun computeResults() {
        val scored = ScoringEngine.score(allMethods, allQuestions, answers.values.toList())
        results = scored
        showWindCountTieBreaker = ScoringEngine.needsWindCountTieBreaker(scored)
        showVisualTieBreaker = ScoringEngine.needsVisualTieBreaker(scored)
    }

    fun applyVisualChoice(chosenMethodId: String) {
        results = ScoringEngine.applyVisualOverride(results, chosenMethodId)
        showVisualTieBreaker = false
    }

    fun dismissWindCountTieBreaker() {
        showWindCountTieBreaker = false
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

    fun resetQuestionnaire() {
        answers = emptyMap()
        results = emptyList()
        showWindCountTieBreaker = false
        showVisualTieBreaker = false
        consultingFor = ""
    }
}
