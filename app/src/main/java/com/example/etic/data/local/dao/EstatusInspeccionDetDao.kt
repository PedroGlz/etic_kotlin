package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.EstatusInspeccionDet

@Dao
interface EstatusInspeccionDetDao {
    @Query("SELECT * FROM estatus_inspeccion_det WHERE Estatus = 'Activo'")
    suspend fun getAll(): List<EstatusInspeccionDet>

    @Query("SELECT * FROM estatus_inspeccion_det WHERE Id_Status_Inspeccion_Det = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getById(id: String): EstatusInspeccionDet?
}
