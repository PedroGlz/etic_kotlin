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
        fun mmX(v: Float) = pageWidth * (v / 220f)
        fun mmY(v: Float) = pageHeight * (v / 280f)

        val left = mmX(30f)
        val right = pageWidth - left
        val top = mmY(25f)
        val bottom = pageHeight - mmY(41f)
        val contentWidth = right - left
        var pageNo = 0
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNo).create())
        var canvas = page.canvas
        var y = top

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

        fun drawCenteredText(text: String, paint: Paint, baselineY: Float) {
            val x = left + ((contentWidth - paint.measureText(text)) / 2f)
            canvas.drawText(text, x, baselineY, paint)
        }

        lateinit var drawHeader: () -> Unit
        lateinit var newPage: () -> Unit

        val ensureSpace: (Float) -> Unit = { space ->
            if (y + space > bottom) newPage()
        }

        fun drawCenteredTextBlock(text: String, paint: Paint, maxWidth: Float = contentWidth, gapAfter: Float = 16f) {
            val lines = wrapText(text, paint, maxWidth)
            lines.forEach { line ->
                ensureSpace(paint.textSize + 8f)
                drawCenteredText(line, paint, y)
                y += paint.textSize + 8f
            }
            y += gapAfter
        }

        fun drawBitmapAtWidth(bitmap: Bitmap, x: Float, y: Float, width: Float) {
            val scale = (width / bitmap.width).coerceAtMost(1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else {
                bitmap
            }
            canvas.drawBitmap(scaled, x, y, null)
        }

        drawHeader = {
            data.logo?.let { logo ->
                drawBitmapAtWidth(logo, mmX(25f), mmY(25f), mmX(53f))
            }
            data.isoLogo?.let { isoLogo ->
                drawBitmapAtWidth(isoLogo, mmX(18f), mmY(9f), mmX(20f))
            }
        }

        newPage = {
            doc.finishPage(page)
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, ++pageNo).create())
            canvas = page.canvas
            y = top
            drawHeader()
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

        y = mmY(55f)
        drawCenteredTextBlock(
            "F-PRS-02 - Resultados de analisis de riesgos con termografia infrarroja",
            titlePaint,
            maxWidth = contentWidth
        )

        data.portada?.let { portada ->
            ensureSpace(350f)
            val scaled = scaleToFit(portada, mmX(106f).toInt(), mmY(70f).toInt())
            val portadaX = mmX(57f)
            val portadaY = mmY(80f)
            canvas.drawBitmap(scaled, portadaX, portadaY, null)
            y = portadaY + scaled.height + mmY(10f)
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
