package com.example.etic.features.inspection.ui.problem

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.RowScope
import java.io.File
import com.example.etic.features.components.ImageInputButtonGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

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
    failureOptions: List<Pair<String, String>>,
    phaseOptions: List<Pair<String, String>>,
    environmentOptions: List<Pair<String, String>>,
    manufacturerOptions: List<Pair<String, String>>,
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
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    continueEnabled: Boolean = true
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
            var failureId by rememberSaveable { mutableStateOf<String?>(null) }
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
                Text("Problema Eléctrico", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(12.dp))
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

                Spacer(Modifier.height(8.dp))
                ReadOnlyFormField(
                    label = "Ruta del equipo",
                    value = equipmentRoute,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(12.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Datos", style = MaterialTheme.typography.bodySmall) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Imágenes", style = MaterialTheme.typography.bodySmall) }
                    )
                }

                Spacer(Modifier.height(12.dp))
                when (selectedTab) {
                    0 -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                            // ─────────────────────────────
                            // Fila independiente para *Falla
                            // ─────────────────────────────
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(0.3f)) {
                                    Text(
                                        text = "*Falla",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Column(Modifier.weight(0.7f)) {
                                    DropdownSelectorNoLabel(
                                        options = failureOptions,
                                        selectedId = failureId,
                                        onSelected = { failureId = it }
                                    )
                                }
                            }

                            // ─────────────────────────────
                            // Tabla de 4 columnas x 3 filas
                            // Col1: títulos de fila
                            // Col2: Temperatura
                            // Col3: Elemento
                            // Col4: I RMS
                            // ─────────────────────────────
                            // Color suave para encabezado
                            val headerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)

                            // Color suave para divisores
                            val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.23f)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                // ───────────── Encabezados ─────────────
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(headerColor)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Columna 1 vacía
                                    Column(Modifier.weight(0.3f)) {}

                                    // Columna 2
                                    Column(
                                        Modifier.weight(0.3f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Temperatura", style = MaterialTheme.typography.labelSmall)
                                    }

                                    // Columna 3
                                    Column(
                                        Modifier.weight(0.2f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Elemento", style = MaterialTheme.typography.labelSmall)
                                    }

                                    // Columna 4
                                    Column(
                                        Modifier.weight(0.2f),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("I RMS", style = MaterialTheme.typography.labelSmall)
                                    }
                                }

                                Divider(color = lineColor, thickness = 1.dp)

                                // ───────────── Fila 1 ─────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(0.3f)) {
                                        Text("*Componente con anomalía", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Column(Modifier.weight(0.3f)) {
                                        ValueFieldNoLabel(
                                            value = componentTemperature,
                                            onValueChange = { componentTemperature = it },
                                            unit = "°C"
                                        )
                                    }
                                    Column(Modifier.weight(0.2f)) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = componentPhaseId,
                                            onSelected = { componentPhaseId = it }
                                        )
                                    }
                                    Column(Modifier.weight(0.2f)) {
                                        ValueFieldNoLabel(
                                            value = componentRms,
                                            onValueChange = { componentRms = it },
                                            unit = "A"
                                        )
                                    }
                                }

                                Divider(color = lineColor, thickness = 1.dp)

                                // ───────────── Fila 2 ─────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(0.3f)) {
                                        Text("*Componente de referencia", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Column(Modifier.weight(0.3f)) {
                                        ValueFieldNoLabel(
                                            value = referenceTemperature,
                                            onValueChange = { referenceTemperature = it },
                                            unit = "°C"
                                        )
                                    }
                                    Column(Modifier.weight(0.2f)) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = referencePhaseId,
                                            onSelected = { referencePhaseId = it }
                                        )
                                    }
                                    Column(Modifier.weight(0.2f)) {
                                        ValueFieldNoLabel(
                                            value = referenceRms,
                                            onValueChange = { referenceRms = it },
                                            unit = "A"
                                        )
                                    }
                                }

                                Divider(color = lineColor, thickness = 1.dp)

                                // ───────────── Fila 3 (Información adicional) ─────────────
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(0.3f)) {
                                        Text("Información adicional", style = MaterialTheme.typography.labelSmall)
                                    }

                                    // Columna vacía para alinear a la derecha
                                    Column(Modifier.weight(0.3f)) {}

                                    Column(Modifier.weight(0.2f)) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = additionalInfoId,
                                            onSelected = { additionalInfoId = it }
                                        )
                                    }

                                    Column(Modifier.weight(0.2f)) {
                                        ValueFieldNoLabel(
                                            value = additionalRms,
                                            onValueChange = { additionalRms = it },
                                            unit = "A"
                                        )
                                    }
                                }
                            }

                            Divider()

                            // ─────────────────────────────
                            // Resto de la sección (emisividad, ambiente, etc.)
                            // ─────────────────────────────
                            SectionRow {
                                Column(
                                    Modifier.weight(0.5f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // ───────────── Emisividad (tabla 2x2 compacta) ─────────────
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {

                                        // ----- Fila 1 -----
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Columna 1 vacía
                                            Box(modifier = Modifier.weight(0.15f)) {}

                                            // Columna 2: Label centrado
                                            Box(
                                                modifier = Modifier.weight(0.85f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "Emisividad",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        // ----- Fila 2 -----
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Columna 1: checkbox pegado a la izquierda
                                            Box(
                                                modifier = Modifier.weight(0.15f),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Checkbox(
                                                    checked = emissivityChecked,
                                                    onCheckedChange = { emissivityChecked = it }
                                                )
                                            }

                                            // Columna 2: textfield numérico pegado al checkbox
                                            Box(
                                                modifier = Modifier
                                                    .weight(0.85f)
                                                    .height(32.dp)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp),
                                                contentAlignment = Alignment.CenterStart
                                            ) {
                                                BasicTextField(
                                                    value = emissivity,
                                                    onValueChange = { emissivity = it },
                                                    singleLine = true,
                                                    textStyle = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    ),
                                                    keyboardOptions = KeyboardOptions(
                                                        keyboardType = KeyboardType.Number
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }

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
                                            ) { environmentId = it }
                                        }
                                    )
                                }
                                Column(
                                    Modifier.weight(0.5f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                    ) { manufacturerId = it }
                                    Text(
                                        "Especificación eléctrica",
                                        style = MaterialTheme.typography.labelMedium
                                    )
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

                            Text("Comentarios", style = MaterialTheme.typography.labelMedium)
                            MultilineField(
                                value = comments,
                                onValueChange = { comments = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(96.dp)
                            )
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Cargas de imágenes", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ImageInputColumn(
                                    title = "",
                                    label = "Archivo IR",
                                    value = thermalImageName,
                                    onValueChange = onThermalImageChange,
                                    onIncrement = onThermalSequenceUp,
                                    onDecrement = onThermalSequenceDown,
                                    onPickInitial = onThermalPickInitial,
                                    onFolder = onThermalFolder,
                                    onCamera = onThermalCamera,
                                    modifier = Modifier.weight(1f)
                                )
                                ImageInputColumn(
                                    title = "",
                                    label = "Archivo ID",
                                    value = digitalImageName,
                                    onValueChange = onDigitalImageChange,
                                    onIncrement = onDigitalSequenceUp,
                                    onDecrement = onDigitalSequenceDown,
                                    onPickInitial = onDigitalPickInitial,
                                    onFolder = onDigitalFolder,
                                    onCamera = onDigitalCamera,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
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
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
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
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
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
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (unit != null) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ValueFieldNoLabel(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DropdownSelectorNoLabel(
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp)
            .clickable { expanded = true },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedLabel.isNotBlank()) selectedLabel else "Seleccionar",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { (id, text) ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = text.ifBlank { id },
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                onClick = {
                    expanded = false
                    onSelected(id)
                }
            )
        }
    }
}

@Composable
private fun SimpleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun MultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = false,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxSize()
        )
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
        Text(label, style = MaterialTheme.typography.bodySmall)
        trailing?.invoke()
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (title.isNotBlank()) {
            Text(title, style = MaterialTheme.typography.labelMedium)
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
        ImagePreviewBox(fileName = value)
    }
}

@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    Column(Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp)
                .clickable { expanded = true },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedLabel.isNotBlank()) selectedLabel else "Seleccionar",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text.ifBlank { id },
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelected(id)
                    }
                )
            }
        }
    }
}
