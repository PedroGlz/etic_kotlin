package com.example.etic.reports

import android.content.Context
import android.graphics.BitmapFactory
import com.example.etic.data.local.DbProvider
import com.example.etic.reports.pdf.InventarioPdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GenerateInventarioPdfUseCase(
    private val context: Context,
    private val folderProvider: ReportesFolderProvider,
    private val pdfGenerator: InventarioPdfGenerator = InventarioPdfGenerator()
) {
    suspend fun run(noInspeccion: String, inspeccionId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val db = DbProvider.get(context)
            val ubicacionDao = db.ubicacionDao()
            val inspeccionDetDao = db.inspeccionDetDao()
            val inspeccionDao = db.inspeccionDao()

            val inspeccion = inspeccionDao.getById(inspeccionId)
            val sitio = inspeccion?.idSitio ?: ""

            val ubicaciones = ubicacionDao.getAllActivas()
            val dets = inspeccionDetDao.getByInspeccion(inspeccionId)

            val detByUb = dets.mapNotNull { d -> d.idUbicacion?.let { it to d } }.toMap()

            val rows = ubicaciones.map { u ->
                val det = detByUb[u.idUbicacion]
                InventoryRow(
                    ruta = u.ruta ?: u.ubicacion ?: "",
                    ubicacion = u.ubicacion ?: "",
                    codigoBarras = u.codigoBarras ?: "",
                    estatus = det?.idStatusInspeccionDet ?: "Por verificar",
                    esEquipo = (u.esEquipo ?: "").equals("SI", ignoreCase = true)
                )
            }.sortedBy { it.ruta }

            val folder = folderProvider.getReportesFolder(noInspeccion)
                ?: return@withContext Result.failure(IllegalStateException("No hay acceso a carpeta Reportes (SAF)."))

            val file = folderProvider.createPdfFile(folder, "Reporte_Inventarios_$noInspeccion.pdf")
                ?: return@withContext Result.failure(IllegalStateException("No se pudo crear el archivo PDF."))

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
                    noInspeccion = noInspeccion,
                    sitio = sitio,
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
