package com.example.etic.reports

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.documentfile.provider.DocumentFile
import java.io.File
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
        val output = context.contentResolver.openOutputStream(outputFile.uri)
            ?: error("No se pudo abrir OutputStream del PDF final.")
        output.use { stream ->
            val merged = PdfDocument()
            try {
                var pageNumber = 1
                sources.forEach { source ->
                    val file = materializeSource(context, source)
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
                        PdfRenderer(pfd).use { renderer ->
                            for (index in 0 until renderer.pageCount) {
                                renderer.openPage(index).use { page ->
                                    val info = PdfDocument.PageInfo.Builder(
                                        page.width,
                                        page.height,
                                        pageNumber++
                                    ).create()
                                    val target = merged.startPage(info)
                                    val bitmap = Bitmap.createBitmap(
                                        page.width,
                                        page.height,
                                        Bitmap.Config.ARGB_8888
                                    )
                                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                                    target.canvas.drawBitmap(
                                        bitmap,
                                        null,
                                        Rect(0, 0, page.width, page.height),
                                        null
                                    )
                                    merged.finishPage(target)
                                    bitmap.recycle()
                                }
                            }
                        }
                    }
                    if (source is PdfSource.AssetSource && file.exists()) {
                        file.delete()
                    }
                }
                merged.writeTo(stream)
            } finally {
                merged.close()
            }
        }
        outputFile.uri.toString()
    }

    private fun materializeSource(context: Context, source: PdfSource): File {
        return when (source) {
            is PdfSource.UriSource -> {
                val temp = File.createTempFile("merge_", ".pdf", context.cacheDir)
                context.contentResolver.openInputStream(source.uri)?.use { input ->
                    FileOutputStream(temp).use { output -> input.copyTo(output) }
                } ?: error("No se pudo abrir fuente PDF.")
                temp
            }
            is PdfSource.AssetSource -> {
                val temp = File.createTempFile("asset_merge_", ".pdf", context.cacheDir)
                context.assets.open(source.assetPath).use { input ->
                    FileOutputStream(temp).use { output -> input.copyTo(output) }
                }
                temp
            }
        }
    }
}
