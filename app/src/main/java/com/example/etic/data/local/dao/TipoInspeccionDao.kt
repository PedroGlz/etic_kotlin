package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.TipoInspeccion

@Dao
interface TipoInspeccionDao {
    @Query("SELECT * FROM tipo_inspecciones WHERE Estatus = 'Activo'")
    suspend fun getAll(): List<TipoInspeccion>

    @Query("SELECT * FROM tipo_inspecciones WHERE Id_Tipo_Inspeccion = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getById(id: String): TipoInspeccion?
}
