package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.TipoAmbiente

@Dao
interface TipoAmbienteDao {
    @Query("SELECT * FROM tipo_ambientes WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<TipoAmbiente>

    @Query("SELECT * FROM tipo_ambientes WHERE Id_Tipo_Ambiente = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): TipoAmbiente?
}

