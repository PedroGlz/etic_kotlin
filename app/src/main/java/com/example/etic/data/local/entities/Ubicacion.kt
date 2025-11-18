package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ubicaciones")
data class Ubicacion(
    @PrimaryKey
    @ColumnInfo(name = "Id_Ubicacion")
    val idUbicacion: String,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = null,

    @ColumnInfo(name = "Id_Ubicacion_Padre")
    val idUbicacionPadre: String? = null,

    @ColumnInfo(name = "Id_Tipo_Prioridad")
    val idTipoPrioridad: String? = null,

    @ColumnInfo(name = "Id_Tipo_Inspeccion")
    val idTipoInspeccion: String? = null,

    @ColumnInfo(name = "Ubicacion")
    val ubicacion: String? = null,

    @ColumnInfo(name = "Descripcion")
    val descripcion: String? = null,

    @ColumnInfo(name = "Es_Equipo")
    val esEquipo: String? = null,

    @ColumnInfo(name = "Codigo_Barras")
    val codigoBarras: String? = null,

    @ColumnInfo(name = "Nivel_arbol")
    val nivelArbol: Int? = null,

    @ColumnInfo(name = "LIMITE")
    val limite: Double? = null,

    @ColumnInfo(name = "Fabricante")
    val fabricante: String? = null,

    @ColumnInfo(name = "Nombre_Foto")
    val nombreFoto: String? = null,

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
    val fechaMod: String? = null,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = null
)

