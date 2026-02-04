package com.example.etic.reports

import androidx.annotation.ColorInt

data class ReportHeaderData(
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

data class ProblemListRow(
    val equipoComentarios: String,
    val fechaCreacion: String,
    val noInspeccion: String,
    val tipoNumero: String,
    val estatusProblema: String,
    val esCronico: String,
    val temperaturaProblema: String,
    val deltaT: String,
    val severidad: String
)

data class AnomaliaBarData(
    val label: String,
    @ColorInt val color: Int,
    val value: Int
)

object ReportSeverityIds {
    const val CRITICO = "1D56EDB0-8D6E-11D3-9270-006008A19766"
    const val SERIO = "1D56EDB1-8D6E-11D3-9270-006008A19766"
    const val IMPORTANTE = "1D56EDB2-8D6E-11D3-9270-006008A19766"
    const val MENOR = "1D56EDB3-8D6E-11D3-9270-006008A19766"
}

