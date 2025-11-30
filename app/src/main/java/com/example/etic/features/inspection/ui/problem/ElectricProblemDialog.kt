package com.example.etic.features.inspection.ui.problem

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val DIALOG_MIN_WIDTH = 720.dp
private val DIALOG_MAX_WIDTH = 980.dp
private val INFO_FIELD_MIN_WIDTH = 130.dp
private val INFO_FIELD_MAX_WIDTH = 220.dp

@Composable
fun ElectricProblemDialog(
    inspectionNumber: String,
    problemNumber: String,
    problemType: String,
    equipmentName: String,
    equipmentRoute: String,
    phaseOptions: List<Pair<String, String>>,
    environmentOptions: List<Pair<String, String>>,
    manufacturerOptions: List<Pair<String, String>>,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    continueEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
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
            val infoRowScrollState = rememberScrollState()

            var selectedTab by rememberSaveable { mutableStateOf(0) }
            var componentTemperature by rememberSaveable { mutableStateOf("") }
            var componentPhaseId by rememberSaveable { mutableStateOf<String?>(null) }
            var componentRms by rememberSaveable { mutableStateOf("") }
            var referenceTemperature by rememberSaveable { mutableStateOf("") }
            var referencePhaseId by rememberSaveable { mutableStateOf<String?>(null) }
            var referenceRms by rememberSaveable { mutableStateOf("") }
            var additionalInfoId by rememberSaveable { mutableStateOf<String?>(null) }
            var additionalRms by rememberSaveable { mutableStateOf("") }
            var emissivityChecked by rememberSaveable { mutableStateOf(false) }
            var emissivity by rememberSaveable { mutableStateOf("") }
            var indirectTempChecked by rememberSaveable { mutableStateOf(false) }
            var ambientTempChecked by rememberSaveable { mutableStateOf(false) }
            var ambientTemp by rememberSaveable { mutableStateOf("") }
            var environmentChecked by rememberSaveable { mutableStateOf(false) }
            var environmentId by rememberSaveable { mutableStateOf<String?>(null) }
            var windSpeedChecked by rememberSaveable { mutableStateOf(false) }
            var windSpeed by rememberSaveable { mutableStateOf("") }
            var manufacturerId by rememberSaveable { mutableStateOf<String?>(null) }
            var ratedLoad by rememberSaveable { mutableStateOf("") }
            var circuitVoltage by rememberSaveable { mutableStateOf("") }
            var comments by rememberSaveable { mutableStateOf("") }

            Column(
                Modifier
                    .widthIn(min = DIALOG_MIN_WIDTH, max = DIALOG_MAX_WIDTH)
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text("Problema Eléctrico", style = MaterialTheme.typography.headlineSmall)

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(infoRowScrollState),
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

                Spacer(Modifier.height(16.dp))
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Datos") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Imágenes") })
                }

                Spacer(Modifier.height(16.dp))
                when (selectedTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SectionRow {
                                Column(Modifier.weight(0.5f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Temperatura", style = MaterialTheme.typography.titleSmall)
                                    LabeledField(
                                        label = "*Componente con anomalía",
                                        value = componentTemperature,
                                        onValueChange = { componentTemperature = it },
                                        unit = "°C"
                                    )
                                    LabeledField(
                                        label = "*Componente de referencia",
                                        value = referenceTemperature,
                                        onValueChange = { referenceTemperature = it },
                                        unit = "°C"
                                    )
                                }
                                Column(Modifier.weight(0.3f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Elemento", style = MaterialTheme.typography.titleSmall)
                                    DropdownSelector(
                                        label = "*Elemento",
                                        options = phaseOptions,
                                        selectedId = componentPhaseId,
                                        onSelected = { componentPhaseId = it }
                                    )
                                    DropdownSelector(
                                        label = "*Fase de referencia",
                                        options = phaseOptions,
                                        selectedId = referencePhaseId,
                                        onSelected = { referencePhaseId = it }
                                    )
                                }
                                Column(Modifier.weight(0.2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("I RMS", style = MaterialTheme.typography.titleSmall)
                                    LabeledField(
                                        label = "I RMS",
                                        value = componentRms,
                                        onValueChange = { componentRms = it },
                                        unit = "A"
                                    )
                                    LabeledField(
                                        label = "I RMS Ref.",
                                        value = referenceRms,
                                        onValueChange = { referenceRms = it },
                                        unit = "A"
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(0.5f)) {
                                    Text("Información adicional", style = MaterialTheme.typography.bodyMedium)
                                }
                                Column(Modifier.weight(0.3f)) {
                                    DropdownSelector(
                                        label = "Selección",
                                        options = phaseOptions,
                                        selectedId = additionalInfoId,
                                        onSelected = { additionalInfoId = it }
                                    )
                                }
                                Column(Modifier.weight(0.2f)) {
                                    LabeledField(
                                        label = "I RMS",
                                        value = additionalRms,
                                        onValueChange = { additionalRms = it },
                                        unit = "A"
                                    )
                                }
                            }

                            Divider()

                            SectionRow {
                                Column(Modifier.weight(0.5f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CheckboxField(
                                        label = "Emisividad",
                                        checked = emissivityChecked,
                                        onCheckedChange = { emissivityChecked = it },
                                        trailing = {
                                            LabeledField(
                                                label = "Emisividad",
                                                value = emissivity,
                                                onValueChange = { emissivity = it }
                                            )
                                        }
                                    )
                                    CheckboxField(
                                        label = "Temp. indirecta",
                                        checked = indirectTempChecked,
                                        onCheckedChange = { indirectTempChecked = it }
                                    )
                                    CheckboxField(
                                        label = "Temp. ambiente",
                                        checked = ambientTempChecked,
                                        onCheckedChange = { ambientTempChecked = it },
                                        trailing = {
                                            LabeledField(
                                                label = "Temp. ambiente",
                                                value = ambientTemp,
                                                onValueChange = { ambientTemp = it },
                                                unit = "°C"
                                            )
                                        }
                                    )
                                    CheckboxField(
                                        label = "Tipo ambiente",
                                        checked = environmentChecked,
                                        onCheckedChange = { environmentChecked = it },
                                        trailing = {
                                            DropdownSelector(
                                                label = "Ambiente",
                                                options = environmentOptions,
                                                selectedId = environmentId,
                                                onSelected = { environmentId = it }
                                            )
                                        }
                                    )
                                }
                                Column(Modifier.weight(0.5f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CheckboxField(
                                        label = "Velocidad del viento",
                                        checked = windSpeedChecked,
                                        onCheckedChange = { windSpeedChecked = it },
                                        trailing = {
                                            LabeledField(
                                                label = "Velocidad viento",
                                                value = windSpeed,
                                                onValueChange = { windSpeed = it },
                                                unit = "m/s"
                                            )
                                        }
                                    )
                                    DropdownSelector(
                                        label = "Fabricante",
                                        options = manufacturerOptions,
                                        selectedId = manufacturerId,
                                        onSelected = { manufacturerId = it }
                                    )
                                    Text("Especificación eléctrica", style = MaterialTheme.typography.titleMedium)
                                    LabeledField(
                                        label = "Corriente nominal (A)",
                                        value = ratedLoad,
                                        onValueChange = { ratedLoad = it },
                                        unit = "A"
                                    )
                                    LabeledField(
                                        label = "Voltaje nominal (V)",
                                        value = circuitVoltage,
                                        onValueChange = { circuitVoltage = it },
                                        unit = "V"
                                    )
                                }
                            }

                            Text("Comentarios", style = MaterialTheme.typography.bodyMedium)
                            TextField(
                                value = comments,
                                onValueChange = { comments = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                label = { Text("Comentarios") }
                            )
                        }
                    }
                    else -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            content()
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Button(onClick = onContinue, enabled = continueEnabled) { Text("Guardar") }
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

@Composable
private fun SectionRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        content()
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            label = { Text(label) }
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CheckboxField(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label, style = MaterialTheme.typography.bodyMedium)
        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (options.isNotEmpty()) expanded = !expanded }
    ) {
        TextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Seleccionar") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text.ifBlank { id }) },
                    onClick = {
                        expanded = false
                        onSelected(id)
                    }
                )
            }
        }
    }
}
