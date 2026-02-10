package com.example.etic.core.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.etic.data.local.DbProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ExportResult(val success: Boolean, val message: String)

fun exportRoomDbToDownloads(context: Context, dbName: String = "etic.db"): ExportResult {
    return try {
        // Fuerza checkpoint del WAL para que los últimos cambios estén en el archivo principal
        try {
            val db = DbProvider.get(context).openHelper.writableDatabase
            db.query("PRAGMA wal_checkpoint(FULL)").use { /* cierra cursor */ }
        } catch (_: Exception) {
            // Si no se puede, seguiremos y copiaremos -wal/-shm si existen
        }
        // Asegurar consolidación del WAL y liberar lectores antes de copiar
        try {
            val room = DbProvider.get(context)
            val db2 = room.openHelper.writableDatabase
            db2.query("PRAGMA wal_checkpoint(TRUNCATE)").use { /* close cursor */ }
            DbProvider.closeAndReset()
        } catch (_: Exception) {
            // Continuar en fallback si falla
        }
        val src = context.getDatabasePath(dbName)
        if (src == null || !src.exists()) {
            return ExportResult(false, "No se encontró la base de datos: $dbName")
        }
        val fileName = "${dbName.substringBeforeLast('.')}_export_${System.currentTimeMillis()}.db"
        val walFile = File(src.parentFile, src.name + "-wal")
        val shmFile = File(src.parentFile, src.name + "-shm")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val resolver = context.contentResolver
            val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            if (uri == null) return ExportResult(false, "No se pudo crear archivo en Descargas")
            resolver.openOutputStream(uri).use { out ->
                FileInputStream(src).use { input ->
                    input.copyTo(out!!, bufferSize = 8 * 1024)
                }
            }
            // Copiar -wal y -shm si tienen datos pendientes
            var extras = ""
            if (walFile.exists() && walFile.length() > 0) {
                val walValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName + "-wal")
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, walValues)?.let { walUri ->
                    context.contentResolver.openOutputStream(walUri).use { outWal ->
                        FileInputStream(walFile).use { it.copyTo(outWal!!, 8 * 1024) }
                    }
                    extras += ", $fileName-wal"
                }
            }
            if (shmFile.exists() && shmFile.length() > 0) {
                val shmValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName + "-shm")
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, shmValues)?.let { shmUri ->
                    context.contentResolver.openOutputStream(shmUri).use { outShm ->
                        FileInputStream(shmFile).use { it.copyTo(outShm!!, 8 * 1024) }
                    }
                    extras += ", $fileName-shm"
                }
            }
            ExportResult(true, "Guardado en Descargas como $fileName" + if (extras.isNotEmpty()) " (incluye$extras)" else " (consolidado sin WAL)")
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, fileName)
            FileInputStream(src).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output, bufferSize = 8 * 1024)
                }
            }
            // Copiar -wal y -shm si tienen datos pendientes
            var extras = ""
            if (walFile.exists() && walFile.length() > 0) {
                val walDest = File(dir, fileName + "-wal")
                FileInputStream(walFile).use { input ->
                    FileOutputStream(walDest).use { output -> input.copyTo(output, 8 * 1024) }
                }
                extras += ", ${walDest.name}"
            }
            if (shmFile.exists() && shmFile.length() > 0) {
                val shmDest = File(dir, fileName + "-shm")
                FileInputStream(shmFile).use { input ->
                    FileOutputStream(shmDest).use { output -> input.copyTo(output, 8 * 1024) }
                }
                extras += ", ${shmDest.name}"
            }
            ExportResult(true, "Guardado en ${dest.absolutePath}" + if (extras.isNotEmpty()) " (incluye$extras)" else " (consolidado sin WAL)")
        }
    } catch (e: SecurityException) {
        ExportResult(false, "Permiso denegado: ${e.message}")
    } catch (e: Exception) {
        ExportResult(false, "Error al exportar: ${e.message}")
    }
}
