package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspecciones_det")
data class InspeccionDet(
    @PrimaryKey
    @ColumnInfo(name = "Id_Inspeccion_Det")
    val idInspeccionDet: String,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = null,

    @ColumnInfo(name = "Id_Ubicacion")
    val idUbicacion: String? = null,

    @ColumnInfo(name = "Id_Status_Inspeccion_Det")
    val idStatusInspeccionDet: String? = null,

    @ColumnInfo(name = "Notas_Inspeccion")
    val notasInspeccion: String? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String = "Activo",

    @ColumnInfo(name = "Id_Estatus_Color_Text")
    val idEstatusColorText: Int? = 1,

    @ColumnInfo(name = "expanded")
    val expanded: String? = "0",

    @ColumnInfo(name = "selected")
    val selected: String? = "0",

    @ColumnInfo(name = "Creado_Por")
    val creadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Creacion")
    val fechaCreacion: String? = null,

    @ColumnInfo(name = "Modificado_Por")
    val modificadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Mod")
    val fechaMod: String? = null,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export"
)

