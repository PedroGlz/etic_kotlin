package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.etic.reports.BaselineGraphPoint
import com.example.etic.reports.BaselineReportHeaderData
import com.example.etic.reports.BaselineReportPageData
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class BaselinePdfGenerator {

    fun generate(
        output: OutputStream,
        header: BaselineReportHeaderData,
        pages: List<BaselineReportPageData>,
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
        val smallPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(7f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
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
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val footerBottomMargin = mm(8f)

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
                    if (current.isNotEmpty()) lines += current
                    current = w
                }
            }
            if (current.isNotEmpty()) lines += current
            return lines.ifEmpty { listOf("") }
        }

        fun drawMultiline(
            c: Canvas,
            text: String,
            x: Float,
            y: Float,
            w: Float,
            paint: TextPaint,
            lineH: Float,
            maxLines: Int = Int.MAX_VALUE
        ): Int {
            val lines = wrapText(text, paint, w).take(maxLines)
            lines.forEachIndexed { idx, line ->
                c.drawText(line, x, y + lineH * (idx + 1) - mm(0.8f), paint)
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
                    max(1, (logo.width * scale).toInt()),
                    max(1, (logo.height * scale).toInt()),
                    true
                )
            } else {
                logo
            }
            c.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        fun drawFooter(c: Canvas) {
            val line1 = "ETIC PdM System V01-2026"
            val line2 = "Copyright \u00a9 $currentYear Todos los derechos reservados."
            val w1 = footerPaint.measureText(line1)
            val w2 = footerPaint.measureText(line2)
            val line2Y = pageHeight - footerBottomMargin
            val line1Y = line2Y - mm(4f)
            c.drawText(line1, (pageWidth - w1) / 2f, line1Y, footerPaint)
            c.drawText(line2, (pageWidth - w2) / 2f, line2Y, footerPaint)
        }

        fun drawGraph(c: Canvas, points: List<BaselineGraphPoint>) {
            if (points.isEmpty()) return

            val selected = points.takeLast(5)
            val values = selected.flatMap { listOfNotNull(it.mta, it.tempMax, it.tempAmb) }
            val maxY = if (values.isEmpty()) 10.0 else max(10.0, ceil(values.maxOrNull() ?: 10.0))
            val yDivs = 10

            val baseX = mm(193f)
            val baseY = mm(70f)
            val fullW = mm(92f)
            val fullH = mm(67f)
            val ordinateW = mm(10f)
            val margin = mm(1f)
            val graphValH = mm(4f)
            val graphW = fullW - ordinateW
            val graphH = fullH - (3f * margin) - graphValH
            val graphX = baseX + ordinateW + margin
            val graphY = baseY + margin
            val graphValY = baseY + (2f * margin) + graphH

            val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = android.graphics.Color.rgb(171, 171, 171)
            }
            c.drawRect(RectF(graphX, graphY, graphX + graphW, graphY + graphH), gridPaint)

            for (i in 0..yDivs) {
                val yy = graphY + (graphH / yDivs) * i
                c.drawLine(graphX, yy, graphX + graphW, yy, gridPaint)
                val label = ((maxY / yDivs) * (yDivs - i)).toInt().toString()
                c.drawText(label, graphX - mm(7f), yy + mm(1f), smallPaint)
            }

            val xCount = selected.size
            val xStep = if (xCount <= 1) graphW else graphW / (xCount - 1)
            for (i in 0 until xCount) {
                val xx = graphX + xStep * i
                c.drawLine(xx, graphY, xx, graphY + graphH, gridPaint)
            }

            fun yFor(v: Double?): Float {
                val value = v ?: 0.0
                val p = (value / maxY).coerceIn(0.0, 1.0)
                return graphY + graphH - (p * graphH).toFloat()
            }

            fun drawSeries(series: List<Double?>, color: Int) {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    this.color = color
                }
                val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    this.color = android.graphics.Color.rgb(243, 255, 0)
                }
                var px = graphX
                var py = yFor(series.firstOrNull())
                series.forEachIndexed { idx, value ->
                    val x = graphX + xStep * idx
                    val y = yFor(value)
                    if (idx > 0) c.drawLine(px, py, x, y, paint)
                    c.drawCircle(x, y, mm(1f), paint)
                    c.drawCircle(x, y, mm(0.6f), fill)
                    px = x
                    py = y
                }
            }

            val tempAmbSeries = selected.map { it.tempAmb }
            val mtaSeries = selected.map { it.tempMax }
            val tempMaxSeries = selected.map { it.mta }

            drawSeries(tempAmbSeries, android.graphics.Color.rgb(23, 49, 182))
            drawSeries(mtaSeries, android.graphics.Color.rgb(245, 0, 0))
            drawSeries(tempMaxSeries, android.graphics.Color.rgb(15, 142, 149))

            val legendY = baseY - mm(3f)
            fun legend(x: Float, name: String, color: Int) {
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    this.color = color
                }
                c.drawLine(x, legendY, x + mm(6f), legendY, p)
                c.drawText(name, x + mm(7f), legendY + mm(1f), smallPaint)
            }
            c.drawText("\u00b0C", graphX - mm(8f), legendY + mm(1f), smallPaint)
            legend(graphX + mm(2f), "Temp Amb", android.graphics.Color.rgb(23, 49, 182))
            legend(graphX + mm(28f), "MTA", android.graphics.Color.rgb(245, 0, 0))
            legend(graphX + mm(44f), "Temp Max", android.graphics.Color.rgb(15, 142, 149))

            selected.forEachIndexed { idx, point ->
                val x = graphX + xStep * idx
                val label = point.label
                val w = smallPaint.measureText(label)
                val lx = when (idx) {
                    0 -> x
                    xCount - 1 -> x - w
                    else -> x - w / 2f
                }
                c.drawText(label, lx, graphValY + mm(3.5f), smallPaint)
            }
        }

        pages.forEachIndexed { pageIdx, pageData ->
            val page = doc.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIdx + 1).create()
            )
            val c = page.canvas

            drawLogo(c)
            val title = "Baseline - Equipo en Monitoreo"
            val titleX = mm(86f)
            val titleW = mm(125f)
            val centeredTitleX = titleX + (titleW - titlePaint.measureText(title)) / 2f
            c.drawText(title, centeredTitleX, mm(16f) - mm(0.8f), titlePaint)

            var y = mm(31f)
            c.drawText(header.cliente, mm(10f), y, boldPaint); y += mm(4f)
            c.drawText(header.sitio, mm(10f), y, textPaint); y += mm(4f)
            c.drawText("Analista term\u00f3grafo: ${header.analista}", mm(10f), y, textPaint); y += mm(4f)
            c.drawText("Nivel de certificaci\u00f3n: ${header.nivel}", mm(10f), y, textPaint); y += mm(4f)
            c.drawText("Fecha de reporte: ${header.fechaReporte}", mm(10f), y, textPaint)

            c.drawText("No. inspecci\u00f3n anterior: ${header.inspeccionAnterior}", mm(95f), mm(31f), textPaint)
            c.drawText("Fecha: ${header.fechaAnterior}", mm(95f), mm(35f), textPaint)
            c.drawText("No. inspecci\u00f3n actual: ${header.inspeccionActual}", mm(95f), mm(39f), textPaint)
            c.drawText("Fecha: ${header.fechaActual}", mm(95f), mm(43f), textPaint)

            c.drawRect(RectF(mm(175f), mm(27f), mm(247f), mm(45f)), linePaint)
            c.drawText("Informaci\u00f3n del equipo", mm(176f), mm(30f), boldPaint)
            c.drawLine(mm(175f), mm(31f), mm(247f), mm(31f), linePaint)
            c.drawText("C\u00f3digo de barras: ${pageData.codigoBarras}", mm(176f), mm(35f), textPaint)
            c.drawText("Fabricante: ${pageData.fabricante}", mm(176f), mm(39f), textPaint)
            c.drawText("Prioridad de operaci\u00f3n: ${pageData.prioridadOperacion}", mm(176f), mm(43f), textPaint)

            c.drawText("RUTA: ${pageData.ruta}", mm(10f), mm(60f), boldPaint)
            c.drawLine(mm(10f), mm(61f), mm(287f), mm(61f), linePaint)

            fun drawImage(xMm: Float, yMm: Float, wMm: Float, hMm: Float, bmp: Bitmap?) {
                val x = mm(xMm)
                val y0 = mm(yMm)
                val w = mm(wMm)
                val h = mm(hMm)
                c.drawRect(RectF(x, y0, x + w, y0 + h), linePaint)
                if (bmp != null) {
                    val s = min(w / bmp.width, h / bmp.height)
                    val dw = bmp.width * s
                    val dh = bmp.height * s
                    val dx = x + (w - dw) / 2f
                    val dy = y0 + (h - dh) / 2f
                    c.drawBitmap(
                        Bitmap.createScaledBitmap(
                            bmp,
                            max(1, dw.toInt()),
                            max(1, dh.toInt()),
                            true
                        ),
                        dx,
                        dy,
                        null
                    )
                } else {
                    c.drawText("Sin imagen", x + w / 2f - mm(10f), y0 + h / 2f, textPaint)
                }
            }

            drawImage(10f, 64f, 90f, 67f, pageData.irBitmap)
            drawImage(104f, 64f, 90f, 67f, pageData.idBitmap)

            drawGraph(c, pageData.graphPoints)

            fun drawImageInfoRow(
                xMm: Float,
                yMm: Float,
                wMm: Float,
                fileName: String,
                date: String,
                time: String
            ) {
                val x = mm(xMm)
                val y = mm(yMm)
                val w = mm(wMm)
                val rowH = mm(4f)
                val safeDate = date.ifBlank { "--/--/----" }
                val safeTime = time.ifBlank { "--:-- --" }
                val safeFileName = fileName.ifBlank { "-" }
                val fileW = w * (41f / 90f)
                val dateW = w * (27f / 90f)
                c.drawRect(RectF(x, y, x + fileW, y + rowH), linePaint)
                c.drawRect(RectF(x + fileW, y, x + fileW + dateW, y + rowH), linePaint)
                c.drawRect(RectF(x + fileW + dateW, y, x + w, y + rowH), linePaint)
                c.drawText("Archivo: $safeFileName", x + mm(1f), y + rowH - mm(0.8f), textPaint)
                c.drawText("Fecha: $safeDate", x + fileW + mm(1f), y + rowH - mm(0.8f), textPaint)
                c.drawText("Hora: $safeTime", x + fileW + dateW + mm(1f), y + rowH - mm(0.8f), textPaint)
            }

            drawImageInfoRow(10f, 133f, 90f, pageData.archivoIr, pageData.irFecha, pageData.irHora)
            drawImageInfoRow(104f, 133f, 90f, pageData.archivoId, pageData.idFecha, pageData.idHora)

            val cols = listOf(24f, 27f, 18f, 12f, 11f, 12f, 173f).map { mm(it) }
            val headers = listOf("No. inspecci\u00f3n", "Fecha inspecci\u00f3n", "Estatus", "T\u00b0 m\u00e1x.", "MTA", "T\u00b0 amb.", "Notas")
            val tableX = mm(10f)
            var ty = mm(150f)
            var x = tableX
            headers.forEachIndexed { idx, headerText ->
                c.drawText(headerText, x + mm(0.6f), ty + mm(3f), boldPaint)
                x += cols[idx]
            }
            c.drawLine(tableX, ty + mm(4f), tableX + cols.sum(), ty + mm(4f), linePaint)
            ty += mm(4f)

            pageData.historyRows.forEach { row ->
                val notesLines = wrapText(row.notas, textPaint, cols[6]).size.coerceAtLeast(1)
                val rowH = max(mm(4f), notesLines * mm(4f))
                if (ty + rowH > mm(191f)) return@forEach
                var cx = tableX
                c.drawText(row.noInspeccion, cx, ty + mm(3f), textPaint); cx += cols[0]
                c.drawText(row.fechaInspeccion, cx, ty + mm(3f), textPaint); cx += cols[1]
                c.drawText(row.estatusInspeccion, cx, ty + mm(3f), textPaint); cx += cols[2]
                c.drawText(row.mta, cx, ty + mm(3f), textPaint); cx += cols[3]
                c.drawText(row.tempMax, cx, ty + mm(3f), textPaint); cx += cols[4]
                c.drawText(row.tempAmb, cx, ty + mm(3f), textPaint); cx += cols[5]
                drawMultiline(c, row.notas, cx, ty, cols[6], textPaint, mm(4f), maxLines = 3)
                ty += rowH
            }

            drawFooter(c)
            doc.finishPage(page)
        }

        doc.writeTo(output)
        doc.close()
    }
}
