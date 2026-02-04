package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import com.example.etic.data.local.DbProvider
import com.example.etic.reports.pdf.AnomaliasChartPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GenerateAnomaliasChartPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val pdfGenerator: AnomaliasChartPdfGenerator = AnomaliasChartPdfGenerator()
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        currentUserId: String? = null,
        currentUserName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val problemaDao = db.problemaDao()
            val inspeccionDao = db.inspeccionDao()
            val clienteDao = db.clienteDao()
            val sitioDao = db.sitioDao()
            val usuarioDao = db.usuarioDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspeccion no encontrada."))
            val siteId = inspeccion.idSitio.orEmpty()
            val all = problemaDao.getAllActivos().filter { it.idSitio == siteId && it.idInspeccion == inspeccionId }

            fun countBy(
                tipo: String? = null,
                estatus: String? = null,
                severidad: String? = null,
                cronico: String? = null
            ): Int = all.count { p ->
                (tipo == null || p.idTipoInspeccion == tipo) &&
                    (estatus == null || p.estatusProblema.equals(estatus, true)) &&
                    (severidad == null || p.idSeveridad == severidad) &&
                    (cronico == null || p.esCronico.equals(cronico, true))
            }

            val bars = listOf(
                AnomaliaBarData("Total de Hallazgos", Color.rgb(74, 127, 194), all.size),
                AnomaliaBarData(
                    "Electricos Abiertos Criticos",
                    Color.rgb(173, 0, 0),
                    countBy(ProblemTypeIds.ELECTRICO, "Abierto", ReportSeverityIds.CRITICO)
                ),
                AnomaliaBarData(
                    "Electricos Abiertos Serios",
                    Color.rgb(245, 0, 0),
                    countBy(ProblemTypeIds.ELECTRICO, "Abierto", ReportSeverityIds.SERIO)
                ),
                AnomaliaBarData(
                    "Electricos Abiertos Importantes",
                    Color.rgb(245, 100, 0),
                    countBy(ProblemTypeIds.ELECTRICO, "Abierto", ReportSeverityIds.IMPORTANTE)
                ),
                AnomaliaBarData(
                    "Electricos Abiertos Menores",
                    Color.rgb(225, 208, 0),
                    countBy(ProblemTypeIds.ELECTRICO, "Abierto", ReportSeverityIds.MENOR)
                ),
                AnomaliaBarData(
                    "Hallazgos Abiertos Mecanicos",
                    Color.rgb(124, 174, 230),
                    countBy("0D32B334-76C3-11D3-82BF-00104BC75DC2", "Abierto")
                ),
                AnomaliaBarData(
                    "Hallazgos Abiertos Visuales",
                    Color.rgb(181, 181, 181),
                    countBy(ProblemTypeIds.VISUAL, "Abierto")
                ),
                AnomaliaBarData(
                    "Anomalias/Hallazgos Reparados",
                    Color.rgb(0, 149, 2),
                    countBy(null, "Cerrado")
                )
            )
            val cronicos = countBy(cronico = "SI")

            fun formatDate(raw: String?): String {
                val r = raw?.take(10).orEmpty()
                if (r.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(r).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(r)
            }

            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else null ?: if (!currentUserName.isNullOrBlank()) usuarioDao.getByUsuario(currentUserName) else null

            val prevDate = runCatching {
                val noPrev = inspeccion.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == noPrev }?.fechaInicio
            }.getOrNull()
            val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val header = ReportHeaderData(
                cliente = inspeccion.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty(),
                sitio = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it)?.sitio }.orEmpty(),
                analista = currentUserName?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.nombre?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.usuario.orEmpty(),
                nivel = usuarioActual?.nivelCertificacion.orEmpty(),
                inspeccionAnterior = inspeccion.noInspeccionAnt?.toString().orEmpty(),
                fechaAnterior = formatDate(prevDate),
                inspeccionActual = inspeccion.noInspeccion?.toString().orEmpty(),
                fechaActual = formatDate(inspeccion.fechaInicio),
                fechaReporte = now
            )

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(IllegalStateException("No hay acceso a carpeta Reportes (SAF)."))
            val file = folderProvider.createPdfFile(
                folder,
                "ETIC_GRAFICA_ANOMALIAS_INSPECCION_$noInspeccion.pdf"
            ) ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el PDF."))

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                pdfGenerator.generate(out, header, bars, cronicos, logoBmp)
            } ?: return@withContext Result.failure(IllegalStateException("No se pudo abrir OutputStream."))

            Result.success(file.uri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

