package com.example.etic.reports

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.etic.data.local.DbProvider
import com.example.etic.reports.pdf.ProblemasPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.roundToInt

class GenerateProblemasPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val getInspeccionImagenesTreeUri: (inspectionNumber: String) -> Uri?,
    private val pdfGenerator: ProblemasPdfGenerator = ProblemasPdfGenerator()
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        selectedProblemaIds: List<String>,
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
            val tipoInspeccionDao = db.tipoInspeccionDao()
            val ubicacionDao = db.ubicacionDao()
            val tipoPrioridadDao = db.tipoPrioridadDao()
            val severidadDao = db.severidadDao()
            val faseDao = db.faseDao()
            val fabricanteDao = db.fabricanteDao()
            val tipoAmbienteDao = db.tipoAmbienteDao()
            val fallaDao = db.fallaDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspeccion no encontrada."))

            val selectedSet = selectedProblemaIds.toSet()
            val problemas = problemaDao.getByInspeccionActivos(inspeccionId)
                .asSequence()
                .filter { it.estatusProblema.equals("Abierto", ignoreCase = true) }
                .filter { selectedSet.isEmpty() || it.idProblema in selectedSet }
                .sortedWith(
                    compareBy(
                        { it.idTipoInspeccion ?: "ZZZ" },
                        { it.numeroProblema ?: Int.MAX_VALUE }
                    )
                )
                .toList()

            if (problemas.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No hay problemas abiertos para generar el reporte.")
                )
            }

            val tipoInspeccionById = tipoInspeccionDao.getAll().associateBy { it.idTipoInspeccion }
            val ubicacionById = ubicacionDao.getAllActivas().associateBy { it.idUbicacion }
            val prioridadById = tipoPrioridadDao.getAll().associateBy { it.idTipoPrioridad }
            val severidadById = severidadDao.getAll().associateBy { it.idSeveridad }
            val faseById = faseDao.getAllActivos().associateBy { it.idFase }
            val fabricanteById = fabricanteDao.getAllActivos().associateBy { it.idFabricante }
            val ambienteById = tipoAmbienteDao.getAllActivos().associateBy { it.idTipoAmbiente }
            val fallaById = fallaDao.getAllActivos().associateBy { it.idFalla }

            val cliente = inspeccion.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty()
            val sitio = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it)?.sitio }.orEmpty()
            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else {
                null
            } ?: if (!currentUserName.isNullOrBlank()) {
                usuarioDao.getByUsuario(currentUserName)
            } else {
                null
            }

            fun formatDate(rawValue: String?): String {
                val raw = rawValue?.take(10).orEmpty()
                if (raw.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(raw)
            }

            val fechaAnteriorRaw = runCatching {
                val prevNo = inspeccion.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == prevNo }?.fechaInicio
            }.getOrNull()

            val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val header = ProblemReportHeaderData(
                cliente = cliente,
                sitio = sitio,
                analista = currentUserName?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.nombre?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.usuario.orEmpty(),
                nivel = usuarioActual?.nivelCertificacion.orEmpty(),
                inspeccionAnterior = inspeccion.noInspeccionAnt?.toString().orEmpty(),
                fechaAnterior = formatDate(fechaAnteriorRaw),
                inspeccionActual = inspeccion.noInspeccion?.toString().orEmpty(),
                fechaActual = formatDate(inspeccion.fechaInicio),
                fechaReporte = now
            )

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(
                    IllegalStateException("No hay acceso a carpeta Reportes (SAF).")
                )

            val file = folderProvider.createPdfFile(
                folder,
                "ETIC_PROBLEMAS_INSPECCION_$noInspeccion.pdf"
            ) ?: return@withContext Result.failure(
                IllegalStateException("No se pudo crear el archivo PDF.")
            )

            val imageFolder = getInspeccionImagenesTreeUri(noInspeccion)?.let { treeUri ->
                DocumentFile.fromTreeUri(context, treeUri) ?: DocumentFile.fromSingleUri(context, treeUri)
            }

            fun findImage(folderDoc: DocumentFile?, name: String?): Bitmap? {
                if (folderDoc == null || name.isNullOrBlank()) return null
                val target = folderDoc.findFile(name)
                    ?: folderDoc.listFiles().firstOrNull { it.name.equals(name, ignoreCase = true) }
                    ?: return null
                return runCatching {
                    context.contentResolver.openInputStream(target.uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }

            fun calcAjusteViento(problemTemp: Double?, ambientTemp: Double?, windSpeed: Double?): Int? {
                val tempProb = problemTemp ?: return null
                val tempAmb = ambientTemp ?: return null
                val vel = windSpeed ?: return tempProb.roundToInt()
                val fcv = when (vel.roundToInt()) {
                    1 -> 1.15
                    2 -> 1.36
                    3 -> 1.64
                    4 -> 1.86
                    5 -> 2.06
                    6 -> 2.23
                    7 -> 2.40
                    else -> 2.40
                }
                return (((tempProb - tempAmb) * fcv) + tempAmb).roundToInt()
            }

            fun calcAjusteCarga(
                ajusteViento: Int?,
                ambientTemp: Double?,
                problemRms: Double?,
                referenceRms: Double?,
                additionalRms: Double?,
                ratedLoad: String?
            ): Int? {
                val viento = ajusteViento ?: return null
                val amb = ambientTemp ?: return null
                val nominal = ratedLoad?.toDoubleOrNull() ?: return null
                if (nominal < 1) return null
                val maxRms = max(problemRms ?: 0.0, max(referenceRms ?: 0.0, additionalRms ?: 0.0))
                val porcentaje = (maxRms / nominal) * 100.0
                val bucket = ((porcentaje / 10.0).roundToInt() * 10)
                val fcc = when {
                    bucket < 40 -> 0.0
                    bucket == 40 -> 4.33
                    bucket == 50 -> 2.98
                    bucket == 60 -> 2.27
                    bucket == 70 -> 1.77
                    bucket == 80 -> 1.46
                    bucket == 90 -> 1.20
                    else -> 4.33
                }
                return (((viento - amb) * fcc) + amb).roundToInt()
            }

            fun fmt(value: Double?, suffix: String = "", decimals: Int = 1): String {
                if (value == null) return ""
                val txt = "%.${decimals}f".format(value)
                return if (suffix.isBlank()) txt else "$txt $suffix"
            }

            val pages = problemas.map { problem ->
                val ubicacion = problem.idUbicacion?.let { ubicacionById[it] }
                val tipo = problem.idTipoInspeccion?.let { tipoInspeccionById[it]?.tipoInspeccion }.orEmpty()
                val prioridadOperacion = ubicacion?.idTipoPrioridad
                    ?.let { prioridadById[it]?.tipoPrioridad }
                    .orEmpty()
                val prioridadReparacion = problem.idSeveridad
                    ?.let { severidadById[it]?.severidad }
                    .orEmpty()
                val faseProblema = problem.problemPhase?.let { faseById[it]?.nombreFase }.orEmpty()
                val faseRef = problem.referencePhase?.let { faseById[it]?.nombreFase }.orEmpty()
                val faseAdicional = problem.additionalInfo?.let { faseById[it]?.nombreFase }.orEmpty()
                val tipoAmbiente = problem.environment?.let { ambienteById[it]?.nombre }.orEmpty()
                val fabricante = problem.idFabricante?.let { fabricanteById[it]?.fabricante }.orEmpty()
                val hallazgoVisual = sequenceOf(
                    problem.hazardIssue?.let { fallaById[it]?.falla },
                    problem.hazardGroup?.let { fallaById[it]?.falla },
                    problem.hazardClassification?.let { fallaById[it]?.falla },
                    problem.hazardType?.let { fallaById[it]?.falla }
                ).firstOrNull { !it.isNullOrBlank() }.orEmpty()

                val ajusteViento = calcAjusteViento(
                    problemTemp = problem.problemTemperature,
                    ambientTemp = problem.tempAmbient,
                    windSpeed = problem.windSpeed
                )
                val ajusteCarga = calcAjusteCarga(
                    ajusteViento = ajusteViento,
                    ambientTemp = problem.tempAmbient,
                    problemRms = problem.problemRms,
                    referenceRms = problem.referenceRms,
                    additionalRms = problem.additionalRms,
                    ratedLoad = problem.ratedLoad
                )

                val typeTag = when (problem.idTipoInspeccion) {
                    ProblemTypeIds.ELECTRICO, ProblemTypeIds.ELECTRICO_2 -> "E"
                    ProblemTypeIds.VISUAL -> "V"
                    else -> "M"
                }

                ProblemReportPageData(
                    idProblema = problem.idProblema,
                    tipoInspeccionId = problem.idTipoInspeccion,
                    tipoInspeccion = tipo,
                    numeroProblema = problem.numeroProblema,
                    tipoProblemaTag = typeTag,
                    esCronico = problem.esCronico.orEmpty(),
                    prioridadOperacion = prioridadOperacion,
                    prioridadReparacion = prioridadReparacion,
                    fechaReporte = now,
                    hallazgoVisual = hallazgoVisual,
                    observaciones = problem.componentComment.orEmpty(),
                    temperaturaAnomalia = fmt(problem.problemTemperature, "C"),
                    temperaturaReferencia = fmt(problem.referenceTemperature, "C"),
                    diferencialTemperatura = fmt(problem.aumentoTemperatura, "C"),
                    temperaturaAmbiente = fmt(problem.tempAmbient, "C"),
                    tipoAmbiente = tipoAmbiente,
                    velocidadViento = fmt(problem.windSpeed, "m/s"),
                    ajusteViento = ajusteViento?.let { "$it C" }.orEmpty(),
                    ajusteCarga = ajusteCarga?.let { "$it C" }.orEmpty(),
                    fabricante = fabricante,
                    voltajeCircuito = problem.circuitVoltage?.takeIf { it.isNotBlank() }?.let { "$it V" }.orEmpty(),
                    corrienteNominal = problem.ratedLoad?.takeIf { it.isNotBlank() }?.let { "$it A" }.orEmpty(),
                    faseProblema = faseProblema,
                    faseReferencia = faseRef,
                    faseAdicional = faseAdicional,
                    rmsProblema = fmt(problem.problemRms, ""),
                    rmsReferencia = fmt(problem.referenceRms, ""),
                    rmsAdicional = fmt(problem.additionalRms, ""),
                    emisividad = fmt(problem.emissivity, ""),
                    codigoBarras = ubicacion?.codigoBarras.orEmpty(),
                    ruta = problem.ruta ?: ubicacion?.ruta.orEmpty(),
                    irFileName = problem.irFile.orEmpty(),
                    irFileDate = problem.irFileDate.orEmpty(),
                    irFileTime = problem.irFileTime.orEmpty(),
                    photoFileName = problem.photoFile.orEmpty(),
                    photoFileDate = problem.photoFileDate.orEmpty(),
                    photoFileTime = problem.photoFileTime.orEmpty(),
                    irBitmap = findImage(imageFolder, problem.irFile),
                    photoBitmap = findImage(imageFolder, problem.photoFile)
                )
            }

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg)
                .takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                pdfGenerator.generate(
                    output = out,
                    header = header,
                    pages = pages,
                    logo = logoBmp
                )
            } ?: return@withContext Result.failure(
                IllegalStateException("No se pudo abrir OutputStream del SAF.")
            )

            Result.success(file.uri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
