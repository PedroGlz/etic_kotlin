package com.example.etic.data.local.queries

import com.example.etic.data.local.AppDatabase

data class CurrentInspectionInfo(
    val idInspeccion: String,
    val idSitio: String?,
    val idCliente: String?,
    val idGrupoSitios: String?,
    val idStatusInspeccion: String?,
    val noInspeccion: Int?,
    val nombreCliente: String?,
    val nombreSitio: String?,
    val nombreGrupoSitio: String?
)

fun getCurrentInspectionInfo(db: AppDatabase): CurrentInspectionInfo? {
    val sql = """
        SELECT 
            i.Id_Inspeccion      AS idInspeccion,
            i.Id_Sitio           AS idSitio,
            i.Id_Cliente         AS idCliente,
            i.Id_Grupo_Sitios    AS idGrupoSitios,
            i.Id_Status_Inspeccion AS idStatusInspeccion,
            i.No_Inspeccion      AS noInspeccion,
            c.Razon_Social       AS nombreCliente,
            s.Sitio              AS nombreSitio,
            g.Grupo              AS nombreGrupoSitio
        FROM inspecciones i
        LEFT JOIN clientes       c ON c.Id_Cliente = i.Id_Cliente
        LEFT JOIN sitios         s ON s.Id_Sitio   = i.Id_Sitio
        LEFT JOIN grupos_sitios  g ON g.Id_Grupo_Sitios = i.Id_Grupo_Sitios
        WHERE i.No_Inspeccion IS NOT NULL
        ORDER BY i.No_Inspeccion DESC
        LIMIT 1
    """.trimIndent()

    val dbh = db.openHelper.writableDatabase
    dbh.query(sql).use { cursor ->
        if (!cursor.moveToFirst()) return null
        fun col(name: String): Int = cursor.getColumnIndex(name)
        val idInspeccion = cursor.getString(col("idInspeccion"))
        val idSitio = cursor.getString(col("idSitio")).let { it }
        val idCliente = cursor.getString(col("idCliente")).let { it }
        val idGrupoSitios = cursor.getString(col("idGrupoSitios")).let { it }
        val idStatusInspeccion = cursor.getString(col("idStatusInspeccion")).let { it }
        val noInspeccion = if (!cursor.isNull(col("noInspeccion"))) cursor.getInt(col("noInspeccion")) else null
        val nombreCliente = cursor.getString(col("nombreCliente")).let { it }
        val nombreSitio = cursor.getString(col("nombreSitio")).let { it }
        val nombreGrupoSitio = cursor.getString(col("nombreGrupoSitio")).let { it }

        return CurrentInspectionInfo(
            idInspeccion = idInspeccion,
            idSitio = idSitio,
            idCliente = idCliente,
            idGrupoSitios = idGrupoSitios,
            idStatusInspeccion = idStatusInspeccion,
            noInspeccion = noInspeccion,
            nombreCliente = nombreCliente,
            nombreSitio = nombreSitio,
            nombreGrupoSitio = nombreGrupoSitio
        )
    }
}

