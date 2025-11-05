package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspecciones")
data class Inspeccion(
    @PrimaryKey
    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = null,

    @ColumnInfo(name = "Id_Grupo_Sitios")
    val idGrupoSitios: String? = null,

    @ColumnInfo(name = "Id_Cliente")
    val idCliente: String? = null,

    @ColumnInfo(name = "Id_Status_Inspeccion")
    val idStatusInspeccion: String? = null,

    @ColumnInfo(name = "Fecha_Inicio")
    val fechaInicio: String? = null,

    @ColumnInfo(name = "Fecha_Fin")
    val fechaFin: String? = null,

    @ColumnInfo(name = "Fotos_Ruta")
    val fotosRuta: String? = null,

    @ColumnInfo(name = "IR_Imagen_Inicial")
    val irImagenInicial: String? = null,

    @ColumnInfo(name = "DIG_Imagen_Inicial")
    val digImagenInicial: String? = null,

    @ColumnInfo(name = "No_Dias")
    val noDias: Int? = null,

    @ColumnInfo(name = "Unidad_Temp")
    val unidadTemp: String? = null,

    @ColumnInfo(name = "No_Inspeccion")
    val noInspeccion: Int? = null,

    @ColumnInfo(name = "No_Inspeccion_Ant")
    val noInspeccionAnt: Int? = null,

    @ColumnInfo(name = "Estatus")
    val estatus: String? = "Activo",

    @ColumnInfo(name = "Creado_Por")
    val creadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Creacion")
    val fechaCreacion: String? = null,

    @ColumnInfo(name = "Modificado_Por")
    val modificadoPor: String? = null,

    @ColumnInfo(name = "Fecha_Mod")
    val fechaMod: String? = null
)
