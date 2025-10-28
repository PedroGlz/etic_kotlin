package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.LineaBase

@Dao
interface LineaBaseDao {

    @Query("SELECT * FROM linea_base")
    suspend fun getAll(): List<LineaBase>

    @Query("SELECT * FROM linea_base WHERE Id_Inspeccion = :idInspeccion")
    suspend fun getByInspeccion(idInspeccion: String): List<LineaBase>
}

