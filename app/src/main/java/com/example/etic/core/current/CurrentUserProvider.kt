package com.example.etic.core.current

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.flow.first

data class CurrentUserInfo(
    val idUsuario: String,
    val usuario: String?,
    val nombre: String?
)

object CurrentUserProvider {
    @Volatile private var cached: CurrentUserInfo? = null

    suspend fun load(context: Context): CurrentUserInfo? {
        val existing = cached
        if (existing != null) return existing
        val session = SessionManager(context.sessionDataStore)
        val username = runCatching { session.username.first() }.getOrNull()
        if (username.isNullOrBlank()) return null
        val db = DbProvider.get(context)
        val usr = runCatching { db.usuarioDao().getByUsuario(username) }.getOrNull()
        val info = usr?.idUsuario?.let { id -> CurrentUserInfo(id, usr.usuario, usr.nombre) }
        cached = info
        return info
    }
}

val LocalCurrentUser = staticCompositionLocalOf<CurrentUserInfo?> { null }

@Composable
fun ProvideCurrentUser(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    var state by remember { mutableStateOf<CurrentUserInfo?>(null) }

    LaunchedEffect(Unit) {
        state = CurrentUserProvider.load(ctx)
    }

    CompositionLocalProvider(LocalCurrentUser provides state) {
        content()
    }
}

