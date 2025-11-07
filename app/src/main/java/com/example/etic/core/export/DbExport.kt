package com.example.etic.core.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class ExportResult(val success: Boolean, val message: String)

fun exportRoomDbToDownloads(context: Context, dbName: String = "etic.db"): ExportResult {
    return try {
        val src = context.getDatabasePath(dbName)
        if (src == null || !src.exists()) {
            return ExportResult(false, "No se encontró la base de datos: $dbName")
        }
        val fileName = "${dbName.substringBeforeLast('.')}_export_${System.currentTimeMillis()}.db"
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
            ExportResult(true, "Guardado en Descargas como $fileName")
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            val dest = File(dir, fileName)
            FileInputStream(src).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output, bufferSize = 8 * 1024)
                }
            }
            ExportResult(true, "Guardado en ${dest.absolutePath}")
        }
    } catch (e: SecurityException) {
        ExportResult(false, "Permiso denegado: ${e.message}")
    } catch (e: Exception) {
        ExportResult(false, "Error al exportar: ${e.message}")
    }
}

