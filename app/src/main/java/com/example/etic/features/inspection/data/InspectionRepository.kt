package com.example.etic.features.inspection.data

import androidx.room.withTransaction
import com.example.etic.data.local.AppDatabase
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

data class CloneUbicacionTreeRequest(
    val sourceNode: TreeNode,
    val targetParentId: String?,
    val targetParentRoute: String,
    val targetParentLevel: Int,
    val rootTitle: String,
    val defaultStatusId: String?,
    val defaultPriorityId: String?,
    val currentInspectionId: String?,
    val currentSiteId: String?,
    val currentUserId: String?,
    val nowTs: String
)

data class CloneUbicacionTreeResult(
    val rootCloneId: String,
    val createdIds: List<String>
)

class InspectionRepository(
    private val db: AppDatabase,
    private val ubicacionDao: UbicacionDao,
    private val inspeccionDetDao: InspeccionDetDao,
    private val vistaUbicacionArbolDao: VistaUbicacionArbolDao
) {
    suspend fun loadTree(
        rootId: String,
        rootTitle: String,
        inspectionId: String? = null
    ): List<TreeNode> =
        withContext(Dispatchers.IO) {
            val rowsVista = runCatching {
                if (inspectionId.isNullOrBlank()) vistaUbicacionArbolDao.getAll()
                else vistaUbicacionArbolDao.getByInspeccion(inspectionId)
            }.getOrElse { emptyList() }
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
                runCatching { inspeccionDetDao.markInactiveByUbicacion(ubId, userId, timestamp) }
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

    suspend fun cloneNodeWithDirectChildren(
        request: CloneUbicacionTreeRequest
    ): CloneUbicacionTreeResult? = withContext(Dispatchers.IO) {
        if (request.sourceNode.id.startsWith("root:")) return@withContext null

        val createdIds = mutableListOf<String>()

        runCatching {
            db.withTransaction {
                val rootCloneId = UUID.randomUUID().toString().uppercase()
                val rootLevel = request.targetParentLevel + 1
                val rootRoute = buildRoute(
                    parentRoute = request.targetParentRoute,
                    rootTitle = request.rootTitle,
                    nodeTitle = request.sourceNode.title
                )

                ubicacionDao.insert(
                    buildClonedUbicacion(
                        id = rootCloneId,
                        parentId = request.targetParentId,
                        siteId = request.currentSiteId,
                        level = rootLevel,
                        route = rootRoute,
                        node = request.sourceNode,
                        defaultPriorityId = request.defaultPriorityId,
                        currentInspectionId = request.currentInspectionId,
                        nowTs = request.nowTs,
                        currentUserId = request.currentUserId
                    )
                )
                insertInspectionDetForClone(
                    ubicacionId = rootCloneId,
                    request = request
                )
                createdIds += rootCloneId

                request.sourceNode.children.forEach { child ->
                    val childCloneId = UUID.randomUUID().toString().uppercase()
                    val childRoute = buildRoute(
                        parentRoute = rootRoute,
                        rootTitle = request.rootTitle,
                        nodeTitle = child.title
                    )
                    ubicacionDao.insert(
                        buildClonedUbicacion(
                            id = childCloneId,
                            parentId = rootCloneId,
                            siteId = request.currentSiteId,
                            level = rootLevel + 1,
                            route = childRoute,
                            node = child,
                            defaultPriorityId = request.defaultPriorityId,
                            currentInspectionId = request.currentInspectionId,
                            nowTs = request.nowTs,
                            currentUserId = request.currentUserId
                        )
                    )
                    insertInspectionDetForClone(
                        ubicacionId = childCloneId,
                        request = request
                    )
                    createdIds += childCloneId
                }

                CloneUbicacionTreeResult(
                    rootCloneId = rootCloneId,
                    createdIds = createdIds.toList()
                )
            }
        }.getOrNull()
    }

    private suspend fun insertInspectionDetForClone(
        ubicacionId: String,
        request: CloneUbicacionTreeRequest
    ) {
        inspeccionDetDao.insert(
            InspeccionDet(
                idInspeccionDet = UUID.randomUUID().toString().uppercase(),
                idInspeccion = request.currentInspectionId,
                idUbicacion = ubicacionId,
                idStatusInspeccionDet = request.defaultStatusId,
                notasInspeccion = null,
                estatus = "Activo",
                idEstatusColorText = 1,
                expanded = "0",
                selected = "0",
                creadoPor = request.currentUserId,
                fechaCreacion = request.nowTs,
                modificadoPor = null,
                fechaMod = null,
                idSitio = request.currentSiteId
            )
        )
    }

    private fun buildClonedUbicacion(
        id: String,
        parentId: String?,
        siteId: String?,
        level: Int,
        route: String,
        node: TreeNode,
        defaultPriorityId: String?,
        currentInspectionId: String?,
        nowTs: String,
        currentUserId: String?
    ): Ubicacion {
        return Ubicacion(
            idUbicacion = id,
            idSitio = siteId,
            idUbicacionPadre = parentId,
            idTipoPrioridad = defaultPriorityId,
            idTipoInspeccion = null,
            ubicacion = node.title,
            descripcion = null,
            esEquipo = if (node.verified) "SI" else "NO",
            codigoBarras = null,
            nivelArbol = level,
            limite = null,
            fabricante = null,
            nombreFoto = null,
            ruta = route,
            estatus = "Activo",
            creadoPor = currentUserId,
            fechaCreacion = nowTs,
            modificadoPor = null,
            fechaMod = null,
            idInspeccion = currentInspectionId
        )
    }

    private fun buildRoute(
        parentRoute: String,
        rootTitle: String,
        nodeTitle: String
    ): String {
        val cleanParent = parentRoute.trim().ifBlank { rootTitle }
        val cleanNode = nodeTitle.trim()
        return if (cleanNode.isBlank()) cleanParent else "$cleanParent / $cleanNode"
    }
}
