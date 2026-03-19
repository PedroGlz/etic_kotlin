package com.example.etic.reports

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfImportedPage
import com.itextpdf.text.pdf.PdfReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

sealed interface PdfSource {
    data class UriSource(val uri: Uri) : PdfSource
    data class AssetSource(val assetPath: String) : PdfSource
}

object PdfMergeUtil {
    fun mergeToFile(
        context: Context,
        outputFile: DocumentFile,
        sources: List<PdfSource>
    ): Result<String> = runCatching {
        val tempFiles = mutableListOf<File>()
        val document = Document()
        val destinationFile = File.createTempFile("merged_result_", ".pdf", context.cacheDir)
        val output = FileOutputStream(destinationFile)
        val writer = PdfCopy(document, output)
        try {
            document.open()
            sources.forEach { source ->
                val file = materializeSource(context, source)
                tempFiles += file

                val reader = PdfReader(file.absolutePath)
                val pageCount = reader.numberOfPages
                for (i in 1..pageCount) {
                    val page: PdfImportedPage = writer.getImportedPage(reader, i)
                    writer.addPage(page)
                }
                reader.close()
            }
            document.close()

            output.close()
            val out = context.contentResolver.openOutputStream(outputFile.uri)
                ?: error("No se pudo abrir OutputStream del PDF final.")
            FileInputStream(destinationFile).use { input ->
                input.copyTo(out)
            }
            out.close()
        } finally {
            tempFiles.forEach { file ->
                if (file.exists()) file.delete()
            }
            if (document.isOpen()) document.close()
            output.close()
            if (destinationFile.exists()) destinationFile.delete()
        }
        outputFile.uri.toString()
    }

    private fun materializeSource(context: Context, source: PdfSource): File {
        return when (source) {
            is PdfSource.UriSource -> {
                val temp = File.createTempFile("merge_", ".pdf", context.cacheDir)
                context.contentResolver.openInputStream(source.uri)?.use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                } ?: error("No se pudo abrir fuente PDF.")
                temp
            }
            is PdfSource.AssetSource -> {
                val temp = File.createTempFile("asset_merge_", ".pdf", context.cacheDir)
                context.assets.open(source.assetPath).use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                temp
            }
        }
    }
}
