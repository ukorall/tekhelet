package org.tekhelet.knotadvisor.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.SavedConsultation

private val Context.dataStore by preferencesDataStore(name = "consultation_history")

/**
 * שומר מקומית (במכשיר בלבד, ללא שרת) את היעוצים שבוצעו - כדי לתמוך בשימוש שבו
 * משתמש אחד (למשל אוריאל) מריץ את השאלון עבור כמה חברים לאורך זמן, וצריך
 * להיזכר מה הומלץ למי ומתי. שם החבר, התשובות והתוצאה נשמרים יחד כרשומה אחת.
 *
 * מימוש v0.1: DataStore עם מפתח יחיד המכיל רשימת ייעוצים כ-JSON. פשוט מספיק
 * לכמות ייעוצים קטנה; אם הכמות תגדל משמעותית שווה לעבור ל-Room.
 */
class HistoryStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("saved_consultations_json")

    val consultations: Flow<List<SavedConsultation>> = context.dataStore.data.map { prefs ->
        val raw = prefs[key] ?: return@map emptyList()
        runCatching {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(SavedConsultation.serializer()),
                raw
            )
        }.getOrDefault(emptyList())
    }

    suspend fun save(consultation: SavedConsultation) {
        context.dataStore.edit { prefs ->
            val existingRaw = prefs[key]
            val existing = existingRaw?.let {
                runCatching {
                    json.decodeFromString(
                        kotlinx.serialization.builtins.ListSerializer(SavedConsultation.serializer()),
                        it
                    )
                }.getOrDefault(emptyList())
            } ?: emptyList()

            val updated = existing.filterNot { it.id == consultation.id } + consultation
            prefs[key] = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(SavedConsultation.serializer()),
                updated
            )
        }
    }

    suspend fun delete(consultationId: String) {
        context.dataStore.edit { prefs ->
            val existingRaw = prefs[key] ?: return@edit
            val existing = runCatching {
                json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(SavedConsultation.serializer()),
                    existingRaw
                )
            }.getOrDefault(emptyList())
            val updated = existing.filterNot { it.id == consultationId }
            prefs[key] = json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(SavedConsultation.serializer()),
                updated
            )
        }
    }
}
