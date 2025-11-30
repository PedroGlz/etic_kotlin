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
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

            var componentTemperature by rememberSaveable { mutableStateOf("") }
            var componentPhase by rememberSaveable { mutableStateOf("") }
            var componentRms by rememberSaveable { mutableStateOf("") }
            var referenceTemperature by rememberSaveable { mutableStateOf("") }
            var referencePhase by rememberSaveable { mutableStateOf("") }
            var referenceRms by rememberSaveable { mutableStateOf("") }
            var additionalInfo by rememberSaveable { mutableStateOf("") }
            var additionalRms by rememberSaveable { mutableStateOf("") }
            var emissivityChecked by rememberSaveable { mutableStateOf(false) }
            var emissivity by rememberSaveable { mutableStateOf("") }
            var indirectTempChecked by rememberSaveable { mutableStateOf(false) }
            var ambientTempChecked by rememberSaveable { mutableStateOf(false) }
            var ambientTemp by rememberSaveable { mutableStateOf("") }
            var environmentChecked by rememberSaveable { mutableStateOf(false) }
            var environment by rememberSaveable { mutableStateOf("") }
            var windSpeedChecked by rememberSaveable { mutableStateOf(false) }
            var windSpeed by rememberSaveable { mutableStateOf("") }
            var manufacturer by rememberSaveable { mutableStateOf("") }
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
                Divider()

                Spacer(Modifier.height(12.dp))
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
                        SimpleField(
                            label = "*Elemento",
                            value = componentPhase,
                            onValueChange = { componentPhase = it }
                        )
                        SimpleField(
                            label = "*Fase de referencia",
                            value = referencePhase,
                            onValueChange = { referencePhase = it }
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

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(0.6f)) {
                        Text("Información adicional", style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(Modifier.weight(0.25f)) {
                        SimpleField(
                            label = "Selección",
                            value = additionalInfo,
                            onValueChange = { additionalInfo = it }
                        )
                    }
                    Column(Modifier.weight(0.15f)) {
                        LabeledField(
                            label = "I RMS",
                            value = additionalRms,
                            onValueChange = { additionalRms = it },
                            unit = "A"
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

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
                                SimpleField(
                                    label = "Ambiente",
                                    value = environment,
                                    onValueChange = { environment = it }
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
                        SimpleField(
                            label = "Fabricante",
                            value = manufacturer,
                            onValueChange = { manufacturer = it }
                        )
                        Divider()
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

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(12.dp))

                Text("Comentarios", style = MaterialTheme.typography.bodyMedium)
                TextField(
                    value = comments,
                    onValueChange = { comments = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("Comentarios") }
                )

                Spacer(Modifier.height(16.dp))
                content()

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
private fun SimpleField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) }
    )
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
