package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.etic.reports.ProblemReportHeaderData
import com.example.etic.reports.ProblemReportPageData
import com.example.etic.reports.ProblemTypeIds
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

class ProblemasPdfGenerator {

    fun generate(
        output: OutputStream,
        header: ProblemReportHeaderData,
        pages: List<ProblemReportPageData>,
        logo: Bitmap? = null
    ) {
        val document = PdfDocument()
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
        val valueBluePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.rgb(0, 35, 172)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = android.graphics.Color.BLACK
        }

        fun wrap(text: String, paint: TextPaint, maxWidth: Float): List<String> {
            if (text.isBlank()) return listOf("")
            val words = text.split(" ")
            val lines = mutableListOf<String>()
            var current = ""
            for (word in words) {
                val candidate = if (current.isBlank()) word else "$current $word"
                if (paint.measureText(candidate) <= maxWidth) {
                    current = candidate
                } else {
                    if (current.isNotBlank()) lines += current
                    current = word
                }
            }
            if (current.isNotBlank()) lines += current
            return lines.ifEmpty { listOf("") }
        }

        fun drawMultiline(
            canvas: Canvas,
            text: String,
            x: Float,
            y: Float,
            w: Float,
            paint: TextPaint,
            lineH: Float,
            maxLines: Int = Int.MAX_VALUE
        ): Int {
            val lines = wrap(text, paint, w).take(maxLines)
            lines.forEachIndexed { idx, line ->
                canvas.drawText(line, x, y + lineH * (idx + 1) - mm(0.8f), paint)
            }
            return lines.size
        }

        fun drawLogo(canvas: Canvas) {
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
            canvas.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        fun drawGeneralHeader(canvas: Canvas, pageData: ProblemReportPageData) {
            val title = "${pageData.tipoInspeccion}\nDocumentacion Del Problema"
            drawMultiline(
                canvas = canvas,
                text = title,
                x = mm(86f),
                y = mm(10f),
                w = mm(125f),
                paint = titlePaint,
                lineH = mm(6f),
                maxLines = 2
            )

            var y = mm(31f)
            val leftX = mm(10f)
            canvas.drawText(header.cliente, leftX, y, boldPaint); y += mm(4f)
            canvas.drawText(header.sitio, leftX, y, textPaint); y += mm(4f)
            canvas.drawText("Analista Termografo: ${header.analista}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("Nivel De Certificacion: ${header.nivel}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("Fecha De Reporte: ${pageData.fechaReporte}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("No. Inspeccion Anterior: ${header.inspeccionAnterior}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("Fecha: ${header.fechaAnterior}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("No. Inspeccion Actual: ${header.inspeccionActual}", leftX, y, textPaint); y += mm(4f)
            canvas.drawText("Fecha: ${header.fechaActual}", leftX, y, textPaint)
        }

        fun drawLabeledValue(
            canvas: Canvas,
            x: Float,
            y: Float,
            w: Float,
            label: String,
            value: String,
            valuePaint: TextPaint = boldPaint,
            valueRight: Boolean = true
        ) {
            canvas.drawText(label, x, y, textPaint)
            val valueX = if (valueRight) {
                x + w - valuePaint.measureText(value)
            } else {
                x + mm(25f)
            }
            canvas.drawText(value, valueX, y, valuePaint)
        }

        fun drawProblemDataBlock(canvas: Canvas, pageData: ProblemReportPageData, visual: Boolean) {
            val x = if (visual) mm(195f) else mm(185f)
            val y = mm(23f)
            val w = if (visual) mm(92f) else mm(102f)
            val h = if (visual) mm(34f) else mm(31f)
            canvas.drawRect(RectF(x, y, x + w, y + h), linePaint)

            var ly = y + mm(4f)
            canvas.drawText("Problema No:", x + mm(2f), ly, textPaint)
            canvas.drawText(
                "${pageData.tipoProblemaTag} / ${pageData.numeroProblema ?: ""}",
                x + mm(26f),
                ly,
                boldPaint
            )
            ly += mm(4f)
            canvas.drawText("Es Cronico: ${pageData.esCronico}", x + mm(2f), ly, textPaint)
            ly += mm(4f)
            canvas.drawText("Prioridad Operacion: ${pageData.prioridadOperacion}", x + mm(2f), ly, textPaint)
            ly += mm(4f)
            canvas.drawText("Prioridad De Reparacion: ${pageData.prioridadReparacion}", x + mm(2f), ly, textPaint)

            if (!visual) {
                ly += mm(4f)
                canvas.drawText(
                    "Temperatura De Anomalia: ${pageData.temperaturaAnomalia}",
                    x + mm(2f),
                    ly,
                    textPaint
                )
                ly += mm(4f)
                canvas.drawText(
                    "Temperatura De Referencia: ${pageData.temperaturaReferencia}",
                    x + mm(2f),
                    ly,
                    valueBluePaint
                )
                ly += mm(4f)
                val redPaint = TextPaint(valueBluePaint).apply {
                    color = android.graphics.Color.rgb(245, 0, 0)
                }
                canvas.drawText(
                    "Diferencial De Temperatura: ${pageData.diferencialTemperatura}",
                    x + mm(2f),
                    ly,
                    redPaint
                )
            }
        }

        fun drawEquipmentAndComment(canvas: Canvas, pageData: ProblemReportPageData, visual: Boolean) {
            if (visual) {
                val x = mm(113f)
                val y = mm(27f)
                val w = mm(80f)
                val h = mm(30f)
                val lineH = mm(4f)
                canvas.drawRect(RectF(x, y, x + w, y + h), linePaint)
                canvas.drawText("Ubicacion Del Equipo", x + mm(1.2f), y + lineH - mm(0.8f), boldPaint)
                canvas.drawLine(x, y + lineH, x + w, y + lineH, linePaint)
                drawMultiline(
                    canvas,
                    "Codigo De Barras: ${pageData.codigoBarras}",
                    x + mm(1.2f),
                    y + lineH,
                    w - mm(2f),
                    textPaint,
                    lineH,
                    maxLines = 2
                )
                drawMultiline(
                    canvas,
                    pageData.ruta,
                    x + mm(1.2f),
                    y + lineH * 3,
                    w - mm(2f),
                    textPaint,
                    lineH,
                    maxLines = 3
                )
                return
            }

            val x = mm(185f)
            val y = mm(54f)
            val w = mm(102f)
            val h = mm(48f)
            val lineH = mm(4f)
            canvas.drawRect(RectF(x, y, x + w, y + h), linePaint)
            canvas.drawText("Informacion Del Equipo", x + mm(1.2f), y + lineH - mm(0.8f), boldPaint)
            canvas.drawLine(x, y + lineH, x + w, y + lineH, linePaint)
            drawMultiline(
                canvas,
                "Codigo De Barras: ${pageData.codigoBarras}",
                x + mm(1.2f),
                y + lineH,
                w - mm(2f),
                textPaint,
                lineH,
                maxLines = 2
            )
            drawMultiline(
                canvas,
                pageData.ruta,
                x + mm(1.2f),
                y + lineH * 3,
                w - mm(2f),
                textPaint,
                lineH,
                maxLines = 6
            )
            drawMultiline(
                canvas,
                pageData.observaciones,
                x + mm(1.2f),
                y + lineH * 8,
                w - mm(2f),
                valueBluePaint,
                lineH,
                maxLines = 4
            )
        }

        fun drawThermalBlocks(canvas: Canvas, pageData: ProblemReportPageData) {
            val lineH = mm(4f)

            val tRect = RectF(mm(10f), mm(67f), mm(77f), mm(105f))
            canvas.drawRect(tRect, linePaint)
            canvas.drawText("Informacion De Temperatura", mm(11f), mm(70f), boldPaint)
            canvas.drawLine(tRect.left, mm(71f), tRect.right, mm(71f), linePaint)
            var ly = mm(75f)
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Temp. Ambiente:", pageData.temperaturaAmbiente); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Tipo Ambiente:", pageData.tipoAmbiente); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Velocidad De Viento:", pageData.velocidadViento); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Ajuste Por Viento:", pageData.ajusteViento); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Ajuste Por Carga:", pageData.ajusteCarga)

            val eRect = RectF(mm(10f), mm(108f), mm(77f), mm(146f))
            canvas.drawRect(eRect, linePaint)
            canvas.drawText("Informacion Del Equipo", mm(11f), mm(111f), boldPaint)
            canvas.drawLine(eRect.left, mm(112f), eRect.right, mm(112f), linePaint)
            ly = mm(116f)
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Tipo Falla:", pageData.tipoInspeccion); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Fabricante:", pageData.fabricante); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Circuito Voltage [V]:", pageData.voltajeCircuito); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Corriente Nominal [A]:", pageData.corrienteNominal)

            val mRect = RectF(mm(10f), mm(149f), mm(77f), mm(187f))
            canvas.drawRect(mRect, linePaint)
            canvas.drawText("Datos De Medicion De Carga", mm(11f), mm(152f), boldPaint)
            canvas.drawLine(mRect.left, mm(153f), mRect.right, mm(153f), linePaint)
            ly = mm(157f)
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "${pageData.faseProblema}:", pageData.rmsProblema, valueBluePaint); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "${pageData.faseReferencia}:", pageData.rmsReferencia, valueBluePaint); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "${pageData.faseAdicional}:", pageData.rmsAdicional, valueBluePaint); ly += lineH
            drawLabeledValue(canvas, mm(11f), ly, mm(64f), "Emisividad:", pageData.emisividad)
        }

        fun drawVisualNarrative(canvas: Canvas, pageData: ProblemReportPageData) {
            val lineH = mm(4f)
            var y = mm(69f)
            canvas.drawText("Hallazgo Visual:", mm(10f), y, boldPaint)
            drawMultiline(
                canvas,
                pageData.hallazgoVisual,
                mm(41f),
                y - lineH,
                mm(246f),
                textPaint,
                lineH,
                maxLines = 2
            )
            y += mm(8f)
            canvas.drawText("Observaciones:", mm(10f), y, boldPaint)
            drawMultiline(
                canvas,
                pageData.observaciones,
                mm(41f),
                y - lineH,
                mm(246f),
                valueBluePaint,
                lineH,
                maxLines = 4
            )
        }

        fun drawImageBox(
            canvas: Canvas,
            x: Float,
            y: Float,
            w: Float,
            h: Float,
            bmp: Bitmap?,
            fileName: String,
            date: String,
            time: String
        ) {
            canvas.drawRect(RectF(x, y, x + w, y + h), linePaint)
            if (bmp == null) {
                canvas.drawText("Sin Imagen", x + (w / 2f) - mm(11f), y + (h / 2f), textPaint)
            } else {
                val scale = min(w / bmp.width, h / bmp.height)
                val dw = bmp.width * scale
                val dh = bmp.height * scale
                val dx = x + (w - dw) / 2f
                val dy = y + (h - dh) / 2f
                canvas.drawBitmap(
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
            }
            val infoY = y + h + mm(4f)
            drawMultiline(
                canvas,
                "Archivo: $fileName    Fecha: $date    Hora: $time",
                x + mm(1f),
                infoY - mm(4f),
                w - mm(2f),
                textPaint,
                mm(4f),
                maxLines = 2
            )
        }

        pages.forEachIndexed { index, pageData ->
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            )
            val canvas = page.canvas

            drawLogo(canvas)
            drawGeneralHeader(canvas, pageData)

            val isVisual = pageData.tipoInspeccionId == ProblemTypeIds.VISUAL
            drawProblemDataBlock(canvas, pageData, visual = isVisual)
            drawEquipmentAndComment(canvas, pageData, visual = isVisual)

            if (isVisual) {
                drawVisualNarrative(canvas, pageData)
                drawImageBox(
                    canvas = canvas,
                    x = mm(26f),
                    y = mm(93f),
                    w = mm(121f),
                    h = mm(90f),
                    bmp = pageData.irBitmap,
                    fileName = pageData.irFileName,
                    date = pageData.irFileDate,
                    time = pageData.irFileTime
                )
                drawImageBox(
                    canvas = canvas,
                    x = mm(150f),
                    y = mm(93f),
                    w = mm(121f),
                    h = mm(90f),
                    bmp = pageData.photoBitmap,
                    fileName = pageData.photoFileName,
                    date = pageData.photoFileDate,
                    time = pageData.photoFileTime
                )
            } else {
                drawThermalBlocks(canvas, pageData)
                drawImageBox(
                    canvas = canvas,
                    x = mm(80f),
                    y = mm(105f),
                    w = mm(102f),
                    h = mm(76f),
                    bmp = pageData.irBitmap,
                    fileName = pageData.irFileName,
                    date = pageData.irFileDate,
                    time = pageData.irFileTime
                )
                drawImageBox(
                    canvas = canvas,
                    x = mm(185f),
                    y = mm(105f),
                    w = mm(102f),
                    h = mm(76f),
                    bmp = pageData.photoBitmap,
                    fileName = pageData.photoFileName,
                    date = pageData.photoFileDate,
                    time = pageData.photoFileTime
                )
            }

            document.finishPage(page)
        }

        document.writeTo(output)
        document.close()
    }
}
