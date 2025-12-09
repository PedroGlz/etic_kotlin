package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Falla

@Dao
interface FallaDao {
    @Query("SELECT * FROM fallas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Falla>

    @Query("SELECT * FROM fallas WHERE Id_Falla = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): Falla?

    @Query(
        """
        SELECT f.Id_Falla AS idFalla, f.Falla AS falla, tf.Id_Tipo_Inspeccion AS idTipoInspeccion
        FROM fallas f
        LEFT JOIN tipo_fallas tf ON tf.Id_Tipo_Falla = f.Id_Tipo_Falla
        WHERE f.Estatus = 'Activo'
        """
    )
    suspend fun getAllWithTipoInspeccion(): List<FallaWithTipoInspeccion>

    @Query(
        """
        SELECT f.Id_Falla AS idFalla, f.Falla AS falla, tf.Id_Tipo_Inspeccion AS idTipoInspeccion
        FROM fallas f
        LEFT JOIN tipo_fallas tf ON tf.Id_Tipo_Falla = f.Id_Tipo_Falla
        WHERE f.Estatus = 'Activo'
        AND tf.Id_Tipo_Inspeccion = :tipoInspeccion
        """
    )
    suspend fun getByTipoInspeccion(tipoInspeccion: String): List<FallaWithTipoInspeccion>
}

data class FallaWithTipoInspeccion(
    val idFalla: String,
    val falla: String?,
    val idTipoInspeccion: String?
)
