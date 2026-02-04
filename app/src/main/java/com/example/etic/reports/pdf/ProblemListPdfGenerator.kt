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
import kotlin.math.max
import kotlin.math.min
import java.io.OutputStream

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
            "Tipo No",
            "Estatus",
            "Cronico",
            "Temp",
            "DeltaT",
            "Severidad"
        )
        val tableX = mm(10f)
        val lineH = mm(4f)
        val tableTop = mm(57f)
        val tableBottom = mm(191f)

        fun wrap(text: String, paint: TextPaint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val words = text.split(" ")
            val out = mutableListOf<String>()
            var cur = ""
            for (w in words) {
                val c = if (cur.isBlank()) w else "$cur $w"
                if (paint.measureText(c) <= maxWidth) {
                    cur = c
                } else {
                    if (cur.isNotBlank()) out += cur
                    cur = w
                }
            }
            if (cur.isNotBlank()) out += cur
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
            } else logo
            c.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        fun drawHeader(c: Canvas) {
            drawLogo(c)
            c.drawText(title, mm(86f), mm(16f), titlePaint)
            var y = mm(31f)
            c.drawText(header.cliente, mm(10f), y, boldPaint); y += lineH
            c.drawText(header.sitio, mm(10f), y, textPaint); y += lineH
            c.drawText("Analista Termografo: ${header.analista}", mm(10f), y, textPaint); y += lineH
            c.drawText("Nivel Certificacion: ${header.nivel}", mm(10f), y, textPaint)

            c.drawText("Fecha Reporte: ${header.fechaReporte}", mm(243f), mm(31f), textPaint)
            c.drawText(
                "No. Inspeccion Anterior: ${header.inspeccionAnterior}  Fecha: ${header.fechaAnterior}",
                mm(218f),
                mm(35f),
                textPaint
            )
            c.drawText(
                "No. Inspeccion Actual: ${header.inspeccionActual}  Fecha: ${header.fechaActual}",
                mm(218f),
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
                    lines.forEachIndexed { i, l ->
                        canvas.drawText(l, x + mm(0.7f), y + lineH * (i + 1) - mm(0.7f), textPaint)
                    }
                } else {
                    canvas.drawText(value, x + mm(0.7f), y + lineH - mm(0.7f), textPaint)
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

