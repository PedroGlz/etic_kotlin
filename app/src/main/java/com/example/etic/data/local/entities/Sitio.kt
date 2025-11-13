package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sitios")
data class Sitio(
    @PrimaryKey
    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String,

    @ColumnInfo(name = "Id_Cliente")
    val idCliente: String? = null,

    @ColumnInfo(name = "Id_Grupo_Sitios")
    val idGrupoSitios: String? = null,

    @ColumnInfo(name = "Sitio")
    val sitio: String? = null,

    @ColumnInfo(name = "Desc_Sitio")
    val descSitio: String? = null,

    @ColumnInfo(name = "Direccion")
    val direccion: String? = null,

    @ColumnInfo(name = "Colonia")
    val colonia: String? = null,

    @ColumnInfo(name = "Estado")
    val estado: String? = null,

    @ColumnInfo(name = "Municipio")
    val municipio: String? = null,

    @ColumnInfo(name = "Folder")
    val folder: String? = null,

    @ColumnInfo(name = "Contacto_1")
    val contacto1: String? = null,

    @ColumnInfo(name = "Puesto_Contacto_1")
    val puestoContacto1: String? = null,

    @ColumnInfo(name = "Contacto_2")
    val contacto2: String? = null,

    @ColumnInfo(name = "Puesto_Contacto_2")
    val puestoContacto2: String? = null,

    @ColumnInfo(name = "Contacto_3")
    val contacto3: String? = null,

    @ColumnInfo(name = "Puesto_Contacto_3")
    val puestoContacto3: String? = null,

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
)

