package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.etic.reports.ProblemListRow
import com.example.etic.reports.ReportHeaderData
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

class ProblemListPdfGenerator {

    fun generate(
        output: OutputStream,
        header: ReportHeaderData,
        title: String,
        rows: List<ProblemListRow>,
        logo: Bitmap? = null
    ) {
        val doc = PdfDocument()
        val pageWidth = 1754
        val pageHeight = 1240
        val dpi = 150f
        val pxPerMm = dpi / 25.4f

        fun mm(v: Float) = v * pxPerMm
        fun pt(v: Float) = v * (dpi / 72f)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(13f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }
        val centerPaint = TextPaint(textPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        val boldCenterPaint = TextPaint(boldPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val redCenterPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.rgb(245, 0, 0)
            textAlign = Paint.Align.CENTER
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(7f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = android.graphics.Color.BLACK
        }
        val altPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.rgb(236, 236, 236)
        }

        val widths = listOf(140f, 19f, 22f, 17f, 17f, 14f, 15f, 15f, 18f).map { mm(it) }
        val headers = listOf(
            "Equipo / Comentarios",
            "Fecha",
            "No Insp",
            "# Problema",
            "Estatus",
            "Crónico",
            "Temp",
            "DeltaT",
            "Severidad"
        )
        val tableX = mm(10f)
        val lineH = mm(4f)
        val tableTop = mm(55f)
        val tableBottom = mm(188f)
        val headerRightX = mm(287f)
        val titleLeftX = mm(55f)
        val titleRightX = mm(287f)
        val cellPadding = mm(1.2f)
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val footerBottomMargin = mm(8f)

        fun wrap(text: String, paint: TextPaint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val out = mutableListOf<String>()
            val paragraphs = text.replace("\r", "").split("\n")
            paragraphs.forEach { paragraph ->
                if (paragraph.isBlank()) {
                    out += ""
                } else {
                    val words = paragraph.split(" ")
                    var current = ""
                    for (word in words) {
                        val candidate = if (current.isBlank()) word else "$current $word"
                        if (paint.measureText(candidate) <= maxWidth) {
                            current = candidate
                        } else {
                            if (current.isNotBlank()) out += current
                            current = word
                        }
                    }
                    if (current.isNotBlank()) out += current
                }
            }
            return out.ifEmpty { listOf("") }
        }

        fun fitText(text: String, paint: TextPaint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            val ellipsis = "..."
            var result = text
            while (result.isNotEmpty() && paint.measureText(result + ellipsis) > maxWidth) {
                result = result.dropLast(1)
            }
            return if (result.isBlank()) ellipsis else result.trimEnd() + ellipsis
        }

        fun drawLogo(c: Canvas) {
            if (logo == null) return
            val targetW = mm(38f)
            val scale = min(targetW / logo.width, 1f)
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    logo,
                    max(1, (logo.width * scale).toInt()),
                    max(1, (logo.height * scale).toInt()),
                    true
                )
            } else {
                logo
            }
            c.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        fun drawRightText(c: Canvas, text: String, rightX: Float, y: Float, paint: TextPaint) {
            c.drawText(text, rightX - paint.measureText(text), y, paint)
        }

        fun drawHeader(c: Canvas) {
            drawLogo(c)
            c.drawText(title, (titleLeftX + titleRightX) / 2f, mm(16f), titlePaint)
            var y = mm(31f)
            c.drawText(header.cliente, mm(10f), y, boldPaint); y += lineH
            c.drawText(header.sitio, mm(10f), y, textPaint); y += lineH
            c.drawText("Analista Termógrafo: ${header.analista}", mm(10f), y, textPaint); y += lineH
            c.drawText("Nivel Certificación: ${header.nivel}", mm(10f), y, textPaint)

            drawRightText(c, "Fecha Reporte: ${header.fechaReporte}", headerRightX, mm(31f), textPaint)
            drawRightText(c, "No. Inspección Anterior: ${header.inspeccionAnterior}", headerRightX, mm(35f), textPaint)
            drawRightText(c, "No. Inspección Actual: ${header.inspeccionActual}", headerRightX, mm(39f), textPaint)
        }

        fun drawFooter(c: Canvas) {
            val line1 = "ETIC PdM System V01-2026"
            val line2 = "Copyright © $currentYear Todos los derechos reservados."
            val w1 = footerPaint.measureText(line1)
            val w2 = footerPaint.measureText(line2)
            val line2Y = pageHeight - footerBottomMargin
            val line1Y = line2Y - lineH
            c.drawText(line1, (pageWidth - w1) / 2f, line1Y, footerPaint)
            c.drawText(line2, (pageWidth - w2) / 2f, line2Y, footerPaint)
        }

        fun drawTableHeader(c: Canvas, y: Float) {
            var x = tableX
            headers.forEachIndexed { idx, headerText ->
                val width = widths[idx]
                val safeText = fitText(headerText, boldCenterPaint, width - (cellPadding * 2f))
                val centerX = x + (width / 2f)
                val paint = if (idx == 0) boldPaint else boldCenterPaint
                val drawX = if (idx == 0) x + cellPadding else centerX
                c.drawText(safeText, drawX, y + lineH - mm(0.7f), paint)
                x += width
            }
            c.drawLine(tableX, y + lineH, tableX + widths.sum(), y + lineH, linePaint)
        }

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create())
        var canvas = page.canvas
        drawHeader(canvas)
        drawTableHeader(canvas, tableTop)
        var y = tableTop + lineH

        fun newPage() {
            drawFooter(canvas)
            doc.finishPage(page)
            pageNo += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create())
            canvas = page.canvas
            drawHeader(canvas)
            drawTableHeader(canvas, tableTop)
            y = tableTop + lineH
        }

        rows.forEachIndexed { idx, row ->
            val firstColLines = wrap(row.equipoComentarios, textPaint, widths[0] - (cellPadding * 2f))
            val rowH = max(lineH, firstColLines.size * lineH)
            if (y + rowH > tableBottom) newPage()

            if (idx % 2 == 0) {
                canvas.drawRect(tableX, y, tableX + widths.sum(), y + rowH, altPaint)
            }

            val values = listOf(
                row.equipoComentarios,
                row.fechaCreacion,
                row.noInspeccion,
                row.tipoNumero,
                row.estatusProblema,
                row.esCronico,
                row.temperaturaProblema,
                row.deltaT,
                row.severidad
            )

            var x = tableX
            values.forEachIndexed { col, value ->
                val width = widths[col]
                if (col == 0) {
                    val lines = wrap(value, textPaint, width - (cellPadding * 2f))
                    lines.forEachIndexed { lineIndex, line ->
                        canvas.drawText(
                            line,
                            x + cellPadding,
                            y + lineH * (lineIndex + 1) - mm(0.7f),
                            textPaint
                        )
                    }
                } else {
                    val centerX = x + (width / 2f)
                    val safeText = fitText(value, centerPaint, width - (cellPadding * 2f))
                    val paint = if (col == 5 && value.equals("SI", ignoreCase = true)) {
                        redCenterPaint
                    } else {
                        centerPaint
                    }
                    canvas.drawText(safeText, centerX, y + lineH - mm(0.7f), paint)
                }
                x += width
            }

            y += rowH
        }

        canvas.drawLine(tableX, y, tableX + widths.sum(), y, linePaint)
        drawFooter(canvas)
        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}


