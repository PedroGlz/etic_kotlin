package com.example.etic.core.current

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.etic.data.local.DbProvider
import com.example.etic.data.local.queries.CurrentInspectionInfo
import com.example.etic.data.local.queries.getCurrentInspectionInfo

// Proveedor global inmutable: se carga una sola vez desde BD y se cachea en memoria.
object CurrentInspectionProvider {
    @Volatile private var cached: CurrentInspectionInfo? = null

    fun get(context: Context): CurrentInspectionInfo? {
        val c = cached
        if (c != null) return c
        synchronized(this) {
            val again = cached
            if (again != null) return again
            val db = DbProvider.get(context)
            val info = runCatching { getCurrentInspectionInfo(db) }.getOrNull()
            cached = info
            return info
        }
    }
}

val LocalCurrentInspection = staticCompositionLocalOf<CurrentInspectionInfo?> { null }

@Composable
fun ProvideCurrentInspection(content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    var state by remember { mutableStateOf<CurrentInspectionInfo?>(null) }

    LaunchedEffect(Unit) {
        // Carga única; llamadas posteriores usan cache en memoria
        state = CurrentInspectionProvider.get(ctx)
    }

    CompositionLocalProvider(LocalCurrentInspection provides state) {
        content()
    }
}

