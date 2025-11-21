package com.example.etic.features.inspection.data

import com.example.etic.data.local.dao.InspeccionDetDao
import com.example.etic.data.local.dao.UbicacionDao
import com.example.etic.data.local.dao.VistaUbicacionArbolDao
import com.example.etic.data.local.entities.InspeccionDet
import com.example.etic.data.local.entities.Ubicacion
import com.example.etic.features.inspection.tree.TreeNode
import com.example.etic.features.inspection.tree.buildTreeFromVista
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class UbicacionSaveContext(
    val isEdit: Boolean,
    val editingDetId: String?,
    val editingInspId: String?,
    val newStatusId: String?,
    val currentInspectionId: String?,
    val currentSiteId: String?
)

class InspectionRepository(
    private val ubicacionDao: UbicacionDao,
    private val inspeccionDetDao: InspeccionDetDao,
    private val vistaUbicacionArbolDao: VistaUbicacionArbolDao
) {
    suspend fun loadTree(rootId: String, rootTitle: String): List<TreeNode> =
        withContext(Dispatchers.IO) {
            val rowsVista = runCatching { vistaUbicacionArbolDao.getAll() }.getOrElse { emptyList() }
            val roots = buildTreeFromVista(rowsVista)
            val siteRoot = TreeNode(id = rootId, title = rootTitle)
            siteRoot.children.addAll(roots)
            listOf(siteRoot)
        }

    suspend fun markUbicacionInactive(ubId: String, userId: String?, timestamp: String) {
        withContext(Dispatchers.IO) {
            val existing = runCatching { ubicacionDao.getById(ubId) }.getOrNull()
            if (existing != null) {
                val updated = existing.copy(
                    estatus = "Inactivo",
                    modificadoPor = userId,
                    fechaMod = timestamp
                )
                runCatching { ubicacionDao.update(updated) }
            }
        }
    }

    suspend fun saveUbicacion(
        entity: Ubicacion,
        context: UbicacionSaveContext,
        nowTs: String,
        currentUserId: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val okUb = runCatching {
            if (context.isEdit) ubicacionDao.update(entity) else ubicacionDao.insert(entity)
        }.isSuccess
        if (!okUb) return@withContext false

        if (context.isEdit && context.editingDetId != null) {
            val existingDet = runCatching { inspeccionDetDao.getByUbicacion(entity.idUbicacion) }
                .getOrElse { emptyList() }
                .firstOrNull { it.idInspeccionDet == context.editingDetId }
            val det = InspeccionDet(
                idInspeccionDet = context.editingDetId,
                idInspeccion = context.editingInspId,
                idUbicacion = entity.idUbicacion,
                idStatusInspeccionDet = context.newStatusId,
                notasInspeccion = existingDet?.notasInspeccion,
                estatus = "Activo",
                idEstatusColorText = existingDet?.idEstatusColorText ?: 1,
                expanded = existingDet?.expanded ?: "0",
                selected = existingDet?.selected ?: "0",
                creadoPor = existingDet?.creadoPor ?: currentUserId,
                fechaCreacion = existingDet?.fechaCreacion ?: nowTs,
                modificadoPor = currentUserId,
                fechaMod = nowTs,
                idSitio = context.currentSiteId
            )
            runCatching { inspeccionDetDao.update(det) }
        } else if (!context.isEdit) {
            val det = InspeccionDet(
                idInspeccionDet = UUID.randomUUID().toString().uppercase(),
                idInspeccion = context.currentInspectionId,
                idUbicacion = entity.idUbicacion,
                idStatusInspeccionDet = context.newStatusId,
                notasInspeccion = null,
                estatus = "Activo",
                idEstatusColorText = 1,
                expanded = "0",
                selected = "0",
                creadoPor = currentUserId,
                fechaCreacion = nowTs,
                modificadoPor = null,
                fechaMod = null,
                idSitio = context.currentSiteId
            )
            runCatching { inspeccionDetDao.insert(det) }
        }
        true
    }
}
