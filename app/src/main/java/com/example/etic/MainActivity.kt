// Kotlin
package com.example.etic

import android.os.Bundle
import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import com.example.etic.ui.theme.EticTheme
import com.example.etic.ui.theme.FontSizeOption
import com.example.etic.features.auth.LoginScreen
import com.example.etic.features.home.MainScreen
import com.example.etic.core.session.FontPrefs
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        enableEdgeToEdge()
        setContent {
            val appContext = LocalContext.current.applicationContext
            val fontSize by remember(appContext) { FontPrefs.fontSizeFlow(appContext) }
                .collectAsState(initial = FontSizeOption.Large)
            val scope = rememberCoroutineScope()

            EticTheme(fontSizeOption = fontSize) {
                var loggedIn by remember { mutableStateOf(false) }
                var userName by remember { mutableStateOf("") }

                if (!loggedIn) {
                    LoginScreen { user ->
                        userName = user
                        loggedIn = true
                    }
                } else {
                    MainScreen(
                        userName = userName,
                        currentFontSize = fontSize,
                        onChangeFontSize = { option ->
                            scope.launch { FontPrefs.setFontSize(appContext, option) }
                        },
                        onLogout = {
                            // logout
                            userName = ""
                            loggedIn = false
                        }
                    )
                }
            }
        }
    }
}
