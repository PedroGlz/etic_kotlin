package com.example.etic.data.local.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.example.etic.data.local.views.VistaUbicacionArbol

@Dao
interface VistaUbicacionArbolDao {
    @RawQuery
    fun getAllInternal(query: SupportSQLiteQuery): List<VistaUbicacionArbol>

    @RawQuery
    fun getByInspeccionInternal(query: SupportSQLiteQuery): List<VistaUbicacionArbol>

    fun getAll(): List<VistaUbicacionArbol> =
        getAllInternal(SimpleSQLiteQuery("SELECT * FROM vista_ubicaciones_arbol"))

    fun getByInspeccion(inspId: String): List<VistaUbicacionArbol> =
        getByInspeccionInternal(
            SimpleSQLiteQuery(
                "SELECT * FROM vista_ubicaciones_arbol WHERE Id_Inspeccion = ?",
                arrayOf(inspId)
            )
        )
}
