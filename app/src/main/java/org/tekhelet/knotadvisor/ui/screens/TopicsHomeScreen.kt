package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.JourneyState
import org.tekhelet.knotadvisor.model.Topic
import org.tekhelet.knotadvisor.ui.components.KnotDivider

/**
 * מסך הפתיחה. הטון כאן מכוון: זו לא אנציקלופדיה שמכריזה על האמת, אלא שיחה
 * עם אוריאל על הנושא - ולכן הכל בגוף ראשון.
 */
@Composable
fun TopicsHomeScreen(
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
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("בורר קשירת תכלת", style = MaterialTheme.typography.headlineMedium)
        KnotDivider(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))

        Text(
            "ברכותיי! אתה בדרך אל האור.",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "נהוג בעולם שכל מי שרוצה להתחיל עם תכלת מתחיל בלשאול מישהו, קורא מאמר, " +
                "ואז מתחיל לטבוע במידע בלי לראות את הדרך החוצה. באתי לעקור את המנהג הזה " +
                "מהשורש, בתקווה לעשות יותר תועלת מנזק.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "האפליקציה הזאת היא לא פסק הלכה ולא \"האמת\". היא בעצם שיחה איתי על הנושא - " +
                "אותן שאלות שהייתי שואל אותך אם היינו יושבים יחד, ואותם שיקולים שהייתי " +
                "מעלה בפניך. בסוף אתה מחליט.",
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
        )

        Spacer(Modifier.height(20.dp))
        ConsultingForField(consultingFor, onSetConsultingFor)

        Spacer(Modifier.height(20.dp))
        if (journey.active) {
            Card(modifier = Modifier.fillMaxWidth(), onClick = onContinueJourney) {
                Column(Modifier.padding(16.dp)) {
                    Text("להמשיך מאיפה שעצרנו", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(journey.currentStation.title, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { journey.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), onClick = onStartJourney) {
                Column(Modifier.padding(16.dp)) {
                    Text("לצאת למסע", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "מעבר מסודר בין כל התחנות, לפי הסדר. אפשר לעצור בכל שלב - " +
                            "אני אזכור איפה עצרנו.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("או פשוט לקפוץ לאן שרלוונטי לך", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(10.dp))

        Topic.entries.forEach { topic ->
            TopicCard(topic) { onSelectTopic(topic) }
            Spacer(Modifier.height(10.dp))
        }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
            Text("ייעוצים קודמים")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onOpenFeedback, modifier = Modifier.fillMaxWidth()) {
            Text("יש לך הערה בשבילי?")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ConsultingForField(value: String, onChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    Column {
        Text("עם מי אנחנו מתייעצים?", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it; onChange(it) },
            placeholder = { Text("השם שלי, או של מי שאני עוזר לו") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "אני שומר כל ייעוץ בנפרד, כדי שאפשר יהיה להיזכר אחר כך מה הומלץ למי.",
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TopicCard(topic: Topic, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(topic.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (!topic.isImplemented) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            "בבנייה",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(topic.subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
