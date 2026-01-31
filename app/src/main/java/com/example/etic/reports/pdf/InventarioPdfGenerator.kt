package com.example.etic.reports.pdf

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.etic.reports.InventoryRow
import java.io.OutputStream

class InventarioPdfGenerator {

    fun generate(
        output: OutputStream,
        noInspeccion: String,
        sitio: String,
        rows: List<InventoryRow>,
        logo: Bitmap? = null
    ) {
        val doc = PdfDocument()

        val pageWidth = 1240 // aprox A4 horizontal a 150dpi
        val pageHeight = 1754

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val smallPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            color = Color.WHITE
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 20f }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.LTGRAY
        }
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(7, 71, 166) }

        val margin = 50f
        val rowH = 44f
        val headerH = 54f

        // Column widths (ajústalas luego)
        val wRuta = 520f
        val wUbic = 320f
        val wCb = 220f
        val wEst = 130f

        val tableX = margin
        val tableW = wRuta + wUbic + wCb + wEst

        var y = margin
        var pageNumber = 1

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = doc.startPage(pageInfo)
            val c = page.canvas

            // Header
            if (logo != null) {
                val maxW = 200f
                val maxH = 60f
                val scale = kotlin.math.min(maxW / logo.width, maxH / logo.height)
                val bmp = if (scale < 1f) {
                    Bitmap.createScaledBitmap(
                        logo,
                        (logo.width * scale).toInt(),
                        (logo.height * scale).toInt(),
                        true
                    )
                } else {
                    logo
                }
                c.drawBitmap(bmp, pageWidth - margin - bmp.width, margin - 6f, null)
            }
            c.drawText("Reporte de Inventarios", margin, y + 30f, titlePaint)
            c.drawText("No. Inspección: $noInspeccion", margin, y + 65f, smallPaint)
            c.drawText("Sitio: $sitio", margin, y + 92f, smallPaint)

            // Table header
            val thY = y + 130f
            c.drawRect(tableX, thY, tableX + tableW, thY + headerH, headerBg)

            var x = tableX
            c.drawText("Ruta", x + 10f, thY + 35f, headerPaint); x += wRuta
            c.drawText("Ubicación", x + 10f, thY + 35f, headerPaint); x += wUbic
            c.drawText("Código barras", x + 10f, thY + 35f, headerPaint); x += wCb
            c.drawText("Estatus", x + 10f, thY + 35f, headerPaint)

            // Column separators
            x = tableX
            c.drawLine(x, thY, x, thY + headerH, linePaint); x += wRuta
            c.drawLine(x, thY, x, thY + headerH, linePaint); x += wUbic
            c.drawLine(x, thY, x, thY + headerH, linePaint); x += wCb
            c.drawLine(x, thY, x, thY + headerH, linePaint)
            c.drawRect(tableX, thY, tableX + tableW, thY + headerH, linePaint)

            // footer
            c.drawText("Página $pageNumber", pageWidth - margin - 140f, pageHeight - 40f, smallPaint)

            // set cursor under header
            y = thY + headerH
            return page
        }

        var page = newPage()
        var c = page.canvas

        fun endPage() {
            doc.finishPage(page)
            pageNumber++
        }

        rows.forEachIndexed { idx, r ->
            val bottom = y + rowH

            // salto de página si ya no cabe (deja margen para footer)
            if (bottom > pageHeight - 90f) {
                endPage()
                page = newPage()
                c = page.canvas
            }

            // zebra
            if (idx % 2 == 1) {
                val zebra = Paint().apply { color = Color.argb(25, 0, 0, 0) }
                c.drawRect(tableX, y, tableX + tableW, bottom, zebra)
            }

            var x = tableX
            c.drawText(ellipsize(r.ruta, 60), x + 10f, y + 30f, textPaint); x += wRuta
            c.drawText(ellipsize(r.ubicacion, 28), x + 10f, y + 30f, textPaint); x += wUbic
            c.drawText(ellipsize(r.codigoBarras, 18), x + 10f, y + 30f, textPaint); x += wCb
            c.drawText(ellipsize(r.estatus, 10), x + 10f, y + 30f, textPaint)

            // grid
            x = tableX
            c.drawLine(x, y, x, bottom, linePaint); x += wRuta
            c.drawLine(x, y, x, bottom, linePaint); x += wUbic
            c.drawLine(x, y, x, bottom, linePaint); x += wCb
            c.drawLine(x, y, x, bottom, linePaint)
            c.drawRect(tableX, y, tableX + tableW, bottom, linePaint)

            y = bottom
        }

        // cerrar última página
        doc.finishPage(page)

        doc.writeTo(output)
        doc.close()
    }

    private fun ellipsize(text: String, max: Int): String =
        if (text.length <= max) text else text.take(max - 1) + "…"
}
