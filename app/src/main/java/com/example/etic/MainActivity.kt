// Kotlin
package com.example.etic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.etic.ui.theme.EticTheme
import com.example.etic.features.auth.LoginScreen
import com.example.etic.features.home.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EticTheme {
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
