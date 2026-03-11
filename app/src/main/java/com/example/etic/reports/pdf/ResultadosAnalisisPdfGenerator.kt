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
            textAlign = Paint.Align.CENTER
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

            footerRow("Especialistas en Termografia Industrial y Corporativa, S.A. de C.V.", "Sucursal Bajio", 1)
            footerRow("Sucursal matriz", "Col. El Dorado, C.P. 37590", 2)
            footerRow("Col. Las Americas, C.P. 55076", "Leon, Guanajuato", 3)
            footerRow("Ecatepec de Morelos, Estado de Mexico", "Telefonos: 55 8032 5401", 4)
            footerRow("Telefonos: 55 8032 5401", "F-PRS-02", 5)
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

        fun drawSingleLine(text: String, xMm: Float, yMm: Float, paint: TextPaint, align: Paint.Align = Paint.Align.LEFT) {
            val previous = paint.textAlign
            paint.textAlign = align
            canvas.drawText(text, mm(xMm), mm(yMm), paint)
            paint.textAlign = previous
        }

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
                drawSingleLine(bulletLabel, xMm - 7f, yMm + 4.4f, bodyPaint)
            } else {
                drawSingleLine("\u2022", xMm - 3f, yMm + 4.4f, bodyPaint)
            }
            val endY = drawTextBlock(text, xMm, yMm, widthMm, bodyPaint, Layout.Alignment.ALIGN_NORMAL)
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
            paint.color = textColor
            val textHeightMm = drawTextBlock(text, xMm + 1f, yMm + ((hMm - 4f) / 2f), wMm - 2f, paint, align)
            if (textHeightMm > yMm + hMm) {
                drawTextBlock(text, xMm + 1f, yMm + 1f, wMm - 2f, paint, align)
            }
            paint.color = prevColor
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
            "F-PRS-02 - Resultados de analisis de riesgos con termografia infrarroja",
            30f,
            55f,
            160f,
            titlePaint,
            Layout.Alignment.ALIGN_CENTER
        )

        data.portada?.let { drawBitmapInBox(it, 57f, 80f, 106f) }

        currentY = mm(170f)
        drawRow("Cliente:", data.header.cliente)
        if (data.header.grupoSitio.isNotBlank()) {
            val yMm = currentY * 25.4f / dpi
            drawTextBlock(data.header.grupoSitio, 72f, yMm, 118f, bodyPaint)
            currentY = mm(yMm + (measureTextHeight(data.header.grupoSitio, bodyPaint, 118f) * 25.4f / dpi))
        }
        drawRow("", data.header.sitio)
        drawRow("", data.header.direccionCompleta.uppercase())
        currentY += mm(13f)

        data.contactos.filter { it.nombre.isNotBlank() && it.puesto.isNotBlank() }.forEachIndexed { index, contacto ->
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
        drawRow("Realizo:", "${data.header.analista} / Termografo Certificado ${data.header.nivel}.")

        startPage()
        drawSingleLine(data.header.cliente, 190f, 39.4f, bodyBoldPaint, Paint.Align.RIGHT)
        if (data.header.grupoSitio.isNotBlank()) {
            drawTextBlock(data.header.grupoSitio, 30f, 40f, 160f, bodyPaint, Layout.Alignment.ALIGN_OPPOSITE)
        }
        drawTextBlock(
            "Asunto: Entrega de informe servicio analisis de riesgo con termografia infrarroja.",
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
            drawTextBlock(
                "Por este medio, hacemos entrega de los resultados finales de la inspeccion por termografia infrarroja realizada en las instalaciones electricas y mecanicas de ${data.header.sitio}, ubicadas en $municipioEstado. Servicio realizado ${data.header.fechaServicioNarrativa}.",
                30f,
                120f,
                160f,
                bodyPaint,
                Layout.Alignment.ALIGN_NORMAL
            ) + 5f
        )
        currentY = mm(drawTextBlock("Agradecemos a ustedes la confianza y facilidades otorgadas durante la ejecucion de nuestro servicio. Asi mismo, expresamos nuestro reconocimiento a su personal tecnico por su colaboracion y profesionalismo.", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 5f)
        currentY = mm(drawTextBlock("Sin mas, quedamos atentos a sus amables comentarios", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 5f)
        currentY = mm(drawTextBlock("Cordialmente,", 30f, currentY * 25.4f / dpi, 160f, bodyPaint) + 30f)
        drawSingleLine(data.header.analista, 30f, currentY * 25.4f / dpi, bodyPaint)
        currentY += mm(5f)
        drawSingleLine("Especialistas en Termografia Industrial y", 30f, currentY * 25.4f / dpi, bodyPaint)
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
            drawTextBlock(
                "Personal de ETIC, S.A. de C.V., se presento en las instalaciones de ${data.header.sitio} ubicadas en ${data.header.municipio}, ${data.header.estado}, ${data.header.fechaServicioNarrativa}, con el objeto de realizar la Inspeccion por Termografia Infrarroja en los equipos seleccionados para su estudio definidos en conjunto con responsables de cada area, descargando la informacion recabada durante este servicio en el software ETIC System.",
                30f,
                currentY * 25.4f / dpi,
                160f,
                bodyPaint
            ) + 15f
        )
        drawSingleLine("1. Descripcion del reporte:", 30f, currentY * 25.4f / dpi, sectionBluePaint)
        currentY += mm(10f)
        drawBullet(
            richText(
                "Se genero el inventario de todos los equipos criticos en nuestra base de datos ETIC System, " to Color.BLACK,
                "(Inventario De Equipo). " to Color.rgb(79, 129, 189),
                "Se coloco un codigo de barras que ayudara a tener un control de rapida identificacion para futuras inspecciones." to Color.BLACK
            ),
            bulletLabel = "a."
        )
        drawBullet(
            richText(
                "Se tomaron termogramas como referencia del comportamiento actual de los equipos esenciales para la operacion definidos en la reunion inicial, se documentan en la seccion de baseline " to Color.BLACK,
                "(Baseline Equipo En Monitoreo Informe de Tendencias) " to Color.rgb(79, 129, 189),
                "con objeto de conocer la tendencia del comportamiento de temperatura de estos equipos." to Color.BLACK
            ),
            bulletLabel = "b."
        )
        drawBullet(
            richText(
                "Los problemas identificados en esta inspeccion, han sido documentados en las secciones electrica, mecanica y anomalias visuales; segun aplique, " to Color.BLACK,
                "(Electrico, Mecanico, Visual)." to Color.rgb(79, 129, 189)
            ),
            bulletLabel = "c."
        )
        drawBullet(
            "El total de problemas y anomalias documentados se enlistan en la ultima seccion agrupandolos de acuerdo al siguiente criterio:",
            bulletLabel = "e."
        )
        drawBullet("Lista de todos los problemas abiertos.")
        drawBullet("Lista de todos los problemas cerrados.")
        data.descripciones.filter { it.isNotBlank() }.forEach { drawBullet(it) }

        startPage()
        currentY = mm(35f)
        currentY = mm(drawTextBlock("Durante esta inspeccion, se tuvo como objeto principal, la revision de las instalaciones electricas criticas en la continuidad de su operacion para identificar anomalias termicas y/o anomalias visuales en los componentes y/o conexiones", 30f, 35f, 160f, bodyPaint) + 5f)
        drawTextBlock("Las areas/equipos inspeccionados durante el estudio de termografia infrarroja son las siguientes:", 37f, currentY * 25.4f / dpi, 153f, sectionBluePaint)
        currentY += mm(10f)
        data.areasInspeccionadas.filter { it.isNotBlank() }.forEach {
            drawBullet(it, xMm = 37f, widthMm = 153f, gapAfterMm = 2f)
        }
        currentY += mm(8f)

        fun drawTable1(yMm: Float): Float {
            val c1 = 38f
            val c2 = 75f
            val c3 = 117f
            val c4 = 147f
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val deepBlue = Color.rgb(0, 32, 96)
            val red = Color.rgb(255, 0, 0)

            drawTableCell(c1, yMm, 37f, 10f, "Tipo de Anomalia", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 37f, 6f, "Electricos", red, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 16f, 37f, 6f, "Mecanico", Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c1, yMm + 22f, 37f, 6f, "Total:", deepBlue, Color.WHITE, tableBoldPaint, Layout.Alignment.ALIGN_OPPOSITE)

            drawTableCell(c2, yMm, 37f, 10f, "Anomalias\nDocumentadas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c2, yMm + 10f, 37f, 6f, data.stats.totalElectricos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c2, yMm + 16f, 37f, 6f, data.stats.totalMecanicos.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 22f, 37f, 6f, data.stats.totalT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawTableCell(c3, yMm, 30f, 10f, "Anomalias\nCronicas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 30f, 6f, data.stats.cronicosElectricos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 16f, 30f, 6f, data.stats.cronicosMecanicos.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c3, yMm + 22f, 30f, 6f, data.stats.totalCronicosT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawTableCell(c4, yMm, 25f, 10f, "Cerradas\nEn Sitio", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 25f, 6f, data.stats.electricosCerrados.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 16f, 25f, 6f, data.stats.mecanicosCerrados.toString(), Color.BLACK, Color.WHITE, tableTextPaint)
            drawTableCell(c4, yMm + 22f, 25f, 6f, data.stats.totalCerradosT1.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawSingleLine("Tabla No. 1 Listado de anomalias termicas documentados en esta inspeccion.", 110f, yMm + 37f, captionPaint, Paint.Align.CENTER)
            return yMm + 45f
        }

        fun drawTable2(yMm: Float): Float {
            val c1 = 38f
            val c2 = 75f
            val c3 = 117f
            val c4 = 147f
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val deepBlue = Color.rgb(0, 32, 96)
            val visualBlue = Color.rgb(0, 112, 192)

            drawTableCell(c1, yMm, 37f, 10f, "Tipo de Anomalia", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 37f, 6f, "Visuales", visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 16f, 37f, 6f, "Total:", deepBlue, Color.WHITE, tableBoldPaint, Layout.Alignment.ALIGN_OPPOSITE)

            drawTableCell(c2, yMm, 37f, 10f, "Anomalias\nDocumentados", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c2, yMm + 10f, 37f, 6f, data.stats.totalVisuales.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c2, yMm + 16f, 37f, 6f, data.stats.totalVisuales.toString(), Color.BLACK, Color.WHITE, tableTextPaint)

            drawTableCell(c3, yMm, 30f, 10f, "Anomalias\nCronicos", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 30f, 6f, data.stats.cronicosVisuales.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 16f, 30f, 6f, data.stats.cronicosVisuales.toString(), Color.BLACK, Color.WHITE, tableTextPaint)

            drawTableCell(c4, yMm, 25f, 10f, "Cerrados\nEn Sitio", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 25f, 6f, data.stats.visualesCerrados.toString(), visualBlue, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 16f, 25f, 6f, data.stats.visualesCerrados.toString(), deepBlue, Color.WHITE, tableBoldPaint)

            drawSingleLine("Tabla No. 2 Listado de anomalias documentadas en esta inspeccion", 110f, yMm + 31f, captionPaint, Paint.Align.CENTER)
            return yMm + 39f
        }

        fun drawTable3(yMm: Float): Float {
            val blue = Color.rgb(51, 121, 204)
            val lightGray = Color.rgb(236, 236, 236)
            val red = Color.rgb(255, 0, 0)
            val orange = Color.rgb(227, 108, 10)
            val yellow = Color.rgb(255, 192, 0)
            val sky = Color.rgb(0, 112, 192)
            val darkGray = Color.rgb(74, 69, 69)
            val c1 = 25f
            val c2 = 60f
            val c3 = 95f
            val c4 = 127f

            drawTableCell(c1, yMm, 70f, 10f, "Clasificacion por diferencial\nde temperatura", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c1, yMm + 10f, 35f, 12f, "Criticos", red, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 22f, 35f, 12f, "Serios", orange, Color.WHITE, tableBoldPaint)
            drawTableCell(c1, yMm + 34f, 35f, 18f, "Importantes", yellow, lightGray, tableBoldPaint)
            drawTableCell(c1, yMm + 52f, 35f, 12f, "Menores", sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c1, yMm + 64f, 35f, 12f, "Normal", sky, lightGray, tableBoldPaint)

            drawTableCell(c2, yMm + 10f, 35f, 12f, "Mayores a 16C", darkGray, lightGray, tableTextPaint)
            drawTableCell(c2, yMm + 22f, 35f, 12f, "De 9C a 15C", darkGray, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 34f, 35f, 18f, "De 4C a 8C", darkGray, lightGray, tableTextPaint)
            drawTableCell(c2, yMm + 52f, 35f, 12f, "De 1C a 3C", darkGray, Color.WHITE, tableTextPaint)
            drawTableCell(c2, yMm + 64f, 35f, 12f, "0C", darkGray, lightGray, tableTextPaint)

            drawTableCell(c3, yMm, 32f, 10f, "Anomalias\nDocumentadas", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c3, yMm + 10f, 32f, 12f, data.stats.totalCriticos.toString(), red, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 22f, 32f, 12f, data.stats.totalSerios.toString(), orange, Color.WHITE, tableTextPaint)
            drawTableCell(c3, yMm + 34f, 32f, 18f, data.stats.totalImportantes.toString(), yellow, lightGray, tableBoldPaint)
            drawTableCell(c3, yMm + 52f, 32f, 12f, data.stats.totalMenores.toString(), sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c3, yMm + 64f, 32f, 12f, data.stats.totalNormal.toString(), sky, lightGray, tableBoldPaint)

            drawTableCell(c4, yMm, 58f, 10f, "Accion Recomendada", Color.WHITE, blue, tableHeaderPaint)
            drawTableCell(c4, yMm + 10f, 58f, 12f, "Discrepancia mayor, reparar inmediatamente", red, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 22f, 58f, 12f, "Monitoreo hasta que se pueda realizar medidas correctivas", orange, Color.WHITE, tableBoldPaint)
            drawTableCell(c4, yMm + 34f, 58f, 18f, "Indica posible deficiencia, reparar segun lo permita el tiempo", yellow, lightGray, tableBoldPaint)
            drawTableCell(c4, yMm + 52f, 58f, 12f, "Posible deficiencia, requiere de investigacion", sky, Color.WHITE, tableBoldPaint)
            drawTableCell(c4, yMm + 64f, 58f, 12f, "---", sky, lightGray, tableBoldPaint)

            drawTextBlock(
                "Tabla No. 3 Listado de anomalias termicas electricas por diferencial de temperatura Agrupados de acuerdo al Diferencial de Temperatura, basado en la comparacion entre componentes similares bajo similar carga (ANSI-NETA) \"International Electric Testing Association Inc-Approved American National Standards\"",
                20f,
                yMm + 82f,
                180f,
                captionPaint,
                Layout.Alignment.ALIGN_CENTER
            )
            return yMm + 96f
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
            drawSingleLine("\u2022", 30f, currentY * 25.4f / dpi + mm(4f), bodyPaint)
            currentY = mm(drawTextBlock(recomendacion.texto.ifBlank { " " }, 33f, currentY * 25.4f / dpi, 157f, bodyPaint) + 1f)
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
            drawSingleLine("\u2022", 30f, currentY * 25.4f / dpi + mm(4f), bodyPaint)
            currentY = mm(drawTextBlock(it, 33f, currentY * 25.4f / dpi, 157f, bodyPaint) + 2f)
        }

        finishCurrentPage()
        doc.writeTo(output)
        doc.close()
    }
}
