package com.example.etic.reports

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadResultadosAnalisisDraftUseCase(private val context: Context) {
    suspend fun run(
        inspectionId: String,
        siteId: String?,
        defaultDraft: ResultadosAnalisisDraft
    ): Result<ResultadosAnalisisDraft> = withContext(Dispatchers.IO) {
        DatosReporteStore
            .loadLatestByInspection(context, inspectionId)
            .mapCatching { loadedDraft ->
                loadedDraft
                    ?.copy(
                        siteId = loadedDraft.siteId ?: siteId,
                        contactos = if (loadedDraft.contactos.any { contacto ->
                                contacto.nombre.isNotBlank() || contacto.puesto.isNotBlank()
                            }
                        ) {
                            loadedDraft.contactos
                        } else {
                            defaultDraft.contactos
                        }
                    )
                    ?: defaultDraft
            }
    }
}

class SaveResultadosAnalisisDraftUseCase(private val context: Context) {
    suspend fun run(draft: ResultadosAnalisisDraft): Result<Unit> = withContext(Dispatchers.IO) {
        DatosReporteStore.save(context, draft)
    }
}
