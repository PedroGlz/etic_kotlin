package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.etic.data.local.entities.CausaPrincipal

@Dao
interface CausaPrincipalDao {
    @Query("SELECT * FROM causa_principal WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<CausaPrincipal>

    @Query("SELECT * FROM causa_principal WHERE Id_Causa_Raiz = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getByIdActivo(id: String): CausaPrincipal?

    @Query(
        """
        SELECT *
        FROM causa_principal
        WHERE lower(trim(Causa_Raiz)) = lower(trim(:name))
          AND (:tipoInspeccion IS NULL OR Id_Tipo_Inspeccion = :tipoInspeccion)
        LIMIT 1
        """
    )
    suspend fun findByNameAndTipoInspeccion(name: String, tipoInspeccion: String?): CausaPrincipal?

    @Query(
        """
        SELECT Id_Causa_Raiz AS idCausaRaiz, Causa_Raiz AS causaRaiz, Id_Tipo_Inspeccion AS idTipoInspeccion
        FROM causa_principal
        WHERE Estatus = 'Activo'
          AND Id_Tipo_Inspeccion = :tipoInspeccion
        ORDER BY Causa_Raiz COLLATE NOCASE
        """
    )
    suspend fun getByTipoInspeccion(tipoInspeccion: String): List<CausaPrincipalWithTipoInspeccion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CausaPrincipal)
}

data class CausaPrincipalWithTipoInspeccion(
    val idCausaRaiz: String,
    val causaRaiz: String?,
    val idTipoInspeccion: String?
)
