package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.etic.data.local.entities.Recomendacion

@Dao
interface RecomendacionDao {
    @Query("SELECT * FROM recomendaciones WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Recomendacion>

    @Query("SELECT * FROM recomendaciones WHERE Id_Recomendacion = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Recomendacion?

    @Query(
        """
        SELECT *
        FROM recomendaciones
        WHERE lower(trim(Recomendacion)) = lower(trim(:name))
          AND (:tipoInspeccion IS NULL OR Id_Tipo_Inspeccion = :tipoInspeccion)
        LIMIT 1
        """
    )
    suspend fun findByNameAndTipoInspeccion(name: String, tipoInspeccion: String?): Recomendacion?

    @Query(
        """
        SELECT Id_Recomendacion AS idRecomendacion, Recomendacion AS recomendacion, Id_Tipo_Inspeccion AS idTipoInspeccion
        FROM recomendaciones
        WHERE Estatus = 'Activo'
          AND Id_Tipo_Inspeccion = :tipoInspeccion
        ORDER BY Recomendacion COLLATE NOCASE
        """
    )
    suspend fun getByTipoInspeccion(tipoInspeccion: String): List<RecomendacionWithTipoInspeccion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Recomendacion)
}

data class RecomendacionWithTipoInspeccion(
    val idRecomendacion: String,
    val recomendacion: String?,
    val idTipoInspeccion: String?
)
