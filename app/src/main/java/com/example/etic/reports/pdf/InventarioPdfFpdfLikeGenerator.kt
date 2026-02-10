package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.etic.reports.InventoryReportRow
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class InventoryReportHeader(
    val cliente: String,
    val sitio: String,
    val noInspeccion: String,
    val fechaInspeccion: String,
    val fechaReporte: String,
    val inspector: String,
    val tipoProblema: List<String>,
    val prioridadOperativa: List<String>,
    val estadoEquipo: List<String>
)

class InventarioPdfFpdfLikeGenerator {

    fun generate(
        output: OutputStream,
        header: InventoryReportHeader,
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
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)

        val headerYTop = mm(10f)
        val headerBlockY = mm(27f)
        val tableTopY = mm(60f)
        val lineHeight = mm(4f)
        val footerMargin = mm(12f)

        val colW = listOf(20f, 16f, 21f, 119f, 29f, 72f).map { mm(it) }
        val tableX = mm(10f)
        val tableW = colW.sum()

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = tableTopY

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
            val x = mm(86f)
            val width = mm(125f)
            val layout = StaticLayout.Builder.obtain(
                "Inventario De Equipo",
                0,
                "Inventario De Equipo".length,
                titlePaint,
                width.toInt()
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .build()
            c.save()
            c.translate(x, headerYTop)
            layout.draw(c)
            c.restore()
        }

        fun drawHeaderBlocks(c: Canvas) {
            var x = mm(10f)
            var y0 = headerBlockY

            fun drawLine(label: String, value: String, isBoldValue: Boolean = false) {
                c.drawText(label, x, y0, textPaint)
                val labelW = textPaint.measureText(label)
                val paint = if (isBoldValue) boldPaint else textPaint
                c.drawText(value, x + labelW + mm(2f), y0, paint)
                y0 += lineHeight
            }

            drawLine("Cliente:", header.cliente, isBoldValue = true)
            drawLine("Sitio:", header.sitio)
            drawLine("No. Inspección:", header.noInspeccion)
            drawLine("Fecha Inspección:", header.fechaInspeccion)
            drawLine("Fecha Reporte:", header.fechaReporte)
            drawLine("Inspector:", header.inspector)

            // Bloque inspecciones
            x = mm(97f)
            y0 = headerBlockY
            header.estadoEquipo.forEachIndexed { index, line ->
                if (index == 0) {
                    c.drawText("Inspección:", x, y0, textPaint)
                }
                c.drawText(line, x + mm(20f), y0, textPaint)
                y0 += lineHeight
            }

            // Recuadros con listas
            fun drawBoxList(xMm: Float, yMm: Float, wMm: Float, hLines: Int, title: String, items: List<String>) {
                val xPx = mm(xMm)
                val yPx = mm(yMm)
                val wPx = mm(wMm)
                val hPx = lineHeight * hLines
                c.drawRect(xPx, yPx, xPx + wPx, yPx + hPx, linePaint)
                c.drawText(title, xPx + mm(1f), yPx + lineHeight - mm(1f), textPaint)
                items.take(hLines - 1).forEachIndexed { idx, it ->
                    c.drawText(it, xPx + mm(1f), yPx + lineHeight * (idx + 2) - mm(1f), textPaint)
                }
            }

            drawBoxList(150f, 27f, 30f, 4, "Tipo De Problema", header.tipoProblema)
            drawBoxList(189f, 27f, 40f, 4, "Prioridad Operativa", header.prioridadOperativa)
            drawBoxList(237f, 27f, 50f, 6, "Estado De Equipo En Inspección", header.estadoEquipo)
        }

        fun drawTableHeader(c: Canvas) {
            val labels = listOf("Estado", "Prioridad", "# Problema", "Ubicación", "Código Barras", "Notas")
            var x = tableX
            val yTop = tableTopY
            val yText = yTop + lineHeight - mm(1f)
            labels.forEachIndexed { idx, label ->
                c.drawText(label, x, yText, boldPaint)
                x += colW[idx]
            }
            c.drawLine(tableX, yTop + lineHeight, tableX + tableW, yTop + lineHeight, linePaint)
        }

        fun drawFooter(c: Canvas) {
            val line1 = "ETIC SA DE CV"
            val line2Text = "Copyright \u00A9 $currentYear Todos los derechos reservados."
            val line2 = "Copyright © 2023 NefWorks Todos los derechos reservados."
            val y1 = pageHeight - footerMargin
            val y2 = y1 + lineHeight
            val w1 = textPaint.measureText(line1)
            val w2 = textPaint.measureText(line2Text)
            c.drawText(line1, (pageWidth - w1) / 2f, y1, textPaint)
            c.drawText(line2Text, (pageWidth - w2) / 2f, y2, textPaint)
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
            val levelIndentMm = if (row.level <= 1) 1f else row.level * 2f
            val indentPx = mm(levelIndentMm)
            val ubicWidth = colW[3] - indentPx

            val notesLayout = StaticLayout.Builder.obtain(
                row.notas,
                0,
                row.notas.length,
                textPaint,
                colW[5].toInt()
            )
                .setLineSpacing(0f, 1f)
                .build()

            val notesLines = max(1, notesLayout.lineCount)
            val rowHeight = max(lineHeight, notesLines * lineHeight)

            if (y + rowHeight + footerMargin > pageHeight) {
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

            canvas.save()
            canvas.translate(x, y)
            val notesHeight = notesLines * lineHeight
            val layout = StaticLayout.Builder.obtain(
                row.notas,
                0,
                row.notas.length,
                textPaint,
                colW[5].toInt()
            )
                .setLineSpacing(0f, 1f)
                .build()
            layout.draw(canvas)
            canvas.restore()

            y += rowHeight
        }

        canvas.drawLine(tableX, y, tableX + tableW, y, linePaint)
        drawFooter(canvas)
        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}

fun generateInventarioPdfLike(
    output: OutputStream,
    header: InventoryReportHeader,
    rows: List<InventoryReportRow>,
    logo: Bitmap? = null
) {
    InventarioPdfFpdfLikeGenerator().generate(output, header, rows, logo)
}
