package com.example.etic.reports.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.etic.features.inspection.tree.Baseline
import com.example.etic.features.inspection.tree.Problem
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import kotlin.math.min

class InspectionPdfReportGenerator {
    suspend fun generate(
        context: Context,
        inspectionTitle: String,
        siteName: String?,
        inspectionNumber: String?,
        createdBy: String?,
        baselines: List<Baseline>,
        problems: List<Problem>,
        outputFile: File
    ): File {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 24f
        val lineGap = 14f
        val footerGap = 24f

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        var pageNumber = 0
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNumber).create())
        var canvas = page.canvas
        var y = margin

        fun newPage() {
            doc.finishPage(page)
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNumber).create())
            canvas = page.canvas
            y = margin
        }

        fun ensureSpace(minHeight: Float) {
            if (y + minHeight > pageHeight - footerGap) newPage()
        }

        fun drawTextLine(text: String, paint: Paint = textPaint) {
            ensureSpace(lineGap)
            canvas.drawText(text, margin, y, paint)
            y += lineGap
        }

        fun ellipsize(text: String, maxWidth: Float, paint: Paint): String {
            if (paint.measureText(text) <= maxWidth) return text
            val ellipsis = "..."
            val ellipsisWidth = paint.measureText(ellipsis)
            var end = text.length
            while (end > 0 && paint.measureText(text, 0, end) + ellipsisWidth > maxWidth) {
                end -= 1
            }
            return if (end <= 0) ellipsis else text.substring(0, end) + ellipsis
        }

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // Header
        val logo = loadLogo(context)
        if (logo != null) {
            val maxW = 120f
            val maxH = 40f
            val scale = min(maxW / logo.width, maxH / logo.height)
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    logo,
                    (logo.width * scale).toInt(),
                    (logo.height * scale).toInt(),
                    true
                )
            } else {
                logo
            }
            canvas.drawBitmap(bmp, pageWidth - margin - bmp.width, margin - 6f, null)
        }
        drawTextLine(inspectionTitle, titlePaint)
        drawTextLine("Sitio: ${siteName.orEmpty()}")
        drawTextLine("No. Inspección: ${inspectionNumber.orEmpty()}")
        drawTextLine("Creado por: ${createdBy.orEmpty()}")
        y += 4f

        // Baselines table
        drawTextLine("Baselines", headerPaint)
        val blCols = listOf(
            "NoInsp" to 60f,
            "Fecha" to 70f,
            "MTA" to 45f,
            "MAX" to 45f,
            "AMB" to 45f,
            "IR" to 70f,
            "ID" to 70f,
            "Notas" to 160f
        )

        fun drawRow(values: List<String>, cols: List<Pair<String, Float>>) {
            ensureSpace(lineGap)
            var x = margin
            for (i in values.indices) {
                val maxWidth = cols[i].second
                val text = ellipsize(values[i], maxWidth - 4f, textPaint)
                canvas.drawText(text, x, y, textPaint)
                x += maxWidth
            }
            y += lineGap
        }

        ensureSpace(lineGap * 2)
        var x = margin
        for ((label, width) in blCols) {
            canvas.drawText(label, x, y, labelPaint)
            x += width
        }
        y += lineGap

        baselines.forEach { b ->
            val fecha = runCatching { b.fecha.format(dateFormatter) }.getOrDefault(b.fecha.toString())
            drawRow(
                listOf(
                    b.numInspeccion,
                    fecha,
                    b.mtaC.toString(),
                    b.tempC.toString(),
                    b.ambC.toString(),
                    b.imgR.orEmpty(),
                    b.imgD.orEmpty(),
                    b.notas
                ),
                blCols
            )
        }

        y += 6f

        // Problems table
        drawTextLine("Problemas", headerPaint)
        val prCols = listOf(
            "No" to 35f,
            "Fecha" to 70f,
            "Tipo" to 70f,
            "Estatus" to 70f,
            "Temp" to 50f,
            "ΔT" to 40f,
            "Severidad" to 70f,
            "Equipo" to 120f
        )

        ensureSpace(lineGap * 2)
        x = margin
        for ((label, width) in prCols) {
            canvas.drawText(label, x, y, labelPaint)
            x += width
        }
        y += lineGap

        problems.forEach { p ->
            val fecha = runCatching { p.fecha.format(dateFormatter) }.getOrDefault(p.fecha.toString())
            drawRow(
                listOf(
                    p.no.toString(),
                    fecha,
                    p.tipo,
                    p.estatus,
                    p.tempC.toString(),
                    p.deltaTC.toString(),
                    p.severidad,
                    p.equipo
                ),
                prCols
            )
        }

        doc.finishPage(page)
        FileOutputStream(outputFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        return outputFile
    }

    private fun loadLogo(context: Context): Bitmap? {
        val res = context.resources
        val pkg = context.packageName
        val id = res.getIdentifier("ETIC_logo", "drawable", pkg)
            .takeIf { it != 0 }
            ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
            ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
        return id?.let { BitmapFactory.decodeResource(res, it) }
    }
}
