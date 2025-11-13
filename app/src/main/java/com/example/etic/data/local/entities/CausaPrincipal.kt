package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "causa_principal")
data class CausaPrincipal(
    @PrimaryKey
    @ColumnInfo(name = "Id_Causa_Raiz")
    val idCausaRaiz: String,

    @ColumnInfo(name = "Id_Tipo_Inspeccion")
    val idTipoInspeccion: String? = null,

    @ColumnInfo(name = "Id_Falla")
    val idFalla: String? = null,

    @ColumnInfo(name = "Causa_Raiz")
    val causaRaiz: String? = null,

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

