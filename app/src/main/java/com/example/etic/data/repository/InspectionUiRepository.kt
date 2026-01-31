package com.example.etic.data.repository

import com.example.etic.data.local.dao.EquipoDao
import com.example.etic.data.local.dao.InspeccionDao
import com.example.etic.data.local.dao.InspeccionDetDao
import com.example.etic.data.local.dao.LineaBaseDao
import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.dao.SeveridadDao
import com.example.etic.data.local.dao.TipoInspeccionDao
import com.example.etic.data.local.dao.UbicacionDao
import com.example.etic.features.inspection.tree.Baseline
import com.example.etic.features.inspection.tree.Problem
import com.example.etic.features.inspection.tree.descendantIds
import java.time.LocalDate

class InspectionUiRepository(
    private val problemaDao: ProblemaDao,
    private val ubicacionDao: UbicacionDao,
    private val inspeccionDao: InspeccionDao,
    private val inspeccionDetDao: InspeccionDetDao,
    private val severidadDao: SeveridadDao,
    private val equipoDao: EquipoDao,
    private val tipoInspeccionDao: TipoInspeccionDao,
    private val lineaBaseDao: LineaBaseDao
) {
    suspend fun loadProblemsForUi(
        currentInspectionId: String?,
        currentSiteId: String?,
        selectedUbicacionId: String?,
        typeFilterId: String?,
        statusFilterId: String
    ): List<Problem> = try {
        val rows = try {
            if (!currentSiteId.isNullOrBlank()) {
                problemaDao.getActivosPorSitio(currentSiteId)
            } else {
                problemaDao.getAllActivos()
            }
        } catch (_: Exception) { emptyList() }

        val ubicaciones = try { ubicacionDao.getAll() } catch (_: Exception) { emptyList() }
        val locationFilteredRows = when {
            selectedUbicacionId == null -> rows
            selectedUbicacionId.startsWith("root:") -> rows
            else -> {
                val allowed = descendantIds(ubicaciones, selectedUbicacionId)
                rows.filter { r -> r.idUbicacion != null && allowed.contains(r.idUbicacion!!) }
            }
        }

        val selectedTypeFilter = PROBLEM_TYPE_FILTERS.firstOrNull { it.id == typeFilterId }
            ?: PROBLEM_TYPE_FILTERS.first()
        val typeFilteredRows = if (selectedTypeFilter.matchIds.isNotEmpty()) {
            locationFilteredRows.filter { row ->
                val rowId = row.idTipoInspeccion
                rowId != null && selectedTypeFilter.matchIds.any { it.equals(rowId, ignoreCase = true) }
            }
        } else locationFilteredRows

        val openRows = typeFilteredRows.filter {
            it.estatusProblema?.equals("Abierto", ignoreCase = true) == true
        }
        val closedRows = typeFilteredRows.filter {
            it.estatusProblema?.equals("Cerrado", ignoreCase = true) == true
        }

        val statusFilteredRows = if (statusFilterId != PROBLEM_STATUS_ALL) {
            when (statusFilterId) {
                PROBLEM_STATUS_OPEN_CURRENT ->
                    openRows.filter { row ->
                        val isCurrent = row.idInspeccion?.equals(currentInspectionId, ignoreCase = true) == true
                        !currentInspectionId.isNullOrBlank() && isCurrent
                    }
                PROBLEM_STATUS_OPEN_PAST ->
                    openRows.filter { row ->
                        val isCurrent = row.idInspeccion?.equals(currentInspectionId, ignoreCase = true) == true
                        currentInspectionId.isNullOrBlank() || !isCurrent
                    }
                PROBLEM_STATUS_OPEN_ALL -> openRows
                PROBLEM_STATUS_CLOSED -> closedRows
                else -> typeFilteredRows
            }
        } else typeFilteredRows

        val inspMap = try { inspeccionDao.getAll().associateBy { it.idInspeccion } } catch (_: Exception) { emptyMap() }
        val sevMap = try { severidadDao.getAll().associateBy { it.idSeveridad } } catch (_: Exception) { emptyMap() }
        val tipoMap = try { tipoInspeccionDao.getAll().associateBy { it.idTipoInspeccion } } catch (_: Exception) { emptyMap() }
        val ubicMap = ubicaciones.associateBy { it.idUbicacion }

        statusFilteredRows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                    ?: r.irFileDate?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) LocalDate.parse(onlyDate) else LocalDate.now()
            }.getOrDefault(LocalDate.now())

            val numInspDisplay = r.idInspeccion?.let { inspMap[it]?.noInspeccion?.toString() } ?: ""
            val severidadDisplay = r.idSeveridad?.let { sevMap[it]?.severidad } ?: (r.idSeveridad ?: "")
            val equipoDisplay = r.idUbicacion?.let { ubicMap[it]?.ubicacion } ?: ""
            val tipoDisplay = r.idTipoInspeccion?.let { tipoMap[it]?.tipoInspeccion } ?: (r.idTipoInspeccion ?: "")

            Problem(
                id = r.idProblema,
                no = r.numeroProblema ?: 0,
                fecha = fecha,
                numInspeccion = numInspDisplay,
                tipo = tipoDisplay,
                tipoId = r.idTipoInspeccion,
                inspectionId = r.idInspeccion,
                estatus = r.estatusProblema ?: "",
                cronico = (r.esCronico ?: "").equals("SI", ignoreCase = true),
                tempC = r.problemTemperature ?: 0.0,
                deltaTC = r.aumentoTemperatura ?: 0.0,
                severidad = severidadDisplay,
                equipo = equipoDisplay,
                comentarios = r.componentComment ?: ""
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun softDeleteProblemAndRecalculate(
        problemId: String,
        currentUserId: String?,
        nowTs: String,
        statusPorVerificarId: String,
        statusVerificadoId: String
    ): Boolean = try {
        val entity = try { problemaDao.getById(problemId) } catch (_: Exception) { null }
        if (entity == null) return false

        val updated = entity.copy(
            estatus = "Inactivo",
            modificadoPor = currentUserId,
            fechaMod = nowTs
        )
        runCatching { problemaDao.update(updated) }

        val inspectionId = entity.idInspeccion
        val tipoId = entity.idTipoInspeccion
        if (!inspectionId.isNullOrBlank() && !tipoId.isNullOrBlank()) {
            val activeRows = try {
                problemaDao.getActivosByInspeccionAndTipo(inspectionId, tipoId)
            } catch (_: Exception) { emptyList() }
            var num = 1
            activeRows.forEach { row ->
                if (row.numeroProblema != num) {
                    val renumbered = row.copy(
                        numeroProblema = num,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { problemaDao.update(renumbered) }
                }
                num += 1
            }
        }

        val ubicacionId = entity.idUbicacion
        val inspeccionDetId = entity.idInspeccionDet
        if (!ubicacionId.isNullOrBlank() && !inspectionId.isNullOrBlank()) {
            val activeCount = try {
                if (!inspeccionDetId.isNullOrBlank()) {
                    problemaDao.countActivosByInspeccionDet(inspeccionDetId)
                } else {
                    problemaDao.countActivosByInspeccionAndUbicacion(inspectionId, ubicacionId)
                }
            } catch (_: Exception) { null }
            if (activeCount != null && activeCount < 1) {
                val detRow = try {
                    inspeccionDetDao.getByUbicacion(ubicacionId)
                        .firstOrNull { it.idInspeccion == inspectionId }
                } catch (_: Exception) { null }
                if (detRow != null) {
                    val updatedDet = detRow.copy(
                        idStatusInspeccionDet = statusPorVerificarId,
                        idEstatusColorText = 1,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { inspeccionDetDao.update(updatedDet) }
                }
            }
            updateParentInspectionStatuses(
                inspectionId = inspectionId,
                startUbicacionId = ubicacionId,
                statusPorVerificarId = statusPorVerificarId,
                statusVerificadoId = statusVerificadoId,
                currentUserId = currentUserId,
                nowTs = nowTs
            )
        }

        true
    } catch (_: Exception) {
        false
    }

    suspend fun loadBaselinesForUi(
        currentInspectionId: String?,
        selectedUbicacionId: String?
    ): List<Baseline> = try {
        val rows = try {
            if (!currentInspectionId.isNullOrBlank()) {
                lineaBaseDao.getByInspeccionActivos(currentInspectionId)
            } else {
                lineaBaseDao.getAllActivos()
            }
        } catch (_: Exception) { emptyList() }
        val ubicaciones = try { ubicacionDao.getAllActivas() } catch (_: Exception) { emptyList() }
        val filteredRows = when {
            selectedUbicacionId == null -> rows
            selectedUbicacionId.startsWith("root:") -> rows
            else -> {
                val allowed = descendantIds(ubicaciones, selectedUbicacionId)
                rows.filter { r -> r.idUbicacion != null && allowed.contains(r.idUbicacion!!) }
            }
        }
        val inspMap = try { inspeccionDao.getAll().associateBy { it.idInspeccion } } catch (_: Exception) { emptyMap() }
        val ubicMap = ubicaciones.associateBy { it.idUbicacion }

        filteredRows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) LocalDate.parse(onlyDate) else LocalDate.now()
            }.getOrDefault(LocalDate.now())

            val numInspDisplay = r.idInspeccion?.let { inspMap[it]?.noInspeccion?.toString() } ?: ""
            val ubicDisplay = r.idUbicacion?.let { ubicMap[it]?.ubicacion } ?: ""

            Baseline(
                id = r.idLineaBase,
                numInspeccion = numInspDisplay,
                equipo = ubicDisplay,
                fecha = fecha,
                mtaC = r.mta ?: 0.0,
                tempC = r.tempMax ?: 0.0,
                ambC = r.tempAmb ?: 0.0,
                imgR = r.archivoIr,
                imgD = r.archivoId,
                notas = r.notas ?: ""
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    suspend fun deleteBaselineAndRevertStatus(
        baselineId: String,
        currentUserId: String?,
        nowTs: String,
        statusPorVerificarId: String
    ): Boolean = try {
        val row = try { lineaBaseDao.getById(baselineId) } catch (_: Exception) { null }
        if (row != null) {
            val ubicacionId = row.idUbicacion
            val inspeccionId = row.idInspeccion
            if (!ubicacionId.isNullOrBlank() && !inspeccionId.isNullOrBlank()) {
                val detRow = try {
                    inspeccionDetDao.getByUbicacion(ubicacionId)
                        .firstOrNull { it.idInspeccion == inspeccionId }
                } catch (_: Exception) { null }
                if (detRow != null) {
                    val revertedDet = detRow.copy(
                        idStatusInspeccionDet = statusPorVerificarId,
                        idEstatusColorText = 1,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { inspeccionDetDao.update(revertedDet) }
                }
            }
        } else {
            return false
        }

        runCatching { lineaBaseDao.deleteById(baselineId) }
        true
    } catch (_: Exception) {
        false
    }

    private suspend fun updateParentInspectionStatuses(
        inspectionId: String,
        startUbicacionId: String,
        statusPorVerificarId: String,
        statusVerificadoId: String,
        currentUserId: String?,
        nowTs: String
    ) {
        val ubicaciones = runCatching { ubicacionDao.getAllActivas() }.getOrElse { emptyList() }
        if (ubicaciones.isEmpty()) return

        val detRows = runCatching { inspeccionDetDao.getByInspeccion(inspectionId) }.getOrElse { emptyList() }
        if (detRows.isEmpty()) return

        val ubicById = ubicaciones.associateBy { it.idUbicacion }
        val childrenByParent = ubicaciones.groupBy { it.idUbicacionPadre }
        val detByUbicacion = detRows.mapNotNull { row ->
            row.idUbicacion?.let { it to row }
        }.toMap().toMutableMap()

        var currentId: String? = startUbicacionId
        while (true) {
            val parentId = currentId?.let { ubicById[it]?.idUbicacionPadre }
            if (parentId.isNullOrBlank() || parentId == "0") break

            val childIds = childrenByParent[parentId].orEmpty().map { it.idUbicacion }
            if (childIds.isNotEmpty()) {
                val hasPendingChild = childIds.any { childId ->
                    detByUbicacion[childId]?.idStatusInspeccionDet == statusPorVerificarId
                }
                val statusId = if (hasPendingChild) statusPorVerificarId else statusVerificadoId
                val colorId = if (hasPendingChild) 1 else 4
                val parentDet = detByUbicacion[parentId]
                if (parentDet != null &&
                    (parentDet.idStatusInspeccionDet != statusId || parentDet.idEstatusColorText != colorId)
                ) {
                    val updated = parentDet.copy(
                        idStatusInspeccionDet = statusId,
                        idEstatusColorText = colorId,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { inspeccionDetDao.update(updated) }
                    detByUbicacion[parentId] = updated
                }
            }
            currentId = parentId
        }
    }
}

private const val PROBLEM_STATUS_ALL = "0"
private const val PROBLEM_STATUS_OPEN_CURRENT = "1"
private const val PROBLEM_STATUS_OPEN_PAST = "2"
private const val PROBLEM_STATUS_OPEN_ALL = "3"
private const val PROBLEM_STATUS_CLOSED = "4"

private data class ProblemTypeFilter(val id: String, val label: String, val matchIds: List<String>)

private val PROBLEM_TYPE_FILTERS = listOf(
    ProblemTypeFilter("", "Todos", emptyList()),
    ProblemTypeFilter(
        "electrico",
        "Electrico",
        listOf(
            "0D32B331-76C3-11D3-82BF-00104BC75DC2",
            "0D32B332-76C3-11D3-82BF-00104BC75DC2"
        )
    ),
    ProblemTypeFilter(
        "visual",
        "Visual",
        listOf("0D32B333-76C3-11D3-82BF-00104BC75DC2")
    ),
    ProblemTypeFilter(
        "mecanico",
        "Mecanico",
        listOf("0D32B334-76C3-11D3-82BF-00104BC75DC2")
    )
)
