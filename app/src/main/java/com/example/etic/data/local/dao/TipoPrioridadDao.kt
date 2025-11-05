package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.TipoPrioridad

@Dao
interface TipoPrioridadDao {
    @Query("SELECT * FROM tipo_prioridades WHERE Estatus = 'Activo'")
    suspend fun getAllActivas(): List<TipoPrioridad>

    @Query("SELECT * FROM tipo_prioridades")
    suspend fun getAll(): List<TipoPrioridad>
}

