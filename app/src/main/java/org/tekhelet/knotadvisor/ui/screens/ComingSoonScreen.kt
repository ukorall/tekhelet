package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Topic

/**
 * מסך placeholder גנרי לאזורים שעדיין לא מומשו (ראו Topic.isImplemented). מציג roadmap -
 * מה כבר ידוע/מתוכנן לאזור הזה, לפי Topic.roadmap - עד שיתווסף שאלון אמיתי.
 */
@Composable
fun ComingSoonScreen(topic: Topic) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Text(topic.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(topic.subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(
            "האזור הזה עדיין בפיתוח - אין כאן עדיין שאלון פעיל. הנה מה שכבר ידוע ומתוכנן:",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            topic.roadmap.forEach { point ->
                Text(
                    "•  $point",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}
