package com.example.etic.reports

data class InventoryRow(
    val ruta: String,
    val ubicacion: String,
    val codigoBarras: String,
    val estatus: String,
    val esEquipo: Boolean
)
