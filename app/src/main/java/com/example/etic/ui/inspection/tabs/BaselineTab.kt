package com.example.etic.ui.inspection.tabs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DialogProperties
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.etic.data.local.DbProvider
import com.example.etic.data.repository.InspectionUiRepository
import com.example.etic.features.inspection.BaselineTable
import com.example.etic.features.inspection.LocalCurrentInspection
import com.example.etic.features.inspection.LocalCurrentUser
import com.example.etic.features.inspection.STATUS_POR_VERIFICAR
import com.example.etic.features.inspection.tree.Baseline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun BaselineTableFromDatabase(
    selectedId: String?,
    refreshTick: Int,
    onBaselineChanged: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val currentInspection = LocalCurrentInspection.current
    val currentUser = LocalCurrentUser.current
    val scope = rememberCoroutineScope()

    val repo = remember {
        val db = DbProvider.get(ctx)
        InspectionUiRepository(
            problemaDao = db.problemaDao(),
            ubicacionDao = db.ubicacionDao(),
            inspeccionDao = db.inspeccionDao(),
            inspeccionDetDao = db.inspeccionDetDao(),
            severidadDao = db.severidadDao(),
            equipoDao = db.equipoDao(),
            tipoInspeccionDao = db.tipoInspeccionDao(),
            lineaBaseDao = db.lineaBaseDao()
        )
    }

    val dao = remember { DbProvider.get(ctx).lineaBaseDao() }
    val ubicacionDao = remember { DbProvider.get(ctx).ubicacionDao() }
    val inspeccionDetDao = remember { DbProvider.get(ctx).inspeccionDetDao() }

    var baselinesCache by remember { mutableStateOf(emptyList<Baseline>()) }
    val uiBaselines by produceState(
        initialValue = baselinesCache,
        selectedId,
        currentInspection?.idInspeccion,
        refreshTick
    ) {
        value = repo.loadBaselinesForUi(
            currentInspectionId = currentInspection?.idInspeccion,
            selectedUbicacionId = selectedId
        )
        baselinesCache = value
    }

    var baselineToDelete by remember { mutableStateOf<Baseline?>(null) }
    data class BaselineDraft(
        val mta: String,
        val tempMax: String,
        val tempAmb: String,
        val notas: String,
        val imgIr: String,
        val imgId: String
    )
    var baselineToEdit by remember { mutableStateOf<Baseline?>(null) }
    var showBaselineDialog by remember { mutableStateOf(false) }
    var baselineNavList by remember { mutableStateOf<List<Baseline>>(emptyList()) }
    var baselineNavIndex by remember { mutableStateOf(-1) }
    var baselineDrafts by remember { mutableStateOf<Map<String, BaselineDraft>>(emptyMap()) }
    var isSavingBaseline by remember { mutableStateOf(false) }
    var baselineEditEntity by remember { mutableStateOf<com.example.etic.data.local.entities.LineaBase?>(null) }

    var mta by remember { mutableStateOf("") }
    var tempMax by remember { mutableStateOf("") }
    var tempAmb by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var imgIr by remember { mutableStateOf("") }
    var imgId by remember { mutableStateOf("") }
    var irPreview by remember { mutableStateOf<Bitmap?>(null) }
    var idPreview by remember { mutableStateOf<Bitmap?>(null) }

    fun resetBaselineFormState() {
        mta = ""
        tempMax = ""
        tempAmb = ""
        notas = ""
        imgIr = ""
        imgId = ""
        irPreview = null
        idPreview = null
    }

    fun applyBaselineDraft(baselineId: String) {
        val draft = baselineDrafts[baselineId] ?: return
        mta = draft.mta
        tempMax = draft.tempMax
        tempAmb = draft.tempAmb
        notas = draft.notas
        imgIr = draft.imgIr
        imgId = draft.imgId
        irPreview = null
        idPreview = null
    }

    fun buildBaselineDraft(): BaselineDraft =
        BaselineDraft(
            mta = mta,
            tempMax = tempMax,
            tempAmb = tempAmb,
            notas = notas,
            imgIr = imgIr,
            imgId = imgId
        )

    LaunchedEffect(showBaselineDialog, baselineToEdit?.id, refreshTick) {
        if (!showBaselineDialog) {
            baselineEditEntity = null
            resetBaselineFormState()
            baselineDrafts = emptyMap()
            baselineNavList = emptyList()
            baselineNavIndex = -1
            isSavingBaseline = false
            return@LaunchedEffect
        }
        val current = baselineToEdit ?: return@LaunchedEffect
        val refreshed = withContext(Dispatchers.IO) {
            runCatching { dao.getById(current.id) }.getOrNull()
        }
        baselineEditEntity = refreshed
        val draft = baselineDrafts[current.id]
        if (draft != null) {
            applyBaselineDraft(current.id)
            return@LaunchedEffect
        }
        if (refreshed != null) {
            mta = (refreshed.mta ?: current.mtaC).toString()
            tempMax = (refreshed.tempMax ?: current.tempC).toString()
            tempAmb = (refreshed.tempAmb ?: current.ambC).toString()
            notas = refreshed.notas ?: current.notas
            imgIr = refreshed.archivoIr ?: current.imgR.orEmpty()
            imgId = refreshed.archivoId ?: current.imgD.orEmpty()
            irPreview = null
            idPreview = null
        }
    }

    fun filter2Dec(input: String): String {
        if (input.isEmpty()) return ""
        val norm = input.replace(',', '.')
        val regex = Regex("^\\d*([.]\\d{0,2})?$")
        return if (regex.matches(norm)) norm else norm.let { s ->
            val idx = s.indexOf('.')
            if (idx >= 0 && s.length > idx + 3) s.substring(0, idx + 3) else s
        }
    }

    fun saveBitmapToImagenes(
        ctx: android.content.Context,
        bmp: Bitmap,
        prefix: String
    ): String? {
        return try {
            val dir = java.io.File(ctx.filesDir, "Imagenes").apply { mkdirs() }
            val name = "$prefix-" + System.currentTimeMillis().toString() + ".jpg"
            val file = java.io.File(dir, name)
            java.io.FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            name
        } catch (_: Exception) { null }
    }

    suspend fun persistBaselineDrafts(
        drafts: Map<String, BaselineDraft>,
        currentUserId: String?
    ): Boolean {
        if (drafts.isEmpty()) return true
        val nowTs = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        var failed = false
        drafts.forEach { (baselineId, draft) ->
            val base = runCatching { dao.getById(baselineId) }.getOrNull()
            if (base == null) {
                failed = true
                return@forEach
            }
            val updated = base.copy(
                mta = draft.mta.toDoubleOrNull(),
                tempMax = draft.tempMax.toDoubleOrNull(),
                tempAmb = draft.tempAmb.toDoubleOrNull(),
                notas = draft.notas.ifBlank { null },
                archivoIr = draft.imgIr.ifBlank { null },
                archivoId = draft.imgId.ifBlank { null },
                modificadoPor = currentUserId,
                fechaMod = nowTs
            )
            val updateResult = runCatching { dao.update(updated) }
            if (updateResult.isFailure) {
                failed = true
                return@forEach
            }
            val ubicacionId = base.idUbicacion
            val inspeccionId = base.idInspeccion
            if (!ubicacionId.isNullOrBlank() && !inspeccionId.isNullOrBlank()) {
                val detRow = runCatching {
                    inspeccionDetDao.getByUbicacion(ubicacionId)
                        .firstOrNull { it.idInspeccion == inspeccionId }
                }.getOrNull()
                if (detRow != null) {
                    val updatedDet = detRow.copy(
                        idStatusInspeccionDet = "568798D2-76BB-11D3-82BF-00104BC75DC2",
                        idEstatusColorText = 3,
                        modificadoPor = currentUserId,
                        fechaMod = nowTs
                    )
                    runCatching { inspeccionDetDao.update(updatedDet) }
                }
            }
        }
        return !failed
    }

    Box(modifier) {
        BaselineTable(
            baselines = uiBaselines,
            onDelete = { baseline -> baselineToDelete = baseline },
            onDoubleTap = { baseline, list ->
                baselineDrafts = emptyMap()
                baselineNavList = list
                baselineNavIndex = list.indexOfFirst { it.id == baseline.id }
                baselineToEdit = baseline
                showBaselineDialog = true
            }
        )

        if (baselineToDelete != null) {
            val baseline = baselineToDelete!!
            AlertDialog(
                onDismissRequest = { baselineToDelete = null },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            val nowTs = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            val ok = repo.deleteBaselineAndRevertStatus(
                                baselineId = baseline.id,
                                currentUserId = currentUser?.idUsuario,
                                nowTs = nowTs,
                                statusPorVerificarId = STATUS_POR_VERIFICAR
                            )
                            if (ok) {
                                baselinesCache = baselinesCache.filter { it.id != baseline.id }
                                baselineToDelete = null
                                onBaselineChanged()
                            }
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    Button(onClick = { baselineToDelete = null }) { Text("Cancelar") }
                },
                text = { Text("Eliminar baseline seleccionado?") }
            )
        }

        if (showBaselineDialog && baselineToEdit != null) {
            val navigationActive = baselineNavList.isNotEmpty()
            val canNavigatePrevious = baselineNavList.isNotEmpty() && baselineNavIndex > 0
            val canNavigateNext =
                baselineNavList.isNotEmpty() && baselineNavIndex >= 0 && baselineNavIndex < baselineNavList.lastIndex

            fun navigateBaseline(delta: Int) {
                if (isSavingBaseline) return
                if (mta.isBlank() || tempMax.isBlank() || tempAmb.isBlank() || imgIr.isBlank() || imgId.isBlank()) return
                val current = baselineToEdit ?: return
                baselineDrafts = baselineDrafts + (current.id to buildBaselineDraft())
                val nextIndex = baselineNavIndex + delta
                if (nextIndex !in baselineNavList.indices) return
                baselineNavIndex = nextIndex
                baselineToEdit = baselineNavList[nextIndex]
            }

            val isBaselineValid by remember(mta, tempMax, tempAmb, imgIr, imgId) {
                mutableStateOf(
                    mta.isNotBlank() &&
                        tempMax.isNotBlank() &&
                        tempAmb.isNotBlank() &&
                        imgIr.isNotBlank() &&
                        imgId.isNotBlank()
                )
            }

            val irCameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.TakePicturePreview()
            ) { bmp ->
                if (bmp != null) {
                    val name = saveBitmapToImagenes(ctx, bmp, "IR")
                    if (name != null) {
                        imgIr = name
                        irPreview = bmp
                    }
                }
            }
            val idCameraLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.TakePicturePreview()
            ) { bmp ->
                if (bmp != null) {
                    val name = saveBitmapToImagenes(ctx, bmp, "ID")
                    if (name != null) {
                        imgId = name
                        idPreview = bmp
                    }
                }
            }

            val ubId = baselineEditEntity?.idUbicacion
            val rutaEquipo by produceState(initialValue = "", ubId) {
                value = if (!ubId.isNullOrBlank()) {
                    runCatching { ubicacionDao.getById(ubId)?.ruta ?: "" }.getOrDefault("")
                } else ""
            }

            AlertDialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                ),
                confirmButton = {
                    Button(
                        enabled = isBaselineValid && !isSavingBaseline,
                        onClick = {
                            if (isSavingBaseline) return@Button
                            val currentId = baselineToEdit?.id ?: return@Button
                            scope.launch {
                                if (isSavingBaseline) return@launch
                                isSavingBaseline = true
                                try {
                                    val draftsToSave = baselineDrafts + (currentId to buildBaselineDraft())
                                    val ok = persistBaselineDrafts(draftsToSave, currentUser?.idUsuario)
                                    if (ok) {
                                        showBaselineDialog = false
                                        baselineToEdit = null
                                        baselineDrafts = emptyMap()
                                        baselineNavList = emptyList()
                                        baselineNavIndex = -1
                                        onBaselineChanged()
                                    }
                                } finally {
                                    isSavingBaseline = false
                                }
                            }
                        }
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            if (!isSavingBaseline) {
                                showBaselineDialog = false
                                baselineToEdit = null
                                baselineDrafts = emptyMap()
                                baselineNavList = emptyList()
                                baselineNavIndex = -1
                            }
                        },
                        enabled = !isSavingBaseline
                    ) { Text("Cancelar") }
                },
                text = {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Editar Baseline", style = MaterialTheme.typography.titleMedium)
                            if (navigationActive) {
                                Row {
                                    IconButton(
                                        onClick = { navigateBaseline(-1) },
                                        enabled = canNavigatePrevious
                                    ) {
                                        Icon(Icons.Outlined.ArrowLeft, contentDescription = "Anterior")
                                    }
                                    IconButton(
                                        onClick = { navigateBaseline(1) },
                                        enabled = canNavigateNext
                                    ) {
                                        Icon(Icons.Outlined.ArrowRight, contentDescription = "Siguiente")
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                value = mta,
                                onValueChange = { mta = filter2Dec(it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("MTA")
                                        Text(" *", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                            TextField(
                                value = tempMax,
                                onValueChange = { tempMax = filter2Dec(it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("MAX")
                                        Text(" *", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                            TextField(
                                value = tempAmb,
                                onValueChange = { tempAmb = filter2Dec(it) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f),
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("AMB")
                                        Text(" *", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = notas,
                            onValueChange = { notas = it },
                            label = { Text("Notas") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextField(
                                        value = imgIr,
                                        onValueChange = { imgIr = it },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("IR (archivo)")
                                                Text(" *", color = MaterialTheme.colorScheme.error)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { irCameraLauncher.launch(null) }) {
                                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                val bmp = irPreview ?: run {
                                    if (imgIr.isNotBlank()) {
                                        val f = java.io.File(ctx.filesDir, "Imagenes/$imgIr")
                                        if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                                    } else null
                                }
                                if (bmp != null) {
                                    Image(
                                        bmp.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                } else {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image,
                                            contentDescription = "Imagen no encontrada",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TextField(
                                        value = imgId,
                                        onValueChange = { imgId = it },
                                        label = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("ID (archivo)")
                                                Text(" *", color = MaterialTheme.colorScheme.error)
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    IconButton(onClick = { idCameraLauncher.launch(null) }) {
                                        Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                val bmp2 = idPreview ?: run {
                                    if (imgId.isNotBlank()) {
                                        val f = java.io.File(ctx.filesDir, "Imagenes/$imgId")
                                        if (f.exists()) BitmapFactory.decodeFile(f.absolutePath) else null
                                    } else null
                                }
                                if (bmp2 != null) {
                                    Image(
                                        bmp2.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                    )
                                } else {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image,
                                            contentDescription = "Imagen no encontrada",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = rutaEquipo,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Ruta del equipo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }
    }
}
