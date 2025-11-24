package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Factory
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.etic.ui.theme.EticTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.etic.data.local.entities.EstatusInspeccionDet
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.LocalCurrentUser
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.LinkedHashSet
import kotlin.collections.buildList
import com.example.etic.features.inspection.data.InspectionRepository
import com.example.etic.features.inspection.data.UbicacionSaveContext
import com.example.etic.features.inspection.ui.components.InspectionHeader
import com.example.etic.features.inspection.ui.components.NewLocationDialog
import com.example.etic.features.inspection.ui.state.rememberLocationFormState
import com.example.etic.features.inspection.tree.Baseline
import com.example.etic.features.inspection.tree.Problem
import com.example.etic.features.inspection.tree.TreeNode
import com.example.etic.features.inspection.tree.buildTreeFromVista
import com.example.etic.features.inspection.tree.collectBaselines
import com.example.etic.features.inspection.tree.collectProblems
import com.example.etic.features.inspection.tree.depthOfId
import com.example.etic.features.inspection.tree.descendantIds
import com.example.etic.features.inspection.tree.findById
import com.example.etic.features.inspection.tree.findPathByBarcode
import com.example.etic.features.inspection.tree.titlePathForId
import androidx.compose.ui.window.Dialog

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
    val ubicacionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val vistaUbicacionArbolDao = remember { com.example.etic.data.local.DbProvider.get(ctx).vistaUbicacionArbolDao() }
    val usuarioDao = remember { com.example.etic.data.local.DbProvider.get(ctx).usuarioDao() }
    val inspeccionDetDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDetDao() }
    val inspectionRepository = remember {
        InspectionRepository(ubicacionDao, inspeccionDetDao, vistaUbicacionArbolDao)
    }
    val problemaDao = remember { com.example.etic.data.local.DbProvider.get(ctx).problemaDao() }
    val lineaBaseDaoGlobal = remember { com.example.etic.data.local.DbProvider.get(ctx).lineaBaseDao() }
    val currentInspection = LocalCurrentInspection.current
    val rootTitle = currentInspection?.nombreSitio ?: "Sitio"
    val rootId = remember(currentInspection?.idSitio) { (currentInspection?.idSitio?.let { "root:$it" } ?: "root:site") }
    val expanded = remember { mutableStateListOf<String>() }
        // Centralizar seleccion como si fuera un tap del usuario
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var highlightedId by remember { mutableStateOf<String?>(null) }
    var baselineRefreshTick by remember { mutableStateOf(0) }
    var hasSignaledReady by rememberSaveable { mutableStateOf(false) }
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

    // ---------- Estados NUEVOS para el dialogo de edicion con tabs ----------
    var showEditUbDialog by remember { mutableStateOf(false) }
    var isSavingEditUb by remember { mutableStateOf(false) }
    var editTab by rememberSaveable { mutableStateOf(0) }
    // -----------------------------------------------------------------------

    LaunchedEffect(showEditUbDialog) {
        if (!showEditUbDialog) {
            isSavingEditUb = false
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Convierte dimensiones del BoxWithConstraints a pixeles de forma segura
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.value * density.density }
        val totalHeightPx = with(density) { maxHeight.value * density.density }

        Column(Modifier.fillMaxSize()) {

            // Controles superiores (fuera de los paneles)
            var barcode by rememberSaveable { mutableStateOf("") }
            var statusMenuExpanded by remember { mutableStateOf(false) }
            var selectedStatusLabel by rememberSaveable { mutableStateOf("Todos") }
            var selectedStatusId by rememberSaveable { mutableStateOf<String?>(null) }
            var statusOptions by remember { mutableStateOf<List<EstatusInspeccionDet>>(emptyList()) }
            val estatusDao = remember { com.example.etic.data.local.DbProvider.get(ctx).estatusInspeccionDetDao() }
            LaunchedEffect(Unit) {
                statusOptions = runCatching { estatusDao.getAll() }.getOrElse { emptyList() }
            }
            // Tipo de prioridad y fabricantes
            var prioridadOptions by remember { mutableStateOf<List<com.example.etic.data.local.entities.TipoPrioridad>>(emptyList()) }
            var fabricanteOptions by remember { mutableStateOf<List<com.example.etic.data.local.entities.Fabricante>>(emptyList()) }
            val prioridadDao = remember { com.example.etic.data.local.DbProvider.get(ctx).tipoPrioridadDao() }
            val fabricanteDao = remember { com.example.etic.data.local.DbProvider.get(ctx).fabricanteDao() }
            LaunchedEffect(Unit) {
                prioridadOptions = runCatching { prioridadDao.getAllActivas() }.getOrElse { emptyList() }
                fabricanteOptions = runCatching { fabricanteDao.getAllActivos() }.getOrElse { emptyList() }
            }
            var searchMessage by remember { mutableStateOf<String?>(null) }
            var showNoSelectionDialog by rememberSaveable { mutableStateOf(false) }
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
    var deleteUbConfirmNode by remember { mutableStateOf<TreeNode?>(null) }
    val scope = rememberCoroutineScope()

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
            // Lee el usuario actual del CompositionLocal en contexto @Composable
            val currentUser = LocalCurrentUser.current

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

            fun triggerSearch() {
                searchMessage = null
                val code = barcode.trim()
                if (code.isEmpty()) return
                val path = findPathByBarcode(nodes, code)
                if (path == null) {
                    searchMessage = "No hay elementos con ese Codigo de barras"
                } else {
                    // expandir ancestros y seleccionar objetivo
                    path.dropLast(1).forEach { id -> if (!expanded.contains(id)) expanded.add(id) }
                    val targetId = path.last()
                    selectedId = targetId
                    highlightedId = targetId
                    scope.launch {
                        delay(3000)
                        if (highlightedId == targetId) highlightedId = null
                    }
                }
            }

            InspectionHeader(
                barcode = barcode,
                onBarcodeChange = { barcode = it },
                onSearch = { triggerSearch() },
                statusMenuExpanded = statusMenuExpanded,
                onStatusMenuToggle = { statusMenuExpanded = !statusMenuExpanded },
                onStatusMenuDismiss = { statusMenuExpanded = false },
                selectedStatusLabel = selectedStatusLabel,
                statusOptions = statusOptions,
                onStatusSelected = { opt ->
                    if (opt == null) {
                        selectedStatusLabel = "Todos"
                        selectedStatusId = null
                    } else {
                        selectedStatusLabel = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                        selectedStatusId = opt.idStatusInspeccionDet
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
                Text(
                    searchMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 12.dp)
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

                                inspectionRepository.markUbicacionInactive(ubId, userId, nowTs)

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
                                editingUbId = null
                                editingParentId = null
                                editingDetId = null
                                editingInspId = null
                                locationForm.error = null
                                selectedId?.let { pid -> if (!expanded.contains(pid)) expanded.add(pid) }
                                delay(3000)
                                showNewUbDialog = false
                                locationForm.resetForNew()
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
                            .fillMaxWidth(0.98f)   // 98% del ancho de la pantalla
                            .fillMaxHeight(0.98f)  // 98% del alto de la pantalla
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        // ---------------------------------------------------------------
                        // CARD PRINCIPAL DEL DIÁLOGO
                        // Se expande a todo el ancho permitido por el Box
                        // ---------------------------------------------------------------
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxSize(),    // ← importante: usa TODO el Box (que ya es 98% x 98%)
                            shape = RoundedCornerShape(12.dp)
                        ) {

                            // ---------------------------------------------------------------
                            // CONTENEDOR PRINCIPAL
                            // ---------------------------------------------------------------
                            Column(
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {

                                // ---------------------------------------------------------------
                                // TÍTULO DEL DIÁLOGO
                                // ---------------------------------------------------------------
                                Text("Editar ubicación", style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(8.dp))


                                // ===============================================================
                                // FILA PRINCIPAL EN DOS COLUMNAS
                                //   IZQUIERDA: formulario de ubicación
                                //   DERECHA: tabs Baseline / Histórico
                                // ===============================================================
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f, fill = true),
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

                                        // ------------------------------------------------------------------

                                        ExposedDropdownMenuBox(
                                            expanded = locationForm.statusExpanded,
                                            onExpandedChange = { locationForm.statusExpanded = !locationForm.statusExpanded }
                                        ) {
                                            TextField(
                                                value = if (locationForm.statusLabel.isNotBlank()) locationForm.statusLabel else "Seleccionar estatus",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(stringResource(com.example.etic.R.string.label_estatus_inspeccion)) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationForm.statusExpanded) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = locationForm.statusExpanded,
                                                onDismissRequest = { locationForm.statusExpanded = false }
                                            ) {
                                                statusOptions.forEach { opt ->
                                                    val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            locationForm.statusLabel = label
                                                            locationForm.statusId = opt.idStatusInspeccionDet
                                                            locationForm.statusExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = locationForm.prioridadExpanded,
                                            onExpandedChange = { locationForm.prioridadExpanded = !locationForm.prioridadExpanded }
                                        ) {
                                            TextField(
                                                value = if (locationForm.prioridadLabel.isNotBlank()) locationForm.prioridadLabel else "Seleccionar prioridad",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Tipo de prioridad") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationForm.prioridadExpanded) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = locationForm.prioridadExpanded,
                                                onDismissRequest = { locationForm.prioridadExpanded = false }
                                            ) {
                                                prioridadOptions.forEach { opt ->
                                                    val label = opt.tipoPrioridad ?: opt.idTipoPrioridad
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            locationForm.prioridadLabel = label
                                                            locationForm.prioridadId = opt.idTipoPrioridad
                                                            locationForm.prioridadExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        ExposedDropdownMenuBox(
                                            expanded = locationForm.fabricanteExpanded,
                                            onExpandedChange = { locationForm.fabricanteExpanded = !locationForm.fabricanteExpanded }
                                        ) {
                                            TextField(
                                                value = if (locationForm.fabricanteLabel.isNotBlank()) locationForm.fabricanteLabel else "Seleccionar fabricante",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Fabricante") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationForm.fabricanteExpanded) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = locationForm.fabricanteExpanded,
                                                onDismissRequest = { locationForm.fabricanteExpanded = false }
                                            ) {
                                                fabricanteOptions.forEach { opt ->
                                                    val label = opt.fabricante ?: opt.idFabricante
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            locationForm.fabricanteLabel = label
                                                            locationForm.fabricanteId = opt.idFabricante
                                                            locationForm.fabricanteExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Es equipo")
                                            Spacer(Modifier.width(12.dp))
                                            Switch(
                                                checked = locationForm.isEquipment,
                                                onCheckedChange = { locationForm.isEquipment = it }
                                            )
                                        }

                                        TextField(
                                            value = locationForm.name,
                                            onValueChange = { locationForm.name = it },
                                            singleLine = true,
                                            label = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(stringResource(com.example.etic.R.string.label_nombre_ubicacion))
                                                    Text(" *", color = MaterialTheme.colorScheme.error)
                                                }
                                            },
                                            isError = locationForm.error != null,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        TextField(
                                            value = locationForm.description,
                                            onValueChange = { locationForm.description = it },
                                            singleLine = false,
                                            label = { Text(stringResource(com.example.etic.R.string.label_descripcion)) },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        TextField(
                                            value = locationForm.barcode,
                                            onValueChange = { locationForm.barcode = it },
                                            singleLine = true,
                                            label = { Text(stringResource(com.example.etic.R.string.label_codigo_barras)) },
                                            modifier = Modifier.fillMaxWidth()
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
                                        TabRow(selectedTabIndex = editTab) {
                                            Tab(
                                                selected = editTab == 0,
                                                onClick = { editTab = 0 },
                                                text = { Text("Baseline") }
                                            )
                                            Tab(
                                                selected = editTab == 1,
                                                onClick = { editTab = 1 },
                                                text = { Text("Histórico") }
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

                                                Column(Modifier.fillMaxSize()) {
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
                                                    Spacer(Modifier.height(8.dp))

                                                    @Composable
                                                    fun RowScope.cell(flex: Int, content: @Composable () -> Unit) =
                                                        Box(
                                                            Modifier.weight(flex.toFloat()),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) { content() }

                                                    Row(
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .background(MaterialTheme.colorScheme.surface)
                                                            .padding(vertical = 8.dp, horizontal = 8.dp)
                                                    ) {
                                                        cell(2) { Text("No. Insp") }
                                                        cell(2) { Text("Fecha") }
                                                        cell(1) { Text("MTA °C") }
                                                        cell(1) { Text("Temp °C") }
                                                        cell(1) { Text("Amb °C") }
                                                        cell(1) { Text("IR") }
                                                        cell(1) { Text("ID") }
                                                        cell(3) { Text("Notas") }
                                                        Box(
                                                            modifier = Modifier.align(Alignment.CenterVertically),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text("")
                                                        }
                                                    }
                                                    Divider(thickness = DIVIDER_THICKNESS)

                                                    val baselineTabListState = rememberSaveable(
                                                        "baseline_tab2_state",
                                                        saver = LazyListState.Saver
                                                    ) { LazyListState() }

                                                    if (tableData.isEmpty()) {
                                                        Box(
                                                            Modifier
                                                                .fillMaxWidth()
                                                                .padding(8.dp),
                                                            contentAlignment = Alignment.CenterStart
                                                        ) {
                                                            Text(stringResource(com.example.etic.R.string.msg_sin_baseline))
                                                        }
                                                    } else {
                                                        LazyColumn(
                                                            Modifier.fillMaxSize(),
                                                            state = baselineTabListState
                                                        ) {
                                                            items(tableData, key = { it.id }) { b ->
                                                                Row(
                                                                    Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(vertical = 6.dp, horizontal = 8.dp)
                                                                        .pointerInput(b.id) {
                                                                            detectTapGestures(onDoubleTap = {
                                                                                baselineToEdit = b
                                                                                showNewBaseline = true
                                                                            })
                                                                        }
                                                                ) {
                                                                    cell(2) { Text(b.numInspeccion) }
                                                                    cell(2) { Text(b.fecha.toString()) }
                                                                    cell(1) { Text(b.mtaC.toString()) }
                                                                    cell(1) { Text(b.tempC.toString()) }
                                                                    cell(1) { Text(b.ambC.toString()) }
                                                                    cell(1) { Text(b.imgR ?: "") }
                                                                    cell(1) { Text(b.imgD ?: "") }
                                                                    cell(3) { Text(b.notas) }
                                                                    Box(
                                                                        modifier = Modifier.align(Alignment.CenterVertically),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        CompositionLocalProvider(
                                                                            androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement provides false
                                                                        ) {
                                                                            IconButton(
                                                                                onClick = { confirmDeleteId = b.id },
                                                                                modifier = Modifier.size(28.dp)  // tamaño compacto
                                                                            ) {
                                                                                Icon(
                                                                                    Icons.Outlined.Delete,
                                                                                    contentDescription = "Eliminar",
                                                                                    modifier = Modifier.size(18.dp),
                                                                                    tint = MaterialTheme.colorScheme.error
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                                Divider(thickness = DIVIDER_THICKNESS)
                                                            }
                                                        }
                                                    }

                                                    if (confirmDeleteId != null) {
                                                        AlertDialog(
                                                            onDismissRequest = { confirmDeleteId = null },
                                                            confirmButton = {
                                                                Button(onClick = {
                                                                    val id = confirmDeleteId ?: return@Button
                                                                    scope.launch {
                                                                        runCatching { lineaBaseDao.deleteById(id) }

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

                                                    LaunchedEffect(baselineToEdit, showNewBaseline) {
                                                        if (showNewBaseline && baselineToEdit != null) {
                                                            mta = baselineToEdit!!.mtaC.toString()
                                                            tempMax = baselineToEdit!!.tempC.toString()
                                                            tempAmb = baselineToEdit!!.ambC.toString()
                                                            notas = baselineToEdit!!.notas
                                                            imgIr = baselineToEdit!!.imgR ?: ""
                                                            imgId = baselineToEdit!!.imgD ?: ""
                                                        }
                                                        if (showNewBaseline && baselineToEdit == null) {
                                                            mta = ""; tempMax = ""; tempAmb = ""; notas = ""; imgIr = ""; imgId = ""
                                                        }
                                                    }

                                                    var irPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                                    var idPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

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
                                                        return try {
                                                            val dir = java.io.File(ctx.filesDir, "Imagenes").apply { mkdirs() }
                                                            val name = "$prefix-" + System.currentTimeMillis().toString() + ".jpg"
                                                            val file = java.io.File(dir, name)
                                                            java.io.FileOutputStream(file).use { out ->
                                                                bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, out)
                                                            }
                                                            name
                                                        } catch (_: Exception) { null }
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

                                                    val isBaselineValid by remember(mta, tempMax, tempAmb, imgIr, imgId) {
                                                        mutableStateOf(
                                                            mta.isNotBlank() &&
                                                                    tempMax.isNotBlank() &&
                                                                    tempAmb.isNotBlank() &&
                                                                    imgIr.isNotBlank() &&
                                                                    imgId.isNotBlank()
                                                        )
                                                    }

                                                    AlertDialog(
                                                        onDismissRequest = { },
                                                        properties = DialogProperties(
                                                            dismissOnClickOutside = false,
                                                            dismissOnBackPress = false
                                                        ),
                                                        confirmButton = {
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
                                                        },
                                                        dismissButton = {
                                                            Button(
                                                                onClick = {
                                                                    if (!isSavingBaseline) showNewBaseline = false
                                                                },
                                                                enabled = !isSavingBaseline
                                                            ) { Text("Cancelar") }
                                                        },
                                                        text = {
                                                            Column(Modifier.fillMaxWidth()) {
                                                                Text(
                                                                    if (baselineToEdit == null) "Nuevo Baseline" else "Editar Baseline",
                                                                    style = MaterialTheme.typography.titleMedium
                                                                )
                                                                Spacer(Modifier.height(8.dp))

                                                                Row(
                                                                    Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    TextField(
                                                                        value = mta,
                                                                        onValueChange = { mta = filter2Dec(it) },
                                                                        singleLine = true,
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    TextField(
                                                                        value = tempMax,
                                                                        onValueChange = { tempMax = filter2Dec(it) },
                                                                        singleLine = true,
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                    TextField(
                                                                        value = tempAmb,
                                                                        onValueChange = { tempAmb = filter2Dec(it) },
                                                                        singleLine = true,
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                                                        modifier = Modifier.weight(1f)
                                                                    )
                                                                }

                                                                Spacer(Modifier.height(8.dp))

                                                                TextField(
                                                                    value = notas,
                                                                    onValueChange = { notas = it },
                                                                    label = { Text("Notas") },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )

                                                                Spacer(Modifier.height(8.dp))

                                                                Row(
                                                                    Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(Modifier.weight(1f)) {
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            TextField(
                                                                                value = imgIr,
                                                                                onValueChange = { imgIr = it },
                                                                                label = {
                                                                                    Row(
                                                                                        verticalAlignment = Alignment.CenterVertically
                                                                                    ) {
                                                                                        Text("IR (archivo)")
                                                                                        Text(" *", color = MaterialTheme.colorScheme.error)
                                                                                    }
                                                                                },
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                            Spacer(Modifier.width(8.dp))
                                                                            IconButton(onClick = { irCameraLauncher.launch(null) }) {
                                                                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                                                                            }
                                                                        }
                                                                        Spacer(Modifier.height(4.dp))
                                                                        val bmp = irPreview ?: run {
                                                                            if (imgIr.isNotBlank()) {
                                                                                val f = java.io.File(
                                                                                    androidx.compose.ui.platform.LocalContext.current.filesDir,
                                                                                    "Imagenes/$imgIr"
                                                                                )
                                                                                if (f.exists()) android.graphics.BitmapFactory.decodeFile(f.absolutePath) else null
                                                                            } else null
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
                                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                                            TextField(
                                                                                value = imgId,
                                                                                onValueChange = { imgId = it },
                                                                                label = {
                                                                                    Row(
                                                                                        verticalAlignment = Alignment.CenterVertically
                                                                                    ) {
                                                                                        Text("ID (archivo)")
                                                                                        Text(" *", color = MaterialTheme.colorScheme.error)
                                                                                    }
                                                                                },
                                                                                modifier = Modifier.weight(1f)
                                                                            )
                                                                            Spacer(Modifier.width(8.dp))
                                                                            IconButton(onClick = { idCameraLauncher.launch(null) }) {
                                                                                Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                                                                            }
                                                                        }
                                                                        Spacer(Modifier.height(4.dp))
                                                                        val bmp2 = idPreview ?: run {
                                                                            if (imgId.isNotBlank()) {
                                                                                val f = java.io.File(
                                                                                    androidx.compose.ui.platform.LocalContext.current.filesDir,
                                                                                    "Imagenes/$imgId"
                                                                                )
                                                                                if (f.exists()) android.graphics.BitmapFactory.decodeFile(f.absolutePath) else null
                                                                            } else null
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
                                                                TextField(
                                                                    value = rutaEquipo,
                                                                    onValueChange = {},
                                                                    readOnly = true,
                                                                    label = { Text("Ruta del equipo") },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )
                                                            }
                                                        }
                                                    )
                                                }
                                            }


                                            // -----------------------------------------------------------
                                            // TAB 1 → HISTÓRICO
                                            // -----------------------------------------------------------
                                            1 -> {
                                                val scroll = rememberScrollState()
                                                Column(
                                                    Modifier
                                                        .fillMaxSize()
                                                        .verticalScroll(scroll)
                                                ) {
                                                    Text("Contenido de Tab 3 (Historico)")
                                                }
                                            }
                                        }
                                    }
                                }


                                // ===============================================================
                                // BOTÓN CANCELAR EN EL PIE DEL DIÁLOGO
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
                                        Text("Cancelar")
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
                                        problemaDao.getAllActivos()
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
                            // Abrir dialogo de edicion con tabs y precargar datos desde BD
                            locationForm.error = null
                            editingUbId = node.id
                            showEditUbDialog = true
                            // Tab inicial
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
                    baselineRefreshTick = baselineRefreshTick,
                    onBaselineChanged = { baselineRefreshTick++ },
                    modifier = Modifier.fillMaxSize()  // asegura ocupar todo el espacio
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
    onToggle: (String) -> Unit,
    onSelect: (String) -> Unit,
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
    val treeListState = rememberSaveable("tree_list_state", saver = LazyListState.Saver) { LazyListState() }
    val rootNodeId = nodes.firstOrNull()?.id
    val hasValidSelection = selectedId != null && flat.any { it.node.id == selectedId }
    val effectiveSelectedId = if (hasValidSelection) selectedId else rootNodeId
    LaunchedEffect(hasValidSelection, rootNodeId) {
        if (!hasValidSelection && rootNodeId != null) {
            onSelect(rootNodeId)
        }
    }
    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth(), state = treeListState) {
            items(flat, key = { it.node.id }) { item ->
                val n = item.node
                val isSelected = effectiveSelectedId == n.id
                val rowBackground = if (isSelected) selColor else Color.Transparent
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(rowBackground)
                        .clickable { onSelect(n.id) }
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

// -------------------------
// Tablas
// -------------------------

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsTable(
    children: List<TreeNode>,
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
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            HeaderCell("ubicacion", 3)
            HeaderCell("Codigo de barras", 2)
            HeaderCell("Estatus", 2)
            HeaderCell("Op", 1)
        }
        Divider(thickness = DIVIDER_THICKNESS)

        val listState = rememberSaveable("details_list_state", saver = LazyListState.Saver) { LazyListState() }
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
                items(children, key = { it.id }) { n ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 8.dp)
                            .pointerInput(n.id) {
                                detectTapGestures(onDoubleTap = { onEdit(n) })
                            }
                    ) {
                        BodyCell(3) { Text(n.title) }
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
    Box(Modifier.weight(flex.toFloat())) {
        content()
    }
}

@Composable
private fun ListTabs(
    node: TreeNode?,
    onDeleteProblem: (Problem) -> Unit,
    onDeleteBaseline: (Baseline) -> Unit,
    baselineRefreshTick: Int,
    onBaselineChanged: () -> Unit,
    modifier: Modifier = Modifier
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
        Box(Modifier.fillMaxSize()) {
            ProblemsTableFromDatabase(
                selectedId = node?.id,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (showProblems) 1f else 0f)
                    .zIndex(if (showProblems) 1f else 0f)
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
private fun ProblemsTable(problems: List<Problem>, onDelete: (Problem) -> Unit) {
    @Composable
    fun RowScope.cell(flex: Int, content: @Composable () -> Unit) =
        Box(Modifier.weight(flex.toFloat()), contentAlignment = Alignment.CenterStart) { content() }

    val listState = rememberSaveable("problems_list_state", saver = LazyListState.Saver) { LazyListState() }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            cell(1) { Text(stringResource(com.example.etic.R.string.col_numero)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_fecha)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_num_inspeccion)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_tipo)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_estatus)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_cronico)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_temp_c)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_delta_t_c)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_severidad)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_equipo)) }
            cell(3) { Text(stringResource(com.example.etic.R.string.col_comentarios)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_op)) }
        }
        Divider(thickness = DIVIDER_THICKNESS)
        if (problems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(com.example.etic.R.string.msg_sin_problemas)) }
        } else {
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                items(problems, key = { it.id }) { p ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    ) {
                        cell(1) { Text("${p.no}") }
                        cell(2) { Text(p.fecha.toString()) }
                        cell(2) { Text(p.numInspeccion) }
                        cell(2) { Text(p.tipo) }
                        cell(2) { Text(p.estatus) }
                        cell(1) { Text(if (p.cronico) "SI" else "NO") }
                        cell(1) { Text(p.tempC.toString()) }
                        cell(1) { Text(p.deltaTC.toString()) }
                        cell(2) { Text(p.severidad) }
                        cell(2) { Text(p.equipo) }
            cell(3) { Text(p.comentarios) }
                        cell(1) {
                            IconButton(onClick = { onDelete(p) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
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
private fun BaselineTable(baselines: List<Baseline>, onDelete: (Baseline) -> Unit) {
    @Composable
    fun RowScope.cell(flex: Int, content: @Composable () -> Unit) =
        Box(Modifier.weight(flex.toFloat())) { content() }

    val listState = rememberSaveable("baseline_list_state", saver = LazyListState.Saver) { LazyListState() }
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            cell(2) { Text(stringResource(com.example.etic.R.string.col_no_inspeccion)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_equipo)) }
            cell(2) { Text(stringResource(com.example.etic.R.string.col_fecha)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_mta_c)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_temp_c)) }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_amb_c)) }
            cell(1) { Text("IR") }
            cell(1) { Text("ID") }
            cell(3) { Text("Notas") }
            cell(1) { Text(stringResource(com.example.etic.R.string.col_op)) }
        }
        Divider(thickness = DIVIDER_THICKNESS)
        if (baselines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(com.example.etic.R.string.msg_sin_baseline)) }
        } else {
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                items(baselines, key = { it.id }) { b ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    ) {
                        cell(2) { Text(b.numInspeccion) }
                        cell(2) { Text(b.equipo) }
                        cell(2) { Text(b.fecha.toString()) }
                        cell(1) { Text(b.mtaC.toString()) }
                        cell(1) { Text(b.tempC.toString()) }
                        cell(1) { Text(b.ambC.toString()) }
                        cell(1) { Text(b.imgR ?: "") }
                        cell(1) { Text(b.imgD ?: "") }
                        cell(3) { Text(b.notas) }
                        cell(1) {
                            IconButton(onClick = { onDelete(b) }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                            }
                        }
                    }
                    Divider(thickness = DIVIDER_THICKNESS)
                }
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

@Composable
private fun ProblemsTableFromDatabase(selectedId: String?, modifier: Modifier = Modifier) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { com.example.etic.data.local.DbProvider.get(ctx).problemaDao() }
    val ubicacionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val inspDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDao() }
    val sevDao = remember { com.example.etic.data.local.DbProvider.get(ctx).severidadDao() }
    val eqDao = remember { com.example.etic.data.local.DbProvider.get(ctx).equipoDao() }
    val tipoInspDao = remember { com.example.etic.data.local.DbProvider.get(ctx).tipoInspeccionDao() }

    var problemsCache by remember { mutableStateOf(emptyList<Problem>()) }
    val uiProblems by produceState(initialValue = problemsCache, selectedId) {
        val rows = try { dao.getAllActivos() } catch (_: Exception) { emptyList() }
        val ubicaciones = try { ubicacionDao.getAll() } catch (_: Exception) { emptyList() }
        val filteredRows = when {
            selectedId == null -> rows
            selectedId!!.startsWith("root:") -> rows
            else -> {
                val allowed = descendantIds(ubicaciones, selectedId!!)
                rows.filter { r -> r.idUbicacion != null && allowed.contains(r.idUbicacion!!) }
            }
        }
        val inspMap = try { inspDao.getAll().associateBy { it.idInspeccion } } catch (_: Exception) { emptyMap() }
        val sevMap = try { sevDao.getAll().associateBy { it.idSeveridad } } catch (_: Exception) { emptyMap() }
        val eqMap = try { eqDao.getAll().associateBy { it.idEquipo } } catch (_: Exception) { emptyMap() }
        val ubicMap = ubicaciones.associateBy { it.idUbicacion }
        val tipoMap = try { tipoInspDao.getAll().associateBy { it.idTipoInspeccion } } catch (_: Exception) { emptyMap() }
        value = filteredRows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                    ?: r.irFileDate?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else java.time.LocalDate.now()
            }.getOrDefault(java.time.LocalDate.now())

            val numInspDisplay = r.idInspeccion?.let { inspMap[it]?.noInspeccion?.toString() } ?: ""
            val severidadDisplay = r.idSeveridad?.let { sevMap[it]?.severidad } ?: (r.idSeveridad ?: "")
            // Mostrar el nombre del equipo desde la Ubicacion asociada al problema
            val equipoDisplay = r.idUbicacion?.let { ubicMap[it]?.ubicacion } ?: ""
            val tipoDisplay = r.idTipoInspeccion?.let { tipoMap[it]?.tipoInspeccion } ?: (r.idTipoInspeccion ?: "")

            Problem(
                id = r.idProblema,
                no = r.numeroProblema ?: 0,
                fecha = fecha,
                numInspeccion = numInspDisplay,
                tipo = tipoDisplay,
                estatus = r.estatusProblema ?: "",
                cronico = (r.esCronico ?: "").equals("SI", ignoreCase = true),
                tempC = r.problemTemperature ?: 0.0,
                deltaTC = r.aumentoTemperatura ?: 0.0,
                severidad = severidadDisplay,
                equipo = equipoDisplay,
                comentarios = r.componentComment ?: ""
            )
        }
        problemsCache = value
    }

    Box(modifier) {
        ProblemsTable(problems = uiProblems, onDelete = { /* no-op: from DB */ })
    }
}

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

// -------------------------
// DB-backed Baseline table
// -------------------------

@Composable
private fun BaselineTableFromDatabase(
    selectedId: String?,
    refreshTick: Int,
    onBaselineChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val currentInspection = LocalCurrentInspection.current
    val dao = remember { com.example.etic.data.local.DbProvider.get(ctx).lineaBaseDao() }
    val ubicacionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val inspDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDao() }

    val inspeccionDetDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDetDao() }
    val currentUser = LocalCurrentUser.current
    val scope = rememberCoroutineScope()

    var baselinesCache by remember { mutableStateOf(emptyList<Baseline>()) }
    val uiBaselines by produceState(initialValue = baselinesCache, selectedId, currentInspection?.idInspeccion, refreshTick) {
        val rows = try {
            val inspId = currentInspection?.idInspeccion
            if (!inspId.isNullOrBlank()) dao.getByInspeccionActivos(inspId) else dao.getAllActivos()
        } catch (_: Exception) { emptyList() }
        val ubicaciones = try { ubicacionDao.getAllActivas() } catch (_: Exception) { emptyList() }
        val filteredRows = when {
            selectedId == null -> rows
            selectedId!!.startsWith("root:") -> rows
            else -> {
                val allowed = descendantIds(ubicaciones, selectedId!!)
                rows.filter { r -> r.idUbicacion != null && allowed.contains(r.idUbicacion!!) }
            }
        }
        val inspMap = try { inspDao.getAll().associateBy { it.idInspeccion } } catch (_: Exception) { emptyMap() }
        val ubicMap = ubicaciones.associateBy { it.idUbicacion }

        value = filteredRows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else java.time.LocalDate.now()
            }.getOrDefault(java.time.LocalDate.now())

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
        baselinesCache = value
    }

    var baselineToDelete by remember { mutableStateOf<Baseline?>(null) }

    Box(modifier) {
        BaselineTable(
            baselines = uiBaselines,
            onDelete = { baseline -> baselineToDelete = baseline }
        )

        if (baselineToDelete != null) {
            val baseline = baselineToDelete!!
            AlertDialog(
                onDismissRequest = { baselineToDelete = null },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            // Leer la linea base antes de eliminarla para obtener relacion con Inspeccion_det
                            val row = runCatching { dao.getById(baseline.id) }.getOrNull()

                            if (row != null) {
                                val idUb = row.idUbicacion
                                val idInsp = row.idInspeccion
                                if (!idUb.isNullOrBlank() && !idInsp.isNullOrBlank()) {
                                    val nowTs = java.time.LocalDateTime.now()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                                    val detRow = try {
                                        inspeccionDetDao.getByUbicacion(idUb)
                                            .firstOrNull { it.idInspeccion == idInsp }
                                    } catch (_: Exception) { null }
                                    if (detRow != null) {
                                        val revertedDet = detRow.copy(
                                            idStatusInspeccionDet = "568798D1-76BB-11D3-82BF-00104BC75DC2",
                                            idEstatusColorText = 1,
                                            modificadoPor = currentUser?.idUsuario,
                                            fechaMod = nowTs
                                        )
                                        runCatching { inspeccionDetDao.update(revertedDet) }
                                    }
                                }
                            }

                            // Eliminar la linea base de la base de datos
                            runCatching { dao.deleteById(baseline.id) }

                            // Actualizar cach? local y notificar cambio
                            baselinesCache = baselinesCache.filter { it.id != baseline.id }
                            baselineToDelete = null
                            onBaselineChanged()
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    Button(onClick = { baselineToDelete = null }) { Text("Cancelar") }
                },
                text = { Text("Eliminar baseline seleccionado?") }
            )
        }
    }
}

