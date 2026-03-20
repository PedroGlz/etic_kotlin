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
import com.example.etic.core.current.CurrentInspectionProvider
import com.example.etic.core.current.CurrentUserProvider
import com.example.etic.ui.theme.EticTheme
import com.example.etic.ui.theme.FontSizeOption
import com.example.etic.features.auth.LoginScreen
import com.example.etic.features.home.MainScreen
import com.example.etic.core.session.FontPrefs
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull

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
            val session = remember { SessionManager(appContext.sessionDataStore) }
            var loggedIn by remember { mutableStateOf(false) }
            var userName by remember { mutableStateOf("") }

            EticTheme(fontSizeOption = fontSize) {
                LaunchedEffect(Unit) {
                    val restoredLogin = session.isLoggedIn.firstOrNull() ?: false
                    val restoredUser = session.username.firstOrNull().orEmpty()
                    loggedIn = restoredLogin
                    userName = if (restoredLogin) restoredUser else ""
                    CurrentInspectionProvider.invalidate()
                    CurrentUserProvider.invalidate()
                }

                if (!loggedIn) {
                    LoginScreen { user ->
                        CurrentInspectionProvider.invalidate()
                        CurrentUserProvider.invalidate()
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
                            scope.launch {
                                session.clear()
                                CurrentInspectionProvider.invalidate()
                                CurrentUserProvider.invalidate()
                                userName = ""
                                loggedIn = false
                            }
                        }
                    )
                }
            }
        }
    }
}
