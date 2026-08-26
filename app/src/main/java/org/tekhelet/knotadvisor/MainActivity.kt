package org.tekhelet.knotadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import org.tekhelet.knotadvisor.ui.AppViewModel
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
                // ה-ViewModel נוצר כאן ולא בתוך AppNavHost, כי מצב התאורה שמור בו
                // והתמה צריכה לקרוא אותו. מצב התאורה הוא בחירה מפורשת של המשתמש
                // ולא נגזר מהגדרת המערכת - "מבצעי" הוא לא "מצב לילה".
                val viewModel: AppViewModel = viewModel()
                KnotAdvisorTheme(mode = viewModel.mode) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavHost(viewModel)
                    }
                }
            }
        }
    }
}
