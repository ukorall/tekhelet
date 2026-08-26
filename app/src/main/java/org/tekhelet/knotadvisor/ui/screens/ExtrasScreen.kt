package org.tekhelet.knotadvisor.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.tekhelet.knotadvisor.ui.components.GadilRule

private data class Entry(val title: String, val body: String, val link: String? = null)

/**
 * "עיון נוסף והרחבות" - סוגיות שכדאי לפתוח, שאלות נפוצות, וחומר לעיון.
 * זה גם המקום של מצב הלימוד.
 */
@Composable
fun ExtrasScreen(onOpenLibrary: () -> Unit, onOpenTree: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("עיון נוסף והרחבות", style = MaterialTheme.typography.headlineSmall)
        GadilRule(modifier = Modifier.padding(top = 10.dp, bottom = 12.dp))
        Text(
            "דברים שלא נכנסים לתהליך ההחלטה עצמו, אבל שווה להכיר. חלקם סוגיות " +
                "שאני עצמי עדיין חוכך בהן.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), onClick = onOpenLibrary) {
            Column(Modifier.padding(14.dp)) {
                Text("ספריית השיטות", style = MaterialTheme.typography.titleSmall)
                Text("לעבור על כל השיטות בלי לעבור שאלון", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), onClick = onOpenTree) {
            Column(Modifier.padding(14.dp)) {
                Text("מפת השיטות", style = MaterialTheme.typography.titleSmall)
                Text("התרשים שלי, בגרסה שאפשר לנווט בה", style = MaterialTheme.typography.bodySmall)
            }
        }

        Text("סוגיות שכדאי לפתוח", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        entries.forEach { ExpandableEntry(it) }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ExpandableEntry(entry: Entry) {
    var open by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), onClick = { open = !open }) {
        Column(Modifier.padding(14.dp)) {
            Text(entry.title, style = MaterialTheme.typography.titleSmall)
            AnimatedVisibility(open) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    Text(entry.body, style = MaterialTheme.typography.bodyMedium)
                    entry.link?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private val entries = listOf(
    Entry(
        "גרדומי תכלת - כשהציצית נקרעת",
        "בשו\"ע ובמשנ\"ב נפסקו הלכות לציציות שנקרעו, ולמסקנה נפסק שם שילוב מסוים של " +
            "שיטות ר\"ת והרא\"ש. הבעיה: שיטת ר\"ת מבוססת באופן מובהק על שיטתו במספר חוטי " +
            "התכלת (ר\"ת הוא תוספות). אז מה קורה עם מי שנוהג כרמב\"ם או כראב\"ד? הב\"י מביא " +
            "שלוש צורות להבין את פסיקת הרמב\"ם בנושא. ואולי בכלל צריך להחמיר גם כשו\"ע וגם " +
            "כשיטה שנוהגים בה? אני עדיין חוכך בשאלות האלה ומעיין בהן - אתה מוזמן לבוא ולהאיר את עיניי.",
        "מקורות: שו\"ע או\"ח סי' י\"ב, ומשנ\"ב שם"
    ),
    Entry(
        "מנהג הלבן לא רלוונטי לתכלת",
        "זו נקודה שמפתיעה הרבה אנשים. הפוסקים מציינים במפורש שהמנהג המקובל בהטלת ציצית " +
            "אינו כפי דין הגמרא - כי דין הגמרא נאמר לציצית של תכלת, ובציצית של לבן נוצרו " +
            "מנהגים אחרים אחרי שהתכלת נגנזה. כלומר: דווקא עכשיו, כשחוזרים לתכלת, צריך " +
            "לחזור לדין הגמרא ולא להמשיך את מה שהתרגלנו אליו.",
        "הרב שמואל אריאל, \"התכלת וחידושה\", פרק ז"
    ),
    Entry(
        "מתחילים בלבן ומסיימים בלבן",
        "הדבר היחיד שאני באמת מתעקש עליו. \"תנא, כשהוא מתחיל - מתחיל בלבן, 'הכנף' מין כנף; " +
            "וכשהוא מסיים - מסיים בלבן, מעלין בקודש ולא מורידין\". זו גמרא מפורשת וחד משמעית, " +
            "והרבה אנשים טועים בה - במיוחד מי שקושר כמו שהוא רגיל בלבן ופשוט מחליף את הצבע. " +
            "לא משנה באיזה הרכב בחרת: הליפוף הראשון והאחרון בלבן.",
        "מנחות ל\"ט ע\"א"
    ),
    Entry(
        "חוליה תימנית או רגילה",
        "יש היום לא מעט אשכנזים וספרדים שעברו לחוליות תימניות. שאלתי על זה, והתשובה של " +
            "הרב שמואל אריאל היתה שאין סיבה לעשות כך - צורת החוליה צריכה להישאר כפי שנהגו " +
            "קודם, לתימנים כמנהגם ולשאר העדות כמנהגן. הוא מסביר שהמעבר נבע משתי סיבות: " +
            "שממילא צריך לשנות דברים בתכלת אז עיינו בפוסקים והכניסו גם את זה, ושמי שכורך " +
            "הכול בתכלת צריך איכשהו להפריד בין חוליה לחוליה. לדעתו שתיהן לא נכונות - " +
            "מפרידים בקשרים כפולים, כמו שעשו האבות. הרב רפמן מסכים ומוסיף נימוקים.",
        "שו\"ת בעניין צורת החוליות; \"שער כריכת התכלת\" פרקים י\"ג-י\"ד"
    ),
    Entry(
        "חמישה קשרים - עד כמה זה מחייב",
        "המנהג לעשות חמישה קשרים נזכר בזוהר, בתרגום יונתן ובמדרש תנחומא. הרב רפמן מחמיר " +
            "בזה מאוד וכותב שגם מי שנוהג כרמב\"ם וגם בני תימן צריכים לעשותם בזמן שיש תכלת, " +
            "ומי שלא נקרא בפי הפוסקים \"עובר על דברי חכמים\". מנגד, הרב אריאל מונה את זה " +
            "כאחד מארבעה כיווני פתרון, ומזכיר שהרמב\"ם לא פסק את דברי המדרש כלל.",
        "\"שער כריכת התכלת\" פרק ח'"
    ),
    Entry(
        "שליש גדיל ושני שליש ענף",
        "דין גמרא: אורך הגדיל (הקשרים והכריכות) הוא שליש, והענף (החוטים החופשיים) שני שליש. " +
            "בנוסף צריך שהציצית כולה תהיה לפחות י\"ב אגודלים. יש שיטות שאם קצרה מזה היא " +
            "פסולה, כי דין גרדומין נאמר רק כשנעשתה בכשרות מלכתחילה. זה רלוונטי מאוד " +
            "כשקונים - ראו את החישוב באזור \"איך בפועל\".",
        "מנחות ל\"ט ע\"א"
    ),
    Entry(
        "עוד להשלמה",
        "כאן צריכים להיכנס עוד: שאלות נפוצות שחוזרות אצל אנשים, סוגיות שצריך לבדוק אם " +
            "ההלכה בהן זהה לציצית בלי תכלת, והפניות למאמרים המלאים - בקישור אם הם ברשת, " +
            "או כקובץ בתוך האפליקציה אם לא."
    )
)
