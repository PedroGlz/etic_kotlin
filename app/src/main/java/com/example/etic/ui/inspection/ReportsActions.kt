package com.example.etic.ui.inspection

sealed interface ReportAction {
    data object InventarioPdf : ReportAction
    data object ProblemasPdf : ReportAction
    data object BaselinePdf : ReportAction
    data object ListaProblemasAbiertosPdf : ReportAction
    data object ListaProblemasCerradosPdf : ReportAction
    data object GraficaAnomaliasPdf : ReportAction
    data object ListaProblemasExcel : ReportAction
}
