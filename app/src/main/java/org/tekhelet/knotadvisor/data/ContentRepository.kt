package org.tekhelet.knotadvisor.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.KnotMethod
import org.tekhelet.knotadvisor.model.Question
import java.io.IOException

/**
 * טוען את תוכן הבסיס (שיטות קשירה ושאלות) מקבצי ה-assets.
 *
 * התוכן נשמר כ-JSON בתוך assets ולא ב-Room או בשרת, כי זה תוכן שנערך ידנית
 * ולא על ידי האפליקציה - JSON קריא-אנושית הוא הכי נוח לעריכה כזו.
 *
 * מעל זה: `texts.txt` (מגיע מ-content/texts.txt, ראו content/README.md) יכול
 * "לדרוס" חלק מהטקסטים החופשיים בלי לגעת ב-methods.json.
 *
 * **עמידות לשגיאות.** קודם, כל טעות קטנה בקובץ ה-JSON - למשל ערך enum שלא
 * קיים יותר בקוד - הפילה את כל האפליקציה בכל מסך שנגע בשיטות, בלי שום הסבר.
 * זה בדיוק מה שקרה. עכשיו כישלון טעינה נתפס, נרשם ללוג, ומוחזר כרשימה ריקה
 * יחד עם הודעת שגיאה קריאה שהממשק יכול להציג - כך שגם אם התוכן שבור,
 * האפליקציה עדיין נפתחת ואפשר להבין למה.
 */
class ContentRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class MethodsFile(val methods: List<KnotMethod>)

    @Serializable
    private data class QuestionsFile(val questions: List<Question>)

    private var cachedMethods: List<KnotMethod>? = null
    private var cachedQuestions: List<Question>? = null

    /** מתמלא אם טעינת התוכן נכשלה, כדי שאפשר יהיה להראות למה. */
    var loadError: String? = null
        private set

    fun loadMethods(): List<KnotMethod> {
        cachedMethods?.let { return it }
        val parsed = try {
            val text = context.assets.open("methods.json").bufferedReader().use { it.readText() }
            val base = json.decodeFromString(MethodsFile.serializer(), text).methods
            val overrides = loadTextOverrides()
            if (overrides.isEmpty()) base else base.map { applyTextOverrides(it, overrides) }
        } catch (e: Exception) {
            recordError("methods.json", e)
            emptyList()
        }
        cachedMethods = parsed
        return parsed
    }

    fun loadQuestions(): List<Question> {
        cachedQuestions?.let { return it }
        val parsed = try {
            val text = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            json.decodeFromString(QuestionsFile.serializer(), text).questions
        } catch (e: Exception) {
            recordError("questions.json", e)
            emptyList()
        }
        cachedQuestions = parsed
        return parsed
    }

    fun findMethod(id: String): KnotMethod? = loadMethods().find { it.id == id }

    private fun recordError(file: String, e: Exception) {
        val msg = "שגיאה בטעינת $file: ${e.message ?: e::class.simpleName}"
        Log.e("ContentRepository", msg, e)
        loadError = (loadError?.plus("\n") ?: "") + msg
    }

    private fun applyTextOverrides(method: KnotMethod, overrides: Map<String, String>): KnotMethod =
        method.copy(
            shortSummary = overrides["${method.id}.shortSummary"] ?: method.shortSummary,
            fullDescription = overrides["${method.id}.fullDescription"] ?: method.fullDescription,
            editorialNote = overrides["${method.id}.editorialNote"] ?: method.editorialNote
        )

    /**
     * מפרסר את texts.txt: כותרות בצורת "### <method-id>.<field>" מתחילות בלוק,
     * וכל מה שאחריהן הוא הטקסט. הקובץ אופציונלי - חסר = בלי override.
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
