package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.components.KnotDivider

/**
 * מסך הבית העליון של האפליקציה: בורר בין ארבעת אזורי הייעוץ (Topic). כרגע רק "איך"
 * ממומש; שאר האזורים מובילים למסך placeholder (ComingSoonScreen) עד שיתווסף תוכן.
 */
@Composable
fun TopicsHomeScreen(onSelectTopic: (Topic) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text("בורר קשירת תכלת", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        KnotDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            "כלי עזר להחלטות סביב הטלת תכלת בציצית. בחר/י באיזה שלב את/ה נמצא/ת:",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Topic.entries.forEach { topic ->
            TopicCard(topic = topic, onClick = { onSelectTopic(topic) })
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun TopicCard(topic: Topic, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(topic.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (!topic.isImplemented) {
                    AssistChip(onClick = onClick, label = { Text("בקרוב") })
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(topic.subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
