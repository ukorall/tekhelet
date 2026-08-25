package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.JourneyState
import org.tekhelet.knotadvisor.model.JourneyStation
import org.tekhelet.knotadvisor.ui.components.KnotDivider

/**
 * מסך המסע: רואים את כל התחנות, איפה עומדים, ומה כבר הושלם. אפשר לקפוץ לכל
 * תחנה, ואפשר לעצור - המצב נשמר וממשיכים ממנו בפעם הבאה.
 */
@Composable
fun JourneyScreen(
    journey: JourneyState,
    consultingFor: String,
    onOpenStation: (JourneyStation) -> Unit,
    onCompleteCurrent: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("המסע", style = MaterialTheme.typography.headlineSmall)
        if (consultingFor.isNotBlank()) {
            Text("עבור $consultingFor", style = MaterialTheme.typography.bodyMedium)
        }
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "חמש תחנות, לפי הסדר שבו הן באמת מגיעות. אפשר לעצור בכל שלב - " +
                "אני זוכר איפה עצרנו.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(14.dp))
        LinearProgressIndicator(progress = { journey.progress }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        Text(
            "${journey.completed.size} מתוך ${JourneyStation.ordered.size} תחנות הושלמו",
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(Modifier.height(18.dp))
        JourneyStation.ordered.forEach { station ->
            val isCurrent = station == journey.currentStation
            val isDone = station in journey.completed
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                onClick = { onOpenStation(station) },
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                        isDone -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${station.index + 1}. ${station.title}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )
                        if (isDone) Text("✓", style = MaterialTheme.typography.titleMedium)
                        else if (isCurrent) Text("כאן", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(station.blurb, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(onClick = onCompleteCurrent, modifier = Modifier.fillMaxWidth()) {
            Text("סיימתי את \"${journey.currentStation.title}\", לתחנה הבאה")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onPause, modifier = Modifier.fillMaxWidth()) {
            Text("לעצור כאן בינתיים")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text("להתחיל מסע מחדש")
        }
        Spacer(Modifier.height(24.dp))
    }
}
