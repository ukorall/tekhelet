package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.components.GadilRule

/**
 * מסך לאזור שעדיין אין בו שאלון פעיל. במקום "בקרוב" ריק, מוצג כאן מה שכבר
 * ידוע - השיקולים, סדר הקריאה, והנקודות שכדאי להכיר. זה שימושי גם כמו שהוא.
 */
@Composable
fun TopicNotesScreen(topic: Topic) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text(topic.title, style = MaterialTheme.typography.headlineSmall)
        Text(topic.subtitle, style = MaterialTheme.typography.bodyMedium)
        GadilRule(modifier = Modifier.padding(top = 12.dp, bottom = 14.dp))

        Text(
            "עוד לא בניתי כאן שאלון אינטראקטיבי. בינתיים, אלה הדברים שהייתי אומר לך " +
                "אם היינו יושבים ומדברים על זה:",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(16.dp))

        topic.notes.forEach { note ->
            Card(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                Text(note, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(14.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
