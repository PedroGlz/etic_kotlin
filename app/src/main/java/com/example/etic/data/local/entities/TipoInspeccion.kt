package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipo_inspecciones")
data class TipoInspeccion(
    @PrimaryKey
    @ColumnInfo(name = "Id_Tipo_Inspeccion")
    val idTipoInspeccion: String,

    @ColumnInfo(name = "Tipo_Inspeccion")
    val tipoInspeccion: String? = null,

    @ColumnInfo(name = "Desc_Inspeccion")
    val descInspeccion: String? = null,

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
    val idSitio: String? = "flag_export"
)

