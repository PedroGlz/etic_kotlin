package com.example.etic.ui.inspection

sealed interface ReportAction {
    data object InventarioPdf : ReportAction
    // (después agregamos Problemas, Baseline, etc.)
}
