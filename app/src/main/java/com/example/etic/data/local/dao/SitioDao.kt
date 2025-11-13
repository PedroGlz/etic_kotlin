package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Sitio

@Dao
interface SitioDao {
    @Query("SELECT * FROM sitios WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Sitio>

    @Query("SELECT * FROM sitios WHERE Id_Sitio = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Sitio?
}

