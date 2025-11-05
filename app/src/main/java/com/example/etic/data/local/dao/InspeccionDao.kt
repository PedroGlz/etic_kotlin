package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Inspeccion

@Dao
interface InspeccionDao {
    @Query("SELECT * FROM inspecciones")
    suspend fun getAll(): List<Inspeccion>

    @Query("SELECT * FROM inspecciones WHERE Id_Inspeccion = :id LIMIT 1")
    suspend fun getById(id: String): Inspeccion?
}

