package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Query
import com.example.etic.data.local.entities.Ubicacion

@Dao
interface UbicacionDao {

    @Query("SELECT * FROM ubicaciones WHERE Estatus = 'Activo' ORDER BY Fecha_Creacion ASC")
    suspend fun getAllActivas(): List<Ubicacion>

    @Query("SELECT * FROM ubicaciones ORDER BY Fecha_Creacion ASC")
    suspend fun getAll(): List<Ubicacion>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(ubicacion: Ubicacion)

    @Query("SELECT * FROM ubicaciones WHERE Id_Ubicacion = :id LIMIT 1")
    suspend fun getById(id: String): Ubicacion?

    @Update
    suspend fun update(ubicacion: Ubicacion)
}
