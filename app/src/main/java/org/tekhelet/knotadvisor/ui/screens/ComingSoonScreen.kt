package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.Topic

/**
 * מסך placeholder גנרי לאזורים שעדיין לא מומשו (ראו Topic.isImplemented).
 * מטרתו רק לסמן את המקום בניווט, עד שיתווסף תוכן (שאלות/מאמרים) לכל אזור.
 */
@Composable
fun ComingSoonScreen(topic: Topic) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(topic.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(topic.subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(
            "האזור הזה עדיין בפיתוח - יתווסף בהמשך.",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center
        )
    }
}
