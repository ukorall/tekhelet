package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.components.*

/**
 * שאלת משנה: כשלמועמדת המובילה יש כמה אורכים (7/13 כריכות) - מציגה מסך הסבר קצר.
 * v0.1: לא משנה עדיין את הניקוד, רק מציינת שיש לבחור variant במסך הפירוט של השיטה.
 */
@Composable
fun WindCountTieBreakerScreen(onContinue: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter)) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "שאלת המשך: מספר כריכות",
            lead = "לפחות באחת השיטות המובילות אצלך יש כמה גרסאות אורך - למשל 7 מול " +
                "13 כריכות. במסך פירוט השיטה תראה את שתי האפשרויות ותוכל לבחור ביניהן."
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("הבנתי, ממשיכים לתוצאות", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * שאלת משנה חזותית: כששני המועמדים המובילים צמודים בניקוד, מציגה אותם זה מול זה
 * ומבקשת מהמשתמש לבחור מה יפה בעיניו יותר - בדיוק כמו ההכרעה הסופית שקרתה בפועל
 * בשיחה שעליה מבוססת האפליקציה. מציגה תמונות אמיתיות אם קיימות (ראו AssetImages),
 * אחרת placeholder כללי.
 */
@Composable
fun VisualTieBreakerScreen(viewModel: AppViewModel, onChoiceMade: () -> Unit) {
    val topCandidates = viewModel.results.take(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            PageHeader(
                title = "מה הכי יפה בעיניך?",
                kicker = "המועמדות קרובות מאוד",
                lead = "לפי התשובות שלך כמה שיטות מובילות יצאו כמעט באותו ניקוד. " +
                    "הבחירה החזותית שלך כאן תכריע את הסדר הסופי."
            )
        }
        items(topCandidates) { candidate ->
            Leaf(onClick = {
                viewModel.applyVisualChoice(candidate.method.id)
                onChoiceMade()
            }) {
                Text(candidate.method.name, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(10.dp))
                MethodImages(candidate.method, imageHeight = 120.dp)
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}
