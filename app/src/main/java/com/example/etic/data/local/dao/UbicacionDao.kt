package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Ubicacion

@Dao
interface UbicacionDao {

    @Query("SELECT * FROM ubicaciones WHERE Estatus = 'Activo'")
    suspend fun getAllActivas(): List<Ubicacion>

    @Query("SELECT * FROM ubicaciones")
    suspend fun getAll(): List<Ubicacion>
}

