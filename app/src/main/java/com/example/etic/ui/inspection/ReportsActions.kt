package com.example.etic.ui.inspection

sealed interface ReportAction {
    data object InventarioPdf : ReportAction
    data object ProblemasPdf : ReportAction
}
