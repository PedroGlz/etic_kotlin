package com.example.etic.reports

import android.content.ContentValues
import android.content.Context
import com.example.etic.data.local.DbProvider

private data class StoredDatosReporte(
    val id: Int,
    val inspectionId: String?,
    val siteId: String?,
    val detalleUbicacion: String?,
    val nombreContacto: String?,
    val puestoContacto: String?,
    val fechaInicio: String?,
    val fechaFin: String?,
    val nombreImgPortada: String?,
    val descripcionReporte: String?,
    val areasInspeccionadas: String?,
    val recomendacionReporte: String?,
    val imagenRecomendacion: String?,
    val imagenRecomendacion2: String?,
    val referenciaReporte: String?,
    val arrayElementosSeleccionados: String?,
    val arrayProblemasSeleccionados: String?
)

private const val PORTADA_IMAGES_COUNT = 3

private fun splitPortadaSerialized(raw: String?): List<String> =
    if (raw.isNullOrBlank()) {
        emptyList()
    } else {
        raw.split("$").map { it.trim() }
    }

private fun normalizePortadaImagenes(values: List<String>): List<String> =
    (values + List(PORTADA_IMAGES_COUNT) { "" }).take(PORTADA_IMAGES_COUNT)

internal object DatosReporteStore {
    fun loadLatestByInspection(context: Context, inspectionId: String): Result<ResultadosAnalisisDraft?> =
        runCatching {
            val db = DbProvider.get(context).openHelper.writableDatabase
            db.query(
                """
                SELECT
                    Id_Datos_Reporte,
                    Id_Inspeccion,
                    Id_Sitio,
                    detalle_ubicacion,
                    nombre_contacto,
                    puesto_contacto,
                    fecha_inicio_ra,
                    fecha_fin_ra,
                    nombre_img_portada,
                    descripcion_reporte,
                    areas_inspeccionadas,
                    recomendacion_reporte,
                    imagen_recomendacion,
                    imagen_recomendacion_2,
                    referencia_reporte,
                    arrayElementosSeleccionados,
                    arrayProblemasSeleccionados
                FROM datos_reporte
                WHERE Id_Inspeccion = ?
                ORDER BY Id_Datos_Reporte DESC
                LIMIT 1
                """.trimIndent(),
                arrayOf(inspectionId)
            ).use { cursor ->
                if (!cursor.moveToFirst()) return@runCatching null
                val row = StoredDatosReporte(
                    id = cursor.getInt(0),
                    inspectionId = cursor.getString(1),
                    siteId = cursor.getString(2),
                    detalleUbicacion = cursor.getString(3),
                    nombreContacto = cursor.getString(4),
                    puestoContacto = cursor.getString(5),
                    fechaInicio = cursor.getString(6),
                    fechaFin = cursor.getString(7),
                    nombreImgPortada = cursor.getString(8),
                    descripcionReporte = cursor.getString(9),
                    areasInspeccionadas = cursor.getString(10),
                    recomendacionReporte = cursor.getString(11),
                    imagenRecomendacion = cursor.getString(12),
                    imagenRecomendacion2 = cursor.getString(13),
                    referenciaReporte = cursor.getString(14),
                    arrayElementosSeleccionados = cursor.getString(15),
                    arrayProblemasSeleccionados = cursor.getString(16)
                )
        row.toDraft(inspectionId)
            }
        }

    fun save(context: Context, draft: ResultadosAnalisisDraft): Result<Unit> = runCatching {
        val db = DbProvider.get(context).openHelper.writableDatabase
        val existingId = db.query(
            """
            SELECT Id_Datos_Reporte
            FROM datos_reporte
            WHERE Id_Inspeccion = ?
            ORDER BY Id_Datos_Reporte DESC
            LIMIT 1
            """.trimIndent(),
            arrayOf(draft.inspectionId)
        ).use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0) else null
        }

        val values = ContentValues().apply {
            put("Id_Inspeccion", draft.inspectionId)
            put("Id_Sitio", draft.siteId)
            put("detalle_ubicacion", draft.detalleUbicacion.ifBlank { null })
            put("nombre_contacto", serializeStrings(padContactos(draft.contactos.map { it.nombre })))
            put("puesto_contacto", serializeStrings(padContactos(draft.contactos.map { it.puesto })))
            put("fecha_inicio_ra", draft.fechaInicio.ifBlank { null })
            put("fecha_fin_ra", draft.fechaFin.ifBlank { null })
            put(
                "nombre_img_portada",
                serializeStrings(
                    normalizePortadaImagenes(
                        listOf(draft.nombreImgPortada, draft.nombreImgPortada2, draft.nombreImgPortada3).map { it.trim() }
                    )
                ).ifBlank { null }
            )
            put("descripcion_reporte", serializeStrings(draft.descripciones))
            put("areas_inspeccionadas", serializeStrings(draft.areasInspeccionadas))
            put("recomendacion_reporte", serializeStrings(draft.recomendaciones.map { it.texto }))
            put("imagen_recomendacion", serializeStrings(draft.recomendaciones.map { it.imagen1 }))
            put("imagen_recomendacion_2", serializeStrings(draft.recomendaciones.map { it.imagen2 }))
            put("referencia_reporte", serializeStrings(draft.referencias))
            put("arrayElementosSeleccionados", draft.selectedInventoryIds.joinToString(","))
            put("arrayProblemasSeleccionados", draft.selectedProblemIds.joinToString(","))
        }

        if (existingId == null) {
            db.insert("datos_reporte", 0, values)
        } else {
            db.update("datos_reporte", 0, values, "Id_Datos_Reporte = ?", arrayOf(existingId))
        }
    }

    private fun StoredDatosReporte.toDraft(defaultInspectionId: String): ResultadosAnalisisDraft {
        val nombres = padContactos(splitSerialized(nombreContacto))
        val puestos = padContactos(splitSerialized(puestoContacto))
        val contactos = (0 until MAX_CONTACTOS).map { index ->
            ResultadosAnalisisContacto(
                nombre = nombres.getOrElse(index) { "" },
                puesto = puestos.getOrElse(index) { "" }
            )
        }

        val recomendacionesTexto = splitSerialized(recomendacionReporte)
        val recomendacionesImg1 = splitSerialized(imagenRecomendacion)
        val recomendacionesImg2 = splitSerialized(imagenRecomendacion2)
        val portadaImages = normalizePortadaImagenes(splitPortadaSerialized(nombreImgPortada))
        val maxRecCount = maxOf(
            recomendacionesTexto.size,
            recomendacionesImg1.size,
            recomendacionesImg2.size
        )
        val recomendaciones = if (maxRecCount == 0) {
            emptyList()
        } else {
            (0 until maxRecCount).map { index ->
                ResultadosAnalisisRecomendacion(
                    texto = recomendacionesTexto.getOrElse(index) { "" },
                    imagen1 = recomendacionesImg1.getOrElse(index) { "" },
                    imagen2 = recomendacionesImg2.getOrElse(index) { "" }
                )
            }
        }

        return ResultadosAnalisisDraft(
            inspectionId = inspectionId ?: defaultInspectionId,
            siteId = siteId,
            detalleUbicacion = detalleUbicacion.orEmpty(),
            contactos = contactos,
            fechaInicio = fechaInicio.orEmpty(),
            fechaFin = fechaFin.orEmpty(),
            nombreImgPortada = portadaImages.getOrElse(0) { "" },
            nombreImgPortada2 = portadaImages.getOrElse(1) { "" },
            nombreImgPortada3 = portadaImages.getOrElse(2) { "" },
            descripciones = splitSerialized(descripcionReporte),
            areasInspeccionadas = splitSerialized(areasInspeccionadas),
            recomendaciones = recomendaciones,
            referencias = splitSerialized(referenciaReporte),
            selectedInventoryIds = arrayElementosSeleccionados
                .orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() },
            selectedProblemIds = arrayProblemasSeleccionados
                .orEmpty()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        )
    }
}
