package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tipo_ambientes")
data class TipoAmbiente(
    @PrimaryKey
    @ColumnInfo(name = "Id_Tipo_Ambiente")
    val idTipoAmbiente: String,

    @ColumnInfo(name = "Nombre")
    val nombre: String? = null,

    @ColumnInfo(name = "Descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "Adjust")
    val adjust: Double? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = "Activo",

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = "flag_export",

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export",
)

