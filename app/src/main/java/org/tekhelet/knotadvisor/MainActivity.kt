package org.tekhelet.knotadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.tekhelet.knotadvisor.ui.navigation.AppNavHost
import org.tekhelet.knotadvisor.ui.theme.KnotAdvisorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // כל האפליקציה בעברית, ולכן כיוון הפריסה נכפה ל-RTL ולא נגזר מהגדרת
            // השפה של המכשיר. בלי זה, חצי הניווט, הריפוד והיישור מתהפכים אצל מי
            // שהטלפון שלו מוגדר באנגלית - וזה המצב אצל לא מעט אנשים.
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                KnotAdvisorTheme(useDarkTheme = isSystemInDarkTheme()) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavHost()
                    }
                }
            }
        }
    }
}
