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
import org.tekhelet.knotadvisor.ui.components.*

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
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = PageGutter)
    ) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "המסע",
            kicker = if (consultingFor.isNotBlank()) "עבור $consultingFor" else null,
            lead = "חמש תחנות, לפי הסדר שבו הן באמת מגיעות. אפשר לעצור בכל שלב - " +
                "אני זוכר איפה עצרנו."
        )

        Spacer(Modifier.height(22.dp))
        LinearProgressIndicator(progress = { journey.progress }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(6.dp))
        Aside(
            ltr("${journey.completed.size}") + " מתוך " +
                ltr("${JourneyStation.ordered.size}") + " תחנות הושלמו"
        )

        Spacer(Modifier.height(24.dp))
        JourneyStation.ordered.forEach { station ->
            val isCurrent = station == journey.currentStation
            val isDone = station in journey.completed
            Leaf(
                modifier = Modifier.padding(bottom = 10.dp),
                tinted = isCurrent,
                onClick = { onOpenStation(station) }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Pill(ltr("${station.index + 1}"), emphasised = isCurrent)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        station.title,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isDone) Text("✓", style = MaterialTheme.typography.titleLarge)
                    else if (isCurrent) Pill("כאן")
                }
                Spacer(Modifier.height(6.dp))
                Text(station.blurb, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(14.dp))
        Button(
            onClick = onCompleteCurrent,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("סיימתי את \"${journey.currentStation.title}\", לתחנה הבאה")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onPause,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("לעצור כאן בינתיים")
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) {
            Text("להתחיל מסע מחדש")
        }
        Spacer(Modifier.height(24.dp))
    }
}
