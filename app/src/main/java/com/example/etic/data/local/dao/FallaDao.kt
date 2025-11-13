package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Falla

@Dao
interface FallaDao {
    @Query("SELECT * FROM fallas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Falla>

    @Query("SELECT * FROM fallas WHERE Id_Falla = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Falla?
}

