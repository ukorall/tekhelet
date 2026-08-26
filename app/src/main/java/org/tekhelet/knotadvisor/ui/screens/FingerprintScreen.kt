package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.logic.FreeformGadil
import org.tekhelet.knotadvisor.logic.FreeformGadil.SegmentKind
import org.tekhelet.knotadvisor.model.FingerprintQuestions
import org.tekhelet.knotadvisor.model.FpKind
import org.tekhelet.knotadvisor.model.FpQuestion
import org.tekhelet.knotadvisor.ui.components.*
import kotlin.random.Random

/**
 * "טביעת אצבע" - חמש שאלות שאין להן שום קשר לתכלת, ומהן נגזר גדיל שאפשר לערוך.
 *
 * זה בכוונה לא כלי החלטה. מי שרוצה מסקנה בלי לעבור את התהליך מקבל מסקנה
 * שמבוססת על "כמה שוקל יום שלישי", וזו הנקודה. אחרי ההגרלה נפתח בונה חופשי
 * שאפשר לשחק בו - כולל מקטעים שאין להם שום משמעות הלכתית.
 */
@Composable
fun FingerprintScreen() {
    // הגרלת השאלות עצמה אקראית בכל כניסה; הגדיל שנוצר מהן - לא.
    val questions = remember { FingerprintQuestions.draw(Random(System.currentTimeMillis())) }
    val answers = remember { mutableStateMapOf<String, String>() }
    var revealed by remember { mutableStateOf(false) }
    var segments by remember { mutableStateOf<List<SegmentKind>>(emptyList()) }

    fun reveal() {
        // שאלת ה"סוד" תורמת ערך קבוע - לא גילית, אז אין מה לקחת ממנה
        val seed = FreeformGadil.seedFrom(
            questions.map { if (it.kind == FpKind.SECRET) "סוד" else answers[it.id].orEmpty() }
        )
        val count = 7 + (kotlin.math.abs(seed) % 7)   // בין 7 ל-13 איברים
        segments = FreeformGadil.generate(seed, count)
        revealed = true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = PageGutter),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(20.dp))
            PageHeader(
                title = "טביעת אצבע",
                kicker = "קיצור דרך למסקנה אישית סופית, בלי כל החפירות"
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "את ארגון פתיל תכלת הקימו ארבעה קוקואים שגרים באפרת, ולשלושה מהם " +
                    "קוראים ברוך. אחד הברוכים אמר פעם ש\"החותמת והפתילים\" מתאר " +
                    "מציאות שלכל אחד יש קשירה משל עצמו, ואפשר להשתמש בפתילים בתור " +
                    "חותמת ייחודית.",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "זה וורט יפה שלא מבוסס על שום דבר, אבל החלטתי לזרום. בוא תמצא את " +
                    "הקשירה היותר פנים-פנימית ניש-נישמתית שמבטאת את האני האמיתי שלך.",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (!revealed) {
            itemsIndexed(questions) { index, q ->
                QuestionLeaf(index + 1, q, answers[q.id]) { answers[q.id] = it }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { reveal() },
                    enabled = questions.all {
                        it.kind == FpKind.SECRET || !answers[it.id].isNullOrBlank()
                    },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("תראה לי מה יצא", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Aside("צריך לענות על כל השאלות שיש בהן על מה לענות.")
            }
        } else {
            item {
                SectionHeading("זה שלך")
                Spacer(Modifier.height(12.dp))
                Leaf {
                    FreeformVisual(segments, height = (segments.size * 58).dp)
                }
                Spacer(Modifier.height(10.dp))
                Aside(
                    "אותן תשובות בדיוק ייתנו תמיד את אותו גדיל - אחרת זו לא טביעת " +
                        "אצבע אלא כפתור \"הפתע אותי\"."
                )
            }

            item {
                Spacer(Modifier.height(10.dp))
                SectionHeading("כמה איברים")
                Spacer(Modifier.height(10.dp))
                CountStepper(segments.size) { next ->
                    segments = when {
                        next > segments.size ->
                            segments + List(next - segments.size) { SegmentKind.CHULYA_TEKHELET }
                        else -> segments.take(next)
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                SectionHeading("מה יש בכל מקטע")
                Spacer(Modifier.height(4.dp))
                Aside("רשימה אחת. כולל דברים שאין להם שום משמעות - זה מכוון.")
            }
            itemsIndexed(segments) { index, kind ->
                SegmentPicker(index, kind) { chosen ->
                    segments = segments.toMutableList().also { it[index] = chosen }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { revealed = false; answers.clear() },
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("מהתחלה, עם שאלות אחרות")
                }
            }
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
}

@Composable
private fun QuestionLeaf(
    number: Int,
    q: FpQuestion,
    answer: String?,
    onAnswer: (String) -> Unit
) {
    Leaf {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(ltr("$number"))
            Spacer(Modifier.width(10.dp))
            Text(q.text, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(10.dp))
        when (q.kind) {
            FpKind.SHORT_TEXT -> OutlinedTextField(
                value = answer.orEmpty(),
                onValueChange = onAnswer,
                singleLine = true,
                placeholder = { Text("תשובה קצרה") },
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            )
            FpKind.YES_NO -> ChoiceRow(listOf("כן", "לא"), answer, onAnswer)
            FpKind.CHOICE -> ChoiceRow(q.options, answer, onAnswer)
            // בלי שדה. אם יש איפה להקליד, הבדיחה מתה.
            FpKind.SECRET -> Aside("יופי. אל תגלה.")
        }
    }
}

@Composable
private fun ChoiceRow(options: List<String>, answer: String?, onAnswer: (String) -> Unit) {
    Column {
        options.forEach { option ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(option == answer) { onAnswer(option) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = option == answer, onClick = { onAnswer(option) })
                Spacer(Modifier.width(6.dp))
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun CountStepper(count: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { onChange((count - 1).coerceAtLeast(1)) },
            enabled = count > 1,
            shape = MaterialTheme.shapes.small
        ) { Text("−") }
        Spacer(Modifier.width(16.dp))
        Text(ltr("$count"), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(16.dp))
        OutlinedButton(
            onClick = { onChange((count + 1).coerceAtMost(24)) },
            enabled = count < 24,
            shape = MaterialTheme.shapes.small
        ) { Text("+") }
    }
}

@Composable
private fun SegmentPicker(index: Int, kind: SegmentKind, onPick: (SegmentKind) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Pill(ltr("${index + 1}"))
            Spacer(Modifier.width(10.dp))
            Text(
                kind.label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Box {
                OutlinedButton(onClick = { open = true }, shape = MaterialTheme.shapes.small) {
                    Text("החלף")
                }
                // רשימה אחת רצופה, בלי חלוקה לקטגוריות: מי שמחפש משהו מסוים
                // סורק את השמות, והכותרות רק הוסיפו עצירות באמצע.
                DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                    SegmentKind.entries.forEach { k ->
                        DropdownMenuItem(
                            text = { Text(k.label) },
                            onClick = { onPick(k); open = false }
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
