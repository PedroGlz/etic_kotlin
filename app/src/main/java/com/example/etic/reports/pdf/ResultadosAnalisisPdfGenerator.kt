package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import com.example.etic.reports.ResultadosAnalisisPdfData
import java.io.OutputStream
import java.util.Calendar
import kotlin.math.max
import kotlin.math.min

class ResultadosAnalisisPdfGenerator {

    fun generate(output: OutputStream, data: ResultadosAnalisisPdfData) {
        data class StyledSegment(
            val text: String,
            val color: Int,
            val bold: Boolean = false,
            val underline: Boolean = false
        )

        data class StyledWord(
            val text: String,
            val paint: TextPaint
        )

        val doc = PdfDocument()
        val dpi = 150f
        val pageWidth = ((220f / 25.4f) * dpi).toInt()
        val pageHeight = ((280f / 25.4f) * dpi).toInt()

        fun mm(v: Float) = v * dpi / 25.4f
        fun pt(v: Float) = v * dpi / 72f

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = pt(20f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val headingBluePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 2, 83)
            textSize = pt(11f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val headingBlueUnderlinePaint = TextPaint(headingBluePaint).apply {
            isUnderlineText = true
        }
        val sectionBluePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 4, 173)
            textSize = pt(12f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = pt(11f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val bodyBoldPaint = TextPaint(bodyPaint).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val bodyUnderlinePaint = TextPaint(bodyPaint).apply {
            isUnderlineText = true
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = pt(8f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val captionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(0, 32, 96)
            textSize = pt(11f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val tableHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = pt(10f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val tableTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = pt(10f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        val tableBoldPaint = TextPaint(tableTextPaint).apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
        }

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val contentTopMm = 35f
        val pageBreakMm = 241f

        var pageNumber = 0
        lateinit var page: PdfDocument.Page
        lateinit var canvas: Canvas
        var currentY = mm(contentTopMm)

        fun finishCurrentPage() {
            doc.finishPage(page)
        }

        fun drawBitmapInBox(bitmap: Bitmap, xMm: Float, yMm: Float, widthMm: Float, heightMm: Float? = null) {
            val targetW = mm(widthMm)
            val targetH = heightMm?.let(::mm)
            val widthRatio = targetW / bitmap.width
            val heightRatio = targetH?.let { it / bitmap.height }
            val ratio = heightRatio?.let { min(widthRatio, it) } ?: widthRatio
            val scaled = if (ratio == 1f) {
                bitmap
            } else {
                Bitmap.createScaledBitmap(
                    bitmap,
                    max(1, (bitmap.width * ratio).toInt()),
                    max(1, (bitmap.height * ratio).toInt()),
                    true
                )
            }
            val drawX = mm(xMm)
            val drawY = mm(yMm)
            if (targetH != null) {
                val centeredY = drawY + ((targetH - scaled.height) / 2f)
                canvas.drawBitmap(scaled, drawX, centeredY, null)
            } else {
                canvas.drawBitmap(scaled, drawX, drawY, null)
            }
        }

        fun drawFooter() {
            val footerY = pageHeight - mm(30f)
            val x = mm(30f)
            canvas.drawLine(x, footerY - mm(2f), pageWidth - x, footerY - mm(2f), linePaint)

            fun footerRow(left: String, right: String, row: Int) {
                val y = footerY + mm(row * 4f)
                canvas.drawText(left, x, y, footerPaint)
                val rightWidth = footerPaint.measureText(right)
                canvas.drawText(right, pageWidth - x - rightWidth, y, footerPaint)
            }

            footerRow("Especialistas en Termografía Industrial y Corporativa, S.A. de C.V.", "Sucursal Bajío", 1)
            footerRow("Sucursal matriz", "Col. El Dorado, C.P. 37590", 2)
            footerRow("Col. Las Américas, C.P. 55076", "León, Guanajuato", 3)
            footerRow("Ecatepec de Morelos, Estado de México", "Teléfonos: 55 8032 5401", 4)
            footerRow("Teléfonos: 55 8032 5401", "F-PRS-02", 5)
        }

        fun drawPageHeader() {
            if (pageNumber > 1) {
                data.logo?.let { drawBitmapInBox(it, 147f, 10f, 38f) }
            }
            drawFooter()
        }

        fun startPage(startYmm: Float = contentTopMm) {
            if (pageNumber > 0) finishCurrentPage()
            pageNumber += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            currentY = mm(startYmm)
            drawPageHeader()
        }

        fun measureTextHeight(text: CharSequence, paint: TextPaint, widthMm: Float): Float {
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, mm(widthMm).toInt())
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0f, 1f)
                .build()
            return layout.height.toFloat()
        }

        fun drawTextBlock(
            text: CharSequence,
            xMm: Float,
            yMm: Float,
            widthMm: Float,
            paint: TextPaint,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL
        ): Float {
            val widthPx = mm(widthMm).toInt()
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, paint, widthPx)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1f)
                .build()
            canvas.save()
            canvas.translate(mm(xMm), mm(yMm))
            layout.draw(canvas)
            canvas.restore()
            return yMm + (layout.height * 25.4f / dpi)
        }

        fun wrapPlainText(text: String, paint: TextPaint, maxWidthPx: Float): List<String> {
            val normalized = text.replace("\r", "")
            val out = mutableListOf<String>()
            normalized.split("\n").forEach { paragraph ->
                if (paragraph.isBlank()) {
                    out += ""
                } else {
                    val words = paragraph.trim().split(Regex("\\s+"))
                    var current = ""
                    words.forEach { word ->
                        val candidate = if (current.isBlank()) word else "$current $word"
                        if (paint.measureText(candidate) <= maxWidthPx) {
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

        fun drawJustifiedTextBlock(
            text: String,
            xMm: Float,
            yMm: Float,
            widthMm: Float,
            paint: TextPaint,
            lineHeightMm: Float = 5f
        ): Float {
            // Justificación manual para simular el bloque de texto del PDF original.
            val lines = wrapPlainText(text, paint, mm(widthMm))
            var currentLineY = yMm
            lines.forEachIndexed { index, line ->
                val isLastLine = index == lines.lastIndex || line.isBlank()
                val words = line.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                if (isLastLine || words.size <= 1) {
                    canvas.drawText(line, mm(xMm), mm(currentLineY + 4.2f), paint)
                } else {
                    val wordsWidth = words.sumOf { paint.measureText(it).toDouble() }.toFloat()
                    val gapCount = words.size - 1
                    val extraSpace = ((mm(widthMm) - wordsWidth) / gapCount).coerceAtLeast(paint.measureText(" "))
                    var drawX = mm(xMm)
                    words.forEachIndexed { wordIndex, word ->
                        canvas.drawText(word, drawX, mm(currentLineY + 4.2f), paint)
                        drawX += paint.measureText(word)
                        if (wordIndex < words.lastIndex) drawX += extraSpace
                    }
                }
                currentLineY += lineHeightMm
            }
            return currentLineY
        }

        fun buildPaintForSegment(segment: StyledSegment): TextPaint =
            TextPaint(bodyPaint).apply {
                color = segment.color
                typeface = Typeface.create(
                    Typeface.SANS_SERIF,
                    if (segment.bold) Typeface.BOLD else Typeface.NORMAL
                )
                isUnderlineText = segment.underline
            }

        // Justifica párrafos con estilos mezclados sin perder color ni peso tipográfico.
        fun drawJustifiedStyledTextBlock(
            segments: List<StyledSegment>,
            xMm: Float,
            yMm: Float,
            widthMm: Float,
            lineHeightMm: Float = 5f
        ): Float {
            // Mantiene color y estilo por segmento mientras distribuye el texto por línea.
            val words = mutableListOf<StyledWord>()
            segments.forEach { segment ->
                val paint = buildPaintForSegment(segment)
                segment.text.trim().split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .forEach { word -> words += StyledWord(word, paint) }
            }
            if (words.isEmpty()) return yMm

            val maxWidthPx = mm(widthMm)
            val lines = mutableListOf<List<StyledWord>>()
            var currentLine = mutableListOf<StyledWord>()
            var currentWidth = 0f
            val minSpace = bodyPaint.measureText(" ")

            words.forEach { word ->
                val wordWidth = word.paint.measureText(word.text)
                val candidateWidth = if (currentLine.isEmpty()) wordWidth else currentWidth + minSpace + wordWidth
                if (candidateWidth <= maxWidthPx || currentLine.isEmpty()) {
                    if (currentLine.isNotEmpty()) currentWidth += minSpace
                    currentLine += word
                    currentWidth += wordWidth
                } else {
                    lines += currentLine.toList()
                    currentLine = mutableListOf(word)
                    currentWidth = wordWidth
                }
            }
            if (currentLine.isNotEmpty()) lines += currentLine.toList()

            var currentLineY = yMm
            lines.forEachIndexed { index, line ->
                val isLastLine = index == lines.lastIndex
                if (isLastLine || line.size <= 1) {
                    var drawX = mm(xMm)
                    line.forEachIndexed { wordIndex, word ->
                        canvas.drawText(word.text, drawX, mm(currentLineY + 4.2f), word.paint)
                        drawX += word.paint.measureText(word.text)
                        if (wordIndex < line.lastIndex) drawX += minSpace
                    }
                } else {
                    val wordsWidth = line.sumOf { it.paint.measureText(it.text).toDouble() }.toFloat()
                    val extraSpace = ((maxWidthPx - wordsWidth) / (line.size - 1)).coerceAtLeast(minSpace)
                    var drawX = mm(xMm)
                    line.forEachIndexed { wordIndex, word ->
                        canvas.drawText(word.text, drawX, mm(currentLineY + 4.2f), word.paint)
                        drawX += word.paint.measureText(word.text)
                        if (wordIndex < line.lastIndex) drawX += extraSpace
                    }
                }
                currentLineY += lineHeightMm
            }
            return currentLineY
        }

        fun drawSingleLine(text: String, xMm: Float, yMm: Float, paint: TextPaint, align: Paint.Align = Paint.Align.LEFT) {
            val previous = paint.textAlign
            paint.textAlign = align
            canvas.drawText(text, mm(xMm), mm(yMm), paint)
            paint.textAlign = previous
        }

        // Centra cada línea manualmente para evitar el desplazamiento visual de StaticLayout en captions cortos.
        fun drawCenteredParagraph(
            text: String,
            yMm: Float,
            widthMm: Float,
            paint: TextPaint,
            startXmm: Float = (220f - widthMm) / 2f,
            lineHeightMm: Float = 5f
        ): Float {
            val lines = wrapPlainText(text, paint, mm(widthMm))
            var lineY = yMm
            lines.forEach { line ->
                val lineWidthMm = paint.measureText(line) * 25.4f / dpi
                val drawX = startXmm + ((widthMm - lineWidthMm) / 2f)
                drawSingleLine(line, drawX, lineY + 4.2f, paint)
                lineY += lineHeightMm
            }
            return lineY
        }

        val bulletBaselineOffsetMm = 4.2f

        fun ensureSpace(heightMm: Float) {
            if ((currentY * 25.4f / dpi) + heightMm > pageBreakMm) {
                startPage()
            }
        }

        fun drawRow(label: String, value: String) {
            val yMm = currentY * 25.4f / dpi
            val rowHeightMm = max(
                5f,
                max(
                    measureTextHeight(label, bodyBoldPaint, 47f) * 25.4f / dpi,
                    measureTextHeight(value, bodyPaint, 113f) * 25.4f / dpi
                )
            )
            ensureSpace(rowHeightMm + 1f)
            drawTextBlock(label, 30f, yMm, 47f, bodyBoldPaint)
            drawTextBlock(value, 77f, yMm, 113f, bodyPaint)
            currentY = mm(yMm + rowHeightMm)
        }

        fun drawBullet(text: CharSequence, bulletLabel: String? = null, xMm: Float = 37f, widthMm: Float = 153f, gapAfterMm: Float = 1f) {
            ensureSpace(10f)
            val yMm = currentY * 25.4f / dpi
            if (bulletLabel != null) {
                drawSingleLine(bulletLabel, xMm - 7f, yMm + bulletBaselineOffsetMm, bodyPaint)
            } else {
                drawSingleLine("\u2022", xMm - 3f, yMm + bulletBaselineOffsetMm, bodyPaint)
            }
            val endY = drawTextBlock(text, xMm, yMm, widthMm, bodyPaint, Layout.Alignment.ALIGN_NORMAL)
            currentY = mm(endY + gapAfterMm)
        }

        fun drawBulletJustified(text: String, bulletLabel: String? = null, xMm: Float = 37f, widthMm: Float = 153f, gapAfterMm: Float = 1f) {
            ensureSpace(10f)
            val yMm = currentY * 25.4f / dpi
            if (bulletLabel != null) {
                drawSingleLine(bulletLabel, xMm - 7f, yMm + bulletBaselineOffsetMm, bodyPaint)
            } else {
                drawSingleLine("\u2022", xMm - 3f, yMm + bulletBaselineOffsetMm, bodyPaint)
            }
            val endY = drawJustifiedTextBlock(text, xMm, yMm, widthMm, bodyPaint)
            currentY = mm(endY + gapAfterMm)
        }

        fun drawBulletJustifiedSegments(segments: List<StyledSegment>, bulletLabel: String? = null, xMm: Float = 37f, widthMm: Float = 153f, gapAfterMm: Float = 1f) {
            ensureSpace(10f)
            val yMm = currentY * 25.4f / dpi
            if (bulletLabel != null) {
                drawSingleLine(bulletLabel, xMm - 7f, yMm + bulletBaselineOffsetMm, bodyPaint)
            } else {
                drawSingleLine("\u2022", xMm - 3f, yMm + bulletBaselineOffsetMm, bodyPaint)
            }
            val endY = drawJustifiedStyledTextBlock(segments, xMm, yMm, widthMm)
            currentY = mm(endY + gapAfterMm)
        }

        fun drawTableCell(
            xMm: Float,
            yMm: Float,
            wMm: Float,
            hMm: Float,
            text: String,
            textColor: Int,
            fillColor: Int,
            paint: TextPaint,
            align: Layout.Alignment = Layout.Alignment.ALIGN_CENTER
        ) {
            fillPaint.color = fillColor
            val left = mm(xMm)
            val top = mm(yMm)
            val right = mm(xMm + wMm)
            val bottom = mm(yMm + hMm)
            canvas.drawRect(left, top, right, bottom, fillPaint)
            canvas.drawRect(left, top, right, bottom, linePaint)
            val prevColor = paint.color
            val prevAlign = paint.textAlign
            paint.color = textColor
            val innerWidthPx = mm(wMm - 2f)
            val lines = wrapPlainText(text, paint, innerWidthPx)
            val lineHeightMm = 4.4f
            val totalHeightMm = lines.size * lineHeightMm
            var lineY = yMm + ((hMm - totalHeightMm) / 2f).coerceAtLeast(0.8f) + 3.4f
            lines.forEach { line ->
                val lineWidthPx = paint.measureText(line)
                val drawX = when (align) {
                    Layout.Alignment.ALIGN_CENTER -> mm(xMm) + ((mm(wMm) - lineWidthPx) / 2f)
                    Layout.Alignment.ALIGN_OPPOSITE -> mm(xMm + wMm - 1f) - lineWidthPx
                    else -> mm(xMm + 1f)
                }
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(line, drawX, mm(lineY), paint)
                lineY += lineHeightMm
            }
            paint.color = prevColor
            paint.textAlign = prevAlign
        }

        fun richText(vararg parts: Pair<String, Int>, boldAll: Boolean = false, underline: Boolean = false): CharSequence {
            val builder = SpannableStringBuilder()
            parts.forEach { (text, color) ->
                val start = builder.length
                builder.append(text)
                builder.setSpan(ForegroundColorSpan(color), start, builder.length, 0)
                if (boldAll) builder.setSpan(StyleSpan(Typeface.BOLD), start, builder.length, 0)
                if (underline) builder.setSpan(UnderlineSpan(), start, builder.length, 0)
            }
            return builder
        }

        startPage(startYmm = 0f)

        data.logo?.let { drawBitmapInBox(it, 25f, 25f, 53f) }
        data.isoLogo?.let { drawBitmapInBox(it, 18f, 9f, 20f) }
        data.logoCliente?.let { drawBitmapInBox(it, 139f, 23f, 51f, 21f) }

        drawTextBlock(
            "F-PRS-02 - Resultados de análisis de riesgos con termografía infrarroja",
            30f,
            55f,
            160f,
            titlePaint,
            Layout.Alignment.ALIGN_CENTER
        )

        data.portada?.let { portada ->
            if (data.portada2 == null) {
                drawBitmapInBox(portada, 57f, 80f, 106f)
            } else {
                val portadaWidthMm = 90f
                val portadaGapMm = 2f
                val totalWidthMm = portadaWidthMm * 2f + portadaGapMm
                val startX = (220f - totalWidthMm) / 2f
                drawBitmapInBox(portada, startX, 80f, portadaWidthMm)
                drawBitmapInBox(data.portada2, startX + portadaWidthMm + portadaGapMm, 80f, portadaWidthMm)
            }
        }

        val portadaContactos = data.contactos.filter { it.nombre.isNotBlank() && it.puesto.isNotBlank() }
        val manyContacts = portadaContactos.size > 4
        currentY = if (manyContacts) mm(160f) else mm(166f)
        drawRow("Cliente:", data.header.cliente)
        if (data.header.grupoSitio.isNotBlank()) {
            val yMm = currentY * 25.4f / dpi
            drawTextBlock(data.header.grupoSitio, 72f, yMm, 118f, bodyPaint)
            currentY = mm(yMm + (measureTextHeight(data.header.grupoSitio, bodyPaint, 118f) * 25.4f / dpi))
        }
        drawRow("", data.header.sitio)
        drawRow("", data.header.direccionCompleta.uppercase())
        currentY += if (manyContacts) mm(2.5f) else mm(13f)

        portadaContactos.forEachIndexed { index, contacto ->
            ensureSpace(6f)
            val yMm = currentY * 25.4f / dpi
            if (index == 0) drawSingleLine("Contactos:", 30f, yMm + 4.4f, bodyBoldPaint)
            drawSingleLine("${contacto.nombre} -", 76f, yMm + 4.4f, bodyPaint)
            drawSingleLine(" ${contacto.puesto}", 76f + (bodyPaint.measureText("${contacto.nombre} -") * 25.4f / dpi), yMm + 4.4f, bodyBoldPaint)
            currentY += mm(5f)
        }
        currentY += mm(8f)
        drawRow("Fecha Servicio:", data.header.fechaServicio)
        drawRow("Fecha Servicio anterior:", data.header.fechaServicioAnterior)
        drawRow("Realizó:", "${data.header.analista} / Termógrafo certificado ${data.header.nivel}.")

        startPage()
        drawSingleLine(data.header.cliente, 190f, 39.4f, bodyBoldPaint, Paint.Align.RIGHT)
        if (data.header.grupoSitio.isNotBlank()) {
            drawTextBlock(data.header.grupoSitio, 30f, 40f, 160f, bodyPaint, Layout.Alignment.ALIGN_OPPOSITE)
        }
        drawTextBlock(
            "Asunto: Entrega de informe de servicio de análisis de riesgo con termografía infrarroja.",
            30f,
            64f,
            160f,
            bodyUnderlinePaint,
            Layout.Alignment.ALIGN_OPPOSITE
        )
        drawSingleLine(if (data.contactos.getOrNull(1)?.nombre?.isNotBlank() == true) "Estimados:" else "Estimado:", 30f, 89f, bodyBoldPaint)
        var saludoY = 94f
        data.contactos.filter { it.nombre.isNotBlank() && it.puesto.isNotBlank() }.forEach {
            drawSingleLine(it.nombre, 30f, saludoY, bodyPaint)
            saludoY += 5f
        }
        currentY = mm(120f)
        val municipioEstado = listOf(data.header.municipio, data.header.estado).filter { it.isNotBlank() }.joinToString(", ")
        currentY = mm(
            drawJustifiedTextBlock(
                "Por este medio, hacemos entrega de los resultados finales de la inspección por termografía infrarroja realizada en las instalaciones eléctricas y mecánicas de ${data.header.sitio}, ubicadas en $municipioEstado. Servicio realizado ${data.header.fechaServicioNarrativa}.",
                30f,
                120f,
                160f,
                bodyPaint
            ) + 5f
        )
        currentY = mm(drawJustifiedTextBlock("Agradecemos a ustedes la confianza y facilidades otorgadas durante la ejecución de nuestro servicio. Asimismo, expresamos nuestro reconocimiento a su personal técnico por su colaboración y profesionalismo.", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 5f)
        currentY = mm(drawJustifiedTextBlock("Sin más, quedamos atentos a sus amables comentarios", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 5f)
        currentY = mm(drawTextBlock("Cordialmente,", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 30f)
        drawSingleLine(data.header.analista, 30f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        drawSingleLine("Especialistas en Termografía Industrial y", 30f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        drawSingleLine("Corporativa, S.A. de C.V.", 30f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        drawSingleLine("www.etic-infrared.mx", 30f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        drawSingleLine("Celular:", 30f, currentY * 25.4f / dpi, bodyPaint)
        drawSingleLine(data.header.telefono, 45f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        val emailPaint = TextPaint(bodyPaint).apply {
            color = Color.rgb(0, 4, 173)
            isUnderlineText = true
        }
        drawSingleLine(data.header.email, 30f, currentY * 25.4f / dpi, emailPaint)

        startPage()
        drawSingleLine("I. Resumen Ejecutivo", 30f, 39.4f, headingBlueUnderlinePaint)
        currentY = mm(48f)
        currentY = mm(
            drawJustifiedTextBlock(
                "Personal de ETIC, S.A. de C.V., se presentó en las instalaciones de ${data.header.sitio} ubicadas en ${data.header.municipio}, ${data.header.estado}, ${data.header.fechaServicioNarrativa}, con el objeto de realizar la inspección por termografía infrarroja en los equipos seleccionados para su estudio definidos en conjunto con responsables de cada área, descargando la información recabada durante este servicio en el software ETIC System.",
                30f,
                currentY * 25.4f / dpi,
                160f,
                bodyPaint
            ) + 15f
        )
        drawSingleLine("1. Descripción del reporte:", 30f, currentY * 25.4f / dpi, sectionBluePaint)
        currentY += mm(10f)
        drawBulletJustifiedSegments(
            listOf(
                StyledSegment("Se genero el inventario de todos los equipos criticos en nuestra base de datos ETIC System,", Color.BLACK),
                StyledSegment("(Inventario De Equipo).", Color.rgb(79, 129, 189)),
                StyledSegment("Se colocó un código de barras que ayudará a tener un control de rápida identificación para futuras inspecciones.", Color.BLACK)
            ),
            bulletLabel = "a."
        )
        drawBulletJustifiedSegments(
            listOf(
                StyledSegment("Se tomaron termogramas como referencia del comportamiento actual de los equipos esenciales para la operación definidos en la reunión inicial; se documentan en la sección de baseline", Color.BLACK),
                StyledSegment("(Baseline Equipo En Monitoreo Informe de Tendencias)", Color.rgb(79, 129, 189)),
                StyledSegment("con objeto de conocer la tendencia del comportamiento de temperatura de estos equipos.", Color.BLACK)
            ),
            bulletLabel = "b."
        )
        drawBulletJustifiedSegments(
            listOf(
                StyledSegment("Los problemas identificados en esta inspección han sido documentados en las secciones eléctrica, mecánica y anomalías visuales, según aplique,", Color.BLACK),
                StyledSegment("(Eléctrico, Mecánico, Visual).", Color.rgb(79, 129, 189))
            ),
            bulletLabel = "c."
        )
        drawBulletJustified(
            "El total de problemas y anomalías documentados se enlista en la última sección, agrupándolos de acuerdo con el siguiente criterio:",
            bulletLabel = "e."
        )
        drawBulletJustified("Lista de todos los problemas abiertos.")
        drawBulletJustified("Lista de todos los problemas cerrados.")
        data.descripciones.filter { it.isNotBlank() }.forEach { drawBulletJustified(it) }

        startPage()
        currentY = mm(35f)
        currentY = mm(drawJustifiedTextBlock("Durante esta inspección, se tuvo como objeto principal la revisión de las instalaciones eléctricas críticas para la continuidad de su operación, a fin de identificar anomalías térmicas y/o anomalías visuales en los componentes y/o conexiones.", 30f, 35f, 160f, bodyPaint) + 5f)
        drawTextBlock("Las áreas/equipos inspeccionados durante el estudio de termografía infrarroja son los siguientes:", 37f, currentY * 25.4f / dpi, 153f, sectionBluePaint)
        currentY += mm(10f)
        data.areasInspeccionadas.filter { it.isNotBlank() }.forEach {
            drawBulletJustified(it, xMm = 37f, widthMm = 153f, gapAfterMm = 2f)
        }
        currentY += mm(8f)

        fun drawTable1(yMm: Float): Float {
            val c1 = 43f
            val c2 = 80f
            val c3 = 122f
            val c4 = 152f
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val deepBlue = Color.rgb(0, 32, 96)
            val red = Color.rgb(255, 0, 0)

            drawTableCell(c1, yMm, 37f, 10f, "Tipo de anomalía", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 37f, 6f, "Eléctricos", red, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 16f, 37f, 6f, "Mecánico", Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c1, yMm + 22f, 37f, 6f, "Total:", deepBlue, Color.WHITE, tableBoldPaint, Layout.Alignment.ALIGN_OPPOSITE)

            drawTableCell(c2, yMm, 37f, 10f, "Anomalías\nDocumentadas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c2, yMm + 10f, 37f, 6f, data.stats.totalElectricos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c2, yMm + 16f, 37f, 6f, data.stats.totalMecanicos.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 22f, 37f, 6f, data.stats.totalT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawTableCell(c3, yMm, 30f, 10f, "Anomalías\nCrónicas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 30f, 6f, data.stats.cronicosElectricos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 16f, 30f, 6f, data.stats.cronicosMecanicos.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c3, yMm + 22f, 30f, 6f, data.stats.totalCronicosT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawTableCell(c4, yMm, 25f, 10f, "Cerradas\nEn Sitio", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 25f, 6f, data.stats.electricosCerrados.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 16f, 25f, 6f, data.stats.mecanicosCerrados.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c4, yMm + 22f, 25f, 6f, data.stats.totalCerradosT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawCenteredParagraph(
                "Tabla No. 1 Listado de anomalías térmicas documentadas en esta inspección.",
                yMm + 30f,
                160f,
                captionPaint
            )
            return yMm + 39f
        }

        fun drawTable2(yMm: Float): Float {
            val c1 = 43f
            val c2 = 80f
            val c3 = 122f
            val c4 = 152f
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val deepBlue = Color.rgb(0, 32, 96)
            val visualBlue = Color.rgb(0, 112, 192)

            drawTableCell(c1, yMm, 37f, 10f, "Tipo de anomalía", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 37f, 6f, "Visuales", visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 16f, 37f, 6f, "Total:", deepBlue, Color.WHITE, tableBoldPaint, Layout.Alignment.ALIGN_OPPOSITE)

            drawTableCell(c2, yMm, 37f, 10f, "Anomalías\nDocumentadas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c2, yMm + 10f, 37f, 6f, data.stats.totalVisuales.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c2, yMm + 16f, 37f, 6f, data.stats.totalVisuales.toString(), Color.BLACK, Color.WHITE, tableTextPaint)

            drawTableCell(c3, yMm, 30f, 10f, "Anomalías\nCrónicas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 30f, 6f, data.stats.cronicosVisuales.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 16f, 30f, 6f, data.stats.cronicosVisuales.toString(), Color.BLACK, Color.WHITE, tableTextPaint)

            drawTableCell(c4, yMm, 25f, 10f, "Cerrados\nEn Sitio", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 25f, 6f, data.stats.visualesCerrados.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 16f, 25f, 6f, data.stats.visualesCerrados.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawCenteredParagraph(
                "Tabla No. 2 Listado de anomalías documentadas en esta inspección.",
                yMm + 24f,
                160f,
                captionPaint
            )
            return yMm + 33f
        }

        fun drawTable3(yMm: Float): Float {
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val red = Color.rgb(255, 0, 0)
            val orange = Color.rgb(227, 108, 10)
            val yellow = Color.rgb(255, 192, 0)
            val sky = Color.rgb(0, 112, 192)
            val darkGray = Color.rgb(74, 69, 69)
            val c1 = 30f
            val c2 = 65f
            val c3 = 100f
            val c4 = 132f

            drawTableCell(c1, yMm, 70f, 10f, "Clasificación por diferencial\nde temperatura", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 35f, 12f, "Críticos", red, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 22f, 35f, 12f, "Serios", orange, Color.WHITE, tableBoldPaint)
            drawTableCell(c1, yMm + 34f, 35f, 18f, "Importantes", yellow, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 52f, 35f, 12f, "Menores", sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c1, yMm + 64f, 35f, 12f, "Normal", sky, lightGray, tableBoldPaint)

            drawTableCell(c2, yMm + 10f, 35f, 12f, "Mayores a 16C", darkGray, lightGray, tableTextPaint)
            drawTableCell(c2, yMm + 22f, 35f, 12f, "De 9C a 15C", darkGray, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 34f, 35f, 18f, "De 4C a 8C", darkGray, lightGray, tableTextPaint)
            drawTableCell(c2, yMm + 52f, 35f, 12f, "De 1C a 3C", darkGray, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 64f, 35f, 12f, "0C", darkGray, lightGray, tableTextPaint)

            drawTableCell(c3, yMm, 32f, 10f, "Anomalías\nDocumentadas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 32f, 12f, data.stats.totalCriticos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 22f, 32f, 12f, data.stats.totalSerios.toString(), orange, Color.WHITE, tableTextPaint)
            drawTableCell(c3, yMm + 34f, 32f, 18f, data.stats.totalImportantes.toString(), yellow, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 52f, 32f, 12f, data.stats.totalMenores.toString(), sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c3, yMm + 64f, 32f, 12f, data.stats.totalNormal.toString(), sky, lightGray, tableBoldPaint)

            drawTableCell(c4, yMm, 58f, 10f, "Acción recomendada", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 58f, 12f, "Discrepancia mayor, reparar inmediatamente", red, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 22f, 58f, 12f, "Monitoreo hasta que se pueda realizar medidas correctivas", orange, Color.WHITE, tableBoldPaint)
            drawTableCell(c4, yMm + 34f, 58f, 18f, "Indica posible deficiencia, reparar según lo permita el tiempo", yellow, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 52f, 58f, 12f, "Posible deficiencia, requiere de investigación", sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c4, yMm + 64f, 58f, 12f, "---", sky, lightGray, tableBoldPaint)

            drawCenteredParagraph(
                "Tabla No. 3 Listado de anomalías térmicas eléctricas por diferencial de temperatura, agrupadas de acuerdo con el diferencial de temperatura, basado en la comparación entre componentes similares bajo carga similar (ANSI-NETA) \"International Electric Testing Association Inc-Approved American National Standards\".",
                yMm + 78f,
                160f,
                captionPaint
            )
            return yMm + 90f
        }

        if ((currentY * 25.4f / dpi) + 37f > pageBreakMm) startPage()
        currentY = mm(drawTable1(currentY * 25.4f / dpi) + 8f)
        if ((currentY * 25.4f / dpi) + 31f > pageBreakMm) startPage()
        currentY = mm(drawTable2(currentY * 25.4f / dpi) + 8f)
        if ((currentY * 25.4f / dpi) + 96f > pageBreakMm) startPage()
        currentY = mm(drawTable3(currentY * 25.4f / dpi) + 5f)

        startPage()
        drawSingleLine("2. Recomendaciones:", 30f, 39.4f, sectionBluePaint)
        currentY = mm(50f)
        data.recomendaciones.forEach { recomendacion ->
            if (recomendacion.texto.isBlank() && recomendacion.imagen1 == null && recomendacion.imagen2 == null) return@forEach
            ensureSpace(65f)
            val startYmm = currentY * 25.4f / dpi
            drawSingleLine("\u2022", 30f, startYmm + bulletBaselineOffsetMm, bodyPaint)
            currentY = mm(
                drawJustifiedTextBlock(
                    recomendacion.texto.ifBlank { " " },
                    33f,
                    startYmm,
                    157f,
                    bodyPaint
                ) + 1f
            )
            when {
                recomendacion.imagen1 != null && recomendacion.imagen2 != null -> {
                    drawBitmapInBox(recomendacion.imagen1, 28f, currentY * 25.4f / dpi, 60f, 50f)
                    drawBitmapInBox(recomendacion.imagen2, 125f, currentY * 25.4f / dpi, 60f, 50f)
                    currentY += mm(53f)
                }
                recomendacion.imagen1 != null -> {
                    drawBitmapInBox(recomendacion.imagen1, 75f, currentY * 25.4f / dpi, 60f, 50f)
                    currentY += mm(53f)
                }
                recomendacion.imagen2 != null -> {
                    drawBitmapInBox(recomendacion.imagen2, 75f, currentY * 25.4f / dpi, 60f, 50f)
                    currentY += mm(53f)
                }
                else -> currentY += mm(4f)
            }
        }

        startPage()
        drawSingleLine("3. Referencias:", 30f, 39.4f, sectionBluePaint)
        currentY = mm(50f)
        data.referencias.filter { it.isNotBlank() }.forEach {
            ensureSpace(8f)
            val startYmm = currentY * 25.4f / dpi
            drawSingleLine("\u2022", 30f, startYmm + bulletBaselineOffsetMm, bodyPaint)
            currentY = mm(drawJustifiedTextBlock(it, 33f, startYmm, 157f, bodyPaint) + 2f)
        }

        finishCurrentPage()
        doc.writeTo(output)
        doc.close()
    }
}
