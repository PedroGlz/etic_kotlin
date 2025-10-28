package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Problema

@Dao
interface ProblemaDao {

    @Query("SELECT * FROM problemas")
    suspend fun getAll(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion")
    suspend fun getByInspeccion(idInspeccion: String): List<Problema>
}

