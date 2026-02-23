package com.example.etic.features.inspection.ui.problem

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

private val PROBLEM_DIALOG_HEADER_TURQUOISE = Color(0xFF159BA6)

@Composable
internal fun ProblemDialogDraggableHeader(
    title: String,
    onDrag: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(PROBLEM_DIALOG_HEADER_TURQUOISE)
            .padding(start = 17.dp, end = 17.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(Offset(dragAmount.x, dragAmount.y))
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(PROBLEM_DIALOG_HEADER_TURQUOISE, RoundedCornerShape(6.dp))
                .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 4.dp)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
