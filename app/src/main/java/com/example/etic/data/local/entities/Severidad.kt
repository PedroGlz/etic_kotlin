package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "severidades")
data class Severidad(
    @PrimaryKey
    @ColumnInfo(name = "Id_Severidad")
    val idSeveridad: String,

    @ColumnInfo(name = "Severidad")
    val severidad: String? = null,

    @ColumnInfo(name = "Descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = "flag_export",

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export"
)
