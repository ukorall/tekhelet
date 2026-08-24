package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.components.KnotDivider

/** מסך הבית של אזור "איך" (בחירת שיטת קשירה) - מגיעים אליו מ-TopicsHomeScreen. */
@Composable
fun HowHomeScreen(
    onStartQuestionnaire: () -> Unit,
    onBrowseLibrary: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("איך? - שיטת קשירה", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        KnotDivider(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            "כלי עזר לבחירת שיטת קשירה בתכלת, לאחר שכבר הוחלט לקשור תכלת בציצית. " +
                "עונים על כמה שאלות על מה חשוב לכם, והאפליקציה מציעה שיטות מתאימות עם הסבר קצר.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "\"לאחר שהוכרעו הזיהוי ומספר החוטים, לשאלת הקשירה כמעט אין משמעות הלכתית - " +
                "נשאר רק הכלל של נוי מצווה, 'זה אלי ואנוהו': תבחר את השיטה הכי יפה בעיניך.\"",
            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
            textAlign = TextAlign.Center
        )
        Text(
            "- כפי ששמע אוריאל מהרב אחיה, בשם הרב ריסקין",
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStartQuestionnaire, modifier = Modifier.fillMaxWidth()) {
            Text("התחל שאלון")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onBrowseLibrary, modifier = Modifier.fillMaxWidth()) {
            Text("עיון חופשי בכל השיטות")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
            Text("ייעוצים קודמים ששמרתי")
        }
    }
}
