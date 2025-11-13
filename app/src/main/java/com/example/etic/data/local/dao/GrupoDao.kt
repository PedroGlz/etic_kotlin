package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Grupo

@Dao
interface GrupoDao {
    @Query("SELECT * FROM grupos WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Grupo>

    @Query("SELECT * FROM grupos WHERE Id_Grupo = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Grupo?
}

