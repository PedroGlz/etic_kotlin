package com.example.etic.reports

import com.example.etic.data.local.entities.DatosReporte

internal const val MAX_CONTACTOS = 7
private const val PORTADA_IMAGES_COUNT = 3

data class ResultadosAnalisisContacto(
    val nombre: String = "",
    val puesto: String = ""
)

data class ResultadosAnalisisRecomendacion(
    val texto: String = "",
    val imagen1: String = "",
    val imagen2: String = ""
)

data class ResultadosAnalisisDraft(
    val inspectionId: String,
    val siteId: String? = null,
    val detalleUbicacion: String = "",
    val contactos: List<ResultadosAnalisisContacto> = emptyList(),
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val fechaAnterior: String = "",
    val nombreImgPortada: String = "",
    val nombreImgPortada2: String = "",
    val nombreImgPortada3: String = "",
    val descripciones: List<String> = emptyList(),
    val areasInspeccionadas: List<String> = emptyList(),
    val recomendaciones: List<ResultadosAnalisisRecomendacion> = emptyList(),
    val referencias: List<String> = emptyList(),
    val selectedInventoryIds: List<String> = emptyList(),
    val selectedProblemIds: List<String> = emptyList()
)

data class ResultadosAnalisisProblemOption(
    val id: String,
    val label: String
)

internal fun splitSerialized(raw: String?): List<String> =
    raw.orEmpty()
        .split("$")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

private fun splitPortadaSerialized(raw: String?): List<String> =
    if (raw.isNullOrBlank()) {
        emptyList()
    } else {
        raw.split("$").map { it.trim() }
    }

private fun normalizePortadaImagenes(values: List<String>): List<String> =
    (values + List(PORTADA_IMAGES_COUNT) { "" }).take(PORTADA_IMAGES_COUNT)

internal fun serializeStrings(values: List<String>): String =
    values.joinToString("$") { it.trim() }

internal fun padContactos(values: List<String>): List<String> =
    (values + List(MAX_CONTACTOS) { "" }).take(MAX_CONTACTOS)

fun ResultadosAnalisisDraft.toEntity(existingId: Int = 0): DatosReporte {
    val nombres = padContactos(contactos.map { it.nombre })
    val puestos = padContactos(contactos.map { it.puesto })
    val recomendacionesTexto = recomendaciones.map { it.texto }
    val recomendacionesImg1 = recomendaciones.map { it.imagen1 }
    val recomendacionesImg2 = recomendaciones.map { it.imagen2 }
    val portadaImages = normalizePortadaImagenes(
        listOf(nombreImgPortada, nombreImgPortada2, nombreImgPortada3).map { it.trim() }
    )

    return DatosReporte(
        idDatosReporte = existingId,
        idInspeccion = inspectionId,
        detalleUbicacion = detalleUbicacion.ifBlank { null },
        nombreContacto = serializeStrings(nombres),
        puestoContacto = serializeStrings(puestos),
        fechaInicioRa = fechaInicio.ifBlank { null },
        fechaFinRa = fechaFin.ifBlank { null },
        nombreImgPortada = serializeStrings(portadaImages).ifBlank { null },
        descripcionReporte = serializeStrings(descripciones),
        areasInspeccionadas = serializeStrings(areasInspeccionadas),
        recomendacionReporte = serializeStrings(recomendacionesTexto),
        imagenRecomendacion = serializeStrings(recomendacionesImg1),
        imagenRecomendacion2 = serializeStrings(recomendacionesImg2),
        referenciaReporte = serializeStrings(referencias),
        arrayElementosSeleccionados = selectedInventoryIds.joinToString(","),
        arrayProblemasSeleccionados = selectedProblemIds.joinToString(","),
        idSitio = siteId
    )
}

fun DatosReporte.toDraft(defaultInspectionId: String, defaultSiteId: String?): ResultadosAnalisisDraft {
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
            inspectionId = idInspeccion ?: defaultInspectionId,
            siteId = idSitio ?: defaultSiteId,
            detalleUbicacion = detalleUbicacion.orEmpty(),
            contactos = contactos,
            fechaInicio = fechaInicioRa.orEmpty(),
            fechaFin = fechaFinRa.orEmpty(),
            fechaAnterior = "",
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
