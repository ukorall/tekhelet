package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.ScoredMethod
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.components.*
import kotlin.math.roundToInt

@Composable
fun ResultsScreen(
    viewModel: AppViewModel,
    onOpenDetail: (String) -> Unit,
    onFinalize: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            PageHeader(
                title = "מה יצא",
                lead = "הדירוג מבוסס על מה שאמרת לי שחשוב לך. זה לא פסק הלכה - כל " +
                    "השיטות כאן לגיטימיות, וזו רק דרך למיין ביניהן."
            )
        }

        if (viewModel.variantSuggestions.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                SectionHeading("אולי בעצם זה מה שאתה מחפש")
            }
            items(viewModel.variantSuggestions.take(3)) { s ->
                Leaf(tinted = true) {
                    Text(s.variant.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(s.reason, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    Aside(s.variant.rationale)
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            SectionHeading("הדירוג")
        }

        itemsIndexed(viewModel.results.take(viewModel.visibleResultCount)) { index, scored ->
            ResultLeaf(
                rank = index + 1,
                scored = scored,
                onOpenDetail = { onOpenDetail(scored.method.id) },
                onFinalize = { onFinalize(scored.method.id) }
            )
        }

        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun ResultLeaf(
    rank: Int,
    scored: ScoredMethod,
    onOpenDetail: () -> Unit,
    onFinalize: () -> Unit
) {
    Leaf(onClick = onOpenDetail) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill("$rank", emphasised = rank == 1)
            Spacer(Modifier.width(10.dp))
            Text(
                scored.method.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                ltr("${scored.score.roundToInt()}%"),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(scored.method.shortSummary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Aside(scored.explanation)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenDetail, shape = MaterialTheme.shapes.small) {
                Text("פרטים ומקורות")
            }
            Button(onClick = onFinalize, shape = MaterialTheme.shapes.small) {
                Text("זו הבחירה שלי")
            }
        }
    }
}
