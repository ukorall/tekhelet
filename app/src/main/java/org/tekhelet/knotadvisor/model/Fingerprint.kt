package org.tekhelet.knotadvisor.model

/**
 * "טביעת אצבע" - קיצור דרך למסקנה אישית סופית בלי כל החפירות.
 *
 * חמש שאלות אקראיות שאין להן שום קשר לתכלת, ומהן נגזר גדיל. זה **לא** כלי
 * החלטה, וזו כל הבדיחה: מי שרוצה תשובה בלי לעבור את התהליך יקבל תשובה שמבוססת
 * על "אם היית חיה, איזה צבע היית אוהב לאכול".
 *
 * הרעיון הרציני היחיד כאן הוא הדטרמיניזם: הזרע נגזר מהתשובות, ולכן אותן תשובות
 * תמיד נותנות אותו גדיל. בלי זה זו לא טביעת אצבע אלא כפתור "הפתע אותי".
 *
 * המאגר מחולק לשלושה טעמים בכוונה - ישיבתי, רנדומלי, ונונסנס טהור - וכל הרצה
 * מגרילה מכולם.
 */
enum class FpKind { CHOICE, YES_NO, SHORT_TEXT }

data class FpQuestion(
    val id: String,
    val text: String,
    val kind: FpKind,
    val options: List<String> = emptyList()
)

object FingerprintQuestions {

    private fun choice(id: String, text: String, vararg options: String) =
        FpQuestion(id, text, FpKind.CHOICE, options.toList())

    private fun yesNo(id: String, text: String) = FpQuestion(id, text, FpKind.YES_NO)
    private fun open(id: String, text: String) = FpQuestion(id, text, FpKind.SHORT_TEXT)

    /** ישיבתי. */
    val beitMidrash = listOf(
        yesNo("y_zvachim", "זבחים, כבר למדת?"),
        choice(
            "y_migo", "מה זה מיגו?",
            "כוח טענה", "נאמנות", "סתם טריק של חז\"ל", "תלוי את מי שואלים"
        ),
        open("y_mango", "מה מברכים על מנגו?"),
        choice(
            "y_seder", "איזה סדר הכי אהוב עליך?",
            "זרעים", "מועד", "נשים", "נזיקין", "קדשים", "טהרות"
        ),
        choice(
            "y_chavruta", "חברותא מדברת יותר מדי. מה עושים?",
            "מדברים איתה על זה", "עוברים חברותא", "מצטרפים אליה", "שותקים עוד שנה"
        ),
        yesNo("y_tosafot", "אתה קורא את התוספות לפני הגמרא?"),
        choice(
            "y_shiur", "כזית - לפי מי?",
            "החזון איש", "הרב חיים נאה", "מה שיש בצלחת", "לא נכנסתי לזה"
        ),
        open("y_kasha", "תגיד קושיה אחת שאתה עדיין חייב עליה תשובה")
    )

    /** רנדומלי - עליך, לא על הלימוד. */
    val personal = listOf(
        open("p_friends", "איזו דמות אתה מחברים?"),
        open("p_miluim", "כמה ימי מילואים עשית?"),
        choice("p_coffee", "קפה?", "שחור", "הפוך", "נס", "לא שותה"),
        choice(
            "p_wake", "מתי אתה קם?",
            "לפני הנץ", "עם השמש", "כשחייבים", "שאלה מעליבה"
        ),
        open("p_city", "באיזו עיר היית רוצה לגור, גם אם לא תגור בה?"),
        yesNo("p_maps", "אתה משתמש בוויז גם לנסיעות שאתה מכיר?"),
        choice(
            "p_shabbat", "סעודה שלישית",
            "הדבר הכי טוב בשבוע", "בעיקר שינה", "תלוי מי מארח", "מה זה"
        ),
        open("p_song", "שיר אחד שאתה שם כשאף אחד לא רואה")
    )

    /** נונסנס טהור. אין כאן שום ניסיון להסיק משהו. */
    val nonsense = listOf(
        open("n_number", "תבחר מספר ואל תגלה לי"),
        open("n_animal", "אם היית חיה, איזה צבע היית אוהב לאכול?"),
        choice(
            "n_door", "דלת או חלון?",
            "דלת", "חלון", "המשקוף", "אני בחוץ"
        ),
        open("n_tuesday", "כמה שוקל יום שלישי?"),
        choice(
            "n_soup", "המרק צוחק עליך. אתה?",
            "צוחק בחזרה", "מוסיף מלח", "קם והולך", "זה לא מרק"
        ),
        yesNo("n_ladder", "הסולם יודע?"),
        open("n_shape", "תאר ריח בעזרת צורה גיאומטרית"),
        choice(
            "n_seven", "שבע.",
            "כן", "לא", "בערך", "העדפתי שש"
        )
    )

    /**
     * חמש שאלות: שתיים ישיבתיות, שתיים אישיות, ואחת נונסנס. התמהיל קבוע כדי
     * שכל הרצה תרגיש דומה באופייה, גם כשהשאלות עצמן משתנות.
     */
    fun draw(random: kotlin.random.Random): List<FpQuestion> =
        beitMidrash.shuffled(random).take(2) +
            personal.shuffled(random).take(2) +
            nonsense.shuffled(random).take(1)
}
