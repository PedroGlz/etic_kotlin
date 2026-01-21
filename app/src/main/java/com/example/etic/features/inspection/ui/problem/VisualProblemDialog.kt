package com.example.etic.features.inspection.ui.problem

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.etic.data.local.dao.VisualProblemHistoryRow
import com.example.etic.features.components.ImageInputButtonGroup
import java.io.File

private val DIALOG_MIN_WIDTH = 720.dp
private val DIALOG_MAX_WIDTH = 980.dp
private val INFO_FIELD_MIN_WIDTH = 130.dp
private val INFO_FIELD_MAX_WIDTH = 220.dp

private val FIELD_HEIGHT = 25.dp
private val FIELD_RADIUS = 4.dp
private val FIELD_BORDER = 1.dp
private val FIELD_PADDING = PaddingValues(horizontal = 4.dp, vertical = 2.dp)

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
    onCronicoClick: (() -> Unit)? = null,
    cronicoEnabled: Boolean = false,
    cronicoChecked: Boolean = false,
    cerradoChecked: Boolean = false,
    cerradoEnabled: Boolean = false,
    onCerradoChange: (Boolean) -> Unit = {},
    showEditControls: Boolean = false,
    onDismiss: () -> Unit,
    onContinue: () -> Unit
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
            Column(
                Modifier
                    .widthIn(min = DIALOG_MIN_WIDTH, max = DIALOG_MAX_WIDTH)
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text("Problema Visual", style = MaterialTheme.typography.headlineSmall)
                if (showEditControls) {
                    Divider(Modifier.padding(top = 12.dp, bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.ArrowLeft, contentDescription = "Anterior")
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.ArrowRight, contentDescription = "Siguiente")
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = cronicoChecked,
                                onCheckedChange = {},
                                enabled = false
                            )
                            Text("Cronico")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = cerradoChecked,
                                onCheckedChange = { onCerradoChange(it) },
                                enabled = cerradoEnabled
                            )
                            Text("Cerrado")
                        }
                        IconButton(
                            onClick = { onCronicoClick?.invoke() },
                            enabled = cronicoEnabled
                        ) {
                            Icon(
                                Icons.Outlined.AccessTime,
                                contentDescription = "Historial",
                                tint = Color(0xFFFFC107)
                            )
                        }
                    }
                    Divider(Modifier.padding(top = 8.dp, bottom = 16.dp))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(infoRowScroll),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoField("Inspección No.", inspectionNumber)
                    InfoField("Problema No.", problemNumber)
                    InfoField("Tipo de problema", problemType)
                    InfoField("Equipo", equipmentName)
                }

                Spacer(Modifier.height(12.dp))
                ReadOnlyFormField(
                    label = "Ruta del equipo",
                    value = equipmentRoute,
                    modifier = Modifier.fillMaxWidth()
                )

                var selectedTab by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Datos") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Imágenes") })
                }

                Spacer(Modifier.height(16.dp))
                val hazardError = selectedHazardIssue.isNullOrBlank()
                val severityError = selectedSeverity.isNullOrBlank()
                val thermalError = thermalImageName.isBlank()
                val digitalError = digitalImageName.isBlank()
                when (selectedTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DropdownSelector(
                                    label = "Problema *",
                                    options = hazardIssues,
                                    selectedId = selectedHazardIssue,
                                    onSelected = onHazardSelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = hazardError
                                )
                                DropdownSelector(
                                    label = "Severidad *",
                                    options = severities,
                                    selectedId = selectedSeverity,
                                    onSelected = onSeveritySelected,
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = severityError
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
                                Spacer(Modifier.height(8.dp))
                                HistorySection(
                                    rows = historyRows,
                                    isLoading = historyLoading
                                )
                            }
                        }
                    }
                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Cargas de imágenes", style = MaterialTheme.typography.labelLarge)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ImageInputColumn(
                                    label = "Archivo IR",
                                    value = thermalImageName,
                                    onValueChange = onThermalImageChange,
                                    onIncrement = onThermalSequenceUp,
                                    onDecrement = onThermalSequenceDown,
                                    onPickInitial = onThermalPickInitial,
                                    onFolder = onThermalFolder,
                                    onCamera = onThermalCamera,
                                    modifier = Modifier.weight(1f),
                                    isError = thermalError,
                                    title = ""
                                )
                                ImageInputColumn(
                                    label = "Archivo ID",
                                    value = digitalImageName,
                                    onValueChange = onDigitalImageChange,
                                    onIncrement = onDigitalSequenceUp,
                                    onDecrement = onDigitalSequenceDown,
                                    onPickInitial = onDigitalPickInitial,
                                    onFolder = onDigitalFolder,
                                    onCamera = onDigitalCamera,
                                    modifier = Modifier.weight(1f),
                                    isError = digitalError,
                                    title = ""
                                )
                            }
                        }
                    }
                }

                val canSave = !hazardError && !severityError && !thermalError && !digitalError
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = onContinue, enabled = canSave) { Text("Guardar") }
                }
            }
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(
        Modifier
            .widthIn(min = INFO_FIELD_MIN_WIDTH, max = INFO_FIELD_MAX_WIDTH)
            .defaultMinSize(minWidth = INFO_FIELD_MIN_WIDTH)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .widthIn(min = INFO_FIELD_MIN_WIDTH, max = INFO_FIELD_MAX_WIDTH)
                .height(FIELD_HEIGHT)
                .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(FIELD_RADIUS))
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
private fun ReadOnlyFormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(FIELD_HEIGHT)
                .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(FIELD_RADIUS))
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
    isError: Boolean = false
) {
    Column(modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        DropdownSelectorNoLabel(
            options = options,
            selectedId = selectedId,
            onSelected = onSelected,
            isError = isError
        )
    }
}

@Composable
private fun DropdownSelectorNoLabel(
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()
    val outlineColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(FIELD_HEIGHT)
            .border(FIELD_BORDER, outlineColor, RoundedCornerShape(FIELD_RADIUS))
            .padding(FIELD_PADDING)
            .clickable(enabled = options.isNotEmpty()) { expanded = true },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (selectedLabel.isNotBlank()) selectedLabel else "Seleccionar",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "v",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text("Seleccionar...") },
            onClick = {
                expanded = false
                onSelected("")
            }
        )
        if (options.isNotEmpty()) {
            Divider()
        }
        options.forEach { (id, text) ->
            DropdownMenuItem(
                text = { Text(text) },
                onClick = {
                    expanded = false
                    onSelected(id)
                }
            )
        }
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
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
                text = "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        ImagePreviewBox(fileName = value)
    }
}

@Composable
private fun ImagePreviewBox(fileName: String) {
    val ctx = LocalContext.current
    val bitmap = remember(fileName) {
        if (fileName.isBlank()) null
        else {
            val file = File(ctx.filesDir, "Imagenes/$fileName")
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
        }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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
