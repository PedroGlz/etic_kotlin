package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.etic.reports.InventoryHeaderData
import com.example.etic.reports.InventoryReportRow
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class InventarioPdfGenerator {

    fun generate(
        output: OutputStream,
        header: InventoryHeaderData,
        rows: List<InventoryReportRow>,
        logo: Bitmap? = null
    ) {
        val doc = PdfDocument()
        val pageWidth = 1754
        val pageHeight = 1240
        val dpi = 150f
        val pxPerMm = dpi / 25.4f

        fun mm(v: Float) = v * pxPerMm
        fun pt(v: Float) = v * (dpi / 72f)
        fun mmToPt(v: Float) = v * 2.83465f

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(13f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(7f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val headerTopY = mm(10f)
        val headerBlockY = mm(27f)
        val tableTopY = mm(60f)
        val lineHeight = mm(4f)
        val footerY = mm(195f)

        val colW = listOf(20f, 16f, 21f, 119f, 29f, 72f).map { mm(it) }
        val tableX = mm(10f)
        val tableW = colW.sum()

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = tableTopY

        fun wrapText(text: String, paint: TextPaint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var current = ""
            for (w in words) {
                val candidate = if (current.isEmpty()) w else "$current $w"
                if (paint.measureText(candidate) <= maxWidth) {
                    current = candidate
                } else {
                    if (current.isNotEmpty()) lines.add(current)
                    current = w
                }
            }
            if (current.isNotEmpty()) lines.add(current)
            return lines.ifEmpty { listOf("") }
        }

        fun measureLines(text: String, paint: TextPaint, maxWidth: Float): Int =
            wrapText(text, paint, maxWidth).size

        fun drawMultiCell(
            c: Canvas,
            text: String,
            x: Float,
            y: Float,
            maxWidth: Float,
            paint: TextPaint,
            lineH: Float
        ): Int {
            val lines = wrapText(text, paint, maxWidth)
            lines.forEachIndexed { idx, line ->
                c.drawText(line, x, y + lineH * (idx + 1) - mm(1f), paint)
            }
            return lines.size
        }

        fun drawLogo(c: Canvas) {
            if (logo == null) return
            val targetW = mm(38f)
            val scale = min(targetW / logo.width, 1f)
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    logo,
                    (logo.width * scale).toInt(),
                    (logo.height * scale).toInt(),
                    true
                )
            } else logo
            c.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        fun drawTitle(c: Canvas) {
            val text = "Inventario De Equipo"
            val x = mm(86f)
            val width = mm(125f)
            val textWidth = titlePaint.measureText(text)
            val offset = max(0f, (width - textWidth) / 2f)
            c.drawText(text, x + offset, headerTopY + mm(6f), titlePaint)
        }

        fun drawHeaderBlocks(c: Canvas) {
            var x = mm(10f)
            var y0 = headerBlockY + lineHeight

            fun drawTextLine(value: String, boldValue: Boolean = false) {
                val paint = if (boldValue) boldPaint else textPaint
                c.drawText(value, x, y0, paint)
                y0 += lineHeight
            }

            // Cliente y sitio sin prefijos.
            drawTextLine(header.cliente, boldValue = true)
            drawTextLine(header.sitio)
            drawTextLine("Analista Termógrafo: ${header.analista}")
            drawTextLine("Nivel De Certificación: ${header.nivel}")
            drawTextLine("Fecha Reporte: ${header.fechaReporte}")

            x = mm(97f)
            y0 = headerBlockY + lineHeight
            c.drawText("No. Inspección Anterior:", x, y0, boldPaint)
            c.drawText(header.inspeccionAnterior, x + mm(30f), y0, textPaint)
            y0 += lineHeight
            c.drawText("Fecha:", x, y0, boldPaint)
            c.drawText(header.fechaAnterior, x + mm(9f), y0, textPaint)
            y0 += lineHeight
            c.drawText("No. Inspección Actual:", x, y0, boldPaint)
            c.drawText(header.inspeccionActual, x + mm(28f), y0, textPaint)
            y0 += lineHeight
            c.drawText("Fecha:", x, y0, boldPaint)
            c.drawText(header.fechaActual, x + mm(9f), y0, textPaint)

            fun drawBoxWithContent(
                xMm: Float,
                yMm: Float,
                wMm: Float,
                hMm: Float,
                title: String,
                lines: List<String>
            ) {
                val xPx = mm(xMm)
                val yPx = mm(yMm)
                val wPx = mm(wMm)
                val hPx = mm(hMm)
                c.drawRect(xPx, yPx, xPx + wPx, yPx + hPx, linePaint)
                c.drawText(title, xPx + mm(1f), yPx + lineHeight - mm(1f), boldPaint)
                c.drawLine(xPx, yPx + lineHeight, xPx + wPx, yPx + lineHeight, linePaint)
                var ly = yPx + (lineHeight * 2f) - mm(1f)
                lines.forEachIndexed { idx, line ->
                    val rowTop = yPx + lineHeight * (idx + 1)
                    val rowBottom = yPx + lineHeight * (idx + 2)
                    c.drawLine(xPx, rowTop, xPx, rowBottom, linePaint)
                    c.drawLine(xPx + wPx, rowTop, xPx + wPx, rowBottom, linePaint)
                    if (idx == lines.lastIndex) {
                        c.drawLine(xPx, rowBottom, xPx + wPx, rowBottom, linePaint)
                    }
                    if (ly <= yPx + hPx - mm(0.5f)) {
                        c.drawText(line, xPx + mm(1f), ly, textPaint)
                    }
                    ly += lineHeight
                }
            }

            drawBoxWithContent(
                xMm = 150f,
                yMm = 27f,
                wMm = 30f,
                hMm = 16f,
                title = "Tipo De Problema",
                lines = listOf("E = Eléctrico", "M = Mecánico", "V = Visual")
            )
            drawBoxWithContent(
                xMm = 189f,
                yMm = 27f,
                wMm = 40f,
                hMm = 16f,
                title = "Prioridad Operativa",
                lines = listOf("CTO = Crítico", "ETO = Esencial", "UN = No clasificado")
            )
            drawBoxWithContent(
                xMm = 237f,
                yMm = 27f,
                wMm = 50f,
                hMm = 28f,
                title = "Estado De Equipo En Inspección",
                lines = listOf(
                    "PVERIF = Para Verificar",
                    "VERIFICADO = Verificado",
                    "NOCARGA = Sin Carga",
                    "MTTO = En Mantenimiento",
                    "BLOQ = Bloqueado",
                    "NOACC = No Accesible"
                )
            )
        }

        fun drawTableHeader(c: Canvas) {
            val labels = listOf("Estado", "Prioridad", "# Problema", " Ubicación", "Código Barras", "Notas")
            var x = tableX
            val yText = tableTopY + lineHeight - mm(1f)
            labels.forEachIndexed { idx, label ->
                c.drawText(label, x, yText, boldPaint)
                x += colW[idx]
            }
            c.drawLine(tableX, tableTopY + lineHeight, tableX + tableW, tableTopY + lineHeight, linePaint)
        }

        fun drawFooter(c: Canvas) {
            val line1 = "ETIC SA DE CV"
            val line2 = "Copyright © 2023 NefWorks Todos los derechos reservados."
            val w1 = footerPaint.measureText(line1)
            val w2 = footerPaint.measureText(line2)
            c.drawText(line1, (pageWidth - w1) / 2f, footerY, footerPaint)
            c.drawText(line2, (pageWidth - w2) / 2f, footerY + lineHeight, footerPaint)
        }

        fun startPage() {
            drawLogo(canvas)
            drawTitle(canvas)
            drawHeaderBlocks(canvas)
            drawTableHeader(canvas)
            y = tableTopY + lineHeight
        }

        fun newPage() {
            drawFooter(canvas)
            doc.finishPage(page)
            pageNumber += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            startPage()
        }

        startPage()

        rows.forEach { row ->
            val indentMm = if (row.level <= 1) 1f else row.level * 2f
            val indentPx = mm(indentMm)
            val ubicWidth = colW[3] - indentPx

            val notesLines = max(1, measureLines(row.notas, textPaint, colW[5]))
            val rowHeight = max(lineHeight, notesLines * lineHeight)

            if (y + rowHeight + mm(10f) > pageHeight) {
                newPage()
            }

            var x = tableX
            val baseline = y + lineHeight - mm(1f)
            canvas.drawText(row.estatus, x, baseline, textPaint); x += colW[0]
            canvas.drawText(row.prioridad, x, baseline, textPaint); x += colW[1]
            canvas.drawText(row.problemas, x, baseline, textPaint); x += colW[2]

            val ubPaint = if (row.level == 1) boldPaint else textPaint
            canvas.drawText(row.elemento, x + indentPx, baseline, ubPaint); x += colW[3]
            canvas.drawText(row.codigoBarras, x, baseline, textPaint); x += colW[4]

            drawMultiCell(canvas, row.notas, x, y, colW[5], textPaint, lineHeight)

            y += rowHeight
        }

        canvas.drawLine(tableX, y, tableX + tableW, y, linePaint)
        drawFooter(canvas)
        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}
