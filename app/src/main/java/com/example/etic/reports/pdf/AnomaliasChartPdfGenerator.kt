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
        val barLabelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = pt(12f)
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            color = android.graphics.Color.rgb(0, 22, 102)
        }

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
            } else logo
            c.drawBitmap(bmp, mm(11f), mm(10f), null)
        }

        drawLogo()
        c.drawText(
            "Grafica de anomalias/hallazgos detectadas durante la auditoria-inspeccion termografica.",
            mm(40f),
            mm(16f),
            titlePaint
        )

        var y = mm(31f)
        c.drawText(header.cliente, mm(10f), y, boldPaint); y += mm(4f)
        c.drawText(header.sitio, mm(10f), y, textPaint); y += mm(4f)
        c.drawText("Analista Termografo: ${header.analista}", mm(10f), y, textPaint); y += mm(4f)
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

        val dataMax = max(1, bars.maxOfOrNull { it.value } ?: 1)
        val yUnits = chartBoxH / dataMax
        for (i in 0..dataMax step 10) {
            val yPos = chartBoxY + yUnits * i
            c.drawLine(chartBoxX - mm(2f), yPos, mm(282f), yPos, axisPaint)
            c.drawText((dataMax - i).toString(), chartBoxX - mm(7f), yPos + mm(1f), textPaint)
        }

        val xLabelW = chartBoxW / max(1, bars.size)
        val barW = mm(20f)
        bars.forEachIndexed { i, b ->
            val barH = yUnits * b.value
            val barX = chartBoxX + (xLabelW / 2f) + (xLabelW * i) - (barW / 2f)
            val barY = chartBoxY + chartBoxH - barH
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL_AND_STROKE
                color = b.color
            }
            c.drawRect(RectF(barX, barY, barX + barW, barY + barH), fill)
            c.drawText(b.value.toString(), barX + mm(6f), barY - mm(1f), barLabelPaint)
            val labelLines = b.label.split(" ")
            var ly = chartBoxY + chartBoxH + mm(5f)
            labelLines.forEach {
                c.drawText(it, barX - mm(6f), ly, textPaint)
                ly += mm(4f)
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
        c.drawText("Anomalias / Hallazgos Cronicos: $cronicos", mm(223f), mm(52f), chronicPaint)

        doc.finishPage(page)
        doc.writeTo(output)
        doc.close()
    }
}

