package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estatus_inspeccion_det")
data class EstatusInspeccionDet(
    @PrimaryKey
    @ColumnInfo(name = "Id_Status_Inspeccion_Det")
    val idStatusInspeccionDet: String,

    @ColumnInfo(name = "Estatus_Inspeccion_Det")
    val estatusInspeccionDet: String? = null,

    @ColumnInfo(name = "Desc_Estatus_Det")
    val descEstatusDet: String? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = "Activo",

    @ColumnInfo(name = "Creado_Por")
    val creadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Creacion")
    val fechaCreacion: String? = null,

    @ColumnInfo(name = "Modificado_Por")
    val modificadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Mod")
    val fechaMod: String? = null,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = "flag_export",

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export",
)
