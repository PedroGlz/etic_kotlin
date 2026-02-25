package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.etic.data.local.entities.TipoFalla

@Dao
interface TipoFallaDao {
    @Query("SELECT * FROM tipo_fallas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<TipoFalla>

    @Query("SELECT * FROM tipo_fallas WHERE Id_Tipo_Falla = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): TipoFalla?

    @Query("SELECT * FROM tipo_fallas WHERE Id_Tipo_Inspeccion = :tipoInspeccion AND Estatus = 'Activo' LIMIT 1")
    suspend fun findFirstByTipoInspeccion(tipoInspeccion: String): TipoFalla?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TipoFalla)
}
