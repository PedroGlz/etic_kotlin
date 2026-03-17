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

        val grafica = GenerateAnomaliasChartPdfUseCase(context, folderProvider).run(
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
        ).getOrElse { return Result.failure(it) }

        val cerrados = GenerateProblemListPdfUseCase(context, folderProvider).run(
            noInspeccion = noInspeccion,
            inspeccionId = inspeccionId,
            listType = GenerateProblemListPdfUseCase.ProblemListType.CERRADOS,
            currentUserId = currentUserId,
            currentUserName = currentUserName
        ).getOrElse { return Result.failure(it) }

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

        return PdfMergeUtil.mergeToFile(
            context = context,
            outputFile = finalFile,
            sources = listOf(
                PdfSource.UriSource(Uri.parse(pdfPrincipal)),
                PdfSource.UriSource(Uri.parse(grafica)),
                PdfSource.UriSource(Uri.parse(inventario)),
                PdfSource.UriSource(Uri.parse(problemas)),
                PdfSource.UriSource(Uri.parse(baseline)),
                PdfSource.UriSource(Uri.parse(abiertos)),
                PdfSource.UriSource(Uri.parse(cerrados)),
                PdfSource.AssetSource("plantillas_reportes/F-PRS-02_PROCEDIMIENTO_INSPECCIONES.pdf")
            )
        )
    }
}
