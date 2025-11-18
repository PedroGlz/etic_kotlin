package com.example.etic.data.local.views

import androidx.room.ColumnInfo

data class VistaUbicacionArbol(
    @ColumnInfo(name = "Id_Inspeccion_Det") val idInspeccionDet: String,
    @ColumnInfo(name = "Id_Ubicacion") val idUbicacion: String,
    @ColumnInfo(name = "Id_Sitio") val idSitio: String?,
    @ColumnInfo(name = "nombreUbicacion") val nombreUbicacion: String?,
    @ColumnInfo(name = "level") val level: Int?,
    @ColumnInfo(name = "Codigo_Barras") val codigoBarras: String?,
    @ColumnInfo(name = "Es_Equipo") val esEquipo: String?,
    @ColumnInfo(name = "Estatus") val estatus: String?,
    @ColumnInfo(name = "Id_Tipo_Prioridad") val idTipoPrioridad: String?,
    @ColumnInfo(name = "Descripcion") val descripcion: String?,
    @ColumnInfo(name = "Id_Fabricante") val idFabricante: String?,
    @ColumnInfo(name = "Id_Ubicacion_Padre") val idUbicacionPadre: String?,
    @ColumnInfo(name = "Id_Status_Inspeccion_Det") val idStatusInspeccionDet: String?,
    @ColumnInfo(name = "Id_Inspeccion") val idInspeccion: String?,
    @ColumnInfo(name = "No_Inspeccion") val noInspeccion: Int?,
    @ColumnInfo(name = "Fecha_inspeccion") val fechaInspeccion: String?,
    @ColumnInfo(name = "Notas_Inspeccion") val notasInspeccion: String?,
    @ColumnInfo(name = "path") val path: String?,
    @ColumnInfo(name = "Fecha_Creacion") val fechaCreacion: String?,
    @ColumnInfo(name = "icon") val icon: String?,
    @ColumnInfo(name = "Estatus_Inspeccion_Det") val estatusInspeccionDet: String?,
    @ColumnInfo(name = "color") val color: String?,
    @ColumnInfo(name = "expanded") val expanded: String?,
    @ColumnInfo(name = "selected") val selected: String?
)
