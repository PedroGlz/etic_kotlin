package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipos")
data class Equipo(
    @PrimaryKey
    @ColumnInfo(name = "Id_Equipo")
    val idEquipo: String,

    @ColumnInfo(name = "Equipo")
    val equipo: String? = null,

    @ColumnInfo(name = "Descr_equipo")
    val descrEquipo: String? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = null,

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
