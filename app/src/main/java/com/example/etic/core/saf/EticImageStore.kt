package com.example.etic.core.saf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

object EticImageStore {
    private const val DEFAULT_EXTENSION = ".jpg"
    private const val DEFAULT_MIME = "image/jpeg"
    private const val DEFAULT_DIGITS = 3

    fun loadBitmap(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        fileName: String?
    ): Bitmap? {
        if (rootTreeUri == null || inspectionNumero.isNullOrBlank() || fileName.isNullOrBlank()) return null
        val imagesDir = SafEticManager().getImagesDir(context, rootTreeUri, inspectionNumero) ?: return null
        val file = imagesDir.findFile(fileName) ?: return null
        return context.contentResolver.openInputStream(file.uri)?.use { BitmapFactory.decodeStream(it) }
    }

    fun saveBitmap(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        prefix: String,
        bmp: Bitmap
    ): String? {
        if (rootTreeUri == null || inspectionNumero.isNullOrBlank()) return null
        val imagesDir = SafEticManager().getImagesDir(context, rootTreeUri, inspectionNumero) ?: return null
        val fileName = nextImageName(imagesDir, prefix) ?: return null
        imagesDir.findFile(fileName)?.delete()
        val file = imagesDir.createFile(DEFAULT_MIME, fileName) ?: return null
        val ok = context.contentResolver.openOutputStream(file.uri)?.use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
        } ?: false
        return if (ok) fileName else null
    }

    fun copyFromUri(
        context: Context,
        rootTreeUri: Uri?,
        inspectionNumero: String?,
        prefix: String,
        uri: Uri
    ): String? {
        if (rootTreeUri == null || inspectionNumero.isNullOrBlank()) return null
        val imagesDir = SafEticManager().getImagesDir(context, rootTreeUri, inspectionNumero) ?: return null
        val fileName = nextImageName(imagesDir, prefix) ?: return null
        imagesDir.findFile(fileName)?.delete()
        val file = imagesDir.createFile(DEFAULT_MIME, fileName) ?: return null
        val ok = context.contentResolver.openInputStream(uri)?.use { input ->
            context.contentResolver.openOutputStream(file.uri)?.use { output ->
                input.copyTo(output)
                true
            } ?: false
        } ?: false
        return if (ok) fileName else null
    }

    private fun nextImageName(imagesDir: DocumentFile, prefix: String): String? {
        val safePrefix = prefix.trim()
        if (safePrefix.isBlank()) return null
        val regex = Regex(
            "^${Regex.escape(safePrefix)}[-_]?([0-9]+)(\\.[^.]+)?$",
            RegexOption.IGNORE_CASE
        )
        val maxNumber = imagesDir.listFiles()
            .mapNotNull { it.name }
            .mapNotNull { name ->
                val match = regex.find(name) ?: return@mapNotNull null
                match.groupValues.getOrNull(1)?.toIntOrNull()
            }
            .maxOrNull() ?: 0
        val next = maxNumber + 1
        val numberPart = next.toString().padStart(DEFAULT_DIGITS, '0')
        return "$safePrefix-$numberPart$DEFAULT_EXTENSION"
    }
}
