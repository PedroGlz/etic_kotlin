package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.etic.reports.ResultadosAnalisisPdfData
import java.io.OutputStream
import kotlin.math.min

class ResultadosAnalisisPdfGenerator {
    fun generate(output: OutputStream, data: ResultadosAnalisisPdfData) {
        val doc = PdfDocument()
        val pageWidth = 1240
        val pageHeight = 1754
        val left = 72f
        val right = pageWidth - 72f
        val bottom = pageHeight - 72f
        var pageNo = 0
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNo).create())
        var canvas = page.canvas
        var y = 72f

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 24f
        }
        val titlePaint = Paint(textPaint).apply {
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(0, 60, 110)
        }
        val sectionPaint = Paint(textPaint).apply {
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.rgb(0, 80, 150)
        }
        val labelPaint = Paint(textPaint).apply {
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint(textPaint).apply {
            textSize = 20f
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(240, 245, 250)
        }

        fun drawHeader() {
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 52f, fillPaint)
            data.logo?.let { logo ->
                val scaled = scaleToFit(logo, 220, 80)
                canvas.drawBitmap(scaled, left, 60f, null)
            }
            canvas.drawText("Resultados de analisis de riesgos", right - 440f, 105f, titlePaint)
            canvas.drawLine(left, 132f, right, 132f, linePaint)
            y = 160f
        }

        fun newPage() {
            doc.finishPage(page)
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNo).create())
            canvas = page.canvas
            y = 72f
            drawHeader()
        }

        fun ensureSpace(space: Float) {
            if (y + space > bottom) newPage()
        }

        fun drawTextBlock(text: String, paint: Paint, indent: Float = 0f, gapAfter: Float = 16f) {
            val paragraphs = text.split("\n")
            paragraphs.forEach { paragraph ->
                val safe = if (paragraph.isBlank()) " " else paragraph
                val lines = wrapText(safe, paint, right - left - indent)
                lines.forEach { line ->
                    ensureSpace(paint.textSize + 8f)
                    canvas.drawText(line, left + indent, y, paint)
                    y += paint.textSize + 8f
                }
            }
            y += gapAfter
        }

        fun drawBulletList(items: List<String>, paint: Paint, bullet: String = "-") {
            items.filter { it.isNotBlank() }.forEach { item ->
                val lines = wrapText(item.trim(), paint, right - left - 44f)
                lines.forEachIndexed { index, line ->
                    ensureSpace(paint.textSize + 8f)
                    val prefix = if (index == 0) "$bullet " else "  "
                    canvas.drawText(prefix + line, left, y, paint)
                    y += paint.textSize + 8f
                }
                y += 8f
            }
        }

        fun drawSection(title: String) {
            ensureSpace(56f)
            canvas.drawText(title, left, y, sectionPaint)
            y += 16f
            canvas.drawLine(left, y, right, y, linePaint)
            y += 28f
        }

        drawHeader()

        canvas.drawText("F-PRS-02 - Resultados de analisis de riesgos con termografia infrarroja", left, y, titlePaint)
        y += 54f

        data.portada?.let { portada ->
            ensureSpace(350f)
            val scaled = scaleToFit(portada, (right - left).toInt(), 320)
            val x = left + ((right - left) - scaled.width) / 2f
            canvas.drawBitmap(scaled, x, y, null)
            y += scaled.height + 28f
        }

        drawTextBlock("Cliente: ${data.header.cliente}", labelPaint, gapAfter = 8f)
        drawTextBlock("Sitio: ${data.header.sitio}", labelPaint, gapAfter = 8f)
        if (data.header.grupoSitio.isNotBlank()) {
            drawTextBlock("Grupo: ${data.header.grupoSitio}", labelPaint, gapAfter = 8f)
        }
        if (data.header.direccionCompleta.isNotBlank()) {
            drawTextBlock("Direccion: ${data.header.direccionCompleta}", smallPaint, gapAfter = 8f)
        }
        drawTextBlock("Fecha servicio: ${data.header.fechaServicio}", labelPaint, gapAfter = 8f)
        if (data.header.fechaServicioAnterior.isNotBlank()) {
            drawTextBlock("Fecha servicio anterior: ${data.header.fechaServicioAnterior}", labelPaint, gapAfter = 8f)
        }
        drawTextBlock("Realizo: ${data.header.analista} / Termografo certificado ${data.header.nivel}.", smallPaint)

        if (data.contactos.any { it.nombre.isNotBlank() || it.puesto.isNotBlank() }) {
            drawSection("Contactos")
            data.contactos.forEach { contacto ->
                if (contacto.nombre.isNotBlank() || contacto.puesto.isNotBlank()) {
                    drawTextBlock("${contacto.nombre} - ${contacto.puesto}".trim(' ', '-'), smallPaint, gapAfter = 6f)
                }
            }
        }

        if (data.descripciones.isNotEmpty()) {
            drawSection("Descripcion del reporte")
            drawBulletList(data.descripciones, smallPaint)
        }

        if (data.areasInspeccionadas.isNotEmpty()) {
            drawSection("Areas inspeccionadas")
            drawBulletList(data.areasInspeccionadas, smallPaint)
        }

        if (data.metricas.isNotEmpty()) {
            drawSection("Resumen")
            data.metricas.forEach { metric ->
                drawTextBlock("${metric.label}: ${metric.value}", smallPaint, gapAfter = 6f)
            }
        }

        if (data.recomendaciones.isNotEmpty()) {
            drawSection("Recomendaciones")
            data.recomendaciones.forEach { recomendacion ->
                if (recomendacion.texto.isBlank()) return@forEach
                drawBulletList(listOf(recomendacion.texto), smallPaint)
            }
        }

        if (data.referencias.isNotEmpty()) {
            drawSection("Referencias")
            drawBulletList(data.referencias, smallPaint)
        }

        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        if (words.isEmpty()) return listOf("")
        val lines = mutableListOf<String>()
        var current = ""
        words.forEach { word ->
            val candidate = if (current.isBlank()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = candidate
            } else {
                if (current.isNotBlank()) lines.add(current)
                current = word
            }
        }
        if (current.isNotBlank()) lines.add(current)
        return lines.ifEmpty { listOf(text) }
    }

    private fun scaleToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val ratio = min(
            maxWidth.toFloat() / bitmap.width.toFloat(),
            maxHeight.toFloat() / bitmap.height.toFloat()
        ).coerceAtMost(1f)
        if (ratio == 1f) return bitmap
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    }
}
