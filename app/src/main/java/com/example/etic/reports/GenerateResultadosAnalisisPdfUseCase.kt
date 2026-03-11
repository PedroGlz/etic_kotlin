package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.etic.data.local.DbProvider
import com.example.etic.reports.pdf.ResultadosAnalisisPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class GenerateResultadosAnalisisPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val getInspeccionImagenesTreeUri: (inspectionNumber: String) -> Uri?,
    private val pdfGenerator: ResultadosAnalisisPdfGenerator = ResultadosAnalisisPdfGenerator()
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        draft: ResultadosAnalisisDraft,
        currentUserId: String? = null,
        currentUserName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val inspeccionDao = db.inspeccionDao()
            val clienteDao = db.clienteDao()
            val sitioDao = db.sitioDao()
            val grupoDao = db.grupoSitiosDao()
            val usuarioDao = db.usuarioDao()
            val problemaDao = db.problemaDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspeccion no encontrada."))
            val sitio = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it) }
            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else null ?: if (!currentUserName.isNullOrBlank()) {
                usuarioDao.getByUsuario(currentUserName)
            } else {
                null
            }

            fun formatDate(raw: String?): String {
                val value = raw?.take(10).orEmpty()
                if (value.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(value).format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale("es", "MX"))
                    )
                }.getOrDefault(value)
            }

            fun formatFechaServicio(startRaw: String?, endRaw: String?): String {
                val startValue = startRaw?.take(10).orEmpty()
                val endValue = endRaw?.take(10).orEmpty()
                val start = runCatching { LocalDate.parse(startValue) }.getOrNull()
                val end = runCatching { LocalDate.parse(endValue) }.getOrNull() ?: start
                if (start == null || end == null) return formatDate(startRaw ?: endRaw)
                return if (start == end) {
                    formatDate(start.toString())
                } else {
                    val startFmt = start.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX"))
                    )
                    val endFmt = end.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale("es", "MX"))
                    )
                    "Del $startFmt al $endFmt"
                }
            }

            val previousInspectionDate = runCatching {
                val previousNo = inspeccion.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == previousNo }?.fechaInicio
            }.getOrNull()

            val allProblems = problemaDao.getByInspeccionActivos(inspeccionId)
            val selectedProblems = if (draft.selectedProblemIds.isEmpty()) {
                allProblems
            } else {
                val selectedSet = draft.selectedProblemIds.toSet()
                allProblems.filter { it.idProblema in selectedSet }
            }

            val electricos = selectedProblems.count {
                it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                    it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true)
            }
            val visuales = selectedProblems.count { it.idTipoInspeccion.equals(ProblemTypeIds.VISUAL, true) }
            val mecanicos = selectedProblems.count { it.idTipoInspeccion.equals("0D32B334-76C3-11D3-82BF-00104BC75DC2", true) }
            val aislamiento = selectedProblems.count { it.idTipoInspeccion.equals(ProblemTypeIds.AISLAMIENTO_TERMICO, true) }
            val cronicos = selectedProblems.count { it.esCronico.equals("SI", true) }
            val cerrados = selectedProblems.count { it.estatusProblema.equals("Cerrado", true) }

            val imageFolder = getInspeccionImagenesTreeUri(noInspeccion)?.let { treeUri ->
                DocumentFile.fromTreeUri(context, treeUri) ?: DocumentFile.fromSingleUri(context, treeUri)
            }
            val portada = draft.nombreImgPortada.takeIf { it.isNotBlank() }?.let { imageName ->
                val file = imageFolder?.findFile(imageName)
                    ?: imageFolder?.listFiles()?.firstOrNull { it.name.equals(imageName, true) }
                runCatching {
                    file?.let {
                        context.contentResolver.openInputStream(it.uri)?.use(BitmapFactory::decodeStream)
                    }
                }.getOrNull()
            }

            val direccion = listOfNotNull(
                sitio?.direccion?.takeIf { it.isNotBlank() },
                sitio?.colonia?.takeIf { it.isNotBlank() },
                sitio?.municipio?.takeIf { it.isNotBlank() },
                sitio?.estado?.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            val header = ResultadosAnalisisPdfHeader(
                cliente = inspeccion.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty(),
                sitio = sitio?.sitio.orEmpty(),
                grupoSitio = inspeccion.idGrupoSitios?.let { grupoDao.getByIdActivo(it)?.grupo }.orEmpty(),
                direccionCompleta = direccion,
                fechaServicio = formatFechaServicio(draft.fechaInicio, draft.fechaFin),
                fechaServicioAnterior = formatDate(previousInspectionDate),
                analista = currentUserName?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.nombre?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.usuario.orEmpty(),
                nivel = usuarioActual?.nivelCertificacion.orEmpty()
            )

            val metricas = listOf(
                ResultadosAnalisisPdfMetric("Total de hallazgos", selectedProblems.size.toString()),
                ResultadosAnalisisPdfMetric("Electricos", electricos.toString()),
                ResultadosAnalisisPdfMetric("Visuales", visuales.toString()),
                ResultadosAnalisisPdfMetric("Mecanicos", mecanicos.toString()),
                ResultadosAnalisisPdfMetric("Aislamiento termico", aislamiento.toString()),
                ResultadosAnalisisPdfMetric("Cronicos", cronicos.toString()),
                ResultadosAnalisisPdfMetric("Cerrados", cerrados.toString())
            )

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }
            val isoLogoId = res.getIdentifier("iso_img", "drawable", pkg).takeIf { it != 0 }
            val isoLogoBmp = isoLogoId?.let { BitmapFactory.decodeResource(res, it) }

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(IllegalStateException("No hay acceso a carpeta Reportes (SAF)."))
            val file = folderProvider.createPdfFile(
                folder,
                "ETIC_PORTADA_REPORTE_INSPECCION_$noInspeccion.pdf"
            ) ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el PDF principal."))

            val data = ResultadosAnalisisPdfData(
                header = header,
                contactos = draft.contactos,
                portada = portada,
                descripciones = draft.descripciones,
                areasInspeccionadas = draft.areasInspeccionadas,
                metricas = metricas,
                recomendaciones = draft.recomendaciones,
                referencias = draft.referencias,
                logo = logoBmp,
                isoLogo = isoLogoBmp
            )

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                pdfGenerator.generate(out, data)
            } ?: return@withContext Result.failure(IllegalStateException("No se pudo abrir OutputStream del PDF."))

            Result.success(file.uri.toString())
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
