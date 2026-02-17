package com.example.etic.features.inspection.ui.problem

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import com.example.etic.data.local.dao.VisualProblemHistoryRow
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.core.saf.EticImageStore
import com.example.etic.core.settings.EticPrefs
import com.example.etic.core.settings.settingsDataStore

private val DIALOG_MIN_WIDTH = 980.dp
private val DIALOG_MAX_WIDTH = 980.dp
private val INFO_FIELD_MIN_WIDTH = 130.dp
private val INFO_FIELD_MAX_WIDTH = 220.dp
private val DIALOG_HEADER_TURQUOISE = Color(0xFF159BA6)

private val FIELD_HEIGHT = 25.dp
private val FIELD_RADIUS = 4.dp
private val FIELD_BORDER = 1.dp
private val FIELD_PADDING = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
private val CONTENT_GAP = 10.dp
private val SECTION_GAP = 12.dp
private val ROW_GAP = 12.dp

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
fun VisualProblemDialog(
    inspectionNumber: String,
    problemNumber: String,
    problemType: String,
    equipmentName: String,
    equipmentRoute: String,
    hazardIssues: List<Pair<String, String>>,
    severities: List<Pair<String, String>>,
    selectedHazardIssue: String?,
    selectedSeverity: String?,
    observations: String,
    onObservationsChange: (String) -> Unit,
    historyRows: List<VisualProblemHistoryRow>,
    historyLoading: Boolean,
    thermalImageName: String,
    digitalImageName: String,
    onThermalImageChange: (String) -> Unit,
    onDigitalImageChange: (String) -> Unit,
    onThermalSequenceUp: () -> Unit,
    onThermalSequenceDown: () -> Unit,
    onDigitalSequenceUp: () -> Unit,
    onDigitalSequenceDown: () -> Unit,
    onThermalPickInitial: () -> Unit,
    onDigitalPickInitial: () -> Unit,
    onThermalFolder: () -> Unit,
    onDigitalFolder: () -> Unit,
    onThermalCamera: () -> Unit,
    onDigitalCamera: () -> Unit,
    onHazardSelected: (String) -> Unit,
    onSeveritySelected: (String) -> Unit,
    onAddHazardIssue: () -> Unit = {},
    onAddSeverity: () -> Unit = {},
    onCronicoClick: (() -> Unit)? = null,
    cronicoEnabled: Boolean = false,
    cronicoChecked: Boolean = false,
    cerradoChecked: Boolean = false,
    cerradoEnabled: Boolean = false,
    onCerradoChange: (Boolean) -> Unit = {},
    onNavigatePrevious: (() -> Unit)? = null,
    onNavigateNext: (() -> Unit)? = null,
    canNavigatePrevious: Boolean = true,
    canNavigateNext: Boolean = true,
    showEditControls: Boolean = false,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    selectedTabIndex: Int? = null,
    onSelectedTabChange: ((Int) -> Unit)? = null,
    transitionKey: Any = Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 6.dp
        ) {
            val scrollState = rememberScrollState()
            val infoRowScroll = rememberScrollState()
            var saveAttempted by rememberSaveable { mutableStateOf(false) }
            AnimatedContent(
                targetState = transitionKey,
                transitionSpec = { fadeIn(tween(140)) togetherWith fadeOut(tween(140)) },
                label = "visual-problem-transition"
            ) {
                Column(
                Modifier
                    .widthIn(min = DIALOG_MIN_WIDTH, max = DIALOG_MAX_WIDTH)
                    .verticalScroll(scrollState)
                    .padding(
                            start = 17.dp,
                            end = 17.dp,
                            top = 11.dp,
                            bottom = 4.dp
                        )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DIALOG_HEADER_TURQUOISE, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "Problema Visual",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
                if (showEditControls) {
                    Divider(Modifier.padding(top = 5.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onNavigatePrevious?.invoke() },
                                enabled = onNavigatePrevious != null && canNavigatePrevious
                            ) {
                                Icon(Icons.Outlined.ArrowLeft, contentDescription = "Anterior")
                            }
                            IconButton(
                                onClick = { onNavigateNext?.invoke() },
                                enabled = onNavigateNext != null && canNavigateNext
                            ) {
                                Icon(Icons.Outlined.ArrowRight, contentDescription = "Siguiente")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = cronicoChecked,
                                    onCheckedChange = {},
                                    enabled = false
                                )
                                Text("Cronico")
                            }
                            Spacer(Modifier.width(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = cerradoChecked,
                                    onCheckedChange = { onCerradoChange(it) },
                                    enabled = cerradoEnabled
                                )
                                Text("Cerrado")
                            }
                            if (cronicoEnabled) {
                                Spacer(Modifier.width(12.dp))
                                IconButton(onClick = { onCronicoClick?.invoke() }) {
                                    Icon(
                                        Icons.Outlined.AccessTime,
                                        contentDescription = "Historial",
                                        tint = Color(0xFFFFC107)
                                    )
                                }
                            }
                        }
                    }
                    Divider(Modifier.padding(top = 8.dp, bottom = 10.dp))
                }
                val hazardMissing = selectedHazardIssue.isNullOrBlank()
                val severityMissing = selectedSeverity.isNullOrBlank()
                val hazardError = saveAttempted && hazardMissing
                val severityError = saveAttempted && severityMissing
                val imagesProvided = thermalImageName.isNotBlank() && digitalImageName.isNotBlank()
                val thermalError = saveAttempted && thermalImageName.isBlank()
                val digitalError = saveAttempted && digitalImageName.isBlank()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1.65f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(infoRowScroll),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InfoField("Inspección No.", inspectionNumber, 85.dp)
                            InfoField("Problema No.", problemNumber, 85.dp)
                            InfoField("Tipo de problema", problemType, 95.dp)
                            InfoField(
                                label = "Equipo",
                                value = equipmentName,
                                ancho = null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                                                   Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                InfoField(
                                    label = "Ruta del equipo",
                                    value = equipmentRoute,
                                    ancho = null,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                        Divider(Modifier.padding(top = 6.dp, bottom = 6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                DropdownSelector(
                                    label = "Problema *",
                                    options = hazardIssues,
                                    selectedId = selectedHazardIssue,
                                    onSelected = onHazardSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = hazardError,
                                    onAddClick = onAddHazardIssue
                                )
                                DropdownSelector(
                                    label = "Severidad *",
                                    options = severities,
                                    selectedId = selectedSeverity,
                                    onSelected = onSeveritySelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = severityError,
                                    onAddClick = onAddSeverity
                                )
                            }

                            MultilineField(
                                label = "Observaciones",
                                value = observations,
                                onValueChange = onObservationsChange,
                                modifier = Modifier.fillMaxWidth(),
                                fieldHeight = 48.dp
                            )

                            Column {
                                Text("Historia de este problema", style = MaterialTheme.typography.labelLarge)
                                Spacer(Modifier.height(4.dp))
                                HistorySection(
                                    rows = historyRows,
                                    isLoading = historyLoading
                                )
                            }
                        }
                    }

                    VerticalDivider()

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(SECTION_GAP)
                    ) {
                        ImageInputColumn(
                            label = "Archivo IR",
                            value = thermalImageName,
                            inspectionNumber = inspectionNumber,
                            onValueChange = onThermalImageChange,
                            onIncrement = onThermalSequenceUp,
                            onDecrement = onThermalSequenceDown,
                            onPickInitial = onThermalPickInitial,
                            onFolder = onThermalFolder,
                            onCamera = onThermalCamera,
                            modifier = Modifier.fillMaxWidth(),
                            isError = thermalError,
                            title = ""
                        )
                        ImageInputColumn(
                            label = "Archivo ID",
                            value = digitalImageName,
                            inspectionNumber = inspectionNumber,
                            onValueChange = onDigitalImageChange,
                            onIncrement = onDigitalSequenceUp,
                            onDecrement = onDigitalSequenceDown,
                            onPickInitial = onDigitalPickInitial,
                            onFolder = onDigitalFolder,
                            onCamera = onDigitalCamera,
                            modifier = Modifier.fillMaxWidth(),
                            isError = digitalError,
                            title = ""
                        )
                    }
                }

                val canSave = !hazardMissing && !severityMissing && imagesProvided
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(
                        onClick = {
                            saveAttempted = true
                            if (canSave) onContinue()
                        },
                        enabled = true
                    ) { Text("Guardar") }
                }
                }
            }
        }
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

@Composable
private fun InfoField(
    label: String,
    value: String,
    ancho: Dp? = null,
    modifier: Modifier = Modifier
) {
    val widthModifier = if (ancho != null) {
        Modifier.widthIn(min = INFO_FIELD_MIN_WIDTH, max = ancho)
    } else {
        Modifier.fillMaxWidth()
    }

    Column(modifier.then(widthModifier)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FIELD_HEIGHT)
                .border(
                    FIELD_BORDER,
                    MaterialTheme.colorScheme.outline,
                    RoundedCornerShape(FIELD_RADIUS)
                )
                .padding(FIELD_PADDING),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onAddClick: () -> Unit = {}
) {
    Column(modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        FilterableSelectorNoLabel(
            options = options,
            selectedId = selectedId,
            onSelected = onSelected,
            isError = isError,
            onAddClick = onAddClick
        )
    }
}

@Composable
private fun AddInlineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(FIELD_HEIGHT)
            .border(
                FIELD_BORDER,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(FIELD_RADIUS)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FilterableSelectorNoLabel(
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    onAddClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selectedId, options) {
        mutableStateOf(options.firstOrNull { it.first == selectedId }?.second.orEmpty())
    }
    val filtered = remember(query, options) {
        val q = query.trim()
        if (q.isBlank()) options else options.filter { (_, text) -> text.contains(q, ignoreCase = true) }
    }
    val outlineColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(FIELD_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FIELD_HEIGHT)
                    .border(FIELD_BORDER, outlineColor, RoundedCornerShape(FIELD_RADIUS))
                    .padding(FIELD_PADDING)
                    .clickable(enabled = options.isNotEmpty()) { expanded = true },
                contentAlignment = Alignment.CenterStart
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BasicTextField(
                        value = query,
                        onValueChange = {
                            query = it
                            expanded = true
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = false)
            ) {
                filtered.forEach { (id, text) ->
                    DropdownMenuItem(
                        text = { Text(text.ifBlank { id }) },
                        onClick = {
                            expanded = false
                            query = text.ifBlank { id }
                            onSelected(id)
                        }
                    )
                }
            }
        }
        AddInlineButton(onClick = onAddClick)
    }
}

@Composable
private fun MultilineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    fieldHeight: Dp = 48.dp
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(FIELD_RADIUS))
                .padding(FIELD_PADDING)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun HistorySection(rows: List<VisualProblemHistoryRow>, isLoading: Boolean) {
    when {
        isLoading -> {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.height(20.dp))
                Text("Cargando historial...")
            }
        }
        rows.isEmpty() -> {
            Text("Sin registros previos para esta ubicación.", style = MaterialTheme.typography.bodyMedium)
        }
        else -> {
            HistoryTable(rows = rows)
        }
    }
}

@Composable
private fun HistoryTable(rows: List<VisualProblemHistoryRow>) {
    val outline = MaterialTheme.colorScheme.outline
    val headerStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    Column(
        Modifier
            .fillMaxWidth()
            .border(1.dp, outline, RoundedCornerShape(8.dp))
    ) {
        HistoryRow(
            cells = listOf("No", "No. Inspección", "Fecha", "Severidad", "Comentarios"),
            isHeader = true,
            textStyle = headerStyle
        )
        Divider()
        rows.forEach { row ->
            HistoryRow(
                cells = listOf(
                    row.numero?.toString().orEmpty().ifBlank { "-" },
                    row.numeroInspeccion?.toString().orEmpty().ifBlank { "-" },
                    row.fecha.orEmpty().ifBlank { "-" },
                    row.severidad.orEmpty().ifBlank { "-" },
                    row.comentario.orEmpty().ifBlank { "-" }
                ),
                isHeader = false,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HistoryRow(
    cells: List<String>,
    isHeader: Boolean,
    textStyle: androidx.compose.ui.text.TextStyle
) {
    val weights = listOf(0.8f, 1f, 1.2f, 1f, 2f)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cells.forEachIndexed { index, cell ->
            Text(
                text = cell,
                style = textStyle,
                modifier = Modifier.weight(weights.getOrElse(index) { 1f }),
                maxLines = if (isHeader) 1 else 3
            )
        }
    }
}

@Composable
private fun ImageInputColumn(
    title: String,
    label: String,
    value: String,
    inspectionNumber: String,
    onValueChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onPickInitial: () -> Unit,
    onFolder: () -> Unit,
    onCamera: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CONTENT_GAP)
    ) {
        if (title.isNotBlank()) {
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        ImageInputButtonGroup(
            label = label,
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            isRequired = true,
            enabled = true,
            onMoveUp = onIncrement,
            onMoveDown = onDecrement,
            onDotsClick = onPickInitial,
            onFolderClick = onFolder,
            onCameraClick = onCamera
        )
        if (isError) {
            Text(
                text = "Cargar imagen requerida",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        ImagePreviewBox(fileName = value, inspectionNumber = inspectionNumber)
    }
}

@Composable
private fun ImagePreviewBox(fileName: String, inspectionNumber: String) {
    val ctx = LocalContext.current
    val eticPrefs = remember { EticPrefs(ctx.settingsDataStore) }
    val rootTreeUriStr by eticPrefs.rootTreeUriFlow.collectAsState(initial = null)
    val rootTreeUri = remember(rootTreeUriStr) { rootTreeUriStr?.let { android.net.Uri.parse(it) } }
    val bitmap = remember(fileName, rootTreeUriStr, inspectionNumber) {
        EticImageStore.loadBitmap(
            context = ctx,
            rootTreeUri = rootTreeUri,
            inspectionNumero = inspectionNumber,
            fileName = fileName
        )?.asImageBitmap()
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (fileName.isBlank()) "Sin imagen" else "Imagen no encontrada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}







