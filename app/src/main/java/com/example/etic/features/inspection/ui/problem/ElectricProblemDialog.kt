package com.example.etic.features.inspection.ui.problem

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
//import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.features.components.ImageInputButtonGroup
import java.io.File

private val DIALOG_MIN_WIDTH = 710.dp
private val DIALOG_MAX_WIDTH = 710.dp
private val INFO_FIELD_MIN_WIDTH = 100.dp
private val INFO_FIELD_MAX_WIDTH = 100.dp

// ✅ Homogeneidad de inputs
private val FIELD_HEIGHT = 25.dp
private val FIELD_RADIUS = 4.dp
private val FIELD_BORDER = 1.dp
private val FIELD_PADDING = PaddingValues(horizontal = 4.dp, vertical = 2.dp)

// ✅ Compactación checkbox + input
private val CHECKBOX_GAP = 4.dp
private val SECTION_GAP = 12.dp
private val ROW_GAP = 12.dp

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
    showEditControls: Boolean = false,
    onDismiss: () -> Unit,
    onContinue: (ElectricProblemFormData) -> Unit,
    continueEnabled: Boolean = true,
    initialFormData: ElectricProblemFormData? = null,
    dialogKey: Any = Unit
) {
    key(dialogKey) {
        val initial = initialFormData ?: ElectricProblemFormData()
        val initialFailureLabel = failureOptions.firstOrNull { it.first == initial.failureId }?.second?.takeIf { it.isNotBlank() }
        val initialPhaseLabel = phaseOptions.firstOrNull { it.first == initial.componentPhaseId }?.second?.takeIf { it.isNotBlank() }
        val initialAutoCommentText = buildList {
            initialFailureLabel?.let { add(it) }
            initialPhaseLabel?.let { add(it) }
            equipmentName.takeUnless { it.isBlank() }?.let { add(it) }
        }.joinToString(", ")
        val initialCommentWasAuto = initial.comments.isNotBlank() && initial.comments == initialAutoCommentText
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

                var failureId by rememberSaveable { mutableStateOf(initial.failureId) }
                var componentTemperature by rememberSaveable { mutableStateOf(initial.componentTemperature) }
                var componentPhaseId by rememberSaveable { mutableStateOf(initial.componentPhaseId) }
                var componentRms by rememberSaveable { mutableStateOf(initial.componentRms) }

                var referenceTemperature by rememberSaveable { mutableStateOf(initial.referenceTemperature) }
                var referencePhaseId by rememberSaveable { mutableStateOf(initial.referencePhaseId) }
                var referenceRms by rememberSaveable { mutableStateOf(initial.referenceRms) }

                var additionalInfoId by rememberSaveable { mutableStateOf(initial.additionalInfoId) }
                var additionalRms by rememberSaveable { mutableStateOf(initial.additionalRms) }

                var emissivityChecked by rememberSaveable { mutableStateOf(initial.emissivityChecked) }
                var emissivity by rememberSaveable { mutableStateOf(initial.emissivity) }
                var emissivityError by rememberSaveable { mutableStateOf<String?>(null) }

                var indirectTempChecked by rememberSaveable { mutableStateOf(initial.indirectTempChecked) }

                var ambientTempChecked by rememberSaveable { mutableStateOf(initial.ambientTempChecked) }
                var ambientTemp by rememberSaveable { mutableStateOf(initial.ambientTemp) }

                var environmentChecked by rememberSaveable { mutableStateOf(initial.environmentChecked) }
                var environmentId by rememberSaveable { mutableStateOf(initial.environmentId) }

                var windSpeedChecked by rememberSaveable { mutableStateOf(initial.windSpeedChecked) }
                var windSpeed by rememberSaveable { mutableStateOf(initial.windSpeed) }

                var manufacturerId by rememberSaveable { mutableStateOf(initial.manufacturerId) }

                var ratedLoad by rememberSaveable { mutableStateOf(initial.ratedLoad) }
                var circuitVoltage by rememberSaveable { mutableStateOf(initial.circuitVoltage) }

                var comments by rememberSaveable { mutableStateOf(initial.comments) }
                var commentsTouched by rememberSaveable {
                    mutableStateOf(initial.comments.isNotBlank() && !initialCommentWasAuto)
                }
                var lastAutoComment by rememberSaveable {
                    mutableStateOf(if (initialCommentWasAuto) initial.comments else "")
                }

            val failureLabel = failureOptions.firstOrNull { it.first == failureId }?.second?.takeIf { it.isNotBlank() }
            val phaseLabel = phaseOptions.firstOrNull { it.first == componentPhaseId }?.second?.takeIf { it.isNotBlank() }
            val requiredFieldsFilled = listOf(
                failureId?.isNotBlank() == true,
                componentTemperature.trim().isNotBlank(),
                componentPhaseId?.isNotBlank() == true,
                referenceTemperature.trim().isNotBlank(),
                referencePhaseId?.isNotBlank() == true
            ).all { it }

            val autoCommentText = buildList {
                failureLabel?.let { add(it) }
                phaseLabel?.let { add(it) }
                equipmentName.takeUnless { it.isBlank() }?.let { add(it) }
            }.joinToString(", ")

            LaunchedEffect(autoCommentText) {
                if (autoCommentText.isNotBlank()) {
                    if (!commentsTouched || comments == lastAutoComment || comments.isBlank()) {
                        comments = autoCommentText
                        lastAutoComment = autoCommentText
                        commentsTouched = false
                    }
                } else if (!commentsTouched) {
                    comments = ""
                    lastAutoComment = ""
                }
            }

            val handleEmissivityInput: (String) -> Unit = { input ->
                val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                val normalized = filtered.replace(',', '.')
                val number = normalized.toDoubleOrNull()
                val decimals = normalized.substringAfter('.', "")
                val hasTooManyDecimals = normalized.contains('.') && decimals.length > 2
                val shouldValidate = emissivityChecked

                when {
                    filtered.isBlank() -> {
                        emissivity = ""
                        emissivityError = if (shouldValidate) "Fuera del rango valido" else null
                    }
                    number == null -> emissivityError = if (shouldValidate) "Fuera del rango valido" else null
                    number < 0.0 || number > 1.0 ->
                        emissivityError = if (shouldValidate) "Ingresar valor entre 0.00 y 1.00" else null
                    hasTooManyDecimals ->
                        emissivityError = if (shouldValidate) "Ingresar valor con máximo 2 decimales" else null
                    else -> {
                        emissivity = filtered
                        emissivityError = null
                    }
                }
            }

            LaunchedEffect(emissivityChecked) {
                emissivityError = if (emissivityChecked && emissivity.isBlank()) "Fuera del rango valido" else null
            }

            val thermalError = thermalImageName.isBlank()
            val digitalError = digitalImageName.isBlank()
            val imagesProvided = !thermalError && !digitalError

            Column(
                Modifier
                    .widthIn(min = DIALOG_MIN_WIDTH, max = DIALOG_MAX_WIDTH)
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                Text("Problema Eléctrico", style = MaterialTheme.typography.titleMedium)

                if (showEditControls) {
                    var isCronico by rememberSaveable { mutableStateOf(false) }
                    var isCerrado by rememberSaveable { mutableStateOf(false) }
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
                            Checkbox(checked = isCronico, onCheckedChange = { isCronico = it })
                            Text("Cronico")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isCerrado, onCheckedChange = { isCerrado = it })
                            Text("Cerrado")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = "Historial")
                        }
                    }
                    Divider(Modifier.padding(top = 8.dp, bottom = 12.dp))
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(infoRowScrollState),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoField("Inspección No.", inspectionNumber, 85.dp)
                    InfoField("Problema No.", problemNumber, 85.dp)
                    InfoField("Tipo de problema", problemType, 95.dp)
                    InfoField("Equipo", equipmentName, 210.dp)
                }

                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ROW_GAP),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column() {
                        Text(text = "*Falla", style = MaterialTheme.typography.labelSmall)
                        DropdownSelectorNoLabel(
                            options = failureOptions,
                            selectedId = failureId,
                            onSelected = { failureId = it },
                            ancho = 180.dp
                        )
                    }
                    Column() {
                        ReadOnlyFormField(
                            label = "Ruta del equipo",
                            value = equipmentRoute,
                            modifier = Modifier.fillMaxWidth(),
                            ancho = 480.dp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                TabRow(selectedTabIndex = selectedTab, divider = {}) {
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
                        Column(verticalArrangement = Arrangement.spacedBy(SECTION_GAP)) {

                            val headerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f)
                            val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.23f)

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(headerColor)
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CenterCell(0.2f) {}

                                    CenterCell(0.1f) {
                                        Text(
                                            text = "Temperatura",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    CenterCell(0.3f) {
                                        Text(
                                            text = "Elemento",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    CenterCell(0.1f) {
                                        Text(
                                            text = "I RMS",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }



                                Divider(color = lineColor, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                )
                                {
                                    // Columna 1: etiqueta
                                    CenterCell(0.2f) {
                                        Text(
                                            "*Componente con anomalía",
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Columna 2: temperatura
                                    CenterCell(0.1f) {
                                        ValueFieldNoLabel(
                                            value = componentTemperature,
                                            onValueChange = { componentTemperature = it },
                                            unit = "°C",
                                        )
                                    }

                                    // Columna 3: elemento
                                    CenterCell(0.3f) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = componentPhaseId,
                                            onSelected = { componentPhaseId = it },
                                        )
                                    }

                                    // Columna 4: RMS
                                    CenterCell(0.1f) {
                                        ValueFieldNoLabel(
                                            value = componentRms,
                                            onValueChange = { componentRms = it },
                                            unit = "A",
                                        )
                                    }
                                }

                                Divider(color = lineColor, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(0.2f)) {
                                        Text("*Componente de referencia", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Column(Modifier.weight(0.1f)) {
                                        ValueFieldNoLabel(
                                            value = referenceTemperature,
                                            onValueChange = { referenceTemperature = it },
                                            unit = "°C",
                                        )
                                    }
                                    Column(Modifier.weight(0.3f)) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = referencePhaseId,
                                            onSelected = { referencePhaseId = it },
                                        )
                                    }
                                    Column(Modifier.weight(0.1f)) {
                                        ValueFieldNoLabel(
                                            value = referenceRms,
                                            onValueChange = { referenceRms = it },
                                            unit = "A",
                                        )
                                    }
                                }

                                Divider(color = lineColor, thickness = 1.dp)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(0.2f)) {
                                        Text("Información adicional", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Column(Modifier.weight(0.1f)) {}
                                    Column(Modifier.weight(0.3f)) {
                                        DropdownSelectorNoLabel(
                                            options = phaseOptions,
                                            selectedId = additionalInfoId,
                                            onSelected = { additionalInfoId = it },
                                        )
                                    }
                                    Column(Modifier.weight(0.1f)) {
                                        ValueFieldNoLabel(
                                            value = additionalRms,
                                            onValueChange = { additionalRms = it },
                                            unit = "A",
                                        )
                                    }
                                }
                            }

                            Divider()

                            // ✅ 2 columnas 50/50, compactas y con checkbox pegado al input
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Col 1 (50%)
                                Column(modifier = Modifier.weight(1f)) {
                                    val currentEmissivityError = emissivityError

                                    Column {
                                        CheckboxNumericRow(
                                            label = "Emisividad",
                                            checked = emissivityChecked,
                                            onCheckedChange = { emissivityChecked = it },
                                            value = emissivity,
                                            onValueChange = handleEmissivityInput
                                        )
                                        if (currentEmissivityError != null) {
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                text = currentEmissivityError,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(CHECKBOX_GAP),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = indirectTempChecked,
                                            onCheckedChange = { indirectTempChecked = it },
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .padding(0.dp)
                                        )
                                        Text("Temp. indirecta", style = MaterialTheme.typography.labelSmall)
                                    }

                                    CheckboxNumericRow(
                                        label = "Temp. ambiente",
                                        checked = ambientTempChecked,
                                        onCheckedChange = { ambientTempChecked = it },
                                        value = ambientTemp,
                                        onValueChange = { ambientTemp = it },
                                        unit = "°C"
                                    )

                                    CheckboxDropdownRow(
                                        label = "Tipo ambiente",
                                        checked = environmentChecked,
                                        onCheckedChange = { environmentChecked = it },
                                        options = environmentOptions,
                                        selectedId = environmentId,
                                        onSelected = { environmentId = it }
                                    )
                                }

                                VerticalDivider()

                                // Col 2 (50%)
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    CheckboxNumericRow(
                                        label = "Velocidad del viento",
                                        checked = windSpeedChecked,
                                        onCheckedChange = { windSpeedChecked = it },
                                        value = windSpeed,
                                        onValueChange = { windSpeed = it },
                                        unit = "m/s"
                                    )

                                    DropdownSelector(
                                        label = "Fabricante",
                                        options = manufacturerOptions,
                                        selectedId = manufacturerId,
                                        onSelected = { manufacturerId = it }
                                    )

                                    Divider()

                                    Text("Especificación eléctrica", style = MaterialTheme.typography.labelMedium)

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

                            MultilineField(
                                label = "Comentarios",
                                value = comments,
                                onValueChange = {
                                    commentsTouched = true
                                    comments = it
                                },
                                modifier = Modifier.fillMaxWidth(),
                                fieldHeight = 48.dp
                            )
                        }
                    }

                    else -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Cargas de imágenes", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                                    modifier = Modifier.weight(1f),
                                    isError = thermalError
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
                                    modifier = Modifier.weight(1f),
                                    isError = digitalError
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
                ){
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    val canSubmit =
                        continueEnabled && emissivityError == null && requiredFieldsFilled && imagesProvided
                    Button(
                        onClick = {
                            onContinue(
                                ElectricProblemFormData(
                                    failureId = failureId?.takeIf { it.isNotBlank() },
                                    componentTemperature = componentTemperature,
                                    componentPhaseId = componentPhaseId,
                                    componentRms = componentRms,
                                    referenceTemperature = referenceTemperature,
                                    referencePhaseId = referencePhaseId,
                                    referenceRms = referenceRms,
                                    additionalInfoId = additionalInfoId,
                                    additionalRms = additionalRms,
                                    emissivityChecked = emissivityChecked,
                                    emissivity = emissivity,
                                    indirectTempChecked = indirectTempChecked,
                                    ambientTempChecked = ambientTempChecked,
                                    ambientTemp = ambientTemp,
                                    environmentChecked = environmentChecked,
                                    environmentId = environmentId,
                                    windSpeedChecked = windSpeedChecked,
                                    windSpeed = windSpeed,
                                    manufacturerId = manufacturerId,
                                    ratedLoad = ratedLoad,
                                    circuitVoltage = circuitVoltage,
                                    comments = comments,
                                    rpm = ""
                                )
                            )
                        },
                        enabled = canSubmit
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
}

data class ElectricProblemFormData(
    val failureId: String? = null,
    val componentTemperature: String = "",
    val componentPhaseId: String? = null,
    val componentRms: String = "",
    val referenceTemperature: String = "",
    val referencePhaseId: String? = null,
    val referenceRms: String = "",
    val additionalInfoId: String? = null,
    val additionalRms: String = "",
    val emissivityChecked: Boolean = false,
    val emissivity: String = "",
    val indirectTempChecked: Boolean = false,
    val ambientTempChecked: Boolean = false,
    val ambientTemp: String = "",
    val environmentChecked: Boolean = false,
    val environmentId: String? = null,
    val windSpeedChecked: Boolean = false,
    val windSpeed: String = "",
    val manufacturerId: String? = null,
    val ratedLoad: String = "",
    val circuitVoltage: String = "",
    val comments: String = "",
    val rpm: String = ""
)

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    )
}

/* ------------------------- Campos base (homogéneos) ------------------------- */

@Composable
private fun OutlinedFieldBox(
    modifier: Modifier = Modifier,
    height: Dp = FIELD_HEIGHT,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(FIELD_RADIUS))
            .padding(FIELD_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun InfoField(label: String, value: String, ancho: Dp) {
    Column(Modifier.widthIn(min = INFO_FIELD_MIN_WIDTH, max = ancho)) {
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
private fun ReadOnlyFormField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    ancho: Dp? = null
) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )

        Box(
            modifier = Modifier
                .then(
                    if (ancho != null) {
                        Modifier.width(ancho)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
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
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String? = null
) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        OutlinedFieldBox {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            if (unit != null) {
                Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ValueFieldNoLabel(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String? = null,
    ancho: Dp? = null,              // ✅ ancho opcional (si viene null usa fillMaxWidth)
    modifier: Modifier = Modifier   // ✅ por si también quieres pasar weight/align desde afuera
) {
    val widthModifier = if (ancho != null) Modifier.width(ancho) else Modifier.fillMaxWidth()

    OutlinedFieldBox(
        modifier = modifier.then(widthModifier) // ✅ aquí aplica el ancho personalizado
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f)
        )
        if (unit != null) {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ------------------------- Dropdowns ------------------------- */

@Composable
private fun DropdownSelectorNoLabel(
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    ancho: Dp? = null,              // ✅ ancho personalizado opcional
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    val widthModifier = if (ancho != null) {
        Modifier.width(ancho)
    } else {
        Modifier.fillMaxWidth()
    }

    Box(
        modifier = modifier
            .then(widthModifier)   // ✅ aplica el ancho definido
            .height(FIELD_HEIGHT)
            .border(
                FIELD_BORDER,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(FIELD_RADIUS)
            )
            .padding(FIELD_PADDING)
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
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        DropdownSelectorNoLabel(
            options = options,
            selectedId = selectedId,
            onSelected = onSelected
        )
    }
}

/* ------------------------- Checkbox + campo (compacto) ------------------------- */

@Composable
private fun CheckboxNumericRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CHECKBOX_GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .wrapContentWidth()
                .padding(0.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)

            OutlinedFieldBox {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                if (unit != null) {
                    Text(unit, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun CheckboxDropdownRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selectedId }?.second.orEmpty()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CHECKBOX_GAP),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier
                .wrapContentWidth()
                .padding(0.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FIELD_HEIGHT)
                    .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(FIELD_RADIUS))
                    .padding(FIELD_PADDING)
                    .clickable { expanded = true },
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
                    Text("▼", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (id, text) ->
                    DropdownMenuItem(
                        text = { Text(text = text.ifBlank { id }, style = MaterialTheme.typography.bodySmall) },
                        onClick = {
                            expanded = false
                            onSelected(id)
                        }
                    )
                }
            }
        }
    }
}

/* ------------------------- Multiline ------------------------- */

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
        //Spacer(Modifier.height(4.dp))

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

/* ------------------------- Imágenes ------------------------- */

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
                .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
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
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (title.isNotBlank()) Text(title, style = MaterialTheme.typography.labelMedium)

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
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        ImagePreviewBox(fileName = value)
    }
}

@Composable
private fun RowScope.CenterCell(
    weight: Float,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier.weight(weight),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
