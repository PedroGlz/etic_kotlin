package com.example.etic.reports

import android.graphics.Bitmap

data class BaselineReportHeaderData(
    val cliente: String,
    val sitio: String,
    val analista: String,
    val nivel: String,
    val inspeccionAnterior: String,
    val fechaAnterior: String,
    val inspeccionActual: String,
    val fechaActual: String,
    val fechaReporte: String
)

data class BaselineGraphPoint(
    val label: String,
    val noInspeccion: Int?,
    val mta: Double?,
    val tempMax: Double?,
    val tempAmb: Double?
)

data class BaselineHistoryRow(
    val noInspeccion: String,
    val fechaInspeccion: String,
    val estatusInspeccion: String,
    val mta: String,
    val tempMax: String,
    val tempAmb: String,
    val notas: String
)

data class BaselineReportPageData(
    val codigoBarras: String,
    val fabricante: String,
    val prioridadOperacion: String,
    val ruta: String,
    val archivoIr: String,
    val archivoId: String,
    val irBitmap: Bitmap?,
    val idBitmap: Bitmap?,
    val irFecha: String,
    val irHora: String,
    val idFecha: String,
    val idHora: String,
    val historyRows: List<BaselineHistoryRow>,
    val graphPoints: List<BaselineGraphPoint>
)
