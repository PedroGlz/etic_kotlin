package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estatus_color_text")
data class EstatusColorText(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id_Estatus_Color_Text")
    val idEstatusColorText: Int? = null,

    @ColumnInfo(name = "Color_Text")
    val colorText: String? = null,

    @ColumnInfo(name = "Descripcion")
    val descripcion: String,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = "flag_export",

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export",
)
