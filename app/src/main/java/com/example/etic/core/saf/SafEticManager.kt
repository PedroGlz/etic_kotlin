package com.example.etic.core.saf

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.documentfile.provider.DocumentFile

class SafEticManager {
    fun ensureEticFolders(context: Context, rootTreeUri: Uri): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, rootTreeUri) ?: return null
        val etic = resolveEticDir(root) ?: return null
        findOrCreateDir(etic, "IMG_CLIENTES")
        return findOrCreateDir(etic, "Inspecciones")
    }

    fun getEticDir(context: Context, rootTreeUri: Uri): DocumentFile? {
        val root = DocumentFile.fromTreeUri(context, rootTreeUri) ?: return null
        return resolveEticDir(root)
    }

    fun ensureInspectionFolders(
        context: Context,
        rootTreeUri: Uri,
        inspectionNumero: String
    ): Pair<DocumentFile?, DocumentFile?> {
        val root = DocumentFile.fromTreeUri(context, rootTreeUri) ?: return null to null
        val inspectionDir = resolveInspectionDir(root, inspectionNumero) ?: return null to null
        val images = findOrCreateDir(inspectionDir, "Imagenes")
        val reports = findOrCreateDir(inspectionDir, "Reportes")
        return images to reports
    }

    fun getImagesDir(
        context: Context,
        rootTreeUri: Uri,
        inspectionNumero: String
    ): DocumentFile? = ensureInspectionFolders(context, rootTreeUri, inspectionNumero).first

    fun getClientesDir(
        context: Context,
        rootTreeUri: Uri
    ): DocumentFile? = getEticDir(context, rootTreeUri)?.let { etic ->
        findOrCreateDir(etic, "IMG_CLIENTES")
    }

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

    private fun resolveEticDir(root: DocumentFile): DocumentFile? {
        if (root.isDirectory && root.name.equals("ETIC", ignoreCase = true)) {
            return root
        }
        return findOrCreateDir(root, "ETIC")
    }

    private fun resolveInspectionsRoot(root: DocumentFile): DocumentFile? {
        if (root.isDirectory && root.name.equals("Inspecciones", ignoreCase = true)) {
            return root
        }
        val etic = resolveEticDir(root) ?: return null
        return findOrCreateDir(etic, "Inspecciones")
    }

    private fun resolveInspectionDir(root: DocumentFile, inspectionNumero: String): DocumentFile? {
        if (root.isDirectory && root.name.equals(inspectionNumero, ignoreCase = true)) {
            return root
        }
        val inspectionsRoot = resolveInspectionsRoot(root) ?: return null
        return findOrCreateDir(inspectionsRoot, inspectionNumero)
    }

    private fun findOrCreateDir(parent: DocumentFile, name: String): DocumentFile? {
        parent.listFiles()
            .firstOrNull { it.isDirectory && it.name?.trim()?.equals(name.trim(), ignoreCase = true) == true }
            ?.let { return it }
        return parent.createDirectory(name)
    }
}
