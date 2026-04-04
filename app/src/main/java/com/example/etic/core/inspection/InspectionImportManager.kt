package com.example.etic.core.inspection

import android.content.Context
import android.net.Uri
import android.database.sqlite.SQLiteDatabase
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

private fun isValidSqliteFile(file: File): Boolean {
    if (!file.exists() || file.length() <= 0L) return false

    val headerOk = runCatching {
        file.inputStream().use { input ->
            val header = ByteArray(16)
            val read = input.read(header)
            read == 16 && String(header, Charsets.US_ASCII) == "SQLite format 3\u0000"
        }
    }.getOrDefault(false)
    if (!headerOk) return false

    return runCatching {
        SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            val integrity = db.rawQuery("PRAGMA integrity_check(1)", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
            integrity.equals("ok", ignoreCase = true)
        }
    }.getOrDefault(false)
}

suspend fun importInspectionDatabase(
    context: Context,
    sourceUri: Uri
): InspectionImportResult {
    return withContext(Dispatchers.IO) {
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

            if (!isValidSqliteFile(tempFile)) {
                tempFile.delete()
                return@withContext InspectionImportResult(
                    success = false,
                    message = "El archivo seleccionado no es una base de datos SQLite valida."
                )
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
