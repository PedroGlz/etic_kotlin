package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Problema

@Dao
interface ProblemaDao {

    @Query("SELECT * FROM problemas")
    suspend fun getAll(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion")
    suspend fun getByInspeccion(idInspeccion: String): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion AND Estatus = 'Activo'")
    suspend fun getByInspeccionActivos(idInspeccion: String): List<Problema>

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
}

data class VisualProblemHistoryRow(
    val numero: Int?,
    val numeroInspeccion: Int?,
    val fecha: String?,
    val severidad: String?,
    val comentario: String?
)
