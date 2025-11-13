package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Cliente

@Dao
interface ClienteDao {
    @Query("SELECT * FROM clientes WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Cliente>

    @Query("SELECT * FROM clientes WHERE Id_Cliente = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Cliente?
}

