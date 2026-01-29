package com.example.etic.core.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

class SafEticManager {
    fun ensureEticFolders(context: Context, rootTreeUri: Uri): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, rootTreeUri) ?: return null
        val etic = findOrCreateDir(root, "ETIC") ?: return null
        return findOrCreateDir(etic, "Inspecciones")
    }

    fun ensureInspectionFolders(
        context: Context,
        rootTreeUri: Uri,
        inspectionNumero: String
    ): Pair<DocumentFile?, DocumentFile?> {
        val inspectionsRoot = ensureEticFolders(context, rootTreeUri) ?: return null to null
        val inspectionDir = findOrCreateDir(inspectionsRoot, inspectionNumero) ?: return null to null
        val images = findOrCreateDir(inspectionDir, "Imagenes")
        val reports = findOrCreateDir(inspectionDir, "Reportes")
        return images to reports
    }

    fun getImagesDir(
        context: Context,
        rootTreeUri: Uri,
        inspectionNumero: String
    ): DocumentFile? = ensureInspectionFolders(context, rootTreeUri, inspectionNumero).first

    fun getReportsDir(
        context: Context,
        rootTreeUri: Uri,
        inspectionNumero: String
    ): DocumentFile? = ensureInspectionFolders(context, rootTreeUri, inspectionNumero).second

    fun listFiles(dir: DocumentFile?): List<DocumentFile> {
        if (dir == null || !dir.isDirectory) return emptyList()
        return dir.listFiles()
            .filter { it.isFile }
            .sortedBy { it.name?.lowercase() ?: "" }
    }

    fun rename(file: DocumentFile, newName: String): Boolean = file.renameTo(newName)

    fun delete(file: DocumentFile): Boolean = file.delete()

    fun openFileIntent(fileUri: Uri, mime: String?): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mime ?: "*/*")
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }

    fun openFolderIntent(folderUri: Uri): Intent =
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(folderUri, DocumentsContract.Document.MIME_TYPE_DIR)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }

    private fun findOrCreateDir(parent: DocumentFile, name: String): DocumentFile? {
        parent.listFiles()
            .firstOrNull { it.isDirectory && it.name == name }
            ?.let { return it }
        return parent.createDirectory(name)
    }
}
