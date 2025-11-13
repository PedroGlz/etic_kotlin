package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey
    @ColumnInfo(name = "Id_Cliente")
    val idCliente: String,

    @ColumnInfo(name = "Id_Compania")
    val idCompania: String? = null,

    @ColumnInfo(name = "Id_Giro")
    val idGiro: String? = null,

    @ColumnInfo(name = "Razon_Social")
    val razonSocial: String? = null,

    @ColumnInfo(name = "Nombre_Comercial")
    val nombreComercial: String? = null,

    @ColumnInfo(name = "RFC")
    val rfc: String? = null,

    @ColumnInfo(name = "Imagen_Cliente")
    val imagenCliente: String? = null,

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

