package org.tekhelet.knotadvisor.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.tekhelet.knotadvisor.model.JourneyState

private val Context.sessionDataStore by preferencesDataStore(name = "session_state")

/**
 * שומר את מצב המסע ואת שם המתייעץ הנוכחי, כך שאפשר לסגור את האפליקציה באמצע
 * ולחזור בדיוק לאותה נקודה. זו הדרישה המרכזית של "המסע" - שאפשר יהיה לעצור
 * בכל שלב ושהאפליקציה תזכור איפה עצרנו.
 */
class SessionStore(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val journeyKey = stringPreferencesKey("journey_state_json")
    private val consultingForKey = stringPreferencesKey("consulting_for")

    val journey: Flow<JourneyState> = context.sessionDataStore.data.map { prefs ->
        prefs[journeyKey]
            ?.let { runCatching { json.decodeFromString(JourneyState.serializer(), it) }.getOrNull() }
            ?: JourneyState()
    }

    val consultingFor: Flow<String> = context.sessionDataStore.data.map { it[consultingForKey] ?: "" }

    suspend fun saveJourney(state: JourneyState) {
        context.sessionDataStore.edit {
            it[journeyKey] = json.encodeToString(JourneyState.serializer(), state)
        }
    }

    suspend fun saveConsultingFor(name: String) {
        context.sessionDataStore.edit { it[consultingForKey] = name }
    }
}
