package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.CausaPrincipal

@Dao
interface CausaPrincipalDao {
    @Query("SELECT * FROM causa_principal WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<CausaPrincipal>

    @Query("SELECT * FROM causa_principal WHERE Id_Causa_Raiz = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): CausaPrincipal?
}

