package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Equipo

@Dao
interface EquipoDao {
    @Query("SELECT * FROM equipos WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Equipo>

    @Query("SELECT * FROM equipos")
    suspend fun getAll(): List<Equipo>

    @Query("SELECT * FROM equipos WHERE Id_Equipo = :id LIMIT 1")
    suspend fun getById(id: String): Equipo?
}

