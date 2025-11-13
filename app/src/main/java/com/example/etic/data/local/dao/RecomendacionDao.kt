package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Recomendacion

@Dao
interface RecomendacionDao {
    @Query("SELECT * FROM recomendaciones WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Recomendacion>

    @Query("SELECT * FROM recomendaciones WHERE Id_Recomendacion = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Recomendacion?
}

