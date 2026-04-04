package com.example.etic.features.home

import android.graphics.Bitmap
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.R
import com.example.etic.core.current.CurrentInspectionProvider
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.LocalCurrentUser
import com.example.etic.core.current.ProvideCurrentInspection
import com.example.etic.core.current.ProvideCurrentUser
import com.example.etic.core.export.buildInspectionExportFileName
import com.example.etic.core.inspection.INSPECTION_STATUS_EN_PROGRESO
import com.example.etic.core.inspection.INSPECTION_STATUS_CERRADA
import com.example.etic.core.inspection.importInspectionDatabase
import com.example.etic.core.inspection.changeInspectionStatus
import com.example.etic.core.saf.SafEticManager
import com.example.etic.core.settings.EticPrefs
import com.example.etic.core.settings.settingsDataStore
import com.example.etic.data.local.DbProvider
import com.example.etic.data.local.entities.EstatusInspeccion
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.features.components.SequenceInputButtonGroup
import com.example.etic.features.inspection.data.InspectionRepository
import com.example.etic.features.inspection.ui.home.InspectionScreen
import com.example.etic.features.saf.EticFolderShortcutScreen
import com.example.etic.features.saf.EticFolderType
import com.example.etic.features.inspection.tree.TreeNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.etic.ui.inspection.ReportAction
import com.example.etic.ui.inspection.ReportsMenuSection
import com.example.etic.reports.GenerateAnomaliasChartPdfUseCase
import com.example.etic.reports.GenerateBaselinePdfUseCase
import com.example.etic.reports.GenerateInventarioPdfUseCase
import com.example.etic.reports.GenerateProblemListExcelUseCase
import com.example.etic.reports.GenerateProblemListPdfUseCase
import com.example.etic.reports.GenerateProblemasPdfUseCase
import com.example.etic.reports.GenerateResultadosAnalisisUseCase
import com.example.etic.reports.LoadResultadosAnalisisDraftUseCase
import com.example.etic.reports.ReportesFolderProvider
import com.example.etic.reports.ProblemTypeIds
import com.example.etic.reports.ResultadosAnalisisContacto
import com.example.etic.reports.ResultadosAnalisisDraft
import com.example.etic.reports.ResultadosAnalisisProblemOption
import com.example.etic.reports.ResultadosAnalisisRecomendacion
import com.example.etic.reports.SaveResultadosAnalisisDraftUseCase
import com.example.etic.data.local.queries.CurrentInspectionInfo
import com.example.etic.ui.theme.FontSizeOption
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlin.math.max
import com.example.etic.core.saf.EticImageStore
import com.example.etic.core.saf.ReportsRefreshBus
import com.example.etic.ui.inspection.ResultsAnalysisDialog

private enum class HomeSection {
    Inspection,
    Reports,
    FolderClientImages,
    FolderImages,
    FolderReports
}

private enum class PendingFolderAction {
    None,
    ImportSetup,
    InventarioPdf,
    ProblemasPdf,
    AislamientoTermicoPdf,
    BaselinePdf,
    ListaProblemasAbiertosPdf,
    ListaProblemasCerradosPdf,
    GraficaAnomaliasPdf,
    ListaProblemasExcel,
    ResultadosAnalisis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    userName: String,
    currentFontSize: FontSizeOption = FontSizeOption.Large,
    onChangeFontSize: (FontSizeOption) -> Unit = {},
    onReportAction: ((ReportAction) -> Unit)? = null,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val appContext = LocalContext.current
    val db = remember { DbProvider.get(appContext) }
    val inspeccionDao = remember { db.inspeccionDao() }
    val sitioDao = remember { db.sitioDao() }
    val problemaDao = remember { db.problemaDao() }
    val eticPrefs = remember { EticPrefs(appContext.settingsDataStore) }
    val safManager = remember { SafEticManager() }
    val inspectionRepo = remember {
        InspectionRepository(
            db = db,
            ubicacionDao = db.ubicacionDao(),
            inspeccionDetDao = db.inspeccionDetDao(),
            vistaUbicacionArbolDao = db.vistaUbicacionArbolDao()
        )
    }
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var section by rememberSaveable { mutableStateOf(HomeSection.Inspection) }
    var isLoading by rememberSaveable { mutableStateOf(true) }
    var fontsExpanded by rememberSaveable { mutableStateOf(false) }
    var showInitImagesDialog by rememberSaveable { mutableStateOf(false) }
    var showInitBarcodeDialog by rememberSaveable { mutableStateOf(false) }
    var showInspectionStatusDialog by rememberSaveable { mutableStateOf(false) }
    var showImportInspectionAfterCloseDialog by rememberSaveable { mutableStateOf(false) }
    var initThermalPath by rememberSaveable { mutableStateOf("") }
    var initDigitalPath by rememberSaveable { mutableStateOf("") }
    var initBarcodeValue by rememberSaveable { mutableStateOf("") }
    var isSavingInitImages by rememberSaveable { mutableStateOf(false) }
    var isSavingInitBarcode by rememberSaveable { mutableStateOf(false) }
    var isSavingInspectionStatus by rememberSaveable { mutableStateOf(false) }
    var isImportingInspection by rememberSaveable { mutableStateOf(false) }
    var isGeneratingReport by rememberSaveable { mutableStateOf(false) }
    var inspectionStatusMessage by rememberSaveable { mutableStateOf("Procesando...") }
    var inspectionImportMessage by rememberSaveable { mutableStateOf("Importando inspección...") }
    var showCloseInspectionConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var showInventoryReportDialog by rememberSaveable { mutableStateOf(false) }
    var isLoadingInventoryOptions by rememberSaveable { mutableStateOf(false) }
    var inventoryOptions by remember { mutableStateOf<List<TreeNode>>(emptyList()) }
    var inventorySelection by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }
    var showResultsAnalysisDialog by rememberSaveable { mutableStateOf(false) }
    var isLoadingResultsAnalysisDialog by rememberSaveable { mutableStateOf(false) }
    var resultsAnalysisDraft by remember { mutableStateOf<ResultadosAnalisisDraft?>(null) }
    var resultsLocationOptions by remember { mutableStateOf<List<TreeNode>>(emptyList()) }
    var resultsProblemOptions by remember { mutableStateOf<List<ResultadosAnalisisProblemOption>>(emptyList()) }
    var resultsAvailableImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var resultsAvailableClientImages by remember { mutableStateOf<List<String>>(emptyList()) }
    val imageExtensionRegex = remember { Regex("\\.(jpg|jpeg|png|bmp|gif)$", RegexOption.IGNORE_CASE) }
    val rootTreeUriStr by eticPrefs.rootTreeUriFlow.collectAsState(initial = null)
    val rootTreeUri = remember(rootTreeUriStr) { rootTreeUriStr?.let { Uri.parse(it) } }
    var currentInspectionSnapshot by remember { mutableStateOf<CurrentInspectionInfo?>(null) }
    var currentUserSnapshot by remember { mutableStateOf<com.example.etic.core.current.CurrentUserInfo?>(null) }
    var inspectionStatusOptions by remember { mutableStateOf<List<EstatusInspeccion>>(emptyList()) }
    var selectedInspectionStatusId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingFolderAction by rememberSaveable { mutableStateOf(PendingFolderAction.None) }
    var pendingInitBarcodeAfterImages by rememberSaveable { mutableStateOf(false) }
    var requestFolderAccess by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun inspectionStatusDescription(statusId: String?): String? =
        when (statusId) {
            INSPECTION_STATUS_EN_PROGRESO ->
                "La inspección actual permanecerá activa para captura y edición."
            INSPECTION_STATUS_CERRADA ->
                "La inspección actual se cerrará, se exportará en Descargas y dejará de estar activa."
            else -> null
        }

    fun saveInspectionStatus(inspection: CurrentInspectionInfo, statusId: String) {
        scope.launch {
            isSavingInspectionStatus = true
            inspectionStatusMessage =
                if (statusId == INSPECTION_STATUS_CERRADA) {
                    "Cerrando y generando archivo de la inspección..."
                } else {
                    "Actualizando estatus..."
                }
            val exportFileName = if (statusId == INSPECTION_STATUS_CERRADA) {
                buildInspectionExportFileName(
                    inspectionNumber = inspection.noInspeccion?.toString(),
                    siteName = inspection.nombreSitio
                )
            } else {
                null
            }
            val result = changeInspectionStatus(
                context = appContext,
                inspectionId = inspection.idInspeccion,
                statusId = statusId,
                currentUserId = currentUserSnapshot?.idUsuario,
                exportFileName = exportFileName
            )
            CurrentInspectionProvider.invalidate()
            currentInspectionSnapshot = CurrentInspectionProvider.get(appContext)
            isSavingInspectionStatus = false
            if (result.success) {
                section = HomeSection.Inspection
                showInspectionStatusDialog = false
                showCloseInspectionConfirmDialog = false
                if (result.inspectionCleared) {
                    showImportInspectionAfterCloseDialog = true
                }
            }
            Toast.makeText(appContext, result.message, Toast.LENGTH_LONG).show()
        }
    }

    fun collectNodeIds(node: TreeNode, out: MutableList<String>) {
        out.add(node.id)
        node.children.forEach { collectNodeIds(it, out) }
    }

    fun problemTypeLabel(typeId: String?): String =
        when (typeId?.uppercase()) {
            ProblemTypeIds.ELECTRICO, ProblemTypeIds.ELECTRICO_2 -> "Electrico"
            ProblemTypeIds.VISUAL -> "Visual"
            ProblemTypeIds.AISLAMIENTO_TERMICO -> "Aislamiento termico"
            "0D32B334-76C3-11D3-82BF-00104BC75DC2" -> "Mecanico"
            else -> "Problema"
        }

    suspend fun buildDefaultResultsContacts(siteId: String?): List<ResultadosAnalisisContacto> {
        val site = siteId?.let { sitioDao.getByIdActivo(it) }
        return listOf(
            ResultadosAnalisisContacto(site?.contacto1.orEmpty(), site?.puestoContacto1.orEmpty()),
            ResultadosAnalisisContacto(site?.contacto2.orEmpty(), site?.puestoContacto2.orEmpty()),
            ResultadosAnalisisContacto(site?.contacto3.orEmpty(), site?.puestoContacto3.orEmpty())
        )
    }

    fun buildFolderProvider(): ReportesFolderProvider {
        return ReportesFolderProvider(appContext) { inspectionNumber ->
            val currentRootUri = rootTreeUri ?: return@ReportesFolderProvider null
            safManager.getReportsDir(appContext, currentRootUri, inspectionNumber)?.uri
        }
    }

    fun buildImagesProvider(): (String) -> Uri? = { inspectionNumber ->
        val currentRootUri = rootTreeUri
        if (currentRootUri == null) null
        else safManager.getImagesDir(appContext, currentRootUri, inspectionNumber)?.uri
    }

    suspend fun ensureInspectionFoldersReady(inspectionNumber: String?): Boolean {
        val rootUri = rootTreeUri ?: return false
        if (inspectionNumber.isNullOrBlank()) return false
        return withContext(Dispatchers.IO) {
            val inspectionsRoot = safManager.ensureEticFolders(appContext, rootUri)
            val created = safManager.ensureInspectionFolders(appContext, rootUri, inspectionNumber)
            inspectionsRoot != null && created.first != null && created.second != null
        }
    }

    fun openInitBarcodeDialogForCurrentInspection() {
        initBarcodeValue = ""
        val currentInspectionForBarcode = currentInspectionSnapshot
        if (currentInspectionForBarcode?.idInspeccion.isNullOrBlank()) {
            Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val value = withContext(Dispatchers.IO) {
                runCatching {
                    inspeccionDao.getById(currentInspectionForBarcode?.idInspeccion.orEmpty())
                        ?.codigoBarrasInicial
                }.getOrNull()
            }
            initBarcodeValue = value.orEmpty()
            showInitBarcodeDialog = true
        }
    }

    fun openInventoryReportDialog(insp: CurrentInspectionInfo) {
        showInventoryReportDialog = true
        if (inventoryOptions.isEmpty() && !isLoadingInventoryOptions) {
            isLoadingInventoryOptions = true
            val rootId = insp.idSitio?.let { "root:$it" } ?: "root:site"
            val rootTitle = insp.nombreSitio ?: "Sitio"
            scope.launch {
                val roots = inspectionRepo.loadTree(rootId, rootTitle, insp.idInspeccion)
                val children = roots.firstOrNull()?.children.orEmpty()
                inventoryOptions = children
                inventorySelection = children.associate { it.id to false }
                isLoadingInventoryOptions = false
            }
        }
    }

    fun startPostImportSetup() {
        showInitImagesDialog = true
        pendingInitBarcodeAfterImages = true
    }

    suspend fun ensureFolderAccessAndStructure(
        inspectionNumber: String?,
        actionIfMissingRoot: PendingFolderAction
    ): Boolean {
        if (inspectionNumber.isNullOrBlank()) return false
        if (rootTreeUri == null) {
            pendingFolderAction = actionIfMissingRoot
            requestFolderAccess?.invoke()
            return false
        }
        val ready = ensureInspectionFoldersReady(inspectionNumber)
        if (!ready) {
            Toast.makeText(appContext, "No se pudo preparar la carpeta de la inspección.", Toast.LENGTH_LONG).show()
        }
        return ready
    }

    fun fileNameFromUri(uriString: String): String {
        val uri = runCatching { Uri.parse(uriString) }.getOrNull()
        val name = uri?.let { parsed ->
            DocumentFile.fromSingleUri(appContext, parsed)?.name
        }.orEmpty()
        return name.ifBlank {
            uri?.lastPathSegment?.substringAfterLast('/')?.substringAfterLast(':').orEmpty().ifBlank { "archivo" }
        }
    }

    fun reportSavedMessage(kind: String, uriString: String): String {
        val fileName = fileNameFromUri(uriString)
        return "$kind guardado en Archivos como $fileName"
    }

    fun reportErrorMessage(kind: String, error: Throwable): String {
        val detail = error.message?.trim().orEmpty()
        return if (detail.isBlank()) {
            "No se pudo generar $kind."
        } else {
            "No se pudo generar $kind. $detail"
        }
    }

    fun openResultsAnalysisDialog(insp: CurrentInspectionInfo) {
        if (isGeneratingReport || isLoadingResultsAnalysisDialog) return
        val noInspeccion = insp.noInspeccion?.toString()
        if (noInspeccion.isNullOrBlank()) {
            Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.ResultadosAnalisis)) {
                return@launch
            }
            isLoadingResultsAnalysisDialog = true
            try {
                val rootId = insp.idSitio?.let { "root:$it" } ?: "root:site"
                val rootTitle = insp.nombreSitio ?: "Sitio"
                val roots = inspectionRepo.loadTree(rootId, rootTitle, insp.idInspeccion)
                val locationRoots = roots.firstOrNull()?.children.orEmpty()
                resultsLocationOptions = locationRoots

                val activeProblems = problemaDao.getByInspeccionActivos(insp.idInspeccion)
                    .sortedWith(
                        compareBy(
                            { it.idTipoInspeccion ?: "ZZZ" },
                            { it.numeroProblema ?: Int.MAX_VALUE }
                        )
                    )
                val problemOptions = activeProblems.map { problem ->
                    ResultadosAnalisisProblemOption(
                        id = problem.idProblema,
                        label = "${problemTypeLabel(problem.idTipoInspeccion)} ${problem.numeroProblema ?: ""} - ${problem.ruta.orEmpty()}".trim()
                    )
                }
                resultsProblemOptions = problemOptions

                val defaultContacts = withContext(Dispatchers.IO) {
                    buildDefaultResultsContacts(insp.idSitio)
                }
                val defaultDraft = ResultadosAnalisisDraft(
                    inspectionId = insp.idInspeccion,
                    siteId = insp.idSitio,
                    contactos = defaultContacts,
                    fechaInicio = "",
                    fechaFin = "",
                    descripciones = listOf(""),
                    areasInspeccionadas = listOf(""),
                    recomendaciones = listOf(ResultadosAnalisisRecomendacion()),
                    referencias = listOf(""),
                    selectedInventoryIds = locationRoots.map { it.id },
                    selectedProblemIds = problemOptions.map { it.id }
                )

                val loadedDraft = LoadResultadosAnalisisDraftUseCase(appContext).run(
                    inspectionId = insp.idInspeccion,
                    siteId = insp.idSitio,
                    defaultDraft = defaultDraft
                ).getOrElse { defaultDraft }
                val previousInspectionDate = runCatching {
                    val previousNo = inspeccionDao.getById(insp.idInspeccion)?.noInspeccionAnt ?: return@runCatching ""
                    inspeccionDao.getAll().firstOrNull { it.noInspeccion == previousNo }?.fechaInicio.orEmpty()
                }.getOrNull().orEmpty()

                resultsAnalysisDraft = loadedDraft.copy(
                    fechaInicio = loadedDraft.fechaInicio,
                    fechaFin = loadedDraft.fechaFin.ifBlank { loadedDraft.fechaInicio },
                    fechaAnterior = loadedDraft.fechaAnterior.ifBlank { previousInspectionDate },
                    selectedInventoryIds = loadedDraft.selectedInventoryIds.ifEmpty { locationRoots.map { it.id } },
                    selectedProblemIds = loadedDraft.selectedProblemIds.ifEmpty { problemOptions.map { it.id } }
                )

                resultsAvailableImages = withContext(Dispatchers.IO) {
                    val currentRootUri = rootTreeUri ?: return@withContext emptyList()
                    val dir = safManager.getImagesDir(appContext, currentRootUri, noInspeccion)
                    safManager.listFiles(dir)
                        .mapNotNull { it.name }
                        .filter { imageExtensionRegex.containsMatchIn(it) }
                        .sorted()
                }
                resultsAvailableClientImages = withContext(Dispatchers.IO) {
                    val currentRootUri = rootTreeUri ?: return@withContext emptyList()
                    val dir = safManager.getClientesDir(appContext, currentRootUri)
                    safManager.listFiles(dir)
                        .mapNotNull { it.name }
                        .filter { imageExtensionRegex.containsMatchIn(it) }
                        .sorted()
                }

                if (resultsAnalysisDraft?.fechaInicio.isNullOrBlank()) {
                    val start = inspeccionDao.getById(insp.idInspeccion)?.fechaInicio?.take(10).orEmpty()
                    val end = inspeccionDao.getById(insp.idInspeccion)?.fechaFin?.take(10).orEmpty()
                    resultsAnalysisDraft = resultsAnalysisDraft?.copy(
                        fechaInicio = start,
                        fechaFin = end.ifBlank { start }
                    )
                }

                showResultsAnalysisDialog = true
            } finally {
                isLoadingResultsAnalysisDialog = false
            }
        }
    }

    fun saveResultsAnalysisDraft(draft: ResultadosAnalisisDraft, silent: Boolean = true) {
        scope.launch {
            val result = SaveResultadosAnalisisDraftUseCase(appContext).run(draft)
            if (!silent && result.isFailure) {
                Toast.makeText(appContext, "No se pudo guardar el borrador.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun generateResultadosAnalisis(
        insp: CurrentInspectionInfo,
        draft: ResultadosAnalisisDraft,
        selectedInventoryIds: List<String>
    ) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        if (noInspeccion.isNullOrBlank()) {
            Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            isGeneratingReport = true
            drawerState.close()
            showResultsAnalysisDialog = false
            try {
                SaveResultadosAnalisisDraftUseCase(appContext).run(draft).getOrElse { throw it }
                val useCase = GenerateResultadosAnalisisUseCase(
                    context = appContext,
                    folderProvider = buildFolderProvider(),
                    getInspeccionImagenesTreeUri = buildImagesProvider(),
                    getClientesImagenesTreeUri = { _: String ->
                        rootTreeUri?.let { safManager.getClientesDir(appContext, it)?.uri }
                    }
                )
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = insp.idInspeccion,
                    draft = draft,
                    selectedInventoryIds = selectedInventoryIds,
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(
                            appContext,
                            reportSavedMessage("el resultado de análisis", uriString),
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el resultado de análisis", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateInventarioPdf(insp: CurrentInspectionInfo, selectedUbicacionIds: List<String>) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.InventarioPdf)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                val reportsDir = safManager.getReportsDir(appContext, rootUri, inspectionNumber)
                reportsDir?.uri
            }
            val useCase = GenerateInventarioPdfUseCase(appContext, folderProvider)
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    selectedUbicacionIds = selectedUbicacionIds,
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("el reporte de inventario", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el reporte de inventario", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateProblemasPdf(insp: CurrentInspectionInfo) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.ProblemasPdf)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                val reportsDir = safManager.getReportsDir(appContext, rootUri, inspectionNumber)
                reportsDir?.uri
            }
            val useCase = GenerateProblemasPdfUseCase(
                context = appContext,
                folderProvider = folderProvider,
                getInspeccionImagenesTreeUri = { inspectionNumber ->
                    val rootUri = rootTreeUri
                    if (rootUri == null) {
                        null
                    } else {
                        val imagesDir = safManager.getImagesDir(appContext, rootUri, inspectionNumber)
                        imagesDir?.uri
                    }
                }
            )
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    selectedProblemaIds = emptyList(),
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("el reporte de problemas", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el reporte de problemas", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateAislamientoTermicoPdf(insp: CurrentInspectionInfo) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.AislamientoTermicoPdf)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                val reportsDir = safManager.getReportsDir(appContext, rootUri, inspectionNumber)
                reportsDir?.uri
            }
            val useCase = GenerateProblemasPdfUseCase(
                context = appContext,
                folderProvider = folderProvider,
                getInspeccionImagenesTreeUri = { inspectionNumber ->
                    val rootUri = rootTreeUri
                    if (rootUri == null) {
                        null
                    } else {
                        val imagesDir = safManager.getImagesDir(appContext, rootUri, inspectionNumber)
                        imagesDir?.uri
                    }
                }
            )
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    selectedProblemaIds = emptyList(),
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario,
                    allowedTypeIds = listOf(ProblemTypeIds.AISLAMIENTO_TERMICO),
                    fileNameOverride = "ETIC_AISLAMIENTO_TERMICO_INSPECCION_$noInspeccion.pdf"
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("el reporte de aislamiento térmico", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el reporte de aislamiento térmico", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateBaselinePdf(insp: CurrentInspectionInfo) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.BaselinePdf)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                val reportsDir = safManager.getReportsDir(appContext, rootUri, inspectionNumber)
                reportsDir?.uri
            }
            val useCase = GenerateBaselinePdfUseCase(
                context = appContext,
                folderProvider = folderProvider,
                getInspeccionImagenesTreeUri = { inspectionNumber ->
                    val rootUri = rootTreeUri
                    if (rootUri == null) {
                        null
                    } else {
                        safManager.getImagesDir(appContext, rootUri, inspectionNumber)?.uri
                    }
                }
            )
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("el reporte de baseline", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el reporte de baseline", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateListaProblemasPdf(
        insp: CurrentInspectionInfo,
        tipo: GenerateProblemListPdfUseCase.ProblemListType
    ) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            val pendingAction = when (tipo) {
                GenerateProblemListPdfUseCase.ProblemListType.ABIERTOS -> PendingFolderAction.ListaProblemasAbiertosPdf
                GenerateProblemListPdfUseCase.ProblemListType.CERRADOS -> PendingFolderAction.ListaProblemasCerradosPdf
            }
            if (!ensureFolderAccessAndStructure(noInspeccion, pendingAction)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                safManager.getReportsDir(appContext, rootUri, inspectionNumber)?.uri
            }
            val useCase = GenerateProblemListPdfUseCase(appContext, folderProvider)
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    listType = tipo,
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("la lista de problemas", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("la lista de problemas", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateGraficaAnomaliasPdf(insp: CurrentInspectionInfo) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.GraficaAnomaliasPdf)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                safManager.getReportsDir(appContext, rootUri, inspectionNumber)?.uri
            }
            val useCase = GenerateAnomaliasChartPdfUseCase(appContext, folderProvider)
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId,
                    currentUserId = currentUserSnapshot?.idUsuario,
                    currentUserName = currentUserSnapshot?.nombre ?: currentUserSnapshot?.usuario
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("la gráfica de anomalías", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("la gráfica de anomalías", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    fun generateListaProblemasExcel(insp: CurrentInspectionInfo) {
        if (isGeneratingReport) return
        val noInspeccion = insp.noInspeccion?.toString()
        val inspeccionId = insp.idInspeccion
        if (noInspeccion.isNullOrBlank() || inspeccionId.isNullOrBlank()) {
            Toast.makeText(appContext, "Inspección inválida.", Toast.LENGTH_SHORT).show()
            return
        }
        scope.launch {
            if (!ensureFolderAccessAndStructure(noInspeccion, PendingFolderAction.ListaProblemasExcel)) {
                return@launch
            }
            isGeneratingReport = true
            drawerState.close()
            val folderProvider = ReportesFolderProvider(appContext) { inspectionNumber ->
                val rootUri = rootTreeUri ?: return@ReportesFolderProvider null
                safManager.getReportsDir(appContext, rootUri, inspectionNumber)?.uri
            }
            val useCase = GenerateProblemListExcelUseCase(appContext, folderProvider)
            try {
                val result = useCase.run(
                    noInspeccion = noInspeccion,
                    inspeccionId = inspeccionId
                )
                result.fold(
                    onSuccess = { uriString ->
                        ReportsRefreshBus.notifyChanged()
                        Toast.makeText(appContext, reportSavedMessage("el archivo de Excel", uriString), Toast.LENGTH_LONG).show()
                    },
                    onFailure = { e ->
                        Toast.makeText(
                            appContext,
                            reportErrorMessage("el archivo de Excel", e),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } finally {
                isGeneratingReport = false
            }
        }
    }

    val reportHandler: (ReportAction) -> Unit = { action ->
        when (action) {
            ReportAction.InventarioPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    openInventoryReportDialog(insp)
                }
            }
            ReportAction.ProblemasPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateProblemasPdf(insp)
                }
            }
            ReportAction.AislamientoTermicoPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateAislamientoTermicoPdf(insp)
                }
            }
            ReportAction.BaselinePdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateBaselinePdf(insp)
                }
            }
            ReportAction.ListaProblemasAbiertosPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateListaProblemasPdf(insp, GenerateProblemListPdfUseCase.ProblemListType.ABIERTOS)
                }
            }
            ReportAction.ListaProblemasCerradosPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateListaProblemasPdf(insp, GenerateProblemListPdfUseCase.ProblemListType.CERRADOS)
                }
            }
            ReportAction.GraficaAnomaliasPdf -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateGraficaAnomaliasPdf(insp)
                }
            }
            ReportAction.ListaProblemasExcel -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    generateListaProblemasExcel(insp)
                }
            }
            ReportAction.ResultadosAnalisis -> {
                val insp = currentInspectionSnapshot
                if (insp == null) {
                    Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                } else {
                    openResultsAnalysisDialog(insp)
                }
            }
        }
    }
    val drawerItemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = Color(0xFF202327),
        unselectedContainerColor = Color.Transparent,
        selectedTextColor = Color.White,
        unselectedTextColor = Color.White,
        selectedIconColor = Color.White,
        unselectedIconColor = Color.White
    )
    val importInspectionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isImportingInspection = true
            inspectionImportMessage = "Importando inspección..."
            val result = importInspectionDatabase(appContext, uri)
            CurrentInspectionProvider.invalidate()
            currentInspectionSnapshot = CurrentInspectionProvider.get(appContext)
            isImportingInspection = false
            if (result.success) {
                section = HomeSection.Inspection
                val importedInspectionNumber = currentInspectionSnapshot?.noInspeccion?.toString()
                if (rootTreeUri == null) {
                    pendingFolderAction = PendingFolderAction.ImportSetup
                    requestFolderAccess?.invoke()
                } else {
                    scope.launch {
                        val ready = ensureInspectionFoldersReady(importedInspectionNumber)
                        if (ready) {
                            startPostImportSetup()
                        } else {
                            Toast.makeText(appContext, "No se pudo preparar la carpeta de la inspección.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            Toast.makeText(appContext, result.message, Toast.LENGTH_LONG).show()
        }
    }

    val folderTreeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri == null) {
            pendingFolderAction = PendingFolderAction.None
            return@rememberLauncherForActivityResult
        }
        scope.launch {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            runCatching { appContext.contentResolver.takePersistableUriPermission(uri, flags) }
            eticPrefs.setRootTreeUri(uri)
            val inspectionNumber = currentInspectionSnapshot?.noInspeccion?.toString()
            val ready = withContext(Dispatchers.IO) {
                safManager.ensureEticFolders(appContext, uri)
                if (!inspectionNumber.isNullOrBlank()) {
                    val pair = safManager.ensureInspectionFolders(appContext, uri, inspectionNumber)
                    pair.first != null && pair.second != null
                } else {
                    true
                }
            }
            if (!ready) {
                Toast.makeText(appContext, "No se pudo preparar la carpeta de la inspección.", Toast.LENGTH_LONG).show()
                pendingFolderAction = PendingFolderAction.None
                return@launch
            }
            when (pendingFolderAction) {
                PendingFolderAction.ImportSetup -> startPostImportSetup()
                PendingFolderAction.InventarioPdf -> currentInspectionSnapshot?.let { openInventoryReportDialog(it) }
                PendingFolderAction.ProblemasPdf -> currentInspectionSnapshot?.let { generateProblemasPdf(it) }
                PendingFolderAction.AislamientoTermicoPdf -> currentInspectionSnapshot?.let { generateAislamientoTermicoPdf(it) }
                PendingFolderAction.BaselinePdf -> currentInspectionSnapshot?.let { generateBaselinePdf(it) }
                PendingFolderAction.ListaProblemasAbiertosPdf -> currentInspectionSnapshot?.let {
                    generateListaProblemasPdf(it, GenerateProblemListPdfUseCase.ProblemListType.ABIERTOS)
                }
                PendingFolderAction.ListaProblemasCerradosPdf -> currentInspectionSnapshot?.let {
                    generateListaProblemasPdf(it, GenerateProblemListPdfUseCase.ProblemListType.CERRADOS)
                }
                PendingFolderAction.GraficaAnomaliasPdf -> currentInspectionSnapshot?.let { generateGraficaAnomaliasPdf(it) }
                PendingFolderAction.ListaProblemasExcel -> currentInspectionSnapshot?.let { generateListaProblemasExcel(it) }
                PendingFolderAction.ResultadosAnalisis -> currentInspectionSnapshot?.let { openResultsAnalysisDialog(it) }
                PendingFolderAction.None -> Unit
            }
            pendingFolderAction = PendingFolderAction.None
        }
    }
    requestFolderAccess = { folderTreeLauncher.launch(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = true, // permite cerrar al tocar fuera / gesto
        scrimColor = Color.Black.copy(alpha = 0.32f),
        drawerContent = {
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val drawerWidth = when {
                screenWidth < 360.dp -> screenWidth - 24.dp
                screenWidth < 600.dp -> 250.dp
                else -> 260.dp
            }

            ModalDrawerSheet(
                modifier = Modifier.width(drawerWidth),
                drawerContainerColor = Color(0xFF202327),
                drawerContentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("ETIC System", color = Color.White) },
                        supportingContent = { Text(userName, color = Color.LightGray) },
                        leadingContent = {
                            Image(
                                painter = painterResource(id = R.drawable.img_etic_menu),
                                contentDescription = "Logo ETIC",
                                modifier = Modifier.size(65.dp)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )

                    Spacer(Modifier.height(4.dp))
                    NavigationDrawerItem(
                        label = { Text("Estatus de inspección") },
                        selected = false,
                        onClick = {
                            val insp = currentInspectionSnapshot
                            if (insp == null) {
                                Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    val options = withContext(Dispatchers.IO) {
                                        runCatching {
                                            DbProvider.get(appContext).estatusInspeccionDao().getAllActivos()
                                        }.getOrElse { emptyList() }
                                    }
                                    inspectionStatusOptions = options
                                    selectedInspectionStatusId = insp.idStatusInspeccion
                                    showInspectionStatusDialog = true
                                    fontsExpanded = false
                                    drawerState.close()
                                }
                            }
                        },
                        icon = { Icon(Icons.Filled.Check, contentDescription = null) },
                        colors = drawerItemColors
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 4.dp),
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    NavigationDrawerItem(
                        label = { Text("Inspección actual") },
                        selected = section == HomeSection.Inspection,
                        onClick = {
                            section = HomeSection.Inspection
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Filled.ChecklistRtl, contentDescription = null) },
                        colors = drawerItemColors
                    )
                    NavigationDrawerItem(
                        label = { Text("Inicializar imágenes") },
                        selected = false,
                        onClick = {
                            showInitImagesDialog = true
                            fontsExpanded = false
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Filled.Image, contentDescription = null) },
                        colors = drawerItemColors
                    )
                    NavigationDrawerItem(
                        label = { Text("Inicializar codigo de barras") },
                        selected = false,
                        onClick = {
                            initBarcodeValue = ""
                            val currentInspectionForBarcode = currentInspectionSnapshot
                            if (currentInspectionForBarcode?.idInspeccion.isNullOrBlank()) {
                                Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                            } else {
                                scope.launch {
                                    val value = withContext(Dispatchers.IO) {
                                        runCatching {
                                            inspeccionDao.getById(currentInspectionForBarcode?.idInspeccion.orEmpty())
                                                ?.codigoBarrasInicial
                                        }.getOrNull()
                                    }
                                    initBarcodeValue = value.orEmpty()
                                    showInitBarcodeDialog = true
                                }
                            }
                            fontsExpanded = false
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Filled.Image, contentDescription = null) },
                        colors = drawerItemColors
                    )

                    HorizontalDivider(
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    NavigationDrawerItem(
                        label = { Text("Carpeta img clientes") },
                        selected = section == HomeSection.FolderClientImages,
                        onClick = {
                            section = HomeSection.FolderClientImages
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                        colors = drawerItemColors
                    )

                    NavigationDrawerItem(
                        label = { Text("Carpeta Imágenes") },
                        selected = section == HomeSection.FolderImages,
                        onClick = {
                            section = HomeSection.FolderImages
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                        colors = drawerItemColors
                    )

                    NavigationDrawerItem(
                        label = { Text("Carpeta de reportes") },
                        selected = section == HomeSection.FolderReports,
                        onClick = {
                            section = HomeSection.FolderReports
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                        colors = drawerItemColors
                    )

                    HorizontalDivider(
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    ReportsMenuSection(
                        onReport = { action -> (onReportAction ?: reportHandler)(action) },
                        enabled = !isGeneratingReport
                    )

                    HorizontalDivider(
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )

                    // Opción Fuentes con subopciones
                    NavigationDrawerItem(
                        label = { Text("Fuentes") },
                        selected = false,
                        onClick = { fontsExpanded = !fontsExpanded },
                        icon = {
                            Icon(
                                imageVector = if (fontsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (fontsExpanded) "Contraer" else "Expandir"
                            )
                        },
                        colors = drawerItemColors
                    )
                    if (fontsExpanded) {
                        val isSmall = currentFontSize == FontSizeOption.Small
                        val isMedium = currentFontSize == FontSizeOption.Medium
                        val isLarge = currentFontSize == FontSizeOption.Large
                        NavigationDrawerItem(
                            label = { Text("Pequeña") },
                            selected = isSmall,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Small)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isSmall) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                        NavigationDrawerItem(
                            label = { Text("Mediana") },
                            selected = isMedium,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Medium)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isMedium) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                        NavigationDrawerItem(
                            label = { Text("Grande") },
                            selected = isLarge,
                            onClick = {
                                onChangeFontSize(FontSizeOption.Large)
                                fontsExpanded = false
                                scope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(start = 24.dp, top = 2.dp, bottom = 2.dp),
                            icon = { if (isLarge) Icon(Icons.Filled.Check, contentDescription = null) },
                            colors = drawerItemColors
                        )
                    }

                    HorizontalDivider(
                        thickness = androidx.compose.material3.DividerDefaults.Thickness,
                        color = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }
    ) {
        ProvideCurrentUser {
            ProvideCurrentInspection {
                val currentUser = LocalCurrentUser.current
                val currentInspection = LocalCurrentInspection.current
                SideEffect {
                    currentUserSnapshot = currentUser
                    currentInspectionSnapshot = currentInspection
                }
                val inspectionNumero = currentInspection?.noInspeccion?.toString()
                val canEditInspection = currentInspection?.idStatusInspeccion == INSPECTION_STATUS_EN_PROGRESO

                LaunchedEffect(section, currentInspection?.idInspeccion, canEditInspection, showImportInspectionAfterCloseDialog) {
                    if (section == HomeSection.Inspection && !canEditInspection) {
                        showImportInspectionAfterCloseDialog = true
                    }
                    if (section != HomeSection.Inspection || canEditInspection) {
                        showImportInspectionAfterCloseDialog = false
                    }
                }

                LaunchedEffect(rootTreeUriStr, inspectionNumero) {
                    eticPrefs.setActiveInspectionNum(inspectionNumero)
                    val uri = rootTreeUriStr?.let { Uri.parse(it) } ?: return@LaunchedEffect
                    withContext(Dispatchers.IO) {
                        safManager.ensureEticFolders(appContext, uri)
                        if (!inspectionNumero.isNullOrBlank()) {
                            safManager.ensureInspectionFolders(appContext, uri, inspectionNumero)
                        }
                    }
                }

                LaunchedEffect(showInitImagesDialog, pendingInitBarcodeAfterImages) {
                    if (!showInitImagesDialog && pendingInitBarcodeAfterImages) {
                        pendingInitBarcodeAfterImages = false
                        openInitBarcodeDialogForCurrentInspection()
                    }
                }

                fun saveCameraBitmap(prefix: String, bmp: Bitmap): String? {
                    return EticImageStore.saveBitmap(
                        context = appContext,
                        rootTreeUri = rootTreeUri,
                        inspectionNumero = inspectionNumero,
                        prefix = prefix,
                        bmp = bmp
                    )
                }
                fun loadInitialImageFromDb(isThermal: Boolean, onResult: (String) -> Unit) {
                    val inspId = currentInspection?.idInspeccion
                    if (inspId.isNullOrBlank()) {
                        Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    scope.launch {
                        val value = withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }
                                .getOrNull()
                                ?.let { if (isThermal) it.irImagenInicial else it.digImagenInicial }
                        }
                        if (!value.isNullOrBlank()) {
                            val parts = parseImageName(value)
                            val incremented = if (parts.digits == 0 && parts.number == 0L) {
                                value
                            } else {
                                composeImageName(parts.copy(number = parts.number + 1))
                            }
                            onResult("")
                            onResult(incremented)
                        } else {
                            onResult("")
                            val label = if (isThermal) "IR" else "digital"
                            Toast.makeText(
                                appContext,
                                "No se encontró imagen $label inicial.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                fun loadInitialBarcodeFromDb(onResult: (String) -> Unit) {
                    val inspId = currentInspection?.idInspeccion
                    if (inspId.isNullOrBlank()) {
                        Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    scope.launch {
                        val value = withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }
                                .getOrNull()
                                ?.codigoBarrasInicial
                        }
                        onResult(value.orEmpty())
                    }
                }
                val thermalCameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicturePreview()
                ) { bmp ->
                    if (bmp != null) {
                        val name = saveCameraBitmap("IR", bmp)
                        if (name != null) {
                            initThermalPath = ""
                            initThermalPath = name
                        } else {
                            Toast.makeText(appContext, "No se pudo guardar la imagen térmica.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(appContext, "La cámara no devolvió imagen.", Toast.LENGTH_SHORT).show()
                    }
                }
                val digitalCameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicturePreview()
                ) { bmp ->
                    if (bmp != null) {
                        val name = saveCameraBitmap("DIG", bmp)
                        if (name != null) {
                            initDigitalPath = ""
                            initDigitalPath = name
                        } else {
                            Toast.makeText(appContext, "No se pudo guardar la imagen digital.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(appContext, "La cámara no devolvió imagen.", Toast.LENGTH_SHORT).show()
                    }
                }

                // BLUR para TODO el Scaffold (header + contenido)
                val blurModifier =
                    if (drawerState.isOpen) Modifier.blur(8.dp) else Modifier

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                val info = LocalCurrentInspection.current
                                val titleText = info?.let { data ->
                                    val no = data.noInspeccion?.toString() ?: "-"
                                    val cliente =
                                        data.nombreCliente?.ifBlank { data.idCliente ?: "-" } ?: "-"
                                    "No. Inspección actual: $no - Cliente: $cliente"
                                } ?: "Pantalla principal"
                                Text(titleText)
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        if (drawerState.isOpen) drawerState.close()
                                        else drawerState.open()
                                    }
                                }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Abrir menú")
                                }
                            },
                            actions = {
                                IconButton(onClick = { showLogoutDialog = true }) {
                                    Icon(Icons.Filled.Logout, contentDescription = "Cerrar sesión")
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFF202327),
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    },
                    modifier = modifier.then(blurModifier)
                ) { innerPadding ->

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {

                            Box(
                                modifier = if (section == HomeSection.Inspection) {
                                    Modifier.fillMaxSize()
                                } else {
                                    Modifier.size(0.dp)
                                }
                            ) {
                                InspectionScreen(
                                    isInteractionEnabled = canEditInspection,
                                    onReady = {
                                        scope.launch {
                                            delay(900)
                                            isLoading = false
                                        }
                                }
                            )
                        }

                        Box(
                            modifier = if (section == HomeSection.Reports) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.size(0.dp)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Reportes")
                        }

                        Box(
                            modifier = if (section == HomeSection.FolderImages) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.size(0.dp)
                            }
                        ) {
                            EticFolderShortcutScreen(
                                folderType = EticFolderType.Images,
                                rootTreeUri = rootTreeUri,
                                inspectionNumero = inspectionNumero,
                                onPickRoot = { uri ->
                                    scope.launch {
                                        eticPrefs.setRootTreeUri(uri)
                                        withContext(Dispatchers.IO) {
                                            safManager.ensureEticFolders(appContext, uri)
                                            if (!inspectionNumero.isNullOrBlank()) {
                                                safManager.ensureInspectionFolders(appContext, uri, inspectionNumero)
                                            }
                                        }
                                    }
                                },
                                manager = safManager,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = if (section == HomeSection.FolderClientImages) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.size(0.dp)
                            }
                        ) {
                            EticFolderShortcutScreen(
                                folderType = EticFolderType.ClientImages,
                                rootTreeUri = rootTreeUri,
                                inspectionNumero = inspectionNumero,
                                onPickRoot = { uri ->
                                    scope.launch {
                                        eticPrefs.setRootTreeUri(uri)
                                        withContext(Dispatchers.IO) {
                                            safManager.ensureEticFolders(appContext, uri)
                                            if (!inspectionNumero.isNullOrBlank()) {
                                                safManager.ensureInspectionFolders(appContext, uri, inspectionNumero)
                                            }
                                        }
                                    }
                                },
                                manager = safManager,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = if (section == HomeSection.FolderReports) {
                                Modifier.fillMaxSize()
                            } else {
                                Modifier.size(0.dp)
                            }
                        ) {
                            EticFolderShortcutScreen(
                                folderType = EticFolderType.Reports,
                                rootTreeUri = rootTreeUri,
                                inspectionNumero = inspectionNumero,
                                onPickRoot = { uri ->
                                    scope.launch {
                                        eticPrefs.setRootTreeUri(uri)
                                        withContext(Dispatchers.IO) {
                                            safManager.ensureEticFolders(appContext, uri)
                                            if (!inspectionNumero.isNullOrBlank()) {
                                                safManager.ensureInspectionFolders(appContext, uri, inspectionNumero)
                                            }
                                        }
                                    }
                                },
                                manager = safManager,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (isLoading) {
                            LoadingOverlay("Cargando datos...")
                        }
                        if (isGeneratingReport) {
                            LoadingOverlay("Generando reporte...")
                        }
                        if (isImportingInspection) {
                            LoadingOverlay(inspectionImportMessage)
                        }
                        if (isSavingInspectionStatus) {
                            LoadingOverlay(inspectionStatusMessage)
                        }
                    }
                }

                if (showLogoutDialog) {
                    AlertDialog(
                        onDismissRequest = { showLogoutDialog = false },
                        title = { Text("Cerrar sesión") },
                        text = { Text("¿Seguro que deseas cerrar sesión?") },
                        dismissButton = {
                            TextButton(onClick = { showLogoutDialog = false }) {
                                Text("Cancelar")
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutDialog = false
                                onLogout()
                            }) {
                                Text("Cerrar sesión")
                            }
                        }
                    )
                }

                if (showInitImagesDialog) {
                    Dialog(
                        onDismissRequest = { /* bloqueo: solo botones cierran */ },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        )
                    ) {
                        Card(
                            colors = CardDefaults.cardColors()
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 470.dp)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "Inicializar imágenes")

                                Text(text = "Imagen térmica", style = MaterialTheme.typography.titleSmall)
                                ImageInputButtonGroup(
                                    label = "Archivo IR",
                                    value = initThermalPath,
                                    onValueChange = { initThermalPath = it },
                        modifier = Modifier.fillMaxWidth(),
                        isRequired = true,
                        onMoveUp = { initThermalPath = adjustImageSequence(initThermalPath, +1) },
                        onMoveDown = { initThermalPath = adjustImageSequence(initThermalPath, -1) },
                        onDotsClick = { loadInitialImageFromDb(true) { initThermalPath = it } },
                        onFolderClick = { /* TODO: IR explorador */ },
                        onCameraClick = { thermalCameraLauncher.launch(null) }
                    )
                                Text(text = "Imagen digital", style = MaterialTheme.typography.titleSmall)
                                ImageInputButtonGroup(
                                    label = "Archivo ID",
                                    value = initDigitalPath,
                                    onValueChange = { initDigitalPath = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isRequired = true,
                                    onMoveUp = { initDigitalPath = adjustImageSequence(initDigitalPath, +1) },
                                    onMoveDown = { initDigitalPath = adjustImageSequence(initDigitalPath, -1) },
                                    onDotsClick = { loadInitialImageFromDb(false) { initDigitalPath = it } },
                                    onFolderClick = { /* TODO: ID explorador */ },
                                    onCameraClick = { digitalCameraLauncher.launch(null) }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        enabled = !isSavingInitImages,
                                        onClick = {
                                            if (!isSavingInitImages) {
                                                showInitImagesDialog = false
                                            }
                                        }
                                    ) {
                                        Text("Cancelar")
                                    }
                                    val canSave = initThermalPath.isNotBlank() &&
                                            initDigitalPath.isNotBlank() &&
                                            imageExtensionRegex.containsMatchIn(initThermalPath.trim()) &&
                                            imageExtensionRegex.containsMatchIn(initDigitalPath.trim()) &&
                                            !isSavingInitImages
                                    Button(
                                        enabled = canSave,
                                        onClick = {
                                            if (isSavingInitImages) return@Button
                                            val inspId = currentInspection?.idInspeccion
                                            if (inspId.isNullOrBlank()) {
                                                Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            scope.launch {
                                                isSavingInitImages = true
                                                val result = withContext(Dispatchers.IO) {
                                                    runCatching {
                                                        val dao = inspeccionDao
                                                        val existing = dao.getById(inspId)
                                                        if (existing != null) {
                                                            val updated = existing.copy(
                                                                irImagenInicial = initThermalPath.trim(),
                                                                digImagenInicial = initDigitalPath.trim()
                                                            )
                                                            dao.update(updated)
                                                            true
                                                        } else false
                                                    }.getOrDefault(false)
                                                }
                                                isSavingInitImages = false
                                                if (result) {
                                                    Toast.makeText(appContext, "Imágenes iniciales actualizadas.", Toast.LENGTH_SHORT).show()
                                                    showInitImagesDialog = false
                                                } else {
                                                    Toast.makeText(appContext, "No se pudo actualizar la inspección.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Guardar")
                                    }
                                }
                            }
                        }
                    }
                }
                if (showInitBarcodeDialog) {
                    Dialog(
                        onDismissRequest = { /* bloqueo: solo botones cierran */ },
                        properties = DialogProperties(
                            dismissOnClickOutside = false,
                            dismissOnBackPress = false
                        )
                    ) {
                        Card(
                            colors = CardDefaults.cardColors()
                        ) {
                            Column(
                                modifier = Modifier
                                    .widthIn(max = 470.dp)
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = "Inicializar codigo de barras")
                                SequenceInputButtonGroup(
                                    label = "Código de barras inicial",
                                    value = initBarcodeValue,
                                    onValueChange = { initBarcodeValue = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isRequired = true,
                                    onMoveUp = { initBarcodeValue = adjustBarcodeSequence(initBarcodeValue, +1) },
                                    onMoveDown = { initBarcodeValue = adjustBarcodeSequence(initBarcodeValue, -1) },
                                    onCurrentValueClick = { loadInitialBarcodeFromDb { initBarcodeValue = it } }
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    TextButton(
                                        enabled = !isSavingInitBarcode,
                                        onClick = {
                                            if (!isSavingInitBarcode) {
                                                showInitBarcodeDialog = false
                                            }
                                        }
                                    ) {
                                        Text("Cancelar")
                                    }
                                    val barcodeTrimmed = initBarcodeValue.trim()
                                    val canSave = isBarcodeIncrementable(barcodeTrimmed) && !isSavingInitBarcode
                                    Button(
                                        enabled = canSave,
                                        onClick = {
                                            if (isSavingInitBarcode) return@Button
                                            val inspId = currentInspection?.idInspeccion
                                            if (inspId.isNullOrBlank()) {
                                                Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            val valueToSave = barcodeTrimmed
                                            scope.launch {
                                                isSavingInitBarcode = true
                                                val result = withContext(Dispatchers.IO) {
                                                    runCatching {
                                                        inspeccionDao.updateInitialBarcode(inspId, valueToSave)
                                                    }.isSuccess
                                                }
                                                isSavingInitBarcode = false
                                                if (result) {
                                                    Toast.makeText(appContext, "Código de barras inicial actualizado.", Toast.LENGTH_SHORT).show()
                                                    showInitBarcodeDialog = false
                                                } else {
                                                    Toast.makeText(appContext, "No se pudo actualizar el codigo de barras inicial.", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Text("Guardar")
                                    }
                                }
                            }
                        }
                    }
                }

                if (showInspectionStatusDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!isSavingInspectionStatus) {
                                showInspectionStatusDialog = false
                            }
                        },
                        title = { Text("Estatus de la inspección") },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (inspectionStatusOptions.isEmpty()) {
                                    Text("No hay estatus disponibles.")
                                } else {
                                    inspectionStatusOptions.forEach { option ->
                                        val optionId = option.idStatusInspeccion
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable(enabled = !isSavingInspectionStatus) {
                                                    selectedInspectionStatusId = optionId
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = selectedInspectionStatusId == optionId,
                                                onClick = { selectedInspectionStatusId = optionId },
                                                enabled = !isSavingInspectionStatus
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Column {
                                                Text(option.statusInspeccion.orEmpty().ifBlank { optionId })
                                                inspectionStatusDescription(optionId)?.let { description ->
                                                    Text(description, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !isSavingInspectionStatus,
                                onClick = { showInspectionStatusDialog = false }
                            ) {
                                Text("Cancelar")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !isSavingInspectionStatus &&
                                    !selectedInspectionStatusId.isNullOrBlank() &&
                                    currentInspectionSnapshot != null,
                                onClick = {
                                    val inspection = currentInspectionSnapshot
                                    val statusId = selectedInspectionStatusId
                                    if (inspection == null || statusId.isNullOrBlank()) {
                                        Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }
                                    if (statusId == INSPECTION_STATUS_CERRADA) {
                                        showCloseInspectionConfirmDialog = true
                                    } else {
                                        saveInspectionStatus(inspection, statusId)
                                    }
                                }
                            ) {
                                Text("Guardar")
                            }
                        }
                    )
                }

                if (showCloseInspectionConfirmDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!isSavingInspectionStatus) {
                                showCloseInspectionConfirmDialog = false
                            }
                        },
                        title = { Text("Confirmar cierre de inspección") },
                        text = {
                            Text(
                                "La inspección actual se cerrará y se exportará en Descargas. ¿Deseas continuar?"
                            )
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !isSavingInspectionStatus,
                                onClick = { showCloseInspectionConfirmDialog = false }
                            ) {
                                Text("Cancelar")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !isSavingInspectionStatus,
                                onClick = {
                                    val inspection = currentInspectionSnapshot
                                    val statusId = selectedInspectionStatusId
                                    if (inspection == null || statusId.isNullOrBlank()) {
                                        Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }
                                    saveInspectionStatus(inspection, statusId)
                                }
                            ) {
                                Text("Continuar")
                            }
                        }
                    )
                }

                if (showImportInspectionAfterCloseDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportInspectionAfterCloseDialog = false },
                        title = { Text("Seleccionar Base de Datos") },
                        text = {
                            Text("Seleccionar archivo de la inspección a realizar.")
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showImportInspectionAfterCloseDialog = false }
                            ) { Text("Cancelar") }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showImportInspectionAfterCloseDialog = false
                                    importInspectionLauncher.launch(
                                        arrayOf(
                                            "application/octet-stream",
                                            "application/x-sqlite3",
                                            "*/*"
                                        )
                                    )
                                }
                            ) { Text("Procesar") }
                        }
                    )
                }

                if (showInventoryReportDialog) {
                    AlertDialog(
                        onDismissRequest = { if (!isGeneratingReport) showInventoryReportDialog = false },
                        title = { Text("Seleccionar elementos para reporte") },
                        text = {
                            Column(Modifier.fillMaxWidth()) {
                                when {
                                    isLoadingInventoryOptions -> Text("Cargando elementos...")
                                    inventoryOptions.isEmpty() -> Text("No hay elementos para seleccionar.")
                                    else -> {
                                        val selectedCount = inventorySelection.values.count { it }
                                        val totalCount = inventorySelection.size
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Seleccionados: $selectedCount / $totalCount")
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                TextButton(
                                                    onClick = {
                                                        inventorySelection =
                                                            inventoryOptions.associate { it.id to true }
                                                    }
                                                ) { Text("Seleccionar todo") }
                                                TextButton(
                                                    onClick = {
                                                        inventorySelection =
                                                            inventoryOptions.associate { it.id to false }
                                                    }
                                                ) { Text("Limpiar") }
                                            }
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        val listState = rememberScrollState()
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 520.dp)
                                                .verticalScroll(listState)
                                        ) {
                                            inventoryOptions.forEach { node ->
                                                val checked = inventorySelection[node.id] == true
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            inventorySelection =
                                                                inventorySelection + (node.id to !checked)
                                                        }
                                                        .padding(vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = checked,
                                                        onCheckedChange = { isChecked ->
                                                            inventorySelection =
                                                                inventorySelection + (node.id to isChecked)
                                                        },
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        node.title,
                                                        style = MaterialTheme.typography.bodySmall.merge(
                                                            TextStyle(lineHeight = 12.sp)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        dismissButton = {
                            TextButton(
                                enabled = !isGeneratingReport,
                                onClick = { showInventoryReportDialog = false }
                            ) { Text("Cancelar") }
                        },
                        confirmButton = {
                            TextButton(
                                enabled = !isGeneratingReport && inventorySelection.values.any { it },
                                onClick = {
                                    val insp = currentInspectionSnapshot
                                    if (insp == null) {
                                        Toast.makeText(
                                            appContext,
                                            "No hay inspección activa.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@TextButton
                                    }
                                    val selectedIds = mutableListOf<String>()
                                    inventoryOptions.forEach { node ->
                                        if (inventorySelection[node.id] == true) {
                                            collectNodeIds(node, selectedIds)
                                        }
                                    }
                                    showInventoryReportDialog = false
                                    generateInventarioPdf(insp, selectedIds)
                                }
                            ) { Text("Generar PDF") }
                        }
                    )
                }

                if (showResultsAnalysisDialog && resultsAnalysisDraft != null) {
                    ResultsAnalysisDialog(
                        initialDraft = resultsAnalysisDraft!!,
                        locationOptions = resultsLocationOptions,
                        problemOptions = resultsProblemOptions,
                        availableImages = resultsAvailableImages,
                        availableClientImages = resultsAvailableClientImages,
                        rootTreeUri = rootTreeUri,
                        inspectionNumber = currentInspectionSnapshot?.noInspeccion?.toString(),
                        isBusy = isGeneratingReport,
                        onDismiss = { draft ->
                            resultsAnalysisDraft = draft
                            showResultsAnalysisDialog = false
                            saveResultsAnalysisDraft(draft)
                        },
                        onConfirm = { draft, selectedInventoryIds ->
                            resultsAnalysisDraft = draft
                            val insp = currentInspectionSnapshot
                            if (insp == null) {
                                Toast.makeText(appContext, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                            } else {
                                val expandedIds = mutableListOf<String>()
                                resultsLocationOptions.forEach { node ->
                                    if (selectedInventoryIds.contains(node.id)) {
                                        collectNodeIds(node, expandedIds)
                                    }
                                }
                                generateResultadosAnalisis(
                                    insp = insp,
                                    draft = draft,
                                    selectedInventoryIds = expandedIds.distinct()
                                )
                            }
                        }
                    )
                }

            } // ProvideCurrentInspection
        } // ProvideCurrentUser
    }
}



// ------------------------------------------------------------
//  LOADING OVERLAY (fuera de MainScreen)
// ------------------------------------------------------------

@Composable
fun LoadingOverlay(message: String) {
    val transition = rememberInfiniteTransition(label = "loading")

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    val dot1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 0),
            RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 150),
            RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(900, delayMillis = 300),
            RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF202327),
                contentColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.etic_logo_login),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .rotate(angle)
                )

                Spacer(Modifier.height(16.dp))
                Text(message)

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot1), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot2), CircleShape)
                    )
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.White.copy(alpha = dot3), CircleShape)
                    )
                }
            }
        }
    }
}

private data class ImageNameParts(
    val prefix: String,
    val number: Long,
    val digits: Int,
    val suffix: String
)

private fun parseImageName(raw: String): ImageNameParts {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ImageNameParts("", 0, 0, "")
    val regex = Regex("^(.*?)(\\d+)(.*)$")
    val match = regex.find(trimmed)
    return if (match != null) {
        val prefix = match.groupValues[1]
        val digitsStr = match.groupValues[2]
        val suffix = match.groupValues[3]
        val number = digitsStr.toLongOrNull() ?: 0L
        ImageNameParts(prefix, number, digitsStr.length, suffix)
    } else {
        ImageNameParts(trimmed, 0, 0, "")
    }
}

private fun composeImageName(parts: ImageNameParts): String {
    val safeNumber = parts.number.coerceAtLeast(0)
    val formattedNumber = when {
        parts.digits > 0 -> safeNumber.toString().padStart(parts.digits, '0')
        safeNumber > 0 -> safeNumber.toString()
        else -> ""
    }
    return buildString {
        append(parts.prefix)
        append(formattedNumber)
        append(parts.suffix)
    }
}

private fun adjustImageSequence(current: String, delta: Int): String {
    if (delta == 0) return current
    val parts = parseImageName(current)
    if (parts.digits == 0 && parts.number == 0L) return current
    val newNumber = (parts.number + delta.toLong()).coerceAtLeast(0)
    val fallbackDigits = max(
        1,
        max(parts.number.toString().length, newNumber.toString().length)
    )
    val digits = if (parts.digits > 0) parts.digits else fallbackDigits
    return composeImageName(parts.copy(number = newNumber, digits = digits))
}

private fun adjustBarcodeSequence(current: String, delta: Int): String {
    if (delta == 0) return current
    val value = current.trim()
    val parts = BarcodeSuffix.parse(value) ?: return current
    val next = (parts.number + delta.toLong()).coerceAtLeast(0L)
    val nextDigits = max(parts.number.toString().length, next.toString().length).coerceAtLeast(parts.digits)
    val nextFormatted = next.toString().padStart(nextDigits, '0')
    return parts.prefix + nextFormatted
}

private fun isBarcodeIncrementable(value: String): Boolean = BarcodeSuffix.parse(value) != null

private data class BarcodeSuffix(
    val prefix: String,
    val number: Long,
    val digits: Int
) {
    companion object {
        fun parse(raw: String): BarcodeSuffix? {
            val trimmed = raw.trim()
            if (trimmed.isBlank()) return null
            val match = Regex("^(.*?)(\\d+)$").find(trimmed) ?: return null
            val numberText = match.groupValues[2]
            val number = numberText.toLongOrNull() ?: return null
            return BarcodeSuffix(
                prefix = match.groupValues[1],
                number = number,
                digits = numberText.length
            )
        }
    }
}

