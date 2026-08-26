package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.components.KnotDivider

@Composable
fun HowHomeScreen(
    onStartQuestionnaire: () -> Unit,
    onBrowseLibrary: () -> Unit,
    onOpenTree: () -> Unit,
    onOpenBuilder: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)
    ) {
        Text("איך לקשור?", style = MaterialTheme.typography.headlineSmall)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "יש כמה מימרות בגמרא שמדברות על הקשירה - איך נראה הגדיל, מספרי ליפופים, " +
                "חוליות, קשרים. יש הרבה מחלוקות בפירוש המימרות ואיך ליישב ביניהן, " +
                "וממילא יש הרבה מאוד שיטות קשירה.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(14.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text("שיקול אחד מיני כמה", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Text(
                    "שמעתי מהרב אחיה בן פזי בשם הרב ריסקין, שאחרי שהכרעת בשאלת הזיהוי " +
                        "ובשאלת מספר החוטים, לשאלת הקשירה אין אפילו חשיבות דרבנן לרוב " +
                        "השיטות. ובגלל שלאבותינו בדורות האחרונים לא היה תכלת, אין לנו " +
                        "מנהג, ונשארנו עם הכלל של נוי מצווה - \"זה אלי ואנוהו\".",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "אני מביא את זה כי זה מעורר מחשבה, לא כי זו הדרך היחידה להחליט. " +
                        "יש שיקולים אחרים לגמרי - ביסוס בראשונים, מה מקובל בפועל, מה " +
                        "מסתדר עם הגמרא - והשאלון נותן מקום לכולם. אם היופי לא השיקול " +
                        "המרכזי שלך, זה בסדר גמור.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        Button(onClick = onStartQuestionnaire, modifier = Modifier.fillMaxWidth()) {
            Text("להתחיל את השאלון")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onOpenBuilder, modifier = Modifier.fillMaxWidth()) {
            Text("לבנות הרכב בעצמי")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onOpenTree, modifier = Modifier.fillMaxWidth()) {
            Text("מפת השיטות")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBrowseLibrary, modifier = Modifier.fillMaxWidth()) {
            Text("לעיין בכל השיטות")
        }
        Spacer(Modifier.height(24.dp))
    }
}
