package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.AppViewModel

/**
 * שאלת משנה: כשלמועמדת המובילה יש כמה אורכים (7/13 כריכות) - מציגה מסך הסבר קצר.
 * v0.1: לא משנה עדיין את הניקוד, רק מציינת שיש לבחור variant במסך הפירוט של השיטה.
 */
@Composable
fun WindCountTieBreakerScreen(viewModel: AppViewModel, onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("שאלת המשך: מספר כריכות", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            "לפחות באחת השיטות המובילות עבורך יש כמה גרסאות אורך (למשל 7 מול 13 כריכות). " +
                "במסך פירוט השיטה תוכלו לראות את שתי האפשרויות ולבחור ביניהן.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("הבנתי, המשך לתוצאות")
        }
    }
}

/**
 * שאלת משנה חזותית: כששני המועמדים המובילים צמודים בניקוד, מציגה אותם זה מול זה
 * ומבקשת מהמשתמש לבחור מה יפה בעיניו יותר - בדיוק כמו ההכרעה הסופית שקרתה בפועל
 * בשיחה שעליה מבוססת האפליקציה. כרגע מוצג placeholder לתמונה, עד שיסופקו תמונות אמיתיות.
 */
@Composable
fun VisualTieBreakerScreen(viewModel: AppViewModel, onChoiceMade: () -> Unit) {
    val topCandidates = viewModel.results.take(3)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("המועמדות קרובות מאוד - מה הכי יפה בעיניך?", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "לפי התשובות שלך כמה שיטות מובילות מאוד קרובות בניקוד. הבחירה החזותית שלכם כאן " +
                "תכריע את הסדר הסופי.",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(topCandidates) { candidate ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.applyVisualChoice(candidate.method.id)
                        onChoiceMade()
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(candidate.method.name, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (candidate.method.imageAssets.isEmpty())
                                "[מקום לתמונה - יתווסף בהמשך]"
                            else "תמונה: ${candidate.method.imageAssets.first()}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
