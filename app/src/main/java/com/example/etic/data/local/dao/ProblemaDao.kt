package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.Problema

@Dao
interface ProblemaDao {

    @Insert
    suspend fun insert(problema: Problema)

    @Update
    suspend fun update(problema: Problema)

    @Query("SELECT * FROM problemas WHERE Id_Problema = :problemId LIMIT 1")
    suspend fun getById(problemId: String): Problema?

    @Query("SELECT * FROM problemas")
    suspend fun getAll(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Problema>

    @Query(
        """
        SELECT * FROM problemas
        WHERE Id_Sitio = :siteId
          AND Estatus = 'Activo'
          AND Id_Problema NOT IN (
            SELECT Id_Problema FROM problemas
            WHERE Es_Cronico = 'SI'
              AND Estatus_Problema = 'Cerrado'
              AND Estatus = 'Activo'
          )
        ORDER BY Fecha_Creacion ASC
        """
    )
    suspend fun getActivosPorSitio(siteId: String): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion")
    suspend fun getByInspeccion(idInspeccion: String): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion AND Estatus = 'Activo'")
    suspend fun getByInspeccionActivos(idInspeccion: String): List<Problema>

    @Query(
        """
        SELECT * FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Tipo_Inspeccion = :idTipoInspeccion
          AND Estatus = 'Activo'
        ORDER BY Numero_Problema ASC
        """
    )
    suspend fun getActivosByInspeccionAndTipo(
        idInspeccion: String,
        idTipoInspeccion: String
    ): List<Problema>

    @Query(
        """
        SELECT COUNT(*) FROM problemas
        WHERE Id_Inspeccion_Det = :idInspeccionDet
          AND Estatus = 'Activo'
        """
    )
    suspend fun countActivosByInspeccionDet(idInspeccionDet: String): Int

    @Query(
        """
        SELECT COUNT(*) FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Ubicacion = :idUbicacion
          AND Estatus = 'Activo'
        """
    )
    suspend fun countActivosByInspeccionAndUbicacion(
        idInspeccion: String,
        idUbicacion: String
    ): Int

    @Query(
        """
        SELECT Numero_Problema FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Tipo_Inspeccion = :idTipoInspeccion
          AND Estatus = 'Activo'
        ORDER BY Numero_Problema DESC
        LIMIT 1
        """
    )
    suspend fun getLastNumberByInspectionAndType(
        idInspeccion: String,
        idTipoInspeccion: String
    ): Int?

    @Query(
        """
        SELECT 
            p.Numero_Problema AS numero,
            i.No_Inspeccion AS numeroInspeccion,
            p.Fecha_Creacion AS fecha,
            s.Severidad AS severidad,
            p.Component_Comment AS comentario
        FROM problemas p
        LEFT JOIN inspecciones i ON i.Id_Inspeccion = p.Id_Inspeccion
        LEFT JOIN severidades s ON s.Id_Severidad = p.Id_Severidad
        WHERE p.Id_Ubicacion = :ubicacionId
          AND p.Id_Tipo_Inspeccion = :tipoInspeccionId
          AND p.Estatus = 'Activo'
        ORDER BY i.No_Inspeccion DESC, p.Fecha_Creacion DESC
        """
    )
    suspend fun getVisualHistoryFor(
        ubicacionId: String,
        tipoInspeccionId: String
    ): List<VisualProblemHistoryRow>

    @Query(
        """
        SELECT Ir_File FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Tipo_Inspeccion = :idTipoInspeccion
          AND Ir_File IS NOT NULL
          AND Ir_File != ''
        ORDER BY Fecha_Creacion DESC
        LIMIT 1
        """
    )
    suspend fun getLastThermalImageName(
        idInspeccion: String,
        idTipoInspeccion: String
    ): String?

    @Query(
        """
        SELECT Photo_File FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Tipo_Inspeccion = :idTipoInspeccion
          AND Photo_File IS NOT NULL
          AND Photo_File != ''
        ORDER BY Fecha_Creacion DESC
        LIMIT 1
        """
    )
    suspend fun getLastDigitalImageName(
        idInspeccion: String,
        idTipoInspeccion: String
    ): String?

    @Query(
        """
        SELECT 
            p.Problem_Temperature AS problemTemperature,
            p.Reference_Temperature AS referenceTemperature,
            p.Fecha_Creacion AS fechaCreacion,
            i.No_Inspeccion AS noInspeccion
        FROM problemas p
        LEFT JOIN inspecciones i ON i.Id_Inspeccion = p.Id_Inspeccion
        WHERE p.Id_Ubicacion = :ubicacionId
          AND p.Id_Tipo_Inspeccion = :tipoInspeccionId
          AND p.Es_Cronico = 'SI'
          AND p.Estatus = 'Activo'
        ORDER BY i.No_Inspeccion DESC
        """
    )
    suspend fun getGraphHistoryFor(
        ubicacionId: String,
        tipoInspeccionId: String
    ): List<ProblemGraphHistoryRow>
}

data class VisualProblemHistoryRow(
    val numero: Int?,
    val numeroInspeccion: Int?,
    val fecha: String?,
    val severidad: String?,
    val comentario: String?
)

data class ProblemGraphHistoryRow(
    val problemTemperature: Double?,
    val referenceTemperature: Double?,
    val fechaCreacion: String?,
    val noInspeccion: Int?
)
