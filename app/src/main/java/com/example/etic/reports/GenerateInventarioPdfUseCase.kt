package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import com.example.etic.data.local.entities.Problema
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.etic.data.local.DbProvider
import com.example.etic.reports.pdf.InventarioPdfGenerator
import com.example.etic.reports.InventoryHeaderData
import com.example.etic.reports.InventoryReportRow
import com.example.etic.reports.ProblemTypeIds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenerateInventarioPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val pdfGenerator: InventarioPdfGenerator = InventarioPdfGenerator()
) {
    suspend fun run(
        noInspeccion: String,
        inspeccionId: String,
        selectedUbicacionIds: List<String>,
        currentUserId: String? = null,
        currentUserName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val ubicacionDao = db.ubicacionDao()
            val inspeccionDetDao = db.inspeccionDetDao()
            val inspeccionDao = db.inspeccionDao()
            val clienteDao = db.clienteDao()
            val sitioDao = db.sitioDao()
            val tipoPrioridadDao = db.tipoPrioridadDao()
            val estatusDetDao = db.estatusInspeccionDetDao()
            val problemaDao = db.problemaDao()
            val usuarioDao = db.usuarioDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
            val sitio = inspeccion?.idSitio

            val ubicacionesById = ubicacionDao.getAllActivas().associateBy { it.idUbicacion }
            val ubicaciones = if (selectedUbicacionIds.isEmpty()) {
                ubicacionesById.values.sortedBy { it.ruta ?: it.ubicacion ?: "" }
            } else {
                // Igual que PHP: respetar el orden recibido en arrayElementosParaReporte.
                selectedUbicacionIds.mapNotNull { ubicacionesById[it] }
            }
            val dets = inspeccionDetDao.getByInspeccion(inspeccionId)

            val detByUb = dets.mapNotNull { d -> d.idUbicacion?.let { it to d } }.toMap()
            val prioridadById = tipoPrioridadDao.getAll().associateBy { it.idTipoPrioridad }
            val estatusById = estatusDetDao.getAll().associateBy { it.idStatusInspeccionDet }
            val problemasActivos = problemaDao.getByInspeccionActivos(inspeccionId)

            fun formatProblema(p: Problema): String {
                val prefijo = when (p.idTipoInspeccion) {
                    ProblemTypeIds.ELECTRICO, ProblemTypeIds.ELECTRICO_2 -> "E"
                    ProblemTypeIds.VISUAL -> "V"
                    else -> "M"
                }
                val numero = p.numeroProblema?.toString().orEmpty()
                return if (numero.isBlank()) prefijo else "$prefijo $numero"
            }

            val problemasPorUbicacion: Map<String, String> = problemasActivos
                .asSequence()
                .filter { !it.idUbicacion.isNullOrBlank() }
                .groupBy { it.idUbicacion!! }
                .mapValues { (_, lista) ->
                    lista.sortedWith(
                        compareBy<Problema>(
                            { it.idTipoInspeccion ?: "ZZZ" },
                            { it.numeroProblema ?: Int.MAX_VALUE }
                        )
                    ).joinToString(",") { formatProblema(it) }
                }

            val rows = ubicaciones.map { u ->
                val det = detByUb[u.idUbicacion]
                val estatus = det?.idStatusInspeccionDet
                    ?.let { estatusById[it]?.estatusInspeccionDet }
                    ?: "Por verificar"
                val prioridad = u.idTipoPrioridad
                    ?.let { prioridadById[it]?.tipoPrioridad }
                    ?: ""
                val problemas = u.idUbicacion?.let { problemasPorUbicacion[it].orEmpty() }.orEmpty()
                InventoryReportRow(
                    estatus = estatus,
                    prioridad = prioridad,
                    problemas = problemas,
                    elemento = u.ubicacion ?: "",
                    codigoBarras = u.codigoBarras ?: "",
                    notas = det?.notasInspeccion ?: "",
                    level = u.nivelArbol ?: 1
                )
            }

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(IllegalStateException("No hay acceso a carpeta Reportes (SAF)."))

            val file = folderProvider.createPdfFile(
                folder,
                "ETIC_INVENTARIO_INSPECCION_$noInspeccion.pdf"
            )
                ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el archivo PDF."))

            val res = context.resources
            val pkg = context.packageName
            val logoId = res.getIdentifier("ETIC_logo", "drawable", pkg)
                .takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo", "drawable", pkg).takeIf { it != 0 }
                ?: res.getIdentifier("etic_logo_login", "drawable", pkg).takeIf { it != 0 }
            val logoBmp = logoId?.let { BitmapFactory.decodeResource(res, it) }

            val cliente = inspeccion?.idCliente?.let { clienteDao.getByIdActivo(it)?.razonSocial }.orEmpty()
            val sitioNombre = sitio?.let { sitioDao.getByIdActivo(it)?.sitio }.orEmpty()
            val usuarioActual = if (!currentUserId.isNullOrBlank()) {
                usuarioDao.getById(currentUserId)
            } else {
                null
            } ?: if (!currentUserName.isNullOrBlank()) {
                usuarioDao.getByUsuario(currentUserName)
            } else null

            fun formatDate(rawValue: String?): String {
                val raw = rawValue?.take(10).orEmpty()
                if (raw.isBlank()) return ""
                return runCatching {
                    LocalDate.parse(raw).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                }.getOrDefault(raw)
            }

            val fechaAnteriorRaw = runCatching {
                val prevNo = inspeccion?.noInspeccionAnt ?: return@runCatching null
                inspeccionDao.getAll().firstOrNull { it.noInspeccion == prevNo }?.fechaInicio
            }.getOrNull()

            val now = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            val header = InventoryHeaderData(
                cliente = cliente,
                sitio = sitioNombre,
                analista = currentUserName?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.nombre?.takeIf { it.isNotBlank() }
                    ?: usuarioActual?.usuario.orEmpty(),
                nivel = usuarioActual?.nivelCertificacion.orEmpty(),
                inspeccionAnterior = inspeccion?.noInspeccionAnt?.toString() ?: "",
                fechaAnterior = formatDate(fechaAnteriorRaw),
                inspeccionActual = inspeccion?.noInspeccion?.toString() ?: "",
                fechaActual = formatDate(inspeccion?.fechaInicio),
                fechaReporte = now
            )

            context.contentResolver.openOutputStream(file.uri)?.use { out ->
                pdfGenerator.generate(
                    output = out,
                    header = header,
                    rows = rows,
                    logo = logoBmp
                )
            } ?: return@withContext Result.failure(IllegalStateException("No se pudo abrir OutputStream del SAF."))

            Result.success(file.uri.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
