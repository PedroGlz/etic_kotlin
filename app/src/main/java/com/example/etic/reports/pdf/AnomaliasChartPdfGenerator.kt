package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import com.example.etic.reports.AnomaliaBarData
import com.example.etic.reports.ReportHeaderData
import java.io.OutputStream
import kotlin.math.max
import kotlin.math.min

class AnomaliasChartPdfGenerator {

    fun generate(
        output: OutputStream,
        header: ReportHeaderData,
        bars: List<AnomaliaBarData>,
        cronicos: Int,
        logo: Bitmap? = null,
        clientLogo: Bitmap? = null
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
            color = android.graphics.Color.rgb(0, 22, 102)
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
        val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1f
            color = android.graphics.Color.rgb(171, 171, 171)
        }
        val barValuePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(12f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.rgb(0, 22, 102)
            textAlign = Paint.Align.CENTER
        }
        val centeredTextPaint = TextPaint(textPaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(7f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        }
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val footerBottomMargin = mm(8f)

        val page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
        val c = page.canvas

        fun drawLogo() {
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

        fun drawClientLogo() {
            if (clientLogo == null) return
            val targetW = mm(38f)
            val scale = min(targetW / clientLogo.width, 1f)
            val bmp = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    clientLogo,
                    max(1, (clientLogo.width * scale).toInt()),
                    max(1, (clientLogo.height * scale).toInt()),
                    true
                )
            } else {
                clientLogo
            }
            val drawX = mm(287f) - bmp.width
            c.drawBitmap(bmp, drawX, mm(10f), null)
        }

        fun drawRightText(text: String, rightX: Float, y: Float, paint: TextPaint) {
            c.drawText(text, rightX - paint.measureText(text), y, paint)
        }

        fun drawFooter() {
            val line1 = "ETIC PdM System V01-2026"
            val line2 = "Copyright © $currentYear Todos los derechos reservados."
            val w1 = footerPaint.measureText(line1)
            val w2 = footerPaint.measureText(line2)
            val line2Y = pageHeight - footerBottomMargin
            val line1Y = line2Y - mm(4f)
            c.drawText(line1, (pageWidth - w1) / 2f, line1Y, footerPaint)
            c.drawText(line2, (pageWidth - w2) / 2f, line2Y, footerPaint)
        }

        drawLogo()
        drawClientLogo()
        val centeredTitlePaint = TextPaint(titlePaint).apply {
            textAlign = Paint.Align.CENTER
        }
        val titleCenterX = mm(150f)
        c.drawText(
            "Gráfica de anomalías/hallazgos detectados",
            titleCenterX,
            mm(14f),
            centeredTitlePaint
        )
        c.drawText(
            "durante la auditoría-inspección termográfica.",
            titleCenterX,
            mm(19f),
            centeredTitlePaint
        )

        var y = mm(31f)
        c.drawText(header.cliente, mm(10f), y, boldPaint); y += mm(4f)
        c.drawText(header.sitio, mm(10f), y, textPaint); y += mm(4f)
        c.drawText("Analista termógrafo: ${header.analista}", mm(10f), y, textPaint); y += mm(4f)
        c.drawText("Nivel de certificación: ${header.nivel}", mm(10f), y, textPaint)

        val rightX = mm(287f)
        drawRightText("Fecha Reporte: ${header.fechaReporte}", rightX, mm(31f), textPaint)
        drawRightText("No. inspección anterior: ${header.inspeccionAnterior}", rightX, mm(35f), textPaint)
        drawRightText("No. inspección actual: ${header.inspeccionActual}", rightX, mm(39f), textPaint)

        val chartX = mm(10f)
        val chartY = mm(47f)
        val chartW = mm(277f)
        val chartH = mm(145f)
        val topPad = mm(10f)
        val leftPad = mm(10f)
        val bottomPad = mm(15f)
        val chartBoxX = chartX + leftPad
        val chartBoxY = chartY + topPad
        val chartBoxW = chartW - leftPad
        val chartBoxH = chartH - bottomPad - topPad

        c.drawLine(chartBoxX, chartBoxY, chartBoxX, chartBoxY + chartBoxH, axisPaint)
        c.drawLine(chartBoxX - mm(2f), chartBoxY + chartBoxH, chartBoxX + chartBoxW, chartBoxY + chartBoxH, axisPaint)

        val roundedMax = max(10, ((bars.maxOfOrNull { it.value } ?: 1) + 9) / 10 * 10)
        val gridSteps = roundedMax / 10
        for (step in 0..gridSteps) {
            val value = roundedMax - (step * 10)
            val ratio = value.toFloat() / roundedMax.toFloat()
            val yPos = chartBoxY + chartBoxH - (chartBoxH * ratio)
            c.drawLine(chartBoxX - mm(2f), yPos, chartBoxX + chartBoxW, yPos, axisPaint)
            c.drawText(value.toString(), chartBoxX - mm(7f), yPos + mm(1f), textPaint)
        }

        val xLabelW = chartBoxW / max(1, bars.size)
        val barW = min(mm(22f), xLabelW * 0.55f)
        bars.forEachIndexed { index, bar ->
            val barCenterX = chartBoxX + (xLabelW * index) + (xLabelW / 2f)
            val barH = if (roundedMax == 0) 0f else chartBoxH * (bar.value.toFloat() / roundedMax.toFloat())
            val barX = barCenterX - (barW / 2f)
            val barY = chartBoxY + chartBoxH - barH
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                color = bar.color
            }
            c.drawRect(RectF(barX, barY, barX + barW, barY + barH), fill)
            c.drawText(bar.value.toString(), barCenterX, barY - mm(1f), barValuePaint)

            val labelLines = bar.label.split(" ")
            var labelY = chartBoxY + chartBoxH + mm(5f)
            labelLines.forEach { line ->
                c.drawText(line, barCenterX, labelY, centeredTextPaint)
                labelY += mm(4f)
            }
        }

        val chronicBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.rgb(232, 76, 76)
        }
        val chronicRect = RectF(mm(221f), mm(47f), mm(287f), mm(55f))
        c.drawRect(chronicRect, chronicBg)
        val chronicPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(10f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.WHITE
        }
        c.drawText("Anomalías / Hallazgos crónicos: $cronicos", mm(223f), mm(52f), chronicPaint)

        drawFooter()
        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}


