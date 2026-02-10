package com.example.etic.core.saf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object EticImageStore {
    private fun getImagesFolder(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?
    ): DocumentFile? {
        if (rootTreeUri == null || inspectionNumero.isNullOrBlank()) return null
        val manager = SafEticManager()
        return manager.getImagesDir(context, rootTreeUri, inspectionNumero)
    }

    fun saveBitmap(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        prefix: String,
        bmp: Bitmap
    ): String? {
        val folder = getImagesFolder(context, rootTreeUri, inspectionNumero) ?: return null
        val name = "$prefix-" + System.currentTimeMillis().toString() + ".jpg"
        val existing = folder.findFile(name)
        if (existing != null && !existing.delete()) return null
        val file = folder.createFile("image/jpeg", name) ?: return null
        return try {
            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
            } ?: return null
            file.name ?: name
        } catch (_: Exception) {
            null
        }
    }

    fun copyFromUri(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        prefix: String,
        uri: Uri
    ): String? {
        val folder = getImagesFolder(context, rootTreeUri, inspectionNumero) ?: return null
        val mime = context.contentResolver.getType(uri).orEmpty()
        val ext = when {
            mime.contains("png") -> ".png"
            mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
            else -> ".jpg"
        }
        val name = "$prefix-" + System.currentTimeMillis().toString() + ext
        val existing = folder.findFile(name)
        if (existing != null && !existing.delete()) return null
        val targetMime = if (ext == ".png") "image/png" else "image/jpeg"
        val file = folder.createFile(targetMime, name) ?: return null
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                context.contentResolver.openOutputStream(file.uri)?.use { output ->
                    input.copyTo(output, 8 * 1024)
                }
            } ?: return null
            file.name ?: name
        } catch (_: Exception) {
            null
        }
    }

    fun loadBitmap(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        fileName: String?
    ): Bitmap? {
        if (fileName.isNullOrBlank()) return null
        val folder = getImagesFolder(context, rootTreeUri, inspectionNumero) ?: return null
        val target = folder.findFile(fileName)
            ?: folder.listFiles().firstOrNull { it.name.equals(fileName, ignoreCase = true) }
            ?: return null
        return runCatching {
            context.contentResolver.openInputStream(target.uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()
    }
}
