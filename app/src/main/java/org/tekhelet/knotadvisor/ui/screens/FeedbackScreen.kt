package org.tekhelet.knotadvisor.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.model.FeedbackKind
import org.tekhelet.knotadvisor.ui.AppViewModel
import org.tekhelet.knotadvisor.ui.components.GadilRule

@Composable
fun FeedbackScreen(viewModel: AppViewModel, fromScreen: String) {
    val context = LocalContext.current
    var kind by remember { mutableStateOf(FeedbackKind.GENERAL) }
    var text by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf<Int?>(null) }
    var saved by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("יש לך הערה בשבילי?", style = MaterialTheme.typography.headlineSmall)
        GadilRule(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "כל הערה עוזרת - טעות בתוכן, ניסוח מבלבל, משהו שחסר, או סתם מחשבה. " +
                "אחרי ששולחים, נוצר קובץ קטן שאפשר לשלוח לי בוואטסאפ או במייל.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(18.dp))
        Text("על מה מדובר", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        FeedbackKind.entries.forEach { k ->
            Row(
                Modifier.fillMaxWidth().selectable(kind == k) { kind = k }.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = kind == k, onClick = { kind = k })
                Spacer(Modifier.width(6.dp))
                Text(k.label, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("מה רצית לומר") },
            minLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(14.dp))
        Text("ואם בא לך לדרג (לא חובה)", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        Row {
            (1..5).forEach { n ->
                FilterChip(
                    selected = rating == n,
                    onClick = { rating = if (rating == n) null else n },
                    label = { Text("$n") },
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                viewModel.submitFeedback(kind, text, rating, fromScreen, null) { file ->
                    saved = true
                    val intent = viewModel.feedbackShareIntent(file)
                    context.startActivity(Intent.createChooser(intent, "לשלוח לאוריאל"))
                }
            },
            enabled = text.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("שלח") }

        if (saved) {
            Spacer(Modifier.height(10.dp))
            Text("נשמר. תודה!", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "מה נשלח חוץ מהטקסט: גרסת האפליקציה, המסך שממנו שלחת, ודגם המכשיר. " +
                "זה מה שמאפשר לי להבין על מה בדיוק מדובר בלי לשאול אותך חזרה. " +
                "שום דבר לא נשלח לשום מקום מעצמו - הקובץ נשאר אצלך עד שאתה בוחר לשלוח אותו.",
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.height(24.dp))
    }
}
