package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Fabricante

@Dao
interface FabricanteDao {
    @Query("SELECT * FROM fabricantes WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Fabricante>

    @Query("SELECT * FROM fabricantes")
    suspend fun getAll(): List<Fabricante>
}

