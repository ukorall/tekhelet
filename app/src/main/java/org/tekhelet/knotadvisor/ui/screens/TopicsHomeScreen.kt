package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.JourneyState
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.components.*

/**
 * מסך הפתיחה. הטון כאן מכוון: זו לא אנציקלופדיה שמכריזה על האמת, אלא שיחה
 * עם אוריאל על הנושא - ולכן הכל בגוף ראשון.
 */
@Composable
fun TopicsHomeScreen(
    contentError: String?,
    journey: JourneyState,
    consultingFor: String,
    onSelectTopic: (Topic) -> Unit,
    onStartJourney: () -> Unit,
    onContinueJourney: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenFeedback: () -> Unit,
    onSetConsultingFor: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PageGutter)
    ) {
        Spacer(Modifier.height(20.dp))
        PageHeader(
            title = "בורר קשירת תכלת",
            kicker = "ברכותיי! אתה בדרך אל האור."
        )
        Spacer(Modifier.height(16.dp))
        GadilRule()
        Spacer(Modifier.height(20.dp))

        Text(
            "נהוג בעולם שכל מי שרוצה להתחיל עם תכלת מתחיל בלשאול מישהו, קורא מאמר, " +
                "ואז מתחיל לטבוע במידע בלי לראות את הדרך החוצה. באתי לעקור את המנהג הזה " +
                "מהשורש, בתקווה לעשות יותר תועלת מנזק.",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(16.dp))
        SourceQuote(
            "האפליקציה הזאת היא לא פסק הלכה ולא \"האמת\". היא בעצם שיחה איתי על הנושא - " +
                "אותן שאלות שהייתי שואל אותך אם היינו יושבים יחד, ואותם שיקולים שהייתי " +
                "מעלה בפניך. בסוף אתה מחליט."
        )

        contentError?.let { err ->
            Spacer(Modifier.height(20.dp))
            Leaf(tinted = true) {
                Text("בעיה בטעינת התוכן", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("חלק מהאפליקציה לא יעבוד. זה מה שנכשל:", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Text(err, style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(Modifier.height(28.dp))
        ConsultingForField(consultingFor, onSetConsultingFor)

        Spacer(Modifier.height(28.dp))
        SectionHeading("המסע")
        Spacer(Modifier.height(12.dp))
        if (journey.active) {
            Leaf(onClick = onContinueJourney) {
                Text("להמשיך מאיפה שעצרנו", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(journey.currentStation.title, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { journey.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Leaf(onClick = onStartJourney) {
                Text("לצאת למסע", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "מעבר מסודר בין כל התחנות, לפי הסדר. אפשר לעצור בכל שלב - " +
                        "אני אזכור איפה עצרנו.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        SectionHeading("או לקפוץ לאן שרלוונטי")
        Spacer(Modifier.height(4.dp))

        Topic.entries.forEach { topic ->
            NavRow(
                title = topic.title,
                subtitle = topic.subtitle,
                badge = if (topic.isImplemented) null else "בבנייה"
            ) { onSelectTopic(topic) }
        }

        Spacer(Modifier.height(28.dp))
        NavRow("ייעוצים קודמים", onClick = onOpenHistory)
        NavRow("יש לך הערה בשבילי?", onClick = onOpenFeedback)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ConsultingForField(value: String, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    Column {
        Text("עם מי אנחנו מתייעצים?", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; onChange(it) },
            placeholder = { Text("השם שלי, או של מי שאני עוזר לו") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Aside("אני שומר כל ייעוץ בנפרד, כדי שאפשר יהיה לחזור ולראות מה יצא למי.")
    }
}
