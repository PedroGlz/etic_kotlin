package com.example.etic.reports

data class InventoryRow(
    val ruta: String,
    val ubicacion: String,
    val codigoBarras: String,
    val estatus: String,
    val esEquipo: Boolean
)

data class InventoryReportRow(
    val estatus: String,
    val prioridad: String,
    val problemas: String,
    val elemento: String,
    val codigoBarras: String,
    val notas: String,
    val level: Int,
    val isParentUbicacion: Boolean
)

data class InventoryHeaderData(
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
