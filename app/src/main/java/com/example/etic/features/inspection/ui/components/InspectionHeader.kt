package com.example.etic.features.inspection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import com.example.etic.R
import com.example.etic.data.local.entities.EstatusInspeccionDet

private val HEADER_ACTION_SPACING: Dp = 8.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionHeader(
    barcode: String,
    onBarcodeChange: (String) -> Unit,
    onSearch: () -> Unit,
    statusMenuExpanded: Boolean,
    onStatusMenuToggle: () -> Unit,
    onStatusMenuDismiss: () -> Unit,
    selectedStatusLabel: String,
    statusOptions: List<EstatusInspeccionDet>,
    onStatusSelected: (EstatusInspeccionDet?) -> Unit,
    onClickNewLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val controlHeight = 56.dp
        TextField(
            value = barcode,
            onValueChange = onBarcodeChange,
            singleLine = true,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            label = { Text(stringResource(R.string.label_codigo_barras)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            modifier = Modifier
                .widthIn(min = 180.dp, max = 320.dp)
                .height(controlHeight)
        )
        Spacer(Modifier.width(HEADER_ACTION_SPACING))
        ExposedDropdownMenuBox(
            expanded = statusMenuExpanded,
            onExpandedChange = { onStatusMenuToggle() }
        ) {
            TextField(
                value = selectedStatusLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_estatus)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .height(controlHeight)
            )
            DropdownMenu(
                expanded = statusMenuExpanded,
                onDismissRequest = { onStatusMenuDismiss() }
            ) {
                DropdownMenuItem(
                    text = { Text("Todos") },
                    onClick = {
                        onStatusSelected(null)
                        onStatusMenuDismiss()
                    }
                )
                statusOptions.forEach { opt ->
                    val label = opt.estatusInspeccionDet ?: opt.idStatusInspeccionDet
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStatusSelected(opt)
                            onStatusMenuDismiss()
                        }
                    )
                }
            }
        }
        Spacer(Modifier.width(HEADER_ACTION_SPACING))

        Button(
            onClick = onClickNewLocation,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.height(controlHeight)
        ) {
            Text(stringResource(R.string.btn_nueva_ubicacion), color = Color.White)
        }
    }
}
