package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipo_fallas")
data class TipoFalla(
    @PrimaryKey
    @ColumnInfo(name = "Id_Tipo_Falla")
    val idTipoFalla: String,

    @ColumnInfo(name = "Id_Tipo_Inspeccion")
    val idTipoInspeccion: String? = null,

    @ColumnInfo(name = "Tipo_Falla")
    val tipoFalla: String? = null,

    @ColumnInfo(name = "Desc_Tipo_Falla")
    val descTipoFalla: String? = null,

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

