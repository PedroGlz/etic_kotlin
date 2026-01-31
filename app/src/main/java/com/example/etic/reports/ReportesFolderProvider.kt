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
        // Limpia duplicados simples: si existe, lo recreamos con suffix
        val base = filename.removeSuffix(".pdf")
        var attempt = 0
        var name = "$base.pdf"
        while (folder.findFile(name) != null && attempt < 20) {
            attempt++
            name = "${base}_$attempt.pdf"
        }
        return folder.createFile("application/pdf", name)
    }
}
