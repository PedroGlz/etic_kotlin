package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.DatosReporte

@Dao
interface DatosReporteDao {
    @Query(
        """
        SELECT * FROM datos_reporte
        WHERE Id_Inspeccion = :inspectionId
        ORDER BY Id_Datos_Reporte DESC
        LIMIT 1
        """
    )
    suspend fun getLatestByInspection(inspectionId: String): DatosReporte?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DatosReporte): Long

    @Update
    suspend fun update(item: DatosReporte)
}
