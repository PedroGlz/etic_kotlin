package com.example.etic.features.inspection.logic

import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.dao.UbicacionDao
import com.example.etic.data.local.entities.Problema

data class VisualProblemDraft(
    val problema: Problema,
    val equipmentName: String?,
    val route: String?
)

object VisualProblemEditor {
    suspend fun loadDraft(
        problemId: String,
        problemaDao: ProblemaDao,
        ubicacionDao: UbicacionDao,
        visualTypeId: String?
    ): VisualProblemDraft? {
        if (visualTypeId == null) return null
        val entity = problemaDao.getById(problemId) ?: return null
        if (!entity.idTipoInspeccion.equals(visualTypeId, true)) return null
        val ubicacion = entity.idUbicacion?.let { ubicacionDao.getById(it) }
        return VisualProblemDraft(
            problema = entity,
            equipmentName = ubicacion?.ubicacion,
            route = ubicacion?.ruta ?: entity.ruta
        )
    }
}
