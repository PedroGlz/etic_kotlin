package com.example.etic.reports

import android.graphics.Bitmap

data class ResultadosAnalisisPdfHeader(
    val cliente: String,
    val sitio: String,
    val grupoSitio: String,
    val direccionCompleta: String,
    val fechaServicio: String,
    val fechaServicioAnterior: String,
    val analista: String,
    val nivel: String
)

data class ResultadosAnalisisPdfMetric(
    val label: String,
    val value: String
)

data class ResultadosAnalisisPdfData(
    val header: ResultadosAnalisisPdfHeader,
    val contactos: List<ResultadosAnalisisContacto>,
    val portada: Bitmap?,
    val descripciones: List<String>,
    val areasInspeccionadas: List<String>,
    val metricas: List<ResultadosAnalisisPdfMetric>,
    val recomendaciones: List<ResultadosAnalisisRecomendacion>,
    val referencias: List<String>,
    val logo: Bitmap?
)
