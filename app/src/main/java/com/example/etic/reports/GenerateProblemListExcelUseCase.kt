package com.example.etic.reports

import android.content.Context
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GenerateProblemListExcelUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        reportStartDate: String? = null,
        reportEndDate: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val problemaDao = db.problemaDao()
            val inspeccionDao = db.inspeccionDao()
            val clienteDao = db.clienteDao()
            val sitioDao = db.sitioDao()
            val tipoInspeccionDao = db.tipoInspeccionDao()
            val severidadDao = db.severidadDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspección no encontrada."))

            val problemas = problemaDao.getByInspeccionActivos(inspeccionId)
                .sortedWith(
                    compareBy(
                        { it.idTipoInspeccion ?: "ZZZ" },
                        { it.numeroProblema ?: Int.MAX_VALUE }
                    )
                )

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(
                    IllegalStateException("No hay acceso a carpeta Reportes (SAF).")
                )

            val sitioNombre = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it)?.sitio }.orEmpty()
            val clienteNombre = inspeccion.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty()
            val tipoById = tipoInspeccionDao.getAll().associateBy { it.idTipoInspeccion }
            val sevById = severidadDao.getAll().associateBy { it.idSeveridad }

            fun formatDate(raw: String?): String {
                val value = raw?.take(10).orEmpty()
                if (value.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(value)
            }

            fun formatFechaServicio(startRaw: String?, endRaw: String?): String {
                val start = startRaw?.take(10).orEmpty()
                val end = endRaw?.take(10).orEmpty()
                if (start.isBlank() && end.isBlank()) return ""
                val startDate = runCatching { LocalDate.parse(start) }.getOrNull()
                val endDate = runCatching { LocalDate.parse(end) }.getOrNull() ?: startDate

                fun mesEs(m: Int) = listOf(
                    "enero", "febrero", "marzo", "abril", "mayo", "junio",
                    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
                )[m - 1]

                val s = startDate
                val e = endDate
                if (s == null || e == null) {
                    val fallback = (startRaw ?: "").ifBlank { endRaw.orEmpty() }
                    return formatDate(fallback)
                }
                return if (s == e) {
                    "${s.dayOfMonth} de ${mesEs(s.monthValue)} del ${s.year}"
                } else {
                    "Del ${s.dayOfMonth} de ${mesEs(s.monthValue)} al ${e.dayOfMonth} de ${mesEs(e.monthValue)} del ${e.year}"
                }
            }

            val fechaServicio = formatFechaServicio(
                reportStartDate ?: inspeccion.fechaInicio,
                reportEndDate ?: inspeccion.fechaFin
            )

            val sitioFilePart = sitioNombre
                .replace(Regex("[^A-Za-z0-9_-]"), "_")
                .ifBlank { "SITIO" }
            val file = folderProvider.createOrReplaceFile(
                folder,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "ETIC_LISTADO_DE_PROBLEMAS_${sitioFilePart}_INSPECCION_$noInspeccion.xlsx"
            ) ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el archivo Excel."))

            context.assets.open("plantillas_reportes/LISTA_PROBLEMAS.xlsx").use { templateStream ->
                WorkbookFactory.create(templateStream).use { workbook ->
                    val sheet = workbook.getSheetAt(0)

                    fun setCellValue(rowIndex: Int, colIndex: Int, value: String) {
                        val row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
                        val cell = row.getCell(colIndex) ?: row.createCell(colIndex)
                        cell.setCellValue(value)
                    }

                    setCellValue(3, 11, "Cliente: $clienteNombre / $sitioNombre")
                    setCellValue(4, 11, "Fecha Servicio: $fechaServicio")

                    var rowNum = 8
                    problemas.forEach { p ->
                        setCellValue(rowNum, 0, noInspeccion)
                        setCellValue(rowNum, 1, p.numeroProblema?.toString().orEmpty())
                        setCellValue(rowNum, 2, sitioNombre)
                        setCellValue(
                            rowNum,
                            3,
                            p.idTipoInspeccion?.let { tipoById[it]?.tipoInspeccion }.orEmpty()
                        )
                        setCellValue(rowNum, 4, p.ruta.orEmpty())
                        setCellValue(
                            rowNum,
                            5,
                            p.idSeveridad?.let { sevById[it]?.severidad }.orEmpty()
                        )
                        setCellValue(rowNum, 6, p.problemTemperature?.toString().orEmpty())
                        setCellValue(rowNum, 7, p.referenceTemperature?.toString().orEmpty())
                        setCellValue(rowNum, 8, p.estatusProblema.orEmpty())
                        setCellValue(rowNum, 9, p.esCronico.orEmpty())
                        setCellValue(rowNum, 10, formatDate(p.fechaCreacion))
                        setCellValue(rowNum, 11, p.componentComment.orEmpty())
                        rowNum++
                    }

                    context.contentResolver.openOutputStream(file.uri)?.use { out ->
                        workbook.write(out)
                    } ?: return@withContext Result.failure(
                        IllegalStateException("No se pudo abrir OutputStream del SAF.")
                    )
                }
            }

            Result.success(file.uri.toString())
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
