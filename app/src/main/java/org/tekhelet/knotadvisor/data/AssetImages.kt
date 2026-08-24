package org.tekhelet.knotadvisor.data

import android.content.Context

/**
 * מאתר תמונות קשירה בזמן ריצה מתוך assets/images/<methodId>/ - התיקייה הזו מתמלאת
 * בזמן ה-build מתוך content/images/<methodId>/ שבשורש הריפו (ראו content/README.md
 * ו-syncContentImages ב-app/build.gradle.kts). כלומר: מוסיפים תמונה על ידי הוספת
 * קובץ לתיקייה הנכונה ב-content/images/ - בלי לערוך JSON או קוד בכלל.
 */
object AssetImages {
    fun imagesFor(context: Context, methodId: String): List<String> =
        context.assets.list("images/$methodId")
            ?.filterNot { it.startsWith(".") }
            ?.sorted()
            ?: emptyList()

    fun assetUri(methodId: String, fileName: String): String =
        "file:///android_asset/images/$methodId/$fileName"
}
