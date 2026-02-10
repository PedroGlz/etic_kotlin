package com.example.etic.reports

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile

class ReportesFolderProvider(
    private val context: Context,
    private val getInspeccionReportesTreeUri: (inspectionNumber: String) -> Uri?
) {
    fun getReportesFolder(noInspeccion: String): DocumentFile? {
        val treeUri = getInspeccionReportesTreeUri(noInspeccion) ?: return null
        return DocumentFile.fromTreeUri(context, treeUri)
            ?: DocumentFile.fromSingleUri(context, treeUri)
    }

    fun createPdfFile(folder: DocumentFile, filename: String): DocumentFile? {
        return createOrReplaceFile(folder, "application/pdf", filename)
    }

    fun createOrReplaceFile(
        folder: DocumentFile,
        mimeType: String,
        filename: String
    ): DocumentFile? {
        val existing = folder.findFile(filename)
        if (existing != null && !existing.delete()) {
            return null
        }
        return folder.createFile(mimeType, filename)
    }
}
