package com.example.etic.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore
import com.example.etic.features.auth.LoginScreen
import com.example.etic.features.home.MainScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = remember { SessionManager(context.sessionDataStore) }
    val scope = rememberCoroutineScope()

    // Leer una vez si el usuario ya estaba logueado
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val loggedIn = session.isLoggedIn.firstOrNull() ?: false
        startDestination = if (loggedIn) "home" else "login"
    }

    // Mientras obtenemos la sesión, no dibujamos el NavHost
    val resolvedStart = startDestination ?: return

    NavHost(
        navController = navController,
        startDestination = resolvedStart
    ) {
        composable("login") {
            LoginScreen(
                onLogin = { _ ->
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            MainScreen(
                userName = "Usuario",
                onLogout = {
                    // Limpiar sesión y volver a login
                    scope.launch {
                        session.clear()
                        navController.navigate("login") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
