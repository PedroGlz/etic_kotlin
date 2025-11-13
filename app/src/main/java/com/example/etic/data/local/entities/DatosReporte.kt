package com.example.etic.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datos_reporte")
data class DatosReporte(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "Id_Datos_Reporte")
    val idDatosReporte: Int = 0,

    @ColumnInfo(name = "Id_Inspeccion")
    val idInspeccion: String? = null,

    @ColumnInfo(name = "detalle_ubicacion")
    val detalleUbicacion: String? = null,

    @ColumnInfo(name = "nombre_contacto")
    val nombreContacto: String? = null,

    @ColumnInfo(name = "puesto_contacto")
    val puestoContacto: String? = null,

    @ColumnInfo(name = "fecha_inicio_ra")
    val fechaInicioRa: String? = null,

    @ColumnInfo(name = "fecha_fin_ra")
    val fechaFinRa: String? = null,

    @ColumnInfo(name = "nombre_img_portada")
    val nombreImgPortada: String? = null,

    @ColumnInfo(name = "descripcion_reporte")
    val descripcionReporte: String? = null,

    @ColumnInfo(name = "areas_inspeccionadas")
    val areasInspeccionadas: String? = null,

    @ColumnInfo(name = "recomendacion_reporte")
    val recomendacionReporte: String? = null,

    @ColumnInfo(name = "imagen_recomendacion")
    val imagenRecomendacion: String? = null,

    @ColumnInfo(name = "imagen_recomendacion_2")
    val imagenRecomendacion2: String? = null,

    @ColumnInfo(name = "referencia_reporte")
    val referenciaReporte: String? = null,

    @ColumnInfo(name = "arrayElementosSeleccionados")
    val arrayElementosSeleccionados: String? = null,

    @ColumnInfo(name = "arrayProblemasSeleccionados")
    val arrayProblemasSeleccionados: String? = null,

    @ColumnInfo(name = "Id_Sitio")
    val idSitio: String? = "flag_export",
)

