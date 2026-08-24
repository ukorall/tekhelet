package org.tekhelet.knotadvisor.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.model.Question
import java.io.IOException

/**
 * טוען את תוכן הבסיס (שיטות קשירה ושאלות) מקבצי ה-assets המצורפים לאפליקציה.
 *
 * הבחירה לשמור את התוכן כ-JSON בתוך assets, ולא ב-Room או שרת מרוחק, היא מכוונת:
 * בשלב הזה התוכן (שיטות, ניסוחי שאלות, ציוני צירים) משתנה בעריכה ידנית של המשתמש
 * (אוריאל) ולא על ידי האפליקציה עצמה - JSON קריא-אנושית הוא הכי נוח לעריכה כזו.
 * כשיתווספו תמונות/מקורות מלאים אפשר בקלות לפצל לכמה קבצים לפי שיטה, או לעבור
 * ל-Room אם יידרש חיפוש/סינון כבד יותר.
 *
 * מעל זה: `texts.txt` (מגיע מ-content/texts.txt שבשורש הריפו, ראו content/README.md)
 * יכול "לדרוס" חלק מהטקסטים החופשיים (shortSummary/fullDescription/editorialNote)
 * בלי לגעת ב-methods.json - כדי לאפשר עריכת טקסט נוחה בלי לגעת ב-JSON/קוד.
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
        val base = json.decodeFromString(MethodsFile.serializer(), text).methods
        val overrides = loadTextOverrides()
        val parsed = if (overrides.isEmpty()) base else base.map { applyTextOverrides(it, overrides) }
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

    private fun applyTextOverrides(method: KnotMethod, overrides: Map<String, String>): KnotMethod =
        method.copy(
            shortSummary = overrides["${method.id}.shortSummary"] ?: method.shortSummary,
            fullDescription = overrides["${method.id}.fullDescription"] ?: method.fullDescription,
            editorialNote = overrides["${method.id}.editorialNote"] ?: method.editorialNote
        )

    /**
     * מפרסר את texts.txt: כותרות בצורת "### <method-id>.<field>" מתחילות בלוק, וכל מה
     * שאחריהן (עד הכותרת הבאה או סוף הקובץ) הוא הטקסט החופשי לאותו שדה. שדות נתמכים:
     * shortSummary, fullDescription, editorialNote. הקובץ אופציונלי - חסר = בלי override.
     */
    private fun loadTextOverrides(): Map<String, String> {
        val text = try {
            context.assets.open("texts.txt").bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            return emptyMap()
        }

        val overrides = mutableMapOf<String, String>()
        var currentKey: String? = null
        val buffer = StringBuilder()

        fun flush() {
            currentKey?.let { key ->
                val value = buffer.toString().trim()
                if (value.isNotEmpty()) overrides[key] = value
            }
            buffer.clear()
        }

        text.lineSequence().forEach { line ->
            if (line.startsWith("### ")) {
                flush()
                currentKey = line.removePrefix("### ").trim()
            } else if (currentKey != null) {
                buffer.appendLine(line)
            }
        }
        flush()

        return overrides
    }
}
