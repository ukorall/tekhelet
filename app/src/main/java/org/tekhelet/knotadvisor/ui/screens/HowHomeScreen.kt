package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.components.*

@Composable
fun HowHomeScreen(
    onStartQuestionnaire: () -> Unit,
    onBrowseLibrary: () -> Unit,
    onOpenTree: () -> Unit,
    onOpenBuilder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PageGutter)
    ) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "איך לקשור?",
            lead = "יש כמה מימרות בגמרא שמדברות על הקשירה - איך נראה הגדיל, מספרי " +
                "ליפופים, חוליות, קשרים. יש הרבה מחלוקות בפירוש המימרות ואיך ליישב " +
                "ביניהן, וממילא יש הרבה מאוד שיטות קשירה."
        )

        Spacer(Modifier.height(24.dp))
        SectionHeading("שיקול אחד מיני כמה")
        Spacer(Modifier.height(14.dp))
        SourceQuote(
            "אחרי שהכרעת בשאלת הזיהוי ובשאלת מספר החוטים, לשאלת הקשירה אין אפילו " +
                "חשיבות דרבנן לרוב השיטות. ובגלל שלאבותינו בדורות האחרונים לא היה " +
                "תכלת, אין לנו מנהג, ונשארנו עם הכלל של נוי מצווה - \"זה אלי ואנוהו\".",
            source = "שמעתי מהרב אחיה בן פזי בשם הרב ריסקין"
        )
        Spacer(Modifier.height(14.dp))
        Aside(
            "אני מביא את זה כי זה מעורר מחשבה, לא כי זו הדרך היחידה להחליט. יש " +
                "שיקולים אחרים לגמרי - ביסוס בראשונים, מה מקובל בפועל, מה מסתדר עם " +
                "הגמרא - והשאלון נותן מקום לכולם. אם היופי לא השיקול המרכזי שלך, זה " +
                "בסדר גמור."
        )

        Spacer(Modifier.height(30.dp))
        Button(
            onClick = onStartQuestionnaire,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text("להתחיל את השאלון", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(24.dp))
        SectionHeading("או דרכים אחרות")
        Spacer(Modifier.height(4.dp))
        NavRow("לבנות הרכב בעצמי", "לבחור כל החלטה בנפרד ולראות מה יוצא", onClick = onOpenBuilder)
        NavRow("מפת השיטות", "התרשים - איפה כל שיטה יושבת ביחס לאחרות", onClick = onOpenTree)
        NavRow("לעיין בכל השיטות", "הרשימה המלאה, בלי דירוג", onClick = onBrowseLibrary)
        Spacer(Modifier.height(32.dp))
    }
}
