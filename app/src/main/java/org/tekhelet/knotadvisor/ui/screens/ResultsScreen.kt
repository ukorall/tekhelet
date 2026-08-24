package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.ScoredMethod
import org.tekhelet.knotadvisor.ui.AppViewModel
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    viewModel: AppViewModel,
    onOpenDetail: (String) -> Unit,
    onFinalize: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("השיטות המתאימות ביותר עבורך", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(
            "הדירוג מבוסס על התשובות שלך. אין כאן פסק הלכה - רק כלי עזר לבחירה מתוך שיטות לגיטימיות.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(viewModel.results) { scored ->
                ResultCard(
                    scored = scored,
                    onOpenDetail = { onOpenDetail(scored.method.id) },
                    onFinalize = { onFinalize(scored.method.id) }
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    scored: ScoredMethod,
    onOpenDetail: () -> Unit,
    onFinalize: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onOpenDetail) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(scored.method.name, style = MaterialTheme.typography.titleMedium)
                Text("${scored.score.roundToInt()}% התאמה", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
            Text(scored.method.shortSummary, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(scored.explanation, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenDetail) { Text("פרטים ומקורות") }
                Button(onClick = onFinalize) { Text("זו הבחירה שלי") }
            }
        }
    }
}
