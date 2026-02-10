package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.InspeccionDet

@Dao
interface InspeccionDetDao {
    @Query("SELECT * FROM inspecciones_det")
    suspend fun getAll(): List<InspeccionDet>

    @Query("SELECT * FROM inspecciones_det WHERE Id_Inspeccion = :inspectionId")
    suspend fun getByInspeccion(inspectionId: String): List<InspeccionDet>

    @Query("SELECT * FROM inspecciones_det WHERE Id_Ubicacion = :ubicacionId")
    suspend fun getByUbicacion(ubicacionId: String): List<InspeccionDet>

    @Update
    suspend fun update(item: InspeccionDet)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InspeccionDet)

    @Query(
        """
        UPDATE inspecciones_det
        SET Estatus = 'Inactivo',
            Modificado_Por = :userId,
            Fecha_Mod = :timestamp
        WHERE Id_Ubicacion = :ubicacionId
        """
    )
    suspend fun markInactiveByUbicacion(ubicacionId: String, userId: String?, timestamp: String)

    @Query("UPDATE inspecciones_det SET selected = '0' WHERE Id_Inspeccion = :inspectionId")
    suspend fun clearSelectedByInspeccion(inspectionId: String)

    @Query(
        """
        UPDATE inspecciones_det
        SET selected = '1'
        WHERE Id_Inspeccion = :inspectionId AND Id_Ubicacion = :ubicacionId
        """
    )
    suspend fun setSelectedByUbicacion(inspectionId: String, ubicacionId: String)

    suspend fun updateSelectedByUbicacion(inspectionId: String, ubicacionId: String) {
        clearSelectedByInspeccion(inspectionId)
        setSelectedByUbicacion(inspectionId, ubicacionId)
    }

    @Query(
        """
        UPDATE inspecciones_det
        SET expanded = :expanded
        WHERE Id_Inspeccion = :inspectionId AND Id_Ubicacion = :ubicacionId
        """
    )
    suspend fun updateExpandedByUbicacion(inspectionId: String, ubicacionId: String, expanded: String)

    @Query(
        """
        SELECT
            insdet.Id_Inspeccion_Det AS idInspeccionDet,
            insdet.Id_Inspeccion AS idInspeccion,
            insp.No_Inspeccion AS numInspeccion,
            insp.Fecha_Inicio AS fechaCreacionInspeccion,
            eid.Estatus_Inspeccion_Det AS estatusUbicacion,
            insdet.Notas_Inspeccion AS notasInspeccion
        FROM inspecciones_det AS insdet
        JOIN inspecciones AS insp ON insdet.Id_Inspeccion = insp.Id_Inspeccion
        LEFT JOIN estatus_inspeccion_det AS eid
            ON insdet.Id_Status_Inspeccion_Det = eid.Id_Status_Inspeccion_Det
        WHERE insdet.Id_Ubicacion = :ubicacionId
        ORDER BY insp.No_Inspeccion DESC
        """
    )
    suspend fun getHistorialInspeccionesByUbicacion(ubicacionId: String): List<HistorialInspeccionRow>
}

data class HistorialInspeccionRow(
    val idInspeccionDet: String? = null,
    val idInspeccion: String? = null,
    val numInspeccion: Int? = null,
    val fechaCreacionInspeccion: String? = null,
    val estatusUbicacion: String? = null,
    val notasInspeccion: String? = null
)
