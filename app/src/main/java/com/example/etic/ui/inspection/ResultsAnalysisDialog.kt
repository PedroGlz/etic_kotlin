package com.example.etic.ui.inspection

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.features.inspection.tree.TreeNode
import com.example.etic.features.inspection.ui.problem.ProblemDialogDraggableHeader
import com.example.etic.reports.ResultadosAnalisisContacto
import com.example.etic.reports.ResultadosAnalisisDraft
import com.example.etic.reports.ResultadosAnalisisProblemOption
import com.example.etic.reports.ResultadosAnalisisRecomendacion
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.roundToInt

private data class WizardStep(
    val title: String,
    val subtitle: String
)

private val FIELD_PADDING = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
private val INPUT_FIELD_RADIUS = 4.dp
private val FIELD_HEIGHT = 25.dp
private val FIELD_BORDER = 1.dp

@Composable
fun ResultsAnalysisDialog(
    initialDraft: ResultadosAnalisisDraft,
    locationOptions: List<TreeNode>,
    problemOptions: List<ResultadosAnalisisProblemOption>,
    availableImages: List<String>,
    isBusy: Boolean,
    onDismiss: (ResultadosAnalisisDraft) -> Unit,
    onConfirm: (ResultadosAnalisisDraft, List<String>) -> Unit
) {
    val steps = listOf(
        WizardStep("Portada", "Información principal"),
        WizardStep("Descripción", "Objetivos y alcance"),
        WizardStep("Recomendaciones", "Hallazgos y seguimiento"),
        WizardStep("Referencias", "Normas y observaciones"),
        WizardStep("Inventario", "Seleccionar equipos inspeccionados"),
        WizardStep("Problemas", "Seleccionar observaciones"),
        WizardStep("Resumen", "Revisión final antes de generar")
    )

    val context = LocalContext.current
    val offset = remember { mutableStateOf(Offset.Zero) }
    var fechaInicio by remember { mutableStateOf(initialDraft.fechaInicio) }
    var fechaFin by remember { mutableStateOf(initialDraft.fechaFin) }
    var nombreImgPortada by remember { mutableStateOf(initialDraft.nombreImgPortada) }
    var detalleUbicacion by remember { mutableStateOf(initialDraft.detalleUbicacion) }
    val contactos = remember {
        mutableStateListOf<ResultadosAnalisisContacto>().apply {
            addAll((initialDraft.contactos + List(4) { ResultadosAnalisisContacto() }).take(4))
        }
    }
    val descripciones = remember {
        mutableStateListOf<String>().apply {
            addAll(initialDraft.descripciones.ifEmpty { listOf("") })
        }
    }
    val areas = remember {
        mutableStateListOf<String>().apply {
            addAll(initialDraft.areasInspeccionadas.ifEmpty { listOf("") })
        }
    }
    val referencias = remember {
        mutableStateListOf<String>().apply {
            addAll(initialDraft.referencias.ifEmpty { listOf("") })
        }
    }
    val recomendaciones = remember {
        mutableStateListOf<ResultadosAnalisisRecomendacion>().apply {
            addAll(initialDraft.recomendaciones.ifEmpty { listOf(ResultadosAnalisisRecomendacion()) })
        }
    }
    val selectedLocationIds = remember {
        mutableStateListOf<String>().apply { addAll(initialDraft.selectedInventoryIds) }
    }
    val selectedProblemIds = remember {
        mutableStateListOf<String>().apply { addAll(initialDraft.selectedProblemIds) }
    }

    var validationError by remember { mutableStateOf<String?>(null) }
    var currentStep by remember { mutableStateOf(0) }
    var maxReachedStep by remember { mutableStateOf(0) }

    fun buildDraft(): ResultadosAnalisisDraft {
        return ResultadosAnalisisDraft(
            inspectionId = initialDraft.inspectionId,
            siteId = initialDraft.siteId,
            detalleUbicacion = detalleUbicacion.trim(),
            contactos = contactos.toList(),
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            nombreImgPortada = nombreImgPortada.trim(),
            descripciones = descripciones.map { it.trim() }.filter { it.isNotEmpty() },
            areasInspeccionadas = areas.map { it.trim() }.filter { it.isNotEmpty() },
            recomendaciones = recomendaciones.map {
                ResultadosAnalisisRecomendacion(
                    texto = it.texto.trim(),
                    imagen1 = it.imagen1.trim(),
                    imagen2 = it.imagen2.trim()
                )
            }.filter { it.texto.isNotEmpty() || it.imagen1.isNotEmpty() || it.imagen2.isNotEmpty() },
            referencias = referencias.map { it.trim() }.filter { it.isNotEmpty() },
            selectedInventoryIds = selectedLocationIds.toList(),
            selectedProblemIds = selectedProblemIds.toList()
        )
    }

    fun validateStep(step: Int, draft: ResultadosAnalisisDraft): String? {
        return when (step) {
            0, 1, 2, 3, 4, 5, 6 -> null
            else -> null
        }
    }

    fun goToStep(step: Int) {
        if (step <= maxReachedStep + 1) {
            currentStep = step
            validationError = null
        }
    }

    fun nextOrConfirm() {
        val draft = buildDraft()
        val error = validateStep(currentStep, draft)
        validationError = error
        if (error != null) {
            return
        }

        if (currentStep == steps.lastIndex) {
            onConfirm(draft, draft.selectedInventoryIds)
            return
        }

        currentStep += 1
        maxReachedStep = max(maxReachedStep, currentStep)
        validationError = null
    }

    fun nextImage(current: String, forward: Boolean): String {
        if (availableImages.isEmpty()) return current
        val currentIndex = availableImages.indexOfFirst { it.equals(current, true) }
        return when {
            currentIndex == -1 -> if (forward) availableImages.first() else availableImages.last()
            forward -> availableImages[(currentIndex + 1) % availableImages.size]
            else -> availableImages[(currentIndex - 1 + availableImages.size) % availableImages.size]
        }
    }

    fun openDatePicker(currentValue: String, onSelected: (String) -> Unit) {
        val base = runCatching { LocalDate.parse(currentValue) }.getOrElse { LocalDate.now() }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onSelected("%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
            },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth
        ).show()
    }

    Dialog(
        onDismissRequest = { if (!isBusy) onDismiss(buildDraft()) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x66000000))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.9f)
                    .align(Alignment.Center)
                    .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProblemDialogDraggableHeader(
                        title = "Resultados de análisis",
                        onDrag = { drag -> offset.value += drag }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surface
                                    )
                                )
                            )
                    ) {
                        StepNavigationBar(
                            steps = steps,
                            currentStep = currentStep,
                            maxReachedStep = maxReachedStep,
                            onStepSelected = ::goToStep
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(5.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                when (currentStep) {
                                    0 -> {
                                        Text("Paso 1: Portada", style = MaterialTheme.typography.titleMedium)
                                        ContactosEditor(contactos = contactos)
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            DateField(
                                                modifier = Modifier.weight(1f),
                                                label = "Fecha de inicio",
                                                value = fechaInicio,
                                                onClick = { openDatePicker(fechaInicio) { fechaInicio = it } }
                                            )
                                            DateField(
                                                modifier = Modifier.weight(1f),
                                                label = "Fecha final",
                                                value = fechaFin,
                                                onClick = { openDatePicker(fechaFin) { fechaFin = it } }
                                            )
                                        }
                                        ImageInputButtonGroup(
                                            label = "Portada",
                                            value = nombreImgPortada,
                                            onValueChange = { nombreImgPortada = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            isRequired = true,
                                            onMoveUp = { nombreImgPortada = nextImage(nombreImgPortada, true) },
                                            onMoveDown = { nombreImgPortada = nextImage(nombreImgPortada, false) },
                                            onDotsClick = {
                                                if (availableImages.isNotEmpty()) {
                                                    nombreImgPortada = availableImages.last()
                                                }
                                            }
                                        )
                                        MultilineField(
                                            value = detalleUbicacion,
                                            onValueChange = { detalleUbicacion = it },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = "Detalle ubicación"
                                        )
                                    }
                                    1 -> {
                                        Text("Paso 2: Descripción", style = MaterialTheme.typography.titleMedium)
                                        EditableStringList(
                                            title = "Descripciones",
                                            values = descripciones,
                                            minRows = 1
                                        )
                                        EditableStringList(
                                            title = "Áreas inspeccionadas",
                                            values = areas,
                                            minRows = 1
                                        )
                                    }
                                    2 -> {
                                        Text("Paso 3: Recomendaciones", style = MaterialTheme.typography.titleMedium)
                                        recomendaciones.forEachIndexed { index, rec ->
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(5.dp),
                                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            "Recomendación ${index + 1}",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        IconButton(
                                                            enabled = recomendaciones.size > 1 && !isBusy,
                                                            onClick = { recomendaciones.removeAt(index) }
                                                        ) {
                                                            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
                                                        }
                                                    }
                                                    MultilineField(
                                                        value = rec.texto,
                                                        onValueChange = { recomendaciones[index] = rec.copy(texto = it) },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        label = "Texto"
                                                    )
                                                    ImageInputButtonGroup(
                                                        label = "Imagen 1",
                                                        value = rec.imagen1,
                                                        onValueChange = {
                                                            recomendaciones[index] = recomendaciones[index].copy(imagen1 = it)
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onMoveUp = {
                                                            recomendaciones[index] = recomendaciones[index].copy(
                                                                imagen1 = nextImage(recomendaciones[index].imagen1, true)
                                                            )
                                                        },
                                                        onMoveDown = {
                                                            recomendaciones[index] = recomendaciones[index].copy(
                                                                imagen1 = nextImage(recomendaciones[index].imagen1, false)
                                                            )
                                                        },
                                                        onDotsClick = {
                                                            if (availableImages.isNotEmpty()) {
                                                                recomendaciones[index] = recomendaciones[index].copy(
                                                                    imagen1 = availableImages.last()
                                                                )
                                                            }
                                                        }
                                                    )
                                                    ImageInputButtonGroup(
                                                        label = "Imagen 2",
                                                        value = rec.imagen2,
                                                        onValueChange = {
                                                            recomendaciones[index] = recomendaciones[index].copy(imagen2 = it)
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        onMoveUp = {
                                                            recomendaciones[index] = recomendaciones[index].copy(
                                                                imagen2 = nextImage(recomendaciones[index].imagen2, true)
                                                            )
                                                        },
                                                        onMoveDown = {
                                                            recomendaciones[index] = recomendaciones[index].copy(
                                                                imagen2 = nextImage(recomendaciones[index].imagen2, false)
                                                            )
                                                        },
                                                        onDotsClick = {
                                                            if (availableImages.isNotEmpty()) {
                                                                recomendaciones[index] = recomendaciones[index].copy(
                                                                    imagen2 = availableImages.last()
                                                                )
                                                            }
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        TextButton(
                                            enabled = !isBusy,
                                            onClick = { recomendaciones.add(ResultadosAnalisisRecomendacion()) }
                                        ) {
                                            Icon(Icons.Outlined.Add, contentDescription = null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Agregar recomendación")
                                        }
                                    }
                                    3 -> {
                                        Text("Paso 4: Referencias", style = MaterialTheme.typography.titleMedium)
                                        EditableStringList(
                                            title = "Referencias",
                                            values = referencias,
                                            minRows = 1
                                        )
                                    }
                                    4 -> {
                                        Text("Paso 5: Inventario", style = MaterialTheme.typography.titleMedium)
                                        SelectionList(
                                            title = "Elementos para inventario",
                                            selectedIds = selectedLocationIds,
                                            options = locationOptions.map { it.id to it.title },
                                            enabled = !isBusy
                                        )
                                    }
                                    5 -> {
                                        Text("Paso 6: Problemas", style = MaterialTheme.typography.titleMedium)
                                        SelectionList(
                                            title = "Problemas para reporte",
                                            selectedIds = selectedProblemIds,
                                            options = problemOptions.map { it.id to it.label },
                                            enabled = !isBusy
                                        )
                                    }
                                    6 -> {
                                        val draft = buildDraft()
                                        val locationLookup = locationOptions.associate { it.id to it.title }
                                        val problemLookup = problemOptions.associate { it.id to it.label }
                                        Text("Paso 7: Resumen", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "Revisa esta sección y usa Atrás para corregir cualquier sección.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        SectionCard("Portada") {
                                            SummaryRow("Fechas", "${draft.fechaInicio} / ${draft.fechaFin}")
                                            SummaryRow("Portada", draft.nombreImgPortada.ifBlank { "Sin imagen" })
                                            SummaryRow("Detalle de ubicación", draft.detalleUbicacion.ifBlank { "Sin detalle" })
                                        }
                                        SectionCard("Descripción") {
                                            SummaryList("Descripciones", draft.descripciones, "Sin descripciones")
                                            SummaryList("Áreas inspeccionadas", draft.areasInspeccionadas, "Sin áreas")
                                        }
                                        SectionCard("Recomendaciones") {
                                            if (draft.recomendaciones.isEmpty()) {
                                                Text("Sin recomendaciones", style = MaterialTheme.typography.bodySmall)
                                            } else {
                                                draft.recomendaciones.forEachIndexed { index, recomendacion ->
                                                    Text("${index + 1}. ${recomendacion.texto.ifBlank { "Sin texto" }}")
                                                }
                                            }
                                        }
                                        SectionCard("Referencias") {
                                            SummaryList("Referencias", draft.referencias, "Sin referencias")
                                        }
                                        SectionCard("Selecciones") {
                                            SummaryList(
                                                "Inventario",
                                                draft.selectedInventoryIds.map { locationLookup[it].orEmpty().ifBlank { it } },
                                                "Sin inventario"
                                            )
                                            SummaryList(
                                                "Problemas",
                                                draft.selectedProblemIds.map { problemLookup[it].orEmpty().ifBlank { it } },
                                                "Sin problemas"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        validationError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            enabled = !isBusy,
                            onClick = { onDismiss(buildDraft()) }
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(8.dp))
                        if (currentStep > 0) {
                            TextButton(
                                enabled = !isBusy,
                                onClick = { currentStep -= 1 }
                            ) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Anterior")
                            }
                            Spacer(Modifier.width(8.dp))
                        }
                        Button(
                            enabled = !isBusy,
                            onClick = { nextOrConfirm() }
                        ) {
                            if (currentStep == steps.lastIndex) {
                                Text("Generar")
                            } else {
                                Text("Siguiente")
                                Spacer(Modifier.width(6.dp))
                                Icon(Icons.Outlined.ArrowForward, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepNavigationBar(
    steps: List<WizardStep>,
    currentStep: Int,
    maxReachedStep: Int,
    onStepSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, _ ->
                val isActive = index == currentStep
                val isVisited = index <= maxReachedStep
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(if (isVisited) MaterialTheme.colorScheme.primary else Color.LightGray)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = when {
                                isActive -> MaterialTheme.colorScheme.primary
                                isVisited -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                else -> Color.LightGray
                            },
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(enabled = index <= maxReachedStep + 1) { onStepSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = if (isActive || isVisited) Color.White else Color.DarkGray,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = steps[currentStep].title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = steps[currentStep].subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleSmall)
                content()
            }
        )
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SummaryList(label: String, items: List<String>, fallback: String) {
    Text(label, style = MaterialTheme.typography.titleSmall)
    if (items.isEmpty()) {
        Text(fallback, style = MaterialTheme.typography.bodySmall)
    } else {
        items.forEachIndexed { index, value ->
            Text("${index + 1}. $value", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ContactosEditor(contactos: androidx.compose.runtime.snapshots.SnapshotStateList<ResultadosAnalisisContacto>) {
    Text("Contactos", style = MaterialTheme.typography.titleSmall)
    contactos.forEachIndexed { index, contacto ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            InfoField(
                value = contacto.nombre,
                onValueChange = { contactos[index] = contacto.copy(nombre = it) },
                modifier = Modifier.weight(1f),
                label = "Nombre ${index + 1}"
            )
            InfoField(
                value = contacto.puesto,
                onValueChange = { contactos[index] = contacto.copy(puesto = it) },
                modifier = Modifier.weight(1f),
                label = "Puesto ${index + 1}"
            )
        }
    }
}

@Composable
private fun DateField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    InfoField(
        value = value,
        onValueChange = {},
        modifier = modifier.clickable(onClick = onClick),
        label = label,
        readOnly = true,
        enabled = true
    )
}

@Composable
private fun EditableStringList(
    title: String,
    values: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    minRows: Int
) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    values.forEachIndexed { index, value ->
        Row(verticalAlignment = Alignment.Top) {
            MultilineField(
                value = value,
                onValueChange = { values[index] = it },
                modifier = Modifier.weight(1f),
                label = "${title.dropLastWhile { it == 's' }} ${index + 1}",
                minLines = 2,
                maxLines = 8
            )
            IconButton(
                enabled = values.size > minRows,
                onClick = { if (values.size > minRows) values.removeAt(index) }
            ) {
                Icon(Icons.Outlined.Delete, contentDescription = "Eliminar")
            }
        }
    }
    TextButton(onClick = { values.add("") }) {
        Icon(Icons.Outlined.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("Agregar")
    }
}

@Composable
private fun SelectionList(
    title: String,
    selectedIds: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    options: List<Pair<String, String>>,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Row {
            TextButton(
                enabled = enabled,
                onClick = {
                    selectedIds.clear()
                    selectedIds.addAll(options.map { it.first })
                }
            ) { Text("Todo") }
            TextButton(
                enabled = enabled,
                onClick = { selectedIds.clear() }
            ) { Text("Limpiar") }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        options.forEach { (id, label) ->
            val checked = id in selectedIds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        if (checked) selectedIds.remove(id) else selectedIds.add(id)
                    }
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checked,
                    onCheckedChange = {
                        if (it) {
                            if (id !in selectedIds) selectedIds.add(id)
                        } else {
                            selectedIds.remove(id)
                        }
                    },
                    enabled = enabled,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(label, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

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
            .border(FIELD_BORDER, MaterialTheme.colorScheme.outline, RoundedCornerShape(INPUT_FIELD_RADIUS))
            .padding(FIELD_PADDING),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = content
    )
}

@Composable
private fun InfoField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    readOnly: Boolean = false,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        OutlinedFieldBox(modifier = Modifier) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = enabled,
                readOnly = readOnly,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MultilineField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    minLines: Int = 2,
    maxLines: Int = 6
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        OutlinedFieldBox(
            height = 48.dp
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

