package org.tekhelet.knotadvisor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import org.tekhelet.knotadvisor.ui.navigation.AppNavHost
import org.tekhelet.knotadvisor.ui.theme.KnotAdvisorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KnotAdvisorTheme(useDarkTheme = isSystemInDarkTheme()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()
                }
            }
        }
    }
}
