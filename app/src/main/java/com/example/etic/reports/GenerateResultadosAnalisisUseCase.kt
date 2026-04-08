package com.example.etic.reports

import android.content.Context
import android.net.Uri

class GenerateResultadosAnalisisUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val getInspeccionImagenesTreeUri: (inspectionNumber: String) -> Uri?,
    private val getClientesImagenesTreeUri: (inspectionNumber: String) -> Uri?
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        draft: ResultadosAnalisisDraft,
        selectedInventoryIds: List<String>,
        currentUserId: String? = null,
        currentUserName: String? = null
    ): Result<String> {
        fun isMissingProblemList(error: Throwable): Boolean {
            val message = error.message.orEmpty()
            return message.contains("No hay problemas abiertos para generar la lista.") ||
                message.contains("No hay problemas cerrados en la inspección actual para generar la lista.")
        }

        val pdfPrincipal = GenerateResultadosAnalisisPdfUseCase(
            context = context,
            folderProvider = folderProvider,
            getInspeccionImagenesTreeUri = getInspeccionImagenesTreeUri,
            getClientesImagenesTreeUri = getClientesImagenesTreeUri
        ).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            draft = draft,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

        val grafica = GenerateAnomaliasChartPdfUseCase(
            context,
            folderProvider,
            getInspeccionImagenesTreeUri,
            getClientesImagenesTreeUri
        ).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

        val inventario = GenerateInventarioPdfUseCase(context, folderProvider).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            selectedUbicacionIds = selectedInventoryIds,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

        val problemas = GenerateProblemasPdfUseCase(
            context = context,
            folderProvider = folderProvider,
            getInspeccionImagenesTreeUri = getInspeccionImagenesTreeUri
        ).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            selectedProblemaIds = draft.selectedProblemIds,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

        val baseline = GenerateBaselinePdfUseCase(
            context = context,
            folderProvider = folderProvider,
            getInspeccionImagenesTreeUri = getInspeccionImagenesTreeUri
        ).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

        val abiertos = GenerateProblemListPdfUseCase(context, folderProvider).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            listType = GenerateProblemListPdfUseCase.ProblemListType.ABIERTOS,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).fold(
            onSuccess = { it },
            onFailure = {
                if (isMissingProblemList(it)) null else return Result.failure(it)
            }
        )

        val cerrados = GenerateProblemListPdfUseCase(context, folderProvider).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            listType = GenerateProblemListPdfUseCase.ProblemListType.CERRADOS,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).fold(
            onSuccess = { it },
            onFailure = {
                if (isMissingProblemList(it)) null else return Result.failure(it)
            }
        )

        GenerateProblemListExcelUseCase(context, folderProvider).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            reportStartDate = draft.fechaInicio,
            reportEndDate = draft.fechaFin
        ).getOrElse { return Result.failure(it) }

        val folder = folderProvider.getReportesFolder(noInspeccion)
            ?: return Result.failure(IllegalStateException("No hay acceso a carpeta Reportes (SAF)."))
        val finalFile = folderProvider.createPdfFile(
            folder,
            "ETIC_RESULTADOS_ANALISIS_DE_RIESGO_CON_TERMOGRAFIA_INSPECCION_$noInspeccion.pdf"
        ) ?: return Result.failure(IllegalStateException("No se pudo crear el PDF final."))

        val sources = mutableListOf<PdfSource>(
                PdfSource.UriSource(Uri.parse(pdfPrincipal)),
                PdfSource.UriSource(Uri.parse(grafica)),
                PdfSource.UriSource(Uri.parse(inventario)),
                PdfSource.UriSource(Uri.parse(problemas)),
                PdfSource.UriSource(Uri.parse(baseline)),
                PdfSource.AssetSource("plantillas_reportes/F-PRS-02_PROCEDIMIENTO_INSPECCIONES.pdf")
            )
        abiertos?.let { sources.add(5, PdfSource.UriSource(Uri.parse(it))) }
        cerrados?.let {
            val insertIndex = if (abiertos != null) 6 else 5
            sources.add(insertIndex, PdfSource.UriSource(Uri.parse(it)))
        }

        return PdfMergeUtil.mergeToFile(
            context = context,
            outputFile = finalFile,
            sources = sources
        )
    }
}
