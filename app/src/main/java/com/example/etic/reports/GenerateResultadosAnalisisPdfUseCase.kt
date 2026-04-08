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
    private val getClientesImagenesTreeUri: (inspectionNumber: String) -> Uri?,
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
                ?: return@withContext Result.failure(IllegalStateException("Inspección no encontrada."))
            val sitio = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it) }
            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else null ?: if (!currentUserName.isNullOrBlank()) {
                usuarioDao.getByUsuario(currentUserName)
            } else {
                null
            }

            val inputDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            fun parseInputDate(raw: String?): LocalDate? {
                val value = raw.orEmpty().trim()
                if (value.isBlank()) return null
                return runCatching { LocalDate.parse(value) }.getOrNull()
                    ?: runCatching { LocalDate.parse(value, inputDateFormatter) }.getOrNull()
            }

            fun formatMonthYear(raw: String?): String {
                val value = raw.orEmpty().trim()
                if (value.isBlank()) return ""
                val parsed = parseInputDate(value) ?: return ""
                val text = parsed.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "MX"))
                )
                return text.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(java.util.Locale("es", "MX")) else it.toString()
                }
            }

            fun formatDate(raw: String?): String {
                val value = raw?.trim().orEmpty()
                if (value.isBlank()) return ""
                val parsed = parseInputDate(value) ?: return value
                return runCatching {
                    parsed.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale("es", "MX"))
                    )
                }.getOrDefault(value)
            }

            fun formatFechaServicio(startRaw: String?, endRaw: String?): String {
                val start = parseInputDate(startRaw)
                val end = parseInputDate(endRaw) ?: start
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

            fun formatFechaServicioNarrativa(startRaw: String?, endRaw: String?): String {
                val start = parseInputDate(startRaw)
                val end = parseInputDate(endRaw) ?: start
                if (start == null || end == null) return ""
                return if (start == end) {
                    "el ${formatDate(start.toString())}"
                } else {
                    val startFmt = start.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "MX"))
                    )
                    val endFmt = end.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM 'del' yyyy", Locale("es", "MX"))
                    )
                    "del $startFmt al $endFmt"
                }
            }

            val previousInspectionDate = runCatching {
                val previousNo = inspeccion.noInspeccionAnt ?: return@runCatching null
                val previousInspection = inspeccionDao.getAll().firstOrNull { it.noInspeccion == previousNo }
                previousInspection?.fechaFin?.take(10)
                    ?.ifBlank { null }
                    ?: previousInspection?.fechaInicio
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
            val cronicosElectricos = selectedProblems.count {
                (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                    it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true)) &&
                    it.esCronico.equals("SI", true)
            }
            val electricosCerrados = selectedProblems.count {
                (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                    it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true)) &&
                    it.estatusProblema.equals("Cerrado", true)
            }
            val cronicosVisuales = selectedProblems.count {
                it.idTipoInspeccion.equals(ProblemTypeIds.VISUAL, true) &&
                    it.esCronico.equals("SI", true)
            }
            val visualesCerrados = selectedProblems.count {
                it.idTipoInspeccion.equals(ProblemTypeIds.VISUAL, true) &&
                    it.estatusProblema.equals("Cerrado", true)
            }
            val cronicosMecanicos = selectedProblems.count {
                it.idTipoInspeccion.equals("0D32B334-76C3-11D3-82BF-00104BC75DC2", true) &&
                    it.esCronico.equals("SI", true)
            }
            val mecanicosCerrados = selectedProblems.count {
                it.idTipoInspeccion.equals("0D32B334-76C3-11D3-82BF-00104BC75DC2", true) &&
                    it.estatusProblema.equals("Cerrado", true)
            }
            val totalCriticos = selectedProblems.count {
                it.idSeveridad.equals("1D56EDB0-8D6E-11D3-9270-006008A19766", true) &&
                    (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                        it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true))
            }
            val totalSerios = selectedProblems.count {
                it.idSeveridad.equals("1D56EDB1-8D6E-11D3-9270-006008A19766", true) &&
                    (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                        it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true))
            }
            val totalImportantes = selectedProblems.count {
                it.idSeveridad.equals("1D56EDB2-8D6E-11D3-9270-006008A19766", true) &&
                    (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                        it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true))
            }
            val totalMenores = selectedProblems.count {
                it.idSeveridad.equals("1D56EDB3-8D6E-11D3-9270-006008A19766", true) &&
                    (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                        it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true))
            }
            val totalNormal = selectedProblems.count {
                it.idSeveridad.equals("1D56EDB4-8D6E-11D3-9270-006008A19766", true) &&
                    (it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO, true) ||
                        it.idTipoInspeccion.equals(ProblemTypeIds.ELECTRICO_2, true))
            }

            val imageFolder = getInspeccionImagenesTreeUri(noInspeccion)?.let { treeUri ->
                DocumentFile.fromTreeUri(context, treeUri) ?: DocumentFile.fromSingleUri(context, treeUri)
            }
            val clienteImageFolder = getClientesImagenesTreeUri(noInspeccion)?.let { treeUri ->
                DocumentFile.fromTreeUri(context, treeUri) ?: DocumentFile.fromSingleUri(context, treeUri)
            }

            fun loadImageByName(imageName: String?, folder: DocumentFile?): android.graphics.Bitmap? {
                val normalized = imageName?.trim().orEmpty()
                if (normalized.isBlank()) return null
                val file = folder?.findFile(normalized)
                    ?: folder?.listFiles()?.firstOrNull { it.name.equals(normalized, true) }
                return runCatching {
                    file?.let {
                        context.contentResolver.openInputStream(it.uri)?.use(BitmapFactory::decodeStream)
                    }
                }.getOrNull()
            }

            val portada = draft.nombreImgPortada.takeIf { it.isNotBlank() }?.let { imageName ->
                loadImageByName(imageName, imageFolder)
            }
            val portada2 = draft.nombreImgPortada2.takeIf { it.isNotBlank() }?.let { imageName ->
                loadImageByName(imageName, imageFolder)
            }
            val portada3 = draft.nombreImgPortada3.takeIf { it.isNotBlank() }?.let { imageName ->
                loadImageByName(imageName, clienteImageFolder)
                    ?: loadImageByName(imageName, imageFolder)
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
                detalleUbicacion = draft.detalleUbicacion.trim(),
                municipio = sitio?.municipio.orEmpty(),
                estado = sitio?.estado.orEmpty(),
                fechaServicio = formatFechaServicio(draft.fechaInicio, draft.fechaFin),
                fechaServicioNarrativa = formatFechaServicioNarrativa(draft.fechaInicio, draft.fechaFin),
                fechaServicioAnterior = formatMonthYear(
                    draft.fechaAnterior.ifBlank { previousInspectionDate }
                ),
                analista = currentUserName?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.nombre?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.usuario.orEmpty(),
                nivel = usuarioActual?.nivelCertificacion.orEmpty(),
                telefono = usuarioActual?.telefono.orEmpty(),
                email = usuarioActual?.email.orEmpty()
            )
            val stats = ResultadosAnalisisPdfStats(
                totalHallazgos = selectedProblems.size,
                totalElectricos = electricos,
                cronicosElectricos = cronicosElectricos,
                electricosCerrados = electricosCerrados,
                totalMecanicos = mecanicos,
                cronicosMecanicos = cronicosMecanicos,
                mecanicosCerrados = mecanicosCerrados,
                totalVisuales = visuales,
                cronicosVisuales = cronicosVisuales,
                visualesCerrados = visualesCerrados,
                totalCriticos = totalCriticos,
                totalSerios = totalSerios,
                totalImportantes = totalImportantes,
                totalMenores = totalMenores,
                totalNormal = totalNormal
            )

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }
            val isoLogoId = res.getIdentifier("iso_img", "drawable", pkg).takeIf { it != 0 }
            val isoLogoBmp = isoLogoId?.let { BitmapFactory.decodeResource(res, it) }
            val clienteLogo = draft.nombreImgPortada3.takeIf { it.isNotBlank() }?.let { imageName ->
                loadImageByName(imageName, clienteImageFolder)
            }

            val recomendaciones = draft.recomendaciones.map {
                ResultadosAnalisisPdfRecommendationEntry(
                    texto = it.texto,
                    imagen1 = loadImageByName(it.imagen1, imageFolder),
                    imagen2 = loadImageByName(it.imagen2, imageFolder)
                )
            }

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
                portada2 = portada2,
                portada3 = portada3,
                logoCliente = clienteLogo,
                descripciones = draft.descripciones,
                areasInspeccionadas = draft.areasInspeccionadas,
                stats = stats,
                recomendaciones = recomendaciones,
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
