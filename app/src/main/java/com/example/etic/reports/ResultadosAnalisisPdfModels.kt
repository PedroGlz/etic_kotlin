package com.example.etic.reports

import android.graphics.Bitmap

data class ResultadosAnalisisPdfHeader(
    val cliente: String,
    val sitio: String,
    val grupoSitio: String,
    val direccionCompleta: String,
    val municipio: String,
    val estado: String,
    val fechaServicio: String,
    val fechaServicioNarrativa: String,
    val fechaServicioAnterior: String,
    val analista: String,
    val nivel: String,
    val telefono: String,
    val email: String
)

data class ResultadosAnalisisPdfStats(
    val totalHallazgos: Int,
    val totalElectricos: Int,
    val cronicosElectricos: Int,
    val electricosCerrados: Int,
    val totalMecanicos: Int,
    val cronicosMecanicos: Int,
    val mecanicosCerrados: Int,
    val totalVisuales: Int,
    val cronicosVisuales: Int,
    val visualesCerrados: Int,
    val totalCriticos: Int,
    val totalSerios: Int,
    val totalImportantes: Int,
    val totalMenores: Int,
    val totalNormal: Int
) {
    val totalT1: Int get() = totalElectricos + totalMecanicos
    val totalCronicosT1: Int get() = cronicosElectricos + cronicosMecanicos
    val totalCerradosT1: Int get() = electricosCerrados + mecanicosCerrados
}

data class ResultadosAnalisisPdfRecommendationEntry(
    val texto: String,
    val imagen1: Bitmap?,
    val imagen2: Bitmap?
)

data class ResultadosAnalisisPdfData(
    val header: ResultadosAnalisisPdfHeader,
    val contactos: List<ResultadosAnalisisContacto>,
    val portada: Bitmap?,
    val logoCliente: Bitmap?,
    val descripciones: List<String>,
    val areasInspeccionadas: List<String>,
    val stats: ResultadosAnalisisPdfStats,
    val recomendaciones: List<ResultadosAnalisisPdfRecommendationEntry>,
    val referencias: List<String>,
    val logo: Bitmap?,
    val isoLogo: Bitmap?
)
