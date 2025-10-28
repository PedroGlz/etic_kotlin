package com.example.etic.features.inspection.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.etic.ui.theme.EticTheme

@Composable
fun InspectionScreen() {
    CurrentInspectionSplitView()
}

@Composable
private fun CurrentInspectionSplitView() {
    var hFrac by rememberSaveable { mutableStateOf(0.6f) } // fracción alto del panel superior
    var vFrac by rememberSaveable { mutableStateOf(0.5f) } // fracción ancho del panel izquierdo
    val minFrac = 0.2f
    val maxFrac = 0.8f
    val handleThickness: Dp = 2.dp

    var nodes by remember { mutableStateOf(generateInitialNodes().toMutableList()) }
    val expanded = remember { mutableStateListOf<String>() }
    var selectedId by remember { mutableStateOf<String?>(null) }

    val borderColor = DividerDefaults.color

    BoxWithConstraints(Modifier.fillMaxSize()) {
        // ✅ Convierte dimensiones del BoxWithConstraints a píxeles de forma segura
        val density = LocalDensity.current
        val totalWidthPx = with(density) { maxWidth.value * density.density }
        val totalHeightPx = with(density) { maxHeight.value * density.density }

        Column(Modifier.fillMaxSize()) {

            Row(Modifier.weight(hFrac)) {

                // Panel izquierdo
                CellPanel(
                    title = "Resumen",
                    icon = Icons.Outlined.Info,
                    borderColor = borderColor,
                    modifier = Modifier
                        .weight(vFrac)
                        .fillMaxHeight()
                ) {
                    SimpleTreeView(
                        nodes = nodes,
                        expanded = expanded.toSet(),
                        selectedId = selectedId,
                        onToggle = { id -> if (!expanded.remove(id)) expanded.add(id) },
                        onSelect = { id -> selectedId = id }
                    )
                }

                // Handle vertical (más suave)
                Box(
                    Modifier
                        .width(handleThickness)
                        .fillMaxHeight()
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { deltaPx: Float ->
                                vFrac = (vFrac + deltaPx / totalWidthPx)
                                    .coerceIn(minFrac, maxFrac)
                            }
                        )
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                // Panel derecho
                val children = findById(selectedId, nodes)?.children ?: emptyList()
                CellPanel(
                    title = "Progreso",
                    icon = Icons.Outlined.Timeline,
                    borderColor = borderColor,
                    modifier = Modifier
                        .weight(1f - vFrac)
                        .fillMaxHeight()
                ) {
                    ProgressTable(
                        children = children,
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
                    .height(handleThickness)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { deltaPx: Float ->
                            hFrac = (hFrac + deltaPx / totalHeightPx)
                                .coerceIn(minFrac, maxFrac)
                        }
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Panel inferior
            CellPanel(
                title = "Detalles",
                icon = Icons.Outlined.List,
                borderColor = borderColor,
                modifier = Modifier.weight(1f - hFrac)
            ) {
                DetailsTabs(
                    node = findById(selectedId, nodes),
                    onDeleteProblem = { p ->
                        val cur = findById(selectedId, nodes)
                        cur?.problems?.remove(p)
                    },
                    onDeleteBaseline = { b ->
                        val cur = findById(selectedId, nodes)
                        cur?.baselines?.remove(b)
                    }
                )
            }
        }
    }
}


@Composable
private fun CellPanel(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    borderColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(width = 0.5.dp, color = borderColor)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // (Opcional) encabezado visual si lo deseas:
            // Row(verticalAlignment = Alignment.CenterVertically) {
            //     Icon(icon, contentDescription = null)
            //     Spacer(Modifier.width(8.dp))
            //     Text(title, style = MaterialTheme.typography.titleMedium)
            // }
            // Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                content()
            }
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

private fun generateInitialNodes(): List<TreeNode> {
    val now = java.time.LocalDate.now()
    val nodes = mutableListOf(
        TreeNode(
            id = "i001",
            title = "SUBESCTACION 001",
            children = mutableListOf(
                TreeNode(
                    id = "i001-siteA",
                    title = "Sitio: Planta A",
                    children = mutableListOf(
                        TreeNode(
                            id = "i001-a1",
                            title = "Area 1",
                            barcode = "A1-0001",
                            verified = true,
                            problems = mutableListOf(
                                Problem(1, now, "INS-001", "Térmica", "Abierto", false, 65.0, 12.3, "Media", "Motor A", "Vibración leve detectada"),
                                Problem(2, now, "INS-001", "Eléctrica", "Cerrado", true, 72.2, 15.0, "Alta", "Tablero 1", "Ajuste de terminales")
                            ),
                            baselines = mutableListOf(
                                Baseline("INS-BASE-01", "Motor A", now, 40.0, 42.5, 22.0, null, null, "Valores dentro de rango")
                            )
                        ),
                        TreeNode(
                            id = "i001-a2",
                            title = "Area 2",
                            barcode = "A2-0002",
                            verified = false,
                            problems = mutableListOf(
                                Problem(1, now, "INS-002", "Mecánica", "En progreso", false, 50.5, 5.5, "Baja", "Bomba B", "Requiere seguimiento")
                            ),
                            baselines = mutableListOf(
                                Baseline("INS-BASE-02", "Bomba B", now, 38.0, 39.1, 23.0, null, null, "OK")
                            )
                        )
                    )
                ),
                TreeNode(
                    id = "i001-findings",
                    title = "Hallazgos",
                    children = mutableListOf(
                        TreeNode(id = "i001-sec", title = "Seguridad", barcode = "SEC-001", verified = false),
                        TreeNode(id = "i001-mant", title = "Mantenimiento", barcode = "MAN-002", verified = true)
                    )
                )
            )
        ),
        TreeNode(
            id = "i002",
            title = "SUBESTACION 002",
            children = mutableListOf(
                TreeNode(id = "i002-siteB", title = "Sitio: Planta B", barcode = "PB-0001", verified = false),
                TreeNode(id = "i002-findings", title = "Hallazgos", barcode = "HAL-0001", verified = false)
            )
        )
    )
    nodes.addAll(generateMockNodes())
    return nodes
}

private fun generateMockNodes(): List<TreeNode> {
    val out = mutableListOf<TreeNode>()
    val now = java.time.LocalDate.now()
    val inspections = 4
    val sitesPerInspection = 10
    val areasPerSite = 10
    val equiposPerArea = 9
    for (i in 1..inspections) {
        val insId = i.toString().padStart(3, '0')
        val inspection = TreeNode(id = "ins$insId", title = "SUBESTACION $insId")
        for (s in 1..sitesPerInspection) {
            val site = TreeNode(
                id = "ins$insId-site$s",
                title = "Sitio: Planta ${('A' + (s - 1) % 26)}",
                barcode = "S$insId-${s.toString().padStart(3, '0')}",
                verified = s % 2 == 0
            )
            for (a in 1..areasPerSite) {
                val area = TreeNode(
                    id = "ins$insId-site$s-area$a",
                    title = "Area $a",
                    barcode = "A$insId-$s-$a",
                    verified = a % 3 == 0
                )
                for (e in 1..equiposPerArea) {
                    val equipo = TreeNode(
                        id = "ins$insId-site$s-area$a-e$e",
                        title = "Equipo $e",
                        barcode = "EQ$insId-$s-$a-$e",
                        verified = e % 4 == 0
                    )
                    if (e == 1) {
                        area.problems.add(
                            Problem(
                                no = a * 10 + e,
                                fecha = now,
                                numInspeccion = "INS-$insId",
                                tipo = "Termica",
                                estatus = if (a % 2 == 0) "Abierto" else "Cerrado",
                                cronico = a % 4 == 0,
                                tempC = 40.0 + (e % 10),
                                deltaTC = 5.0 + (e % 5),
                                severidad = if (a % 3 == 0) "Alta" else "Media",
                                equipo = "Equipo $e",
                                comentarios = "Mock generado"
                            )
                        )
                        area.baselines.add(
                            Baseline(
                                numInspeccion = "INS-BASE-$insId",
                                equipo = "Equipo $e",
                                fecha = now,
                                mtaC = 38.0,
                                tempC = 39.0 + (e % 3),
                                ambC = 23.0,
                                imgR = null,
                                imgD = null,
                                notas = "OK"
                            )
                        )
                    }
                    area.children.add(equipo)
                }
                site.children.add(area)
            }
            inspection.children.add(site)
        }
        out.add(inspection)
    }
    return out
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
    onToggle: (String) -> Unit,
    onSelect: (String) -> Unit
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
    val selColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)

    LazyColumn(Modifier.fillMaxSize()) {
        items(flat, key = { it.node.id }) { item ->
            val n = item.node
            val isSelected = selectedId == n.id
            Column(Modifier.background(if (isSelected) selColor else Color.Transparent)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = (item.depth * 16).dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.hasChildren) {
                        IconButton(onClick = { onToggle(n.id) }) {
                            Icon(
                                if (item.expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                                contentDescription = null
                            )
                        }
                    } else {
                        Spacer(Modifier.width(40.dp))
                    }
                    Icon(Icons.Outlined.Folder, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = { onSelect(n.id) }) { Text(n.title) }
                }
                Divider(thickness = 0.5.dp)
            }
        }
    }
}

// -------------------------
// Tablas
// -------------------------

@Composable
private fun ProgressTable(children: List<TreeNode>, onDelete: (TreeNode) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 8.dp, horizontal = 8.dp)
        ) {
            HeaderCell("Ubicacion", 3)
            HeaderCell("Codigo de barras", 2)
            HeaderCell("Estatus", 2)
            HeaderCell("Op", 1)
        }
        Divider(thickness = 0.5.dp)
        if (children.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Sin elementos") }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(children, key = { it.id }) { n ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp)
                    ) {
                        BodyCell(3) { Text(n.title) }
                        BodyCell(2) { Text(n.barcode ?: "-") }
                        BodyCell(2) { Text(if (n.verified) "Verificado" else "Por verificar") }
                        BodyCell(1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onDelete(n) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                    Divider(thickness = 0.5.dp)
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
private fun DetailsTabs(
    node: TreeNode?,
    onDeleteProblem: (Problem) -> Unit,
    onDeleteBaseline: (Baseline) -> Unit
) {
    val problems = remember(node) { collectProblems(node) }
    val baselines = remember(node) { collectBaselines(node) }
    var tab by rememberSaveable { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Listado de problemas") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Listado Base Line") })
        }
        Divider(thickness = 0.5.dp)
        when (tab) {
            0 -> ProblemsTableFromDatabase()
            1 -> BaselineTable(baselines = baselines, onDelete = onDeleteBaseline)
        }
    }
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
        Divider(thickness = 0.5.dp)
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
                    Divider(thickness = 0.5.dp)
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
        Divider(thickness = 0.5.dp)
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
                    Divider(thickness = 0.5.dp)
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
