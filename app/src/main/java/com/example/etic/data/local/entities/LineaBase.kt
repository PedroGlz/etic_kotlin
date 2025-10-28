package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "linea_base")
data class LineaBase(
    @PrimaryKey
    @ColumnInfo(name = "Id_Linea_Base")
    val idLineaBase: String,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = null,

    @ColumnInfo(name = "Id_Ubicacion")
    val idUbicacion: String? = null,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = null,

    @ColumnInfo(name = "Id_Inspeccion_Det")
    val idInspeccionDet: String? = null,

    @ColumnInfo(name = "MTA")
    val mta: Double? = null,

    @ColumnInfo(name = "Temp_max")
    val tempMax: Double? = null,

    @ColumnInfo(name = "Temp_amb")
    val tempAmb: Double? = null,

    @ColumnInfo(name = "Notas")
    val notas: String? = null,

    @ColumnInfo(name = "Archivo_IR")
    val archivoIr: String? = null,

    @ColumnInfo(name = "Archivo_ID")
    val archivoId: String? = null,

    @ColumnInfo(name = "Ruta")
    val ruta: String? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = null,

    @ColumnInfo(name = "Creado_Por")
    val creadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Creacion")
    val fechaCreacion: String? = null,

    @ColumnInfo(name = "Modificado_Por")
    val modificadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Mod")
    val fechaMod: String? = null
)

