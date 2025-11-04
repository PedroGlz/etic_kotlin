package com.example.etic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.etic.data.local.dao.UsuarioDao
import com.example.etic.data.local.dao.ProblemaDao
import com.example.etic.data.local.dao.LineaBaseDao
import com.example.etic.data.local.dao.UbicacionDao
import com.example.etic.data.local.dao.EstatusInspeccionDetDao
import com.example.etic.data.local.entities.Usuario
import com.example.etic.data.local.entities.Problema
import com.example.etic.data.local.entities.LineaBase
import com.example.etic.data.local.entities.Ubicacion
import com.example.etic.data.local.entities.EstatusInspeccionDet

@Database(
    entities = [
        Usuario::class,
        Problema::class,
        LineaBase::class,
        Ubicacion::class,
        EstatusInspeccionDet::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun problemaDao(): ProblemaDao
    abstract fun lineaBaseDao(): LineaBaseDao
    abstract fun ubicacionDao(): UbicacionDao
    abstract fun estatusInspeccionDetDao(): EstatusInspeccionDetDao
}
