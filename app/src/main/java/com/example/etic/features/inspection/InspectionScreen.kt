package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.etic.ui.theme.EticTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.etic.data.local.entities.EstatusInspeccionDet
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.LocalCurrentUser
import com.example.etic.core.saf.EticImageStore
import com.example.etic.core.settings.EticPrefs
import com.example.etic.core.settings.settingsDataStore
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.LinkedHashSet
import kotlin.collections.buildList
import kotlin.math.max
import kotlin.math.roundToInt
import com.example.etic.features.inspection.data.InspectionRepository
import com.example.etic.features.inspection.data.UbicacionSaveContext
import com.example.etic.features.inspection.ui.components.FILTER_FIELD_ROW_SPACING
import com.example.etic.features.inspection.ui.components.FilterDropdownField
import com.example.etic.features.inspection.ui.components.FilterableSelector
import com.example.etic.features.inspection.ui.components.InspectionHeader
import com.example.etic.features.inspection.ui.components.NewLocationDialog
import com.example.etic.features.inspection.ui.components.DropdownSelector
import com.example.etic.features.inspection.ui.components.LabeledInputField
import com.example.etic.features.inspection.ui.state.rememberLocationFormState
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.features.inspection.logic.VisualProblemEditor
import com.example.etic.features.inspection.tree.Baseline
import com.example.etic.features.inspection.tree.Problem
import com.example.etic.features.inspection.tree.TreeNode
import com.example.etic.features.inspection.tree.buildTreeFromVista
import com.example.etic.features.inspection.tree.collectBaselines
import com.example.etic.ui.inspection.tabs.BaselineTableFromDatabase
import com.example.etic.ui.inspection.tabs.ProblemsTableFromDatabase
import com.example.etic.features.inspection.tree.collectProblems
import com.example.etic.features.inspection.tree.depthOfId
import com.example.etic.features.inspection.tree.descendantIds
import com.example.etic.features.inspection.tree.findById
import com.example.etic.features.inspection.tree.findPathByBarcode
import com.example.etic.features.inspection.tree.titlePathForId
import com.example.etic.features.inspection.ui.problem.ElectricProblemDialog
import com.example.etic.features.inspection.ui.problem.ElectricProblemFormData
import com.example.etic.features.inspection.ui.problem.VisualProblemDialog
import com.example.etic.features.inspection.ui.problem.MechanicalProblemDialog
import com.example.etic.features.inspection.ui.problem.MechanicalProblemFormData
import com.example.etic.features.inspection.ui.problem.AislamientoTermicoProblemDialog
import com.example.etic.features.inspection.ui.problem.AislamientoTermicoProblemFormData
import com.example.etic.data.local.entities.Severidad
import com.example.etic.data.local.entities.Problema
import com.example.etic.data.local.dao.VisualProblemHistoryRow
import androidx.compose.ui.window.Dialog
import java.text.Normalizer
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp

// Centralizamos algunos "magic numbers" para facilitar ajuste futuro
private const val MIN_FRAC: Float = 0.2f     // Limite inferior de los splitters
private const val MAX_FRAC: Float = 0.8f     // Limite superior de los splitters
private const val H_INIT_FRAC: Float = 0.6f  // Fraccion inicial del panel superior
private const val V_INIT_FRAC: Float = 0.5f  // Fraccion inicial del panel izquierdo

private val HANDLE_THICKNESS: Dp = 2.dp      // Grosor de los handles de split
private val DIVIDER_THICKNESS: Dp = 0.5.dp   // Grosor estandar de divisores/bordes
private val PANEL_PADDING: Dp = 12.dp        // Padding interno de cada panel
private const val SURFACE_VARIANT_ALPHA: Float = 0.4f
private const val SELECT_ALPHA: Float = 0.10f
private val ICON_EQUIPO_COLOR: Color = Color(0xFFFFC107)     // Amarillo (Traffic)
private val ICON_NO_EQUIPO_COLOR: Color = Color(0xFF4CAF50)  // Verde (DragIndicator)
private val DIALOG_HEADER_TURQUOISE: Color = Color(0xFF159BA6)
private val PROBLEM_TYPE_IDS = mapOf(
    "Eléctrico" to "0D32B331-76C3-11D3-82BF-00104BC75DC2",
    "Visual" to "0D32B333-76C3-11D3-82BF-00104BC75DC2",
    "Mecánico" to "0D32B334-76C3-11D3-82BF-00104BC75DC2",
    "Aislamiento Térmico" to "0D32B335-76C3-11D3-82BF-00104BC75DC2"
)

private const val PROBLEM_STATUS_ALL = "0"
private const val PROBLEM_STATUS_OPEN_CURRENT = "1"
private const val PROBLEM_STATUS_OPEN_PAST = "2"
private const val PROBLEM_STATUS_OPEN_ALL = "3"
private const val PROBLEM_STATUS_CLOSED = "4"
private const val DEFAULT_PRIORIDAD_ID = "6F5F0EB1-76B8-11D3-82BF-00104BC75DC2"
internal const val STATUS_POR_VERIFICAR = "568798D1-76BB-11D3-82BF-00104BC75DC2"
internal const val STATUS_VERIFICADO = "568798D2-76BB-11D3-82BF-00104BC75DC2"

private data class ProblemTypeFilter(val id: String, val label: String, val matchIds: List<String>)

private sealed class ProblemDraft {
    data class VisualDraft(
        val hazardId: String?,
        val hazardLabel: String?,
        val severityId: String?,
        val severityLabel: String?,
        val observation: String,
        val thermalImage: String,
        val digitalImage: String,
        val closed: Boolean
    ) : ProblemDraft()

    data class ElectricDraft(
        val formData: ElectricProblemFormData,
        val thermalImage: String,
        val digitalImage: String,
        val closed: Boolean
    ) : ProblemDraft()

    data class MechanicalDraft(
        val formData: MechanicalProblemFormData,
        val thermalImage: String,
        val digitalImage: String,
        val closed: Boolean
    ) : ProblemDraft()

    data class AislamientoTermicoDraft(
        val formData: AislamientoTermicoProblemFormData,
        val thermalImage: String,
        val digitalImage: String,
        val closed: Boolean
    ) : ProblemDraft()
}

private val PROBLEM_TYPE_FILTERS = listOf(
    ProblemTypeFilter("", "Todos", emptyList()),
    ProblemTypeFilter(
        "electrico",
        "Eléctrico",
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
        "Mecánico",
        listOf("0D32B334-76C3-11D3-82BF-00104BC75DC2")
    ),
    ProblemTypeFilter(
        "aislamiento",
        "Aislamiento Térmico",
        listOf("0D32B335-76C3-11D3-82BF-00104BC75DC2")
    )
)

private val PROBLEM_TYPE_FILTER_OPTIONS = PROBLEM_TYPE_FILTERS.map { it.id to it.label }

private val PROBLEM_STATUS_FILTER_OPTIONS = listOf(
    PROBLEM_STATUS_ALL to "Todos",
    PROBLEM_STATUS_OPEN_CURRENT to "Abiertos, actuales",
    PROBLEM_STATUS_OPEN_PAST to "Abiertos, pasado",
    PROBLEM_STATUS_OPEN_ALL to "Abiertos, todos",
    PROBLEM_STATUS_CLOSED to "Cerrados"
)


private val VISUAL_PROBLEM_TYPE_ID = PROBLEM_TYPE_IDS["Visual"]
private val ELECTRIC_PROBLEM_TYPE_ID = PROBLEM_TYPE_IDS["Eléctrico"]
private val PROBLEM_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

private enum class ProblemColumn {
    NUMERO, FECHA, INSPECCION, TIPO, ESTATUS, CRONICO, TEMP_C, DELTA_T, SEVERIDAD, EQUIPO, COMENTARIOS
}

private enum class BaselineColumn {
    INSPECCION, EQUIPO, FECHA, MTA, TEMP, AMB, IR, ID, NOTAS
}
private val MECHANICAL_PROBLEM_TYPE_ID = PROBLEM_TYPE_IDS["Mecánico"]
private val AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID = PROBLEM_TYPE_IDS["Aislamiento Térmico"]

private fun problemTypeLabelForId(typeId: String?): String {
    val fallback = PROBLEM_TYPE_IDS.entries.firstOrNull { it.key.contains("Visual", ignoreCase = true) }
        ?.key ?: PROBLEM_TYPE_IDS.keys.firstOrNull().orEmpty()
    if (typeId.isNullOrBlank()) return fallback
    val rawLabel = PROBLEM_TYPE_IDS.entries.firstOrNull { it.value.equals(typeId, ignoreCase = true) }?.key ?: fallback
    return rawLabel.canonicalProblemLabel()
}

private fun String.normalizeProblemKey(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
    return normalized.replace(Regex("\\p{Mn}+"), "").lowercase(Locale.ROOT)
}

private fun problemTypeIdFromLabel(label: String?): String? {
    if (label.isNullOrBlank()) return null
    val normalized = label.normalizeProblemKey()
    return PROBLEM_TYPE_IDS.entries.firstOrNull { it.key.normalizeProblemKey() == normalized }?.value
}

private fun String.canonicalProblemLabel(): String =
    when (this.normalizeProblemKey()) {
        "electrico" -> "Eléctrico"
        "mecanico" -> "Mecánico"
        "aislamiento termico" -> "Aislamiento Térmico"
        else -> this
    }
private val VISUAL_SEVERITY_OPTIONS = listOf(
    "1D56EDB0-8D6E-11D3-9270-006008A19766" to "Crítico",
    "1D56EDB1-8D6E-11D3-9270-006008A19766" to "Serio",
    "1D56EDB2-8D6E-11D3-9270-006008A19766" to "Importante",
    "1D56EDB3-8D6E-11D3-9270-006008A19766" to "Menor",
    "1D56EDB4-8D6E-11D3-9270-006008A19766" to "Normal"
)

// Ajustes de compacidad para filas del arbol
private val TREE_TOGGLE_SIZE: Dp = 20.dp   // Tamano del icono de expandir/colapsar
private val TREE_ICON_SIZE: Dp = 18.dp     // Tamano del icono del nodo
private val TREE_SPACING: Dp = 4.dp        // Espaciado horizontal pequeno
private val TREE_INDENT: Dp = 12.dp        // Indentacion por nivel

// Nota: la tabla de Progreso ocupa siempre todo el ancho del panel

@Composable
fun InspectionScreen(onReady: () -> Unit = {}) {
    CurrentInspectionSplitView(onReady = onReady)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CurrentInspectionSplitView(onReady: () -> Unit = {}) {
    // Usamos constantes para evitar numero magicos in-line
    var hFrac by rememberSaveable { mutableStateOf(H_INIT_FRAC) } // Fraccion alto del panel superior
    var vFrac by rememberSaveable { mutableStateOf(V_INIT_FRAC) } // Fraccion ancho del panel izquierdo

    var nodes by remember { mutableStateOf<List<TreeNode>>(emptyList()) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val eticPrefs = remember { EticPrefs(ctx.settingsDataStore) }
    val rootTreeUriStr by eticPrefs.rootTreeUriFlow.collectAsState(initial = null)
    val rootTreeUri = remember(rootTreeUriStr) { rootTreeUriStr?.let { Uri.parse(it) } }
    val keyboardController = LocalSoftwareKeyboardController.current
    val ubicacionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val vistaUbicacionArbolDao = remember { com.example.etic.data.local.DbProvider.get(ctx).vistaUbicacionArbolDao() }
    val usuarioDao = remember { com.example.etic.data.local.DbProvider.get(ctx).usuarioDao() }
    val inspeccionDetDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDetDao() }
    val inspeccionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDao() }
    val inspectionRepository = remember {
        InspectionRepository(ubicacionDao, inspeccionDetDao, vistaUbicacionArbolDao)
    }
    val problemaDao = remember { com.example.etic.data.local.DbProvider.get(ctx).problemaDao() }
    val lineaBaseDaoGlobal = remember { com.example.etic.data.local.DbProvider.get(ctx).lineaBaseDao() }
    val fallaDao = remember { com.example.etic.data.local.DbProvider.get(ctx).fallaDao() }
    val severidadDao = remember { com.example.etic.data.local.DbProvider.get(ctx).severidadDao() }
    val estatusDao = remember { com.example.etic.data.local.DbProvider.get(ctx).estatusInspeccionDetDao() }
    val prioridadDao = remember { com.example.etic.data.local.DbProvider.get(ctx).tipoPrioridadDao() }
    val fabricanteDao = remember { com.example.etic.data.local.DbProvider.get(ctx).fabricanteDao() }
    val faseDao = remember { com.example.etic.data.local.DbProvider.get(ctx).faseDao() }
    val tipoAmbienteDao = remember { com.example.etic.data.local.DbProvider.get(ctx).tipoAmbienteDao() }
    val currentInspection = LocalCurrentInspection.current
    val inspectionNumero = currentInspection?.noInspeccion?.toString()
    val rootTitle = currentInspection?.nombreSitio ?: "Sitio"
    val rootId = remember(currentInspection?.idSitio) { (currentInspection?.idSitio?.let { "root:$it" } ?: "root:site") }
    val expanded = remember { mutableStateListOf<String>() }
        // Centralizar seleccion como si fuera un tap del usuario
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var highlightedId by remember { mutableStateOf<String?>(null) }
    var scrollToNodeId by remember { mutableStateOf<String?>(null) }
    var baselineRefreshTick by remember { mutableStateOf(0) }
    var problemsRefreshTick by remember { mutableStateOf(0) }
    var hasSignaledReady by rememberSaveable { mutableStateOf(false) }
    var severityCatalog by remember { mutableStateOf<List<Severidad>>(emptyList()) }
    var hazardOptionsByType by remember {
        mutableStateOf<Map<String, List<Pair<String, String>>>>(emptyMap())
    }
    var statusOptionsCache by remember { mutableStateOf<List<EstatusInspeccionDet>>(emptyList()) }
    var prioridadOptionsCache by remember { mutableStateOf<List<com.example.etic.data.local.entities.TipoPrioridad>>(emptyList()) }
    var fabricanteOptionsCache by remember { mutableStateOf<List<com.example.etic.data.local.entities.Fabricante>>(emptyList()) }
    var electricPhaseOptionsCache by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var electricEnvironmentOptionsCache by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val treeScope = rememberCoroutineScope()
    val onSelectNode: (String) -> Unit = { id ->
        selectedId = id
        val inspId = currentInspection?.idInspeccion
        if (!inspId.isNullOrBlank() && !id.startsWith("root:")) {
            treeScope.launch {
                runCatching {
                    inspeccionDetDao.updateSelectedByUbicacion(inspId, id)
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        val visualTypeId = PROBLEM_TYPE_IDS["Visual"]
        val electricTypeId = ELECTRIC_PROBLEM_TYPE_ID
        val mechanicalTypeId = MECHANICAL_PROBLEM_TYPE_ID
        val aislamientoTermicoTypeId = AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID
        val typesToLoad = listOfNotNull(visualTypeId, electricTypeId, mechanicalTypeId, aislamientoTermicoTypeId)

        coroutineScope {
            val hazardsDeferred = async(Dispatchers.IO) {
                val hazardsByType = mutableMapOf<String, List<Pair<String, String>>>()
                typesToLoad.forEach { typeId ->
                    val options = runCatching { fallaDao.getByTipoInspeccion(typeId) }
                        .getOrElse { emptyList() }
                        .asSequence()
                        .map { it.idFalla to (it.falla ?: it.idFalla) }
                        .sortedBy { it.second.lowercase() }
                        .toList()
                    hazardsByType[typeId] = options
                }
                hazardsByType
            }
            val statusDeferred = async { runCatching { estatusDao.getAll() }.getOrElse { emptyList() } }
            val prioridadDeferred = async { runCatching { prioridadDao.getAllActivas() }.getOrElse { emptyList() } }
            val fabricanteDeferred = async {
                runCatching { fabricanteDao.getAllActivos() }.getOrElse { emptyList() }
                    .sortedBy { it.fabricante?.lowercase(Locale.getDefault()) ?: "" }
            }
            val phaseDeferred = async(Dispatchers.IO) {
                runCatching { faseDao.getAllActivos() }.getOrElse { emptyList() }
                    .sortedBy { it.nombreFase?.lowercase(Locale.getDefault()) ?: "" }
                    .map { it.idFase to (it.nombreFase ?: it.idFase) }
            }
            val environmentDeferred = async(Dispatchers.IO) {
                runCatching { tipoAmbienteDao.getAllActivos() }.getOrElse { emptyList() }
                    .sortedBy { it.nombre?.lowercase(Locale.getDefault()) ?: "" }
                    .map { it.idTipoAmbiente to (it.nombre ?: it.idTipoAmbiente) }
            }
            val severityDeferred = async(Dispatchers.IO) {
                runCatching { severidadDao.getAll() }.getOrElse { emptyList() }
            }

            val hazardsByType = hazardsDeferred.await()
            hazardOptionsByType = hazardsByType
            statusOptionsCache = statusDeferred.await()
            prioridadOptionsCache = prioridadDeferred.await()
            fabricanteOptionsCache = fabricanteDeferred.await()
            electricPhaseOptionsCache = phaseDeferred.await()
            electricEnvironmentOptionsCache = environmentDeferred.await()
            severityCatalog = severityDeferred.await()
        }
    }
    // Reconstruir el arbol cuando llegue/ cambie la Inspeccion actual
    LaunchedEffect(rootId, rootTitle, currentInspection?.idInspeccion) {
        val rowsVista = try {
            withContext(Dispatchers.IO) { vistaUbicacionArbolDao.getAll() }
        } catch (e: Exception) {
            android.util.Log.e("VistaUbicacionArbol", "Error al cargar vista_ubicaciones_arbol", e)
            emptyList()
        }
        android.util.Log.d("VistaUbicacionArbol", "rowsVista.size = ${rowsVista.size}")

        val roots = buildTreeFromVista(rowsVista)
        val siteRoot = TreeNode(id = rootId, title = rootTitle)
        siteRoot.children.addAll(roots)
        nodes = listOf(siteRoot)
        if (!expanded.contains(rootId)) expanded.add(rootId)
        onSelectNode(rootId)
        // seleccion programatica equivalente a un tap sobre el sitio
        kotlinx.coroutines.delay(0)
        onSelectNode(rootId)
        // Seleccionar por defecto el nodo padre (sitio)
        if (selectedId == null) selectedId = rootId
        val defaultExpanded = rowsVista.filter { it.expanded == "1" }.map { it.idUbicacion }
        defaultExpanded.forEach { if (!expanded.contains(it)) expanded.add(it) }
        rowsVista.firstOrNull { it.selected == "1" }?.idUbicacion?.let { onSelectNode(it) }
        if (!hasSignaledReady) { hasSignaledReady = true; onReady() }
    }

    // Refrescar arbol cuando cambie el baseline (para actualizar colores)
    LaunchedEffect(baselineRefreshTick, currentInspection?.idInspeccion, rootId, rootTitle) {
        if (baselineRefreshTick == 0) return@LaunchedEffect
        val rowsVista = try {
            withContext(Dispatchers.IO) { vistaUbicacionArbolDao.getAll() }
        } catch (_: Exception) {
            emptyList()
        }
        val roots = buildTreeFromVista(rowsVista)
        val siteRoot = TreeNode(id = rootId, title = rootTitle)
        siteRoot.children.addAll(roots)
        nodes = listOf(siteRoot)
        if (!expanded.contains(rootId)) expanded.add(rootId)
        val currentSelection = selectedId ?: rootId
        onSelectNode(currentSelection)
    }

    // selectedId y highlightedId declarados antes para usarlos en LaunchedEffect

    val borderColor = DividerDefaults.color

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Convierte dimensiones del BoxWithConstraints a pixeles de forma segura
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.value * density.density }
        val totalHeightPx = with(density) { maxHeight.value * density.density }

        Column(Modifier.fillMaxSize()) {

            // Controles superiores (fuera de los paneles)
            var barcode by rememberSaveable { mutableStateOf("") }
            var selectedStatusId by rememberSaveable { mutableStateOf<String?>(null) }
            val checkedStatusLocationIds = remember { mutableStateListOf<String>() }
            var statusOptions by remember { mutableStateOf<List<EstatusInspeccionDet>>(emptyList()) }
            LaunchedEffect(statusOptionsCache) {
                statusOptions = statusOptionsCache
            }
            // Tipo de prioridad, fabricantes y catálogos para problema eléctrico
            var prioridadOptions by remember { mutableStateOf<List<com.example.etic.data.local.entities.TipoPrioridad>>(emptyList()) }
            var fabricanteOptions by remember { mutableStateOf<List<com.example.etic.data.local.entities.Fabricante>>(emptyList()) }
            var electricPhaseOptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            var electricEnvironmentOptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            var electricHazardOptions by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
            LaunchedEffect(
                prioridadOptionsCache,
                fabricanteOptionsCache,
                electricPhaseOptionsCache,
                electricEnvironmentOptionsCache,
                hazardOptionsByType
            ) {
                val electricTypeId = ELECTRIC_PROBLEM_TYPE_ID
                prioridadOptions = prioridadOptionsCache
                fabricanteOptions = fabricanteOptionsCache
                electricPhaseOptions = electricPhaseOptionsCache
                electricEnvironmentOptions = electricEnvironmentOptionsCache
                electricHazardOptions = if (electricTypeId.isNullOrBlank()) {
                    emptyList()
                } else {
                    hazardOptionsByType[electricTypeId].orEmpty()
                }
            }
            var searchMessage by remember { mutableStateOf<String?>(null) }
            var showNoSelectionDialog by rememberSaveable { mutableStateOf(false) }
            var showProblemTypeDialog by rememberSaveable { mutableStateOf(false) }
            var showBaselineRestrictionDialog by rememberSaveable { mutableStateOf(false) }
            var showVisualInspectionWarning by rememberSaveable { mutableStateOf(false) }
            var showVisualInspectionDialog by rememberSaveable { mutableStateOf(false) }
            var showElectricProblemDialog by rememberSaveable { mutableStateOf(false) }
            var electricProblemFormKey by rememberSaveable { mutableStateOf(0) }
            var showMechanicalProblemDialog by rememberSaveable { mutableStateOf(false) }
            var mechanicalProblemFormKey by rememberSaveable { mutableStateOf(0) }
            var showAislamientoTermicoProblemDialog by rememberSaveable { mutableStateOf(false) }
            var aislamientoTermicoProblemFormKey by rememberSaveable { mutableStateOf(0) }
            var cronicoActionEnabled by rememberSaveable { mutableStateOf(false) }
            var showEditUbDialog by remember { mutableStateOf(false) }
            var isSavingEditUb by remember { mutableStateOf(false) }
            var editTab by rememberSaveable { mutableStateOf(0) }
            var problemDialogTab by rememberSaveable { mutableStateOf(0) }
            var pendingProblemEquipmentName by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingProblemRoute by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingProblemType by rememberSaveable { mutableStateOf(problemTypeLabelForId(VISUAL_PROBLEM_TYPE_ID)) }
            var pendingProblemNumber by rememberSaveable { mutableStateOf("Pendiente") }
            var pendingInspectionNumber by rememberSaveable { mutableStateOf("-") }
            var pendingHazardId by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingHazardLabel by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingSeverityId by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingSeverityLabel by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingProblemUbicacionId by rememberSaveable { mutableStateOf<String?>(null) }
            var pendingObservation by rememberSaveable { mutableStateOf("") }
            var pendingThermalImage by rememberSaveable { mutableStateOf("") }
            var pendingDigitalImage by rememberSaveable { mutableStateOf("") }
            var isSavingVisualProblem by remember { mutableStateOf(false) }
            var isSavingElectricProblem by remember { mutableStateOf(false) }
            var isSavingMechanicalProblem by remember { mutableStateOf(false) }
            var isSavingAislamientoTermicoProblem by remember { mutableStateOf(false) }
            var isSavingCronico by remember { mutableStateOf(false) }
            var showCronicoConfirmDialog by rememberSaveable { mutableStateOf(false) }
            var pendingCronicoEntity by remember { mutableStateOf<Problema?>(null) }
            var editingProblemId by rememberSaveable { mutableStateOf<String?>(null) }
            var editingProblemOriginal by remember { mutableStateOf<Problema?>(null) }
            var visualProblemClosed by rememberSaveable { mutableStateOf(false) }
            var problemNavList by remember { mutableStateOf<List<Problem>>(emptyList()) }
            var problemNavIndex by remember { mutableStateOf(-1) }
            var problemDrafts by remember { mutableStateOf<Map<String, ProblemDraft>>(emptyMap()) }
            var electricProblemDraftData by remember { mutableStateOf<ElectricProblemFormData?>(null) }
            var mechanicalProblemDraftData by remember { mutableStateOf<MechanicalProblemFormData?>(null) }
            var aislamientoTermicoProblemDraftData by remember { mutableStateOf<AislamientoTermicoProblemFormData?>(null) }
            fun resetVisualProblemForm() {
                editingProblemId = null
                editingProblemOriginal = null
                visualProblemClosed = false
                pendingProblemType = problemTypeLabelForId(VISUAL_PROBLEM_TYPE_ID)
                pendingHazardId = null
                pendingHazardLabel = null
                pendingSeverityId = null
                pendingSeverityLabel = null
                pendingObservation = ""
                pendingThermalImage = ""
                pendingDigitalImage = ""
                pendingProblemUbicacionId = null
                pendingProblemEquipmentName = null
                pendingProblemRoute = null
            }
            var editingElectricProblemId by rememberSaveable { mutableStateOf<String?>(null) }
            var editingElectricProblemOriginal by remember { mutableStateOf<Problema?>(null) }
            var electricProblemClosed by rememberSaveable { mutableStateOf(false) }
            fun resetElectricProblemState() {
                editingElectricProblemId = null
                editingElectricProblemOriginal = null
                electricProblemClosed = false
                pendingThermalImage = ""
                pendingDigitalImage = ""
                electricProblemDraftData = null
            }
            var editingMechanicalProblemId by rememberSaveable { mutableStateOf<String?>(null) }
            var editingMechanicalProblemOriginal by remember { mutableStateOf<Problema?>(null) }
            var mechanicalProblemClosed by rememberSaveable { mutableStateOf(false) }
            var editingAislamientoTermicoProblemId by rememberSaveable { mutableStateOf<String?>(null) }
            var editingAislamientoTermicoProblemOriginal by remember { mutableStateOf<Problema?>(null) }
            var aislamientoTermicoProblemClosed by rememberSaveable { mutableStateOf(false) }
            fun resetMechanicalProblemState() {
                editingMechanicalProblemId = null
                editingMechanicalProblemOriginal = null
                mechanicalProblemClosed = false
                pendingThermalImage = ""
                pendingDigitalImage = ""
                mechanicalProblemDraftData = null
                aislamientoTermicoProblemDraftData = null
            }
            fun resetAislamientoTermicoProblemState() {
                editingAislamientoTermicoProblemId = null
                editingAislamientoTermicoProblemOriginal = null
                aislamientoTermicoProblemClosed = false
                pendingThermalImage = ""
                pendingDigitalImage = ""
                aislamientoTermicoProblemDraftData = null
            }
            val thermalCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
                if (bmp != null) {
                    val saved = saveProblemBitmap(ctx, rootTreeUri, inspectionNumero, bmp, "IR")
                    if (saved != null) {
                        pendingThermalImage = saved
                    } else {
                        Toast.makeText(ctx, "No se pudo guardar la imagen térmica.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(ctx, "La cámara no devolvió imagen.", Toast.LENGTH_SHORT).show()
                }
            }
            val digitalCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp ->
                if (bmp != null) {
                    val saved = saveProblemBitmap(ctx, rootTreeUri, inspectionNumero, bmp, "ID")
                    if (saved != null) {
                        pendingDigitalImage = saved
                    } else {
                        Toast.makeText(ctx, "No se pudo guardar la imagen digital.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(ctx, "La cámara no devolvió imagen.", Toast.LENGTH_SHORT).show()
                }
            }
            val thermalFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    val saved = copyProblemImageFromUri(ctx, rootTreeUri, inspectionNumero, uri, "IR")
                    if (saved != null) {
                        pendingThermalImage = saved
                    } else {
                        Toast.makeText(ctx, "No se pudo importar la imagen térmica.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            val digitalFolderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    val saved = copyProblemImageFromUri(ctx, rootTreeUri, inspectionNumero, uri, "ID")
                    if (saved != null) {
                        pendingDigitalImage = saved
                    } else {
                        Toast.makeText(ctx, "No se pudo importar la imagen digital.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            LaunchedEffect(showEditUbDialog) {
                if (!showEditUbDialog) {
                    isSavingEditUb = false
                }
            }

            val visualSeverityOptions = remember(severityCatalog) {
                val map = severityCatalog.associateBy { it.idSeveridad }
                VISUAL_SEVERITY_OPTIONS.map { (id, fallback) ->
                    id to (map[id]?.severidad ?: fallback)
                }
            }
            val visualHazardOptionsFixed = remember(hazardOptionsByType) {
                val visualTypeId = VISUAL_PROBLEM_TYPE_ID
                if (visualTypeId.isNullOrBlank()) {
                    emptyList()
                } else {
                    hazardOptionsByType[visualTypeId].orEmpty()
                }
            }

            fun buildVisualObservation(hazardId: String?, equipmentName: String?): String {
                val hazardText = hazardId?.let { id ->
                    visualHazardOptionsFixed.firstOrNull { it.first == id }?.second
                }
                val equipment = equipmentName?.takeUnless { it.isBlank() }
                val parts = listOfNotNull(hazardText, equipment).filter { it.isNotBlank() }
                return parts.joinToString(", ").uppercase(Locale.getDefault())
            }

            fun ensureVisualDefaults(
                allowObservationUpdate: Boolean = true,
                allowUnknownSelections: Boolean = false
            ) {
                fun normalizePendingId(
                    pendingId: String?,
                    options: List<Pair<String, String>>
                ): String? {
                    if (pendingId.isNullOrBlank()) return null
                    return options.firstOrNull { it.first == pendingId }?.first
                        ?: options.firstOrNull { it.first.equals(pendingId, ignoreCase = true) }?.first
                        ?: options.firstOrNull { it.second.equals(pendingId, ignoreCase = true) }?.first
                }

                if (visualHazardOptionsFixed.isNotEmpty()) {
                    val normalized = normalizePendingId(pendingHazardId, visualHazardOptionsFixed)
                    pendingHazardId = if (normalized != null) normalized else if (allowUnknownSelections) pendingHazardId else null
                }
                if (visualSeverityOptions.isNotEmpty()) {
                    val normalized = normalizePendingId(pendingSeverityId, visualSeverityOptions)
                    pendingSeverityId = if (normalized != null) normalized else if (allowUnknownSelections) pendingSeverityId else null
                }
                if (allowObservationUpdate) {
                    pendingObservation = buildVisualObservation(pendingHazardId, pendingProblemEquipmentName)
                }
            }
            LaunchedEffect(visualHazardOptionsFixed, visualSeverityOptions, editingProblemId) {
                ensureVisualDefaults(
                    allowObservationUpdate = editingProblemId == null,
                    allowUnknownSelections = editingProblemId != null
                )
            }

            var visualProblemHistory by remember { mutableStateOf<List<VisualProblemHistoryRow>>(emptyList()) }
            var isHistoryLoading by remember { mutableStateOf(false) }
            val loadInitialImageScope = rememberCoroutineScope()
            val scope = rememberCoroutineScope()
            fun loadInitialImageFromInspection(isThermal: Boolean, onResult: (String) -> Unit) {
                val inspId = currentInspection?.idInspeccion
                if (inspId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                    return
                }
                loadInitialImageScope.launch {
                    val initial = withContext(Dispatchers.IO) {
                        runCatching { inspeccionDao.getById(inspId) }.getOrNull()?.let {
                            if (isThermal) it.irImagenInicial else it.digImagenInicial
                        }
                    }
                    if (!initial.isNullOrBlank()) {
                        val parts = parseImageName(initial)
                        val suggestion = if (parts.digits == 0 && parts.number == 0L) {
                            initial
                        } else {
                            composeImageName(parts.copy(number = parts.number + 1))
                        }
                        onResult(suggestion)
                    } else {
                        val label = if (isThermal) "térmica" else "digital"
                        Toast.makeText(ctx, "No se encontró imagen $label inicial.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            suspend fun updateInspectionInitialImages(irName: String?, digName: String?) {
                val inspId = currentInspection?.idInspeccion ?: return
                withContext(Dispatchers.IO) {
                    runCatching { inspeccionDao.updateInitialImages(inspId, irName, digName) }
                }
            }
            LaunchedEffect(showVisualInspectionDialog, pendingProblemUbicacionId, editingProblemId) {
                if (showVisualInspectionDialog) {
                    if (editingProblemId == null) {
                        pendingThermalImage = ""
                        pendingDigitalImage = ""
                    }
                    val ubicacionId = pendingProblemUbicacionId
                    val tipoId = PROBLEM_TYPE_IDS["Visual"]
                    if (!ubicacionId.isNullOrBlank() && tipoId != null) {
                        isHistoryLoading = true
                        visualProblemHistory = withContext(Dispatchers.IO) {
                            runCatching { problemaDao.getVisualHistoryFor(ubicacionId, tipoId) }
                                .getOrDefault(emptyList())
                        }
                        isHistoryLoading = false
                    } else {
                        visualProblemHistory = emptyList()
                        isHistoryLoading = false
                    }
                } else {
                    visualProblemHistory = emptyList()
                    isHistoryLoading = false
                }
            }


            suspend fun fetchNextProblemNumber(problemTypeId: String?): String {
                val inspId = currentInspection?.idInspeccion ?: return "1"
                val typeId = problemTypeId ?: return "1"
                val last = withContext(Dispatchers.IO) {
                    runCatching { problemaDao.getLastNumberByInspectionAndType(inspId, typeId) }
                        .getOrNull()
                }
                return ((last ?: 0) + 1).toString()
            }
            fun canEnableCronico(entity: Problema?): Boolean {
                val currentId = currentInspection?.idInspeccion
                if (currentId.isNullOrBlank() || entity == null) return false
                val isOpen = entity.estatusProblema?.equals("Abierto", ignoreCase = true) == true
                val isPast = entity.idInspeccion?.equals(currentId, ignoreCase = true) != true
                return isOpen && isPast
            }
            fun computePreviousInspectionNumber(
                currentNumber: Int?,
                visibleProblems: List<Problem>
            ): Int? {
                val numbers = buildList {
                    currentNumber?.let { add(it) }
                    visibleProblems.mapNotNull { it.numInspeccion.toIntOrNull() }.forEach { add(it) }
                }
                if (numbers.isEmpty()) return null
                val unique = numbers.distinct().sortedDescending()
                return if (unique.size > 1) unique[1] else unique[0]
            }

            fun buildNavigationList(
                visibleProblems: List<Problem>,
                inspectionPrevious: Int?
            ): List<Problem> {
                if (inspectionPrevious == null) return visibleProblems
                return visibleProblems.filter { problem ->
                    val num = problem.numInspeccion.toIntOrNull()
                    val isOpen = problem.estatus.equals("Abierto", ignoreCase = true)
                    (num != null && num >= inspectionPrevious) || isOpen
                }
            }

            fun shouldBlockProblemOpen(problem: Problem, inspectionPrevious: Int?): Boolean {
                if (inspectionPrevious == null) return false
                val isClosed = problem.estatus.equals("Cerrado", ignoreCase = true)
                val isDifferentInspection =
                    problem.inspectionId?.equals(currentInspection?.idInspeccion, ignoreCase = true) != true
                val num = problem.numInspeccion.toIntOrNull() ?: return false
                return isDifferentInspection && isClosed && num <= inspectionPrevious
            }

            fun resetProblemNavigation() {
                problemNavList = emptyList()
                problemNavIndex = -1
                problemDrafts = emptyMap()
                electricProblemDraftData = null
                mechanicalProblemDraftData = null
                aislamientoTermicoProblemDraftData = null
            }

            fun applyVisualDraft(problemId: String) {
                val draft = problemDrafts[problemId] as? ProblemDraft.VisualDraft ?: return
                pendingHazardId = draft.hazardId
                pendingHazardLabel = draft.hazardLabel
                pendingSeverityId = draft.severityId
                pendingSeverityLabel = draft.severityLabel
                pendingObservation = draft.observation
                pendingThermalImage = draft.thermalImage
                pendingDigitalImage = draft.digitalImage
                visualProblemClosed = draft.closed
                ensureVisualDefaults(allowObservationUpdate = false, allowUnknownSelections = true)
            }

            fun applyElectricDraft(problemId: String) {
                val draft = problemDrafts[problemId] as? ProblemDraft.ElectricDraft ?: return
                electricProblemDraftData = draft.formData
                pendingThermalImage = draft.thermalImage
                pendingDigitalImage = draft.digitalImage
                electricProblemClosed = draft.closed
            }

            fun applyMechanicalDraft(problemId: String) {
                val draft = problemDrafts[problemId] as? ProblemDraft.MechanicalDraft ?: return
                mechanicalProblemDraftData = draft.formData
                pendingThermalImage = draft.thermalImage
                pendingDigitalImage = draft.digitalImage
                mechanicalProblemClosed = draft.closed
            }

            fun applyAislamientoTermicoDraft(problemId: String) {
                val draft = problemDrafts[problemId] as? ProblemDraft.AislamientoTermicoDraft ?: return
                aislamientoTermicoProblemDraftData = draft.formData
                pendingThermalImage = draft.thermalImage
                pendingDigitalImage = draft.digitalImage
                aislamientoTermicoProblemClosed = draft.closed
            }

            fun normalizeEmissivityValue(raw: String): String = raw.trim().replace(',', '.')

            fun toElectricFormData(entity: Problema): ElectricProblemFormData =
                ElectricProblemFormData(
                    failureId = entity.idFalla,
                    componentTemperature = entity.problemTemperature?.toString() ?: "",
                    componentPhaseId = entity.problemPhase,
                    componentRms = entity.problemRms?.toString() ?: "",
                    referenceTemperature = entity.referenceTemperature?.toString() ?: "",
                    referencePhaseId = entity.referencePhase,
                    referenceRms = entity.referenceRms?.toString() ?: "",
                    additionalInfoId = entity.additionalInfo,
                    additionalRms = entity.additionalRms?.toString() ?: "",
                    emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true),
                    emissivity = entity.emissivity?.toString() ?: "",
                    indirectTempChecked = (entity.indirectTempCheck ?: "").equals("on", ignoreCase = true),
                    ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true),
                    ambientTemp = entity.tempAmbient?.toString() ?: "",
                    environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true),
                    environmentId = entity.environment,
                    windSpeedChecked = (entity.windSpeedCheck ?: "").equals("on", ignoreCase = true),
                    windSpeed = entity.windSpeed?.toString() ?: "",
                    manufacturerId = entity.idFabricante,
                    ratedLoad = entity.ratedLoad ?: "",
                    circuitVoltage = entity.circuitVoltage ?: "",
                    comments = entity.componentComment ?: "",
                    rpm = entity.rpm?.toString() ?: ""
                )

            fun toMechanicalFormData(entity: Problema): MechanicalProblemFormData =
                MechanicalProblemFormData(
                    failureId = entity.idFalla,
                    componentTemperature = entity.problemTemperature?.toString() ?: "",
                    componentPhaseId = null,
                    componentRms = entity.problemRms?.toString() ?: "",
                    referenceTemperature = entity.referenceTemperature?.toString() ?: "",
                    referencePhaseId = null,
                    referenceRms = entity.referenceRms?.toString() ?: "",
                    additionalInfoId = null,
                    additionalRms = "",
                    emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true),
                    emissivity = entity.emissivity?.toString() ?: "",
                    indirectTempChecked = false,
                    ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true),
                    ambientTemp = entity.tempAmbient?.toString() ?: "",
                    environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true),
                    environmentId = entity.environment,
                    windSpeedChecked = false,
                    windSpeed = "",
                    manufacturerId = entity.idFabricante,
                    ratedLoad = entity.ratedLoad ?: "",
                    circuitVoltage = entity.circuitVoltage ?: "",
                    comments = entity.componentComment ?: "",
                    rpm = entity.rpm?.toString() ?: "",
                    bearingType = entity.bearingType ?: ""
                )

            fun toAislamientoFormData(entity: Problema): AislamientoTermicoProblemFormData =
                AislamientoTermicoProblemFormData(
                    failureId = entity.idFalla,
                    componentTemperature = entity.problemTemperature?.toString() ?: "",
                    componentPhaseId = null,
                    componentRms = entity.problemRms?.toString() ?: "",
                    referenceTemperature = entity.referenceTemperature?.toString() ?: "",
                    referencePhaseId = null,
                    referenceRms = entity.referenceRms?.toString() ?: "",
                    additionalInfoId = null,
                    additionalRms = "",
                    emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true),
                    emissivity = entity.emissivity?.toString() ?: "",
                    indirectTempChecked = false,
                    ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true),
                    ambientTemp = entity.tempAmbient?.toString() ?: "",
                    environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true),
                    environmentId = entity.environment,
                    windSpeedChecked = false,
                    windSpeed = "",
                    manufacturerId = entity.idFabricante,
                    ratedLoad = entity.ratedLoad ?: "",
                    circuitVoltage = entity.circuitVoltage ?: "",
                    comments = entity.componentComment ?: "",
                    rpm = entity.rpm?.toString() ?: "",
                    bearingType = entity.bearingType ?: ""
                )

            fun toElectricRememberedFields(entity: Problema): ElectricProblemFormData =
                ElectricProblemFormData().let { base ->
                    val emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true)
                    val indirectTempChecked = (entity.indirectTempCheck ?: "").equals("on", ignoreCase = true)
                    val ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                    val environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true)
                    val windSpeedChecked = (entity.windSpeedCheck ?: "").equals("on", ignoreCase = true)
                    base.copy(
                        emissivityChecked = emissivityChecked,
                        emissivity = if (emissivityChecked) entity.emissivity?.toString() ?: "" else "",
                        indirectTempChecked = indirectTempChecked,
                        ambientTempChecked = ambientTempChecked,
                        ambientTemp = if (ambientTempChecked) entity.tempAmbient?.toString() ?: "" else "",
                        environmentChecked = environmentChecked,
                        environmentId = if (environmentChecked) entity.environment else null,
                        windSpeedChecked = windSpeedChecked,
                        windSpeed = if (windSpeedChecked) entity.windSpeed?.toString() ?: "" else ""
                    )
                }

            fun applySharedRememberedFields(
                source: Problema?,
                base: ElectricProblemFormData
            ): ElectricProblemFormData {
                if (source == null) return base
                val emissivityChecked = (source.emissivityCheck ?: "").equals("on", ignoreCase = true)
                val ambientTempChecked = (source.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                val environmentChecked = (source.environmentCheck ?: "").equals("on", ignoreCase = true)
                return base.copy(
                    emissivityChecked = emissivityChecked,
                    emissivity = if (emissivityChecked) source.emissivity?.toString() ?: "" else "",
                    ambientTempChecked = ambientTempChecked,
                    ambientTemp = if (ambientTempChecked) source.tempAmbient?.toString() ?: "" else "",
                    environmentChecked = environmentChecked,
                    environmentId = if (environmentChecked) source.environment else null
                )
            }

            fun toMechanicalRememberedFields(entity: Problema): MechanicalProblemFormData =
                MechanicalProblemFormData().let { base ->
                    val emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true)
                    val indirectTempChecked = (entity.indirectTempCheck ?: "").equals("on", ignoreCase = true)
                    val ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                    val environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true)
                    val windSpeedChecked = (entity.windSpeedCheck ?: "").equals("on", ignoreCase = true)
                    base.copy(
                        emissivityChecked = emissivityChecked,
                        emissivity = if (emissivityChecked) entity.emissivity?.toString() ?: "" else "",
                        indirectTempChecked = indirectTempChecked,
                        ambientTempChecked = ambientTempChecked,
                        ambientTemp = if (ambientTempChecked) entity.tempAmbient?.toString() ?: "" else "",
                        environmentChecked = environmentChecked,
                        environmentId = if (environmentChecked) entity.environment else null,
                        windSpeedChecked = windSpeedChecked,
                        windSpeed = if (windSpeedChecked) entity.windSpeed?.toString() ?: "" else ""
                    )
                }

            fun applySharedRememberedFields(
                source: Problema?,
                base: MechanicalProblemFormData
            ): MechanicalProblemFormData {
                if (source == null) return base
                val emissivityChecked = (source.emissivityCheck ?: "").equals("on", ignoreCase = true)
                val ambientTempChecked = (source.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                val environmentChecked = (source.environmentCheck ?: "").equals("on", ignoreCase = true)
                return base.copy(
                    emissivityChecked = emissivityChecked,
                    emissivity = if (emissivityChecked) source.emissivity?.toString() ?: "" else "",
                    ambientTempChecked = ambientTempChecked,
                    ambientTemp = if (ambientTempChecked) source.tempAmbient?.toString() ?: "" else "",
                    environmentChecked = environmentChecked,
                    environmentId = if (environmentChecked) source.environment else null
                )
            }

            fun toAislamientoRememberedFields(entity: Problema): AislamientoTermicoProblemFormData =
                AislamientoTermicoProblemFormData().let { base ->
                    val emissivityChecked = (entity.emissivityCheck ?: "").equals("on", ignoreCase = true)
                    val indirectTempChecked = (entity.indirectTempCheck ?: "").equals("on", ignoreCase = true)
                    val ambientTempChecked = (entity.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                    val environmentChecked = (entity.environmentCheck ?: "").equals("on", ignoreCase = true)
                    val windSpeedChecked = (entity.windSpeedCheck ?: "").equals("on", ignoreCase = true)
                    base.copy(
                        emissivityChecked = emissivityChecked,
                        emissivity = if (emissivityChecked) entity.emissivity?.toString() ?: "" else "",
                        indirectTempChecked = indirectTempChecked,
                        ambientTempChecked = ambientTempChecked,
                        ambientTemp = if (ambientTempChecked) entity.tempAmbient?.toString() ?: "" else "",
                        environmentChecked = environmentChecked,
                        environmentId = if (environmentChecked) entity.environment else null,
                        windSpeedChecked = windSpeedChecked,
                        windSpeed = if (windSpeedChecked) entity.windSpeed?.toString() ?: "" else ""
                    )
                }

            fun applySharedRememberedFields(
                source: Problema?,
                base: AislamientoTermicoProblemFormData
            ): AislamientoTermicoProblemFormData {
                if (source == null) return base
                val emissivityChecked = (source.emissivityCheck ?: "").equals("on", ignoreCase = true)
                val ambientTempChecked = (source.tempAmbientCheck ?: "").equals("on", ignoreCase = true)
                val environmentChecked = (source.environmentCheck ?: "").equals("on", ignoreCase = true)
                return base.copy(
                    emissivityChecked = emissivityChecked,
                    emissivity = if (emissivityChecked) source.emissivity?.toString() ?: "" else "",
                    ambientTempChecked = ambientTempChecked,
                    ambientTemp = if (ambientTempChecked) source.tempAmbient?.toString() ?: "" else "",
                    environmentChecked = environmentChecked,
                    environmentId = if (environmentChecked) source.environment else null
                )
            }

            suspend fun loadLastProblemDefaultsByType(problemTypeId: String?): Problema? {
                val inspId = currentInspection?.idInspeccion ?: return null
                val typeId = problemTypeId ?: return null
                return withContext(Dispatchers.IO) {
                    runCatching { problemaDao.getLastByInspectionAndType(inspId, typeId) }.getOrNull()
                }
            }

            suspend fun loadLastProblemDefaultsGlobalForThermalTypes(): Problema? {
                val inspId = currentInspection?.idInspeccion ?: return null
                val typeIds = listOfNotNull(
                    ELECTRIC_PROBLEM_TYPE_ID,
                    MECHANICAL_PROBLEM_TYPE_ID,
                    AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID
                )
                if (typeIds.isEmpty()) return null
                return withContext(Dispatchers.IO) {
                    runCatching { problemaDao.getLastByInspectionAndTypes(inspId, typeIds) }.getOrNull()
                }
            }

            fun isEmissivityValid(checked: Boolean, value: String): Boolean {
                if (!checked) return true
                val normalized = normalizeEmissivityValue(value)
                if (normalized.isBlank()) return false
                val number = normalized.toDoubleOrNull() ?: return false
                if (number < 0.0 || number > 1.0) return false
                val decimals = normalized.substringAfter('.', "")
                return !normalized.contains('.') || decimals.length <= 2
            }

            fun validateVisualForNavigation(): Boolean {
                val missing = buildList {
                    if (pendingHazardId.isNullOrBlank()) add("Problema")
                    if (pendingSeverityId.isNullOrBlank()) add("Severidad")
                    if (pendingThermalImage.isBlank()) add("Imagen térmica")
                    if (pendingDigitalImage.isBlank()) add("Imagen digital")
                }
                if (missing.isNotEmpty()) {
                    Toast.makeText(ctx, "Completa: ${missing.joinToString()}", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }

            fun validateElectricForNavigation(formData: ElectricProblemFormData): Boolean {
                val missing = buildList {
                    if (formData.failureId.isNullOrBlank()) add("Falla")
                    if (formData.componentTemperature.isBlank()) add("Temp. componente")
                    if (formData.componentPhaseId.isNullOrBlank()) add("Fase componente")
                    if (formData.referenceTemperature.isBlank()) add("Temp. referencia")
                    if (formData.referencePhaseId.isNullOrBlank()) add("Fase referencia")
                    if (pendingThermalImage.isBlank()) add("Imagen térmica")
                    if (pendingDigitalImage.isBlank()) add("Imagen digital")
                }
                if (missing.isNotEmpty()) {
                    Toast.makeText(ctx, "Completa: ${missing.joinToString()}", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (!isEmissivityValid(formData.emissivityChecked, formData.emissivity)) {
                    Toast.makeText(ctx, "Revisa el valor de emisividad.", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }

            fun validateMechanicalForNavigation(formData: MechanicalProblemFormData): Boolean {
                val missing = buildList {
                    if (formData.failureId.isNullOrBlank()) add("Falla")
                    if (formData.componentTemperature.isBlank()) add("Temp. componente")
                    if (formData.referenceTemperature.isBlank()) add("Temp. referencia")
                    if (pendingThermalImage.isBlank()) add("Imagen térmica")
                    if (pendingDigitalImage.isBlank()) add("Imagen digital")
                }
                if (missing.isNotEmpty()) {
                    Toast.makeText(ctx, "Completa: ${missing.joinToString()}", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (!isEmissivityValid(formData.emissivityChecked, formData.emissivity)) {
                    Toast.makeText(ctx, "Revisa el valor de emisividad.", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }

            fun validateAislamientoTermicoForNavigation(formData: AislamientoTermicoProblemFormData): Boolean {
                val missing = buildList {
                    if (formData.failureId.isNullOrBlank()) add("Falla")
                    if (formData.componentTemperature.isBlank()) add("Temp. componente")
                    if (formData.referenceTemperature.isBlank()) add("Temp. referencia")
                    if (pendingThermalImage.isBlank()) add("Imagen térmica")
                    if (pendingDigitalImage.isBlank()) add("Imagen digital")
                }
                if (missing.isNotEmpty()) {
                    Toast.makeText(ctx, "Completa: ${missing.joinToString()}", Toast.LENGTH_SHORT).show()
                    return false
                }
                if (!isEmissivityValid(formData.emissivityChecked, formData.emissivity)) {
                    Toast.makeText(ctx, "Revisa el valor de emisividad.", Toast.LENGTH_SHORT).show()
                    return false
                }
                return true
            }

            fun startVisualProblemEdit(problem: Problem) {
                val visualTypeId = PROBLEM_TYPE_IDS["Visual"]
                if (visualTypeId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No se encontro el tipo Visual.", Toast.LENGTH_SHORT).show()
                    return
                }
                scope.launch {
                    showElectricProblemDialog = false
                    showMechanicalProblemDialog = false
                    showAislamientoTermicoProblemDialog = false
                    resetVisualProblemForm()
                    val draft = withContext(Dispatchers.IO) {
                        runCatching {
                            VisualProblemEditor.loadDraft(
                                problemId = problem.id,
                                problemaDao = problemaDao,
                                ubicacionDao = ubicacionDao,
                                visualTypeId = visualTypeId
                            )
                        }.getOrNull()
                    }
                    if (draft == null) {
                        Toast.makeText(ctx, "Solo se pueden editar problemas visuales.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val entity = draft.problema
                    val ubId = entity.idUbicacion
                    if (ubId.isNullOrBlank()) {
                        Toast.makeText(ctx, "El problema no tiene una ubicacion asociada.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val fallbackRoute = titlePathForId(nodes, ubId).joinToString(" / ")
                    val equipmentName = draft.equipmentName
                        ?: problem.equipo.takeIf { it.isNotBlank() }
                        ?: ubId
                    val resolvedRoute = draft.route ?: if (fallbackRoute.isNotBlank()) fallbackRoute else entity.ruta ?: "-"
                    val inspectionNumber = entity.idInspeccion?.let { inspId ->
                        withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }.getOrNull()?.noInspeccion?.toString()
                        }
                    } ?: currentInspection?.noInspeccion?.toString() ?: "-"
                    pendingProblemEquipmentName = equipmentName
                    pendingProblemRoute = resolvedRoute
                    pendingProblemUbicacionId = ubId
                    pendingProblemNumber = entity.numeroProblema?.toString() ?: problem.no.toString()
                    pendingInspectionNumber = inspectionNumber
                    pendingHazardId = entity.idFalla?.takeIf { !it.isNullOrBlank() }
                        ?: visualHazardOptionsFixed.firstOrNull { option ->
                            entity.hazardIssue?.equals(option.second, ignoreCase = true) == true
                        }?.first
                    val hazardCatalogLabel = VISUAL_PROBLEM_TYPE_ID?.let { typeId ->
                        hazardOptionsByType[typeId]
                            ?.firstOrNull { it.first.equals(entity.idFalla, ignoreCase = true) }
                            ?.second
                    }
                    pendingHazardLabel = hazardCatalogLabel
                        ?: entity.hazardIssue?.takeIf { it.isNotBlank() }
                    pendingSeverityId = entity.idSeveridad
                    pendingSeverityLabel = severityCatalog.firstOrNull {
                        it.idSeveridad.equals(entity.idSeveridad, ignoreCase = true)
                    }?.severidad
                    pendingObservation = entity.componentComment.orEmpty()
                    pendingThermalImage = entity.irFile.orEmpty()
                    pendingDigitalImage = entity.photoFile.orEmpty()
                    pendingProblemType = problemTypeLabelForId(VISUAL_PROBLEM_TYPE_ID)
                    editingProblemId = entity.idProblema
                    editingProblemOriginal = entity
                    visualProblemClosed = entity.estatusProblema?.equals("Cerrado", ignoreCase = true) == true
                    cronicoActionEnabled = canEnableCronico(entity)
                    ensureVisualDefaults(allowObservationUpdate = false, allowUnknownSelections = true)
                    applyVisualDraft(entity.idProblema)
                    showVisualInspectionDialog = true
                }
            }
            fun startElectricProblemEdit(problem: Problem) {
                val electricTypeId = ELECTRIC_PROBLEM_TYPE_ID
                if (electricTypeId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No se encontro el tipo eléctrico.", Toast.LENGTH_SHORT).show()
                    return
                }
                scope.launch {
                    showVisualInspectionDialog = false
                    showMechanicalProblemDialog = false
                    showAislamientoTermicoProblemDialog = false
                    val entity = withContext(Dispatchers.IO) {
                        runCatching { problemaDao.getById(problem.id) }.getOrNull()
                    }
                    if (entity == null || entity.idTipoInspeccion.isNullOrBlank()
                        || !entity.idTipoInspeccion.equals(electricTypeId, ignoreCase = true)
                    ) {
                        Toast.makeText(ctx, "Solo se pueden editar problemas eléctricos.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val ubicacionId = entity.idUbicacion
                    if (ubicacionId.isNullOrBlank()) {
                        Toast.makeText(ctx, "El problema no tiene una ubicacion asociada.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    resetElectricProblemState()
                    editingElectricProblemOriginal = entity
                    editingElectricProblemId = entity.idProblema
                    electricProblemClosed = entity.estatusProblema?.equals("Cerrado", ignoreCase = true) == true
                    val equipmentName = problem.equipo.takeIf { it.isNotBlank() } ?: ubicacionId
                    val fallbackRoute = titlePathForId(nodes, ubicacionId).joinToString(" / ")
                    val resolvedRoute = fallbackRoute.takeIf { it.isNotBlank() } ?: entity.ruta ?: "-"
                    val inspectionNumber = entity.idInspeccion?.let { inspId ->
                        withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }.getOrNull()?.noInspeccion?.toString()
                        }
                    } ?: currentInspection?.noInspeccion?.toString() ?: "-"
                    pendingProblemEquipmentName = equipmentName
                    pendingProblemRoute = resolvedRoute
                    pendingProblemUbicacionId = ubicacionId
                    pendingProblemNumber = entity.numeroProblema?.toString() ?: problem.no.toString()
                    pendingInspectionNumber = inspectionNumber
                    pendingProblemType = problemTypeLabelForId(electricTypeId)
                    pendingThermalImage = entity.irFile.orEmpty()
                    pendingDigitalImage = entity.photoFile.orEmpty()
                    electricProblemFormKey += 1
                    cronicoActionEnabled = canEnableCronico(entity)
                    applyElectricDraft(entity.idProblema)
                    showElectricProblemDialog = true
                }
            }
            fun startMechanicalProblemEdit(problem: Problem) {
                val mechanicalTypeId = MECHANICAL_PROBLEM_TYPE_ID
                if (mechanicalTypeId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No se encontro el tipo mecánico.", Toast.LENGTH_SHORT).show()
                    return
                }
                scope.launch {
                    showVisualInspectionDialog = false
                    showElectricProblemDialog = false
                    val entity = withContext(Dispatchers.IO) {
                        runCatching { problemaDao.getById(problem.id) }.getOrNull()
                    }
                    if (entity == null || entity.idTipoInspeccion.isNullOrBlank()
                        || !entity.idTipoInspeccion.equals(mechanicalTypeId, ignoreCase = true)
                    ) {
                        Toast.makeText(ctx, "Solo se pueden editar problemas mecánicos.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    val ubicacionId = entity.idUbicacion
                    if (ubicacionId.isNullOrBlank()) {
                        Toast.makeText(ctx, "El problema no tiene una ubicacion asociada.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    resetMechanicalProblemState()
                    val equipmentName = problem.equipo.takeIf { it.isNotBlank() } ?: ubicacionId
                    val fallbackRoute = titlePathForId(nodes, ubicacionId).joinToString(" / ")
                    val resolvedRoute = fallbackRoute.takeIf { it.isNotBlank() } ?: entity.ruta ?: "-"
                    val inspectionNumber = entity.idInspeccion?.let { inspId ->
                        withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }.getOrNull()?.noInspeccion?.toString()
                        }
                    } ?: currentInspection?.noInspeccion?.toString() ?: "-"
                    pendingProblemEquipmentName = equipmentName
                    pendingProblemRoute = resolvedRoute
                    pendingProblemUbicacionId = ubicacionId
                    pendingProblemNumber = entity.numeroProblema?.toString() ?: problem.no.toString()
                    pendingInspectionNumber = inspectionNumber
                    pendingProblemType = problemTypeLabelForId(mechanicalTypeId)
                    pendingThermalImage = entity.irFile.orEmpty()
                    pendingDigitalImage = entity.photoFile.orEmpty()
                    editingMechanicalProblemOriginal = entity
                    editingMechanicalProblemId = entity.idProblema
                    mechanicalProblemClosed = entity.estatusProblema?.equals("Cerrado", ignoreCase = true) == true
                    mechanicalProblemFormKey += 1
                    cronicoActionEnabled = canEnableCronico(entity)
                    applyMechanicalDraft(entity.idProblema)
                    showMechanicalProblemDialog = true
                }
            }
            fun startAislamientoTermicoProblemEdit(problem: Problem) {
                val aislamientoTypeId = AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID
                if (aislamientoTypeId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No se encontro el tipo aislamiento térrmico.", Toast.LENGTH_SHORT).show()
                    return
                }
                scope.launch {
                    showVisualInspectionDialog = false
                    showElectricProblemDialog = false
                    showMechanicalProblemDialog = false
                    showAislamientoTermicoProblemDialog = false
                    val entity = withContext(Dispatchers.IO) {
                        runCatching { problemaDao.getById(problem.id) }.getOrNull()
                    }
                    if (entity == null || entity.idTipoInspeccion.isNullOrBlank()
                        || !entity.idTipoInspeccion.equals(aislamientoTypeId, ignoreCase = true)
                    ) {
                        Toast.makeText(
                            ctx,
                            "Solo se pueden editar problemas de aislamiento térrmico.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@launch
                    }
                    val ubicacionId = entity.idUbicacion
                    if (ubicacionId.isNullOrBlank()) {
                        Toast.makeText(ctx, "El problema no tiene una ubicacion asociada.", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    resetAislamientoTermicoProblemState()
                    val equipmentName = problem.equipo.takeIf { it.isNotBlank() } ?: ubicacionId
                    val fallbackRoute = titlePathForId(nodes, ubicacionId).joinToString(" / ")
                    val resolvedRoute = fallbackRoute.takeIf { it.isNotBlank() } ?: entity.ruta ?: "-"
                    val inspectionNumber = entity.idInspeccion?.let { inspId ->
                        withContext(Dispatchers.IO) {
                            runCatching { inspeccionDao.getById(inspId) }.getOrNull()?.noInspeccion?.toString()
                        }
                    } ?: currentInspection?.noInspeccion?.toString() ?: "-"
                    pendingProblemEquipmentName = equipmentName
                    pendingProblemRoute = resolvedRoute
                    pendingProblemUbicacionId = ubicacionId
                    pendingProblemNumber = entity.numeroProblema?.toString() ?: problem.no.toString()
                    pendingInspectionNumber = inspectionNumber
                    pendingProblemType = problemTypeLabelForId(aislamientoTypeId)
                    pendingThermalImage = entity.irFile.orEmpty()
                    pendingDigitalImage = entity.photoFile.orEmpty()
                    editingAislamientoTermicoProblemOriginal = entity
                    editingAislamientoTermicoProblemId = entity.idProblema
                    aislamientoTermicoProblemClosed = entity.estatusProblema?.equals("Cerrado", ignoreCase = true) == true
                    aislamientoTermicoProblemFormKey += 1
                    cronicoActionEnabled = canEnableCronico(entity)
                    applyAislamientoTermicoDraft(entity.idProblema)
                    showAislamientoTermicoProblemDialog = true
                }
            }
            fun openProblemForNavigation(problem: Problem) {
                val normalizedType = problem.tipo?.normalizeProblemKey()
                when {
                    problem.tipoId?.equals(VISUAL_PROBLEM_TYPE_ID, ignoreCase = true) == true -> startVisualProblemEdit(problem)
                    problem.tipoId?.equals(ELECTRIC_PROBLEM_TYPE_ID, ignoreCase = true) == true -> startElectricProblemEdit(problem)
                    problem.tipoId?.equals(MECHANICAL_PROBLEM_TYPE_ID, ignoreCase = true) == true -> startMechanicalProblemEdit(problem)
                    problem.tipoId?.equals(AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID, ignoreCase = true) == true -> startAislamientoTermicoProblemEdit(problem)
                    normalizedType == "visual" -> startVisualProblemEdit(problem)
                    normalizedType == "electrico" -> startElectricProblemEdit(problem)
                    normalizedType == "mecanico" -> startMechanicalProblemEdit(problem)
                    normalizedType == "aislamiento termico" -> startAislamientoTermicoProblemEdit(problem)
                    else -> Toast.makeText(ctx, "La edición de este tipo de problema no está disponible.", Toast.LENGTH_SHORT).show()
                }
            }

            fun navigateFromVisual(delta: Int) {
                val currentId = editingProblemId ?: return
                if (!validateVisualForNavigation()) return
                val draft = ProblemDraft.VisualDraft(
                    hazardId = pendingHazardId,
                    hazardLabel = pendingHazardLabel,
                    severityId = pendingSeverityId,
                    severityLabel = pendingSeverityLabel,
                    observation = pendingObservation,
                    thermalImage = pendingThermalImage,
                    digitalImage = pendingDigitalImage,
                    closed = visualProblemClosed
                )
                problemDrafts = problemDrafts + (currentId to draft)
                val nextIndex = problemNavIndex + delta
                if (nextIndex !in problemNavList.indices) return
                problemNavIndex = nextIndex
                openProblemForNavigation(problemNavList[nextIndex])
            }

            fun navigateFromElectric(delta: Int, formData: ElectricProblemFormData) {
                val currentId = editingElectricProblemId ?: return
                if (!validateElectricForNavigation(formData)) return
                val draft = ProblemDraft.ElectricDraft(
                    formData = formData,
                    thermalImage = pendingThermalImage,
                    digitalImage = pendingDigitalImage,
                    closed = electricProblemClosed
                )
                problemDrafts = problemDrafts + (currentId to draft)
                val nextIndex = problemNavIndex + delta
                if (nextIndex !in problemNavList.indices) return
                problemNavIndex = nextIndex
                openProblemForNavigation(problemNavList[nextIndex])
            }

            fun navigateFromMechanical(delta: Int, formData: MechanicalProblemFormData) {
                val currentId = editingMechanicalProblemId ?: return
                if (!validateMechanicalForNavigation(formData)) return
                val draft = ProblemDraft.MechanicalDraft(
                    formData = formData,
                    thermalImage = pendingThermalImage,
                    digitalImage = pendingDigitalImage,
                    closed = mechanicalProblemClosed
                )
                problemDrafts = problemDrafts + (currentId to draft)
                val nextIndex = problemNavIndex + delta
                if (nextIndex !in problemNavList.indices) return
                problemNavIndex = nextIndex
                openProblemForNavigation(problemNavList[nextIndex])
            }
            var selectedProblemType by rememberSaveable { mutableStateOf(problemTypeLabelForId(ELECTRIC_PROBLEM_TYPE_ID)) }
            var showInvalidParentDialog by rememberSaveable { mutableStateOf(false) }
            var showNewUbDialog by remember { mutableStateOf(false) }
            var isSavingNewUb by remember { mutableStateOf(false) }
            val locationForm = rememberLocationFormState()
            var currentUserId by remember { mutableStateOf<String?>(null) }
            var currentSitioId by remember { mutableStateOf<String?>(null) }
            var editingUbId by remember { mutableStateOf<String?>(null) }
            var editingParentId by remember { mutableStateOf<String?>(null) }
            var editingDetId by remember { mutableStateOf<String?>(null) }
            var editingInspId by remember { mutableStateOf<String?>(null) }
            var deleteUbInfoMessage by remember { mutableStateOf<String?>(null) }
            var pendingDeleteUbId by remember { mutableStateOf<String?>(null) }
            fun startEditUb(node: TreeNode) {
                if (node.id.startsWith("root:")) return
                locationForm.error = null
                editingUbId = node.id
                showEditUbDialog = true
                editTab = 0
                scope.launch {
                    val ub = runCatching { ubicacionDao.getById(node.id) }.getOrNull()
                    if (ub != null) {
                        locationForm.name = ub.ubicacion ?: ""
                        locationForm.description = ub.descripcion ?: ""
                        locationForm.isEquipment = (ub.esEquipo ?: "").equals("SI", ignoreCase = true)
                        locationForm.barcode = ub.codigoBarras ?: ""
                        editingParentId = ub.idUbicacionPadre
                        locationForm.prioridadId = ub.idTipoPrioridad
                        locationForm.prioridadLabel = prioridadOptions.firstOrNull { it.idTipoPrioridad == ub.idTipoPrioridad }?.tipoPrioridad
                            ?: (ub.idTipoPrioridad ?: "")
                        locationForm.fabricanteId = ub.fabricante
                        locationForm.fabricanteLabel = fabricanteOptions.firstOrNull { it.idFabricante == ub.fabricante }?.fabricante
                            ?: (ub.fabricante ?: "")
                    }
                    val det = runCatching { inspeccionDetDao.getByUbicacion(node.id).firstOrNull() }.getOrNull()
                    editingDetId = det?.idInspeccionDet
                    editingInspId = det?.idInspeccion
                    val statusId = det?.idStatusInspeccionDet
                    if (!statusId.isNullOrBlank()) {
                        locationForm.statusId = statusId
                        locationForm.statusLabel = statusOptions.firstOrNull { it.idStatusInspeccionDet == statusId }?.estatusInspeccionDet
                            ?: statusId
                    }
                }
            }
            fun navigateFromAislamientoTermico(delta: Int, formData: AislamientoTermicoProblemFormData) {
                val currentId = editingAislamientoTermicoProblemId ?: return
                if (!validateAislamientoTermicoForNavigation(formData)) return
                val draft = ProblemDraft.AislamientoTermicoDraft(
                    formData = formData,
                    thermalImage = pendingThermalImage,
                    digitalImage = pendingDigitalImage,
                    closed = aislamientoTermicoProblemClosed
                )
                val nextIndex = problemNavIndex + delta
                if (nextIndex !in problemNavList.indices) return
                problemDrafts = problemDrafts + (currentId to draft)
                problemNavIndex = nextIndex
                openProblemForNavigation(problemNavList[nextIndex])
            }
    var deleteUbConfirmNode by remember { mutableStateOf<TreeNode?>(null) }

    suspend fun refreshTree(
        selectIfAvailable: String? = null,
        preserveSelection: String? = selectedId,
        extraExpanded: Collection<String?> = emptyList()
    ) {
        val newNodes = inspectionRepository.loadTree(rootId, rootTitle)
        nodes = newNodes

        fun nodeExists(id: String?): Boolean = !id.isNullOrBlank() && findById(id, newNodes) != null

        val expandedSnapshot = expanded.toList()
        val desiredExpanded = LinkedHashSet<String>()
        desiredExpanded.add(rootId)
        val idsToPreserve = buildList {
            addAll(expandedSnapshot)
            extraExpanded.forEach { id -> if (!id.isNullOrBlank()) add(id) }
        }
        idsToPreserve.forEach { id ->
            if (nodeExists(id)) desiredExpanded.add(id)
        }
        expanded.clear()
        expanded.addAll(desiredExpanded)

        val targetSelection = when {
            nodeExists(selectIfAvailable) -> selectIfAvailable
            nodeExists(preserveSelection) -> preserveSelection
            else -> rootId
        }
        val previousSelection = selectedId
        if (targetSelection != null && (previousSelection != targetSelection || !nodeExists(previousSelection))) {
            onSelectNode(targetSelection)
        }
    }
            suspend fun updateParentStatusesAfterManualChange(
                inspectionId: String,
                startUbicacionId: String,
                nowTs: String,
                knownParentId: String? = null
            ) {
                val ubicaciones = runCatching { ubicacionDao.getAllActivas() }.getOrElse { emptyList() }
                if (ubicaciones.isEmpty()) return

                val detRows = runCatching { inspeccionDetDao.getByInspeccion(inspectionId) }.getOrElse { emptyList() }
                if (detRows.isEmpty()) return

                val ubicById = ubicaciones.associateBy { it.idUbicacion }
                val childrenByParent = ubicaciones.groupBy { it.idUbicacionPadre }
                val detByUbicacion = detRows.mapNotNull { row -> row.idUbicacion?.let { it to row } }.toMap().toMutableMap()

                var currentId: String? = startUbicacionId
                var forcedParentId: String? = knownParentId
                while (true) {
                    val parentId = forcedParentId ?: currentId?.let { ubicById[it]?.idUbicacionPadre }
                    forcedParentId = null
                    if (parentId.isNullOrBlank() || parentId == "0") break

                    val childIds = childrenByParent[parentId].orEmpty().map { it.idUbicacion }
                    if (childIds.isNotEmpty()) {
                        val hasPendingChild = childIds.any { childId ->
                            detByUbicacion[childId]?.idStatusInspeccionDet.equals(STATUS_POR_VERIFICAR, ignoreCase = true)
                        }
                        val parentStatusId = if (hasPendingChild) STATUS_POR_VERIFICAR else STATUS_VERIFICADO
                        val parentColorId = if (hasPendingChild) 1 else 4
                        val parentDet = detByUbicacion[parentId]

                        if (parentDet != null &&
                            (!parentDet.idStatusInspeccionDet.equals(parentStatusId, ignoreCase = true) ||
                                parentDet.idEstatusColorText != parentColorId)
                        ) {
                            val updated = parentDet.copy(
                                idStatusInspeccionDet = parentStatusId,
                                idEstatusColorText = parentColorId,
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

            suspend fun applyManualStatusToUbicacionIds(statusId: String, ubicacionIds: List<String>) {
                val inspectionId = currentInspection?.idInspeccion
                if (inspectionId.isNullOrBlank()) return

                val targetIds = ubicacionIds
                    .filter { it.isNotBlank() && !it.startsWith("root:") }
                    .distinct()
                if (targetIds.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(ctx, "Selecciona una ubicación del árbol.", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val nowTs = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val colorId = if (statusId.equals(STATUS_POR_VERIFICAR, ignoreCase = true)) 1 else 4
                var updatedCount = 0

                targetIds.forEach { ubicacionId ->
                    val detRow = runCatching {
                        inspeccionDetDao.getByUbicacion(ubicacionId).firstOrNull { it.idInspeccion == inspectionId }
                    }.getOrNull() ?: return@forEach

                    val updatedDet = detRow.copy(
                        idStatusInspeccionDet = statusId,
                        idEstatusColorText = colorId,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { inspeccionDetDao.update(updatedDet) }
                    updateParentStatusesAfterManualChange(
                        inspectionId = inspectionId,
                        startUbicacionId = ubicacionId,
                        nowTs = nowTs
                    )
                    updatedCount += 1
                }

                refreshTree(preserveSelection = selectedId)
                checkedStatusLocationIds.clear()
                withContext(Dispatchers.Main) {
                    if (updatedCount > 0) {
                        Toast.makeText(ctx, "Estatus actualizado.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(ctx, "No se encontró detalle de inspección para los elementos seleccionados.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            // Lee el usuario actual del CompositionLocal en contexto @Composable
            val currentUser = LocalCurrentUser.current
            LaunchedEffect(statusOptions, locationForm.statusId) {
                val statusId = locationForm.statusId
                if (!statusId.isNullOrBlank() && locationForm.statusLabel.isBlank()) {
                    locationForm.statusLabel = statusOptions.firstOrNull {
                        it.idStatusInspeccionDet == statusId
                    }?.estatusInspeccionDet ?: statusId
                }
            }
            LaunchedEffect(prioridadOptions, locationForm.prioridadId) {
                val prioridadId = locationForm.prioridadId
                if (!prioridadId.isNullOrBlank() && locationForm.prioridadLabel.isBlank()) {
                    locationForm.prioridadLabel = prioridadOptions.firstOrNull {
                        it.idTipoPrioridad == prioridadId
                    }?.tipoPrioridad ?: prioridadId
                }
            }
            LaunchedEffect(fabricanteOptions, locationForm.fabricanteId) {
                val fabricanteId = locationForm.fabricanteId
                if (!fabricanteId.isNullOrBlank() && locationForm.fabricanteLabel.isBlank()) {
                    locationForm.fabricanteLabel = fabricanteOptions.firstOrNull {
                        it.idFabricante == fabricanteId
                    }?.fabricante ?: fabricanteId
                }
            }
            fun calculateSeverityId(difference: Double?): String? {
                if (difference == null) return null
                return when {
                    difference < 1 -> "1D56EDB4-8D6E-11D3-9270-006008A19766"
                    difference < 4 -> "1D56EDB3-8D6E-11D3-9270-006008A19766"
                    difference < 9 -> "1D56EDB2-8D6E-11D3-9270-006008A19766"
                    difference < 16 -> "1D56EDB1-8D6E-11D3-9270-006008A19766"
                    else -> "1D56EDB0-8D6E-11D3-9270-006008A19766"
                }
            }
            suspend fun persistProblemDrafts(
                currentId: String,
                currentDraft: ProblemDraft,
                currentUserId: String?
            ): Boolean {
                val inspection = currentInspection
                if (inspection == null) {
                    Toast.makeText(ctx, "No hay inspeccion activa.", Toast.LENGTH_SHORT).show()
                    return false
                }
                problemDrafts = problemDrafts + (currentId to currentDraft)
                val draftsSnapshot = problemDrafts
                val nowTs = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                val saved = withContext(Dispatchers.IO) {
                    var failed = false
                    draftsSnapshot.forEach { (problemId, draft) ->
                        val base = runCatching { problemaDao.getById(problemId) }.getOrNull()
                        if (base == null) {
                            failed = true
                            return@forEach
                        }
                        val updated = when (draft) {
                            is ProblemDraft.VisualDraft -> {
                                base.copy(
                                    hazardIssue = draft.hazardId,
                                    idFalla = draft.hazardId,
                                    idSeveridad = draft.severityId,
                                    componentComment = draft.observation.ifBlank {
                                        base.componentComment.orEmpty()
                                    },
                                    estatusProblema = if (draft.closed) "Cerrado" else "Abierto",
                                    cerradoEnInspeccion = if (draft.closed) inspection.idInspeccion
                                    else base.cerradoEnInspeccion,
                                    irFile = draft.thermalImage,
                                    photoFile = draft.digitalImage,
                                    modificadoPor = currentUserId,
                                    fechaMod = nowTs
                                )
                            }
                            is ProblemDraft.ElectricDraft -> {
                                val formData = draft.formData
                                val componentTemp = formData.componentTemperature.toDoubleOrNull()
                                val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                                val difference = componentTemp?.let { comp ->
                                    referenceTemp?.let { ref -> comp - ref }
                                }
                                val severityId = calculateSeverityId(difference)
                                val emissivityValue = normalizeEmissivityValue(formData.emissivity).toDoubleOrNull()
                                val finalComment = formData.comments.ifBlank {
                                    base.componentComment.orEmpty()
                                }
                                base.copy(
                                    problemTemperature = componentTemp,
                                    referenceTemperature = referenceTemp,
                                    problemPhase = formData.componentPhaseId,
                                    referencePhase = formData.referencePhaseId,
                                    problemRms = formData.componentRms.toDoubleOrNull(),
                                    referenceRms = formData.referenceRms.toDoubleOrNull(),
                                    additionalInfo = formData.additionalInfoId,
                                    additionalRms = formData.additionalRms.toDoubleOrNull(),
                                    emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                    emissivity = emissivityValue,
                                    indirectTempCheck = if (formData.indirectTempChecked) "on" else "off",
                                    tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                    tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                    environmentCheck = if (formData.environmentChecked) "on" else "off",
                                    environment = formData.environmentId,
                                    windSpeedCheck = if (formData.windSpeedChecked) "on" else "off",
                                    windSpeed = formData.windSpeed.toDoubleOrNull(),
                                    idFabricante = formData.manufacturerId,
                                    ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                    circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                    idFalla = formData.failureId,
                                    componentComment = finalComment,
                                    estatusProblema = if (draft.closed) "Cerrado" else "Abierto",
                                    cerradoEnInspeccion = if (draft.closed) inspection.idInspeccion
                                    else base.cerradoEnInspeccion,
                                    aumentoTemperatura = difference,
                                    idSeveridad = severityId,
                                    irFile = draft.thermalImage,
                                    photoFile = draft.digitalImage,
                                    modificadoPor = currentUserId,
                                    fechaMod = nowTs
                                )
                            }
                            is ProblemDraft.MechanicalDraft -> {
                                val formData = draft.formData
                                val componentTemp = formData.componentTemperature.toDoubleOrNull()
                                val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                                val difference = componentTemp?.let { comp ->
                                    referenceTemp?.let { ref -> comp - ref }
                                }
                                val severityId = calculateSeverityId(difference)
                                val emissivityValue = normalizeEmissivityValue(formData.emissivity).toDoubleOrNull()
                                val finalComment = formData.comments.ifBlank {
                                    base.componentComment.orEmpty()
                                }
                                base.copy(
                                    problemTemperature = componentTemp,
                                    referenceTemperature = referenceTemp,
                                    problemPhase = null,
                                    referencePhase = null,
                                    problemRms = formData.componentRms.toDoubleOrNull(),
                                    referenceRms = formData.referenceRms.toDoubleOrNull(),
                                    additionalInfo = null,
                                    additionalRms = null,
                                    emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                    emissivity = emissivityValue,
                                    indirectTempCheck = "off",
                                    tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                    tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                    environmentCheck = if (formData.environmentChecked) "on" else "off",
                                    environment = formData.environmentId,
                                    windSpeedCheck = "off",
                                    windSpeed = null,
                                    idFabricante = formData.manufacturerId,
                                    ratedLoadCheck = "off",
                                    ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                    circuitVoltageCheck = "off",
                                    circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                    idFalla = formData.failureId,
                                    componentComment = finalComment,
                                    estatusProblema = if (draft.closed) "Cerrado" else "Abierto",
                                    cerradoEnInspeccion = if (draft.closed) inspection.idInspeccion
                                    else base.cerradoEnInspeccion,
                                    aumentoTemperatura = difference,
                                    idSeveridad = severityId,
                                    rpm = formData.rpm.toDoubleOrNull(),
                                    bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                    irFile = draft.thermalImage,
                                    photoFile = draft.digitalImage,
                                    modificadoPor = currentUserId,
                                    fechaMod = nowTs
                                )
                            }
                            is ProblemDraft.AislamientoTermicoDraft -> {
                                val formData = draft.formData
                                val componentTemp = formData.componentTemperature.toDoubleOrNull()
                                val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                                val difference = componentTemp?.let { comp ->
                                    referenceTemp?.let { ref -> comp - ref }
                                }
                                val severityId = calculateSeverityId(difference)
                                val emissivityValue = normalizeEmissivityValue(formData.emissivity).toDoubleOrNull()
                                val finalComment = formData.comments.ifBlank {
                                    base.componentComment.orEmpty()
                                }
                                base.copy(
                                    problemTemperature = componentTemp,
                                    referenceTemperature = referenceTemp,
                                    problemPhase = null,
                                    referencePhase = null,
                                    problemRms = formData.componentRms.toDoubleOrNull(),
                                    referenceRms = formData.referenceRms.toDoubleOrNull(),
                                    additionalInfo = null,
                                    additionalRms = null,
                                    emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                    emissivity = emissivityValue,
                                    indirectTempCheck = "off",
                                    tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                    tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                    environmentCheck = if (formData.environmentChecked) "on" else "off",
                                    environment = formData.environmentId,
                                    windSpeedCheck = "off",
                                    windSpeed = null,
                                    idFabricante = formData.manufacturerId,
                                    ratedLoadCheck = "off",
                                    ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                    circuitVoltageCheck = "off",
                                    circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                    idFalla = formData.failureId,
                                    componentComment = finalComment,
                                    estatusProblema = if (draft.closed) "Cerrado" else "Abierto",
                                    cerradoEnInspeccion = if (draft.closed) inspection.idInspeccion
                                    else base.cerradoEnInspeccion,
                                    aumentoTemperatura = difference,
                                    idSeveridad = severityId,
                                    rpm = formData.rpm.toDoubleOrNull(),
                                    bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                    irFile = draft.thermalImage,
                                    photoFile = draft.digitalImage,
                                    modificadoPor = currentUserId,
                                    fechaMod = nowTs
                                )
                            }
                        }
                        val updateResult = runCatching { problemaDao.update(updated) }
                        if (updateResult.isFailure) {
                            failed = true
                            return@forEach
                        }
                        val locationId = base.idUbicacion
                        if (!locationId.isNullOrBlank()) {
                            val detRow = runCatching {
                                inspeccionDetDao.getByUbicacion(locationId)
                                    .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                            }.getOrNull()
                            if (detRow != null) {
                                val updatedDet = detRow.copy(
                                    idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                    idEstatusColorText = 2,
                                    modificadoPor = currentUserId,
                                    fechaMod = nowTs
                                )
                                runCatching { inspeccionDetDao.update(updatedDet) }
                                updateParentStatusesAfterManualChange(
                                    inspectionId = inspection.idInspeccion,
                                    startUbicacionId = locationId,
                                    nowTs = nowTs
                                )
                            }
                        }
                    }
                    !failed
                }
                if (saved) {
                    problemsRefreshTick++
                    refreshTree(preserveSelection = pendingProblemUbicacionId ?: selectedId)
                    Toast.makeText(ctx, "Cambios guardados.", Toast.LENGTH_SHORT).show()
                    resetProblemNavigation()
                    return true
                }
                Toast.makeText(
                    ctx,
                    "No se pudieron guardar todos los cambios.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            fun createCronicoFromProblem(
                entity: com.example.etic.data.local.entities.Problema,
                onSuccess: (created: com.example.etic.data.local.entities.Problema) -> Unit
            ) {
                val inspection = currentInspection
                if (inspection == null || inspection.idInspeccion.isNullOrBlank()) {
                    Toast.makeText(ctx, "No hay inspeccion activa.", Toast.LENGTH_SHORT).show()
                    return
                }
                val isOpen = entity.estatusProblema?.equals("Abierto", ignoreCase = true) == true
                val isPastInspection = entity.idInspeccion?.equals(inspection.idInspeccion, ignoreCase = true) != true
                if (!isOpen || !isPastInspection) {
                    Toast.makeText(ctx, "Solo puedes marcar como cronico problemas abiertos de inspecciones pasadas.", Toast.LENGTH_SHORT).show()
                    return
                }
                val ubicacionId = entity.idUbicacion
                if (ubicacionId.isNullOrBlank()) {
                    Toast.makeText(ctx, "El problema no tiene una ubicacion asociada.", Toast.LENGTH_SHORT).show()
                    return
                }
                val tipoId = entity.idTipoInspeccion
                if (tipoId.isNullOrBlank()) {
                    Toast.makeText(ctx, "No se encontro el tipo de problema.", Toast.LENGTH_SHORT).show()
                    return
                }
                scope.launch {
                    if (isSavingCronico) return@launch
                    isSavingCronico = true
                    try {
                        val detRow = withContext(Dispatchers.IO) {
                            runCatching {
                                inspeccionDetDao.getByUbicacion(ubicacionId)
                                    .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                            }.getOrNull()
                        }
                        val detId = detRow?.idInspeccionDet
                        val numero = fetchNextProblemNumber(tipoId).toIntOrNull() ?: 1
                        val nowTs = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        val isVisualType = VISUAL_PROBLEM_TYPE_ID?.let {
                            entity.idTipoInspeccion?.equals(it, ignoreCase = true) == true
                        } ?: false
                        val difference = if (!isVisualType) {
                            val comp = entity.problemTemperature
                            val ref = entity.referenceTemperature
                            if (comp != null && ref != null) comp - ref else entity.aumentoTemperatura
                        } else {
                            entity.aumentoTemperatura
                        }
                        val severityId = if (!isVisualType) {
                            calculateSeverityId(difference) ?: entity.idSeveridad
                        } else {
                            entity.idSeveridad
                        }
                        val nuevo = entity.copy(
                            idProblema = java.util.UUID.randomUUID().toString().uppercase(),
                            numeroProblema = numero,
                            idSitio = inspection.idSitio,
                            idInspeccion = inspection.idInspeccion,
                            idInspeccionDet = detId,
                            estatusProblema = "Abierto",
                            estatus = "Activo",
                            esCronico = "SI",
                            aumentoTemperatura = difference,
                            idSeveridad = severityId,
                            creadoPor = currentUser?.idUsuario,
                            fechaCreacion = nowTs,
                            modificadoPor = null,
                            fechaMod = null
                        )
                        val result = withContext(Dispatchers.IO) {
                            runCatching { problemaDao.insert(nuevo) }
                        }
                        if (result.isSuccess) {
                            val markedOriginal = entity.copy(
                                estatusProblema = "Cerrado",
                                esCronico = "SI",
                                modificadoPor = currentUser?.idUsuario,
                                fechaMod = nowTs
                            )
                            withContext(Dispatchers.IO) {
                                runCatching { problemaDao.update(markedOriginal) }
                            }
                            if (detRow != null) {
                                val updatedDet = detRow.copy(
                                    idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                    idEstatusColorText = 2,
                                    modificadoPor = currentUser?.idUsuario,
                                    fechaMod = nowTs
                                )
                                withContext(Dispatchers.IO) { runCatching { inspeccionDetDao.update(updatedDet) } }
                                updateParentStatusesAfterManualChange(
                                    inspectionId = inspection.idInspeccion,
                                    startUbicacionId = ubicacionId,
                                    nowTs = nowTs
                                )
                            }
                            problemsRefreshTick++
                            refreshTree(preserveSelection = ubicacionId)
                            Toast.makeText(ctx, "Problema cronico creado.", Toast.LENGTH_SHORT).show()
                            onSuccess(nuevo)
                        } else {
                            val message = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                            Toast.makeText(
                                ctx,
                                "No se pudo crear el problema cronico: $message",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {
                        isSavingCronico = false
                    }
                }
            }
            fun saveElectricProblem(formData: ElectricProblemFormData) {
                val inspection = currentInspection
                if (inspection == null) {
                    Toast.makeText(ctx, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                    return
                }
                val locationId = pendingProblemUbicacionId ?: selectedId
                if (locationId.isNullOrBlank()) {
                    Toast.makeText(ctx, "Selecciona un equipo.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (pendingThermalImage.isBlank() || pendingDigitalImage.isBlank()) {
                    Toast.makeText(ctx, "Carga las imágenes térmica y digital para guardar el problema.", Toast.LENGTH_SHORT).show()
                    return
                }
                val navigationSave = editingElectricProblemId != null && problemNavList.isNotEmpty()
                if (navigationSave) {
                    if (!validateElectricForNavigation(formData)) return
                    val currentId = editingElectricProblemId ?: return
                    scope.launch {
                        if (isSavingElectricProblem) return@launch
                        isSavingElectricProblem = true
                        try {
                            val draft = ProblemDraft.ElectricDraft(
                                formData = formData,
                                thermalImage = pendingThermalImage,
                                digitalImage = pendingDigitalImage,
                                closed = electricProblemClosed
                            )
                            val saved = persistProblemDrafts(currentId, draft, currentUser?.idUsuario)
                            if (saved) {
                                resetElectricProblemState()
                                showElectricProblemDialog = false
                            }
                        } finally {
                            isSavingElectricProblem = false
                        }
                    }
                    return
                }
                val typeId = ELECTRIC_PROBLEM_TYPE_ID ?: "0D32B331-76C3-11D3-82BF-00104BC75DC2"
                val numero = pendingProblemNumber.toIntOrNull() ?: 1
                scope.launch {
                    if (isSavingElectricProblem) return@launch
                    isSavingElectricProblem = true
                    try {
                        val componentTemp = formData.componentTemperature.toDoubleOrNull()
                        val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                        val difference = componentTemp?.let { comp ->
                            referenceTemp?.let { ref -> comp - ref }
                        }
                        val severityId = calculateSeverityId(difference)
                        val failureLabel = formData.failureId?.let { id ->
                            electricHazardOptions.firstOrNull { it.first == id }?.second
                        }
                        val phaseLabel = formData.componentPhaseId?.let { id ->
                            electricPhaseOptions.firstOrNull { it.first == id }?.second
                        }
                        val emissivityInput = formData.emissivity.trim()
                        val emissivityValue = emissivityInput.replace(',', '.').toDoubleOrNull()
                        if (formData.emissivityChecked) {
                            if (emissivityValue == null || emissivityValue < 0.0 || emissivityValue > 1.0) {
                                Toast.makeText(
                                    ctx,
                                    "La emisividad debe estar entre 0.00 y 1.00.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }
                        }
                        val autoComment = buildList {
                            failureLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
                            phaseLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
                            pendingProblemEquipmentName?.takeIf { it.isNotBlank() }?.let { add(it) }
                        }.joinToString(", ")
                        val finalComment = formData.comments.takeIf { it.isNotBlank() } ?: autoComment
                        val detRow = withContext(Dispatchers.IO) {
                            runCatching {
                                inspeccionDetDao.getByUbicacion(locationId)
                                    .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                            }.getOrNull()
                        }
                        val detId = detRow?.idInspeccionDet
                        val nowTs = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        val editingElectricProblem = editingElectricProblemOriginal
                        val isEditingElectricProblem = editingElectricProblem != null && editingElectricProblemId != null
                        val problema = if (isEditingElectricProblem) {
                            editingElectricProblem!!.copy(
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = formData.componentPhaseId,
                                referencePhase = formData.referencePhaseId,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = formData.additionalInfoId,
                                additionalRms = formData.additionalRms.toDoubleOrNull(),
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = if (formData.indirectTempChecked) "on" else "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = if (formData.windSpeedChecked) "on" else "off",
                                windSpeed = formData.windSpeed.toDoubleOrNull(),
                                idFabricante = formData.manufacturerId,
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = if (electricProblemClosed) "Cerrado" else "Abierto",
                                cerradoEnInspeccion = if (electricProblemClosed) inspection.idInspeccion else editingElectricProblem.cerradoEnInspeccion,
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                modificadoPor = currentUser?.idUsuario,
                                fechaMod = nowTs
                            )
                        } else {
                            Problema(
                                idProblema = java.util.UUID.randomUUID().toString().uppercase(),
                                numeroProblema = numero,
                                idTipoInspeccion = typeId,
                                idSitio = inspection.idSitio,
                                idInspeccion = inspection.idInspeccion,
                                idInspeccionDet = detId,
                                idUbicacion = locationId,
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = formData.componentPhaseId,
                                referencePhase = formData.referencePhaseId,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = formData.additionalInfoId,
                                additionalRms = formData.additionalRms.toDoubleOrNull(),
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = if (formData.indirectTempChecked) "on" else "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = if (formData.windSpeedChecked) "on" else "off",
                                windSpeed = formData.windSpeed.toDoubleOrNull(),
                                idFabricante = formData.manufacturerId,
                                ratedLoadCheck = "off",
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltageCheck = "off",
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = "Abierto",
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                estatus = "Activo",
                                esCronico = "NO",
                                rpm = 0.0,
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                creadoPor = currentUser?.idUsuario,
                                fechaCreacion = nowTs
                            )
                        }
                        val result = withContext(Dispatchers.IO) {
                            if (isEditingElectricProblem) runCatching { problemaDao.update(problema) }
                            else runCatching { problemaDao.insert(problema) }
                        }
                        if (result.isSuccess) {
                            if (detRow != null) {
                                val updatedDet = detRow.copy(
                                    idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                    idEstatusColorText = 2,
                                    modificadoPor = currentUser?.idUsuario,
                                    fechaMod = nowTs
                                )
                                withContext(Dispatchers.IO) { runCatching { inspeccionDetDao.update(updatedDet) } }
                                updateParentStatusesAfterManualChange(
                                    inspectionId = inspection.idInspeccion,
                                    startUbicacionId = locationId,
                                    nowTs = nowTs
                                )
                            }
                            problemsRefreshTick++
                            refreshTree(preserveSelection = locationId)
                            val successMessage = if (isEditingElectricProblem)
                                "Problema eléctrico actualizado."
                            else
                                "Problema eléctrico guardado."
                            Toast.makeText(ctx, successMessage, Toast.LENGTH_SHORT).show()
                            resetElectricProblemState()
                            showElectricProblemDialog = false
                        } else {
                            val message = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                            Toast.makeText(
                                ctx,
                                "No se pudo guardar el problema eléctrico: $message",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {
                        isSavingElectricProblem = false
                    }
                }
            }

            fun saveMechanicalProblem(formData: MechanicalProblemFormData) {
                val inspection = currentInspection
                if (inspection == null) {
                    Toast.makeText(ctx, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                    return
                }
                val locationId = pendingProblemUbicacionId ?: selectedId
                if (locationId.isNullOrBlank()) {
                    Toast.makeText(ctx, "Selecciona un equipo.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (pendingThermalImage.isBlank() || pendingDigitalImage.isBlank()) {
                    Toast.makeText(ctx, "Carga las imágenes térmica y digital para guardar el problema.", Toast.LENGTH_SHORT).show()
                    return
                }
                val navigationSave = editingMechanicalProblemId != null && problemNavList.isNotEmpty()
                if (navigationSave) {
                    if (!validateMechanicalForNavigation(formData)) return
                    val currentId = editingMechanicalProblemId ?: return
                    scope.launch {
                        if (isSavingMechanicalProblem) return@launch
                        isSavingMechanicalProblem = true
                        try {
                            val draft = ProblemDraft.MechanicalDraft(
                                formData = formData,
                                thermalImage = pendingThermalImage,
                                digitalImage = pendingDigitalImage,
                                closed = mechanicalProblemClosed
                            )
                            val saved = persistProblemDrafts(currentId, draft, currentUser?.idUsuario)
                            if (saved) {
                                resetMechanicalProblemState()
                                showMechanicalProblemDialog = false
                            }
                        } finally {
                            isSavingMechanicalProblem = false
                        }
                    }
                    return
                }
                val typeId = MECHANICAL_PROBLEM_TYPE_ID ?: "0D32B334-76C3-11D3-82BF-00104BC75DC2"
                val numero = pendingProblemNumber.toIntOrNull() ?: 1
                scope.launch {
                    if (isSavingMechanicalProblem) return@launch
                    isSavingMechanicalProblem = true
                    try {
                        val componentTemp = formData.componentTemperature.toDoubleOrNull()
                        val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                        val difference = componentTemp?.let { comp ->
                            referenceTemp?.let { ref -> comp - ref }
                        }
                        val severityId = calculateSeverityId(difference)
                        val failureLabel = formData.failureId?.let { id ->
                            electricHazardOptions.firstOrNull { it.first == id }?.second
                        }
                        val emissivityInput = formData.emissivity.trim()
                        val emissivityValue = emissivityInput.replace(',', '.').toDoubleOrNull()
                        if (formData.emissivityChecked) {
                            if (emissivityValue == null || emissivityValue < 0.0 || emissivityValue > 1.0) {
                                Toast.makeText(
                                    ctx,
                                    "La emisividad debe estar entre 0.00 y 1.00.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }
                        }
                        val autoComment = buildList {
                            failureLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
                            pendingProblemEquipmentName?.takeIf { it.isNotBlank() }?.let { add(it) }
                        }.joinToString(", ")
                        val finalComment = formData.comments.takeIf { it.isNotBlank() } ?: autoComment

                        val detRow = withContext(Dispatchers.IO) {
                            runCatching {
                                inspeccionDetDao.getByUbicacion(locationId)
                                    .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                            }.getOrNull()
                        }
                        val detId = detRow?.idInspeccionDet
                        val nowTs = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        val editingProblem = editingMechanicalProblemOriginal
                        val isEditingMechanicalProblem = editingProblem != null && editingMechanicalProblemId != null
                        val problema = if (isEditingMechanicalProblem) {
                            editingProblem!!.copy(
                                numeroProblema = numero,
                                idInspeccionDet = detId,
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = null,
                                referencePhase = null,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = null,
                                additionalRms = null,
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = "off",
                                windSpeed = null,
                                idFabricante = formData.manufacturerId,
                                ratedLoadCheck = "off",
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltageCheck = "off",
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = if (mechanicalProblemClosed) "Cerrado" else "Abierto",
                                cerradoEnInspeccion = if (mechanicalProblemClosed) inspection.idInspeccion else editingProblem.cerradoEnInspeccion,
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                estatus = editingProblem.estatus ?: "Activo",
                                esCronico = editingProblem.esCronico ?: "NO",
                                rpm = formData.rpm.toDoubleOrNull(),
                                bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                modificadoPor = currentUser?.idUsuario,
                                fechaMod = nowTs
                            )
                        } else {
                            Problema(
                                idProblema = java.util.UUID.randomUUID().toString().uppercase(),
                                numeroProblema = numero,
                                idTipoInspeccion = typeId,
                                idSitio = inspection.idSitio,
                                idInspeccion = inspection.idInspeccion,
                                idInspeccionDet = detId,
                                idUbicacion = locationId,
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = null,
                                referencePhase = null,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = null,
                                additionalRms = null,
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = "off",
                                windSpeed = null,
                                idFabricante = formData.manufacturerId,
                                ratedLoadCheck = "off",
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltageCheck = "off",
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = "Abierto",
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                estatus = "Activo",
                                esCronico = "NO",
                                rpm = formData.rpm.toDoubleOrNull(),
                                bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                creadoPor = currentUser?.idUsuario,
                                fechaCreacion = nowTs
                            )
                        }
                        val result = withContext(Dispatchers.IO) {
                            if (isEditingMechanicalProblem) runCatching { problemaDao.update(problema) }
                            else runCatching { problemaDao.insert(problema) }
                        }
                        if (result.isSuccess) {
                            if (detRow != null) {
                                val updatedDet = detRow.copy(
                                    idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                    idEstatusColorText = 2,
                                    modificadoPor = currentUser?.idUsuario,
                                    fechaMod = nowTs
                                )
                                withContext(Dispatchers.IO) { runCatching { inspeccionDetDao.update(updatedDet) } }
                                updateParentStatusesAfterManualChange(
                                    inspectionId = inspection.idInspeccion,
                                    startUbicacionId = locationId,
                                    nowTs = nowTs
                                )
                            }
                            problemsRefreshTick++
                            refreshTree(preserveSelection = locationId)
                            val successMessage = if (isEditingMechanicalProblem)
                                "Problema mecánico actualizado."
                            else
                                "Problema mecánico guardado."
                            Toast.makeText(ctx, successMessage, Toast.LENGTH_SHORT).show()
                            resetMechanicalProblemState()
                            showMechanicalProblemDialog = false
                        } else {
                            val message = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                            Toast.makeText(
                                ctx,
                                "No se pudo guardar el problema mecánico: $message",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {
                        isSavingMechanicalProblem = false
                    }
                }
            }

            fun saveAislamientoTermicoProblem(formData: AislamientoTermicoProblemFormData) {
                val inspection = currentInspection
                if (inspection == null) {
                    Toast.makeText(ctx, "No hay inspección activa.", Toast.LENGTH_SHORT).show()
                    return
                }
                val locationId = pendingProblemUbicacionId ?: selectedId
                if (locationId.isNullOrBlank()) {
                    Toast.makeText(ctx, "Selecciona un equipo.", Toast.LENGTH_SHORT).show()
                    return
                }
                if (pendingThermalImage.isBlank() || pendingDigitalImage.isBlank()) {
                    Toast.makeText(ctx, "Carga las imágenes térmica y digital para guardar el problema.", Toast.LENGTH_SHORT).show()
                    return
                }
                val navigationSave = editingAislamientoTermicoProblemId != null && problemNavList.isNotEmpty()
                if (navigationSave) {
                    if (!validateAislamientoTermicoForNavigation(formData)) return
                    val currentId = editingAislamientoTermicoProblemId ?: return
                    scope.launch {
                        if (isSavingAislamientoTermicoProblem) return@launch
                        isSavingAislamientoTermicoProblem = true
                        try {
                            val draft = ProblemDraft.AislamientoTermicoDraft(
                                formData = formData,
                                thermalImage = pendingThermalImage,
                                digitalImage = pendingDigitalImage,
                                closed = aislamientoTermicoProblemClosed
                            )
                            val saved = persistProblemDrafts(currentId, draft, currentUser?.idUsuario)
                            if (saved) {
                                resetAislamientoTermicoProblemState()
                                showAislamientoTermicoProblemDialog = false
                            }
                        } finally {
                            isSavingAislamientoTermicoProblem = false
                        }
                    }
                    return
                }
                val typeId = AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID ?: "0D32B335-76C3-11D3-82BF-00104BC75DC2"
                val numero = pendingProblemNumber.toIntOrNull() ?: 1
                scope.launch {
                    if (isSavingAislamientoTermicoProblem) return@launch
                    isSavingAislamientoTermicoProblem = true
                    try {
                        val componentTemp = formData.componentTemperature.toDoubleOrNull()
                        val referenceTemp = formData.referenceTemperature.toDoubleOrNull()
                        val difference = componentTemp?.let { comp ->
                            referenceTemp?.let { ref -> comp - ref }
                        }
                        val severityId = calculateSeverityId(difference)
                        val failureLabel = formData.failureId?.let { id ->
                            electricHazardOptions.firstOrNull { it.first == id }?.second
                        }
                        val emissivityInput = formData.emissivity.trim()
                        val emissivityValue = emissivityInput.replace(',', '.').toDoubleOrNull()
                        if (formData.emissivityChecked) {
                            if (emissivityValue == null || emissivityValue < 0.0 || emissivityValue > 1.0) {
                                Toast.makeText(
                                    ctx,
                                    "La emisividad debe estar entre 0.00 y 1.00.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }
                        }
                        val autoComment = buildList {
                            failureLabel?.takeIf { it.isNotBlank() }?.let { add(it) }
                            pendingProblemEquipmentName?.takeIf { it.isNotBlank() }?.let { add(it) }
                        }.joinToString(", ")
                        val finalComment = formData.comments.takeIf { it.isNotBlank() } ?: autoComment

                        val detRow = withContext(Dispatchers.IO) {
                            runCatching {
                                inspeccionDetDao.getByUbicacion(locationId)
                                    .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                            }.getOrNull()
                        }
                        val detId = detRow?.idInspeccionDet
                        val nowTs = java.time.LocalDateTime.now()
                            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                        val editingProblem = editingAislamientoTermicoProblemOriginal
                        val isEditingProblem = editingProblem != null && editingAislamientoTermicoProblemId != null
                        val problema = if (isEditingProblem) {
                            editingProblem!!.copy(
                                numeroProblema = numero,
                                idInspeccionDet = detId,
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = null,
                                referencePhase = null,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = null,
                                additionalRms = null,
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = "off",
                                windSpeed = null,
                                idFabricante = formData.manufacturerId,
                                ratedLoadCheck = "off",
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltageCheck = "off",
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = if (aislamientoTermicoProblemClosed) "Cerrado" else "Abierto",
                                cerradoEnInspeccion = if (aislamientoTermicoProblemClosed) inspection.idInspeccion else editingProblem.cerradoEnInspeccion,
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                estatus = editingProblem.estatus ?: "Activo",
                                esCronico = editingProblem.esCronico ?: "NO",
                                rpm = formData.rpm.toDoubleOrNull(),
                                bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                modificadoPor = currentUser?.idUsuario,
                                fechaMod = nowTs
                            )
                        } else {
                            Problema(
                                idProblema = java.util.UUID.randomUUID().toString().uppercase(),
                                numeroProblema = numero,
                                idTipoInspeccion = typeId,
                                idSitio = inspection.idSitio,
                                idInspeccion = inspection.idInspeccion,
                                idInspeccionDet = detId,
                                idUbicacion = locationId,
                                problemTemperature = componentTemp,
                                referenceTemperature = referenceTemp,
                                problemPhase = null,
                                referencePhase = null,
                                problemRms = formData.componentRms.toDoubleOrNull(),
                                referenceRms = formData.referenceRms.toDoubleOrNull(),
                                additionalInfo = null,
                                additionalRms = null,
                                emissivityCheck = if (formData.emissivityChecked) "on" else "off",
                                emissivity = emissivityValue,
                                indirectTempCheck = "off",
                                tempAmbientCheck = if (formData.ambientTempChecked) "on" else "off",
                                tempAmbient = formData.ambientTemp.toDoubleOrNull(),
                                environmentCheck = if (formData.environmentChecked) "on" else "off",
                                environment = formData.environmentId,
                                windSpeedCheck = "off",
                                windSpeed = null,
                                idFabricante = formData.manufacturerId,
                                ratedLoadCheck = "off",
                                ratedLoad = formData.ratedLoad.takeIf { it.isNotBlank() },
                                circuitVoltageCheck = "off",
                                circuitVoltage = formData.circuitVoltage.takeIf { it.isNotBlank() },
                                idFalla = formData.failureId,
                                componentComment = finalComment,
                                estatusProblema = "Abierto",
                                aumentoTemperatura = difference,
                                idSeveridad = severityId,
                                ruta = pendingProblemRoute ?: pendingProblemEquipmentName ?: "-",
                                estatus = "Activo",
                                esCronico = "NO",
                                rpm = formData.rpm.toDoubleOrNull(),
                                bearingType = formData.bearingType.takeIf { it.isNotBlank() },
                                irFile = pendingThermalImage,
                                photoFile = pendingDigitalImage,
                                creadoPor = currentUser?.idUsuario,
                                fechaCreacion = nowTs
                            )
                        }
                        val result = withContext(Dispatchers.IO) {
                            if (isEditingProblem) runCatching { problemaDao.update(problema) }
                            else runCatching { problemaDao.insert(problema) }
                        }
                        if (result.isSuccess) {
                            if (detRow != null) {
                                val updatedDet = detRow.copy(
                                    idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                    idEstatusColorText = 2,
                                    modificadoPor = currentUser?.idUsuario,
                                    fechaMod = nowTs
                                )
                                withContext(Dispatchers.IO) { runCatching { inspeccionDetDao.update(updatedDet) } }
                                updateParentStatusesAfterManualChange(
                                    inspectionId = inspection.idInspeccion,
                                    startUbicacionId = locationId,
                                    nowTs = nowTs
                                )
                            }
                            problemsRefreshTick++
                            refreshTree(preserveSelection = locationId)
                            val successMessage = if (isEditingProblem)
                                "Problema aislamiento térrmico actualizado."
                            else
                                "Problema aislamiento térrmico guardado."
                            Toast.makeText(ctx, successMessage, Toast.LENGTH_SHORT).show()
                            resetAislamientoTermicoProblemState()
                            showAislamientoTermicoProblemDialog = false
                        } else {
                            val message = result.exceptionOrNull()?.localizedMessage ?: "Error desconocido"
                            Toast.makeText(
                                ctx,
                                "No se pudo guardar el problema aislamiento térrmico: $message",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } finally {
                        isSavingAislamientoTermicoProblem = false
                    }
                }
            }

            LaunchedEffect(showNewUbDialog) {
                if (showNewUbDialog) {
                    // Siempre que se abre \"Nueva ubicacion\", forzamos modo creacion (no edicion)
                    editingUbId = null
                    editingParentId = null
                    editingDetId = null
                    editingInspId = null

                    // Limpiar todos los campos del formulario
                    locationForm.resetForNew()

                    val defaultId = "568798D1-76BB-11D3-82BF-00104BC75DC2"
                    val match = statusOptions.firstOrNull { it.idStatusInspeccionDet.equals(defaultId, true) }
                    if (match != null) {
                        locationForm.statusId = match.idStatusInspeccionDet
                        locationForm.statusLabel = match.estatusInspeccionDet ?: match.idStatusInspeccionDet
                    } else {
                        locationForm.statusId = null
                        locationForm.statusLabel = ""
                    }
                    // Fijar usuario actual leido en composicion
                    currentUserId = currentUser?.idUsuario
                } else {
                    isSavingNewUb = false
                    locationForm.error = null
                }
            }
            fun applyNewUbDefaults() {
                locationForm.resetForNew()
                val defaultId = "568798D1-76BB-11D3-82BF-00104BC75DC2"
                val match = statusOptions.firstOrNull { it.idStatusInspeccionDet.equals(defaultId, true) }
                if (match != null) {
                    locationForm.statusId = match.idStatusInspeccionDet
                    locationForm.statusLabel = match.estatusInspeccionDet ?: match.idStatusInspeccionDet
                } else {
                    locationForm.statusId = null
                    locationForm.statusLabel = ""
                }
                val priorityMatch = prioridadOptions.firstOrNull {
                    it.idTipoPrioridad.equals(DEFAULT_PRIORIDAD_ID, true) ||
                        it.tipoPrioridad?.equals("CTO", true) == true
                }
                if (priorityMatch != null) {
                    locationForm.prioridadId = priorityMatch.idTipoPrioridad
                    locationForm.prioridadLabel = priorityMatch.tipoPrioridad ?: priorityMatch.idTipoPrioridad
                }
            }
            LaunchedEffect(showNewUbDialog, prioridadOptions) {
                if (showNewUbDialog && locationForm.prioridadId == null) {
                    val match = prioridadOptions.firstOrNull {
                        it.idTipoPrioridad.equals(DEFAULT_PRIORIDAD_ID, true) ||
                            it.tipoPrioridad?.equals("CTO", true) == true
                    }
                    if (match != null) {
                        locationForm.prioridadId = match.idTipoPrioridad
                        locationForm.prioridadLabel = match.tipoPrioridad ?: match.idTipoPrioridad
                    }
                }
            }

            fun triggerSearch() {
                keyboardController?.hide()
                searchMessage = null
                val code = barcode.trim()
                if (code.isEmpty()) return
                val path = findPathByBarcode(nodes, code)
                if (path == null) {
                    searchMessage = "No hay elementos con ese Código De Barras"
                } else {
                    // expandir ancestros y seleccionar objetivo
                    path.dropLast(1).forEach { id -> if (!expanded.contains(id)) expanded.add(id) }
                    val targetId = path.last()
                    selectedId = targetId
                    highlightedId = targetId
                    scrollToNodeId = targetId
                    scope.launch {
                        delay(3000)
                        if (highlightedId == targetId) highlightedId = null
                    }
                }
            }
            LaunchedEffect(selectedId) {
                checkedStatusLocationIds.clear()
            }

            InspectionHeader(
                barcode = barcode,
                onBarcodeChange = { barcode = it },
                onSearch = { triggerSearch() },
                selectedStatusId = selectedStatusId,
                statusOptions = statusOptions,
                onStatusSelected = { opt ->
                    selectedStatusId = opt?.idStatusInspeccionDet
                },
                onApplyStatus = {
                    val statusId = selectedStatusId
                    if (statusId.isNullOrBlank()) {
                        Toast.makeText(ctx, "Seleccionar estatus", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            val targets = if (checkedStatusLocationIds.isNotEmpty()) {
                                checkedStatusLocationIds.toList()
                            } else {
                                listOfNotNull(selectedId)
                            }
                            applyManualStatusToUbicacionIds(statusId, targets)
                        }
                    }
                },
                onClickNewLocation = {
                    if (selectedId == null) {
                        showNoSelectionDialog = true
                    } else {
                        val selectedNode = findById(selectedId, nodes)
                        if (selectedNode?.verified == true) {
                            showInvalidParentDialog = true
                        } else {
                            searchMessage = null
                            showNewUbDialog = true
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )

            if (searchMessage != null) {
                AlertDialog(
                    onDismissRequest = { searchMessage = null },
                    confirmButton = {
                        Button(onClick = { searchMessage = null }) { Text("Aceptar") }
                    },
                    title = { Text("Informacion") },
                    text = { Text(searchMessage!!) }
                )
            }
            Divider(thickness = DIVIDER_THICKNESS)

            // dialogo informativo para errores al borrar ubicacion
            if (deleteUbInfoMessage != null) {
                AlertDialog(
                    onDismissRequest = { deleteUbInfoMessage = null },
                    confirmButton = {
                        Button(onClick = { deleteUbInfoMessage = null }) { Text("Aceptar") }
                    },
                    title = { Text("Informacion") },
                    text = { Text(deleteUbInfoMessage!!) }
                )
            }

            // Dialogo cuando se intenta crear una ubicacion debajo de un equipo
            if (showInvalidParentDialog) {
                AlertDialog(
                    onDismissRequest = { showInvalidParentDialog = false },
                    confirmButton = {
                        Button(onClick = { showInvalidParentDialog = false }) { Text("Aceptar") }
                    },
                    title = { Text("Informacion") },
                    text = { Text("Solo puede crear elementos dentro de ubicaciones.") }
                )
            }

            // dialogo cuando no hay ubicacion seleccionada
            if (showNoSelectionDialog) {
                AlertDialog(
                    onDismissRequest = { showNoSelectionDialog = false },
                    confirmButton = {
                        Button(onClick = { showNoSelectionDialog = false }) { Text("Aceptar") }
                    },
                    title = { Text("Informacion") },
                    text = { Text("Debes seleccionar una ubicacion para agregar un nuevo elemento.") }
                )
            }

            if (showProblemTypeDialog) {
                AlertDialog(
                    onDismissRequest = { showProblemTypeDialog = false },
                    confirmButton = {
                        Button(onClick = {
                            val selectedTypeId = problemTypeIdFromLabel(selectedProblemType)
                            val visualTypeId = VISUAL_PROBLEM_TYPE_ID
                            val electricTypeId = ELECTRIC_PROBLEM_TYPE_ID
                            val mechanicalTypeId = MECHANICAL_PROBLEM_TYPE_ID
                            val aislamientoTypeId = AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID
                            when {
                                selectedTypeId != null && visualTypeId != null && selectedTypeId.equals(visualTypeId, ignoreCase = true) -> {
                                    scope.launch {
                                        showProblemTypeDialog = false
                                        resetVisualProblemForm()
                                        pendingProblemType = problemTypeLabelForId(visualTypeId)
                                        if (pendingProblemEquipmentName.isNullOrBlank()) {
                                            val node = selectedId?.let { findById(it, nodes) }
                                            pendingProblemEquipmentName = node?.title ?: "-"
                                            pendingProblemUbicacionId = node?.id ?: selectedId
                                        }
                                        if (pendingProblemRoute.isNullOrBlank()) {
                                            pendingProblemRoute = selectedId?.let { titlePathForId(nodes, it).joinToString(" / ") } ?: "-"
                                        }
                                        if (pendingProblemUbicacionId.isNullOrBlank()) {
                                            pendingProblemUbicacionId = selectedId
                                        }
                                        pendingProblemNumber = fetchNextProblemNumber(visualTypeId)
                                        ensureVisualDefaults()
                                        showVisualInspectionDialog = true
                                    }
                                }
                                selectedTypeId != null && electricTypeId != null && selectedTypeId.equals(electricTypeId, ignoreCase = true) -> {
                                    scope.launch {
                                        showProblemTypeDialog = false
                                        resetElectricProblemState()
                                        val globalDefaults = loadLastProblemDefaultsGlobalForThermalTypes()
                                        val electricDefaults = loadLastProblemDefaultsByType(electricTypeId)
                                        val base = electricDefaults?.let { toElectricRememberedFields(it) }
                                            ?: ElectricProblemFormData()
                                        electricProblemDraftData = applySharedRememberedFields(
                                            source = globalDefaults,
                                            base = base
                                        )
                                        pendingProblemType = problemTypeLabelForId(electricTypeId)
                                        if (pendingProblemEquipmentName.isNullOrBlank()) {
                                            val node = selectedId?.let { findById(it, nodes) }
                                            pendingProblemEquipmentName = node?.title ?: "-"
                                            pendingProblemUbicacionId = node?.id ?: selectedId
                                        }
                                        if (pendingProblemRoute.isNullOrBlank()) {
                                            pendingProblemRoute = selectedId?.let { titlePathForId(nodes, it).joinToString(" / ") } ?: "-"
                                        }
                                        if (pendingProblemUbicacionId.isNullOrBlank()) {
                                            pendingProblemUbicacionId = selectedId
                                        }
                                        pendingProblemNumber = fetchNextProblemNumber(electricTypeId)
                                        electricProblemFormKey += 1
                                        showElectricProblemDialog = true
                                    }
                                }
                                selectedTypeId != null && mechanicalTypeId != null && selectedTypeId.equals(mechanicalTypeId, ignoreCase = true) -> {
                                scope.launch {
                                    showProblemTypeDialog = false
                                    val node = selectedId?.let { findById(it, nodes) }
                                    resetMechanicalProblemState()
                                        val globalDefaults = loadLastProblemDefaultsGlobalForThermalTypes()
                                        val mechanicalDefaults = loadLastProblemDefaultsByType(mechanicalTypeId)
                                        val base = mechanicalDefaults?.let { toMechanicalRememberedFields(it) }
                                            ?: MechanicalProblemFormData()
                                        mechanicalProblemDraftData = applySharedRememberedFields(
                                            source = globalDefaults,
                                            base = base
                                        )
                                        pendingProblemType = problemTypeLabelForId(mechanicalTypeId)
                                        if (pendingProblemEquipmentName.isNullOrBlank()) {
                                            pendingProblemEquipmentName = node?.title ?: "-"
                                            pendingProblemUbicacionId = node?.id ?: selectedId
                                        }
                                        if (pendingProblemRoute.isNullOrBlank()) {
                                            pendingProblemRoute = selectedId?.let { titlePathForId(nodes, it).joinToString(" / ") } ?: "-"
                                        }
                                        if (pendingProblemUbicacionId.isNullOrBlank()) {
                                            pendingProblemUbicacionId = selectedId
                                        }
                                        pendingProblemNumber = fetchNextProblemNumber(mechanicalTypeId)
                                        mechanicalProblemFormKey += 1
                                        showMechanicalProblemDialog = true
                                    }
                                }
                                selectedTypeId != null && aislamientoTypeId != null && selectedTypeId.equals(aislamientoTypeId, ignoreCase = true) -> {
                                    scope.launch {
                                        showProblemTypeDialog = false
                                        val node = selectedId?.let { findById(it, nodes) }
                                        resetAislamientoTermicoProblemState()
                                        val globalDefaults = loadLastProblemDefaultsGlobalForThermalTypes()
                                        val aislamientoDefaults = loadLastProblemDefaultsByType(aislamientoTypeId)
                                        val base = aislamientoDefaults?.let { toAislamientoRememberedFields(it) }
                                            ?: AislamientoTermicoProblemFormData()
                                        aislamientoTermicoProblemDraftData = applySharedRememberedFields(
                                            source = globalDefaults,
                                            base = base
                                        )
                                        pendingProblemType = problemTypeLabelForId(aislamientoTypeId)
                                        if (pendingProblemEquipmentName.isNullOrBlank()) {
                                            pendingProblemEquipmentName = node?.title ?: "-"
                                            pendingProblemUbicacionId = node?.id ?: selectedId
                                        }
                                        if (pendingProblemRoute.isNullOrBlank()) {
                                            pendingProblemRoute = selectedId?.let { titlePathForId(nodes, it).joinToString(" / ") } ?: "-"
                                        }
                                        if (pendingProblemUbicacionId.isNullOrBlank()) {
                                            pendingProblemUbicacionId = selectedId
                                        }
                                        pendingProblemNumber = fetchNextProblemNumber(aislamientoTypeId)
                                        aislamientoTermicoProblemFormKey += 1
                                        showAislamientoTermicoProblemDialog = true
                                    }
                                }                                else -> {
                                    showProblemTypeDialog = false
                                    Toast.makeText(ctx, "Selecciona un tipo válido.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showProblemTypeDialog = false }) { Text("Cancelar") }
                    },
                    title = { Text("Tipo de problema") },
                    text = {
                        Column {
                            Text("Selecciona un tipo:")
                            val options = PROBLEM_TYPE_IDS.entries.toList()
                            options.forEach { (_, id) ->
                                val displayLabel = problemTypeLabelForId(id)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedProblemType.equals(displayLabel, ignoreCase = true),
                                        onClick = { selectedProblemType = displayLabel }
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(displayLabel)
                                }
                            }
                        }
                    }
                )
            }

            if (showBaselineRestrictionDialog) {
                AlertDialog(
                    onDismissRequest = { showBaselineRestrictionDialog = false },
                    confirmButton = {
                        Button(onClick = { showBaselineRestrictionDialog = false }) { Text("Aceptar") }
                    },
                    title = { Text("No se puede crear hallazgos") },
                    text = { Text("Este equipo contiene Baseline") }
                )
            }

            if (showVisualInspectionWarning) {
                AlertDialog(
                    onDismissRequest = { showVisualInspectionWarning = false },
                    title = { Text("Inspección visual") },
                    text = {
                        Text(
                            "Está a punto de crear un registro de inspección visual.\n\n" +
                                    "Actualmente tiene una ubicación seleccionada (en lugar de equipo). " +
                                    "Solo puede crear registros de inspección visual para ubicaciones.\n\n" +
                                    "¿Le gustaría agregar un registro de inspección visual?"
                        )
                    },
                    confirmButton = {
                        Button(onClick = {
                            showVisualInspectionWarning = false
                            showVisualInspectionDialog = true
                        }) { Text("Continuar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showVisualInspectionWarning = false }) { Text("Cancelar") }
                    }
                )
            }

            val canNavigatePrevious = problemNavList.isNotEmpty() && problemNavIndex > 0
            val canNavigateNext =
                problemNavList.isNotEmpty() && problemNavIndex >= 0 && problemNavIndex < problemNavList.lastIndex

            if (showVisualInspectionDialog && pendingProblemEquipmentName != null) {
                val visualHazardOptionsForDialog = remember(
                    visualHazardOptionsFixed,
                    hazardOptionsByType,
                    pendingHazardId,
                    pendingHazardLabel
                ) {
                    val pendingId = pendingHazardId
                    if (pendingId.isNullOrBlank()) {
                        visualHazardOptionsFixed
                    } else {
                        val preferredLabel = pendingHazardLabel
                            ?.takeIf { it.isNotBlank() && !it.equals(pendingId, ignoreCase = true) }
                        if (preferredLabel != null) {
                            val replaced = visualHazardOptionsFixed.map { option ->
                                if (option.first.equals(pendingId, ignoreCase = true)) {
                                    pendingId to preferredLabel
                                } else {
                                    option
                                }
                            }
                            val hasEntry = replaced.any { it.first.equals(pendingId, ignoreCase = true) }
                            if (hasEntry) replaced else replaced + (pendingId to preferredLabel)
                        } else {
                            val exists = visualHazardOptionsFixed.any {
                                it.first.equals(pendingId, ignoreCase = true)
                            }
                            if (exists) {
                                visualHazardOptionsFixed
                            } else {
                                val catalogLabel = VISUAL_PROBLEM_TYPE_ID?.let { typeId ->
                                    hazardOptionsByType[typeId]
                                        ?.firstOrNull { it.first.equals(pendingId, ignoreCase = true) }
                                        ?.second
                                }
                                val label = catalogLabel ?: pendingId
                                visualHazardOptionsFixed + (pendingId to label)
                            }
                        }
                    }
                }
                val visualSeverityOptionsForDialog = remember(
                    visualSeverityOptions,
                    severityCatalog,
                    pendingSeverityId,
                    pendingSeverityLabel
                ) {
                    val pendingId = pendingSeverityId
                    if (pendingId.isNullOrBlank()) {
                        val label = pendingSeverityLabel
                        if (label.isNullOrBlank()) {
                            visualSeverityOptions
                        } else {
                            val exists = visualSeverityOptions.any {
                                it.second.equals(label, ignoreCase = true)
                            }
                            if (exists) {
                                visualSeverityOptions
                            } else {
                                visualSeverityOptions + (label to label)
                            }
                        }
                    } else {
                        val exists = visualSeverityOptions.any { it.first.equals(pendingId, ignoreCase = true) }
                        if (exists) {
                            visualSeverityOptions
                        } else {
                            val catalogLabel = severityCatalog.firstOrNull {
                                it.idSeveridad.equals(pendingId, ignoreCase = true)
                            }?.severidad
                            val label = pendingSeverityLabel?.takeIf { it.isNotBlank() }
                                ?: catalogLabel
                                ?: pendingId
                            visualSeverityOptions + (pendingId to label)
                        }
                    }
                }
                VisualProblemDialog(
                    inspectionNumber = pendingInspectionNumber,
                    problemNumber = pendingProblemNumber,
                    problemType = pendingProblemType,
                    equipmentName = pendingProblemEquipmentName ?: "-",
                    equipmentRoute = pendingProblemRoute ?: "-",
                    hazardIssues = visualHazardOptionsForDialog,
                    severities = visualSeverityOptionsForDialog,
                    selectedHazardIssue = pendingHazardId ?: pendingHazardLabel,
                    selectedSeverity = pendingSeverityId ?: pendingSeverityLabel,
                    observations = pendingObservation,
                    onObservationsChange = { pendingObservation = it },
                    historyRows = visualProblemHistory,
                    historyLoading = isHistoryLoading,
                    thermalImageName = pendingThermalImage,
                    digitalImageName = pendingDigitalImage,
                    onThermalImageChange = { pendingThermalImage = it },
                    onDigitalImageChange = { pendingDigitalImage = it },
                    onThermalSequenceUp = { pendingThermalImage = adjustImageSequence(pendingThermalImage, +1) },
                    onThermalSequenceDown = { pendingThermalImage = adjustImageSequence(pendingThermalImage, -1) },
                    onDigitalSequenceUp = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, +1) },
                    onDigitalSequenceDown = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, -1) },
                    onThermalPickInitial = { loadInitialImageFromInspection(true) { pendingThermalImage = it } },
                    onDigitalPickInitial = { loadInitialImageFromInspection(false) { pendingDigitalImage = it } },
                    onThermalFolder = { thermalFolderLauncher.launch("image/*") },
                    onDigitalFolder = { digitalFolderLauncher.launch("image/*") },
                    onThermalCamera = { thermalCameraLauncher.launch(null) },
                    onDigitalCamera = { digitalCameraLauncher.launch(null) },
                    onCronicoClick = {
                        val entity = editingProblemOriginal ?: return@VisualProblemDialog
                        pendingCronicoEntity = entity
                        showCronicoConfirmDialog = true
                    },
                    cronicoEnabled = cronicoActionEnabled && !isSavingCronico,
                    cronicoChecked = editingProblemOriginal?.esCronico?.equals("SI", ignoreCase = true) == true,
                    cerradoChecked = visualProblemClosed,
                    cerradoEnabled = (
                        editingProblemOriginal?.estatusProblema?.equals("Cerrado", ignoreCase = true) != true
                    ) || (
                        editingProblemOriginal?.idInspeccion
                            ?.equals(currentInspection?.idInspeccion, ignoreCase = true) == true
                    ),
                    onCerradoChange = { visualProblemClosed = it },
                    onNavigatePrevious = if (editingProblemId != null) {
                        { navigateFromVisual(-1) }
                    } else {
                        null
                    },
                    onNavigateNext = if (editingProblemId != null) {
                        { navigateFromVisual(1) }
                    } else {
                        null
                    },
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    onHazardSelected = { selected ->
                        val normalized = selected.takeIf { it.isNotBlank() }
                        pendingHazardId = normalized
                        pendingHazardLabel = visualHazardOptionsForDialog.firstOrNull {
                            it.first.equals(normalized, ignoreCase = true)
                        }?.second
                        pendingObservation = buildVisualObservation(normalized, pendingProblemEquipmentName)
                    },
                    onSeveritySelected = { selected ->
                        val normalized = selected.takeIf { it.isNotBlank() }
                        pendingSeverityId = normalized
                        pendingSeverityLabel = visualSeverityOptionsForDialog.firstOrNull {
                            it.first.equals(normalized, ignoreCase = true) ||
                                it.second.equals(normalized, ignoreCase = true)
                        }?.second
                    },
                    showEditControls = editingProblemId != null,
                    selectedTabIndex = problemDialogTab,
                    onSelectedTabChange = { problemDialogTab = it },
                    transitionKey = editingProblemId ?: pendingProblemNumber,
                    onDismiss = {
                        showVisualInspectionDialog = false
                        cronicoActionEnabled = false
                        resetProblemNavigation()
                        resetVisualProblemForm()
                    },
                    onContinue = {
                        if (isSavingVisualProblem) return@VisualProblemDialog
                        val navigationSave = editingProblemId != null && problemNavList.isNotEmpty()
                        if (navigationSave) {
                            if (!validateVisualForNavigation()) return@VisualProblemDialog
                            val currentId = editingProblemId ?: return@VisualProblemDialog
                            scope.launch {
                                if (isSavingVisualProblem) return@launch
                                isSavingVisualProblem = true
                                try {
                                    val draft = ProblemDraft.VisualDraft(
                                        hazardId = pendingHazardId,
                                        hazardLabel = pendingHazardLabel,
                                        severityId = pendingSeverityId,
                                        severityLabel = pendingSeverityLabel,
                                        observation = pendingObservation,
                                        thermalImage = pendingThermalImage,
                                        digitalImage = pendingDigitalImage,
                                        closed = visualProblemClosed
                                    )
                                    val saved = persistProblemDrafts(currentId, draft, currentUser?.idUsuario)
                                    if (saved) {
                                        showVisualInspectionDialog = false
                                        cronicoActionEnabled = false
                                        resetVisualProblemForm()
                                    }
                                } finally {
                                    isSavingVisualProblem = false
                                }
                            }
                            return@VisualProblemDialog
                        }
                        val inspection = currentInspection ?: return@VisualProblemDialog
                        val ubicacionId = pendingProblemUbicacionId ?: selectedId
                        val hazardId = pendingHazardId
                        val severityId = pendingSeverityId
                        val thermal = pendingThermalImage
                        val digital = pendingDigitalImage
                        val typeId = PROBLEM_TYPE_IDS["Visual"]
                        if (typeId == null) {
                            Toast.makeText(ctx, "No se encontró el tipo Visual.", Toast.LENGTH_SHORT).show()
                            return@VisualProblemDialog
                        }
                        if (ubicacionId.isNullOrBlank()) {
                            Toast.makeText(ctx, "Selecciona un equipo.", Toast.LENGTH_SHORT).show()
                            return@VisualProblemDialog
                        }
                        val missing = buildList {
                            if (hazardId.isNullOrBlank()) add("Problema")
                            if (severityId.isNullOrBlank()) add("Severidad")
                            if (thermal.isBlank()) add("Imagen térmica")
                            if (digital.isBlank()) add("Imagen digital")
                        }
                        if (missing.isNotEmpty()) {
                            Toast.makeText(ctx, "Completa: ${missing.joinToString()}", Toast.LENGTH_SHORT).show()
                            return@VisualProblemDialog
                        }
                        scope.launch {
                            if (isSavingVisualProblem) return@launch
                            isSavingVisualProblem = true
                            try {
                                val editingId = editingProblemId
                                val resolvedRoute = pendingProblemRoute
                                    ?: ubicacionId?.let { titlePathForId(nodes, it).joinToString(" / ") }
                                    ?: "-"
                                val hazardLabel = visualHazardOptionsFixed.firstOrNull { it.first == hazardId }?.second
                                val comment = pendingObservation.ifBlank {
                                    buildVisualObservation(hazardId, pendingProblemEquipmentName)
                                }
                                val nowTs = java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                val detRow = withContext(Dispatchers.IO) {
                                    runCatching {
                                        inspeccionDetDao.getByUbicacion(ubicacionId)
                                            .firstOrNull { it.idInspeccion == inspection.idInspeccion }
                                    }.getOrNull()
                                }
                                val detId = detRow?.idInspeccionDet
                                val operationResult = if (editingId == null) {
                                    val numero = pendingProblemNumber.toIntOrNull()
                                        ?: fetchNextProblemNumber(VISUAL_PROBLEM_TYPE_ID).toIntOrNull()
                                        ?: 1
                                    val problema = Problema(
                                        idProblema = java.util.UUID.randomUUID().toString().uppercase(),
                                        numeroProblema = numero,
                                        idTipoInspeccion = typeId,
                                        idSitio = inspection.idSitio,
                                        idInspeccion = inspection.idInspeccion,
                                        idInspeccionDet = detId,
                                        idUbicacion = ubicacionId,
                                        hazardIssue = hazardId,
                                        idFalla = hazardId,
                                        idSeveridad = severityId,
                                        componentComment = comment,
                                        ruta = resolvedRoute,
                                        estatusProblema = "Abierto",
                                        esCronico = "NO",
                                        cerradoEnInspeccion = "No",
                                        estatus = "Activo",
                                        irFile = thermal,
                                        photoFile = digital,
                                        creadoPor = currentUser?.idUsuario,
                                        fechaCreacion = nowTs
                                    )
                                    runCatching {
                                        withContext(Dispatchers.IO) { problemaDao.insert(problema) }
                                    }
                                } else {
                                    val base = editingProblemOriginal ?: withContext(Dispatchers.IO) {
                                        runCatching { problemaDao.getById(editingId) }.getOrNull()
                                    }
                                    if (base == null) {
                                        Toast.makeText(ctx, "No se pudo cargar el problema a editar.", Toast.LENGTH_SHORT).show()
                                        null
                                    } else {
                                        val updated = base.copy(
                                            idInspeccionDet = detId,
                                            idUbicacion = ubicacionId,
                                            hazardIssue = hazardId,
                                            idFalla = hazardId,
                                            idSeveridad = severityId,
                                            componentComment = comment,
                                            ruta = resolvedRoute,
                                            estatusProblema = if (visualProblemClosed) "Cerrado" else "Abierto",
                                            cerradoEnInspeccion = if (visualProblemClosed) inspection.idInspeccion else base.cerradoEnInspeccion,
                                            irFile = thermal,
                                            photoFile = digital,
                                            modificadoPor = currentUser?.idUsuario,
                                            fechaMod = nowTs
                                        )
                                        runCatching {
                                            withContext(Dispatchers.IO) { problemaDao.update(updated) }
                                        }
                                    }
                                } ?: return@launch
                                if (operationResult.isSuccess) {
                                    if (detRow != null) {
                                        val updatedDet = detRow.copy(
                                            idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                            idEstatusColorText = 2,
                                            modificadoPor = currentUser?.idUsuario,
                                            fechaMod = nowTs
                                        )
                                        runCatching {
                                            withContext(Dispatchers.IO) { inspeccionDetDao.update(updatedDet) }
                                        }
                                        updateParentStatusesAfterManualChange(
                                            inspectionId = inspection.idInspeccion,
                                            startUbicacionId = ubicacionId,
                                            nowTs = nowTs
                                        )
                                    }
                                    updateInspectionInitialImages(thermal, digital)
                                    showVisualInspectionDialog = false
                                    pendingProblemNumber = fetchNextProblemNumber(VISUAL_PROBLEM_TYPE_ID)
                                    problemsRefreshTick++
                                    Toast.makeText(
                                        ctx,
                                        if (editingId == null) "Problema visual guardado." else "Problema visual actualizado.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    refreshTree(preserveSelection = ubicacionId)
                                    resetVisualProblemForm()
                                } else {
                                    val message = operationResult.exceptionOrNull()?.localizedMessage
                                        ?: "Error desconocido"
                                    Toast.makeText(
                                        ctx,
                                        "No se pudo guardar el problema visual: $message",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } finally {
                                isSavingVisualProblem = false
                            }
                        }
                    }
                )
            }

            val electricProblemInitialData = electricProblemDraftData
                ?: editingElectricProblemOriginal?.let { entity -> toElectricFormData(entity) }
            val mechanicalProblemInitialData = mechanicalProblemDraftData
                ?: editingMechanicalProblemOriginal?.let { entity -> toMechanicalFormData(entity) }
            val aislamientoTermicoProblemInitialData = aislamientoTermicoProblemDraftData
                ?: editingAislamientoTermicoProblemOriginal?.let { entity -> toAislamientoFormData(entity) }
            val manufacturerOptionsPairs = fabricanteOptions
                .sortedBy { it.fabricante?.lowercase(Locale.getDefault()) ?: "" }
                .map { it.idFabricante to (it.fabricante ?: it.idFabricante) }
            if (showElectricProblemDialog && pendingProblemEquipmentName != null) {
                ElectricProblemDialog(
                    inspectionNumber = pendingInspectionNumber,
                    problemNumber = pendingProblemNumber,
                    problemType = pendingProblemType,
                    equipmentName = pendingProblemEquipmentName ?: "-",
                    equipmentRoute = pendingProblemRoute ?: "-",
                    failureOptions = electricHazardOptions,
                    phaseOptions = electricPhaseOptions,
                    environmentOptions = electricEnvironmentOptions,
                    manufacturerOptions = manufacturerOptionsPairs,
                    thermalImageName = pendingThermalImage,
                    digitalImageName = pendingDigitalImage,
                    onThermalImageChange = { pendingThermalImage = it },
                    onDigitalImageChange = { pendingDigitalImage = it },
                    onThermalSequenceUp = { pendingThermalImage = adjustImageSequence(pendingThermalImage, +1) },
                    onThermalSequenceDown = { pendingThermalImage = adjustImageSequence(pendingThermalImage, -1) },
                    onDigitalSequenceUp = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, +1) },
                    onDigitalSequenceDown = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, -1) },
                    onThermalPickInitial = { loadInitialImageFromInspection(true) { pendingThermalImage = it } },
                    onDigitalPickInitial = { loadInitialImageFromInspection(false) { pendingDigitalImage = it } },
                    onThermalFolder = { thermalFolderLauncher.launch("image/*") },
                    onDigitalFolder = { digitalFolderLauncher.launch("image/*") },
                    onThermalCamera = { thermalCameraLauncher.launch(null) },
                    onDigitalCamera = { digitalCameraLauncher.launch(null) },
                    onCronicoClick = {
                        val entity = editingElectricProblemOriginal ?: return@ElectricProblemDialog
                        pendingCronicoEntity = entity
                        showCronicoConfirmDialog = true
                    },
                    cronicoEnabled = cronicoActionEnabled && !isSavingCronico,
                    cronicoChecked = editingElectricProblemOriginal?.esCronico?.equals("SI", ignoreCase = true) == true,
                    cerradoChecked = electricProblemClosed,
                    cerradoEnabled = (
                        editingElectricProblemOriginal?.estatusProblema?.equals("Cerrado", ignoreCase = true) != true
                    ) || (
                        editingElectricProblemOriginal?.idInspeccion
                            ?.equals(currentInspection?.idInspeccion, ignoreCase = true) == true
                    ),
                    onCerradoChange = { electricProblemClosed = it },
                    onNavigatePrevious = if (editingElectricProblemId != null) {
                        { data -> navigateFromElectric(-1, data) }
                    } else {
                        null
                    },
                    onNavigateNext = if (editingElectricProblemId != null) {
                        { data -> navigateFromElectric(1, data) }
                    } else {
                        null
                    },
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    showEditControls = editingElectricProblemId != null,
                    selectedTabIndex = problemDialogTab,
                    onSelectedTabChange = { problemDialogTab = it },
                    transitionKey = editingElectricProblemId ?: pendingProblemNumber,
                    onDismiss = {
                        showElectricProblemDialog = false
                        cronicoActionEnabled = false
                        resetProblemNavigation()
                        resetElectricProblemState()
                    },
                    onContinue = { saveElectricProblem(it) },
                    continueEnabled = !isSavingElectricProblem,
                    initialFormData = electricProblemInitialData,
                    dialogKey = electricProblemFormKey
                )
            }
            if (showMechanicalProblemDialog && pendingProblemEquipmentName != null) {
                MechanicalProblemDialog(
                    inspectionNumber = pendingInspectionNumber,
                    problemNumber = pendingProblemNumber,
                    problemType = pendingProblemType,
                    equipmentName = pendingProblemEquipmentName ?: "-",
                    equipmentRoute = pendingProblemRoute ?: "-",
                    failureOptions = electricHazardOptions,
                    phaseOptions = electricPhaseOptions,
                    environmentOptions = electricEnvironmentOptions,
                    manufacturerOptions = manufacturerOptionsPairs,
                    thermalImageName = pendingThermalImage,
                    digitalImageName = pendingDigitalImage,
                    onThermalImageChange = { pendingThermalImage = it },
                    onDigitalImageChange = { pendingDigitalImage = it },
                    onThermalSequenceUp = { pendingThermalImage = adjustImageSequence(pendingThermalImage, +1) },
                    onThermalSequenceDown = { pendingThermalImage = adjustImageSequence(pendingThermalImage, -1) },
                    onDigitalSequenceUp = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, +1) },
                    onDigitalSequenceDown = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, -1) },
                    onThermalPickInitial = { loadInitialImageFromInspection(true) { pendingThermalImage = it } },
                    onDigitalPickInitial = { loadInitialImageFromInspection(false) { pendingDigitalImage = it } },
                    onThermalFolder = { thermalFolderLauncher.launch("image/*") },
                    onDigitalFolder = { digitalFolderLauncher.launch("image/*") },
                    onThermalCamera = { thermalCameraLauncher.launch(null) },
                    onDigitalCamera = { digitalCameraLauncher.launch(null) },
                    onCronicoClick = {
                        val entity = editingMechanicalProblemOriginal ?: return@MechanicalProblemDialog
                        pendingCronicoEntity = entity
                        showCronicoConfirmDialog = true
                    },
                    cronicoEnabled = cronicoActionEnabled && !isSavingCronico,
                    cronicoChecked = editingMechanicalProblemOriginal?.esCronico?.equals("SI", ignoreCase = true) == true,
                    cerradoChecked = mechanicalProblemClosed,
                    cerradoEnabled = (
                        editingMechanicalProblemOriginal?.estatusProblema?.equals("Cerrado", ignoreCase = true) != true
                    ) || (
                        editingMechanicalProblemOriginal?.idInspeccion
                            ?.equals(currentInspection?.idInspeccion, ignoreCase = true) == true
                    ),
                    onCerradoChange = { mechanicalProblemClosed = it },
                    onNavigatePrevious = if (editingMechanicalProblemId != null) {
                        { data -> navigateFromMechanical(-1, data) }
                    } else {
                        null
                    },
                    onNavigateNext = if (editingMechanicalProblemId != null) {
                        { data -> navigateFromMechanical(1, data) }
                    } else {
                        null
                    },
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    showEditControls = editingMechanicalProblemId != null,
                    selectedTabIndex = problemDialogTab,
                    onSelectedTabChange = { problemDialogTab = it },
                    transitionKey = editingMechanicalProblemId ?: pendingProblemNumber,
                    onDismiss = {
                        showMechanicalProblemDialog = false
                        cronicoActionEnabled = false
                        resetProblemNavigation()
                        resetMechanicalProblemState()
                    },
                    onContinue = { saveMechanicalProblem(it) },
                    continueEnabled = !isSavingMechanicalProblem,
                    initialFormData = mechanicalProblemInitialData,
                    dialogKey = mechanicalProblemFormKey
                )
            }
            if (showAislamientoTermicoProblemDialog && pendingProblemEquipmentName != null) {
                AislamientoTermicoProblemDialog(
                    inspectionNumber = pendingInspectionNumber,
                    problemNumber = pendingProblemNumber,
                    problemType = pendingProblemType,
                    equipmentName = pendingProblemEquipmentName ?: "-",
                    equipmentRoute = pendingProblemRoute ?: "-",
                    failureOptions = electricHazardOptions,
                    phaseOptions = electricPhaseOptions,
                    environmentOptions = electricEnvironmentOptions,
                    manufacturerOptions = manufacturerOptionsPairs,
                    thermalImageName = pendingThermalImage,
                    digitalImageName = pendingDigitalImage,
                    onThermalImageChange = { pendingThermalImage = it },
                    onDigitalImageChange = { pendingDigitalImage = it },
                    onThermalSequenceUp = { pendingThermalImage = adjustImageSequence(pendingThermalImage, +1) },
                    onThermalSequenceDown = { pendingThermalImage = adjustImageSequence(pendingThermalImage, -1) },
                    onDigitalSequenceUp = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, +1) },
                    onDigitalSequenceDown = { pendingDigitalImage = adjustImageSequence(pendingDigitalImage, -1) },
                    onThermalPickInitial = { loadInitialImageFromInspection(true) { pendingThermalImage = it } },
                    onDigitalPickInitial = { loadInitialImageFromInspection(false) { pendingDigitalImage = it } },
                    onThermalFolder = { thermalFolderLauncher.launch("image/*") },
                    onDigitalFolder = { digitalFolderLauncher.launch("image/*") },
                    onThermalCamera = { thermalCameraLauncher.launch(null) },
                    onDigitalCamera = { digitalCameraLauncher.launch(null) },
                    onCronicoClick = {
                        val entity = editingAislamientoTermicoProblemOriginal ?: return@AislamientoTermicoProblemDialog
                        pendingCronicoEntity = entity
                        showCronicoConfirmDialog = true
                    },
                    cronicoEnabled = cronicoActionEnabled && !isSavingCronico,
                    cronicoChecked = editingAislamientoTermicoProblemOriginal?.esCronico?.equals("SI", ignoreCase = true) == true,
                    cerradoChecked = aislamientoTermicoProblemClosed,
                    cerradoEnabled = (
                        editingAislamientoTermicoProblemOriginal?.estatusProblema?.equals("Cerrado", ignoreCase = true) != true
                    ) || (
                        editingAislamientoTermicoProblemOriginal?.idInspeccion
                            ?.equals(currentInspection?.idInspeccion, ignoreCase = true) == true
                    ),
                    onCerradoChange = { aislamientoTermicoProblemClosed = it },
                    onNavigatePrevious = if (editingAislamientoTermicoProblemId != null) {
                        { data -> navigateFromAislamientoTermico(-1, data) }
                    } else {
                        null
                    },
                    onNavigateNext = if (editingAislamientoTermicoProblemId != null) {
                        { data -> navigateFromAislamientoTermico(1, data) }
                    } else {
                        null
                    },
                    canNavigatePrevious = canNavigatePrevious,
                    canNavigateNext = canNavigateNext,
                    showEditControls = editingAislamientoTermicoProblemId != null,
                    selectedTabIndex = problemDialogTab,
                    onSelectedTabChange = { problemDialogTab = it },
                    transitionKey = editingAislamientoTermicoProblemId ?: pendingProblemNumber,
                    onDismiss = {
                        showAislamientoTermicoProblemDialog = false
                        cronicoActionEnabled = false
                        resetProblemNavigation()
                        resetAislamientoTermicoProblemState()
                    },
                    onContinue = { saveAislamientoTermicoProblem(it) },
                    continueEnabled = !isSavingAislamientoTermicoProblem,
                    initialFormData = aislamientoTermicoProblemInitialData,
                    dialogKey = aislamientoTermicoProblemFormKey
                )
            }

            if (showCronicoConfirmDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showCronicoConfirmDialog = false
                        pendingCronicoEntity = null
                    },
                    confirmButton = {
                        Button(onClick = {
                            val entity = pendingCronicoEntity
                            showCronicoConfirmDialog = false
                            pendingCronicoEntity = null
                            if (entity != null) {
                                createCronicoFromProblem(entity) { created ->
                                    pendingProblemNumber = created.numeroProblema?.toString() ?: pendingProblemNumber
                                    pendingInspectionNumber = currentInspection?.noInspeccion?.toString() ?: "-"
                                    when (created.idTipoInspeccion?.lowercase(Locale.ROOT)) {
                                        VISUAL_PROBLEM_TYPE_ID?.lowercase(Locale.ROOT) -> {
                                            editingProblemId = created.idProblema
                                            editingProblemOriginal = created
                                        }
                                        ELECTRIC_PROBLEM_TYPE_ID?.lowercase(Locale.ROOT) -> {
                                            editingElectricProblemId = created.idProblema
                                            editingElectricProblemOriginal = created
                                        }
                                        MECHANICAL_PROBLEM_TYPE_ID?.lowercase(Locale.ROOT) -> {
                                            editingMechanicalProblemId = created.idProblema
                                            editingMechanicalProblemOriginal = created
                                        }
                                        AISLAMIENTO_TERMICO_PROBLEM_TYPE_ID?.lowercase(Locale.ROOT) -> {
                                            editingAislamientoTermicoProblemId = created.idProblema
                                            editingAislamientoTermicoProblemOriginal = created
                                        }
                                    }
                                    cronicoActionEnabled = false
                                }
                            }
                        }) { Text("Continuar") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showCronicoConfirmDialog = false
                            pendingCronicoEntity = null
                        }) { Text("Cancelar") }
                    },
                    title = { Text("Confirmación") },
                    text = {
                        Text(
                            "Estás a punto de crear un problema crónico, el problema actual " +
                            "se cerrará y se abrirá un nuevo problema en la inspección actual. " +
                            "¿Deseas continuar?"
                        )
                    }
                )
            }


            // dialogo de confirmacion para eliminar ubicacion
            if (deleteUbConfirmNode != null) {
                val nodeToDelete = deleteUbConfirmNode!!
                AlertDialog(
                    onDismissRequest = { deleteUbConfirmNode = null },
                    confirmButton = {
                        Button(onClick = {
                            scope.launch {
                                val ubId = nodeToDelete.id
                                val nowTs = java.time.LocalDateTime.now()
                                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                val userId = currentUser?.idUsuario
                                val inspectionId = currentInspection?.idInspeccion
                                val parentUbicacionId = runCatching { ubicacionDao.getById(ubId)?.idUbicacionPadre }.getOrNull()

                                inspectionRepository.markUbicacionInactive(ubId, userId, nowTs)
                                if (!inspectionId.isNullOrBlank()) {
                                    updateParentStatusesAfterManualChange(
                                        inspectionId = inspectionId,
                                        startUbicacionId = ubId,
                                        nowTs = nowTs,
                                        knownParentId = parentUbicacionId
                                    )
                                }

                                val preservedSelection = selectedId?.takeIf { it != ubId }
                                refreshTree(
                                    preserveSelection = preservedSelection
                                )

                                deleteUbConfirmNode = null
                            }
                        }) { Text("Eliminar") }
                    },
                    dismissButton = {
                        Button(onClick = { deleteUbConfirmNode = null }) { Text("Cancelar") }
                    },
                    title = { Text("Confirmar eliminacion") },
                    text = { Text("Eliminar la ubicacion seleccionada?") }
                )
            }



            // ------------------ DIaLOGO: NUEVA UBICACIoN ------------------
            val previewRoute = run {
                val parentForPreview = when {
                    editingUbId != null -> editingParentId
                    selectedId == null -> null
                    selectedId == rootId -> "0"
                    else -> selectedId
                }
                val basePath = when {
                    parentForPreview == null || parentForPreview == "0" -> rootTitle
                    else -> titlePathForId(nodes, parentForPreview).joinToString(" / ").ifBlank { rootTitle }
                }
                val trimmedName = locationForm.name.trim()
                when {
                    basePath.isBlank() && trimmedName.isBlank() -> ""
                    basePath.isBlank() -> trimmedName
                    trimmedName.isBlank() -> basePath
                    else -> "$basePath / $trimmedName"
                }
            }
            NewLocationDialog(
                show = showNewUbDialog,
                formState = locationForm,
                statusOptions = statusOptions,
                prioridadOptions = prioridadOptions,
                fabricanteOptions = fabricanteOptions,
                previewRoute = previewRoute,
                isSaving = isSavingNewUb,
                onDismiss = {
                    if (isSavingNewUb) return@NewLocationDialog
                    showNewUbDialog = false
                    locationForm.error = null
                    isSavingNewUb = false
                },
                onConfirm = confirm@{
                    if (isSavingNewUb) return@confirm
                    val name = locationForm.name.trim()
                    if (name.isEmpty()) {
                        locationForm.error = "El nombre es obligatorio"
                        return@confirm
                    }
                    val isEdit = editingUbId != null
                    val id = editingUbId ?: java.util.UUID.randomUUID().toString().uppercase()
                    if (!isEdit && selectedId == null) {
                        locationForm.error = "Selecciona una ubicacion en el arbol"
                        return@confirm
                    }
                    val parentForCalc = if (isEdit) {
                        editingParentId
                    } else {
                        when (selectedId) {
                            null -> null
                            rootId -> "0"
                            else -> selectedId
                        }
                    }
                    val nivel = parentForCalc?.let { parentId ->
                        depthOfId(nodes, parentId) + 1
                    } ?: 0
                    val ruta = when {
                        parentForCalc == "0" -> "$rootTitle / $name"
                        parentForCalc != null -> {
                            val titles = titlePathForId(nodes, parentForCalc)
                            if (titles.isNotEmpty()) titles.joinToString(" / ") + " / " + name else name
                        }
                        else -> "$rootTitle / $name"
                    }
                    scope.launch {
                        isSavingNewUb = true
                        try {
                            val nowTs = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            val existing = if (isEdit) runCatching { ubicacionDao.getById(id) }.getOrNull() else null

                            val nueva = com.example.etic.data.local.entities.Ubicacion(
                                idUbicacion = id,
                                idUbicacionPadre = parentForCalc,
                                idSitio = currentInspection?.idSitio,
                                nivelArbol = nivel,
                                ubicacion = name,
                                descripcion = locationForm.description.trim().ifBlank { null },
                                esEquipo = if (locationForm.isEquipment) "SI" else "NO",
                                codigoBarras = locationForm.barcode.trim().ifBlank { null },
                                fabricante = locationForm.fabricanteId,
                                ruta = ruta,
                                estatus = "Activo",
                                creadoPor = existing?.creadoPor ?: currentUserId,
                                fechaCreacion = existing?.fechaCreacion ?: nowTs,
                                modificadoPor = if (isEdit) currentUserId else null,
                                fechaMod = if (isEdit) nowTs else null,
                                idTipoPrioridad = locationForm.prioridadId,
                                idInspeccion = currentInspection?.idInspeccion
                            )

                            val saveContext = UbicacionSaveContext(
                                isEdit = isEdit,
                                editingDetId = editingDetId,
                                editingInspId = editingInspId,
                                newStatusId = locationForm.statusId,
                                currentInspectionId = currentInspection?.idInspeccion,
                                currentSiteId = currentInspection?.idSitio
                            )
                            val okUb = runCatching {
                                inspectionRepository.saveUbicacion(
                                    entity = nueva,
                                    context = saveContext,
                                    nowTs = nowTs,
                                    currentUserId = currentUserId
                                )
                            }.getOrDefault(false)
                            if (okUb) {
                                val parentToExpand = when (parentForCalc) {
                                    null, "0" -> rootId
                                    else -> parentForCalc
                                }
                                refreshTree(
                                    extraExpanded = listOf(parentToExpand)
                                )
                                locationForm.error = null
                                selectedId?.let { pid -> if (!expanded.contains(pid)) expanded.add(pid) }
                                if (isEdit) {
                                    editingUbId = null
                                    editingParentId = null
                                    editingDetId = null
                                    editingInspId = null
                                    showNewUbDialog = false
                                    locationForm.resetForNew()
                                } else {
                                    Toast.makeText(ctx, "Ubicacion creada correctamente.", Toast.LENGTH_SHORT).show()
                                    applyNewUbDefaults()
                                }
                                isSavingNewUb = false
                            } else {
                                locationForm.error = "No se pudo guardar la ubicacion"
                                isSavingNewUb = false
                            }
                        } catch (_: Exception) {
                            locationForm.error = "No se pudo guardar la ubicacion"
                            isSavingNewUb = false
                        }
                    }
                }
            )
            // -------------------------------------------------------------------------------

            // =====================================================================
            // DIÁLOGO COMPLETO: EDITAR UBICACIÓN EN 2 COLUMNAS
            //   - Diálogo más ancho
            //   - Columna izquierda angosta (formulario)
            //   - Columna derecha ancha (Baseline / Histórico)
            // =====================================================================
            if (showEditUbDialog) {
                var dialogOffset by remember { mutableStateOf(Offset.Zero) }

                Dialog(
                    onDismissRequest = { },
                    properties = DialogProperties(
                        dismissOnClickOutside = false,
                        dismissOnBackPress = false,
                        usePlatformDefaultWidth = false   // ahora sí usamos TODO el tamaño disponible
                    )
                ) {

                    // ---------------------------------------------------------------
                    // BOX QUE CONTROLA EL ANCHO REAL DEL DIÁLOGO
                    // fillMaxWidth(0.98f) = usa casi toda la pantalla
                    // ---------------------------------------------------------------
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {

                        // ---------------------------------------------------------------
                        // CARD PRINCIPAL DEL DIÁLOGO
                        // Se expande a todo el ancho permitido por el Box
                        // ---------------------------------------------------------------
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .fillMaxHeight(0.95f)
                                .offset {
                                    IntOffset(
                                        dialogOffset.x.roundToInt(),
                                        dialogOffset.y.roundToInt()
                                    )
                                },
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            // ---------------------------------------------------------------
                            // CONTENEDOR PRINCIPAL
                            // ---------------------------------------------------------------
                            Column(
                                Modifier
                                    .fillMaxSize()
                            ) {

                                // ---------------------------------------------------------------
                                // TÍTULO DEL DIÁLOGO
                                // ---------------------------------------------------------------
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DIALOG_HEADER_TURQUOISE)
                                        .pointerInput(Unit) {
                                            detectDragGestures { change, dragAmount ->
                                                change.consume()
                                                dialogOffset += Offset(dragAmount.x, dragAmount.y)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "Editar ubicación",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                }
                                Spacer(Modifier.height(8.dp))


                                // ===============================================================
                                // FILA PRINCIPAL EN DOS COLUMNAS
                                //   IZQUIERDA: formulario de ubicación
                                //   DERECHA: tabs Baseline / Histórico
                                // ===============================================================
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = true)
                                        .padding(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {


                                    // ===========================================================
                                    // COLUMNA IZQUIERDA  (Formulario de Ubicación)
                                    // MÁS ANGOSTA → 35%
                                    // ===========================================================
                                    val scrollForm = rememberScrollState()
                                    Column(
                                        modifier = Modifier
                                            .weight(0.32f)   // ← columna angosta
                                            .fillMaxHeight()
                                            .verticalScroll(scrollForm),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        
                                        DropdownSelector(
                                            label = stringResource(com.example.etic.R.string.label_estatus_inspeccion),
                                            options = statusOptions.map {
                                                it.idStatusInspeccionDet to (it.estatusInspeccionDet ?: it.idStatusInspeccionDet)
                                            },
                                            selectedId = locationForm.statusId,
                                            selectedLabelOverride = locationForm.statusLabel,
                                            placeholder = "Seleccionar estatus",
                                            expanded = locationForm.statusExpanded,
                                            onExpandedChange = { locationForm.statusExpanded = it },
                                            onSelected = { id, label ->
                                                locationForm.statusLabel = label
                                                locationForm.statusId = id
                                                locationForm.statusExpanded = false
                                            }
                                        )

                                        DropdownSelector(
                                            label = "Tipo de prioridad",
                                            options = prioridadOptions.map {
                                                it.idTipoPrioridad to (it.tipoPrioridad ?: it.idTipoPrioridad)
                                            },
                                            selectedId = locationForm.prioridadId,
                                            selectedLabelOverride = locationForm.prioridadLabel,
                                            placeholder = "Seleccionar prioridad",
                                            expanded = locationForm.prioridadExpanded,
                                            onExpandedChange = { locationForm.prioridadExpanded = it },
                                            onSelected = { id, label ->
                                                locationForm.prioridadLabel = label
                                                locationForm.prioridadId = id
                                                locationForm.prioridadExpanded = false
                                            }
                                        )

                                        FilterableSelector(
                                            label = "Fabricante",
                                            options = fabricanteOptions.map {
                                                it.idFabricante to (it.fabricante ?: it.idFabricante)
                                            },
                                            selectedId = locationForm.fabricanteId,
                                            selectedLabelOverride = locationForm.fabricanteLabel,
                                            placeholder = "Seleccionar fabricante",
                                            expanded = locationForm.fabricanteExpanded,
                                            onExpandedChange = { locationForm.fabricanteExpanded = it },
                                            onSelected = { id, label ->
                                                locationForm.fabricanteLabel = label
                                                locationForm.fabricanteId = id
                                                locationForm.fabricanteExpanded = false
                                            },
                                            onAddClick = {}
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Es equipo")
                                            Spacer(Modifier.width(12.dp))
                                            Switch(
                                                checked = locationForm.isEquipment,
                                                onCheckedChange = { locationForm.isEquipment = it }
                                            )
                                        }

                                        LabeledInputField(
                                            value = locationForm.name,
                                            onValueChange = { locationForm.name = it },
                                            label = stringResource(com.example.etic.R.string.label_nombre_ubicacion),
                                            required = true,
                                            isError = locationForm.error != null,
                                            singleLine = true,
                                        )

                                        LabeledInputField(
                                            value = locationForm.description,
                                            onValueChange = { locationForm.description = it },
                                            label = stringResource(com.example.etic.R.string.label_descripcion),
                                            singleLine = false,
                                            fieldHeight = 52.dp,
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                        )

                                        LabeledInputField(
                                            value = locationForm.barcode,
                                            onValueChange = { locationForm.barcode = it },
                                            label = stringResource(com.example.etic.R.string.label_codigo_barras),
                                            singleLine = true,
                                        )

                                        if (locationForm.error != null) {
                                            Text(locationForm.error!!, color = MaterialTheme.colorScheme.error)
                                        }

                                        // Botonera propia del formulario (antes en Tab 0)
                                        Row(
                                            Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Button(
                                                enabled = locationForm.name.isNotBlank() && !isSavingEditUb,
                                                onClick = {
                                                    if (isSavingEditUb) return@Button
                                                    val name = locationForm.name.trim()
                                                    if (name.isEmpty()) {
                                                        locationForm.error = "El nombre es obligatorio"
                                                        return@Button
                                                    }
                                                    val id = editingUbId ?: return@Button
                                                    val parentForCalc = editingParentId
                                                    val nivel = parentForCalc?.let { parentId -> depthOfId(nodes, parentId) + 1 } ?: 0
                                                    val ruta = when {
                                                        parentForCalc == "0" -> "${rootTitle} / $name"
                                                        parentForCalc != null -> {
                                                            val titles = titlePathForId(nodes, parentForCalc)
                                                            if (titles.isNotEmpty()) titles.joinToString(" / ") + " / " + name else name
                                                        }
                                                        else -> "${rootTitle} / $name"
                                                    }
                                                    scope.launch {
                                                        isSavingEditUb = true
                                                        try {
                                                            val nowTs = java.time.LocalDateTime.now()
                                                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                                            val existing = runCatching { ubicacionDao.getById(id) }.getOrNull()
                                                            val nueva = com.example.etic.data.local.entities.Ubicacion(
                                                                idUbicacion = id,
                                                                idUbicacionPadre = parentForCalc,
                                                                idSitio = currentInspection?.idSitio,
                                                                nivelArbol = nivel,
                                                                ubicacion = name,
                                                                descripcion = locationForm.description.trim().ifBlank { null },
                                                                esEquipo = if (locationForm.isEquipment) "SI" else "NO",
                                                                codigoBarras = locationForm.barcode.trim().ifBlank { null },
                                                                fabricante = locationForm.fabricanteId,
                                                                ruta = ruta,
                                                                estatus = "Activo",
                                                                creadoPor = existing?.creadoPor ?: currentUserId,
                                                                fechaCreacion = existing?.fechaCreacion,
                                                                modificadoPor = currentUserId,
                                                                fechaMod = nowTs,
                                                                idTipoPrioridad = locationForm.prioridadId,
                                                                idInspeccion = existing?.idInspeccion
                                                            )
                                                            val saveContext = UbicacionSaveContext(
                                                                isEdit = true,
                                                                editingDetId = editingDetId,
                                                                editingInspId = editingInspId,
                                                                newStatusId = locationForm.statusId,
                                                                currentInspectionId = currentInspection?.idInspeccion,
                                                                currentSiteId = currentInspection?.idSitio
                                                            )
                                                            val okUb = runCatching {
                                                                inspectionRepository.saveUbicacion(
                                                                    entity = nueva,
                                                                    context = saveContext,
                                                                    nowTs = nowTs,
                                                                    currentUserId = currentUserId
                                                                )
                                                            }.getOrDefault(false)
                                                            if (okUb) {
                                                                val parentToExpand = when (editingParentId) {
                                                                    null, "0" -> rootId
                                                                    else -> editingParentId
                                                                }
                                                                refreshTree(
                                                                    extraExpanded = listOf(parentToExpand, editingUbId)
                                                                )
                                                                locationForm.error = null
                                                                delay(3000)
                                                                showEditUbDialog = false
                                                                isSavingEditUb = false
                                                            } else {
                                                                locationForm.error = "No se pudo guardar la ubicacion"
                                                                isSavingEditUb = false
                                                            }
                                                        } catch (_: Exception) {
                                                            locationForm.error = "No se pudo guardar la ubicacion"
                                                            isSavingEditUb = false
                                                        }
                                                    }
                                                }
                                            ) { Text("Guardar") }
                                        }

                                        // ------------------------------------------------------------------
                                    }


                                    // ===========================================================
                                    // COLUMNA DERECHA  (Baseline / Histórico)
                                    // MÁS ANCHA → 65%
                                    // ===========================================================
                                    Column(
                                        modifier = Modifier
                                            .weight(0.68f)  // ← columna más ancha
                                            .fillMaxHeight()
                                    ) {

                                        // ---------------------------------------------------------------
                                        // TABS DERECHA (ya NO mostramos tab de Ubicación)
                                        // ---------------------------------------------------------------

                                        val editingRoute = remember(editingUbId, nodes, rootTitle) {
                                            editingUbId?.let { id ->
                                                val titles = titlePathForId(nodes, id)
                                                titles.takeIf { it.isNotEmpty() }?.joinToString(" / ")
                                                    ?: rootTitle
                                            } ?: rootTitle
                                        }
                                        Text(
                                            "Ruta del equipo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            editingRoute,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Divider()

                                        // ------------------------------------------------------------------


                                        TabRow(selectedTabIndex = editTab) {
                                            Tab(
                                                selected = editTab == 0,
                                                onClick = { editTab = 0 },
                                                text = { Text("Baseline") }
                                            )
                                            Tab(
                                                selected = editTab == 1,
                                                onClick = { editTab = 1 },
                                                text = { Text("Histórico Inspecciones") }
                                            )
                                        }

                                        Spacer(Modifier.height(8.dp))


                                        // ===========================================================
                                        // CONTENIDO DE TAB 0 Y TAB 1
                                        // ===========================================================
                                        when (editTab) {


                                            // -----------------------------------------------------------
                                            // TAB 0 → BASELINE
                                            // -----------------------------------------------------------
                                            0 -> {
                                                val lineaBaseDao = remember { com.example.etic.data.local.DbProvider.get(ctx).lineaBaseDao() }
                                                val inspDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDao() }
                                                var showNewBaseline by remember { mutableStateOf(false) }
                                                var isSavingBaseline by remember { mutableStateOf(false) }
                                                val ubId = editingUbId
                                                val inspId = currentInspection?.idInspeccion

                                                LaunchedEffect(showNewBaseline) {
                                                    if (!showNewBaseline) {
                                                        isSavingBaseline = false
                                                    }
                                                }

                                                data class BaselineRow(
                                                    val id: String,
                                                    val numInspeccion: String,
                                                    val fecha: java.time.LocalDate,
                                                    val mtaC: Double,
                                                    val tempC: Double,
                                                    val ambC: Double,
                                                    val imgR: String?,
                                                    val imgD: String?,
                                                    val notas: String
                                                )

                                                var baselineToEdit by remember { mutableStateOf<BaselineRow?>(null) }
                                                var confirmDeleteId by remember { mutableStateOf<String?>(null) }

                                                var baselineCache by remember { mutableStateOf(emptyList<BaselineRow>()) }
                                                val tableData by produceState(initialValue = baselineCache, ubId, inspId, baselineRefreshTick) {
                                                    val rows = if (!inspId.isNullOrBlank()) {
                                                        runCatching { lineaBaseDao.getByInspeccionActivos(inspId) }.getOrElse { emptyList() }
                                                    } else emptyList()
                                                    val ubicaciones = runCatching { ubicacionDao.getAllActivas() }.getOrElse { emptyList() }
                                                    val ubicMap = ubicaciones.associateBy { it.idUbicacion }
                                                    value = rows
                                                        .filter { r -> ubId != null && r.idUbicacion == ubId }
                                                        .map { r ->
                                                            val fecha = runCatching {
                                                                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                                                                val onlyDate = raw?.take(10)
                                                                if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else java.time.LocalDate.now()
                                                            }.getOrDefault(java.time.LocalDate.now())
                                                            val numInspDisplay = r.idInspeccion?.let { id ->
                                                                runCatching { inspDao.getById(id)?.noInspeccion?.toString() }.getOrNull()
                                                            } ?: ""
                                                            val ubicDisplay = r.idUbicacion?.let { id -> ubicMap[id]?.ubicacion } ?: ""
                                                            BaselineRow(
                                                                id = r.idLineaBase,
                                                                numInspeccion = numInspDisplay,
                                                                fecha = fecha,
                                                                mtaC = r.mta ?: 0.0,
                                                                tempC = r.tempMax ?: 0.0,
                                                                ambC = r.tempAmb ?: 0.0,
                                                                imgR = r.archivoIr,
                                                                imgD = r.archivoId,
                                                                notas = r.notas ?: ""
                                                            )
                                                        }
                                                    baselineCache = value
                                                }
                                                val hasActiveBaselineForUbicacion by produceState(
                                                    initialValue = false,
                                                    ubId,
                                                    baselineRefreshTick
                                                ) {
                                                    value = if (ubId.isNullOrBlank()) {
                                                        false
                                                    } else {
                                                        runCatching {
                                                            lineaBaseDao.existsActiveByUbicacion(ubId)
                                                        }.getOrDefault(false)
                                                    }
                                                }

                                                // Layout de baseline unico por ubicacion (vista detalle, no tabla)
                                                Column(Modifier.fillMaxSize()) {

                                                    if (!hasActiveBaselineForUbicacion) {
                                                        Row(
                                                            Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Button(onClick = { baselineToEdit = null; showNewBaseline = true }) {
                                                                Icon(Icons.Filled.Add, contentDescription = null)
                                                                Spacer(Modifier.width(8.dp))
                                                                Text("Nuevo Baseline")
                                                            }
                                                        }
                                                    }
                                                    Spacer(Modifier.height(8.dp))

                                                    val baselineItem = tableData.lastOrNull()
                                                    if (baselineItem == null) {
                                                        Box(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(8.dp),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) {
                                                            Text(stringResource(com.example.etic.R.string.msg_sin_baseline))
                                                        }
                                                    } else {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .verticalScroll(rememberScrollState()),
                                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                                        ) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                    TextButton(
                                                                        onClick = {
                                                                            baselineToEdit = baselineItem
                                                                            showNewBaseline = true
                                                                        }
                                                                    ) { Text("Editar") }
                                                                    IconButton(onClick = { confirmDeleteId = baselineItem.id }) {
                                                                        Icon(
                                                                            Icons.Outlined.Delete,
                                                                            contentDescription = "Eliminar baseline",
                                                                            tint = MaterialTheme.colorScheme.error
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .border(
                                                                        width = 1.dp,
                                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                                                                        shape = RoundedCornerShape(12.dp)
                                                                    )
                                                                    .background(
                                                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f),
                                                                        shape = RoundedCornerShape(12.dp)
                                                                    )
                                                                    .padding(12.dp)
                                                            ) {
                                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                                    Text(
                                                                        "No. inspeccion: ${baselineItem.numInspeccion.ifBlank { "-" }}",
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                    Text(
                                                                        "Fecha: ${baselineItem.fecha}",
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .border(
                                                                                    width = 1.dp,
                                                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                                    shape = RoundedCornerShape(10.dp)
                                                                                )
                                                                                .padding(10.dp)
                                                                        ) {
                                                                            Column {
                                                                                Text("MTA", style = MaterialTheme.typography.labelSmall)
                                                                                Text("${baselineItem.mtaC} C", style = MaterialTheme.typography.titleSmall)
                                                                            }
                                                                        }
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .border(
                                                                                    width = 1.dp,
                                                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                                    shape = RoundedCornerShape(10.dp)
                                                                                )
                                                                                .padding(10.dp)
                                                                        ) {
                                                                            Column {
                                                                                Text("Temp Max", style = MaterialTheme.typography.labelSmall)
                                                                                Text("${baselineItem.tempC} C", style = MaterialTheme.typography.titleSmall)
                                                                            }
                                                                        }
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .weight(1f)
                                                                                .border(
                                                                                    width = 1.dp,
                                                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                                    shape = RoundedCornerShape(10.dp)
                                                                                )
                                                                                .padding(10.dp)
                                                                        ) {
                                                                            Column {
                                                                                Text("Temp Amb", style = MaterialTheme.typography.labelSmall)
                                                                                Text("${baselineItem.ambC} C", style = MaterialTheme.typography.titleSmall)
                                                                            }
                                                                        }
                                                                    }
                                                                    Divider(thickness = DIVIDER_THICKNESS)
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                        val irPreviewBmp = remember(baselineItem.imgR, inspectionNumero, rootTreeUriStr) {
                                                                            EticImageStore.loadBitmap(
                                                                                context = ctx,
                                                                                rootTreeUri = rootTreeUri,
                                                                                inspectionNumero = inspectionNumero,
                                                                                fileName = baselineItem.imgR
                                                                            )
                                                                        }
                                                                        val idPreviewBmp = remember(baselineItem.imgD, inspectionNumero, rootTreeUriStr) {
                                                                            EticImageStore.loadBitmap(
                                                                                context = ctx,
                                                                                rootTreeUri = rootTreeUri,
                                                                                inspectionNumero = inspectionNumero,
                                                                                fileName = baselineItem.imgD
                                                                            )
                                                                        }
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(
                                                                                "IR: ${baselineItem.imgR.orEmpty().ifBlank { "-" }}",
                                                                                maxLines = 1,
                                                                                overflow = TextOverflow.Ellipsis
                                                                            )
                                                                            Spacer(Modifier.height(4.dp))
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(120.dp)
                                                                                    .border(
                                                                                        width = 1.dp,
                                                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                                        shape = RoundedCornerShape(8.dp)
                                                                                    ),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                if (irPreviewBmp != null) {
                                                                                    androidx.compose.foundation.Image(
                                                                                        bitmap = irPreviewBmp.asImageBitmap(),
                                                                                        contentDescription = "Vista previa IR",
                                                                                        modifier = Modifier.fillMaxSize()
                                                                                    )
                                                                                } else {
                                                                                    Icon(Icons.Outlined.Image, contentDescription = null)
                                                                                }
                                                                            }
                                                                        }
                                                                        Column(modifier = Modifier.weight(1f)) {
                                                                            Text(
                                                                                "ID: ${baselineItem.imgD.orEmpty().ifBlank { "-" }}",
                                                                                maxLines = 1,
                                                                                overflow = TextOverflow.Ellipsis
                                                                            )
                                                                            Spacer(Modifier.height(4.dp))
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(120.dp)
                                                                                    .border(
                                                                                        width = 1.dp,
                                                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                                                                        shape = RoundedCornerShape(8.dp)
                                                                                    ),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                if (idPreviewBmp != null) {
                                                                                    androidx.compose.foundation.Image(
                                                                                        bitmap = idPreviewBmp.asImageBitmap(),
                                                                                        contentDescription = "Vista previa ID",
                                                                                        modifier = Modifier.fillMaxSize()
                                                                                    )
                                                                                } else {
                                                                                    Icon(Icons.Outlined.Image, contentDescription = null)
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    Divider(thickness = DIVIDER_THICKNESS)
                                                                    Text("Notas", style = MaterialTheme.typography.labelMedium)
                                                                    Text(
                                                                        baselineItem.notas.ifBlank { "-" },
                                                                        style = MaterialTheme.typography.bodyMedium
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }

                                                    // Diálogo de confirmación de borrado (igual que ya tenías)
                                                    if (confirmDeleteId != null) {
                                                        AlertDialog(
                                                            onDismissRequest = { confirmDeleteId = null },
                                                            confirmButton = {
                                                                Button(onClick = {
                                                                    val id = confirmDeleteId ?: return@Button
                                                                    scope.launch {
                                                                        // Eliminar baseline
                                                                        runCatching { lineaBaseDao.deleteById(id) }

                                                                        // Revertir estatus de inspeccion_det asociado a PVERIF
                                                                        val idUb = ubId
                                                                        val idInsp = inspId
                                                                        if (!idUb.isNullOrBlank() && !idInsp.isNullOrBlank()) {
                                                                            val nowTs = java.time.LocalDateTime.now()
                                                                                .format(
                                                                                    java.time.format.DateTimeFormatter.ofPattern(
                                                                                        "yyyy-MM-dd HH:mm:ss"
                                                                                    )
                                                                                )
                                                                            val detRow = try {
                                                                                inspeccionDetDao.getByUbicacion(idUb)
                                                                                    .firstOrNull { it.idInspeccion == idInsp }
                                                                            } catch (_: Exception) {
                                                                                null
                                                                            }
                                                                            if (detRow != null) {
                                                                                val revertedDet = detRow.copy(
                                                                                    idStatusInspeccionDet = "568798D1-76BB-11D3-82BF-00104BC75DC2",
                                                                                    idEstatusColorText = 1,
                                                                                    modificadoPor = currentUserId,
                                                                                    fechaMod = nowTs
                                                                                )
                                                                                runCatching { inspeccionDetDao.update(revertedDet) }
                                                                                updateParentStatusesAfterManualChange(
                                                                                    inspectionId = idInsp,
                                                                                    startUbicacionId = idUb,
                                                                                    nowTs = nowTs
                                                                                )
                                                                            }
                                                                        }

                                                                        confirmDeleteId = null
                                                                        baselineRefreshTick++
                                                                    }
                                                                }) { Text("Eliminar") }
                                                            },
                                                            dismissButton = {
                                                                Button(onClick = { confirmDeleteId = null }) { Text("Cancelar") }
                                                            },
                                                            text = { Text("Eliminar baseline seleccionado?") }
                                                        )
                                                    }
                                                }

                                                // dialogo de "Nuevo Baseline"
                                                if (showNewBaseline) {
                                                    var mta by remember { mutableStateOf("") }
                                                    var tempMax by remember { mutableStateOf("") }
                                                    var tempAmb by remember { mutableStateOf("") }
                                                    var notas by remember { mutableStateOf("") }
                                                    var imgIr by remember { mutableStateOf("") }
                                                    var imgId by remember { mutableStateOf("") }
                                                    var irPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                                    var idPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                                                    fun resetBaselineFormState() {
                                                        mta = ""
                                                        tempMax = ""
                                                        tempAmb = ""
                                                        notas = ""
                                                        imgIr = ""
                                                        imgId = ""
                                                        irPreview = null
                                                        idPreview = null
                                                    }

                                                    suspend fun BaselineRow.refreshFromDatabase(): BaselineRow {
                                                        val latest = runCatching {
                                                            withContext(Dispatchers.IO) { lineaBaseDao.getById(id) }
                                                        }.getOrNull() ?: return this
                                                        val updatedFecha = runCatching {
                                                            val raw = latest.fechaCreacion?.takeIf { it.isNotBlank() }
                                                            val onlyDate = raw?.take(10)
                                                            if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else fecha
                                                        }.getOrDefault(fecha)
                                                        val updatedNumInsp = latest.idInspeccion?.let { inspId ->
                                                            runCatching {
                                                                withContext(Dispatchers.IO) { inspDao.getById(inspId) }
                                                                    ?.noInspeccion
                                                                    ?.toString()
                                                            }.getOrNull()
                                                        } ?: numInspeccion
                                                        return copy(
                                                            numInspeccion = updatedNumInsp,
                                                            fecha = updatedFecha,
                                                            mtaC = latest.mta ?: mtaC,
                                                            tempC = latest.tempMax ?: tempC,
                                                            ambC = latest.tempAmb ?: ambC,
                                                            notas = latest.notas ?: "",
                                                            imgR = latest.archivoIr,
                                                            imgD = latest.archivoId
                                                        )
                                                    }

                                                    LaunchedEffect(showNewBaseline, baselineToEdit?.id, baselineRefreshTick) {
                                                        if (!showNewBaseline) {
                                                            resetBaselineFormState()
                                                            return@LaunchedEffect
                                                        }
                                                        val current = baselineToEdit
                                                        if (current == null) {
                                                            resetBaselineFormState()
                                                            return@LaunchedEffect
                                                        }
                                                        val refreshed = try {
                                                            current.refreshFromDatabase()
                                                        } catch (_: Exception) {
                                                            current
                                                        }
                                                        baselineToEdit = refreshed
                                                        mta = refreshed.mtaC.toString()
                                                        tempMax = refreshed.tempC.toString()
                                                        tempAmb = refreshed.ambC.toString()
                                                        notas = refreshed.notas
                                                        imgIr = refreshed.imgR ?: ""
                                                        imgId = refreshed.imgD ?: ""
                                                        irPreview = null
                                                        idPreview = null
                                                    }

                                                    fun filter2Dec(input: String): String {
                                                        if (input.isEmpty()) return ""
                                                        val norm = input.replace(',', '.')
                                                        val regex = Regex("^\\d*([.]\\d{0,2})?$")
                                                        return if (regex.matches(norm)) norm else norm.let { s ->
                                                            val idx = s.indexOf('.')
                                                            if (idx >= 0 && s.length > idx + 3) s.substring(0, idx + 3) else s
                                                        }
                                                    }

                                                    fun saveBitmapToImagenes(ctx: android.content.Context, bmp: android.graphics.Bitmap, prefix: String): String? {
                                                        return EticImageStore.saveBitmap(
                                                            context = ctx,
                                                            rootTreeUri = rootTreeUri,
                                                            inspectionNumero = inspectionNumero,
                                                            prefix = prefix,
                                                            bmp = bmp
                                                        )
                                                    }

                                                    val irCameraLauncher = rememberLauncherForActivityResult(
                                                        ActivityResultContracts.TakePicturePreview()
                                                    ) { bmp ->
                                                        if (bmp != null) {
                                                            val name = saveBitmapToImagenes(ctx, bmp, "IR")
                                                            if (name != null) {
                                                                imgIr = name
                                                                irPreview = bmp
                                                            }
                                                        }
                                                    }
                                                    val idCameraLauncher = rememberLauncherForActivityResult(
                                                        ActivityResultContracts.TakePicturePreview()
                                                    ) { bmp ->
                                                        if (bmp != null) {
                                                            val name = saveBitmapToImagenes(ctx, bmp, "ID")
                                                            if (name != null) {
                                                                imgId = name
                                                                idPreview = bmp
                                                            }
                                                        }
                                                    }
                                                    val irFolderLauncher = rememberLauncherForActivityResult(
                                                        ActivityResultContracts.GetContent()
                                                    ) { uri ->
                                                        if (uri != null) {
                                                            val name = copyProblemImageFromUri(
                                                                ctx,
                                                                rootTreeUri,
                                                                inspectionNumero,
                                                                uri,
                                                                "IR"
                                                            )
                                                            if (name != null) {
                                                                imgIr = name
                                                                irPreview = null
                                                            }
                                                        }
                                                    }
                                                    val idFolderLauncher = rememberLauncherForActivityResult(
                                                        ActivityResultContracts.GetContent()
                                                    ) { uri ->
                                                        if (uri != null) {
                                                            val name = copyProblemImageFromUri(
                                                                ctx,
                                                                rootTreeUri,
                                                                inspectionNumero,
                                                                uri,
                                                                "ID"
                                                            )
                                                            if (name != null) {
                                                                imgId = name
                                                                idPreview = null
                                                            }
                                                        }
                                                    }

                                                    val isBaselineValid by remember(mta, tempMax, tempAmb, imgIr, imgId) {
                                                        mutableStateOf(
                                                            mta.isNotBlank() &&
                                                                    tempMax.isNotBlank() &&
                                                                    tempAmb.isNotBlank() &&
                                                                    imgIr.isNotBlank() &&
                                                                    imgId.isNotBlank()
                                                        )
                                                    }

                                                    var baselineDialogOffset by remember { mutableStateOf(Offset.Zero) }
                                                    Dialog(
                                                        onDismissRequest = { },
                                                        properties = DialogProperties(
                                                            usePlatformDefaultWidth = true,
                                                            dismissOnClickOutside = false,
                                                            dismissOnBackPress = false
                                                        )
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .wrapContentSize(Alignment.Center)
                                                        ) {
                                                            Surface(
                                                                modifier = Modifier.offset {
                                                                    IntOffset(
                                                                        baselineDialogOffset.x.roundToInt(),
                                                                        baselineDialogOffset.y.roundToInt()
                                                                    )
                                                                },
                                                                shape = RoundedCornerShape(12.dp),
                                                                tonalElevation = 6.dp
                                                            ) {
                                                                Column(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .verticalScroll(rememberScrollState())
                                                                        .padding(top = 0.dp, bottom = 0.dp)
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .background(DIALOG_HEADER_TURQUOISE)
                                                                            .padding(start = 17.dp, end = 17.dp)
                                                                            .pointerInput(Unit) {
                                                                                detectDragGestures { change, dragAmount ->
                                                                                    change.consume()
                                                                                    baselineDialogOffset += Offset(dragAmount.x, dragAmount.y)
                                                                                }
                                                                            }
                                                                    ) {
                                                                        Box(
                                                                            modifier = Modifier
                                                                                .fillMaxWidth()
                                                                                .background(DIALOG_HEADER_TURQUOISE, RoundedCornerShape(6.dp))
                                                                                .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 4.dp)
                                                                        ) {
                                                                            Text(
                                                                                if (baselineToEdit == null) "Nuevo Baseline" else "Editar Baseline",
                                                                                color = Color.White,
                                                                                style = MaterialTheme.typography.titleMedium
                                                                            )
                                                                        }
                                                                    }
                                                                    Spacer(Modifier.height(6.dp))

                                                                    Column(
                                                                        modifier = Modifier.padding(start = 17.dp, end = 17.dp, bottom = 4.dp)
                                                                    ) {

                                                                Row(
                                                                    Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    BaselineDialogField(
                                                                        label = "MTA \u00B0C",
                                                                        required = true,
                                                                        value = mta,
                                                                        onValueChange = { mta = filter2Dec(it) },
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    BaselineDialogField(
                                                                        label = "MAX \u00B0C",
                                                                        required = true,
                                                                        value = tempMax,
                                                                        onValueChange = { tempMax = filter2Dec(it) },
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    BaselineDialogField(
                                                                        label = "AMB \u00B0C",
                                                                        required = true,
                                                                        value = tempAmb,
                                                                        onValueChange = { tempAmb = filter2Dec(it) },
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                }

                                                                Spacer(Modifier.height(8.dp))

                                                                BaselineDialogField(
                                                                    label = "Notas",
                                                                    value = notas,
                                                                    onValueChange = { notas = it },
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    singleLine = false,
                                                                    fieldHeight = 56.dp
                                                                )

                                                                Spacer(Modifier.height(8.dp))

                                                                Row(
                                                                    Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(Modifier.weight(1f)) {
                                                                        ImageInputButtonGroup(
                                                                            label = "Archivo IR",
                                                                            value = imgIr,
                                                                            onValueChange = { imgIr = it },
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            isRequired = true,
                                                                            enabled = true,
                                                                            onMoveUp = { imgIr = adjustImageSequence(imgIr, +1) },
                                                                            onMoveDown = { imgIr = adjustImageSequence(imgIr, -1) },
                                                                            onDotsClick = { imgIr = nextImageName(imgIr, "IR") },
                                                                            onFolderClick = { irFolderLauncher.launch("image/*") },
                                                                            onCameraClick = { irCameraLauncher.launch(null) }
                                                                        )
                                                                        Spacer(Modifier.height(4.dp))
                                                                        val bmp = irPreview ?: run {
                                                                            EticImageStore.loadBitmap(
                                                                                context = ctx,
                                                                                rootTreeUri = rootTreeUri,
                                                                                inspectionNumero = inspectionNumero,
                                                                                fileName = imgIr
                                                                            )
                                                                        }
                                                                        if (bmp != null) {
                                                                            androidx.compose.foundation.Image(
                                                                                bmp.asImageBitmap(),
                                                                                contentDescription = null,
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(200.dp)
                                                                            )
                                                                        } else {
                                                                            Box(
                                                                                Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(200.dp),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Icon(
                                                                                    Icons.Outlined.Image,
                                                                                    contentDescription = "Imagen no encontrada",
                                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                    Column(Modifier.weight(1f)) {
                                                                        ImageInputButtonGroup(
                                                                            label = "Archivo ID",
                                                                            value = imgId,
                                                                            onValueChange = { imgId = it },
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            isRequired = true,
                                                                            enabled = true,
                                                                            onMoveUp = { imgId = adjustImageSequence(imgId, +1) },
                                                                            onMoveDown = { imgId = adjustImageSequence(imgId, -1) },
                                                                            onDotsClick = { imgId = nextImageName(imgId, "ID") },
                                                                            onFolderClick = { idFolderLauncher.launch("image/*") },
                                                                            onCameraClick = { idCameraLauncher.launch(null) }
                                                                        )
                                                                        Spacer(Modifier.height(4.dp))
                                                                        val bmp2 = idPreview ?: run {
                                                                            EticImageStore.loadBitmap(
                                                                                context = ctx,
                                                                                rootTreeUri = rootTreeUri,
                                                                                inspectionNumero = inspectionNumero,
                                                                                fileName = imgId
                                                                            )
                                                                        }
                                                                        if (bmp2 != null) {
                                                                            androidx.compose.foundation.Image(
                                                                                bmp2.asImageBitmap(),
                                                                                contentDescription = null,
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(200.dp)
                                                                            )
                                                                        } else {
                                                                            Box(
                                                                                Modifier
                                                                                    .fillMaxWidth()
                                                                                    .height(200.dp),
                                                                                contentAlignment = Alignment.Center
                                                                            ) {
                                                                                Icon(
                                                                                    Icons.Outlined.Image,
                                                                                    contentDescription = "Imagen no encontrada",
                                                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                Spacer(Modifier.height(8.dp))

                                                                val rutaEquipo by produceState(initialValue = "", ubId) {
                                                                    value = if (!ubId.isNullOrBlank()) {
                                                                        runCatching {
                                                                            ubicacionDao.getById(ubId!!)?.ruta ?: ""
                                                                        }.getOrDefault("")
                                                                    } else ""
                                                                }
                                                                BaselineDialogField(
                                                                    label = "Ruta del equipo",
                                                                    value = rutaEquipo,
                                                                    onValueChange = {},
                                                                    readOnly = true,
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )

                                                                Spacer(Modifier.height(12.dp))
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Button(
                                                                        onClick = {
                                                                            if (!isSavingBaseline) showNewBaseline = false
                                                                        },
                                                                        enabled = !isSavingBaseline
                                                                    ) { Text("Cancelar") }
                                                                    Button(
                                                                        enabled = isBaselineValid && !isSavingBaseline,
                                                                        onClick = {
                                                                            if (isSavingBaseline) return@Button
                                                                            val idUb = ubId
                                                                            val idInsp = inspId
                                                                            if (idUb.isNullOrBlank() || idInsp.isNullOrBlank()) {
                                                                                return@Button
                                                                            }
                                                                            scope.launch {
                                                                                if (isSavingBaseline) return@launch
                                                                                isSavingBaseline = true
                                                                                try {
                                                                                    val nowTs = java.time.LocalDateTime.now()
                                                                                        .format(
                                                                                            java.time.format.DateTimeFormatter.ofPattern(
                                                                                                "yyyy-MM-dd HH:mm:ss"
                                                                                            )
                                                                                        )
                                                                                    val detRow = try {
                                                                                        inspeccionDetDao.getByUbicacion(idUb)
                                                                                            .firstOrNull { it.idInspeccion == idInsp }
                                                                                    } catch (_: Exception) {
                                                                                        null
                                                                                    }
                                                                                    val detId = detRow?.idInspeccionDet
                                                                                    val item = com.example.etic.data.local.entities.LineaBase(
                                                                                        idLineaBase = baselineToEdit?.id ?: java.util.UUID.randomUUID().toString()
                                                                                            .uppercase(),
                                                                                        idSitio = currentInspection?.idSitio,
                                                                                        idUbicacion = idUb,
                                                                                        idInspeccion = idInsp,
                                                                                        idInspeccionDet = detId,
                                                                                        mta = mta.toDoubleOrNull(),
                                                                                        tempMax = tempMax.toDoubleOrNull(),
                                                                                        tempAmb = tempAmb.toDoubleOrNull(),
                                                                                        notas = notas.ifBlank { null },
                                                                                        archivoIr = imgIr.ifBlank { null },
                                                                                        archivoId = imgId.ifBlank { null },
                                                                                        ruta = null,
                                                                                        estatus = "Activo",
                                                                                        creadoPor = currentUserId,
                                                                                        fechaCreacion = nowTs,
                                                                                        modificadoPor = null,
                                                                                        fechaMod = null
                                                                                    )
                                                                                    val ok = if (baselineToEdit == null) {
                                                                                        val exists = runCatching {
                                                                                            lineaBaseDao.existsActiveByUbicacionOrDet(idUb, detId)
                                                                                        }.getOrDefault(false)
                                                                                        if (exists) {
                                                                                            isSavingBaseline = false
                                                                                            return@launch
                                                                                        }
                                                                                        runCatching { lineaBaseDao.insert(item) }.isSuccess
                                                                                    } else {
                                                                                        runCatching { lineaBaseDao.update(item) }.isSuccess
                                                                                    }
                                                                                    if (ok) {
                                                                                        if (detRow != null) {
                                                                                            val updatedDet = detRow.copy(
                                                                                                idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                                                                                                idEstatusColorText = 3,
                                                                                                modificadoPor = currentUserId,
                                                                                                fechaMod = nowTs
                                                                                            )
                                                                                            runCatching { inspeccionDetDao.update(updatedDet) }
                                                                                            updateParentStatusesAfterManualChange(
                                                                                                inspectionId = idInsp,
                                                                                                startUbicacionId = idUb,
                                                                                                nowTs = nowTs
                                                                                            )
                                                                                        }
                                                                                        delay(3000)
                                                                                        showNewBaseline = false
                                                                                        baselineToEdit = null
                                                                                        baselineRefreshTick++
                                                                                        isSavingBaseline = false
                                                                                    } else {
                                                                                        isSavingBaseline = false
                                                                                    }
                                                                                } catch (_: Exception) {
                                                                                    isSavingBaseline = false
                                                                                }
                                                                            }
                                                                        }
                                                                    ) { Text("Guardar") }
                                                                }
                                                            }
                                                        }
                                                    }
                                                    }
                                                }
                                            }


                                            // -----------------------------------------------------------
                                            // TAB 1 → HISTÓRICO
                                            // -----------------------------------------------------------
                                            }
                                            1 -> {
                                                val ubId = editingUbId
                                                var histSortColumn by rememberSaveable { mutableStateOf("INSPECCION") }
                                                var histSortAsc by rememberSaveable { mutableStateOf(false) }
                                                val historyRows by produceState(
                                                    initialValue = emptyList<com.example.etic.data.local.dao.HistorialInspeccionRow>(),
                                                    ubId,
                                                    baselineRefreshTick,
                                                    problemsRefreshTick
                                                ) {
                                                    value = if (ubId.isNullOrBlank()) {
                                                        emptyList()
                                                    } else {
                                                        runCatching {
                                                            inspeccionDetDao.getHistorialInspeccionesByUbicacion(ubId)
                                                        }.getOrElse { emptyList() }
                                                    }
                                                }

                                                fun formatHistDate(raw: String?): String {
                                                    val onlyDate = raw?.takeIf { it.isNotBlank() }?.take(10) ?: return "-"
                                                    return runCatching {
                                                        java.time.LocalDate.parse(onlyDate)
                                                            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                                    }.getOrElse { onlyDate }
                                                }

                                                fun toggleHistSort(column: String) {
                                                    if (histSortColumn == column) histSortAsc = !histSortAsc
                                                    else {
                                                        histSortColumn = column
                                                        histSortAsc = true
                                                    }
                                                }

                                                val sortedHistoryRows = remember(historyRows, histSortColumn, histSortAsc) {
                                                    val comparator = when (histSortColumn) {
                                                        "INSPECCION" -> compareBy<com.example.etic.data.local.dao.HistorialInspeccionRow> { it.numInspeccion ?: Int.MIN_VALUE }
                                                        "FECHA" -> compareBy { it.fechaCreacionInspeccion ?: "" }
                                                        "ESTATUS" -> compareBy { it.estatusUbicacion ?: "" }
                                                        "NOTAS" -> compareBy { it.notasInspeccion ?: "" }
                                                        else -> compareBy<com.example.etic.data.local.dao.HistorialInspeccionRow> { it.numInspeccion ?: Int.MIN_VALUE }
                                                    }
                                                    if (histSortAsc) historyRows.sortedWith(comparator) else historyRows.sortedWith(comparator.reversed())
                                                }

                                                fun sortMarker(column: String): String {
                                                    if (histSortColumn != column) return ""
                                                    return if (histSortAsc) " ^" else " v"
                                                }

                                                val historyListState =
                                                    rememberSaveable("historial_inspecciones_state", saver = LazyListState.Saver) { LazyListState() }
                                                val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)

                                                Column(Modifier.fillMaxSize()) {
                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(90.dp)
                                                                .padding(horizontal = 4.dp)
                                                                .clickable { toggleHistSort("INSPECCION") }
                                                        ) {
                                                            Text("No. Insp${sortMarker("INSPECCION")}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .width(110.dp)
                                                                .padding(horizontal = 4.dp)
                                                                .clickable { toggleHistSort("FECHA") }
                                                        ) {
                                                            Text("Fecha${sortMarker("FECHA")}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .width(130.dp)
                                                                .padding(horizontal = 4.dp)
                                                                .clickable { toggleHistSort("ESTATUS") }
                                                        ) {
                                                            Text("Estatus${sortMarker("ESTATUS")}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(horizontal = 4.dp)
                                                                .clickable { toggleHistSort("NOTAS") }
                                                        ) {
                                                            Text("Notas${sortMarker("NOTAS")}", style = MaterialTheme.typography.bodySmall)
                                                        }
                                                    }
                                                    Divider(thickness = DIVIDER_THICKNESS)

                                                    if (sortedHistoryRows.isEmpty()) {
                                                        Box(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(8.dp),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) {
                                                            Text("Sin registros")
                                                        }
                                                    } else {
                                                        LazyColumn(
                                                            modifier = Modifier.fillMaxSize(),
                                                            state = historyListState
                                                        ) {
                                                            itemsIndexed(
                                                                sortedHistoryRows,
                                                                key = { _, item -> item.idInspeccionDet ?: "${item.idInspeccion}-${item.numInspeccion}" }
                                                            ) { index, row ->
                                                                val rowColor = if (index % 2 == 1) zebraColor else Color.Transparent
                                                                Row(
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .background(rowColor)
                                                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                                                ) {
                                                                    Box(modifier = Modifier.width(90.dp).padding(horizontal = 4.dp)) {
                                                                        Text((row.numInspeccion?.toString() ?: "-"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                    }
                                                                    Box(modifier = Modifier.width(110.dp).padding(horizontal = 4.dp)) {
                                                                        Text(formatHistDate(row.fechaCreacionInspeccion), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                    }
                                                                    Box(modifier = Modifier.width(130.dp).padding(horizontal = 4.dp)) {
                                                                        Text(row.estatusUbicacion.orEmpty().ifBlank { "-" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                    }
                                                                    Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                                                                        Text(row.notasInspeccion.orEmpty().ifBlank { "-" }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                                    }
                                                                }
                                                                Divider(thickness = DIVIDER_THICKNESS)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }


                                // ===============================================================
                                // BOTÓN CERRAR EN EL PIE DEL DIÁLOGO
                                // ===============================================================
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        enabled = !isSavingEditUb,
                                        onClick = {
                                            showEditUbDialog = false
                                            locationForm.error = null
                                        }
                                    ) {
                                        Text("Cerrar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // =====================================================================


            Row(Modifier.weight(hFrac)) {

                // Panel izquierdo
                val statusLabelMap = remember(statusOptions) {
                    statusOptions.associate { opt ->
                        opt.idStatusInspeccionDet to (opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet)
                    }
                }

                CellPanel(
                    borderColor = borderColor,
                    modifier = Modifier
                        .weight(vFrac)
                        .fillMaxHeight(),
                ) {
                    if (nodes.isNotEmpty()) {
                        SimpleTreeView(
                            nodes = nodes,
                            expanded = expanded.toSet(),
                            selectedId = selectedId,
                            highlightedId = highlightedId,
                            scrollToId = scrollToNodeId,
                            onScrollHandled = { scrollToNodeId = null },
                            onToggle = { id ->
                                val inspId = currentInspection?.idInspeccion
                                if (!expanded.remove(id)) expanded.add(id) else Unit
                                val isExpandedNow = expanded.contains(id)
                                if (!inspId.isNullOrBlank() && !id.startsWith("root:")) {
                                    treeScope.launch {
                                        runCatching {
                                            inspeccionDetDao.updateExpandedByUbicacion(
                                                inspId,
                                                id,
                                                if (isExpandedNow) "1" else "0"
                                            )
                                        }
                                    }
                                }
                            },
                            onSelect = onSelectNode,
                            onDoubleTap = { node -> startEditUb(node) },
                            modifier = Modifier.fillMaxSize() // ocupa todo el panel
                        )
                    } else {
                        Box(
                            Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin ubicaciones registradas")
                        }
                    }
                }

                // Handle vertical (mas suave)
                Box(
                    Modifier
                        .width(HANDLE_THICKNESS)
                        .fillMaxHeight()
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { deltaPx: Float ->
                                vFrac = (vFrac + deltaPx / totalWidthPx)
                                    .coerceIn(MIN_FRAC, MAX_FRAC)
                            }
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Panel derecho
                val weightRight = (1f - vFrac).coerceIn(0.05f, 0.95f) // evita 0 o negativos
                val children = remember(selectedId, nodes) {
                    findById(selectedId, nodes)?.children ?: emptyList()
                }

                CellPanel(
                    borderColor = borderColor,
                    modifier = Modifier
                        .weight(weightRight)
                        .fillMaxHeight(),
                ) {
                    DetailsTable(
                        children = children,
                        modifier = Modifier.fillMaxSize(),
                        selectedForStatus = checkedStatusLocationIds.toSet(),
                        onStatusCheckChanged = { node, checked ->
                            if (checked) {
                                if (!checkedStatusLocationIds.contains(node.id)) {
                                    checkedStatusLocationIds.add(node.id)
                                }
                            } else {
                                checkedStatusLocationIds.remove(node.id)
                            }
                        },
                        onDelete = { node ->
                            scope.launch {
                                val ubId = node.id
                                val inspId = currentInspection?.idInspeccion

                                // 1) Validar si tiene ubicaciones hijas activas
                                val hasChildren = runCatching {
                                    ubicacionDao.getAllActivas().any { it.idUbicacionPadre == ubId }
                                }.getOrDefault(false)
                                if (hasChildren) {
                                    deleteUbInfoMessage = "No se puede eliminar la ubicacion porque tiene ubicaciones hijas."
                                    return@launch
                                }

                                // 2) Validar si tiene baseline activo
                                val hasBaseline = runCatching {
                                    lineaBaseDaoGlobal.existsActiveByUbicacionOrDet(ubId, null)
                                }.getOrDefault(false)

                                // 3) Validar si tiene problemas activos
                                val hasProblems = runCatching {
                                    val problemas = if (!inspId.isNullOrBlank()) {
                                        problemaDao.getByInspeccionActivos(inspId)
                                    } else {
                                        val siteId = currentInspection?.idSitio
                                        if (!siteId.isNullOrBlank()) {
                                            problemaDao.getActivosPorSitio(siteId)
                                        } else {
                                            problemaDao.getAllActivos()
                                        }
                                    }
                                    problemas.any { it.idUbicacion == ubId && (it.estatus ?: "Activo") == "Activo" }
                                }.getOrDefault(false)

                                when {
                                    hasBaseline && hasProblems -> {
                                        deleteUbInfoMessage =
                                            "No se puede eliminar la ubicacion porque tiene baseline y problemas registrados."
                                    }
                                    hasBaseline -> {
                                        deleteUbInfoMessage =
                                            "No se puede eliminar la ubicacion porque tiene baseline registrado."
                                    }
                                    hasProblems -> {
                                        deleteUbInfoMessage =
                                            "No se puede eliminar la ubicacion porque tiene problemas registrados."
                                    }
                                    else -> {
                                        // Sin hijos, sin baseline y sin problemas: pedir confirmacion
                                        // Sin hijos, sin baseline y sin problemas: marcar para confirmar
                                        deleteUbConfirmNode = node
                                    }
                                }
                            }
                        },
                        onEdit = { node ->
                            startEditUb(node)
                        },
                        statusNameForId = { id -> id?.let { statusLabelMap[it] } }
                    )
                }
            }

            // Handle horizontal
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(HANDLE_THICKNESS)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { deltaPx: Float ->
                            hFrac = (hFrac + deltaPx / totalHeightPx)
                                .coerceIn(MIN_FRAC, MAX_FRAC)
                        }
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Panel inferior
            CellPanel(
                borderColor = borderColor,
                modifier = Modifier
                    .weight(1f - hFrac)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                ) {
                ListTabs(
                    node = findById(selectedId, nodes),
                    onDeleteProblem = { p ->
                        val cur = findById(selectedId, nodes)
                        cur?.problems?.remove(p)
                    },
                    onDeleteBaseline = { b ->
                        val cur = findById(selectedId, nodes)
                        cur?.baselines?.remove(b)
                    },
                    onProblemDeleted = {
                        problemsRefreshTick++
                        scope.launch {
                            refreshTree(preserveSelection = selectedId)
                        }
                    },
                    baselineRefreshTick = baselineRefreshTick,
                    onBaselineChanged = { baselineRefreshTick++ },
                    problemsRefreshTick = problemsRefreshTick,
                    modifier = Modifier.fillMaxSize(),  // asegura ocupar todo el espacio
                    onProblemDoubleTap = { problem, visibleProblems ->
                        val inspectionPrevious = computePreviousInspectionNumber(
                            currentInspection?.noInspeccion,
                            visibleProblems
                        )
                        if (shouldBlockProblemOpen(problem, inspectionPrevious)) {
                            Toast.makeText(
                                ctx,
                                "Este problema cerrado pertenece a una inspeccion pasada.",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@ListTabs
                        }
                        val navList = buildNavigationList(visibleProblems, inspectionPrevious)
                        val index = navList.indexOfFirst { it.id == problem.id }
                        resetProblemNavigation()
                        problemNavList = navList
                        problemNavIndex = index
                        openProblemForNavigation(problem)
                    },
                    onNewProblem = {
                        cronicoActionEnabled = false
                        pendingInspectionNumber = currentInspection?.noInspeccion?.toString() ?: "-"
                        val currentSelection = selectedId
                        val node = currentSelection?.let { findById(it, nodes) }
                        if (currentSelection.isNullOrBlank() || currentSelection.startsWith("root:") || node == null) {
                            showNoSelectionDialog = true
                        } else {
                            scope.launch {
                                resetVisualProblemForm()
                                val hasBaseline = withContext(Dispatchers.IO) {
                                    runCatching {
                                        lineaBaseDaoGlobal.existsActiveByUbicacion(currentSelection)
                                    }.getOrDefault(false)
                                }
                                if (hasBaseline) {
                                    showBaselineRestrictionDialog = true
                                } else if (node.verified) {
                                    val electricTypeId = ELECTRIC_PROBLEM_TYPE_ID
                                    if (electricTypeId.isNullOrBlank()) {
                                        Toast.makeText(ctx, "Tipo eléctrico no disponible.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        selectedProblemType = problemTypeLabelForId(electricTypeId)
                                        pendingProblemEquipmentName = node.title
                                        pendingProblemRoute = titlePathForId(nodes, currentSelection).joinToString(" / ")
                                        pendingProblemUbicacionId = currentSelection
                                        pendingProblemType = problemTypeLabelForId(electricTypeId)
                                        ensureVisualDefaults()
                                        showProblemTypeDialog = true
                                    }
                                } else {
                                    pendingProblemEquipmentName = node.title
                                    pendingProblemRoute = titlePathForId(nodes, currentSelection).joinToString(" / ")
                                    pendingProblemUbicacionId = currentSelection
                                    pendingProblemType = problemTypeLabelForId(VISUAL_PROBLEM_TYPE_ID)
                                    pendingProblemNumber = fetchNextProblemNumber(VISUAL_PROBLEM_TYPE_ID)
                                    ensureVisualDefaults()
                                    showVisualInspectionWarning = true
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CellPanel(
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = DIVIDER_THICKNESS, color = borderColor)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(PANEL_PADDING)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = SURFACE_VARIANT_ALPHA)
                )
        ) {
            content()
        }
    }
}

// -------------------------
// TreeView simple
// -------------------------

private data class FlatNode(val node: TreeNode, val depth: Int, val hasChildren: Boolean, val expanded: Boolean)

@Composable
private fun SimpleTreeView(
    nodes: List<TreeNode>,
    expanded: Set<String>,
    selectedId: String?,
    highlightedId: String?,
    scrollToId: String?,
    onScrollHandled: () -> Unit,
    onToggle: (String) -> Unit,
    onSelect: (String) -> Unit,
    onDoubleTap: (TreeNode) -> Unit,
    modifier: Modifier = Modifier
) {
    fun flatten(list: List<TreeNode>, depth: Int, out: MutableList<FlatNode>) {
        for (n in list) {
            val has = n.children.isNotEmpty()
            val isExp = expanded.contains(n.id)
            out += FlatNode(n, depth, has, isExp)
            if (has && isExp) flatten(n.children, depth + 1, out)
        }
    }
    val flat = remember(nodes, expanded) { mutableListOf<FlatNode>().also { flatten(nodes, 0, it) } }
    val selColor = Color(0xFFE1BEE7) // violeta suave
    val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
    val treeListState = rememberSaveable("tree_list_state", saver = LazyListState.Saver) { LazyListState() }
    val rootNodeId = nodes.firstOrNull()?.id
    val hasValidSelection = selectedId != null && flat.any { it.node.id == selectedId }
    val effectiveSelectedId = if (hasValidSelection) selectedId else rootNodeId
    LaunchedEffect(hasValidSelection, rootNodeId) {
        if (!hasValidSelection && rootNodeId != null) {
            onSelect(rootNodeId)
        }
    }
    LaunchedEffect(scrollToId, flat.size) {
        val targetId = scrollToId ?: return@LaunchedEffect
        val targetIndex = flat.indexOfFirst { it.node.id == targetId }
        if (targetIndex >= 0) {
            val isVisible = treeListState.layoutInfo.visibleItemsInfo.any { it.index == targetIndex }
            if (!isVisible) {
                treeListState.animateScrollToItem(targetIndex)
            }
        }
        onScrollHandled()
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth(), state = treeListState) {
            itemsIndexed(flat, key = { _, item -> item.node.id }) { index, item ->
                val n = item.node
                val isSelected = effectiveSelectedId == n.id
                val rowBackground = when {
                    isSelected -> selColor
                    index % 2 == 1 -> zebraColor
                    else -> Color.Transparent
                }
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(rowBackground)
                        .pointerInput(n.id) {
                            detectTapGestures(
                                onTap = { onSelect(n.id) },
                                onDoubleTap = { onDoubleTap(n) }
                            )
                        }
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = (item.depth * TREE_INDENT.value).dp, end = TREE_SPACING),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (item.hasChildren) {
                            Icon(
                                imageVector = if (item.expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(TREE_TOGGLE_SIZE)
                                    .clickable { onToggle(n.id) }
                            )
                        } else {
                            Spacer(Modifier.width(TREE_TOGGLE_SIZE))
                        }
                        // Icono: nodo raiz (sitio) usa Factory; demas segun esEquipo
                        val nodeIcon = when {
                            item.depth == 0 -> Icons.Outlined.Factory
                            n.verified -> Icons.Outlined.Traffic
                            else -> Icons.Filled.DragIndicator
                        }
                        val tintColor = if (item.depth == 0) ICON_NO_EQUIPO_COLOR else if (n.verified) ICON_EQUIPO_COLOR else ICON_NO_EQUIPO_COLOR
                        Icon(nodeIcon, contentDescription = null, tint = tintColor, modifier = Modifier.size(TREE_ICON_SIZE))
                        Spacer(Modifier.width(TREE_SPACING))
                        // Si el nodo corresponde a estatus de texto 1 (no inspeccionado),
                        // no usamos color fijo y dejamos que el tema defina el color.
                        val baseColor = if (n.idStatusInspeccionDet == "568798D1-76BB-11D3-82BF-00104BC75DC2") {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            n.textColorHex?.let { raw ->
                                val hex = raw.trim()
                                when {
                                    hex.startsWith("#") -> {
                                        runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrNull()
                                    }
                                    hex.startsWith("0x", ignoreCase = true) -> {
                                        runCatching {
                                            val intValue = hex.removePrefix("0x").removePrefix("0X").toLong(16).toInt()
                                            Color(intValue)
                                        }.getOrNull()
                                    }
                                    else -> null
                                }
                            } ?: MaterialTheme.colorScheme.onSurface
                        }
                        val textColor = if (n.id == highlightedId) MaterialTheme.colorScheme.error else baseColor
                        Text(
                            n.title,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Divider(thickness = DIVIDER_THICKNESS)
                }
            }
        }
    }
}

private data class ImageNameParts(val prefix: String, val number: Long, val digits: Int, val suffix: String)

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

private fun nextImageName(lastName: String?, prefix: String): String {
    val base = lastName?.takeIf { it.isNotBlank() } ?: "${prefix}0000"
    val next = adjustImageSequence(base, +1)
    return if (next == base) "${prefix}0001" else next
}

private fun saveProblemBitmap(
    ctx: Context,
    rootTreeUri: Uri?,
    inspectionNumero: String?,
    bmp: Bitmap,
    prefix: String
): String? {
    return EticImageStore.saveBitmap(
        context = ctx,
        rootTreeUri = rootTreeUri,
        inspectionNumero = inspectionNumero,
        prefix = prefix,
        bmp = bmp
    )
}

private fun copyProblemImageFromUri(
    ctx: Context,
    rootTreeUri: Uri?,
    inspectionNumero: String?,
    uri: Uri,
    prefix: String
): String? {
    return EticImageStore.copyFromUri(
        context = ctx,
        rootTreeUri = rootTreeUri,
        inspectionNumero = inspectionNumero,
        prefix = prefix,
        uri = uri
    )
}

// -------------------------
// Tablas
// -------------------------

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsTable(
    children: List<TreeNode>,
    selectedForStatus: Set<String>,
    onStatusCheckChanged: (TreeNode, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (TreeNode) -> Unit,
    onEdit: (TreeNode) -> Unit,
    statusNameForId: (String?) -> String?
) {
    Column(
        modifier
            .fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            HeaderCell("Ubicación", 3)
            HeaderCell("Código de barras", 2)
            HeaderCell("Estatus", 2)
            HeaderCell("", 1)
        }
        Divider(thickness = DIVIDER_THICKNESS)

        val listState = rememberSaveable("details_list_state", saver = LazyListState.Saver) { LazyListState() }
        val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
        if (children.isEmpty()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Sin elementos")
            }
        } else {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState
            ) {
                itemsIndexed(children, key = { _, item -> item.id }) { index, n ->
                    val rowColor = if (index % 2 == 1) zebraColor else Color.Transparent
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(rowColor)
                            .padding(vertical = 0.dp, horizontal = 4.dp)
                            .pointerInput(n.id) {
                                detectTapGestures(onDoubleTap = { onEdit(n) })
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BodyCell(3) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement provides false
                                ) {
                                    Checkbox(
                                        checked = selectedForStatus.contains(n.id),
                                        onCheckedChange = { onStatusCheckChanged(n, it) },
                                        modifier = Modifier
                                            .size(16.dp)
                                            .scale(0.78f)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(n.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        BodyCell(2) { Text(n.barcode ?: "-") }
                        val statusLabel = n.estatusInspeccionDet ?: statusNameForId(n.idStatusInspeccionDet)
                        BodyCell(2) { Text(statusLabel ?: "Por verificar") }
                        BodyCell(1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement provides false
                                ) {
                                    IconButton(onClick = { onDelete(n) }, modifier = Modifier.size(28.dp)) {
                                        Icon(
                                            Icons.Outlined.Delete,
                                            contentDescription = "Eliminar",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Divider(thickness = DIVIDER_THICKNESS)
                }
            }
        }
    }
}

@Composable private fun RowScope.HeaderCell(text: String, flex: Int) {
    Box(Modifier.weight(flex.toFloat())) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable private fun RowScope.BodyCell(flex: Int, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.weight(flex.toFloat()),
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    }
}

@Composable
private fun ListTabs(
    node: TreeNode?,
    onDeleteProblem: (Problem) -> Unit,
    onDeleteBaseline: (Baseline) -> Unit,
    onProblemDeleted: (() -> Unit)? = null,
    baselineRefreshTick: Int,
    onBaselineChanged: () -> Unit,
    problemsRefreshTick: Int,
    modifier: Modifier = Modifier,
    onProblemDoubleTap: ((Problem, List<Problem>) -> Unit)? = null,
    onNewProblem: (() -> Unit)? = null
) {
    // Nota: mostramos versiones ligadas a BD; no usamos listas calculadas por nodo aqu?
    var tab by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(com.example.etic.R.string.tab_problemas)) })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(com.example.etic.R.string.tab_baseline)) })
        }
        Divider(thickness = DIVIDER_THICKNESS)
        val showProblems = tab == 0
        var typeFilterId by rememberSaveable {
            mutableStateOf(PROBLEM_TYPE_FILTER_OPTIONS.firstOrNull()?.first.orEmpty())
        }
        var statusFilterId by rememberSaveable {
            mutableStateOf(PROBLEM_STATUS_OPEN_PAST)
        }
        
        if (showProblems) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(FILTER_FIELD_ROW_SPACING),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterDropdownField(
                        label = "Tipo",
                        options = PROBLEM_TYPE_FILTER_OPTIONS.map { it.first as String? to it.second },
                        selectedId = typeFilterId,
                        onSelected = { typeFilterId = it ?: "" }
                    )

                    FilterDropdownField(
                        label = "Estatus",
                        options = PROBLEM_STATUS_FILTER_OPTIONS.map { it.first as String? to it.second },
                        selectedId = statusFilterId,
                        onSelected = { statusFilterId = it ?: PROBLEM_STATUS_ALL }
                    )
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { onNewProblem?.invoke() },
                    enabled = onNewProblem != null,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = 0.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(text = "Nuevo Problema")
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            ProblemsTableFromDatabase(
                selectedId = node?.id,
                refreshTick = problemsRefreshTick,
                typeFilterId = typeFilterId,
                statusFilterId = statusFilterId,
                onProblemDeleted = onProblemDeleted,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (showProblems) 1f else 0f)
                    .zIndex(if (showProblems) 1f else 0f),
                onProblemDoubleTap = onProblemDoubleTap
            )
        BaselineTableFromDatabase(
            selectedId = node?.id,
            refreshTick = baselineRefreshTick,
            onBaselineChanged = onBaselineChanged,
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (showProblems) 0f else 1f)
                .zIndex(if (showProblems) 0f else 1f)
            )
        }
    }
}

@Composable
internal fun ProblemsTable(
    problems: List<Problem>,
    onDelete: (Problem) -> Unit,
    onDoubleTap: ((Problem, List<Problem>) -> Unit)? = null
) {
    val currentInspectionId = LocalCurrentInspection.current?.idInspeccion
    // ─────────────────────────────
    // Anchos fijos (Opción A)
    // ─────────────────────────────
    val wNo = 55.dp
    val wFecha = 95.dp
    val wInspeccion = 85.dp
    val wTipo = 81.dp
    val wEstatus = 80.dp
    val wCronico = 82.dp
    val wTempC = 87.dp
    val wDeltaT = 97.dp
    val wSeveridad = 97.dp
    val wEquipo = 163.dp
    val wComentarios = 300.dp
    val wOp = 32.dp

    val tableMinWidth =
        wNo + wFecha + wInspeccion + wTipo + wEstatus + wCronico +
                wTempC + wDeltaT + wSeveridad + wEquipo + wComentarios + wOp

    val headerBackground = MaterialTheme.colorScheme.surface

    // Helper: header cell (ancho + color)
    @Composable
    fun RowScope.headerCell(
        width: Dp,
        background: Color,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .width(width)
                .background(background)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) { content() }
    }

    // Helper: body cell (ancho fijo)
    @Composable
    fun RowScope.cellFixed(
        width: Dp,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            modifier = modifier
                .width(width)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) { content() }
    }

    // ─────────────────────────────
    // Estado de ordenamiento
    // ─────────────────────────────
    var sortColumn by rememberSaveable { mutableStateOf(ProblemColumn.FECHA) }
    var sortAsc by rememberSaveable { mutableStateOf(true) }

    fun toggleSort(column: ProblemColumn) {
        if (sortColumn == column) sortAsc = !sortAsc
        else {
            sortColumn = column
            sortAsc = true
        }
    }

    val sortedProblems = remember(problems, sortColumn, sortAsc) {
        val baseComparator = when (sortColumn) {
            ProblemColumn.NUMERO -> compareBy<Problem> { it.no }
            ProblemColumn.FECHA -> compareBy { it.fecha }
            ProblemColumn.INSPECCION -> compareBy { it.numInspeccion }
            ProblemColumn.TIPO -> compareBy { it.tipo }
            ProblemColumn.ESTATUS -> compareBy { it.estatus }
            ProblemColumn.CRONICO -> compareBy { it.cronico }
            ProblemColumn.TEMP_C -> compareBy { it.tempC }
            ProblemColumn.DELTA_T -> compareBy { it.deltaTC }
            ProblemColumn.SEVERIDAD -> compareBy { it.severidad }
            ProblemColumn.EQUIPO -> compareBy { it.equipo }
            ProblemColumn.COMENTARIOS -> compareBy { it.comentarios }
        }
        if (sortAsc) problems.sortedWith(baseComparator) else problems.sortedWith(baseComparator.reversed())
    }

    val listState = rememberLazyListState()
    val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
    val hScroll = rememberScrollState()

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(hScroll)
        ) {
            Column(Modifier.widthIn(min = tableMinWidth)) {

                // ───────── HEADER ─────────
                Row(Modifier.fillMaxWidth()) {

                    headerCell(wNo, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.NUMERO) }) {
                        SortHeader(
                            label = "No.",
                            column = ProblemColumn.NUMERO,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wFecha, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.FECHA) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_fecha),
                            column = ProblemColumn.FECHA,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wInspeccion, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.INSPECCION) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_num_inspeccion),
                            column = ProblemColumn.INSPECCION,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wTipo, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.TIPO) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_tipo),
                            column = ProblemColumn.TIPO,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wEstatus, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.ESTATUS) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_estatus),
                            column = ProblemColumn.ESTATUS,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wCronico, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.CRONICO) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_cronico),
                            column = ProblemColumn.CRONICO,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wTempC, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.TEMP_C) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_temp_c),
                            column = ProblemColumn.TEMP_C,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wDeltaT, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.DELTA_T) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_delta_t_c),
                            column = ProblemColumn.DELTA_T,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wSeveridad, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.SEVERIDAD) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_severidad),
                            column = ProblemColumn.SEVERIDAD,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    headerCell(wEquipo, headerBackground, Modifier.clickable { toggleSort(ProblemColumn.EQUIPO) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_equipo),
                            column = ProblemColumn.EQUIPO,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }

                    /*headerCell(wComentarios, cComentarios, Modifier.clickable { toggleSort(ProblemColumn.COMENTARIOS) }) {
                        SortHeader(
                            label = stringResource(com.example.etic.R.string.col_comentarios),
                            column = ProblemColumn.COMENTARIOS,
                            sortColumn = sortColumn,
                            sortAsc = sortAsc
                        )
                    }*/

                    headerCell(wComentarios, headerBackground) {
                        Text(stringResource(com.example.etic.R.string.col_comentarios))
                    }

                    headerCell(wOp, headerBackground) {
                        Text(stringResource(com.example.etic.R.string.col_op))
                    }
                }

                Divider(thickness = DIVIDER_THICKNESS)

                // ───────── BODY ─────────
                if (sortedProblems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(com.example.etic.R.string.msg_sin_problemas))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState
                    ) {
                        itemsIndexed(sortedProblems, key = { _, item -> item.id }) { index, p ->
                            val rowColor = if (index % 2 == 1) zebraColor else Color.Transparent
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowColor)
                                    .padding(vertical = 6.dp)
                                    .pointerInput(p.id) {
                                        detectTapGestures(onDoubleTap = { onDoubleTap?.invoke(p, sortedProblems) })
                                    }
                            ) {
                                cellFixed(wNo) { Text("${p.no}") }
                                cellFixed(wFecha) { Text(p.fecha.format(PROBLEM_DATE_FORMATTER)) }
                                cellFixed(wInspeccion) { Text(p.numInspeccion) }
                                cellFixed(wTipo) { Text(p.tipo) }
                                cellFixed(wEstatus) { Text(p.estatus) }
                                cellFixed(wCronico) { Text(if (p.cronico) "SI" else "NO") }
                                cellFixed(wTempC) { Text(p.tempC.toString()) }
                                cellFixed(wDeltaT) { Text(p.deltaTC.toString()) }
                                cellFixed(wSeveridad) { Text(p.severidad) }
                                cellFixed(wEquipo) { Text(p.equipo) }
                                cellFixed(wComentarios) { Text(p.comentarios) }
                                cellFixed(wOp, Modifier.padding(horizontal = 0.dp)) {
                                    val isOpen = p.estatus.equals("Abierto", ignoreCase = true)
                                    val belongsToCurrentInspection = p.inspectionId?.equals(currentInspectionId, ignoreCase = true) == true
                                    if (isOpen && belongsToCurrentInspection) {
                                        IconButton(onClick = { onDelete(p) }) {
                                            Icon(
                                                Icons.Outlined.Delete,
                                                contentDescription = "Eliminar",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Divider(thickness = DIVIDER_THICKNESS)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────
// Header con flecha delgada (Icon)
// ─────────────────────────────
@Composable
private fun SortHeader(
    label: String,
    column: ProblemColumn,
    sortColumn: ProblemColumn,
    sortAsc: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (sortColumn == column) {
            Icon(
                imageVector = if (sortAsc) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp) // 👈 más delgada / ocupa menos
            )
        }
    }
}

@Composable
private fun BaselineSortHeader(
    label: String,
    column: BaselineColumn,
    sortColumn: BaselineColumn,
    sortAsc: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (sortColumn == column) {
            Icon(
                imageVector = if (sortAsc) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
internal fun BaselineTable(
    baselines: List<Baseline>,
    onDelete: (Baseline) -> Unit,
    onDoubleTap: ((Baseline, List<Baseline>) -> Unit)? = null
) {
    @Composable
    fun RowScope.cell(flex: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
        Box(Modifier.weight(flex.toFloat()).then(modifier)) { content() }

    @Composable
    fun RowScope.headerCell(
        flex: Int,
        background: Color,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            Modifier
                .weight(flex.toFloat())
                .background(background)
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .then(modifier)
        ) { content() }
    }

    @Composable
    fun RowScope.headerCellFixed(
        width: Dp,
        background: Color,
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit
    ) {
        Box(
            Modifier
                .width(width)
                .background(background)
                .padding(horizontal = 6.dp, vertical = 8.dp)
                .then(modifier)
        ) { content() }
    }

    @Composable
    fun RowScope.cellFixed(width: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
        Box(Modifier.width(width).then(modifier)) { content() }

    val listState = rememberSaveable("baseline_list_state", saver = LazyListState.Saver) { LazyListState() }
    val zebraColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
    var sortColumn by rememberSaveable { mutableStateOf(BaselineColumn.FECHA) }
    var sortAsc by rememberSaveable { mutableStateOf(true) }

    fun toggleSort(column: BaselineColumn) {
        if (sortColumn == column) sortAsc = !sortAsc else {
            sortColumn = column
            sortAsc = true
        }
    }

    val sortedBaselines = remember(baselines, sortColumn, sortAsc) {
        val comparator = when (sortColumn) {
            BaselineColumn.INSPECCION -> compareBy<Baseline> { it.numInspeccion }
            BaselineColumn.EQUIPO -> compareBy { it.equipo }
            BaselineColumn.FECHA -> compareBy { it.fecha }
            BaselineColumn.MTA -> compareBy { it.mtaC }
            BaselineColumn.TEMP -> compareBy { it.tempC }
            BaselineColumn.AMB -> compareBy { it.ambC }
            BaselineColumn.IR -> compareBy { it.imgR ?: "" }
            BaselineColumn.ID -> compareBy { it.imgD ?: "" }
            BaselineColumn.NOTAS -> compareBy { it.notas }
        }
        if (sortAsc) baselines.sortedWith(comparator) else baselines.sortedWith(comparator.reversed())
    }

    val headerBackground = MaterialTheme.colorScheme.surface

    val wInspeccion = 85.dp
    val wEquipo = 208.dp
    val wFecha = 95.dp
    val wMta = 83.dp
    val wTemp = 85.dp
    val wAmb = 83.dp
    val wIr = 133.dp
    val wId = 133.dp
    val wNotas = 313.dp
    val wOp = 27.dp

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            headerCellFixed(wInspeccion, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.INSPECCION) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_no_inspeccion), column = BaselineColumn.INSPECCION, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wEquipo, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.EQUIPO) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_equipo), column = BaselineColumn.EQUIPO, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wFecha, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.FECHA) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_fecha), column = BaselineColumn.FECHA, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wMta, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.MTA) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_mta_c), column = BaselineColumn.MTA, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wTemp, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.TEMP) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_temp_c), column = BaselineColumn.TEMP, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wAmb, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.AMB) }) {
                BaselineSortHeader(label = stringResource(com.example.etic.R.string.col_amb_c), column = BaselineColumn.AMB, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wIr, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.IR) }) {
                BaselineSortHeader(label = "IR", column = BaselineColumn.IR, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wId, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.ID) }) {
                BaselineSortHeader(label = "ID", column = BaselineColumn.ID, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wNotas, headerBackground, Modifier.clickable { toggleSort(BaselineColumn.NOTAS) }) {
                BaselineSortHeader(label = "Notas", column = BaselineColumn.NOTAS, sortColumn = sortColumn, sortAsc = sortAsc)
            }
            headerCellFixed(wOp, headerBackground) { Text("") }
        }
        Divider(thickness = DIVIDER_THICKNESS)
        if (baselines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(com.example.etic.R.string.msg_sin_baseline)) }
        } else {
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                itemsIndexed(sortedBaselines, key = { _, item -> item.id }) { index, b ->
                    val rowColor = if (index % 2 == 1) zebraColor else Color.Transparent
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(rowColor)
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                            .pointerInput(b.id) {
                                detectTapGestures(onDoubleTap = {
                                    onDoubleTap?.invoke(b, sortedBaselines)
                                })
                            }
                    ) {
                        cellFixed(wInspeccion) { Text(b.numInspeccion) }
                        cellFixed(wEquipo) { Text(b.equipo) }
                        cellFixed(wFecha) { Text(b.fecha.format(PROBLEM_DATE_FORMATTER)) }
                        cellFixed(wMta) { Text(b.mtaC.toString()) }
                        cellFixed(wTemp) { Text(b.tempC.toString()) }
                        cellFixed(wAmb) { Text(b.ambC.toString()) }
                        cellFixed(wIr) { Text(b.imgR ?: "") }
                        cellFixed(wId) { Text(b.imgD ?: "") }
                        cellFixed(wNotas) { Text(b.notas) }
                        cellFixed(wOp) {
                            IconButton(onClick = { onDelete(b) }) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Eliminar",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Divider(thickness = DIVIDER_THICKNESS)
                }
            }
        }
    }
}

@Composable
private fun BaselineDialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    fieldHeight: Dp = 30.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            if (required) Text(" *", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (readOnly) {
                Text(value, style = MaterialTheme.typography.bodySmall)
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(widthDp = 1200, heightDp = 800)
@Composable
private fun PreviewInspection() { EticTheme { InspectionScreen() } }

// -------------------------
// DB-backed Problems table
// -------------------------


private fun buildTreeFromVista(rows: List<com.example.etic.data.local.views.VistaUbicacionArbol>): MutableList<TreeNode> {
    android.util.Log.d("VistaUbicacionArbol", "Filas obtenidas en buildTreeFromVista: ${rows.size}")
    rows.forEach { r ->
        android.util.Log.d(
            "VistaUbicacionArbol",
            "insp=${r.idInspeccion} det=${r.idInspeccionDet} id=${r.idUbicacion} padre=${r.idUbicacionPadre} nombre=${r.nombreUbicacion}"
        )
    }

    val byId = mutableMapOf<String, TreeNode>()
    val roots = mutableListOf<TreeNode>()

    rows.forEach { r ->
        val node = TreeNode(
            id = r.idUbicacion,
            title = r.nombreUbicacion ?: "(Sin nombre)",
            barcode = r.codigoBarras,
            verified = (r.esEquipo ?: "").equals("SI", ignoreCase = true),
            textColorHex = r.color,
            estatusInspeccionDet = r.estatusInspeccionDet,
            idStatusInspeccionDet = r.idStatusInspeccionDet
        )
        byId[r.idUbicacion] = node
    }

    rows.forEach { r ->
        val node = byId[r.idUbicacion] ?: return@forEach
        val parentId = r.idUbicacionPadre?.takeIf { it.isNotBlank() && it != "0" }
        if (parentId != null) {
            val parent = byId[parentId]
            if (parent != null) parent.children.add(node) else roots.add(node)
        } else {
            roots.add(node)
        }
    }

    return roots
}




