package org.tekhelet.knotadvisor.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.model.Question

/**
 * טוען את תוכן הבסיס (שיטות קשירה ושאלות) מקבצי ה-assets המצורפים לאפליקציה.
 *
 * הבחירה לשמור את התוכן כ-JSON בתוך assets, ולא ב-Room או שרת מרוחק, היא מכוונת:
 * בשלב הזה התוכן (שיטות, ניסוחי שאלות, ציוני צירים) משתנה בעריכה ידנית של המשתמש
 * (אוריאל) ולא על ידי האפליקציה עצמה - JSON קריא-אנושית הוא הכי נוח לעריכה כזו.
 * כשיתווספו תמונות/מקורות מלאים אפשר בקלות לפצל לכמה קבצים לפי שיטה, או לעבור
 * ל-Room אם יידרש חיפוש/סינון כבד יותר.
 */
class ContentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class MethodsFile(val methods: List<KnotMethod>)

    @Serializable
    private data class QuestionsFile(val questions: List<Question>)

    private var cachedMethods: List<KnotMethod>? = null
    private var cachedQuestions: List<Question>? = null

    fun loadMethods(): List<KnotMethod> {
        cachedMethods?.let { return it }
        val text = context.assets.open("methods.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(MethodsFile.serializer(), text).methods
        cachedMethods = parsed
        return parsed
    }

    fun loadQuestions(): List<Question> {
        cachedQuestions?.let { return it }
        val text = context.assets.open("questions.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(QuestionsFile.serializer(), text).questions
        cachedQuestions = parsed
        return parsed
    }

    fun findMethod(id: String): KnotMethod? = loadMethods().find { it.id == id }
}
