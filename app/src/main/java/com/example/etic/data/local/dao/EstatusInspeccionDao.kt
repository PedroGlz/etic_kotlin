package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.EstatusInspeccion

@Dao
interface EstatusInspeccionDao {
    @Query("SELECT * FROM estatus_inspeccion WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<EstatusInspeccion>

    @Query("SELECT * FROM estatus_inspeccion WHERE Id_Status_Inspeccion = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): EstatusInspeccion?
}

