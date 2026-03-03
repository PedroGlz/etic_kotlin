package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            color = android.graphics.Color.BLACK
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.BLACK
        }
        val redPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.rgb(245, 0, 0)
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
        val tableTop = mm(57f)
        val tableBottom = mm(191f)
        val headerRightX = mm(287f)

        fun wrap(text: String, paint: TextPaint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val out = mutableListOf<String>()
            val paragraphs = text.replace("\r", "").split("\n")
            paragraphs.forEach { paragraph ->
                if (paragraph.isBlank()) {
                    out += ""
                } else {
                    val words = paragraph.split(" ")
                    var cur = ""
                    for (word in words) {
                        val candidate = if (cur.isBlank()) word else "$cur $word"
                        if (paint.measureText(candidate) <= maxWidth) {
                            cur = candidate
                        } else {
                            if (cur.isNotBlank()) out += cur
                            cur = word
                        }
                    }
                    if (cur.isNotBlank()) out += cur
                }
            }
            return out.ifEmpty { listOf("") }
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
            val titleX = mm(86f)
            val titleW = mm(125f)
            val centeredTitleX = titleX + (titleW - titlePaint.measureText(title)) / 2f
            c.drawText(title, centeredTitleX, mm(16f), titlePaint)
            var y = mm(31f)
            c.drawText(header.cliente, mm(10f), y, boldPaint); y += lineH
            c.drawText(header.sitio, mm(10f), y, textPaint); y += lineH
            c.drawText("Analista Termógrafo: ${header.analista}", mm(10f), y, textPaint); y += lineH
            c.drawText("Nivel Certificación: ${header.nivel}", mm(10f), y, textPaint)

            drawRightText(c, "Fecha Reporte: ${header.fechaReporte}", headerRightX, mm(31f), textPaint)
            drawRightText(
                c,
                "No. Inspección Anterior: ${header.inspeccionAnterior}  Fecha: ${header.fechaAnterior}",
                headerRightX,
                mm(35f),
                textPaint
            )
            drawRightText(
                c,
                "No. Inspección Actual: ${header.inspeccionActual}  Fecha: ${header.fechaActual}",
                headerRightX,
                mm(39f),
                textPaint
            )
        }

        fun drawTableHeader(c: Canvas, y: Float) {
            var x = tableX
            headers.forEachIndexed { idx, h ->
                c.drawText(h, x + mm(0.7f), y + lineH - mm(0.7f), boldPaint)
                x += widths[idx]
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
            doc.finishPage(page)
            pageNo += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNo).create())
            canvas = page.canvas
            drawHeader(canvas)
            drawTableHeader(canvas, tableTop)
            y = tableTop + lineH
        }

        rows.forEachIndexed { idx, row ->
            val c1Lines = wrap(row.equipoComentarios, textPaint, widths[0] - mm(1f))
            val rowH = max(lineH, c1Lines.size * lineH)
            if (y + rowH > tableBottom) newPage()

            if (idx % 2 == 0) {
                canvas.drawRect(RectF(tableX, y, tableX + widths.sum(), y + rowH), altPaint)
            }

            val vals = listOf(
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
            vals.forEachIndexed { col, value ->
                if (col == 0) {
                    val lines = wrap(value, textPaint, widths[col] - mm(1f))
                    lines.forEachIndexed { i, line ->
                        canvas.drawText(line, x + mm(0.7f), y + lineH * (i + 1) - mm(0.7f), textPaint)
                    }
                } else {
                    val paint = if (col == 5 && value.equals("SI", ignoreCase = true)) redPaint else textPaint
                    canvas.drawText(value, x + mm(0.7f), y + lineH - mm(0.7f), paint)
                }
                x += widths[col]
            }
            y += rowH
        }

        canvas.drawLine(tableX, y, tableX + widths.sum(), y, linePaint)
        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}
