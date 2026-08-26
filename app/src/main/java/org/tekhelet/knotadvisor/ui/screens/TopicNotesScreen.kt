package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.components.*

/**
 * מסך לאזור שעדיין אין בו שאלון פעיל. במקום "בקרוב" ריק, מוצג כאן מה שכבר
 * ידוע - השיקולים, סדר הקריאה, והנקודות שכדאי להכיר. זה שימושי גם כמו שהוא.
 */
@Composable
fun TopicNotesScreen(topic: Topic) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = PageGutter)
    ) {
        Spacer(Modifier.height(20.dp))
        PageHeader(title = topic.title, kicker = topic.subtitle)
        Spacer(Modifier.height(16.dp))
        GadilRule()

        Spacer(Modifier.height(20.dp))
        Text(
            "עוד לא בניתי כאן שאלון אינטראקטיבי. בינתיים, אלה הדברים שהייתי אומר לך " +
                "אם היינו יושבים ומדברים על זה:",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(20.dp))

        topic.notes.forEach { note ->
            Leaf(modifier = Modifier.padding(bottom = 10.dp)) {
                Text(note, style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
