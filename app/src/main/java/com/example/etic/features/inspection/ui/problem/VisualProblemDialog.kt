package com.example.etic.features.inspection.ui.problem

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.data.local.dao.VisualProblemHistoryRow

private val DIALOG_MIN_WIDTH = 720.dp
private val DIALOG_MAX_WIDTH = 980.dp
private val INFO_FIELD_MIN_WIDTH = 130.dp
private val INFO_FIELD_MAX_WIDTH = 220.dp

@OptIn(ExperimentalMaterial3Api::class)
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
    onHazardSelected: (String) -> Unit,
    onSeveritySelected: (String) -> Unit,
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
                
                Spacer(Modifier.height(16.dp))
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
                TextField(
                    value = equipmentRoute,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ruta del equipo") },
                    modifier = Modifier.fillMaxWidth()
                )

                var selectedTab by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Datos") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Imágenes") })
                }

                Spacer(Modifier.height(16.dp))
                when (selectedTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                DropdownField(
                                    label = "Problema",
                                    options = hazardIssues,
                                    selectedId = selectedHazardIssue,
                                    placeholder = "Selecciona una falla",
                                    onSelected = onHazardSelected,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownField(
                                    label = "Severidad",
                                    options = severities,
                                    selectedId = selectedSeverity,
                                    placeholder = "Selecciona severidad",
                                    onSelected = onSeveritySelected,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            TextField(
                                value = observations,
                                onValueChange = onObservationsChange,
                                label = { Text("Observaciones") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                minLines = 2
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Sección de imágenes en desarrollo.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onContinue) { Text("Continuar") }
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
        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(label) },
            modifier = Modifier.widthIn(min = INFO_FIELD_MIN_WIDTH, max = INFO_FIELD_MAX_WIDTH)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    placeholder: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val expanded = remember { mutableStateOf(false) }
    val currentLabel = options.firstOrNull { it.first == selectedId }?.second ?: placeholder
    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            if (options.isNotEmpty()) expanded.value = !expanded.value
        },
        modifier = modifier
    ) {
        TextField(
            value = currentLabel,
            onValueChange = {},
            readOnly = true,
            enabled = options.isNotEmpty(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            DropdownMenuItem(
                text = { Text("Seleccionar...") },
                onClick = {
                    expanded.value = false
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
                        expanded.value = false
                        onSelected(id)
                    }
                )
            }
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
