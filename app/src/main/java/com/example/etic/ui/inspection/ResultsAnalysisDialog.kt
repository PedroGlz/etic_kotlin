package com.example.etic.ui.inspection

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt

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
    val context = LocalContext.current
    val offset = remember { mutableStateOf(Offset.Zero) }
    var fechaInicio by rememberSaveable { mutableStateOf(initialDraft.fechaInicio) }
    var fechaFin by rememberSaveable { mutableStateOf(initialDraft.fechaFin) }
    var nombreImgPortada by rememberSaveable { mutableStateOf(initialDraft.nombreImgPortada) }
    var detalleUbicacion by rememberSaveable { mutableStateOf(initialDraft.detalleUbicacion) }
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
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

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
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    ProblemDialogDraggableHeader(
                        title = "Resultados de analisis",
                        onDrag = { drag -> offset.value += drag }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        SectionCard("Portada") {
                            ContactosEditor(contactos = contactos)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                            OutlinedTextField(
                                value = detalleUbicacion,
                                onValueChange = { detalleUbicacion = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Detalle ubicacion") },
                                minLines = 2
                            )
                        }

                        SectionCard("Descripcion") {
                            EditableStringList(
                                title = "Descripciones",
                                values = descripciones,
                                minRows = 1
                            )
                            EditableStringList(
                                title = "Areas inspeccionadas",
                                values = areas,
                                minRows = 1
                            )
                        }

                        SectionCard("Recomendaciones") {
                            recomendaciones.forEachIndexed { index, rec ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Recomendacion ${index + 1}",
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
                                        OutlinedTextField(
                                            value = rec.texto,
                                            onValueChange = { recomendaciones[index] = rec.copy(texto = it) },
                                            modifier = Modifier.fillMaxWidth(),
                                            label = { Text("Texto") },
                                            minLines = 2
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
                                Text("Agregar recomendacion")
                            }
                        }

                        SectionCard("Referencias") {
                            EditableStringList(
                                title = "Referencias",
                                values = referencias,
                                minRows = 1
                            )
                        }

                        SectionCard("Inventario") {
                            SelectionList(
                                title = "Elementos para inventario",
                                selectedIds = selectedLocationIds,
                                options = locationOptions.map { it.id to it.title },
                                enabled = !isBusy
                            )
                        }

                        SectionCard("Problemas") {
                            SelectionList(
                                title = "Problemas para reporte",
                                selectedIds = selectedProblemIds,
                                options = problemOptions.map { it.id to it.label },
                                enabled = !isBusy
                            )
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
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            enabled = !isBusy,
                            onClick = { onDismiss(buildDraft()) }
                        ) {
                            Text("Cancelar")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            enabled = !isBusy,
                            onClick = {
                                val draft = buildDraft()
                                validationError = when {
                                    draft.fechaInicio.isBlank() || draft.fechaFin.isBlank() -> "Selecciona las fechas del reporte."
                                    draft.nombreImgPortada.isBlank() -> "Selecciona una imagen de portada."
                                    draft.areasInspeccionadas.isEmpty() -> "Agrega al menos un area inspeccionada."
                                    draft.recomendaciones.isEmpty() -> "Agrega al menos una recomendacion."
                                    draft.referencias.isEmpty() -> "Agrega al menos una referencia."
                                    draft.selectedInventoryIds.isEmpty() -> "Selecciona al menos una ubicacion para inventario."
                                    draft.selectedProblemIds.isEmpty() -> "Selecciona al menos un problema."
                                    else -> null
                                }
                                if (validationError == null) {
                                    onConfirm(draft, draft.selectedInventoryIds)
                                }
                            }
                        ) {
                            Text("Generar")
                        }
                    }
                }
            }
        }
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleMedium)
                content()
            }
        )
    }
}

@Composable
private fun ContactosEditor(contactos: androidx.compose.runtime.snapshots.SnapshotStateList<ResultadosAnalisisContacto>) {
    Text("Contactos", style = MaterialTheme.typography.titleSmall)
    contactos.forEachIndexed { index, contacto ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = contacto.nombre,
                onValueChange = { contactos[index] = contacto.copy(nombre = it) },
                modifier = Modifier.weight(1f),
                label = { Text("Nombre ${index + 1}") }
            )
            OutlinedTextField(
                value = contacto.puesto,
                onValueChange = { contactos[index] = contacto.copy(puesto = it) },
                modifier = Modifier.weight(1f),
                label = { Text("Puesto ${index + 1}") }
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
    OutlinedTextField(
        value = value,
        onValueChange = {},
        modifier = modifier.clickable(onClick = onClick),
        label = { Text(label) },
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
            OutlinedTextField(
                value = value,
                onValueChange = { values[index] = it },
                modifier = Modifier.weight(1f),
                minLines = 2,
                label = { Text("${title.dropLastWhile { it == 's' }} ${index + 1}") }
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
                    .padding(vertical = 4.dp),
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
