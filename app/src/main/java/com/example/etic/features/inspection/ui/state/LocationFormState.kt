package com.example.etic.features.inspection.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable

@Stable
class LocationFormState internal constructor(
    private val nameState: MutableState<String>,
    private val descriptionState: MutableState<String>,
    private val isEquipmentState: MutableState<Boolean>,
    private val errorState: MutableState<String?>,
    private val statusExpandedState: MutableState<Boolean>,
    private val statusLabelState: MutableState<String>,
    private val statusIdState: MutableState<String?>,
    private val barcodeState: MutableState<String>,
    private val prioridadExpandedState: MutableState<Boolean>,
    private val prioridadLabelState: MutableState<String>,
    private val prioridadIdState: MutableState<String?>,
    private val fabricanteExpandedState: MutableState<Boolean>,
    private val fabricanteLabelState: MutableState<String>,
    private val fabricanteIdState: MutableState<String?>
) {
    var name: String
        get() = nameState.value
        set(value) { nameState.value = value }

    var description: String
        get() = descriptionState.value
        set(value) { descriptionState.value = value }

    var isEquipment: Boolean
        get() = isEquipmentState.value
        set(value) { isEquipmentState.value = value }

    var error: String?
        get() = errorState.value
        set(value) { errorState.value = value }

    var statusExpanded: Boolean
        get() = statusExpandedState.value
        set(value) { statusExpandedState.value = value }

    var statusLabel: String
        get() = statusLabelState.value
        set(value) { statusLabelState.value = value }

    var statusId: String?
        get() = statusIdState.value
        set(value) { statusIdState.value = value }

    var barcode: String
        get() = barcodeState.value
        set(value) { barcodeState.value = value }

    var prioridadExpanded: Boolean
        get() = prioridadExpandedState.value
        set(value) { prioridadExpandedState.value = value }

    var prioridadLabel: String
        get() = prioridadLabelState.value
        set(value) { prioridadLabelState.value = value }

    var prioridadId: String?
        get() = prioridadIdState.value
        set(value) { prioridadIdState.value = value }

    var fabricanteExpanded: Boolean
        get() = fabricanteExpandedState.value
        set(value) { fabricanteExpandedState.value = value }

    var fabricanteLabel: String
        get() = fabricanteLabelState.value
        set(value) { fabricanteLabelState.value = value }

    var fabricanteId: String?
        get() = fabricanteIdState.value
        set(value) { fabricanteIdState.value = value }

    fun resetForNew() {
        name = ""
        description = ""
        isEquipment = false
        error = null
        barcode = ""
        prioridadId = null
        prioridadLabel = ""
        fabricanteId = null
        fabricanteLabel = ""
        statusId = null
        statusLabel = ""
        prioridadExpanded = false
        fabricanteExpanded = false
        statusExpanded = false
    }
}

@Composable
fun rememberLocationFormState(): LocationFormState {
    val name = rememberSaveable { mutableStateOf("") }
    val description = rememberSaveable { mutableStateOf("") }
    val isEquipment = rememberSaveable { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val statusExpanded = remember { mutableStateOf(false) }
    val statusLabel = rememberSaveable { mutableStateOf("") }
    val statusId = rememberSaveable { mutableStateOf<String?>(null) }
    val barcode = rememberSaveable { mutableStateOf("") }
    val prioridadExpanded = remember { mutableStateOf(false) }
    val prioridadLabel = rememberSaveable { mutableStateOf("") }
    val prioridadId = rememberSaveable { mutableStateOf<String?>(null) }
    val fabricanteExpanded = remember { mutableStateOf(false) }
    val fabricanteLabel = rememberSaveable { mutableStateOf("") }
    val fabricanteId = rememberSaveable { mutableStateOf<String?>(null) }

    return remember {
        LocationFormState(
            nameState = name,
            descriptionState = description,
            isEquipmentState = isEquipment,
            errorState = error,
            statusExpandedState = statusExpanded,
            statusLabelState = statusLabel,
            statusIdState = statusId,
            barcodeState = barcode,
            prioridadExpandedState = prioridadExpanded,
            prioridadLabelState = prioridadLabel,
            prioridadIdState = prioridadId,
            fabricanteExpandedState = fabricanteExpanded,
            fabricanteLabelState = fabricanteLabel,
            fabricanteIdState = fabricanteId
        )
    }
}
