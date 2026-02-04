package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.etic.data.local.DbProvider
import com.example.etic.data.local.entities.LineaBase
import com.example.etic.reports.pdf.BaselinePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class GenerateBaselinePdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val getInspeccionImagenesTreeUri: (inspectionNumber: String) -> Uri?,
    private val pdfGenerator: BaselinePdfGenerator = BaselinePdfGenerator()
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        currentUserId: String? = null,
        currentUserName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val lineaBaseDao = db.lineaBaseDao()
            val inspeccionDao = db.inspeccionDao()
            val clienteDao = db.clienteDao()
            val sitioDao = db.sitioDao()
            val usuarioDao = db.usuarioDao()
            val ubicacionDao = db.ubicacionDao()
            val tipoPrioridadDao = db.tipoPrioridadDao()
            val inspeccionDetDao = db.inspeccionDetDao()
            val estatusDetDao = db.estatusInspeccionDetDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspeccion no encontrada."))

            val baselinesCurrent = lineaBaseDao.getByInspeccionActivos(inspeccionId)
            if (baselinesCurrent.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No hay baseline en la inspeccion actual.")
                )
            }

            val allBaselines = lineaBaseDao.getAllActivos()
            val inspeccionesById = inspeccionDao.getAll().associateBy { it.idInspeccion }
            val ubicacionesById = ubicacionDao.getAllActivas().associateBy { it.idUbicacion }
            val prioridadById = tipoPrioridadDao.getAll().associateBy { it.idTipoPrioridad }
            val detById = inspeccionDetDao.getByInspeccion(inspeccionId).associateBy { it.idInspeccionDet }
            val estatusById = estatusDetDao.getAll().associateBy { it.idStatusInspeccionDet }

            fun formatDate(rawValue: String?): String {
                val raw = rawValue?.take(10).orEmpty()
                if (raw.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(raw)
            }

            fun formatDateLabel(rawValue: String?): String {
                val txt = rawValue?.replace("T", " ")?.trim().orEmpty()
                if (txt.isBlank()) return ""
                val d = txt.take(10)
                return runCatching {
                    LocalDate.parse(d).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(d)
            }

            fun formatTemp(v: Double?): String = if (v == null) "" else "${"%.1f".format(v)}°C"

            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else {
                null
            } ?: if (!currentUserName.isNullOrBlank()) {
                usuarioDao.getByUsuario(currentUserName)
            } else null

            val fechaAnteriorRaw = runCatching {
                val prevNo = inspeccion.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == prevNo }?.fechaInicio
            }.getOrNull()

            val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val header = BaselineReportHeaderData(
                cliente = inspeccion.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty(),
                sitio = inspeccion.idSitio?.let { sitioDao.getByIdActivo(it)?.sitio }.orEmpty(),
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
            val file = folderProvider.createPdfFile(folder, "ETIC_BASELINE_INSPECCION_$noInspeccion.pdf")
                ?: return@withContext Result.failure(
                    IllegalStateException("No se pudo crear el archivo PDF.")
                )

            val imageFolders = mutableMapOf<String, DocumentFile?>()
            fun getFolderByInspectionNo(inspNo: String): DocumentFile? {
                return imageFolders.getOrPut(inspNo) {
                    val uri = getInspeccionImagenesTreeUri(inspNo) ?: return@getOrPut null
                    DocumentFile.fromTreeUri(context, uri) ?: DocumentFile.fromSingleUri(context, uri)
                }
            }

            data class ImgInfo(val bmp: android.graphics.Bitmap?, val fecha: String, val hora: String)
            fun loadImage(inspNo: String, fileName: String?): ImgInfo {
                if (fileName.isNullOrBlank()) return ImgInfo(null, "", "")
                val folderDoc = getFolderByInspectionNo(inspNo) ?: return ImgInfo(null, "", "")
                val fileDoc = folderDoc.findFile(fileName)
                    ?: folderDoc.listFiles().firstOrNull { it.name.equals(fileName, ignoreCase = true) }
                    ?: return ImgInfo(null, "", "")

                val bmp = runCatching {
                    context.contentResolver.openInputStream(fileDoc.uri)?.use { BitmapFactory.decodeStream(it) }
                }.getOrNull()

                val millis = fileDoc.lastModified()
                val dateTime = if (millis > 0L) {
                    val dt = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) to
                        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
                } else "" to ""

                return ImgInfo(bmp, dateTime.first, dateTime.second)
            }

            fun inspectionNoOf(lb: LineaBase): Int? =
                lb.idInspeccion?.let { inspeccionesById[it]?.noInspeccion }

            val pages = baselinesCurrent.sortedWith(
                compareBy<LineaBase>(
                    { it.idUbicacion ?: "ZZZ" },
                    { inspectionNoOf(it) ?: Int.MAX_VALUE }
                )
            ).map { row ->
                val ubId = row.idUbicacion
                val ubic = ubId?.let { ubicacionesById[it] }
                val tipoPrioridad = ubic?.idTipoPrioridad
                    ?.let { prioridadById[it]?.tipoPrioridad }
                    .orEmpty()
                val fabricante = ubic?.fabricante.orEmpty()
                val ruta = row.ruta ?: ubic?.ruta.orEmpty()
                val inspNo = inspectionNoOf(row)?.toString().orEmpty()

                val ir = loadImage(inspNo, row.archivoIr)
                val id = loadImage(inspNo, row.archivoId)

                val history = allBaselines
                    .asSequence()
                    .filter { it.idUbicacion == ubId }
                    .sortedBy { inspectionNoOf(it) ?: Int.MIN_VALUE }
                    .toList()

                val historyRows = history.map { h ->
                    val det = h.idInspeccionDet?.let { detById[it] }
                    val estatus = det?.idStatusInspeccionDet
                        ?.let { estatusById[it]?.estatusInspeccionDet }
                        .orEmpty()
                    BaselineHistoryRow(
                        noInspeccion = inspectionNoOf(h)?.toString().orEmpty(),
                        fechaInspeccion = formatDate(
                            h.idInspeccion?.let { inspeccionesById[it]?.fechaInicio }
                        ),
                        estatusInspeccion = estatus,
                        mta = formatTemp(h.mta),
                        tempMax = formatTemp(h.tempMax),
                        tempAmb = formatTemp(h.tempAmb),
                        notas = h.notas.orEmpty()
                    )
                }

                val graphPoints = history.map { h ->
                    BaselineGraphPoint(
                        label = formatDateLabel(h.idInspeccion?.let { inspeccionesById[it]?.fechaInicio }),
                        noInspeccion = inspectionNoOf(h),
                        mta = h.mta,
                        tempMax = h.tempMax,
                        tempAmb = h.tempAmb
                    )
                }.sortedBy { it.noInspeccion ?: Int.MIN_VALUE }

                BaselineReportPageData(
                    codigoBarras = ubic?.codigoBarras.orEmpty(),
                    fabricante = fabricante,
                    prioridadOperacion = tipoPrioridad,
                    ruta = ruta,
                    archivoIr = row.archivoIr.orEmpty(),
                    archivoId = row.archivoId.orEmpty(),
                    irBitmap = ir.bmp,
                    idBitmap = id.bmp,
                    irFecha = ir.fecha,
                    irHora = ir.hora,
                    idFecha = id.fecha,
                    idHora = id.hora,
                    historyRows = historyRows,
                    graphPoints = graphPoints
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
