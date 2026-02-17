package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.example.etic.R
import com.example.etic.data.local.entities.EstatusInspeccionDet

private val HEADER_ACTION_SPACING: Dp = 8.dp

@Composable
fun InspectionHeader(
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    selectedStatusId: String?,
    statusOptions: List<EstatusInspeccionDet>,
    onStatusSelected: (EstatusInspeccionDet?) -> Unit,
    onApplyStatus: () -> Unit,
    onClickNewLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusItems = remember(statusOptions) {
        buildList {
            add(null to "Todos")
            statusOptions.forEach { opt ->
                val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                add(opt.idStatusInspeccionDet to label)
            }
        }
    }

    Row(
        modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterTextField(
            label = stringResource(R.string.label_codigo_barras),
            value = barcode,
            onValueChange = onBarcodeChange,
            onSearch = onSearch,
            placeholder = stringResource(R.string.label_codigo_barras),
            minWidth = 180.dp,
            maxWidth = 320.dp
        )
        Spacer(Modifier.width(HEADER_ACTION_SPACING))
        FilterDropdownField(
            label = stringResource(R.string.label_estatus),
            options = statusItems,
            selectedId = selectedStatusId,
            onSelected = { id ->
                val selected = statusOptions.firstOrNull { it.idStatusInspeccionDet == id }
                onStatusSelected(selected)
            },
            minWidth = 160.dp,
            maxWidth = 240.dp
        )
        Spacer(Modifier.width(HEADER_ACTION_SPACING))
        Button(
            onClick = onApplyStatus,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F7A8C)),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(
                horizontal = 10.dp,
                vertical = 0.dp
            )
        ) {
            Text("Aplicar", color = Color.White)
        }
        Spacer(Modifier.width(HEADER_ACTION_SPACING))

        Button(
            onClick = onClickNewLocation,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(
                horizontal = 10.dp,
                vertical = 0.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.height(13.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(stringResource(R.string.btn_nueva_ubicacion), color = Color.White)
        }
    }
}
