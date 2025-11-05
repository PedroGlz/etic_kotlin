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
}
