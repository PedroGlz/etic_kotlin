package com.example.etic.reports

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.example.etic.data.local.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GenerateProblemListExcelUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String
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
                ?: return@withContext Result.failure(IllegalStateException("Inspeccion no encontrada."))

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
            val inspeccionesById = inspeccionDao.getAll().associateBy { it.idInspeccion }

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

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("LISTA_PROBLEMAS")

            // Encabezado tipo plantilla PHP
            sheet.createRow(3).createCell(11).setCellValue("Cliente: $clienteNombre / $sitioNombre")
            val fechaServicio = formatFechaServicio(inspeccion.fechaInicio, inspeccion.fechaFin)
            sheet.createRow(4).createCell(11).setCellValue("Fecha Servicio: $fechaServicio")

            val header = listOf(
                "No Inspeccion",
                "No Problema",
                "Sitio",
                "Tipo Inspeccion",
                "Ruta",
                "Severidad",
                "Temp Problema",
                "Temp Referencia",
                "Estatus Problema",
                "Es Cronico",
                "Fecha Creacion",
                "Comentario"
            )
            val headerRow = sheet.createRow(7)
            header.forEachIndexed { i, title -> headerRow.createCell(i).setCellValue(title) }

            var rowNum = 8
            problemas.forEach { p ->
                val row = sheet.createRow(rowNum++)
                row.createCell(0).setCellValue(
                    inspeccionesById[p.idInspeccion]?.noInspeccion?.toString().orEmpty()
                )
                row.createCell(1).setCellValue(p.numeroProblema?.toString().orEmpty())
                row.createCell(2).setCellValue(sitioNombre)
                row.createCell(3).setCellValue(
                    p.idTipoInspeccion?.let { tipoById[it]?.tipoInspeccion }.orEmpty()
                )
                row.createCell(4).setCellValue(p.ruta.orEmpty())
                row.createCell(5).setCellValue(p.idSeveridad?.let { sevById[it]?.severidad }.orEmpty())
                row.createCell(6).setCellValue(p.problemTemperature?.toString().orEmpty())
                row.createCell(7).setCellValue(p.referenceTemperature?.toString().orEmpty())
                row.createCell(8).setCellValue(p.estatusProblema.orEmpty())
                row.createCell(9).setCellValue(p.esCronico.orEmpty())
                row.createCell(10).setCellValue(formatDate(p.fechaCreacion))
                row.createCell(11).setCellValue(p.componentComment.orEmpty())
            }

            // En Android, autoSizeColumn puede lanzar NoClassDefFoundError (AWT no disponible).
            // Definimos anchos fijos similares a la plantilla para evitar cierres inesperados.
            val widths = intArrayOf(
                12 * 256, // A
                10 * 256, // B
                18 * 256, // C
                16 * 256, // D
                45 * 256, // E
                14 * 256, // F
                14 * 256, // G
                16 * 256, // H
                16 * 256, // I
                12 * 256, // J
                14 * 256, // K
                50 * 256  // L
            )
            widths.forEachIndexed { idx, w -> sheet.setColumnWidth(idx, w) }

            fun createXlsxFile(folderDoc: DocumentFile, baseName: String): DocumentFile? {
                var attempt = 0
                var name = "$baseName.xlsx"
                while (folderDoc.findFile(name) != null && attempt < 20) {
                    attempt++
                    name = "${baseName}_$attempt.xlsx"
                }
                return folderDoc.createFile(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    name
                )
            }

            val sitioFilePart = sitioNombre
                .replace(Regex("[^A-Za-z0-9_-]"), "_")
                .ifBlank { "SITIO" }
            val file = createXlsxFile(
                folder,
                "ETIC_LISTADO_DE_PROBLEMAS_${sitioFilePart}_INSPECCION_$noInspeccion"
            ) ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el archivo Excel."))

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                workbook.use { wb -> wb.write(out) }
            } ?: return@withContext Result.failure(
                IllegalStateException("No se pudo abrir OutputStream del SAF.")
            )

            Result.success(file.uri.toString())
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
