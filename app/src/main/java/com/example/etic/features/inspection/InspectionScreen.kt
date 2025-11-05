package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Traffic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.example.etic.ui.theme.EticTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.etic.data.local.entities.EstatusInspeccionDet
import kotlinx.coroutines.flow.first
import com.example.etic.core.session.SessionManager
import com.example.etic.core.session.sessionDataStore

// Centralizamos algunos "magic numbers" para facilitar ajuste futuro
private const val MIN_FRAC: Float = 0.2f     // Límite inferior de los splitters
private const val MAX_FRAC: Float = 0.8f     // Límite superior de los splitters
private const val H_INIT_FRAC: Float = 0.6f  // Fracción inicial del panel superior
private const val V_INIT_FRAC: Float = 0.5f  // Fracción inicial del panel izquierdo

private val HANDLE_THICKNESS: Dp = 2.dp      // Grosor de los handles de split
private val DIVIDER_THICKNESS: Dp = 0.5.dp   // Grosor estándar de divisores/bordes
private val PANEL_PADDING: Dp = 12.dp        // Padding interno de cada panel
private const val SURFACE_VARIANT_ALPHA: Float = 0.4f
private const val SELECT_ALPHA: Float = 0.10f
private val ICON_EQUIPO_COLOR: Color = Color(0xFFFFC107)     // Amarillo (Traffic)
private val ICON_NO_EQUIPO_COLOR: Color = Color(0xFF4CAF50)  // Verde (DragIndicator)

// Ajustes de compacidad para filas del árbol
private val TREE_TOGGLE_SIZE: Dp = 20.dp   // Tamaño del ícono de expandir/colapsar
private val TREE_ICON_SIZE: Dp = 18.dp     // Tamaño del ícono del nodo
private val TREE_SPACING: Dp = 4.dp        // Espaciado horizontal pequeño
private val TREE_INDENT: Dp = 12.dp        // Indentación por nivel
// Ancho mínimo para que la tabla de Progreso no se comprima; habilita scroll horizontal si el panel es más angosto
private val DETAILS_TABLE_MIN_WIDTH: Dp = 900.dp
private val HEADER_ACTION_SPACING: Dp = 8.dp

// Nota: la tabla de Progreso ocupa siempre todo el ancho del panel

@Composable
fun InspectionScreen() {
    CurrentInspectionSplitView()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CurrentInspectionSplitView() {
    // Usamos constantes para evitar números mágicos in-line
    var hFrac by rememberSaveable { mutableStateOf(H_INIT_FRAC) } // fracción alto del panel superior
    var vFrac by rememberSaveable { mutableStateOf(V_INIT_FRAC) } // fracción ancho del panel izquierdo

    var nodes by remember { mutableStateOf<List<TreeNode>>(emptyList()) }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val ubicacionDao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val usuarioDao = remember { com.example.etic.data.local.DbProvider.get(ctx).usuarioDao() }
    val inspeccionDetDao = remember { com.example.etic.data.local.DbProvider.get(ctx).inspeccionDetDao() }
    LaunchedEffect(Unit) {
        val rows = runCatching { ubicacionDao.getAll() }.getOrElse { emptyList() }
        nodes = buildTreeFromUbicaciones(rows)
    }
    val expanded = remember { mutableStateListOf<String>() }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var highlightedId by remember { mutableStateOf<String?>(null) }

    val borderColor = DividerDefaults.color

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // ✅ Convierte dimensiones del BoxWithConstraints a píxeles de forma segura
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
            var showNewUbDialog by remember { mutableStateOf(false) }
            var newUbName by rememberSaveable { mutableStateOf("") }
            var newUbEsEquipo by rememberSaveable { mutableStateOf(false) }
            var newUbError by remember { mutableStateOf<String?>(null) }
            var newUbDesc by rememberSaveable { mutableStateOf("") }
            var newUbStatusExpanded by remember { mutableStateOf(false) }
            var newUbStatusLabel by rememberSaveable { mutableStateOf("") }
            var newUbStatusId by rememberSaveable { mutableStateOf<String?>(null) }
            var newUbBarcode by rememberSaveable { mutableStateOf("") }
            var newUbPrioridadExpanded by remember { mutableStateOf(false) }
            var newUbPrioridadLabel by rememberSaveable { mutableStateOf("") }
            var newUbPrioridadId by rememberSaveable { mutableStateOf<String?>(null) }
            var newUbFabricanteExpanded by remember { mutableStateOf(false) }
            var newUbFabricanteLabel by rememberSaveable { mutableStateOf("") }
            var newUbFabricanteId by rememberSaveable { mutableStateOf<String?>(null) }
            var currentUserId by remember { mutableStateOf<String?>(null) }
            var currentSitioId by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            // Preseleccionar estatus por defecto al abrir el diálogo
            LaunchedEffect(showNewUbDialog) {
                if (showNewUbDialog) {
                    val defaultId = "568798D1-76BB-11D3-82BF-00104BC75DC2"
                    val match = statusOptions.firstOrNull { it.idStatusInspeccionDet.equals(defaultId, true) }
                    if (match != null) {
                        newUbStatusId = match.idStatusInspeccionDet
                        newUbStatusLabel = match.estatusInspeccionDet ?: match.idStatusInspeccionDet
                    } else {
                        newUbStatusId = null
                        newUbStatusLabel = ""
                    }
                    // Obtener usuario de sesión y su sitio
                    val session = SessionManager(ctx.sessionDataStore)
                    val username = runCatching { session.username.first() }.getOrNull()
                    if (!username.isNullOrBlank()) {
                        val usr = runCatching { usuarioDao.getByUsuario(username) }.getOrNull()
                        currentUserId = usr?.idUsuario
                        currentSitioId = usr?.idSitio
                    }
                }
            }

            fun triggerSearch() {
                searchMessage = null
                val code = barcode.trim()
                if (code.isEmpty()) return
                val path = findPathByBarcode(nodes, code)
                if (path == null) {
                    searchMessage = "No hay elementos con ese codigo de barras"
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

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                    label = { Text("Codigo de barras") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { triggerSearch() })
                )
                Spacer(Modifier.width(HEADER_ACTION_SPACING))                // Estatus como ExposedDropdownMenuBox
                ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = !statusMenuExpanded }) {
                    TextField(value = selectedStatusLabel, onValueChange = {}, readOnly = true, label = { Text("Estatus") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                        modifier = Modifier.menuAnchor())
                    DropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Todos") },
                            onClick = {
                                selectedStatusLabel = "Todos"
                                selectedStatusId = null
                                statusMenuExpanded = false
                            }
                        )
                        statusOptions.forEach { opt ->
                            val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedStatusLabel = label
                                    selectedStatusId = opt.idStatusInspeccionDet
                                    statusMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.width(HEADER_ACTION_SPACING))

                Button(
                    onClick = { showNewUbDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp) // Bordes ligeramente redondeados (casi cuadrado)
                ) {
                    Text("Nueva ubicación", color = Color.White)
                }

            }
            if (searchMessage != null) {
                Text(searchMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Divider(thickness = DIVIDER_THICKNESS)

            if (showNewUbDialog) {
                AlertDialog(
                    onDismissRequest = { showNewUbDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    confirmButton = {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(onClick = {
                                showNewUbDialog = false
                                newUbError = null
                            }) { Text("Cancelar") }
                            Button(onClick = {
                                val name = newUbName.trim()
                                if (name.isEmpty()) {
                                    newUbError = "El nombre es obligatorio"
                                    return@Button
                                }
                                val id = java.util.UUID.randomUUID().toString()
                                // Nivel del árbol: si hay padre, nivel del padre + 1, sino 0
                                val nivel = selectedId?.let { parentId -> depthOfId(nodes, parentId) + 1 } ?: 0
                                // Ruta: path de títulos del padre + nombre
                                val ruta = selectedId?.let { parentId ->
                                    val titles = titlePathForId(nodes, parentId)
                                    if (titles.isNotEmpty()) titles.joinToString(" / ") + " / " + name else name
                                } ?: name
                                val nueva = com.example.etic.data.local.entities.Ubicacion(
                                    idUbicacion = id,
                                    idUbicacionPadre = selectedId,
                                    idSitio = currentSitioId,
                                    nivelArbol = nivel,
                                    ubicacion = name,
                                    descripcion = newUbDesc.trim().ifBlank { null },
                                    esEquipo = if (newUbEsEquipo) "SI" else "NO",
                                    codigoBarras = newUbBarcode.trim().ifBlank { null },
                                    fabricante = newUbFabricanteId,
                                    ruta = ruta,
                                    estatus = "Activo",
                                    creadoPor = currentUserId,
                                    fechaCreacion = java.time.LocalDateTime.now().toString(),
                                    idTipoPrioridad = newUbPrioridadId,
                                    idInspeccion = null
                                )
                                scope.launch {
                                    val okUb = runCatching { ubicacionDao.insert(nueva) }.isSuccess
                                    if (okUb) {
                                        // Crear inspecciones_det ligada a la nueva ubicación
                                        val detId = java.util.UUID.randomUUID().toString()
                                        val inspId = java.util.UUID.randomUUID().toString()
                                        val det = com.example.etic.data.local.entities.InspeccionDet(
                                            idInspeccionDet = detId,
                                            idInspeccion = inspId,
                                            idUbicacion = id,
                                            idStatusInspeccionDet = newUbStatusId,
                                            notasInspeccion = null,
                                            estatus = "Activo",
                                            idEstatusColorText = 1,
                                            expanded = "0",
                                            selected = "0",
                                            creadoPor = currentUserId,
                                            fechaCreacion = java.time.LocalDateTime.now().toString(),
                                            idSitio = currentSitioId
                                        )
                                        val okDet = runCatching { inspeccionDetDao.insert(det) }.isSuccess
                                        val rows = runCatching { ubicacionDao.getAll() }.getOrElse { emptyList() }
                                        nodes = buildTreeFromUbicaciones(rows)
                                        newUbName = ""
                                        newUbDesc = ""
                                        newUbEsEquipo = false
                                        newUbError = null
                                        newUbStatusId = null
                                        newUbStatusLabel = ""
                                        newUbBarcode = ""
                                        newUbPrioridadId = null
                                        newUbPrioridadLabel = ""
                                        newUbFabricanteId = null
                                        newUbFabricanteLabel = ""
                                        showNewUbDialog = false
                                        selectedId?.let { pid -> if (!expanded.contains(pid)) expanded.add(pid) }
                                    } else {
                                        newUbError = "No se pudo guardar la ubicación"
                                    }
                                }
                            }) { Text("Guardar") }
                        }
                    },
                    dismissButton = { },
                    title = { Text("Nueva ubicación") },
                    text = {
                        Box(Modifier.fillMaxWidth().widthIn(min = 520.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Estatus (lista de opciones)
                            ExposedDropdownMenuBox(
                                expanded = newUbStatusExpanded,
                                onExpandedChange = { newUbStatusExpanded = !newUbStatusExpanded }
                            ) {
                                TextField(
                                    value = if (newUbStatusLabel.isNotBlank()) newUbStatusLabel else "Seleccionar estatus",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Estatus de inspección") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = newUbStatusExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = newUbStatusExpanded,
                                    onDismissRequest = { newUbStatusExpanded = false }
                                ) {
                                    statusOptions.forEach { opt ->
                                        val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                newUbStatusLabel = label
                                                newUbStatusId = opt.idStatusInspeccionDet
                                                newUbStatusExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Tipo de prioridad
                            ExposedDropdownMenuBox(
                                expanded = newUbPrioridadExpanded,
                                onExpandedChange = { newUbPrioridadExpanded = !newUbPrioridadExpanded }
                            ) {
                                TextField(
                                    value = if (newUbPrioridadLabel.isNotBlank()) newUbPrioridadLabel else "Seleccionar prioridad",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tipo de prioridad") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = newUbPrioridadExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = newUbPrioridadExpanded,
                                    onDismissRequest = { newUbPrioridadExpanded = false }
                                ) {
                                    prioridadOptions.forEach { opt ->
                                        val label = opt.tipoPrioridad ?: opt.idTipoPrioridad
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                newUbPrioridadLabel = label
                                                newUbPrioridadId = opt.idTipoPrioridad
                                                newUbPrioridadExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Fabricante
                            ExposedDropdownMenuBox(
                                expanded = newUbFabricanteExpanded,
                                onExpandedChange = { newUbFabricanteExpanded = !newUbFabricanteExpanded }
                            ) {
                                TextField(
                                    value = if (newUbFabricanteLabel.isNotBlank()) newUbFabricanteLabel else "Seleccionar fabricante",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Fabricante") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = newUbFabricanteExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = newUbFabricanteExpanded,
                                    onDismissRequest = { newUbFabricanteExpanded = false }
                                ) {
                                    fabricanteOptions.forEach { opt ->
                                        val label = opt.fabricante ?: opt.idFabricante
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                newUbFabricanteLabel = label
                                                newUbFabricanteId = opt.idFabricante
                                                newUbFabricanteExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            // Es equipo
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Es equipo")
                                Spacer(Modifier.width(12.dp))
                                Switch(checked = newUbEsEquipo, onCheckedChange = { newUbEsEquipo = it })
                            }
                            // Nombre
                            TextField(
                                value = newUbName,
                                onValueChange = { newUbName = it },
                                singleLine = true,
                                label = { Text("Nombre de la ubicación") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Descripción
                            TextField(
                                value = newUbDesc,
                                onValueChange = { newUbDesc = it },
                                singleLine = false,
                                label = { Text("Descripción") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Código de barras
                            TextField(
                                value = newUbBarcode,
                                onValueChange = { newUbBarcode = it },
                                singleLine = true,
                                label = { Text("Código de barras") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) { }
                            if (newUbError != null) {
                                Text(newUbError!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        }
                    }
                )
            }

            Row(Modifier.weight(hFrac)) {

                // Panel izquierdo
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
                            onToggle = { id -> if (!expanded.remove(id)) expanded.add(id) },
                            onSelect = { id -> selectedId = id },
                            modifier = Modifier.fillMaxSize() // ← ocupa todo el panel
                        )
                    } else {
                        UbicacionesFlatListFromDatabase(
                            modifier = Modifier.fillMaxSize() // ← ocupa todo el panel
                        )
                    }
                }

                // Handle vertical (más suave)
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
                            if (selectedId == node.id) selectedId = null
                            nodes = nodes.toMutableList().also { removeById(node.id, it) }
                        }
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
                    modifier = Modifier.fillMaxSize()  // ← asegura ocupar todo el espacio
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
// Modelo de datos / utilería
// -------------------------

private data class TreeNode(
    val id: String,
    var title: String,
    var barcode: String? = null,
    var verified: Boolean = false,
    val children: MutableList<TreeNode> = mutableListOf(),
    val problems: MutableList<Problem> = mutableListOf(),
    val baselines: MutableList<Baseline> = mutableListOf()
) { val isLeaf: Boolean get() = children.isEmpty() }

private data class Problem(
    val no: Int,
    val fecha: java.time.LocalDate,
    val numInspeccion: String,
    val tipo: String,
    val estatus: String,
    val cronico: Boolean,
    val tempC: Double,
    val deltaTC: Double,
    val severidad: String,
    val equipo: String,
    val comentarios: String
)

private data class Baseline(
    val numInspeccion: String,
    val equipo: String,
    val fecha: java.time.LocalDate,
    val mtaC: Double,
    val tempC: Double,
    val ambC: Double,
    val imgR: String? = null,
    val imgD: String? = null,
    val notas: String
)

private fun findById(id: String?, list: List<TreeNode>): TreeNode? {
    if (id == null) return null
    for (n in list) {
        if (n.id == id) return n
        val found = findById(id, n.children)
        if (found != null) return found
    }
    return null
}

private fun removeById(id: String, list: MutableList<TreeNode>): Boolean {
    val it = list.listIterator()
    while (it.hasNext()) {
        val idx = it.nextIndex()
        val n = it.next()
        if (n.id == id) {
            list.removeAt(idx)
            return true
        }
        if (removeById(id, n.children)) return true
    }
    return false
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
    val selColor = MaterialTheme.colorScheme.primary.copy(alpha = SELECT_ALPHA)

    val hScroll = rememberScrollState()
    Box(Modifier.fillMaxSize().horizontalScroll(hScroll)) {
        LazyColumn(Modifier.fillMaxHeight()) {
            items(flat, key = { it.node.id }) { item ->
                val n = item.node
                val isSelected = selectedId == n.id
                Column(Modifier.background(if (isSelected) selColor else Color.Transparent)) {
                Row(
                    Modifier
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
                    // Icono según esEquipo (mapeado en 'verified' desde BD)
                    val (nodeIcon, tintColor) = if (n.verified) {
                        Icons.Outlined.Traffic to ICON_EQUIPO_COLOR
                    } else {
                        Icons.Outlined.DragIndicator to ICON_NO_EQUIPO_COLOR
                    }
                    Icon(nodeIcon, contentDescription = null, tint = tintColor, modifier = Modifier.size(TREE_ICON_SIZE))
                    Spacer(Modifier.width(TREE_SPACING))
                    Text(
                        n.title,
                        color = if (n.id == highlightedId) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable { onSelect(n.id) }
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
private fun DetailsTable(children: List<TreeNode>,modifier: Modifier = Modifier, onDelete: (TreeNode) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val hScroll = rememberScrollState()
        val minWidth = DETAILS_TABLE_MIN_WIDTH
        val tableWidth = if (maxWidth < minWidth) minWidth else maxWidth

        Box(Modifier.fillMaxSize().horizontalScroll(hScroll)) {
            Column(Modifier.width(tableWidth).fillMaxHeight()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    HeaderCell("Ubicacion", 3)
                    HeaderCell("Codigo de barras", 2)
                    HeaderCell("Estatus", 2)
                    HeaderCell("Op", 1)
                }
                Divider(thickness = DIVIDER_THICKNESS)

                if (children.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin elementos") }
                } else {
                    LazyColumn(Modifier.fillMaxHeight()) {
                        items(children, key = { it.id }) { n ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp, horizontal = 8.dp)
                            ) {
                                BodyCell(3) { Text(n.title) }
                                BodyCell(2) { Text(n.barcode ?: "-") }
                                BodyCell(2) { Text(if (n.verified) "Verificado" else "Por verificar") }
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
    }
}

@Composable private fun RowScope.HeaderCell(text: String, flex: Int) {
    Box(Modifier.weight(flex.toFloat())) { Text(text, style = MaterialTheme.typography.labelLarge) }
}
@Composable private fun RowScope.BodyCell(flex: Int, content: @Composable () -> Unit) {
    Box(Modifier.weight(flex.toFloat())) { content() }
}

@Composable
private fun ListTabs(
    node: TreeNode?,
    onDeleteProblem: (Problem) -> Unit,
    onDeleteBaseline: (Baseline) -> Unit,
    modifier: Modifier = Modifier
) {
    // Nota: mostramos versiones ligadas a BD; no usamos listas calculadas por nodo aquí
    var tab by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Listado de problemas") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Listado Base Line") })
        }
        Divider(thickness = DIVIDER_THICKNESS)
        when (tab) {
            0 -> ProblemsTableFromDatabase()
            1 -> BaselineTableFromDatabase()
        }
    }
}

// Encuentra el camino (ids) desde la raíz hasta el nodo cuyo codigo de barras coincide exactamente
private fun findPathByBarcode(list: List<TreeNode>, barcode: String): List<String>? {
    fun dfs(n: TreeNode, path: List<String>): List<String>? {
        if ((n.barcode ?: "") == barcode) return path + n.id
        for (c in n.children) {
            val found = dfs(c, path + n.id)
            if (found != null) return found
        }
        return null
    }
    for (root in list) {
        val res = dfs(root, emptyList())
        if (res != null) return res
    }
    return null
}

private fun buildTreeFromUbicaciones(rows: List<com.example.etic.data.local.entities.Ubicacion>): MutableList<TreeNode> {
    val byId = mutableMapOf<String, TreeNode>()
    val roots = mutableListOf<TreeNode>()

    rows.forEach { r ->
        val node = TreeNode(
            id = r.idUbicacion,
            title = r.ubicacion ?: "(Sin nombre)",
            barcode = r.codigoBarras,
            verified = (r.esEquipo ?: "").equals("SI", ignoreCase = true)
        )
        byId[r.idUbicacion] = node
    }

    rows.forEach { r ->
        val node = byId[r.idUbicacion] ?: return@forEach
        val parentId = r.idUbicacionPadre?.takeIf { it.isNotBlank() }
        if (parentId != null) {
            val parent = byId[parentId]
            if (parent != null) parent.children.add(node) else roots.add(node)
        } else {
            roots.add(node)
        }
    }

    fun sortRec(n: TreeNode) {
        n.children.sortBy { it.title }
        n.children.forEach { sortRec(it) }
    }
    roots.sortBy { it.title }
    roots.forEach { sortRec(it) }

    return roots
}

// Profundidad del nodo por id (0 = raíz). Si no se encuentra, devuelve 0
private fun depthOfId(list: List<TreeNode>, targetId: String): Int {
    fun dfs(n: TreeNode, depth: Int): Int? {
        if (n.id == targetId) return depth
        for (c in n.children) {
            val d = dfs(c, depth + 1)
            if (d != null) return d
        }
        return null
    }
    for (root in list) {
        val d = dfs(root, 0)
        if (d != null) return d
    }
    return 0
}

// Ruta de títulos desde la raíz hasta el nodo indicado
private fun titlePathForId(list: List<TreeNode>, targetId: String): List<String> {
    fun dfs(n: TreeNode, path: List<String>): List<String>? {
        val newPath = path + n.title
        if (n.id == targetId) return newPath
        for (c in n.children) {
            val res = dfs(c, newPath)
            if (res != null) return res
        }
        return null
    }
    for (root in list) {
        val res = dfs(root, emptyList())
        if (res != null) return res
    }
    return emptyList()
}

private fun collectProblems(root: TreeNode?): List<Problem> {
    if (root == null) return emptyList()
    val out = mutableListOf<Problem>()
    fun dfs(n: TreeNode) { out += n.problems; n.children.forEach { dfs(it) } }
    dfs(root)
    return out
}

private fun collectBaselines(root: TreeNode?): List<Baseline> {
    if (root == null) return emptyList()
    val out = mutableListOf<Baseline>()
    fun dfs(n: TreeNode) { out += n.baselines; n.children.forEach { dfs(it) } }
    dfs(root)
    return out
}

@Composable
private fun ProblemsTable(problems: List<Problem>, onDelete: (Problem) -> Unit) {
    @Composable
    fun RowScope.cell(flex: Int, content: @Composable () -> Unit) =
        Box(Modifier.weight(flex.toFloat()), contentAlignment = Alignment.CenterStart) { content() }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            cell(1) { Text("No") }
            cell(2) { Text("Fecha") }
            cell(2) { Text("Num Inspección") }
            cell(2) { Text("Tipo") }
            cell(2) { Text("Estatus") }
            cell(1) { Text("Cronico") }
            cell(1) { Text("Temp °C") }
            cell(1) { Text("Delta T °C") }
            cell(2) { Text("Severidad") }
            cell(2) { Text("Equipo") }
            cell(3) { Text("Comentarios") }
            cell(1) { Text("Op") }
        }
        Divider(thickness = DIVIDER_THICKNESS)
        if (problems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin problemas") }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(problems) { p ->
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
                        cell(1) { Text(if (p.cronico) "●" else "○") }
                        cell(1) { Text(p.tempC.toString()) }
                        cell(1) { Text(p.deltaTC.toString()) }
                        cell(2) { Text(p.severidad) }
                        cell(2) { Text(p.equipo) }
                        cell(3) { Text(p.comentarios, maxLines = 1) }
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

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            cell(2) { Text("No Inpexxión") }
            cell(2) { Text("Equipo") }
            cell(2) { Text("Fecha") }
            cell(1) { Text("MTA °C") }
            cell(1) { Text("Temp °C") }
            cell(1) { Text("Amb °C") }
            cell(1) { Icon(Icons.Outlined.Image, contentDescription = null) }
            cell(1) { Icon(Icons.Outlined.Image, contentDescription = null) }
            cell(3) { Text("Notas") }
            cell(1) { Text("Op") }
        }
        Divider(thickness = DIVIDER_THICKNESS)
        if (baselines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin base line") }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(baselines) { b ->
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
                        cell(1) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                tint = if ((b.imgR ?: "").isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                        cell(1) {
                            Icon(
                                Icons.Outlined.Image,
                                contentDescription = null,
                                tint = if ((b.imgD ?: "").isEmpty())
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary
                            )
                        }
                        cell(3) { Text(b.notas, maxLines = 1) }
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
private fun ProblemsTableFromDatabase() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { com.example.etic.data.local.DbProvider.get(ctx).problemaDao() }

    val uiProblems by produceState(initialValue = emptyList<Problem>()) {
        val rows = try { dao.getAll() } catch (_: Exception) { emptyList() }
        value = rows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                    ?: r.irFileDate?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else java.time.LocalDate.now()
            }.getOrDefault(java.time.LocalDate.now())

            Problem(
                no = r.numeroProblema ?: 0,
                fecha = fecha,
                numInspeccion = r.idInspeccion ?: "",
                tipo = r.idTipoInspeccion ?: "",
                estatus = r.estatusProblema ?: "",
                cronico = (r.esCronico ?: "").equals("SI", ignoreCase = true),
                tempC = r.problemTemperature ?: 0.0,
                deltaTC = r.aumentoTemperatura ?: 0.0,
                severidad = r.idSeveridad ?: "",
                equipo = r.idEquipo ?: "",
                comentarios = r.componentComment ?: ""
            )
        }
    }

    ProblemsTable(problems = uiProblems, onDelete = { /* no-op: from DB */ })
}

// -------------------------
// DB-backed Baseline table
// -------------------------

@Composable
private fun BaselineTableFromDatabase() {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { com.example.etic.data.local.DbProvider.get(ctx).lineaBaseDao() }

    val uiBaselines by produceState(initialValue = emptyList<Baseline>()) {
        val rows = try { dao.getAll() } catch (_: Exception) { emptyList() }
        value = rows.map { r ->
            val fecha = runCatching {
                val raw = r.fechaCreacion?.takeIf { it.isNotBlank() }
                val onlyDate = raw?.take(10)
                if (onlyDate != null) java.time.LocalDate.parse(onlyDate) else java.time.LocalDate.now()
            }.getOrDefault(java.time.LocalDate.now())

            Baseline(
                numInspeccion = r.idInspeccion ?: "",
                equipo = r.idUbicacion ?: "", // no hay columna Equipo; usamos ubicacion como referencia
                fecha = fecha,
                mtaC = r.mta ?: 0.0,
                tempC = r.tempMax ?: 0.0,
                ambC = r.tempAmb ?: 0.0,
                imgR = r.archivoIr,
                imgD = r.archivoId,
                notas = r.notas ?: ""
            )
        }
    }

    BaselineTable(baselines = uiBaselines, onDelete = { /* no-op: from DB */ })
}

// -------------------------
// DB-backed Ubicaciones flat list (fallback)
// -------------------------

@Composable
private fun UbicacionesFlatListFromDatabase(modifier: Modifier = Modifier) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val dao = remember { com.example.etic.data.local.DbProvider.get(ctx).ubicacionDao() }
    val ubicaciones by produceState(initialValue = emptyList<com.example.etic.data.local.entities.Ubicacion>()) {
        value = try { dao.getAll() } catch (_: Exception) { emptyList() }
    }

    if (ubicaciones.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin ubicaciones en BD")
        }
    } else {
        LazyColumn(Modifier.fillMaxSize()) {
            items(ubicaciones, key = { it.idUbicacion }) { u ->
                Column(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 8.dp)) {
                    Text(u.ubicacion ?: u.descripcion ?: "(Sin nombre)", style = MaterialTheme.typography.bodyLarge)
                    val sub = listOfNotNull(
                        u.codigoBarras?.takeIf { it.isNotBlank() }?.let { "CB: $it" },
                        u.idUbicacionPadre?.takeIf { it.isNotBlank() }?.let { "Padre: $it" }
                    ).joinToString("  •  ")
                    if (sub.isNotEmpty()) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                    Divider(thickness = DIVIDER_THICKNESS)
            }
        }
    }
}


