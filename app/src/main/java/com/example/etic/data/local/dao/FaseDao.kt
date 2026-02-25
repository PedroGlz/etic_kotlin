package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.etic.data.local.entities.Fase

@Dao
interface FaseDao {
    @Query("SELECT * FROM fases WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Fase>

    @Query("SELECT * FROM fases WHERE Id_Fase = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Fase?

    @Query("SELECT * FROM fases WHERE lower(trim(Nombre_Fase)) = lower(trim(:name)) LIMIT 1")
    suspend fun findByName(name: String): Fase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Fase)
}
