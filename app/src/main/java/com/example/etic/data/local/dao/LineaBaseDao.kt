package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.LineaBase

@Dao
interface LineaBaseDao {

    @Query("SELECT * FROM linea_base ORDER BY Fecha_Creacion ASC")
    suspend fun getAll(): List<LineaBase>

    @Query("SELECT * FROM linea_base WHERE Estatus = 'Activo' ORDER BY Fecha_Creacion ASC")
    suspend fun getAllActivos(): List<LineaBase>

    @Query("SELECT * FROM linea_base WHERE Id_Inspeccion = :idInspeccion ORDER BY Fecha_Creacion ASC")
    suspend fun getByInspeccion(idInspeccion: String): List<LineaBase>

    @Query("SELECT * FROM linea_base WHERE Id_Inspeccion = :idInspeccion AND Estatus = 'Activo' ORDER BY Fecha_Creacion ASC")
    suspend fun getByInspeccionActivos(idInspeccion: String): List<LineaBase>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: LineaBase)

    @Update
    suspend fun update(item: LineaBase)

    @Query("SELECT EXISTS(SELECT 1 FROM linea_base WHERE Id_Ubicacion = :idUbicacion AND Estatus = 'Activo' LIMIT 1)")
    suspend fun existsActiveByUbicacion(idUbicacion: String): Boolean

    @Query("DELETE FROM linea_base WHERE Id_Linea_Base = :id")
    suspend fun deleteById(id: String)
}
