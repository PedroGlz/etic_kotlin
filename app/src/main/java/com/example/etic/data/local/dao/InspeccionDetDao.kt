package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.InspeccionDet

@Dao
interface InspeccionDetDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: InspeccionDet)

    @Query("SELECT * FROM inspecciones_det WHERE Id_Ubicacion = :idUbicacion")
    suspend fun getByUbicacion(idUbicacion: String): List<InspeccionDet>

    @Query("SELECT * FROM inspecciones_det WHERE Id_Inspeccion = :idInspeccion AND Estatus = 'Activo'")
    suspend fun getByInspeccion(idInspeccion: String): List<InspeccionDet>

    @Update
    suspend fun update(row: InspeccionDet)

    @Query(
        """
        UPDATE inspecciones_det
        SET Estatus = 'Inactivo',
            Modificado_Por = :userId,
            Fecha_Mod = :timestamp
        WHERE Id_Ubicacion = :idUbicacion
        """
    )
    suspend fun markInactiveByUbicacion(
        idUbicacion: String,
        userId: String?,
        timestamp: String
    )

    @Query(
        """
        UPDATE inspecciones_det
        SET expanded = :expanded
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Ubicacion = :idUbicacion
        """
    )
    suspend fun updateExpandedByUbicacion(
        idInspeccion: String,
        idUbicacion: String,
        expanded: String
    )

    @Query(
        """
        UPDATE inspecciones_det
        SET selected = CASE
            WHEN Id_Ubicacion = :idUbicacion THEN '1'
            ELSE '0'
        END
        WHERE Id_Inspeccion = :idInspeccion
        """
    )
    suspend fun updateSelectedByUbicacion(
        idInspeccion: String,
        idUbicacion: String
    )

    @Query(
        """
        SELECT
            d.Id_Inspeccion_Det AS idInspeccionDet,
            d.Id_Inspeccion AS idInspeccion,
            d.Id_Ubicacion AS idUbicacion,
            d.Id_Status_Inspeccion_Det AS idStatusInspeccionDet,
            d.Notas_Inspeccion AS notasInspeccion,
            i.No_Inspeccion AS numInspeccion,
            i.Fecha_Creacion AS fechaCreacionInspeccion,
            e.Estatus_Inspeccion_Det AS estatusUbicacion
        FROM inspecciones_det d
        LEFT JOIN inspecciones i ON i.Id_Inspeccion = d.Id_Inspeccion
        LEFT JOIN estatus_inspeccion_det e ON e.Id_Status_Inspeccion_Det = d.Id_Status_Inspeccion_Det
        WHERE d.Id_Ubicacion = :idUbicacion
          AND d.Estatus = 'Activo'
        ORDER BY COALESCE(i.No_Inspeccion, 0) DESC, i.Fecha_Creacion DESC, d.Id_Inspeccion DESC
        """
    )
    suspend fun getHistorialInspeccionesByUbicacion(idUbicacion: String): List<HistorialInspeccionRow>
}

data class HistorialInspeccionRow(
    val idInspeccionDet: String?,
    val idInspeccion: String?,
    val idUbicacion: String?,
    val idStatusInspeccionDet: String?,
    val notasInspeccion: String?,
    val numInspeccion: Int?,
    val fechaCreacionInspeccion: String?,
    val estatusUbicacion: String?
)
