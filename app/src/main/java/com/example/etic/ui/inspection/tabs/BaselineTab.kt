package com.example.etic.ui.inspection.tabs

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowLeft
import androidx.compose.material.icons.outlined.ArrowRight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.etic.features.components.ImageInputButtonGroup
import com.example.etic.data.local.DbProvider
import com.example.etic.data.repository.InspectionUiRepository
import com.example.etic.core.current.LocalCurrentInspection
import com.example.etic.core.current.LocalCurrentUser
import com.example.etic.features.inspection.ui.home.BaselineTable
import com.example.etic.features.inspection.ui.home.STATUS_POR_VERIFICAR
import com.example.etic.features.inspection.tree.Baseline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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
            val dir = File(ctx.filesDir, "Imagenes").apply { mkdirs() }
            val name = "$prefix-" + System.currentTimeMillis().toString() + ".jpg"
            val file = File(dir, name)
            FileOutputStream(file).use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
            }
            name
        } catch (_: Exception) { null }
    }

    fun copyImageFromUri(
        ctx: android.content.Context,
        uri: Uri,
        prefix: String
    ): String? {
        return try {
            val dir = File(ctx.filesDir, "Imagenes").apply { mkdirs() }
            val name = "$prefix-" + System.currentTimeMillis().toString() + ".jpg"
            val file = File(dir, name)
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output, 8 * 1024)
                }
            } ?: return null
            name
        } catch (_: Exception) {
            null
        }
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
            Dialog(
                onDismissRequest = { baselineToDelete = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnClickOutside = true,
                    dismissOnBackPress = true
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.92f),
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Eliminar baseline seleccionado?")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { baselineToDelete = null }) { Text("Cancelar") }
                            Spacer(Modifier.width(8.dp))
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
                        }
                    }
                }
            }
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
            val irFolderLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    val name = copyImageFromUri(ctx, uri, "IR")
                    if (name != null) {
                        imgIr = name
                        irPreview = null
                    }
                }
            }
            val idFolderLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    val name = copyImageFromUri(ctx, uri, "ID")
                    if (name != null) {
                        imgId = name
                        idPreview = null
                    }
                }
            }

            val ubId = baselineEditEntity?.idUbicacion
            val rutaEquipo by produceState(initialValue = "", ubId) {
                value = if (!ubId.isNullOrBlank()) {
                    runCatching { ubicacionDao.getById(ubId)?.ruta ?: "" }.getOrDefault("")
                } else ""
            }

            Dialog(
                onDismissRequest = { },
                properties = DialogProperties(
                    usePlatformDefaultWidth = true,
                    dismissOnClickOutside = false,
                    dismissOnBackPress = false
                )
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
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
                            BaselineInputField(
                                label = "MTA",
                                required = true,
                                value = mta,
                                onValueChange = { mta = filter2Dec(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            BaselineInputField(
                                label = "MAX",
                                required = true,
                                value = tempMax,
                                onValueChange = { tempMax = filter2Dec(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            BaselineInputField(
                                label = "AMB",
                                required = true,
                                value = tempAmb,
                                onValueChange = { tempAmb = filter2Dec(it) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        BaselineInputField(
                            label = "Notas",
                            value = notas,
                            onValueChange = { notas = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            fieldHeight = 56.dp
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(Modifier.weight(1f)) {
                                ImageInputButtonGroup(
                                    label = "Archivo IR",
                                    value = imgIr,
                                    onValueChange = { imgIr = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isRequired = true,
                                    enabled = true,
                                    onMoveUp = { imgIr = adjustImageSequence(imgIr, +1, "IR") },
                                    onMoveDown = { imgIr = adjustImageSequence(imgIr, -1, "IR") },
                                    onDotsClick = { imgIr = nextImageName(imgIr, "IR") },
                                    onFolderClick = { irFolderLauncher.launch("image/*") },
                                    onCameraClick = { irCameraLauncher.launch(null) }
                                )
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
                                ImageInputButtonGroup(
                                    label = "Archivo ID",
                                    value = imgId,
                                    onValueChange = { imgId = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isRequired = true,
                                    enabled = true,
                                    onMoveUp = { imgId = adjustImageSequence(imgId, +1, "ID") },
                                    onMoveDown = { imgId = adjustImageSequence(imgId, -1, "ID") },
                                    onDotsClick = { imgId = nextImageName(imgId, "ID") },
                                    onFolderClick = { idFolderLauncher.launch("image/*") },
                                    onCameraClick = { idCameraLauncher.launch(null) }
                                )
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
                        BaselineInputField(
                            label = "Ruta del equipo",
                            value = rutaEquipo,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
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
                        }
                    }
                }
            }
        }
    }
}

private fun nextImageName(current: String, prefix: String): String {
    return if (current.isBlank()) "$prefix-001.jpg" else adjustImageSequence(current, +1, prefix)
}

private fun adjustImageSequence(current: String, delta: Int, fallbackPrefix: String): String {
    val rx = Regex("""^(.*?)(\d+)(\.[^.]+)?$""")
    val match = rx.matchEntire(current.trim())
    if (match == null) {
        return if (current.isBlank()) "$fallbackPrefix-001.jpg" else current
    }
    val prefix = match.groupValues[1]
    val numberRaw = match.groupValues[2]
    val suffix = match.groupValues[3].ifBlank { ".jpg" }
    val digits = numberRaw.length
    val number = numberRaw.toIntOrNull() ?: return current
    val next = (number + delta).coerceAtLeast(1)
    return prefix + next.toString().padStart(digits, '0') + suffix
}

@Composable
private fun BaselineInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    fieldHeight: androidx.compose.ui.unit.Dp = 30.dp,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            if (required) {
                Text(" *", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(fieldHeight)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (readOnly) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
