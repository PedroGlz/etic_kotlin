package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.etic.data.local.entities.Problema

@Dao
interface ProblemaDao {

    @Query("SELECT * FROM problemas")
    suspend fun getAll(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Estatus = 'Activo'")
    suspend fun getAllActivos(): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion")
    suspend fun getByInspeccion(idInspeccion: String): List<Problema>

    @Query("SELECT * FROM problemas WHERE Id_Inspeccion = :idInspeccion AND Estatus = 'Activo'")
    suspend fun getByInspeccionActivos(idInspeccion: String): List<Problema>

    @Query(
        """
        SELECT Numero_Problema FROM problemas
        WHERE Id_Inspeccion = :idInspeccion
          AND Id_Tipo_Inspeccion = :idTipoInspeccion
          AND Estatus = 'Activo'
        ORDER BY Numero_Problema DESC
        LIMIT 1
        """
    )
    suspend fun getLastNumberByInspectionAndType(
        idInspeccion: String,
        idTipoInspeccion: String
    ): Int?
}
