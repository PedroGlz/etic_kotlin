package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.GrupoSitios

@Dao
interface GrupoSitiosDao {
    @Query("SELECT * FROM grupos_sitios WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<GrupoSitios>

    @Query("SELECT * FROM grupos_sitios WHERE Id_Grupo_Sitios = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): GrupoSitios?
}

