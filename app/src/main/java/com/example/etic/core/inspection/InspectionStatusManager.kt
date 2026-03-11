package com.example.etic.core.inspection

import android.content.Context
import com.example.etic.core.current.CurrentInspectionProvider
import com.example.etic.core.export.exportRoomDbToDownloads
import com.example.etic.core.settings.EticPrefs
import com.example.etic.core.settings.settingsDataStore
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

const val INSPECTION_STATUS_EN_PROGRESO = "73F27003-76B3-11D3-82BF-00104BC75DC2"
const val INSPECTION_STATUS_CERRADA = "73F27007-76B3-11D3-82BF-00104BC75DC2"

data class InspectionStatusChangeResult(
    val success: Boolean,
    val message: String,
    val inspectionCleared: Boolean = false
)

private val statusDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
private val closeDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
private val acceptedDateFormats = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
)

private fun parseInspectionDate(value: String?): LocalDateTime? {
    val raw = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return acceptedDateFormats.firstNotNullOfOrNull { formatter ->
        runCatching { LocalDateTime.parse(raw, formatter) }.getOrNull()
    }
}

private suspend fun clearInspectionData(context: Context) {
    withContext(Dispatchers.IO) {
        val db = DbProvider.get(context)
        val sqliteDb = db.openHelper.writableDatabase
        val tables = listOf(
            "causa_principal",
            "clientes",
            "datos_reporte",
            "equipos",
            "estatus_color_text",
            "estatus_inspeccion",
            "estatus_inspeccion_det",
            "fabricantes",
            "fallas",
            "fases",
            "grupos",
            "grupos_sitios",
            "historial_problemas",
            "inspecciones",
            "inspecciones_det",
            "linea_base",
            "problemas",
            "severidades",
            "sitios",
            "tipo_ambientes",
            "tipo_fallas",
            "tipo_inspecciones",
            "tipo_prioridades",
            "ubicaciones"
        )
        sqliteDb.execSQL("PRAGMA foreign_keys=OFF")
        try {
            db.runInTransaction {
                tables.forEach { table ->
                    sqliteDb.execSQL("DELETE FROM $table")
                }
            }
        } finally {
            sqliteDb.execSQL("PRAGMA foreign_keys=ON")
        }
        DbProvider.closeAndReset()
        EticPrefs(context.settingsDataStore).setActiveInspectionNum(null)
        CurrentInspectionProvider.invalidate()
    }
}

suspend fun changeInspectionStatus(
    context: Context,
    inspectionId: String,
    statusId: String,
    currentUserId: String?,
    exportFileName: String? = null
): InspectionStatusChangeResult {
    val updateResult = withContext(Dispatchers.IO) {
        val dao = DbProvider.get(context).inspeccionDao()
        val inspection = dao.getById(inspectionId)
            ?: return@withContext InspectionStatusChangeResult(
                success = false,
                message = "No se encontro la inspeccion actual."
            )

        val now = LocalDateTime.now()
        val updated = if (statusId == INSPECTION_STATUS_CERRADA) {
            val noDias = parseInspectionDate(inspection.fechaInicio)
                ?.let { abs(Duration.between(it, now).toDays().toInt()) }
            inspection.copy(
                idStatusInspeccion = statusId,
                fechaFin = now.format(closeDateTimeFormatter),
                noDias = noDias,
                modificadoPor = currentUserId,
                fechaMod = now.format(statusDateTimeFormatter)
            )
        } else {
            inspection.copy(
                idStatusInspeccion = statusId,
                modificadoPor = currentUserId,
                fechaMod = now.format(statusDateTimeFormatter)
            )
        }
        dao.update(updated)
        CurrentInspectionProvider.invalidate()
        InspectionStatusChangeResult(
            success = true,
            message = if (statusId == INSPECTION_STATUS_CERRADA) {
                "Estatus actualizado. Generando respaldo."
            } else {
                "Estatus actualizado."
            }
        )
    }

    if (!updateResult.success) return updateResult
    if (statusId != INSPECTION_STATUS_CERRADA) return updateResult

    val exportResult = exportRoomDbToDownloads(
        context = context,
        exportFileName = exportFileName
    )
    if (!exportResult.success) {
        return InspectionStatusChangeResult(
            success = false,
            message = exportResult.message
        )
    }

    clearInspectionData(context)
    return InspectionStatusChangeResult(
        success = true,
        message = "${exportResult.message}. Inspeccion cerrada.",
        inspectionCleared = true
    )
}
