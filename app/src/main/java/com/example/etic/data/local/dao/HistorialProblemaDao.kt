package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.HistorialProblema

@Dao
interface HistorialProblemaDao {
    @Query("SELECT * FROM historial_problemas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<HistorialProblema>

    @Query("SELECT * FROM historial_problemas WHERE Id_Historial_Problema = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): HistorialProblema?
}

