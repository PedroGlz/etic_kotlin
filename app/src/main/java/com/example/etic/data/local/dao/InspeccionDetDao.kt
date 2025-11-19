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

    @Update
    suspend fun update(row: InspeccionDet)

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
}
