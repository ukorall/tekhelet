package org.tekhelet.knotadvisor.data

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.Feedback
import java.io.File

/**
 * שומר פידבק כקובץ JSON במכשיר, ומאפשר לשלוח אותו הלאה.
 *
 * למה לא שליחה ישירה ל-GitHub: כדי לכתוב לריפו מתוך האפליקציה צריך טוקן גישה
 * בתוך ה-APK, וטוקן ב-APK הוא טוקן פומבי - כל מי שיוריד את האפליקציה יכול
 * לחלץ אותו ולכתוב לריפו. זו לא הגזמה זהירותית, זה פשוט מה שקורה.
 *
 * לכן הזרימה היא: הפידבק נשמר כקובץ מסודר במכשיר, והבודק שולח אותו אליך בכל
 * דרך שנוחה לו (וואטסאפ, מייל). אתה שומר את הקבצים ב-feedback/inbox/ בריפו,
 * ואז אפשר לדון עליהם. הקובץ הוא JSON מובנה, כך שאפשר יהיה לעבד עשרות כאלה
 * יחד בלי לקרוא כל אחד בנפרד.
 */
class FeedbackStore(private val context: Context) {

    private val json = Json { prettyPrint = true; encodeDefaults = true }

    private val dir: File
        get() = File(context.filesDir, "feedback").apply { mkdirs() }

    fun save(feedback: Feedback): File {
        val file = File(dir, "feedback-${feedback.createdAtEpochMillis}-${feedback.id.take(6)}.json")
        file.writeText(json.encodeToString(Feedback.serializer(), feedback))
        return file
    }

    fun all(): List<Feedback> =
        dir.listFiles { f -> f.extension == "json" }
            ?.mapNotNull { runCatching { json.decodeFromString(Feedback.serializer(), it.readText()) }.getOrNull() }
            ?.sortedByDescending { it.createdAtEpochMillis }
            ?: emptyList()

    /** בונה Intent שיתוף לקובץ הפידבק, כדי שהבודק ישלח אותו לאוריאל. */
    fun shareIntent(file: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "פידבק על בורר קשירת תכלת")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
