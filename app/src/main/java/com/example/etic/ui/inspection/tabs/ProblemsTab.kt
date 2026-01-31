package com.example.etic.ui.inspection.tabs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import com.example.etic.data.local.DbProvider
import com.example.etic.data.repository.InspectionUiRepository
import com.example.etic.features.inspection.LocalCurrentInspection
import com.example.etic.features.inspection.LocalCurrentUser
import com.example.etic.features.inspection.ProblemsTable
import com.example.etic.features.inspection.STATUS_POR_VERIFICAR
import com.example.etic.features.inspection.STATUS_VERIFICADO
import com.example.etic.features.inspection.tree.Problem
import kotlinx.coroutines.launch

@Composable
fun ProblemsTableFromDatabase(
    selectedId: String?,
    refreshTick: Int,
    typeFilterId: String?,
    statusFilterId: String,
    modifier: Modifier = Modifier,
    onProblemDeleted: (() -> Unit)? = null,
    onProblemDoubleTap: ((Problem, List<Problem>) -> Unit)? = null
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

    var problemsCache by remember { mutableStateOf(emptyList<Problem>()) }
    var problemToDelete by remember { mutableStateOf<Problem?>(null) }
    val uiProblems by produceState(
        initialValue = problemsCache,
        selectedId,
        refreshTick,
        typeFilterId,
        statusFilterId,
        currentInspection?.idInspeccion,
        currentInspection?.idSitio
    ) {
        value = repo.loadProblemsForUi(
            currentInspectionId = currentInspection?.idInspeccion,
            currentSiteId = currentInspection?.idSitio,
            selectedUbicacionId = selectedId,
            typeFilterId = typeFilterId,
            statusFilterId = statusFilterId
        )
        problemsCache = value
    }

    Box(modifier) {
        ProblemsTable(
            problems = uiProblems,
            onDelete = { problem -> problemToDelete = problem },
            onDoubleTap = onProblemDoubleTap
        )

        if (problemToDelete != null) {
            val problem = problemToDelete!!
            AlertDialog(
                onDismissRequest = { problemToDelete = null },
                confirmButton = {
                    Button(onClick = {
                        scope.launch {
                            val nowTs = java.time.LocalDateTime.now()
                                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                            val ok = repo.softDeleteProblemAndRecalculate(
                                problemId = problem.id,
                                currentUserId = currentUser?.idUsuario,
                                nowTs = nowTs,
                                statusPorVerificarId = STATUS_POR_VERIFICAR,
                                statusVerificadoId = STATUS_VERIFICADO
                            )
                            if (ok) {
                                problemsCache = emptyList()
                                onProblemDeleted?.invoke()
                                problemToDelete = null
                            }
                        }
                    }) { Text("Eliminar") }
                },
                dismissButton = {
                    Button(onClick = { problemToDelete = null }) { Text("Cancelar") }
                },
                text = { Text("Eliminar problema seleccionado?") }
            )
        }
    }
}
