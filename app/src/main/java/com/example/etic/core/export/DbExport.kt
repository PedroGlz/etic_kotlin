package com.example.etic.core.export

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.Normalizer

data class ExportResult(val success: Boolean, val message: String)

private object DbExportCoordinator {
    val mutex = Mutex()
}

suspend fun exportRoomDbToDownloads(
    context: Context,
    dbName: String = "etic.db",
    exportFileName: String? = null
): ExportResult = withContext(Dispatchers.IO) {
    DbExportCoordinator.mutex.withLock {
        val tempExport = File(context.cacheDir, "${dbName.substringBeforeLast('.')}_export_snapshot.db")
        try {
            if (tempExport.exists()) {
                tempExport.delete()
            }

            val roomDb = DbProvider.get(context)
            val sqliteDb = roomDb.openHelper.writableDatabase
            val journalMode = sqliteDb.stringPragma("journal_mode").orEmpty().lowercase()

            // Asegura que los cambios confirmados previos al inicio de la exportacion
            // ya sean visibles para el snapshot.
            sqliteDb.stringPragma("busy_timeout", "5000")
            if (journalMode == "wal") {
                sqliteDb.checkpointWal("FULL")
            }

            val snapshotResult = createSnapshotWithVacuumInto(sqliteDb, tempExport)
                ?: createSnapshotWithFileCopy(context, dbName, sqliteDb, journalMode, tempExport)

            if (snapshotResult != null) {
                return@withLock snapshotResult
            }

            validateSnapshot(tempExport)

            val finalFileName = exportFileName?.takeIf { it.isNotBlank() }
                ?: "${dbName.substringBeforeLast('.')}_export_${System.currentTimeMillis()}.db"
            val destinationMessage = saveSnapshotToDownloads(context, tempExport, finalFileName)

            ExportResult(
                success = true,
                message = "$destinationMessage | journal_mode=${journalMode.ifBlank { "desconocido" }}"
            )
        } catch (e: SecurityException) {
            ExportResult(false, "Permiso denegado: ${e.message}")
        } catch (e: Exception) {
            ExportResult(false, "Error al exportar: ${e.message}")
        } finally {
            if (tempExport.exists()) {
                tempExport.delete()
            }
        }
    }
}

fun buildInspectionExportFileName(
    inspectionNumber: String?,
    siteName: String?
): String {
    val safeInspection = inspectionNumber?.trim().takeUnless { it.isNullOrBlank() } ?: "SIN_NUMERO"
    val normalizedSite = siteName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { raw ->
            val withoutAccents = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replace(Regex("\\p{Mn}+"), "")
            withoutAccents
                .uppercase()
                .replace(Regex("[^A-Z0-9]+"), "_")
                .trim('_')
                .ifBlank { "SITIO" }
        }
        ?: "SITIO"
    return "ETIC_${safeInspection}_${normalizedSite}_INSPECCIONADA.db"
}

private fun createSnapshotWithVacuumInto(
    sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
    outputFile: File
): ExportResult? {
    return try {
        val escapedPath = outputFile.absolutePath.replace("'", "''")
        sqliteDb.execSQL("VACUUM INTO '$escapedPath'")
        null
    } catch (_: Exception) {
        null
    }
}

private fun createSnapshotWithFileCopy(
    context: Context,
    dbName: String,
    sqliteDb: androidx.sqlite.db.SupportSQLiteDatabase,
    journalMode: String,
    outputFile: File
): ExportResult? {
    return try {
        if (journalMode == "wal") {
            sqliteDb.checkpointWal("TRUNCATE")
        }
        val sourceDb = context.getDatabasePath(dbName)
        if (sourceDb == null || !sourceDb.exists()) {
            return ExportResult(false, "No se encontro la base de datos local.")
        }
        FileInputStream(sourceDb).use { input ->
            FileOutputStream(outputFile).use { output ->
                input.copyTo(output, 8 * 1024)
                output.fd.sync()
            }
        }
        null
    } catch (e: Exception) {
        ExportResult(false, "No se pudo generar el respaldo: ${e.message}")
    }
}

private fun validateSnapshot(snapshot: File) {
    require(snapshot.exists()) { "No se genero el archivo de respaldo." }
    require(snapshot.length() > 0L) { "El respaldo generado esta vacio." }

    val exportedDb = SQLiteDatabase.openDatabase(
        snapshot.absolutePath,
        null,
        SQLiteDatabase.OPEN_READONLY
    )
    try {
        val integrity = exportedDb.rawQuery("PRAGMA integrity_check(1)", null).use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }
        require(integrity.equals("ok", ignoreCase = true)) {
            "La copia exportada no paso integrity_check: ${integrity ?: "sin resultado"}"
        }

        val objectCount = exportedDb.rawQuery(
            "SELECT COUNT(*) FROM sqlite_master WHERE type IN ('table','view')",
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else 0
        }
        require(objectCount > 0) { "La copia exportada no contiene objetos SQLite validos." }
    } finally {
        exportedDb.close()
    }
}

private fun saveSnapshotToDownloads(
    context: Context,
    snapshot: File,
    fileName: String
): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = context.contentResolver
        val uri: Uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("No se pudo crear el archivo de exportacion en Descargas.")
        resolver.openOutputStream(uri)?.use { out ->
            FileInputStream(snapshot).use { input ->
                input.copyTo(out, 8 * 1024)
            }
        } ?: error("No se pudo abrir el OutputStream de exportacion.")
        "Guardado en Descargas como $fileName"
    } else {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val output = File(downloadsDir, fileName)
        FileInputStream(snapshot).use { input ->
            FileOutputStream(output).use { out ->
                input.copyTo(out, 8 * 1024)
                out.fd.sync()
            }
        }
        "Guardado en ${output.absolutePath}"
    }
}

private fun androidx.sqlite.db.SupportSQLiteDatabase.stringPragma(
    pragma: String,
    value: String? = null
): String? {
    val sql = if (value == null) {
        "PRAGMA $pragma"
    } else {
        "PRAGMA $pragma = $value"
    }
    return query(sql).use { cursor ->
        if (cursor.moveToFirst() && cursor.columnCount > 0) {
            cursor.getString(0)
        } else {
            null
        }
    }
}

private fun androidx.sqlite.db.SupportSQLiteDatabase.checkpointWal(mode: String) {
    query("PRAGMA wal_checkpoint($mode)").use { /* cierre explicito del cursor */ }
}
