package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import com.example.etic.data.local.DbProvider
import com.example.etic.data.local.entities.Problema
import com.example.etic.reports.pdf.ProblemListPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GenerateProblemListPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val pdfGenerator: ProblemListPdfGenerator = ProblemListPdfGenerator()
) {
    enum class ProblemListType { ABIERTOS, CERRADOS }

    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        listType: ProblemListType,
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
            val severidadDao = db.severidadDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
                ?: return@withContext Result.failure(IllegalStateException("Inspección no encontrada."))

            val siteId = inspeccion.idSitio.orEmpty()
            val all = problemaDao.getAllActivos().filter { it.idSitio == siteId }
            val filtered = when (listType) {
                ProblemListType.ABIERTOS -> all.filter { it.estatusProblema.equals("Abierto", true) }
                ProblemListType.CERRADOS -> all.filter {
                    it.estatusProblema.equals("Cerrado", true) && it.cerradoEnInspeccion == inspeccionId
                }
            }

            val tipoById = tipoInspeccionDao.getAll().associateBy { it.idTipoInspeccion }
            val sevById = severidadDao.getAll().associateBy { it.idSeveridad }
            val inspeccionesById = inspeccionDao.getAll().associateBy { it.idInspeccion }
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
                    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(value)
            }

            fun formatTemp(v: Double?): String = if (v == null) "" else "${"%.1f".format(v)}°C"

            fun typeAndNo(p: Problema): String {
                val type = p.idTipoInspeccion?.let { tipoById[it]?.tipoInspeccion }
                    ?: typeLabelForId(p.idTipoInspeccion)
                    ?: ""
                val number = p.numeroProblema?.toString().orEmpty()
                val prefix = when {
                    type.equals("Aislamiento Térmico", ignoreCase = true) -> "Ais. Térm."
                    type.equals("Aisl. Térm.", ignoreCase = true) -> "Ais. Térm."
                    type.equals("Aisl. Térmico", ignoreCase = true) -> "Ais. Térm."
                    else -> type
                }
                return "$prefix $number".trim()
            }

            val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val prevDate = runCatching {
                val previousInspectionNo = inspeccion.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == previousInspectionNo }?.fechaInicio
            }.getOrNull()

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

            val rows = filtered.sortedWith(
                compareByDescending<Problema> { inspeccionesById[it.idInspeccion]?.noInspeccion ?: -1 }
                    .thenBy { it.idTipoInspeccion ?: "" }
                    .thenBy { it.numeroProblema ?: Int.MAX_VALUE }
            ).map { problema ->
                ProblemListRow(
                    equipoComentarios = "Equipo: ${(problema.ruta ?: "").ifBlank { "-" }}\nComentarios: ${problema.componentComment.orEmpty()}",
                    fechaCreacion = formatDate(problema.fechaCreacion),
                    noInspeccion = inspeccionesById[problema.idInspeccion]?.noInspeccion?.toString().orEmpty(),
                    tipoNumero = typeAndNo(problema),
                    estatusProblema = problema.estatusProblema.orEmpty(),
                    esCronico = problema.esCronico.orEmpty(),
                    temperaturaProblema = formatTemp(problema.problemTemperature),
                    deltaT = formatTemp(problema.aumentoTemperatura),
                    severidad = problema.idSeveridad?.let { sevById[it]?.severidad }.orEmpty()
                )
            }

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(
                    IllegalStateException("No hay acceso a carpeta Reportes (SAF).")
                )
            val fileName = when (listType) {
                ProblemListType.ABIERTOS -> "ETIC_LISTA_PROBLEMAS_ABIERTOS_INSPECCION_$noInspeccion.pdf"
                ProblemListType.CERRADOS -> "ETIC_LISTA_PROBLEMAS_CERRADOS_INSPECCION_$noInspeccion.pdf"
            }
            val file = folderProvider.createPdfFile(folder, fileName)
                ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el PDF."))

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }

            val title = when (listType) {
                ProblemListType.ABIERTOS -> "Lista De Todos Los Problemas Abiertos"
                ProblemListType.CERRADOS -> "Lista De Problemas Cerrados En La Inspección Actual"
            }
            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                pdfGenerator.generate(out, header, title, rows, logoBmp)
            } ?: return@withContext Result.failure(IllegalStateException("No se pudo abrir OutputStream."))

            Result.success(file.uri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun typeLabelForId(typeId: String?): String? {
    if (typeId.isNullOrBlank()) return null
    return when {
        typeId.equals("0D32B331-76C3-11D3-82BF-00104BC75DC2", ignoreCase = true) -> "Eléctrico"
        typeId.equals("0D32B332-76C3-11D3-82BF-00104BC75DC2", ignoreCase = true) -> "Eléctrico"
        typeId.equals("0D32B333-76C3-11D3-82BF-00104BC75DC2", ignoreCase = true) -> "Visual"
        typeId.equals("0D32B334-76C3-11D3-82BF-00104BC75DC2", ignoreCase = true) -> "Mecánico"
        typeId.equals("0D32B335-76C3-11D3-82BF-00104BC75DC2", ignoreCase = true) -> "Aislamiento Térmico"
        else -> null
    }
}
