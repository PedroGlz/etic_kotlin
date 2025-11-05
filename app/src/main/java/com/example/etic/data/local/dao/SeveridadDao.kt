package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Severidad

@Dao
interface SeveridadDao {
    @Query("SELECT * FROM severidades")
    suspend fun getAll(): List<Severidad>

    @Query("SELECT * FROM severidades WHERE Id_Severidad = :id LIMIT 1")
    suspend fun getById(id: String): Severidad?
}

