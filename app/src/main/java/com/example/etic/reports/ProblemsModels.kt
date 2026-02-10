package com.example.etic.reports

import android.graphics.Bitmap

data class ProblemReportHeaderData(
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

data class ProblemReportPageData(
    val idProblema: String,
    val tipoInspeccionId: String?,
    val tipoInspeccion: String,
    val numeroProblema: Int?,
    val tipoProblemaTag: String,
    val esCronico: String,
    val prioridadOperacion: String,
    val prioridadReparacion: String,
    val fechaReporte: String,
    val hallazgoVisual: String,
    val observaciones: String,
    val temperaturaAnomalia: String,
    val temperaturaReferencia: String,
    val diferencialTemperatura: String,
    val temperaturaAmbiente: String,
    val tipoAmbiente: String,
    val velocidadViento: String,
    val ajusteViento: String,
    val ajusteCarga: String,
    val fabricante: String,
    val voltajeCircuito: String,
    val corrienteNominal: String,
    val faseProblema: String,
    val faseReferencia: String,
    val faseAdicional: String,
    val rmsProblema: String,
    val rmsReferencia: String,
    val rmsAdicional: String,
    val emisividad: String,
    val codigoBarras: String,
    val ruta: String,
    val irFileName: String,
    val irFileDate: String,
    val irFileTime: String,
    val photoFileName: String,
    val photoFileDate: String,
    val photoFileTime: String,
    val irBitmap: Bitmap?,
    val photoBitmap: Bitmap?,
    val graphPoints: List<ProblemGraphPoint> = emptyList()
)

data class ProblemGraphPoint(
    val label: String,
    val problemTemp: Double?,
    val referenceTemp: Double?
)

object ProblemTypeIds {
    const val ELECTRICO = "0D32B331-76C3-11D3-82BF-00104BC75DC2"
    const val ELECTRICO_2 = "0D32B332-76C3-11D3-82BF-00104BC75DC2"
    const val VISUAL = "0D32B333-76C3-11D3-82BF-00104BC75DC2"
    const val AISLAMIENTO_TERMICO = "0D32B335-76C3-11D3-82BF-00104BC75DC2"
}
