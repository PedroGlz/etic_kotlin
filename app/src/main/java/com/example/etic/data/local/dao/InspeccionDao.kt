package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.example.etic.data.local.entities.Inspeccion

@Dao
interface InspeccionDao {
    @Query("SELECT * FROM inspecciones WHERE Estatus = 'Activo'")
    suspend fun getAll(): List<Inspeccion>

    @Query("SELECT * FROM inspecciones WHERE Id_Inspeccion = :id AND Estatus = 'Activo' LIMIT 1")
    suspend fun getById(id: String): Inspeccion?

    @Update
    suspend fun update(item: Inspeccion)

    @Query("UPDATE inspecciones SET IR_Imagen_Inicial = :ir, DIG_Imagen_Inicial = :dig WHERE Id_Inspeccion = :id")
    suspend fun updateInitialImages(id: String, ir: String?, dig: String?)

    @Query("UPDATE inspecciones SET Codigo_Barras_Inicial = :codigoInicial WHERE Id_Inspeccion = :id")
    suspend fun updateInitialBarcode(id: String, codigoInicial: String?)
}
