package com.example.etic.core.inspection

import android.content.Context
import android.net.Uri
import com.example.etic.core.current.CurrentInspectionProvider
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class InspectionImportResult(
    val success: Boolean,
    val message: String
)

private fun Uri.displayNameFallback(): String {
    val raw = lastPathSegment.orEmpty()
    return raw.substringAfterLast('/').substringAfterLast(':')
}

suspend fun importInspectionDatabase(
    context: Context,
    sourceUri: Uri
): InspectionImportResult {
    return withContext(Dispatchers.IO) {
        val sourceName = sourceUri.displayNameFallback().lowercase()
        if (!sourceName.endsWith(".db")) {
            return@withContext InspectionImportResult(
                success = false,
                message = "Archivo no valido. Selecciona un respaldo .db"
            )
        }

        val input = runCatching {
            context.contentResolver.openInputStream(sourceUri)
        }.getOrNull() ?: return@withContext InspectionImportResult(
            success = false,
            message = "No se pudo abrir el archivo seleccionado."
        )

        try {
            DbProvider.closeAndReset()

            val dbFile = context.getDatabasePath("etic.db")
            val dbDir = dbFile.parentFile
            if (dbDir != null && !dbDir.exists()) {
                dbDir.mkdirs()
            }

            File(dbFile.absolutePath + "-wal").delete()
            File(dbFile.absolutePath + "-shm").delete()

            val tempFile = File.createTempFile("etic_import_", ".db", context.cacheDir)
            input.use { source ->
                FileOutputStream(tempFile).use { output ->
                    source.copyTo(output)
                }
            }

            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()

            DbProvider.closeAndReset()
            CurrentInspectionProvider.invalidate()

            InspectionImportResult(
                success = true,
                message = "Inspección importada correctamente."
            )
        } catch (e: Exception) {
            InspectionImportResult(
                success = false,
                message = "Error al importar: ${e.message}"
            )
        }
    }
}
