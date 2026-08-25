package org.tekhelet.knotadvisor.model

import kotlinx.serialization.Serializable

/**
 * "המסע" - מעבר מסודר בין כל התחנות, לפי הסדר, עם אפשרות לעצור באמצע ולחזור
 * בדיוק לאותה נקודה. זו האלטרנטיבה למי שלא יודע מאיפה להתחיל ומעדיף שילוו אותו.
 */
@Serializable
enum class JourneyStation(
    val topic: Topic,
    val title: String,
    val blurb: String
) {
    WHETHER(Topic.WHETHER, "האם להטיל תכלת", "מתחילים מהשאלה הבסיסית ביותר, כולל זיהוי החילזון."),
    HOW_MANY(Topic.HOW_MANY, "כמה חוטים", "אחרי שהחלטת להטיל - כמה מתוך שמונת החוטים יהיו תכלת."),
    HOW(Topic.HOW, "לפי איזו שיטה לקשור", "השאלון שממפה מה חשוב לך, ומציע שיטות מתאימות."),
    TYING_GUIDE(Topic.TYING_GUIDE, "מה לקנות ואיך לקשור", "מהחלטה למעשה: איזה מוצר מתאים, ואיך קושרים אותו."),
    EXTRAS(Topic.EXTRAS, "עיון נוסף", "סוגיות שכדאי לפתוח אחרי שכבר קשרת.");

    val index: Int get() = entries.indexOf(this)
    fun next(): JourneyStation? = entries.getOrNull(index + 1)
    fun previous(): JourneyStation? = entries.getOrNull(index - 1)

    companion object {
        val ordered: List<JourneyStation> = entries.toList()
    }
}

/** מצב המסע השמור - איפה עצרנו, ומה כבר הושלם. */
@Serializable
data class JourneyState(
    val active: Boolean = false,
    val currentStation: JourneyStation = JourneyStation.WHETHER,
    val completed: Set<JourneyStation> = emptySet()
) {
    val progress: Float
        get() = completed.size.toFloat() / JourneyStation.ordered.size
}
