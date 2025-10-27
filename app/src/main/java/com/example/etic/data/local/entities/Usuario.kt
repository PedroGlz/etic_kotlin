package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey
    @ColumnInfo(name = "Id_Usuario")
    val idUsuario: String,

    @ColumnInfo(name = "Id_Grupo")
    val idGrupo: String? = null,

    @ColumnInfo(name = "Usuario")
    val usuario: String? = null,

    @ColumnInfo(name = "Nombre")
    val nombre: String? = null,

    @ColumnInfo(name = "Password")
    val password: String? = null,

    @ColumnInfo(name = "Foto")
    val foto: String? = null,

    @ColumnInfo(name = "Email")
    val email: String? = null,

    @ColumnInfo(name = "Telefono")
    val telefono: String? = null,

    @ColumnInfo(name = "nivelCertificacion")
    val nivelCertificacion: String? = null,

    @ColumnInfo(name = "Ultimo_login")
    val ultimoLogin: String? = null,

    @ColumnInfo(name = "Titulo")
    val titulo: String? = null,

    @ColumnInfo(name = "Id_Cliente")
    val idCliente: String? = null,

    @ColumnInfo(name = "Id_Grupo_Sitios")
    val idGrupoSitios: String? = null,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = null,

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
    val idInspeccion: String? = "flag_export"
)
